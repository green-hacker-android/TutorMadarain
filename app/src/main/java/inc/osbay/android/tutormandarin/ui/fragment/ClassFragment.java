package inc.osbay.android.tutormandarin.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.Map;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Booking;
import inc.osbay.android.tutormandarin.sdk.model.Topic;
import inc.osbay.android.tutormandarin.sdk.model.TopicClass;
import inc.osbay.android.tutormandarin.ui.activity.FragmentHolderActivity;
import inc.osbay.android.tutormandarin.ui.activity.ShareActivity;
import inc.osbay.android.tutormandarin.ui.activity.SignUpActivity;
import inc.osbay.android.tutormandarin.ui.view.ListDialog;

public class ClassFragment extends BackHandledFragment implements View.OnClickListener {
    public static final String EXTRA_CLASS_ID = "ClassFragment.EXTRA_CLASS_ID";

    private TopicClass mTopicClass;
    private Topic mTopic;

    private ServerRequestManager requestManager;

    private RelativeLayout mDialogContentLinearLayout;
    private boolean isMoving;
    private float startY;

    public ClassFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestManager = new ServerRequestManager(getActivity().getApplicationContext());
        CurriculumAdapter curriculumAdapter = new CurriculumAdapter(getActivity());

        Bundle bundle = getArguments();
        if (bundle != null) {
            mTopicClass = curriculumAdapter.getTopicClassById(bundle.getString(EXTRA_CLASS_ID));
            mTopic = curriculumAdapter.getTopicById(mTopicClass.getTopicId());
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
        setTitle(mTopicClass.getTitle());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_class, container, false);

        Toolbar toolBar;
        toolBar = rootView.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        setSupportActionBar(toolBar);

        WebView webView = rootView.findViewById(R.id.wb_class_content);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(CommonConstant.CLASS_CONTENT_URL + "?DataID=" + mTopicClass.getClassId());
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(motionEvent.getY() - startY) > 5) {
                            isMoving = true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (isMoving) {
                            isMoving = false;
                        } else {
//                            showActionBar();

                            Animation fadeOut = new AlphaAnimation(0, 1);
                            fadeOut.setInterpolator(new AccelerateInterpolator());
                            fadeOut.setDuration(300);
                            mDialogContentLinearLayout.setAnimation(fadeOut);
                            mDialogContentLinearLayout.setVisibility(View.VISIBLE);
                        }
                        break;
                }
                return false;
            }
        });

        mDialogContentLinearLayout = rootView.findViewById(R.id.ll_dialog_content);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartOffset(1500);
        fadeOut.setDuration(650);

        mDialogContentLinearLayout.setAnimation(fadeOut);
        mDialogContentLinearLayout.setVisibility(View.INVISIBLE);
        mDialogContentLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                hideActionBar();

                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setDuration(300);
                mDialogContentLinearLayout.setAnimation(fadeOut);
                mDialogContentLinearLayout.setVisibility(View.INVISIBLE);
            }
        });

        TextView mDialogTitleTextView = rootView.findViewById(R.id.tv_dialog_title);
        mDialogTitleTextView.setText(mTopicClass.getTitle());

        TextView tvLevel = rootView.findViewById(R.id.tv_level);
        tvLevel.setText(mTopicClass.getLevel());

        TextView tvTopic = rootView.findViewById(R.id.tv_topic);
        tvTopic.setText(mTopic.getTitle());

        Button btnBook = rootView.findViewById(R.id.btn_book_class);
        btnBook.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_lesson, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.opt_more:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                final String accountId = prefs.getString("account_id", null);

                String[] menuItems = {getString(R.string.cl_add_to_favourite), getString(R.string.cl_share)};
                int[] menuIcons = {R.drawable.ic_add_to_favorite, R.drawable.ic_menu_share};

                ListDialog customMenu = new ListDialog(getActivity(), menuItems, menuIcons,
                        new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                switch (i) {
                                    case 0:
                                        if (accountId != null) {
                                            CurriculumAdapter curriculumAdapter = new CurriculumAdapter(getActivity());
                                            if (curriculumAdapter.checkFavourite(mTopicClass.getClassId(), accountId)) {
                                                Toast.makeText(getActivity(), getString(R.string.cl_already_added), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Map<String, String> favourite = new HashMap<>();
                                                favourite.put("lesson_tutor_id", mTopicClass.getClassId());
                                                favourite.put("fav_lesson_user", accountId);
                                                favourite.put("type", String.valueOf(FavouriteDrawerFragment.CLASS_ID));
                                                curriculumAdapter.insertFavourite(favourite);
                                                ServerRequestManager serverRequestManager = new ServerRequestManager(getActivity().getApplicationContext());
                                                serverRequestManager.addToFavorite(FavouriteDrawerFragment.CLASS_ID, mTopicClass.getClassId(), FavouriteDrawerFragment.FAVOURITE_ID);
                                                Toast.makeText(getActivity(), getString(R.string.cl_successfully_added), Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Intent signUpIntent = new Intent(getActivity(), SignUpActivity.class);
                                            signUpIntent.putExtra(SignUpActivity.MAIN_SIGN_UP, 1);
                                            startActivity(signUpIntent);
                                        }
                                        break;
                                    case 1:
                                        String content = ShareActivity.getShareLessonTemplate(
                                                mTopic.getTitle());

                                        Intent intent = new Intent(getActivity(), ShareActivity.class);
                                        intent.putExtra(ShareActivity.EXTRA_TITLE, mTopic.getTitle());
                                        intent.putExtra(ShareActivity.EXTRA_CONTENT, content);
                                        intent.putExtra(ShareActivity.EXTRA_IMAGE_URL, mTopic.getPhotoUrl());

                                        startActivity(intent);
                                        break;
                                }
                            }
                        });
                customMenu.setGravity(Gravity.TOP);
                customMenu.show();
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sdv_cover_photo:
                showActionBar();

                Animation fadeOut = new AlphaAnimation(0, 1);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setDuration(300);
                mDialogContentLinearLayout.setAnimation(fadeOut);
                mDialogContentLinearLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_book_class:

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String token = prefs.getString("access_token", null);
                String accountId = prefs.getString("account_id", null);

                if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
                    final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage(getString(R.string.cl_check_booking));
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    requestManager.checkLessonToBook(mTopicClass.getClassId(), 2,
                            new ServerRequestManager.OnRequestFinishedListener() {
                                @Override
                                public void onSuccess(Object result) {
                                    progressDialog.dismiss();

                                    if (getActivity() != null) {
                                        if (getActivity() instanceof FragmentHolderActivity) {
                                            Intent data = new Intent();
                                            data.putExtra("EXTRA_CLASS_ID", mTopicClass.getClassId());

                                            getActivity().setResult(Activity.RESULT_OK, data);
                                            getActivity().finish();
                                        } else {
                                            Bundle bundle = new Bundle();
                                            bundle.putString(MyBookingFragment.EXTRA_CLASS_ID, mTopicClass.getClassId());
                                            bundle.putInt(MyBookingFragment.EXTRA_BOOKING_TYPE, Booking.Type.TOPIC);

                                            Fragment bookingFragment = new MyBookingFragment();
                                            bookingFragment.setArguments(bundle);

                                            FragmentManager frgMgr = getFragmentManager();
                                            Fragment frg = frgMgr.findFragmentById(R.id.container);
                                            if (frg == null) {
                                                frgMgr.beginTransaction()
                                                        .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                                                R.animator.fragment_out_new, R.animator.fragment_out_old)
                                                        .addToBackStack(null)
                                                        .add(R.id.container, bookingFragment).commit();
                                            } else {
                                                frgMgr.beginTransaction()
                                                        .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                                                R.animator.fragment_out_new, R.animator.fragment_out_old)
                                                        .addToBackStack(null)
                                                        .replace(R.id.container, bookingFragment).commit();
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onError(ServerError err) {
                                    progressDialog.dismiss();

                                    if (getActivity() != null) {
                                        if (err.getErrorCode() == ServerError.Code.CLASS_ALREADY_BOOKED) {
                                            new AlertDialog.Builder(getActivity())
                                                    .setTitle(getString(R.string.cl_already_booked))
                                                    .setMessage(getString(R.string.cl_already_booked_msg))
                                                    .setPositiveButton(getString(R.string.cl_already_booked_ok), null)
                                                    .show();
                                        } else if (err.getErrorCode() == ServerError.Code.TOPIC_ALREADY_BOOKED) {
                                            new AlertDialog.Builder(getActivity())
                                                    .setTitle(getString(R.string.cl_topic_already_booked))
                                                    .setMessage(getString(R.string.cl_topic_already_booked_msg))
                                                    .setPositiveButton(getString(R.string.cl_topic_already_booked_ok), null)
                                                    .show();
                                        } else if (err.getErrorCode() == ServerError.Code.HIGHER_TOPIC_LEVEL) {
                                            new AlertDialog.Builder(getActivity())
                                                    .setTitle(getString(R.string.cl_topic_level_high))
                                                    .setMessage(getString(R.string.cl_topic_level_high_msg))
                                                    .setPositiveButton(getString(R.string.cl_topic_level_high_ok), null)
                                                    .show();
                                        } else {
                                            new AlertDialog.Builder(getActivity())
                                                    .setTitle(getString(R.string.cl_server_error))
                                                    .setMessage(err.getMessage())
                                                    .setPositiveButton(getString(R.string.cl_server_error_ok), null)
                                                    .show();
                                        }
                                    }
                                }
                            });
                } else {
                    FlurryAgent.logEvent("Sign Up(book class)");
                    Intent signUpIntent = new Intent(getActivity(), SignUpActivity.class);
                    signUpIntent.putExtra(SignUpActivity.MAIN_SIGN_UP, 1);
                    startActivity(signUpIntent);
                }

                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStack();
        return false;
    }

}
