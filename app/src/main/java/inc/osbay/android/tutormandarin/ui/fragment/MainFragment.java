package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.TMApplication;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.service.MessengerService;
import inc.osbay.android.tutormandarin.ui.activity.ClassroomAssistantActivity;
import inc.osbay.android.tutormandarin.ui.activity.FragmentHolderActivity;
import inc.osbay.android.tutormandarin.ui.activity.SignUpContinueActivity;
import inc.osbay.android.tutormandarin.ui.activity.TrialSubmitActivity;
import inc.osbay.android.tutormandarin.util.WSMessageClient;

public class MainFragment extends BackHandledFragment implements View.OnClickListener {
    public static final String TAG = MainFragment.class.getSimpleName();

    ServerRequestManager mRequestManager;
    Typeface typeface;
    private NotificationListener mNotificationListener;
    private SharedPreferences mPreferences;
    private WSMessageClient mWSMessageClient;
    private Toolbar toolBar;
    private LinearLayout mTrialVideoLinearLayout;
    private LinearLayout mTrialMessageLinearLayout;
    private LinearLayout mDiscountLinearLayout;
    private TextView mTrialStatusTextView;
    private TextView mDiscountAmountTextView;
    private TextView mScheduleTextView;
    private LeftMenuDrawerFragment mLeftMenuDrawerFragment;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private RelativeLayout rlMyPackage;
    private RelativeLayout frame;
    private RelativeLayout rlMainCourse;
    private RelativeLayout rlMainTutor;
    private RelativeLayout rlMainDailyContent;
    private RelativeLayout rlMainOnlineSupport;
    private LinearLayout mLlMain;
    private boolean isAnimation;
    private int mPageNo;
    private int mCurrentPage;
    private int[] mMainSlider;
    private int mViewId;
    private int mTop;
    private int mHeight;
    private int mBottom;
    private int mIndex;
    private ImageView getTrialFAB;
    private ImageView getTrialHeaderFAB;
    private SimpleDraweeView mSimpleDraweeView;
    private String TRIAL_REQUEST = "MainFragment.TRIAL_REQUEST";
    private Account account;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() != null) {
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.gc();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mRequestManager = new ServerRequestManager(getActivity().getApplicationContext());

        mWSMessageClient = ((TMApplication) getActivity().getApplication()).getWSMessageClient();
        typeface = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/Montserrat-Regular.ttf");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isAnimation) {
            hideCustomLayout();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUI();
        if (getActivity() != null) {
            getActivity().registerReceiver(mNotificationListener, new IntentFilter("inc.osbay.android.tutormandarin.NOTIFICATION"));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            getActivity().unregisterReceiver(mNotificationListener);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        getTrialHeaderFAB = rootView.findViewById(R.id.imv_demo_classroom);
        mSimpleDraweeView = rootView.findViewById(R.id.imv_notification);
        getTrialFAB = rootView.findViewById(R.id.free_trial);
        getTrialFAB.setOnClickListener(this);
        mTrialStatusTextView = rootView.findViewById(R.id.tv_trial_status);

        String token = mPreferences.getString("access_token", null);
        String accountId = mPreferences.getString("account_id", null);

        if (!TextUtils.isEmpty(accountId) && !TextUtils.isEmpty(token)) {
            AccountAdapter accountAdapter = new AccountAdapter(getActivity());
            account = accountAdapter.getAccountById(accountId);

            if (account.getStatus() == Account.Status.REQUEST || account.getStatus() == Account.Status.REQUEST_TRIAL) {
                mSimpleDraweeView.setVisibility(View.GONE);
                getTrialFAB.setVisibility(View.VISIBLE);
                getTrialHeaderFAB.setVisibility(View.VISIBLE);
                mTrialStatusTextView.setText(getString(R.string.try_demo));

                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.dialog_trial_class);
                dialog.setCancelable(false);

                TextView trial_explain_tv = dialog.findViewById(R.id.trial_intro_tv);
                TextView experienceNowTV = dialog.findViewById(R.id.experience_now);
                TextView laterTV = dialog.findViewById(R.id.later);
                trial_explain_tv.setTypeface(typeface);
                experienceNowTV.setTypeface(typeface);
                laterTV.setTypeface(typeface);
                experienceNowTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        FlurryAgent.logEvent("Entering Demo Classroom");
                        Intent intent = new Intent(getContext(), ClassroomAssistantActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("fragment", MainFragment.class.getSimpleName());
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });
                laterTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        }

        toolBar = rootView.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#DE615B"));
        setSupportActionBar(toolBar);

        frame = rootView.findViewById(R.id.rl_main_content);

        // Implement Left Menu Drawer
        mDrawerLayout = rootView.findViewById(R.id.drawer_layout);
        mLeftMenuDrawerFragment = new LeftMenuDrawerFragment();

        mMainSlider = new int[]{R.drawable.bg_main_slider_1, R.drawable.bg_main_slider_2, R.drawable.bg_main_slider_3, R.drawable.bg_main_slider_4};

        FragmentManager fragmentManager = getChildFragmentManager();
        Fragment oldDrawer = fragmentManager.findFragmentById(R.id.left_menu_drawer);
        if (oldDrawer == null) {
            fragmentManager.beginTransaction().add(R.id.left_menu_drawer, mLeftMenuDrawerFragment)
                    .commitAllowingStateLoss();
        } else {
            fragmentManager.beginTransaction().replace(R.id.left_menu_drawer, mLeftMenuDrawerFragment)
                    .commitAllowingStateLoss();
        }

        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                toolBar,
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                getTrialFAB.setVisibility(View.GONE);
                float moveFactor = (getResources().getDimension(R.dimen.navigation_drawer_width) * slideOffset);

                frame.setTranslationX(moveFactor);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                FlurryAgent.logEvent("Dashboard");

                ServerRequestManager requestManager = new ServerRequestManager(
                        getActivity().getApplicationContext());
                requestManager.refreshStudentBalance(new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        refreshNavigationDrawer();
                    }

                    @Override
                    public void onError(ServerError err) {
                        // ignore
                    }
                });

                mLeftMenuDrawerFragment.refresh();

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (account.getStatus() == Account.Status.REQUEST || account.getStatus() == Account.Status.REQUEST_TRIAL) {
                    getTrialFAB.setVisibility(View.VISIBLE);
                }
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(false);

        toolBar.setNavigationIcon(createImage());
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTrialFAB.setVisibility(View.GONE);
                mDrawerLayout.openDrawer(Gravity.START);
            }
        });

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        mDrawerLayout.addDrawerListener(mDrawerToggle);


        ((TextView) rootView.findViewById(R.id.tv_schedule)).setTypeface(typeface);
        ((TextView) rootView.findViewById(R.id.tv_package)).setTypeface(typeface);
        ((TextView) rootView.findViewById(R.id.tv_course)).setTypeface(typeface);
        ((TextView) rootView.findViewById(R.id.tv_tutor)).setTypeface(typeface);
        ((TextView) rootView.findViewById(R.id.tv_study_materials)).setTypeface(typeface);
        ((TextView) rootView.findViewById(R.id.tv_online_support)).setTypeface(typeface);
        mTrialStatusTextView.setTypeface(typeface);


        RelativeLayout rlMySchedule = rootView.findViewById(R.id.rl_my_schedule);
        rlMySchedule.setOnClickListener(this);

        rlMainTutor = rootView.findViewById(R.id.rl_main_tutor);
        rlMainTutor.setOnClickListener(this);

        rlMainDailyContent = rootView.findViewById(R.id.rl_main_daily_content);
        rlMainDailyContent.setOnClickListener(this);

        rlMainOnlineSupport = rootView.findViewById(R.id.rl_main_online_support);
        rlMainOnlineSupport.setOnClickListener(this);

        rlMyPackage = rootView.findViewById(R.id.rl_package);
        rlMyPackage.setOnClickListener(this);

        rlMainCourse = rootView.findViewById(R.id.rl_main_course);
        rlMainCourse.setOnClickListener(this);

        mLlMain = rootView.findViewById(R.id.ll_main);

        mTrialVideoLinearLayout = rootView.findViewById(R.id.ll_trial_video);

        mScheduleTextView = rootView.findViewById(R.id.tv_schedule);
        mScheduleTextView.setOnClickListener(this);

        mDiscountLinearLayout = rootView.findViewById(R.id.ll_discount);
        mDiscountAmountTextView = rootView.findViewById(R.id.tv_discount_amount);

        mTrialMessageLinearLayout = rootView.findViewById(R.id.ll_trial_message);
        mTrialMessageLinearLayout.setOnClickListener(this);

        final ViewPager vpMainSlider = rootView.findViewById(R.id.vp_main_slider);
        vpMainSlider.setAdapter(new MainImageSlideAdapter(getActivity()));

        mPageNo = mMainSlider.length;
        mCurrentPage = 0;

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mCurrentPage == mPageNo) {
                    mCurrentPage = 0;
                }

                vpMainSlider.setCurrentItem(mCurrentPage++, true);
            }
        };

        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(runnable);
            }
        }, 3000, 3000);

        vpMainSlider.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mCurrentPage = position;
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // Swipe Refresh Layout
        final SwipeRefreshLayout srlRefresh = rootView.findViewById(R.id.srl_refresh);

        if (!TextUtils.isEmpty(accountId) && !TextUtils.isEmpty(token)) {
            srlRefresh.setEnabled(true);

            srlRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mRequestManager.getStudentInfo(new ServerRequestManager.OnRequestFinishedListener() {
                        @Override
                        public void onSuccess(Object result) {
                            if (getActivity() != null) {
                                srlRefresh.setRefreshing(false);

                                refreshUI();
                            }
                        }

                        @Override
                        public void onError(ServerError err) {
                            if (getActivity() != null) {
                                srlRefresh.setRefreshing(false);

                                Toast.makeText(getActivity(), err.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        } else {
            srlRefresh.setEnabled(false);
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        setHasOptionsMenu(true);
        setTitle(getString(R.string.app_name));

        mNotificationListener = new NotificationListener();

        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().beginTransaction()
                    .remove(MainFragment.this)
                    .commit();

            getFragmentManager().popBackStack(getFragmentManager().getBackStackEntryAt(0).getId(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        refreshUI();
    }

    public void refreshNavigationDrawer() {
        Log.d(TAG, "drawer refreshed.");
        mLeftMenuDrawerFragment.refresh();
    }

    private void refreshUI() {

        String token = mPreferences.getString("access_token", null);
        String accountId = mPreferences.getString("account_id", null);

        if (!TextUtils.isEmpty(accountId) && !TextUtils.isEmpty(token)) {
            AccountAdapter accountAdapter = new AccountAdapter(getActivity());
            Account account = accountAdapter.getAccountById(accountId);

            mTrialVideoLinearLayout.setVisibility(View.GONE);
            mScheduleTextView.setVisibility(View.VISIBLE);

            if (account.getStatus() == Account.Status.ACTIVE ||
                    account.getStatus() == Account.Status.NO_TRIAL_ACTIVE) {
                mTrialMessageLinearLayout.setVisibility(View.GONE);
                getTrialFAB.setVisibility(View.GONE);
            } else if (account.getStatus() == Account.Status.TRIAL) {
                mTrialMessageLinearLayout.setVisibility(View.VISIBLE);
                mTrialStatusTextView.setText(getString(R.string.main_trial_not_complete));
                mSimpleDraweeView.setVisibility(View.VISIBLE);
                getTrialFAB.setVisibility(View.GONE);
                getTrialHeaderFAB.setVisibility(View.GONE);
            } else if (account.getStatus() == Account.Status.REQUEST_TRIAL) {
                mTrialMessageLinearLayout.setVisibility(View.VISIBLE);
                mTrialStatusTextView.setText(getString(R.string.try_demo));
                mSimpleDraweeView.setVisibility(View.GONE);
                getTrialFAB.setVisibility(View.VISIBLE);
                getTrialHeaderFAB.setVisibility(View.VISIBLE);
            } else if (account.getStatus() == Account.Status.REQUEST) {
                mTrialMessageLinearLayout.setVisibility(View.VISIBLE);
                mTrialStatusTextView.setText(getString(R.string.try_demo));
                mSimpleDraweeView.setVisibility(View.GONE);
                getTrialFAB.setVisibility(View.VISIBLE);
                getTrialHeaderFAB.setVisibility(View.VISIBLE);
            } else if (account.getStatus() == Account.Status.INACTIVE) {
                mTrialMessageLinearLayout.setVisibility(View.VISIBLE);
                mTrialStatusTextView.setText(getString(R.string.main_submit_information));
            }

            Message message = Message.obtain(null, MessengerService.MSG_WS_LOGIN);
            Bundle bundle = new Bundle();
            bundle.putString("user_id", "S_" + accountId);
            message.setData(bundle);
            try {
                mWSMessageClient.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mTrialMessageLinearLayout.setVisibility(View.GONE);
            mTrialVideoLinearLayout.setVisibility(View.VISIBLE);
            mScheduleTextView.setVisibility(View.GONE);

            mRequestManager.getTrialVideo(new ServerRequestManager.OnRequestFinishedListener() {
                @Override
                public void onSuccess(Object result) {
                    if (getActivity() != null) {
//                        String imageUrl = mPreferences.getString("trial_video_image", "");

//                        DraweeController controller = Fresco.newDraweeControllerBuilder()
//                                .setUri(Uri.parse(imageUrl))
//                                .setControllerListener(listener)
//                                .build();
//                        mVideoThumbnailDraweeView.setController(controller);
                    }
                }

                @Override
                public void onError(ServerError err) {
                    // ignore
                }
            });
        }

        refreshDiscountAmount();
    }

    public void closeDrawer() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START, false);
    }

    private void refreshDiscountAmount() {
        mRequestManager.getDiscountAmount(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                if (getActivity() != null) {
                    mDiscountLinearLayout.setVisibility(View.VISIBLE);
                    mDiscountAmountTextView.setText(String.valueOf(result));
                }
            }

            @Override
            public void onError(ServerError err) {
                if (getActivity() != null) {
                    mDiscountLinearLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        String token = mPreferences.getString("access_token", null);
        String accountId = mPreferences.getString("account_id", null);

        Fragment newFragment;
        final FragmentManager fm = getFragmentManager();
        final Fragment fragment = fm.findFragmentById(R.id.container);

        switch (view.getId()) {
            case R.id.rl_my_schedule:
                isAnimation = false;
                if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
                    newFragment = new ScheduleDetailFragment();
                } else {
                    newFragment = new TrialVideoPlayerFragment();
                }
                makeTransaction(fm, fragment, newFragment);
                break;
            case R.id.tv_schedule:
                isAnimation = false;
                newFragment = new ScheduleDetailFragment();
                makeTransaction(fm, fragment, newFragment);
                break;
            case R.id.rl_main_tutor:
                isAnimation = true;
                mViewId = R.id.rl_main_tutor;
                mIndex = 4;
                makeCustomAnimation(rlMainTutor, mIndex, new TutorListFragment());
                break;
            case R.id.rl_main_course:
                isAnimation = true;
                mViewId = R.id.rl_main_course;
                mIndex = 3;
                makeCustomAnimation(rlMainCourse, mIndex, new CourseTopicFragment());
                break;
            case R.id.rl_package:
                if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
                    AccountAdapter accAdapter = new AccountAdapter(getActivity());
                    Account acc = accAdapter.getAccountById(accountId);
                    if (acc != null /*&& acc.getStatus() == 4*/) {
                        isAnimation = true;
                        mViewId = R.id.rl_package;
                        mIndex = 2;

                        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setMessage(getString(R.string.main_pk_loading));
                        progressDialog.show();
                        ServerRequestManager requestManager = new ServerRequestManager(getActivity().getApplicationContext());
                        requestManager.downloadStudentPackageSize(new ServerRequestManager.OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                progressDialog.dismiss();
                                if (getActivity() != null) {
                                    int pSize = (int) result;
                                    if (pSize > 0) {
                                        makeCustomAnimation(rlMyPackage, mIndex, new MyPackageListFragment());
                                    } else {
                                        makeCustomAnimation(rlMyPackage, mIndex, new PackageStoreFragment());
                                    }
                                }
                            }

                            @Override
                            public void onError(ServerError err) {
                                progressDialog.dismiss();
                                if (getActivity() != null) {
                                    Toast.makeText(getActivity(), err.getErrorCode() + " - " + err.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.main_unavailable))
                                .setMessage(getString(R.string.main_unavailable_msg))
                                .setPositiveButton(getString(R.string.main_unavailable_ok), null)
                                .show();
                    }
                }
                break;
            case R.id.rl_main_daily_content:
                isAnimation = true;
                mViewId = R.id.rl_main_daily_content;
                mIndex = 1;
                makeCustomAnimation(rlMainDailyContent, mIndex, new DailyContentFragment());
                break;
            case R.id.rl_main_online_support:
                isAnimation = true;
                mViewId = R.id.rl_main_online_support;
                mIndex = 5;
                makeCustomAnimation(rlMainOnlineSupport, mIndex, new OnlineSupportFragment());

                Map<String, String> params = new HashMap<>();
                if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
                    params.put("User", "Registered");
                } else {
                    params.put("User", "New");
                }
                FlurryAgent.logEvent("Contact Online Support", params);
                break;
            case R.id.ll_trial_message:
                if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
                    AccountAdapter accountAdapter = new AccountAdapter(getActivity());
                    Account account = accountAdapter.getAccountById(accountId);

                    switch (account.getStatus()) {
                        case Account.Status.INACTIVE:
                            Intent profileSetup = new Intent(getActivity(), SignUpContinueActivity.class);
                            startActivity(profileSetup);
                            break;

                        case Account.Status.REQUEST:
                            if (account.getStatus() == Account.Status.REQUEST) {
                                FlurryAgent.logEvent("Entering Demo Classroom");
                                Intent intent = new Intent(getActivity(), ClassroomAssistantActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("fragment", MainFragment.class.getSimpleName());
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                            break;

                        case Account.Status.REQUEST_TRIAL:
                            if (account.getStatus() == Account.Status.REQUEST_TRIAL) {
                                FlurryAgent.logEvent("Entering Demo Classroom");
                                Intent intent = new Intent(getActivity(), ClassroomAssistantActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("fragment", MainFragment.class.getSimpleName());
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                            break;
                        default:
                            Intent trialExplain = new Intent(getActivity(), FragmentHolderActivity.class);
                            trialExplain.putExtra(FragmentHolderActivity.EXTRA_DISPLAY_FRAGMENT, TrialExplainFragment.class.getSimpleName());
                            startActivity(trialExplain);
                            break;
                    }
                }
                break;
            case R.id.free_trial:
                getActivity().finish();
                Intent trialSubmit = new Intent(getActivity(), TrialSubmitActivity.class);
                startActivity(trialSubmit);
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        String token = mPreferences.getString("access_token", null);
        String accountId = mPreferences.getString("account_id", null);
        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
            inflater.inflate(R.menu.menu_main_store, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager frgMgr = getFragmentManager();
        Fragment frg = frgMgr.findFragmentById(R.id.container);
        Fragment newFragment = null;

        switch (item.getItemId()) {
            case R.id.opt_promo_code:
                newFragment = new PromoCodeFragment();
                break;
            case R.id.opt_store:
                /*** New Rule - not blocking Credit Store ***/
                newFragment = new CreditStoreFragment();

                /*** Old Rule - blocking Credit Store before Account is Active ***/
                /*String accountId = mPreferences.getString("account_id", null);
                AccountAdapter accountAdapter = new AccountAdapter(getActivity());
                Account account = accountAdapter.getAccountById(accountId);
                if (account != null && account.getStatus() == 4) {
                    newFragment = new CreditStoreFragment();
                } else {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.main_unavailable))
                            .setMessage(getString(R.string.main_unavailable_msg))
                            .setPositiveButton(getString(R.string.main_unavailable_ok), null)
                            .show();
                }*/

                break;
            case R.id.opt_sign_up:
                FlurryAgent.logEvent("Clicked Sign Up(Main)");
                break;
        }

        if (newFragment != null) {
            if (frg == null) {
                frgMgr.beginTransaction()
                        .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                R.animator.fragment_out_new, R.animator.fragment_out_old)
                        .addToBackStack(null)
                        .add(R.id.container, newFragment).commit();
            } else {
                frgMgr.beginTransaction()
                        .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                R.animator.fragment_out_new, R.animator.fragment_out_old)
                        .addToBackStack(null)
                        .replace(R.id.container, newFragment).commit();
            }
        }
        return true;
    }

    @Override
    public boolean onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            if (account.getStatus() == Account.Status.REQUEST || account.getStatus() == Account.Status.REQUEST_TRIAL) {
                getTrialFAB.setVisibility(View.VISIBLE);
            }
            mDrawerLayout.closeDrawers();
        } else {
            getActivity().finish();
        }
        return false;
    }

    public void makeTransaction(FragmentManager fm, Fragment fragment, Fragment newFragment) {
        if (newFragment != null) {
            if (fragment == null) {
                fm.beginTransaction()
                        .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                R.animator.fragment_out_new, R.animator.fragment_out_old)
                        .addToBackStack(null)
                        .add(R.id.container, newFragment).commit();
            } else {
                fm.beginTransaction()
                        .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                R.animator.fragment_out_new, R.animator.fragment_out_old)
                        .addToBackStack(null)
                        .replace(R.id.container, newFragment).commit();
            }
        }
    }

    public void makeCustomAnimation(final View vwSelectedItem, int index, final Fragment targetFragment) {
        vwSelectedItem.setOnClickListener(null);

        mTop = vwSelectedItem.getTop();
        mHeight = vwSelectedItem.getHeight();
        mBottom = vwSelectedItem.getBottom();

        mLlMain.removeView(vwSelectedItem);

        final View vwSelectedItemSpace = new View(getActivity());
        vwSelectedItemSpace.setMinimumHeight(mHeight);
        mLlMain.addView(vwSelectedItemSpace, index);
        vwSelectedItemSpace.setBackgroundColor(Color.WHITE);
//        vwSelectedItem.setVisibility(View.INVISIBLE);

        final View vwWhiteBackgroundLayout = new View(getActivity());
        final RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        vwWhiteBackgroundLayout.setBackgroundColor(Color.WHITE);
        frame.addView(vwWhiteBackgroundLayout, params1);
        frame.addView(vwSelectedItem);

        int actionBarSize = (int) getResources().getDimension(R.dimen.actionbar_height);

        Animation translateAnimation;
        if (vwSelectedItem.getId() == R.id.rl_main_course) {
            translateAnimation = new TranslateAnimation(0, 0, vwSelectedItem.getTop(), 0);
        } else {
            translateAnimation = new TranslateAnimation(0, 0, vwSelectedItem.getTop(), 0 - actionBarSize);
        }

        translateAnimation.setFillEnabled(true);
        translateAnimation.setFillAfter(true);
        translateAnimation.setDuration(100);
        translateAnimation.setInterpolator(new DecelerateInterpolator() {
            @Override
            public float getInterpolation(float input) {
//                vwSelectedItem.bringToFront();
                params1.topMargin = -(Math.round(vwSelectedItem.getBottom() * input));
                vwWhiteBackgroundLayout.setLayoutParams(params1);
                return super.getInterpolation(input);
            }
        });

        Animation scaleAnimation = new ScaleAnimation(1f, 1f, 0, 1f, 1f, vwSelectedItem.getBottom());
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                vwWhiteBackgroundLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                FragmentManager fm = getFragmentManager();
                Fragment frg = fm.findFragmentById(R.id.container);

                Bundle bundle = new Bundle();
                bundle.putString("class_type", "normal");
                targetFragment.setArguments(bundle);

                if (frg == null) {
                    fm.beginTransaction().add(R.id.container, targetFragment).
                            addToBackStack(null)
                            .commit();
                } else {
                    fm.beginTransaction().replace(R.id.container, targetFragment).
                            addToBackStack(null).commit();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        scaleAnimation.setFillEnabled(true);
        scaleAnimation.setFillAfter(true);
        if (vwSelectedItem.getId() == R.id.rl_main_course) {
            scaleAnimation.setDuration(150);
        } else {
            scaleAnimation.setDuration(100);
        }
        scaleAnimation.setInterpolator(new DecelerateInterpolator());

        vwWhiteBackgroundLayout.startAnimation(scaleAnimation);
        vwSelectedItem.startAnimation(translateAnimation);
    }

    public void hideCustomLayout() {
        View views;
        if (mViewId == R.id.rl_package) {
            views = rlMyPackage;
            makeHideAnimation(views);
        } else if (mViewId == R.id.rl_main_course) {
            views = rlMainCourse;
            makeHideAnimation(views);
        } else if (mViewId == R.id.rl_main_tutor) {
            views = rlMainTutor;
            makeHideAnimation(views);
        } else if (mViewId == R.id.rl_main_daily_content) {
            views = rlMainDailyContent;
            makeHideAnimation(views);
        } else if (mViewId == R.id.rl_main_online_support) {
            views = rlMainOnlineSupport;
            makeHideAnimation(views);
        }
    }

    private void makeHideAnimation(final View vwSelectedItem) {

        final View vwWhiteBackgroundLayout = new View(getActivity());
        vwWhiteBackgroundLayout.setMinimumHeight(mHeight);
        mLlMain.removeView(vwSelectedItem);

        mLlMain.addView(vwWhiteBackgroundLayout, mIndex);

        final View vwBackgroundAnimation = new View(getActivity());
        final RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        vwBackgroundAnimation.setBackgroundColor(Color.WHITE);
        frame.addView(vwBackgroundAnimation, params1);
        frame.addView(vwSelectedItem);

        int actionBarSize = (int) getResources().getDimension(R.dimen.actionbar_height);

        Animation translateAnimation = new TranslateAnimation(0, 0, 0, mTop + actionBarSize);
        translateAnimation.setFillEnabled(true);
        translateAnimation.setFillAfter(true);
        translateAnimation.setDuration(200);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                frame.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                vwSelectedItem.clearAnimation();
                frame.removeView(vwSelectedItem);
                mLlMain.removeView(vwWhiteBackgroundLayout);

                mLlMain.addView(vwSelectedItem, mIndex);

                frame.removeView(vwBackgroundAnimation);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        final Animation scaleAnimation = new ScaleAnimation(1f, 1f, 1f, 0, 0, mBottom);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                vwBackgroundAnimation.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        scaleAnimation.setFillEnabled(true);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setDuration(200);
        scaleAnimation.setInterpolator(new DecelerateInterpolator());

        vwBackgroundAnimation.startAnimation(scaleAnimation);
        vwSelectedItem.startAnimation(translateAnimation);
    }

    public Drawable createImage() {
        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.drawer_menu);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_person);
        AccountAdapter adapter = new AccountAdapter(getActivity());
        int count = adapter.getNotiCount();

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        c.drawBitmap(bitmap, 0, 0, paint);

        Paint paint1 = new Paint();
        paint1.setColor(Color.WHITE);
        paint1.setTextSize(w * 0.425f);

        Paint paint2 = new Paint();
        paint2.setColor(ContextCompat.getColor(getActivity(), R.color.colorNoti));
        paint2.setAntiAlias(true);

        String text = String.valueOf(count);

        if (!text.equals("0")) {
            if (text.length() == 1) {
                c.drawCircle(w * 0.72f, h * 0.74f, w * 0.28f, paint2);
                c.drawText(text, w * 0.6f, h * 0.88f, paint1);
            }

            if (text.length() > 1) {
                c.drawCircle(w * 0.72f, h * 0.74f, w * 0.28f, paint2);
                c.drawText(text, w * 0.491f, h * 0.88f, paint1);
            }
        }

        return new BitmapDrawable(getResources(), b);
    }

    private class MainImageSlideAdapter extends PagerAdapter {
        private Context mContext;

        private MainImageSlideAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mMainSlider.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            SimpleDraweeView sdvMainImage = new SimpleDraweeView(mContext);
            sdvMainImage.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FIT_XY);
            Uri uri = new Uri.Builder().scheme(UriUtil.LOCAL_RESOURCE_SCHEME).path(String.valueOf(mMainSlider[position])).build();
            sdvMainImage.setImageURI(uri);
            container.addView(sdvMainImage);
            return sdvMainImage;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((SimpleDraweeView) object);
        }
    }

    public class NotificationListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("inc.osbay.android.tutormandarin.NOTIFICATION")) {
                toolBar.setNavigationIcon(createImage());
            }
        }
    }
}
