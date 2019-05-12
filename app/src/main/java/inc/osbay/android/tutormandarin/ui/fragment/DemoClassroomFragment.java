package inc.osbay.android.tutormandarin.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.twilio.video.CameraCapturer;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.VideoView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;
import inc.osbay.android.tutormandarin.util.CameraCapturerCompat;

public class DemoClassroomFragment extends BackHandledFragment {

    public static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;
    private static final int MAX_CLICK_DURATION = 500;
    private static final int MAX_CLICK_DISTANCE = 5;
    private static WebView.WebViewTransport instance;
    private static WebView mWebView;
    private static MessageAdapter mMessageAdapter;
    @BindView(R.id.tv_toolbar_title)
    TextView tvTitle;
    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.fab_white_board)
    FloatingActionButton fabWhiteBoard;
    @BindView(R.id.twilio_remote_video)
    VideoView twilioVideoView;
    @BindView(R.id.remote_video)
    android.widget.VideoView smallRemoteVideoView;
    @BindView(R.id.classroom_layout)
    RelativeLayout classRoomLayout;
    @BindView(R.id.rl_white_board)
    RelativeLayout mRlWhiteBoard;
    @BindView(R.id.vv_local_video)
    VideoView mLocalVideoView;
    @BindView(R.id.vv_remote_video)
    android.widget.VideoView mRemoteVideoView;
    @BindView(R.id.twilio_vv_remote_video)
    VideoView mTwilioRemoteVideoView;
    @BindView(R.id.rl_assistant_pop_up)
    RelativeLayout menuRL;
    @BindView(R.id.imv_local_video_hide)
    ImageView mLocalVideoHideImageView;
    @BindView(R.id.edt_input_text)
    EditText messageET;
    @BindView(R.id.rl_class_status)
    RelativeLayout classStatusRL;
    @BindView(R.id.rv_chat_messages)
    RecyclerView rvChatMessages;
    private boolean pauseVideo;
    private float dY;
    private float dX;
    private int screenHight;
    private int screenWidth;
    private int videoPos;
    private float newY;
    private float newX;
    private long pressStartTime;
    private float pressX;
    private float pressY;
    private float distance;
    private CameraCapturerCompat mCameraCapturerCompat;
    private LocalVideoTrack mLocalVideoTrack;
    private List<String> messages = new ArrayList<>();

    public static WebView.WebViewTransport getInstance() {
        if (instance == null) {
            instance = mWebView.new WebViewTransport();
        }
        return instance;
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStack();
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHight = displaymetrics.heightPixels;
        screenWidth = displaymetrics.widthPixels;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_class_room, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitle.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Montserrat-Bold.ttf"));
        tvTitle.setText(getString(R.string.cr_title));
        classStatusRL.setVisibility(View.GONE);
        mTwilioRemoteVideoView.setVisibility(View.GONE);
        mRemoteVideoView.setVisibility(View.VISIBLE);
        twilioVideoView.setVisibility(View.GONE);

        if (!checkPermissionForCamera()) {
            requestPermissionForCamera();
        } else {
            // Share your camera
            mCameraCapturerCompat = new CameraCapturerCompat(getContext(), CameraCapturer.CameraSource.FRONT_CAMERA);
            mLocalVideoTrack = LocalVideoTrack.create(getContext(), true, mCameraCapturerCompat.getVideoCapturer());
            mLocalVideoView.setMirror(true);
            mLocalVideoTrack.addRenderer(mLocalVideoView);

            mRemoteVideoView.setVideoURI(Uri.parse(CommonConstant.REMOTE_VIDEO_URL));
            mRemoteVideoView.start();
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        if (rvChatMessages != null) {
            rvChatMessages.setLayoutManager(layoutManager);
        }

        mMessageAdapter = new MessageAdapter();
        if (rvChatMessages != null) {
            rvChatMessages.setAdapter(mMessageAdapter);
        }

        loadWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView() {
        mWebView = new WebView(getContext());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mWebView.loadUrl(url);
                return true;
            }
        });

        mWebView.loadUrl(CommonConstant.DEMO_CLASS_URL);

        getInstance().setWebView(mWebView);
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float distanceInPx = (float) Math.sqrt(dx * dx + dy * dy);
        return distanceInPx;
    }

    @OnClick(R.id.tv_back)
    void backFragment() {
        onBackPressed();
        getActivity().finish();
    }

    @OnClick(R.id.tv_send_button)
    void clickSend() {
        String message = messageET.getText().toString();
        if (TextUtils.isEmpty(message)) {
            return;
        }

        messages.add(message);
        mMessageAdapter.notifyDataSetChanged();
        rvChatMessages.smoothScrollToPosition(0);
        messageET.setText("");
    }

    @OnClick(R.id.ll_assistant_online_support)
    void contactConsultant() {
        FlurryAgent.logEvent("Click Online Support from DemoClass");
        menuRL.setVisibility(View.GONE);

        OnlineSupportFragment onlineSupportFragment = new OnlineSupportFragment();
        Bundle bundle = new Bundle();
        bundle.putString("class_type", "demo");
        onlineSupportFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, onlineSupportFragment)
                .addToBackStack(null)
                .commit();
    }

    @OnClick(R.id.ll_assistant_faq)
    void faq() {
        FlurryAgent.logEvent("Click FAQ from DemoClass");
        menuRL.setVisibility(View.GONE);

        ClassroomFAQFragment faqFragment = new ClassroomFAQFragment();
        Bundle bundle = new Bundle();
        bundle.putString("class_type", "demo");
        faqFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, faqFragment)
                .addToBackStack(null)
                .commit();
    }

    @SuppressLint("ClickableViewAccessibility")
    @OnClick(R.id.fab_white_board)
    void goWhiteboard() {
        FlurryAgent.logEvent("Open whiteboard");

        videoPos = mRemoteVideoView.getCurrentPosition();
        mRemoteVideoView.pause();
        menuRL.setVisibility(View.GONE);
        classRoomLayout.setVisibility(View.GONE);
        mLocalVideoView.setVisibility(View.GONE);
        mRemoteVideoView.setVisibility(View.GONE);
        mRlWhiteBoard.setVisibility(View.VISIBLE);
        smallRemoteVideoView.setVisibility(View.VISIBLE);

        smallRemoteVideoView.setVideoURI(Uri.parse(CommonConstant.REMOTE_VIDEO_URL));
        smallRemoteVideoView.seekTo(videoPos);
        smallRemoteVideoView.start();

        mRlWhiteBoard.removeAllViews();
        mRlWhiteBoard.addView(getInstance().getWebView());
        smallRemoteVideoView.setOnTouchListener(new View.OnTouchListener() {
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
                            videoPos = smallRemoteVideoView.getCurrentPosition();
                            smallRemoteVideoView.setVisibility(View.GONE);
                            menuRL.setVisibility(View.GONE);
                            smallRemoteVideoView.pause();
                            mRemoteVideoView.seekTo(videoPos);
                            mRemoteVideoView.start();
                        }
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    private boolean checkPermissionForCamera() {
        int resultCamera = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
        return (resultCamera == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissionForCamera() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
            Toast.makeText(getContext(), getString(R.string.cr_camera_permission), Toast.LENGTH_LONG).show();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_MIC_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_MIC_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length >= 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Share your camera
                    mCameraCapturerCompat = new CameraCapturerCompat(getContext(), CameraCapturer.CameraSource.FRONT_CAMERA);
                    mLocalVideoTrack = LocalVideoTrack.create(getContext(), true, mCameraCapturerCompat.getVideoCapturer());
                    mLocalVideoView.setMirror(true);
                    mLocalVideoTrack.addRenderer(mLocalVideoView);

                    /*MediaController mc = new MediaController(getContext());
                    mc.setAnchorView(mRemoteVideoView);
                    mc.setMediaPlayer(mRemoteVideoView);
                    mRemoteVideoView.setMediaController(mc);*/
                    mRemoteVideoView.setVideoURI(Uri.parse(CommonConstant.REMOTE_VIDEO_URL));
                    mRemoteVideoView.start();
                    /*smallRemoteVideoView.setVideoURI(Uri.parse(CommonConstant.REMOTE_VIDEO_URL));
                    smallRemoteVideoView.start();*/
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

                    alertDialogBuilder.setTitle("Permission");
                    alertDialogBuilder.setMessage("Need to check permission in App System Setting");
                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            getActivity().finish();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
                return;
            }
        }
    }

    @OnClick(R.id.assistant_menu)
    void clickMenu() {
        showHideMenu();
    }

    private void showHideMenu() {
        if (menuRL.getVisibility() == View.VISIBLE)
            menuRL.setVisibility(View.GONE);
        else
            menuRL.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.imv_local_video_hide)
    void hideLocalVideo() {
        pauseVideo = !pauseVideo;

        pauseVideo(pauseVideo);

        if (pauseVideo) {
            mLocalVideoHideImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_video_cam_off));
        } else {
            mLocalVideoHideImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_video_cam_off));
        }
    }

    private void pauseVideo(boolean pauseVideo) {
        if (mLocalVideoTrack != null) {
            mLocalVideoTrack.enable(!pauseVideo);
        } else {
            mLocalVideoTrack.enable(!pauseVideo);
        }
    }

    private class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.item_message, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String message = messages.get(messages.size() - ++position);

            holder.tvSender.setText(getString(R.string.student));
            holder.tvMessage.setText(message);
        }

        @Override
        public int getItemCount() {
            return messages.size();
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
}
