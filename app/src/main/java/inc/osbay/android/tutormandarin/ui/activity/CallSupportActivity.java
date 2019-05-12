package inc.osbay.android.tutormandarin.ui.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.twilio.video.AudioTrack;
import com.twilio.video.ConnectOptions;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteAudioTrackPublication;
import com.twilio.video.RemoteDataTrack;
import com.twilio.video.RemoteDataTrackPublication;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.Room;
import com.twilio.video.TwilioException;
import com.twilio.video.Video;
import com.twilio.video.VideoTrack;

import org.apache.log4j.Logger;

import java.util.Collections;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.ui.fragment.OnlineSupportFragment;
import inc.osbay.android.tutormandarin.util.Log4jHelper;

public class CallSupportActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_CONSULTANT_ID = "CallSupportActivity.EXTRA_CONSULTANT_ID";

    public static final String EXTRA_ACCESS_TOKEN = "CallSupportActivity.EXTRA_ACCESS_TOKEN";

    private static final int MIC_PERMISSION_REQUEST_CODE = 1;

    public static boolean sIsTalking;
    private String mAccessToken;
    private Logger mLog;
    private boolean isMuted;
    private LocalAudioTrack mLocalAudioTrack;
    private TextView mCallingTextView;
    private ImageView mMuteVoiceImageView;
    private ImageView mSpeakerOnImageView;
    private EarPhoneRegister mEarPhoneRegister;
    private AudioManager mAudioManager;
    /*
     * A Room represents communication between a local participant and one or more participants.
     */
    private Room mRoom;

    private int previousAudioMode;
    private boolean previousMicrophoneMute;
    private String mConsultantId;
    private String mParticipantIdentity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_support);

        ServerRequestManager requestManager = new ServerRequestManager(this);

        Intent intent = getIntent();
        if (intent != null) {
            mConsultantId = intent.getStringExtra(EXTRA_CONSULTANT_ID);

            mAccessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN);

            requestManager.savePhoneHistory(mConsultantId, new ServerRequestManager.OnRequestFinishedListener() {
                @Override
                public void onSuccess(Object result) {
                }

                @Override
                public void onError(ServerError err) {
                }
            });
        }

        LinearLayout llEndCall = findViewById(R.id.ll_end_call);
        llEndCall.setOnClickListener(this);

        ImageView mHideCallImageView = findViewById(R.id.imv_hide_call);
        mHideCallImageView.setOnClickListener(this);

        mSpeakerOnImageView = findViewById(R.id.imv_speaker_on);
        mSpeakerOnImageView.setOnClickListener(this);

        mMuteVoiceImageView = findViewById(R.id.imv_mute_voice);
        mMuteVoiceImageView.setOnClickListener(this);

        mCallingTextView = findViewById(R.id.tv_calling);

        /*
         * Enable changing the volume using the up/down keys during a conversation
         */
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        /*
         * Needed for setting/abandoning audio focus during call
         */
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (checkPermissionForSDCard()) {
                Log4jHelper log4jHelper = new Log4jHelper();
                if (mLog != null)
                    mLog = log4jHelper.getLogger("CallSupportActivity");
            }
        } else {
            Log4jHelper log4jHelper = new Log4jHelper();
            mLog = log4jHelper.getLogger("CallSupportActivity");
        }

        if (!checkPermissionForCameraAndMicrophone()) {
            requestPermissionForCameraAndMicrophone();
        } else {
            createAudioTracks();
            if (mAccessToken != null) {
                connectToRoom(mConsultantId);
            }
        }

        mEarPhoneRegister = new EarPhoneRegister();
    }

    private void createAudioTracks() {
        // Share your microphone
        mLocalAudioTrack = LocalAudioTrack.create(this, true);
    }

    private void connectToRoom(String roomName) {
        configureAudio(true);

        ConnectOptions.Builder connectOptionsBuilder = new ConnectOptions.Builder(mAccessToken)
                .roomName(roomName);

        /*
         * Add local audio track to connect options to share with participants.
         */
        if (mLocalAudioTrack != null) {
            connectOptionsBuilder
                    .audioTracks(Collections.singletonList(mLocalAudioTrack));
        }

        mRoom = Video.connect(this, connectOptionsBuilder.build(), roomListener());
    }

    /*
     * Called when participant joins the room
     */
    private void addParticipant(RemoteParticipant participant) {
        /*
         * This app only displays video for one additional participant per Room
         */
        mParticipantIdentity = participant.getIdentity();

        /*
         * Start listening for participant events
         */
        participant.setListener(participantListener());
    }

    /*
     * Called when participant leaves the room
     */
    private void removeParticipant(RemoteParticipant participant) {
        if (!participant.getIdentity().equals(mParticipantIdentity)) {
            return;
        }

        finish();
    }

    private void configureAudio(boolean enable) {
        if (enable) {
            previousAudioMode = mAudioManager.getMode();

            mAudioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

            /*
             * Use MODE_IN_COMMUNICATION as the default audio mode. It is required
             * to be in this mode when playout and/or recording starts for the best
             * possible VoIP performance. Some devices have difficulties with
             * speaker mode if this is not set.
             */
            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

            /*
             * Always disable microphone mute during a WebRTC call.
             */
            previousMicrophoneMute = mAudioManager.isMicrophoneMute();
            mAudioManager.setMicrophoneMute(false);
        } else {
            mAudioManager.setMode(previousAudioMode);
            mAudioManager.abandonAudioFocus(null);
            mAudioManager.setMicrophoneMute(previousMicrophoneMute);
        }
    }

    private RemoteParticipant.Listener participantListener() {
        return new RemoteParticipant.Listener() {
            @Override
            public void onAudioTrackPublished(RemoteParticipant remoteParticipant, RemoteAudioTrackPublication remoteAudioTrackPublication) {

            }

            @Override
            public void onAudioTrackUnpublished(RemoteParticipant remoteParticipant, RemoteAudioTrackPublication remoteAudioTrackPublication) {

            }

            @Override
            public void onAudioTrackSubscribed(RemoteParticipant remoteParticipant, RemoteAudioTrackPublication remoteAudioTrackPublication, RemoteAudioTrack remoteAudioTrack) {
                if (mLog != null)
                    mLog.error("onAudioTrackAdded");
            }

            @Override
            public void onAudioTrackSubscriptionFailed(RemoteParticipant remoteParticipant, RemoteAudioTrackPublication remoteAudioTrackPublication, TwilioException twilioException) {

            }

            @Override
            public void onAudioTrackUnsubscribed(RemoteParticipant remoteParticipant, RemoteAudioTrackPublication remoteAudioTrackPublication, RemoteAudioTrack remoteAudioTrack) {
                if (mLog != null)
                    mLog.error("onAudioTrackRemoved");
            }

            @Override
            public void onVideoTrackPublished(RemoteParticipant remoteParticipant, RemoteVideoTrackPublication remoteVideoTrackPublication) {

            }

            @Override
            public void onVideoTrackUnpublished(RemoteParticipant remoteParticipant, RemoteVideoTrackPublication remoteVideoTrackPublication) {

            }

            @Override
            public void onVideoTrackSubscribed(RemoteParticipant remoteParticipant, RemoteVideoTrackPublication remoteVideoTrackPublication, RemoteVideoTrack remoteVideoTrack) {
                if (mLog != null)
                    mLog.error("onVideoTrackAdded");
            }

            @Override
            public void onVideoTrackSubscriptionFailed(RemoteParticipant remoteParticipant, RemoteVideoTrackPublication remoteVideoTrackPublication, TwilioException twilioException) {

            }

            @Override
            public void onVideoTrackUnsubscribed(RemoteParticipant remoteParticipant, RemoteVideoTrackPublication remoteVideoTrackPublication, RemoteVideoTrack remoteVideoTrack) {
                if (mLog != null)
                    mLog.error("onVideoTrackRemoved");
            }

            @Override
            public void onDataTrackPublished(RemoteParticipant remoteParticipant, RemoteDataTrackPublication remoteDataTrackPublication) {

            }

            @Override
            public void onDataTrackUnpublished(RemoteParticipant remoteParticipant, RemoteDataTrackPublication remoteDataTrackPublication) {

            }

            @Override
            public void onDataTrackSubscribed(RemoteParticipant remoteParticipant, RemoteDataTrackPublication remoteDataTrackPublication, RemoteDataTrack remoteDataTrack) {

            }

            @Override
            public void onDataTrackSubscriptionFailed(RemoteParticipant remoteParticipant, RemoteDataTrackPublication remoteDataTrackPublication, TwilioException twilioException) {

            }

            @Override
            public void onDataTrackUnsubscribed(RemoteParticipant remoteParticipant, RemoteDataTrackPublication remoteDataTrackPublication, RemoteDataTrack remoteDataTrack) {

            }

            @Override
            public void onAudioTrackEnabled(RemoteParticipant remoteParticipant, RemoteAudioTrackPublication remoteAudioTrackPublication) {
                if (mLog != null)
                    mLog.error("onAudioTrackEnabled");
            }

            @Override
            public void onAudioTrackDisabled(RemoteParticipant remoteParticipant, RemoteAudioTrackPublication remoteAudioTrackPublication) {
                if (mLog != null)
                    mLog.error("onAudioTrackDisabled");
            }

            @Override
            public void onVideoTrackEnabled(RemoteParticipant remoteParticipant, RemoteVideoTrackPublication remoteVideoTrackPublication) {
                if (mLog != null)
                    mLog.error("onVideoTrackEnabled");
            }

            @Override
            public void onVideoTrackDisabled(RemoteParticipant remoteParticipant, RemoteVideoTrackPublication remoteVideoTrackPublication) {
                if (mLog != null)
                    mLog.error("onVideoTrackDisabled");
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLog != null)
            mLog.error("On Resume");

        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mEarPhoneRegister, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLog != null)
            mLog.error("On Pause");

        unregisterReceiver(mEarPhoneRegister);
    }

    @Override
    protected void onDestroy() {
        if (CallSupportActivity.sIsTalking) {
            hangUp();

            CallSupportActivity.sIsTalking = false;
        }

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MIC_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mAccessToken != null) {
                        createAudioTracks();
                        connectToRoom(mConsultantId);
                    }
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            this);

                    alertDialogBuilder.setTitle("Permission");
                    alertDialogBuilder.setMessage("Need to check permission in App System Setting");
                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            finish();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imv_speaker_on:
                if (mAudioManager.isSpeakerphoneOn()) {
                    mAudioManager.setSpeakerphoneOn(false);
                    mSpeakerOnImageView.setImageResource(R.drawable.ic_speaker_off);
                } else {
                    mAudioManager.setSpeakerphoneOn(true);
                    mSpeakerOnImageView.setImageResource(R.drawable.ic_speaker_on);
                }
                break;
            case R.id.imv_mute_voice:
                if (isMuted) {
                    isMuted = false;
                    mLocalAudioTrack.enable(true);
                    mMuteVoiceImageView.setImageResource(R.drawable.ic_mute_off);
                } else {
                    isMuted = true;
                    mLocalAudioTrack.enable(false);
                    mMuteVoiceImageView.setImageResource(R.drawable.ic_mute_on);
                }
                break;
            case R.id.imv_hide_call:
                moveTaskToBack(true);
                break;
            case R.id.ll_end_call:
                if (mRoom != null) {
                    OnlineSupportFragment.showError(107);
                    mRoom.disconnect();
                }

                CallSupportActivity.this.finish();
                break;
            default:
                break;
        }
    }

    /*
     * Room events listener
     */
    private Room.Listener roomListener() {
        return new Room.Listener() {
            @Override
            public void onConnected(Room room) {
                if (room.getRemoteParticipants().size() == 1) {
                    for (RemoteParticipant participant : room.getRemoteParticipants()) {
                        if (participant.getIdentity().equals(mConsultantId)) {
                            sIsTalking = true;

                            addParticipant(participant);
                            break;
                        } else {
                            sIsTalking = false;

                            OnlineSupportFragment.showError(106);
                            hangUp();
                        }
                    }
                } else {
                    sIsTalking = false;

                    OnlineSupportFragment.showError(106);
                    hangUp();
                }
                mCallingTextView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onConnectFailure(Room room, TwilioException e) {
                configureAudio(false);

                sIsTalking = false;

                if (CallSupportActivity.this != null) {
                    new AlertDialog.Builder(CallSupportActivity.this)
                            .setTitle(getString(R.string.os_dialog_connect_failure_error))
                            .setMessage(getString(R.string.os_dialog_connect_failure_msg))
                            .setPositiveButton(getString(R.string.os_dialog_connect_failure_ok), null)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    onBackPressed();
                                }
                            })
                            .create()
                            .show();
                }
            }

            @Override
            public void onDisconnected(Room room, TwilioException e) {
//                mLocalParticipant = null;
                // Only reinitialize the UI if disconnect was not called from onDestroy()
//                if (!disconnectedFromOnDestroy) {
//                    configureAudio(false);
//                    intializeUI();
//                    moveLocalVideoToPrimaryView();
//                }

                if (CallSupportActivity.this != null) {
                    CallSupportActivity.this.mRoom = null;

                    if (sIsTalking) {
                        sIsTalking = false;
                    }

                    configureAudio(false);

                    Intent mainActivity = new Intent(CallSupportActivity.this, MainActivity.class);
                    mainActivity.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    Bundle bundle = new Bundle();
                    bundle.putString("class_type", "normal");
                    mainActivity.putExtras(bundle);
                    startActivity(mainActivity);

                    CallSupportActivity.this.finish();
                }
            }

            @Override
            public void onParticipantConnected(Room room, RemoteParticipant participant) {
                if (mLog != null)
                    mLog.error("RemoteParticipant disconnected from room = " + room.getName() + "/" + room.getSid() +
                            ", participant - " + participant.getIdentity() + "/" + participant.getSid());

                addParticipant(participant);
            }

            @Override
            public void onParticipantDisconnected(Room room, RemoteParticipant participant) {
                if (mLog != null)
                    mLog.error("RemoteParticipant disconnected from room = " + room.getName() + "/" + room.getSid() +
                            ", participant - " + participant.getIdentity() + "/" + participant.getSid());

                if (participant.getIdentity().equals(mConsultantId)) {
                    OnlineSupportFragment.showError(107);
                    room.disconnect();
                }

//                removeParticipant(participant);
            }

            @Override
            public void onRecordingStarted(Room room) {
                /*
                 * Indicates when media shared to a Room is being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
//                Log.d(TAG, "onRecordingStarted");
            }

            @Override
            public void onRecordingStopped(Room room) {
                /*
                 * Indicates when media shared to a Room is no longer being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
//                Log.d(TAG, "onRecordingStopped");
            }
        };
    }

    private void hangUp() {
        if (mRoom != null)
            mRoom.disconnect();

        CallSupportActivity.this.finish();
    }

    private boolean checkPermissionForCameraAndMicrophone() {
        int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return resultMic == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionForCameraAndMicrophone() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(this,
                    R.string.os_request_permission_msg,
                    Toast.LENGTH_LONG).show();
            finish();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MIC_PERMISSION_REQUEST_CODE);
        }
    }

    private boolean checkPermissionForSDCard() {
        int resultReadSD = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int resultWriteSD = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (resultReadSD == PackageManager.PERMISSION_GRANTED) && (resultWriteSD == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onBackPressed() {
        if (sIsTalking) {
            moveTaskToBack(true);
        }
    }

    private class EarPhoneRegister extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                    int state = intent.getIntExtra("state", -1);
                    switch (state) {
                        case 0:
                            if (mAudioManager.isSpeakerphoneOn()) {
                                mAudioManager.setSpeakerphoneOn(true);
                                mSpeakerOnImageView.setImageResource(R.drawable.ic_speaker_on);
                            } else {
                                mAudioManager.setSpeakerphoneOn(false);
                                mSpeakerOnImageView.setImageResource(R.drawable.ic_speaker_off);
                            }
                            break;
                        case 1:
                            if (mAudioManager.isSpeakerphoneOn()) {
                                mSpeakerOnImageView.setImageResource(R.drawable.ic_speaker_off);
                            }
                            mAudioManager.setSpeakerphoneOn(false);
                            break;
                        default:
                            if (mLog != null)
                                mLog.error("Headset error.");
                            break;
                    }
                }
            }
        }
    }
}
