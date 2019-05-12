package inc.osbay.android.tutormandarin.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.flurry.android.FlurryAgent;
import com.twilio.video.CameraCapturer;
import com.twilio.video.ConnectOptions;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteAudioTrackPublication;
import com.twilio.video.RemoteDataTrack;
import com.twilio.video.RemoteDataTrackPublication;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.Room;
import com.twilio.video.RoomState;
import com.twilio.video.TwilioException;
import com.twilio.video.Video;
import com.twilio.video.VideoTrack;
import com.twilio.video.VideoView;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.TMApplication;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.database.TutorAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.sdk.model.Booking;
import inc.osbay.android.tutormandarin.sdk.model.ChatMessage;
import inc.osbay.android.tutormandarin.sdk.model.Tutor;
import inc.osbay.android.tutormandarin.service.MessengerService;
import inc.osbay.android.tutormandarin.ui.fragment.ClassroomFAQFragment;
import inc.osbay.android.tutormandarin.ui.fragment.OnlineSupportFragment;
import inc.osbay.android.tutormandarin.util.CameraCapturerCompat;
import inc.osbay.android.tutormandarin.util.Log4jHelper;
import inc.osbay.android.tutormandarin.util.WSMessageClient;

public class ClassRoomActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_BOOKING = "ClassRoomActivity.EXTRA_BOOKING";
    public static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = ClassRoomActivity.class.getSimpleName();
    private static final int MAX_CLICK_DURATION = 500;
    private static final int MAX_CLICK_DISTANCE = 5;
    private static boolean isTalking;
    private static WebView.WebViewTransport instance;
    private static WebView mWebView;
    private static List<ChatMessage> mMessageList = new ArrayList<>();
    private static MessageAdapter mMessageAdapter;
    private static WSMessageClient mWSMessageClient;
    private static Booking mBooking;
    private static Account mAccount;
    private static Tutor mTutor;
    private static RecyclerView rvChatMessages;
    private static MediaPlayer mediaPlayer;
    private static int audioPos;
    private static String mp3Url;
    private EditText mInputEditText;
    private ImageView mLocalVideoHideImageView;
    private RelativeLayout mClassStatusRelativeLayout;
    private TextView mClassStatusTextView;
    private Logger mLog;
    private AudioManager mAudioManager;
    private Messenger mMessenger;
    private ServerRequestManager mServerRequestManager;
    private EarPhoneRegister mEarPhoneRegister;
    private LocalVideoTrack mLocalVideoTrack;
    private LocalAudioTrack mLocalAudioTrack;
    private VideoView mLocalVideoView;
    private VideoView mRemoteVideoView;
    private SimpleDraweeView mTutorPhotoDraweeView;
    private LocalParticipant mLocalParticipant;
    private boolean pauseVideo;
    private String tutorParticipant;
    private int mPreviousAudioMode;
    private boolean mPreviousMicrophoneMute;
    private boolean disconnectedFromOnDestroy;
    private RelativeLayout mRlWhiteBoard;
    private float dY;
    private float dX;
    private VideoView mVideoView;
    private RelativeLayout classRoomLayout;
    private float newY;
    private float newX;
    private int screenHight;
    private int screenWidth;
    private long pressStartTime;
    private float pressX;
    private float pressY;
    private float distance;
    private RelativeLayout menuRL;
    private ImageView assistantImg;
    private LinearLayout onlineSupportLL;
    private LinearLayout faqLL;
    private int remoteParticipantCount;
    /*
     * A Room represents communication between a local participant and one or more participants.
     */
    private Room mRoom;

    private CameraCapturerCompat mCameraCapturerCompat;

    public static WebView.WebViewTransport getInstance() {
        if (instance == null) {
            instance = mWebView.new WebViewTransport();
        }
        return instance;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView(String studentId, String classroomId) {
        mWebView = new WebView(getApplicationContext());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mWebView.loadUrl(url);
                return true;
            }
        });

        mWebView.loadUrl(CommonConstant.WHITE_BOARD_URL + "?classroomId=" +
                classroomId + "&studentName=S_"
                + studentId);

        getInstance().setWebView(mWebView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_room);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHight = displaymetrics.heightPixels;
        screenWidth = displaymetrics.widthPixels;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TextView tvTitle = findViewById(R.id.tv_toolbar_title);
        tvTitle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Montserrat-Bold.ttf"));
        tvTitle.setText(getString(R.string.cr_title));

        TextView tvBack = findViewById(R.id.tv_back);
        tvBack.setOnClickListener(this);

        AccountAdapter accountAdapter = new AccountAdapter(this);
        TutorAdapter tutorAdapter = new TutorAdapter(this);

        Intent intent = getIntent();
        if (intent != null) {
            mBooking = (Booking) intent.getSerializableExtra(EXTRA_BOOKING);
            mAccount = accountAdapter.getAccountById(mBooking.getStudentId());
            mTutor = tutorAdapter.getTutorById(mBooking.getTutorId());
        }

        mClassStatusTextView = findViewById(R.id.tv_class_status);
        mClassStatusRelativeLayout = findViewById(R.id.rl_class_status);
        mInputEditText = findViewById(R.id.edt_input_text);
        mLocalVideoView = findViewById(R.id.vv_local_video);
        mRemoteVideoView = findViewById(R.id.twilio_vv_remote_video);
        mRlWhiteBoard = findViewById(R.id.rl_white_board);
        mVideoView = findViewById(R.id.twilio_remote_video);
        classRoomLayout = findViewById(R.id.classroom_layout);
        menuRL = findViewById(R.id.rl_assistant_pop_up);
        onlineSupportLL = findViewById(R.id.ll_assistant_online_support);
        faqLL = findViewById(R.id.ll_assistant_faq);
        if (onlineSupportLL != null) {
            onlineSupportLL.setOnClickListener(this);
        }
        if (faqLL != null) {
            faqLL.setOnClickListener(this);
        }
        assistantImg = findViewById(R.id.assistant_menu);
        if (assistantImg != null) {
            assistantImg.setOnClickListener(this);
        }

        TextView mSendButtonTextView = findViewById(R.id.tv_send_button);
        if (mSendButtonTextView != null) {
            mSendButtonTextView.setOnClickListener(this);
        }

        FloatingActionButton fabWhiteBoard = findViewById(R.id.fab_white_board);
        if (fabWhiteBoard != null) {
            fabWhiteBoard.setOnClickListener(this);
        }

        rvChatMessages = findViewById(R.id.rv_chat_messages);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        if (rvChatMessages != null) {
            rvChatMessages.setLayoutManager(layoutManager);
        }

        mMessageAdapter = new MessageAdapter();
        if (rvChatMessages != null) {
            rvChatMessages.setAdapter(mMessageAdapter);
        }

        mTutorPhotoDraweeView = findViewById(R.id.sdv_tutor_photo);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(Uri.parse("res:///" + R.drawable.spinner))
                .setAutoPlayAnimations(true)
                .build();
        mTutorPhotoDraweeView.setController(controller);

        mLocalVideoHideImageView = findViewById(R.id.imv_local_video_hide);
        if (mLocalVideoHideImageView != null) {
            mLocalVideoHideImageView.setOnClickListener(this);
        }

        mEarPhoneRegister = new EarPhoneRegister();

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
                    mLog = log4jHelper.getLogger("ClassroomActivity");
            }
        } else {
            Log4jHelper log4jHelper = new Log4jHelper();
            mLog = log4jHelper.getLogger("ClassroomActivity");
        }

        if (!isTalking) {
            loadWebView(mBooking.getStudentId(), mBooking.getClassRoomId());

            mWSMessageClient = ((TMApplication) getApplication()).getWSMessageClient();
            mServerRequestManager = new ServerRequestManager(getApplicationContext());

            if (!checkPermissionForCameraAndMicrophone()) {
                requestPermissionForCameraAndMicrophone();
            } else {
                initializeTwilioSDK();
            }
        } else {
            createAudioAndVideoTracks();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mLog != null)
            mLog.debug("On Resume");

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mEarPhoneRegister, intentFilter);
        mMessenger = new Messenger(new IncomingHandler());
        mWSMessageClient.addMessenger(mMessenger);

        if (mLocalVideoTrack == null && checkPermissionForCameraAndMicrophone()) {
            mCameraCapturerCompat = new CameraCapturerCompat(this, CameraCapturer.CameraSource.FRONT_CAMERA);
            mLocalVideoTrack = LocalVideoTrack.create(this, true, mCameraCapturerCompat.getVideoCapturer());
            mLocalVideoTrack.addRenderer(mLocalVideoView);

            /*
             * If connected to a Room then share the local video track.
             */
            if (mLocalParticipant != null) {
                mLocalParticipant.publishTrack(mLocalVideoTrack);
            }
        }
    }

    @Override
    public void onPause() {
        if (mLog != null)
            mLog.debug("On Pause");

        unregisterReceiver(mEarPhoneRegister);
        mWSMessageClient.removeMessenger(mMessenger);
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (!isTalking) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            finishActivity(1010);
            mMessageList.clear();
        }

        /*
         * Always disconnect from the room before leaving the Activity to
         * ensure any memory allocated to the Room resource is freed.
         */
        if (mRoom != null && mRoom.getState() != RoomState.DISCONNECTED) {
            mRoom.disconnect();
            disconnectedFromOnDestroy = true;
        }

        /*
         * Release the local audio and video tracks ensuring any memory allocated to audio
         * or video is freed.
         */
        if (mLocalAudioTrack != null) {
            mLocalAudioTrack.release();
            mLocalAudioTrack = null;
        }
        if (mLocalVideoTrack != null) {
            mLocalVideoTrack.release();
            mLocalVideoTrack = null;
        }
        super.onDestroy();
    }

    private void showHideMenu() {
        if (menuRL.getVisibility() == View.VISIBLE)
            menuRL.setVisibility(View.GONE);
        else
            menuRL.setVisibility(View.VISIBLE);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_assistant_online_support:
                FlurryAgent.logEvent("Click Online Support from classroom");
                menuRL.setVisibility(View.GONE);

                Intent intent1 = new Intent(this, ClassroomAssistantActivity.class);
                intent1.putExtra("fragment", OnlineSupportFragment.class.getSimpleName());
                startActivity(intent1);
                break;

            case R.id.ll_assistant_faq:
                FlurryAgent.logEvent("Click FAQ from classroom");
                menuRL.setVisibility(View.GONE);

                Intent intent2 = new Intent(this, ClassroomAssistantActivity.class);
                intent2.putExtra("fragment", ClassroomFAQFragment.class.getSimpleName());
                startActivity(intent2);
                break;

            case R.id.assistant_menu:
                showHideMenu();
                break;

            case R.id.tv_back:
                onBackPressed();
                break;
            case R.id.tv_send_button:
                String text = mInputEditText.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    return;
                }

                for (RemoteParticipant remoteParticipant : mRoom.getRemoteParticipants()) {
                    android.os.Message message = android.os.Message.obtain(null, MessengerService.MSG_IM_CLASS);
                    if (!remoteParticipant.getIdentity().equals(mBooking.getStudentId())) {
                        Bundle bundle = new Bundle();
                        if (remoteParticipant.getIdentity().equals(mTutor.getTutorId())) {
                            bundle.putString("send_to", "T_" + remoteParticipant.getIdentity());
                        } else {
                            bundle.putString("send_to", "S_" + remoteParticipant.getIdentity());
                        }
                        bundle.putString("message_content", text);
                        bundle.putString("account_name", mAccount.getFirstName() + " " + mAccount.getLastName());
                        bundle.putString("classroom_id", mBooking.getClassRoomId());
                        message.setData(bundle);
                        try {
                            mWSMessageClient.sendMessage(message);
                            rvChatMessages.smoothScrollToPosition(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                mInputEditText.setText("");

                break;
            case R.id.fab_white_board:
                FlurryAgent.logEvent("Open whiteboard");

                menuRL.setVisibility(View.GONE);
                classRoomLayout.setVisibility(View.GONE);
                mLocalVideoView.setVisibility(View.GONE);
                mRemoteVideoView.setVisibility(View.GONE);
                mRlWhiteBoard.setVisibility(View.VISIBLE);
                mVideoView.setVisibility(View.VISIBLE);

                mRlWhiteBoard.removeAllViews();
                mRlWhiteBoard.addView(getInstance().getWebView());

                mVideoView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        switch (motionEvent.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN:
                                pressStartTime = System.currentTimeMillis();
                                dX = view.getX() - motionEvent.getRawX();
                                dY = view.getY() - motionEvent.getRawY();
                                pressX = motionEvent.getX();
                                pressY = motionEvent.getY();
                                break;
                            case MotionEvent.ACTION_MOVE:
                                distance = distance(pressX, pressY, motionEvent.getX(), motionEvent.getY());
                                if (distance > MAX_CLICK_DISTANCE) {
                                    newX = motionEvent.getRawX() + dX;
                                    newY = motionEvent.getRawY() + dY;
                                    if (newX <= 0 && newY <= 0) {
                                        view.setX(0);
                                        view.setY(0);
                                    } else if (newX <= 0 && newY >= screenHight - view.getHeight()) {
                                        view.setX(0);
                                        view.setY(screenHight - view.getHeight());
                                    } else if (newX >= screenWidth - view.getWidth() && newY <= 0) {
                                        view.setX(screenWidth - view.getWidth());
                                        view.setY(0);
                                    } else if (newX >= screenWidth - view.getWidth() && newY >= screenHight - view.getHeight()) {
                                        view.setX(screenWidth - view.getWidth());
                                        view.setY(screenHight - view.getHeight());
                                    } else if (newX <= 0 && (newY >= 0 && newY <= screenHight - view.getHeight())) {
                                        view.setX(0);
                                        view.setY(newY);
                                    } else if ((newX >= 0 && newX <= screenWidth - view.getWidth()) && newY <= 0) {
                                        view.setX(newX);
                                        view.setY(0);
                                    } else if (newX >= screenWidth - view.getWidth() && (newY >= 0 && newY <= screenHight - view.getHeight())) {
                                        view.setX(screenWidth - view.getWidth());
                                        view.setY(newY);
                                    } else if ((newX >= 0 && newX <= screenWidth - view.getWidth()) && newY >= screenHight - view.getHeight()) {
                                        view.setX(newX);
                                        view.setY(screenHight - view.getHeight());
                                    } else {
                                        view.setX(newX);
                                        view.setY(newY);
                                    }
                                }
                                break;
                            case MotionEvent.ACTION_UP:
                                long pressDuration = System.currentTimeMillis() - pressStartTime;
                                Log.i("pressDuration", String.valueOf(pressDuration));
                                Log.i("distance", String.valueOf(distance));

                                if (pressDuration < MAX_CLICK_DURATION && distance < MAX_CLICK_DISTANCE) {
                                    classRoomLayout.setVisibility(View.VISIBLE);
                                    mLocalVideoView.setVisibility(View.VISIBLE);
                                    mRemoteVideoView.setVisibility(View.VISIBLE);
                                    mRlWhiteBoard.setVisibility(View.GONE);
                                    mVideoView.setVisibility(View.GONE);
                                    menuRL.setVisibility(View.GONE);
                                }
                                break;
                            default:
                                return false;
                        }
                        return true;
                    }
                });
                break;
            case R.id.imv_local_video_hide:
                pauseVideo = !pauseVideo;

                pauseVideo(pauseVideo);

                if (pauseVideo) {
                    mLocalVideoHideImageView.setImageDrawable(ContextCompat.getDrawable(ClassRoomActivity.this, R.drawable.ic_video_cam_off));
                } else {
                    mLocalVideoHideImageView.setImageDrawable(ContextCompat.getDrawable(ClassRoomActivity.this, R.drawable.ic_video_cam_off));
                }
                break;
            default:
                break;
        }
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float distanceInPx = (float) Math.sqrt(dx * dx + dy * dy);
        return distanceInPx;
    }

    private void pauseVideo(boolean pauseVideo) {
        if (mLocalVideoTrack != null) {
            mLocalVideoTrack.enable(!pauseVideo);
        } else {
            if (mLog != null)
                mLog.error("LocalVideoTrack is not present, unable to pause");
        }
    }

    private boolean checkPermissionForCameraAndMicrophone() {
        int resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return (resultCamera == PackageManager.PERMISSION_GRANTED) && (resultMic == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissionForCameraAndMicrophone() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(this, getString(R.string.cr_camera_permission), Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, CAMERA_MIC_PERMISSION_REQUEST_CODE);
        }
    }

    private boolean checkPermissionForSDCard() {
        int resultReadSD = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int resultWriteSD = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (resultReadSD == PackageManager.PERMISSION_GRANTED) && (resultWriteSD == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_MIC_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeTwilioSDK();
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
    public void onBackPressed() {
        if (isTalking) {
            AlertDialog confirm = new AlertDialog.Builder(ClassRoomActivity.this)
                    .setTitle(getString(R.string.cr_leave_room))
                    .setMessage(getString(R.string.cr_leave_room_msg))
                    .setPositiveButton(getString(R.string.cr_leave_room_leave), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(getApplicationContext(), TutorFeedbackActivity.class);
                            intent.putExtra(TutorFeedbackActivity.CLASSROOM_ID, mBooking.getClassRoomId());
                            startActivity(intent);

                            if (isTalking) {
                                hangUp();
                                isTalking = false;
                            }
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }
                    })
                    .setNegativeButton(getString(R.string.cr_leave_room_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FlurryAgent.logEvent("Try to exit but continue in class");
                        }
                    })
                    .create();
            confirm.show();
        } else {
            hangUp();
        }
    }

    private void hangUp() {

        if (mLocalParticipant != null)
            mLocalParticipant.unpublishTrack(mLocalVideoTrack);

        if (mRoom != null)
            mRoom.disconnect();

        ClassRoomActivity.this.finish();
    }

    private void initializeTwilioSDK() {
        mServerRequestManager.getTwilioAccessToken(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                if (ClassRoomActivity.this != null) {
                    String videoToken = String.valueOf(result);

                    createAudioAndVideoTracks();
                    if (mLocalAudioTrack != null && mLocalVideoTrack != null)
                        connectToRoom(mBooking.getClassRoomId(), videoToken);
                }
            }

            @Override
            public void onError(ServerError err) {
                if (ClassRoomActivity.this != null) {
                    new AlertDialog.Builder(ClassRoomActivity.this)
                            .setTitle(getString(R.string.cr_server_error))
                            .setMessage(err.getMessage())
                            .setPositiveButton(getString(R.string.cr_server_error_ok), null)
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
        });
    }

    private void connectToRoom(String roomName, String accessToken) {
        Log.i(TAG, "Classroom Name " + roomName + " Access Token" + accessToken);
        configureAudio(true);
        ConnectOptions.Builder connectOptionsBuilder = new ConnectOptions.Builder(accessToken)
                .roomName(roomName);
        /*ConnectOptions.Builder connectOptionsBuilder = new ConnectOptions.Builder(accessToken)
                .roomName("e1bd84eb-3206-41d9-b299-6ae44bd79e88");*/

        /*
         * Add local audio track to connect options to share with participants.
         */
        if (mLocalAudioTrack != null) {
            connectOptionsBuilder
                    .audioTracks(Collections.singletonList(mLocalAudioTrack));
        }

        /*
         * Add local video track to connect options to share with participants.
         */
        if (mLocalVideoTrack != null) {
            connectOptionsBuilder.videoTracks(Collections.singletonList(mLocalVideoTrack));
        }
        mRoom = Video.connect(this, connectOptionsBuilder.build(), roomListener());
    }

    private void configureAudio(boolean enable) {
        if (enable) {
            mPreviousAudioMode = mAudioManager.getMode();
            // Request audio focus before making any device switch.
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
            mPreviousMicrophoneMute = mAudioManager.isMicrophoneMute();
            mAudioManager.setMicrophoneMute(false);
        } else {
            mAudioManager.setMode(mPreviousAudioMode);
            mAudioManager.abandonAudioFocus(null);
            mAudioManager.setMicrophoneMute(mPreviousMicrophoneMute);
        }
    }

    /*
     * Room events listener
     */
    private Room.Listener roomListener() {
        return new Room.Listener() {
            @Override
            public void onConnected(Room room) {
                mLocalParticipant = room.getLocalParticipant();

                if (ClassRoomActivity.this != null) {
                    Message message = Message.obtain(null, MessengerService.MSG_SIGNAL);
                    Bundle bundle = new Bundle();
                    bundle.putString("tutor_id", "T_" + mBooking.getTutorId());
                    bundle.putString("classroom_id", mBooking.getClassRoomId());
                    message.setData(bundle);
                    try {
                        mWSMessageClient.sendMessage(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    for (RemoteParticipant participant : room.getRemoteParticipants()) {
                        isTalking = true;
                        //if (participant.getIdentity().equals(mBooking.getTutorId())) {
                        addParticipant(participant);
                        //}
                        mClassStatusRelativeLayout.setVisibility(View.GONE);
                        //break;
                    }

                    /*for (Iterator<RemoteParticipant> remoteParticipantIterator = room.getRemoteParticipants().iterator(); remoteParticipantIterator.hasNext(); ) {
                        RemoteParticipant participant = remoteParticipantIterator.next();
                        isTalking = true;
                        if (participant.getIdentity().equals(mBooking.getTutorId())) {
                            addParticipant(participant);
                        }
                        mClassStatusRelativeLayout.setVisibility(View.GONE);
                        break;
                    }*/

                    if (!isTalking) {
                        if (mTutor.getAvatar() != null) {
                            mTutorPhotoDraweeView.setImageURI(Uri.parse(mTutor.getAvatar()));
                        } else {
                            mTutorPhotoDraweeView.setImageURI(Uri.parse("res://" + R.drawable.img_pre_tutor));
                        }
                        mClassStatusTextView.setText(getString(R.string.cr_tutor_soon));
                    }

                }

            }

            @Override
            public void onConnectFailure(Room room, TwilioException e) {
                configureAudio(false);
            }

            @Override
            public void onDisconnected(Room room, TwilioException e) {

                if (e == null && mBooking.getBookingType() == Booking.Type.TRIAL) {
                    FlurryAgent.logEvent("Finished Trial Class");
                } else if (e != null) {
                    FlurryAgent.logEvent("Dropped class");
                }

                mLocalParticipant = null;
                ClassRoomActivity.this.mRoom = null;
                // Only reinitialize the UI if disconnect was not called from onDestroy()
                if (!disconnectedFromOnDestroy) {
                    configureAudio(false);
                }

                if (ClassRoomActivity.this != null) {
                    if (isTalking) {
                        isTalking = false;

                        Intent intent = new Intent(ClassRoomActivity.this, TutorFeedbackActivity.class);
                        intent.putExtra(TutorFeedbackActivity.CLASSROOM_ID, mBooking.getClassRoomId());
                        startActivity(intent);
                        ClassRoomActivity.this.finish();
                    }
                }
            }

            @Override
            public void onParticipantConnected(Room room, RemoteParticipant remoteParticipant) {
                addParticipant(remoteParticipant);
            }

            @Override
            public void onParticipantDisconnected(Room room, RemoteParticipant remoteParticipant) {
                removeParticipant(remoteParticipant);
            }

            @Override
            public void onRecordingStarted(Room room) {
                /*
                 * Indicates when media shared to a Room is being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
                Log.d(TAG, "onRecordingStarted");
            }

            @Override
            public void onRecordingStopped(Room room) {
                /*
                 * Indicates when media shared to a Room is no longer being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
                Log.d(TAG, "onRecordingStopped");
            }
        };
    }

    /*
     * Called when participant joins the room
     */
    private void addParticipant(RemoteParticipant participant) {
        if (participant.getIdentity().equals(mBooking.getTutorId())) {
            tutorParticipant = participant.getIdentity();
        }

        isTalking = true;

        mClassStatusRelativeLayout.setVisibility(View.GONE);

        /*
         * Add participant renderer
         */
        if (participant.getRemoteVideoTracks().size() > 0) {
            RemoteVideoTrackPublication remoteVideoTrackPublication =
                    participant.getRemoteVideoTracks().get(0);
            if (remoteVideoTrackPublication.isTrackSubscribed() /*&& participant.getIdentity().equals(mBooking.getTutorId())*/) {
                addParticipantVideo(remoteVideoTrackPublication.getRemoteVideoTrack());
            }
        }

        /*
         * Start listening for participant events
         */
        participant.setListener(participantListener());
    }

    /*
     * Set primary view as renderer for participant video track
     */
    private void addParticipantVideo(VideoTrack videoTrack) {
        videoTrack.addRenderer(mVideoView);
        videoTrack.addRenderer(mRemoteVideoView);
    }

    /*
     * Called when participant leaves the room
     */
    private void removeParticipant(RemoteParticipant participant) {
        if (!participant.getIdentity().equals(tutorParticipant)) {
            return;
        }

        /*
         * Remove participant renderer
         */
        if (participant.getRemoteVideoTracks().size() > 0) {
            RemoteVideoTrackPublication remoteVideoTrackPublication =
                    participant.getRemoteVideoTracks().get(0);
            if (remoteVideoTrackPublication.isTrackSubscribed() && participant.getIdentity().equals(tutorParticipant)) {
                removeParticipantVideo(remoteVideoTrackPublication.getRemoteVideoTrack());
            }
        }

        if (ClassRoomActivity.this != null) {
            if (isTalking) {
                isTalking = false;

                Intent intent = new Intent(ClassRoomActivity.this, TutorFeedbackActivity.class);
                intent.putExtra(TutorFeedbackActivity.CLASSROOM_ID, mBooking.getClassRoomId());
                startActivity(intent);
                ClassRoomActivity.this.finish();
            }
        }
    }

    private void removeParticipantVideo(VideoTrack videoTrack) {
        videoTrack.removeRenderer(mVideoView);
        videoTrack.removeRenderer(mRemoteVideoView);
    }

    private void createAudioAndVideoTracks() {
        // Share your microphone
        mLocalAudioTrack = LocalAudioTrack.create(this, true);

        // Share your camera
        mCameraCapturerCompat = new CameraCapturerCompat(this, CameraCapturer.CameraSource.FRONT_CAMERA);
        mLocalVideoTrack = LocalVideoTrack.create(this, true, mCameraCapturerCompat.getVideoCapturer());
        mLocalVideoView.setMirror(true);
        mLocalVideoTrack.addRenderer(mLocalVideoView);

        if (mAudioManager != null) {
            mAudioManager.setSpeakerphoneOn(true);
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

            }

            @Override
            public void onAudioTrackSubscriptionFailed(RemoteParticipant remoteParticipant, RemoteAudioTrackPublication remoteAudioTrackPublication, TwilioException twilioException) {

            }

            @Override
            public void onAudioTrackUnsubscribed(RemoteParticipant remoteParticipant, RemoteAudioTrackPublication remoteAudioTrackPublication, RemoteAudioTrack remoteAudioTrack) {

            }

            @Override
            public void onVideoTrackPublished(RemoteParticipant remoteParticipant, RemoteVideoTrackPublication remoteVideoTrackPublication) {

            }

            @Override
            public void onVideoTrackUnpublished(RemoteParticipant remoteParticipant, RemoteVideoTrackPublication remoteVideoTrackPublication) {

            }

            @Override

            public void onVideoTrackSubscribed(RemoteParticipant remoteParticipant, RemoteVideoTrackPublication remoteVideoTrackPublication, RemoteVideoTrack remoteVideoTrack) {
                if (remoteParticipant.getIdentity().equals(mBooking.getTutorId())) {
                    addParticipantVideo(remoteVideoTrack);
                }
            }

            @Override
            public void onVideoTrackSubscriptionFailed(RemoteParticipant remoteParticipant, RemoteVideoTrackPublication remoteVideoTrackPublication, TwilioException twilioException) {

            }

            @Override
            public void onVideoTrackUnsubscribed(RemoteParticipant remoteParticipant, RemoteVideoTrackPublication remoteVideoTrackPublication, RemoteVideoTrack remoteVideoTrack) {
                if (remoteParticipant.getIdentity().equals(tutorParticipant))
                    removeParticipantVideo(remoteVideoTrack);
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

            }

            @Override
            public void onAudioTrackDisabled(RemoteParticipant remoteParticipant, RemoteAudioTrackPublication remoteAudioTrackPublication) {

            }

            @Override
            public void onVideoTrackEnabled(RemoteParticipant remoteParticipant, RemoteVideoTrackPublication remoteVideoTrackPublication) {

            }

            @Override
            public void onVideoTrackDisabled(RemoteParticipant remoteParticipant, RemoteVideoTrackPublication remoteVideoTrackPublication) {

            }
        };
    }

    /**
     * Handler of incoming messages from service.
     */
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MessengerService.MSG_IM_CLASS:
                    Bundle chatBundle = msg.getData();

                    ChatMessage message = new ChatMessage(chatBundle.getString("data"));

                    //Showing Incoming messages
                    if (message.getMessageType().equals(MessengerService.IM_CLASS) ||
                            message.getMessageType().equals(MessengerService.NORMAL_MESSAGE_TYPE)) {
                        /*if (mBooking.getStudentId().equals(message.getSender().split("_")[1])) {
                            message.setSender(mAccount.getFirstName());
                        } else if (mTutor.getTutorId().equals(message.getSender().split("_")[1])) {
                            message.setSender(mTutor.getName());
                        }*/

                        //message.setSender(message.getSenderName());

                        //getting confirmation from group room
                        if (mBooking.getStudentId().equals(message.getSender().split("_")[1])) {
                            for (RemoteParticipant remoteParticipant : mRoom.getRemoteParticipants()) {
                                if (remoteParticipant.getIdentity().equals(message.getTo().split("_")[1])) {
                                    remoteParticipantCount++;
                                }
                            }
                            if (remoteParticipantCount == mRoom.getRemoteParticipants().size()) {
                                mMessageList.add(message);
                                remoteParticipantCount = 0;
                            }
                        }
                        //getting confirmation from tutor
                        else {
                            mMessageList.add(message);
                        }

                        mMessageAdapter.notifyDataSetChanged();
                        rvChatMessages.smoothScrollToPosition(0);
                    }

                    //Trigger when Tutor Click 'Play'
                    else if (message.getMessageType().equals(MessengerService.AUDIO_PLAY_TYPE)) {
                        mp3Url = message.getBody();
                        Log.i("mp3URL", mp3Url);
                        try {
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(mp3Url);
                            mediaPlayer.setVolume(.2f, .2f);
                            mediaPlayer.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mediaPlayer.start();
                    }

                    //Trigger when Tutor Click 'Pause'
                    else if (message.getMessageType().equals(MessengerService.AUDIO_PAUSE_TYPE)) {
                        audioPos = mediaPlayer.getCurrentPosition();
                        mediaPlayer.pause();
                        mediaPlayer.reset();
                    }

                    //Trigger when Tutor Click 'Resume'
                    else if (message.getMessageType().equals(MessengerService.AUDIO_RESUME_TYPE)) {
                        Log.i("mp3 resume", mp3Url + "");
                        if (mp3Url == null) {
                            String messageContent = message.getBody();
                            try {
                                JSONObject msgJson = new JSONObject(messageContent);
                                mp3Url = msgJson.getString("audio_url");
                                Log.i("mp3URL", mp3Url);
                                audioPos = (int) (msgJson.getDouble("audio_time") * 1000);
                                mediaPlayer.setDataSource(mp3Url);
                                mediaPlayer.setVolume(.2f, .2f);
                                mediaPlayer.prepare();
                                mediaPlayer.seekTo(audioPos);
                                mediaPlayer.start();
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            String messageContent = message.getBody();
                            try {
                                JSONObject msgJson = new JSONObject(messageContent);
                                audioPos = (int) (msgJson.getDouble("audio_time") * 1000);
                                mediaPlayer.setDataSource(mp3Url);
                                mediaPlayer.setVolume(.2f, .2f);
                                mediaPlayer.prepare();
                                mediaPlayer.seekTo(audioPos);
                                mediaPlayer.start();
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    //Trigger when Tutor Click 'Stop'
                    else if (message.getMessageType().equals(MessengerService.AUDIO_STOP_TYPE)) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(ClassRoomActivity.this).inflate(R.layout.item_message, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ChatMessage message = mMessageList.get(mMessageList.size() - ++position);

            holder.tvSender.setText(message.getSenderName());
            holder.tvMessage.setText(message.getBody());
        }

        @Override
        public int getItemCount() {
            return mMessageList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSender;
            TextView tvMessage;

            public ViewHolder(View itemView) {
                super(itemView);

                tvSender = itemView.findViewById(R.id.tv_sender);
                tvMessage = itemView.findViewById(R.id.tv_message);
            }
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
                            mAudioManager.setMode(AudioManager.MODE_IN_CALL);
                            if (mAudioManager.isSpeakerphoneOn()) {
                                mAudioManager.setSpeakerphoneOn(true);
                            } else {
                                mAudioManager.setSpeakerphoneOn(false);
                            }
                            break;
                        case 1:
                            mAudioManager.setMode(AudioManager.MODE_IN_CALL);
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
