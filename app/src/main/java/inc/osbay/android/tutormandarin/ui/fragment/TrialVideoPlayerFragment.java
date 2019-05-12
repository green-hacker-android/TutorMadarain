package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.flurry.android.FlurryAgent;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.ui.activity.SignUpActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrialVideoPlayerFragment extends BackHandledFragment implements View.OnClickListener,
        SurfaceHolder.Callback, SeekBar.OnSeekBarChangeListener {

    private static int playerStatus = 1; // 1. Downloaded, 2. playing, 3. pause, 4. completed
    private static int mCounter;
    RelativeLayout rlMediaController;
    SurfaceView svTrialVideo;
    private MediaPlayer mMediaPlayer;
    private SharedPreferences mPreferences;
    private SeekBar mProgressSeekBar;
    private Timer mProgressTimer;
    private ImageView mPlayPauseImageView;

    private boolean isProgressChange;

    public TrialVideoPlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public boolean onBackPressed() {
        mMediaPlayer.stop();
        playerStatus = 1;
        getFragmentManager().popBackStack();
        return false;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showActionBar();
        setTitle("");

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_trial_video_player, container, false);

        svTrialVideo = (SurfaceView) rootView.findViewById(R.id.sv_trial_video);
        SurfaceHolder surfaceHolder = svTrialVideo.getHolder();
        surfaceHolder.addCallback(this);

        mPlayPauseImageView = (ImageView) rootView.findViewById(R.id.imv_play_video);
        mPlayPauseImageView.setOnClickListener(this);

        mProgressSeekBar = (SeekBar) rootView.findViewById(R.id.sb_trial_video);
        mProgressSeekBar.setOnSeekBarChangeListener(this);

        Button btnStartTrial = (Button) rootView.findViewById(R.id.btn_start_trial);
        btnStartTrial.setOnClickListener(this);

        rlMediaController = (RelativeLayout) rootView.findViewById(R.id.rl_media_controller);
        rlMediaController.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mMediaPlayer != null && playerStatus != 1)
            return;

        playerStatus = 1;

        mMediaPlayer = new MediaPlayer();

        String videoUrl = mPreferences.getString("trial_video_url", "");

        Log.e("TrialVideoPlayer", videoUrl);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.vi_dialog_loading));
        progressDialog.show();

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                progressDialog.dismiss();
                mMediaPlayer.start();
                playerStatus = 2;
                mProgressSeekBar.setMax(mMediaPlayer.getDuration());
                mPlayPauseImageView.setImageResource(R.drawable.ic_video_pause);

                changeVideoViewSize();
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                playerStatus = 4;
                mPlayPauseImageView.setImageResource(R.drawable.ic_video_restart);

                for (int i = 0; i < rlMediaController.getChildCount(); i++) {
                    View child = rlMediaController.getChildAt(i);
                    child.setVisibility(View.VISIBLE); // Or whatever you want to do with the view.
                }
            }
        });

        try {
            mMediaPlayer.setDataSource(videoUrl);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        showActionBar();
        setTitle("");
        setDisplayHomeAsUpEnable(true);

        mProgressTimer = new Timer();
        mProgressTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mMediaPlayer.isPlaying()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressSeekBar.setProgress(mMediaPlayer.getCurrentPosition());

                            if (mCounter > 3) {
                                mCounter = 0;
                                for (int i = 0; i < rlMediaController.getChildCount(); i++) {
                                    View view = rlMediaController.getChildAt(i);
                                    view.setVisibility(View.GONE); // Or whatever you want to do with the view.
                                }
                            } else {
                                mCounter++;
                            }
                        }
                    });
                }
            }
        }, 1000, 1000);
    }

    @Override
    public void onPause() {
        Log.e("Player", "onPause");

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            playerStatus = 3;
            mPlayPauseImageView.setImageResource(R.drawable.ic_video_start);
        }

        if (mProgressTimer != null) {
            mProgressTimer.cancel();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e("Player", "onStop");

        if(playerStatus != 3) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        super.onStop();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_media_controller:
                mCounter = 0;
                for (int i = 0; i < rlMediaController.getChildCount(); i++) {
                    View child = rlMediaController.getChildAt(i);
                    child.setVisibility(View.VISIBLE); // Or whatever you want to do with the view.
                }

                if (playerStatus == 3) {
                    playerStatus = 2;
                    mMediaPlayer.start();
                    mPlayPauseImageView.setImageResource(R.drawable.ic_video_pause);
                }

                else if (playerStatus == 4) {
                    playerStatus = 2;
                    mMediaPlayer.start();
                    mPlayPauseImageView.setImageResource(R.drawable.ic_video_pause);
                }
                break;

            case R.id.imv_play_video:
                if (playerStatus != 0 && mMediaPlayer.isPlaying()) {
                    playerStatus = 3;
                    mMediaPlayer.pause();
                    mPlayPauseImageView.setImageResource(R.drawable.ic_video_start);
                } else if (playerStatus == 3) {
                    playerStatus = 2;
                    mMediaPlayer.start();
                    mPlayPauseImageView.setImageResource(R.drawable.ic_video_pause);
                    isProgressChange = false;
                } else if (playerStatus == 4) {
                    playerStatus = 2;
                    mMediaPlayer.start();
                    mPlayPauseImageView.setImageResource(R.drawable.ic_video_pause);
                }
                break;
            case R.id.btn_start_trial:
                mMediaPlayer.stop();
                playerStatus = 1;

                FlurryAgent.logEvent("Sign Up(trial video)");

                Intent signUpIntent = new Intent(getActivity(), SignUpActivity.class);
                signUpIntent.putExtra(SignUpActivity.MAIN_SIGN_UP, 1);
                startActivity(signUpIntent);
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.e("Player", "Surface created." + playerStatus);

        mMediaPlayer.setDisplay(surfaceHolder);
        if (playerStatus == 1) {
            mMediaPlayer.prepareAsync();
        } else if (playerStatus == 3) {
            playerStatus = 2;
            mMediaPlayer.start();
            mPlayPauseImageView.setImageResource(R.drawable.ic_video_pause);
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.e("Player", "Surface changed.");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.e("Player", "Surface destroyed.");
    }

    private void changeVideoViewSize() {
        //Get the dimensions of the video
        int videoWidth = mMediaPlayer.getVideoWidth();
        int videoHeight = mMediaPlayer.getVideoHeight();

        //Get the width of the screen
        int screenWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();

        //Get the SurfaceView layout parameters
        ViewGroup.LayoutParams lp = svTrialVideo.getLayoutParams();

        //Set the width of the SurfaceView to the width of the screen
        lp.width = screenWidth;

        //Set the height of the SurfaceView to match the aspect ratio of the video
        //be sure to cast these as floats otherwise the calculation will likely be 0
        lp.height = (int) (((float) videoHeight / (float) videoWidth) * (float) screenWidth);

        //Commit the layout parameters
        svTrialVideo.setLayoutParams(lp);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (!b) {
            return;
        }
        if (playerStatus != 3) {
            isProgressChange = true;
            mMediaPlayer.pause();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        if (playerStatus == 4) {
            isProgressChange = false;
            playerStatus = 3;
            mPlayPauseImageView.setImageResource(R.drawable.ic_video_start);
        }

        if (isProgressChange) {
            isProgressChange = false;
            mMediaPlayer.start();
        }

        mMediaPlayer.seekTo(seekBar.getProgress());
        mProgressSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
        int duration = mMediaPlayer.getDuration();
        mProgressSeekBar.setSecondaryProgress(duration);
    }
}
