package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flurry.android.FlurryAgent;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.database.TutorAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.sdk.model.Tutor;
import inc.osbay.android.tutormandarin.sdk.util.LGCUtil;
import inc.osbay.android.tutormandarin.ui.activity.FragmentHolderActivity;
import inc.osbay.android.tutormandarin.ui.activity.TutorInfoActivity;
import inc.osbay.android.tutormandarin.ui.activity.TutorTimeSearchAllActivity;
import inc.osbay.android.tutormandarin.util.CalendarUtil;
import inc.osbay.android.tutormandarin.util.CommonUtil;

import static inc.osbay.android.tutormandarin.ui.fragment.TutorialFragment.TRIAL_REQUEST;

public class ApplyTrialFragment extends BackHandledFragment implements NumberPicker.OnValueChangeListener, View.OnClickListener {

    public static final String EXTRA_WEEK_NO = "TutorTimeSelectFragment.EXTRA_WEEK_NO";
    public static final String EXTRA_TUTOR_ID = "TutorTimeSelectFragment.EXTRA_TUTOR_ID";
    public static final String EXTRA_BOOKING_TYPE = "TutorTimeSelectFragment.EXTRA_BOOKING_TYPE";
    public static final String EXTRA_TUTOR_TIME = "TutorTimeSelectFragment.EXTRA_TUTOR_TIME";
    private Button mButton;
    private ImageView tutorialImg;
    private TextView trialTitle;
    private LinearLayout timezoneLayout;
    private LinearLayout trialHeaderLayout;
    private RelativeLayout toolbarLayout;
    private NumberPicker mHourPicker;
    private NumberPicker mMinutePicker;
    private ServerRequestManager mServerRequestManager;
    private String[] mHours = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12",
            "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"};
    private String[] mMinutes = {"00", "30"};
    private TextView mStartTimeTextView;
    private TextView mEndTimeTextView;
    private String mTutorTime;
    private Calendar selectedDate = null;
    private Calendar firstDate = null;
    private Tutor mTutor;
    private String mSelectedTutorId;
    private int mWeekNo;
    private LinearLayout bookTrialFooter;
    private WeekPagerAdapter mWeekPagerAdapter;
    private ImageView mAvailableImageView;
    private TextView mNotAvailableTextView;
    private TextView mSelectedMonthTextView;
    private TextView mSelectedDateTextView;
    private TextView mSelectedMonth1;
    private TutorAdapter mTutorDbAdapter;
    private AvailableTutorAdapter mAdapter;
    private List<Tutor> mTutorList;
    private RecyclerView rvAvailableTutors;

    private ViewPager vpCalendarWeek;
    private Button mConfirmButton;
    private View footerViewGradient;
    private View dotlineView;
    private TextView trialTodayTV;
    private TextView submitTrialTV;
    private SharedPreferences mPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FlurryAgent.logEvent("Applying trial screen is loaded");
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        SharedPreferences preferences = getActivity().getSharedPreferences(TRIAL_REQUEST, Context.MODE_PRIVATE);
        int trialBookingTutorial = preferences.getInt(TRIAL_REQUEST, 0);

        if (trialBookingTutorial == 0) {
            FlurryAgent.logEvent("Trial tutorial");
            Intent intent = new Intent(getActivity(), FragmentHolderActivity.class);
            intent.putExtra(FragmentHolderActivity.EXTRA_DISPLAY_FRAGMENT,
                    TutorialFragment.class.getSimpleName());
            intent.putExtra(TutorialFragment.TUTORIAL_NO, 5);
            startActivity(intent);
        }

        mWeekPagerAdapter = new WeekPagerAdapter();
        mServerRequestManager = new ServerRequestManager(getActivity().getApplicationContext());
        mTutorDbAdapter = new TutorAdapter(getActivity().getApplicationContext());

        firstDate = GregorianCalendar.getInstance(Locale.getDefault());
        if (mWeekNo > 0 && mWeekNo < 5) {
            firstDate.setTimeInMillis(CalendarUtil.getFirstDateOfWeek(mWeekNo).getTime());
        } else {
            firstDate.setTimeInMillis(CalendarUtil.getFirstDateOfWeek(1).getTime());
        }
        if (mTutorTime == null) {
            selectedDate = firstDate;
        } else {
            selectedDate = GregorianCalendar.getInstance(Locale.getDefault());
            selectedDate.setTime(CommonUtil.convertStringToDate(mTutorTime, "yyyy-MM-dd HH:mm:ss"));
        }
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.tu_dialog_download_loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        mServerRequestManager.downloadTrialTutorList(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                progressDialog.dismiss();
            }

            @Override
            public void onError(ServerError err) {
                progressDialog.dismiss();
                if (getActivity() != null) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.tu_dialog_download_error))
                            .setMessage(err.getMessage())
                            .setPositiveButton(getString(R.string.tu_dialog_download_ok), null)
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_tutor_time_select, container, false);

        mButton = rootView.findViewById(R.id.btn_confirm);
        tutorialImg = rootView.findViewById(R.id.imv_booking_rule);
        trialTitle = rootView.findViewById(R.id.trial_title);
        trialTitle.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Montserrat-Bold.ttf"));
        trialTitle.setText(getString(R.string.apply_free_trial));
        timezoneLayout = rootView.findViewById(R.id.ll_time_zone);
        trialHeaderLayout = rootView.findViewById(R.id.trial_header);
        toolbarLayout = rootView.findViewById(R.id.rl_header);
        bookTrialFooter = rootView.findViewById(R.id.trial_footer);
        footerViewGradient = rootView.findViewById(R.id.footer_view_gradient);
        dotlineView = rootView.findViewById(R.id.dotline_view);
        trialHeaderLayout.setVisibility(View.VISIBLE);
        timezoneLayout.setVisibility(View.GONE);
        trialTitle.setVisibility(View.VISIBLE);
        mButton.setVisibility(View.GONE);
        tutorialImg.setVisibility(View.GONE);
        bookTrialFooter.setVisibility(View.VISIBLE);
        footerViewGradient.setVisibility(View.VISIBLE);
        dotlineView.setVisibility(View.GONE);

        LinearLayout llTutorTimeSearchAll = rootView.findViewById(R.id.ll_tutor_time_search_all);
        llTutorTimeSearchAll.setOnClickListener(this);

        vpCalendarWeek = rootView.findViewById(R.id.vp_calendar_week);
        vpCalendarWeek.setAdapter(mWeekPagerAdapter);
        vpCalendarWeek.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.e("TutorCalendar", "Selected week - " + position);
                reloadTutorSchedules();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        LinearLayout llSelectedTutor = rootView.findViewById(R.id.ll_selected_tutor);
        rvAvailableTutors = rootView.findViewById(R.id.rv_available_tutors);

        ImageView imvClose = rootView.findViewById(R.id.imv_ic_close);
        imvClose.setOnClickListener(this);

        mSelectedMonthTextView = rootView.findViewById(R.id.tv_selected_month);
        mSelectedMonthTextView.setText(CommonUtil.getFormattedDate(selectedDate.getTime(), "MMMM yyyy", false));

        mSelectedDateTextView = rootView.findViewById(R.id.tv_selected_date);
        mSelectedMonth1 = rootView.findViewById(R.id.tv_selected_month_1);

        mSelectedDateTextView.setText(CommonUtil.getFormattedDate(selectedDate.getTime(), "d", true));
        mSelectedMonth1.setText(CommonUtil.getFormattedDate(selectedDate.getTime(), "MMM", false));
        TextView tvTutorTimeSelectRate = rootView.findViewById(R.id.tv_tutor_time_select_rate);

        SimpleDraweeView sdvTutorPhoto = rootView.findViewById(R.id.sdv_tutor_photo);

        if (mTutor == null) {
            llSelectedTutor.setVisibility(View.GONE);
            rvAvailableTutors.setVisibility(View.VISIBLE);

            mTutorList = new ArrayList<>();
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            rvAvailableTutors.setLayoutManager(layoutManager);

            mAdapter = new AvailableTutorAdapter();
            rvAvailableTutors.setAdapter(mAdapter);
        } else {
            rvAvailableTutors.setVisibility(View.GONE);
            llSelectedTutor.setVisibility(View.VISIBLE);

            if (!TextUtils.isEmpty(mTutor.getAvatar())) {
                sdvTutorPhoto.setImageURI(Uri.parse(mTutor.getAvatar()));
                tvTutorTimeSelectRate.setText(getString(R.string.tu_rate_text, String.valueOf(mTutor.getCreditWeight())));
            }
        }

        mStartTimeTextView = rootView.findViewById(R.id.tv_start_time);
        mEndTimeTextView = rootView.findViewById(R.id.tv_end_time);
        mEndTimeTextView.setText("00:50");
        mAvailableImageView = rootView.findViewById(R.id.imv_tutor_available);
        mNotAvailableTextView = rootView.findViewById(R.id.tv_not_available);

        mHourPicker = rootView.findViewById(R.id.np_book_hour);
        mHourPicker.setMinValue(0);
        mHourPicker.setMaxValue(23);
        mHourPicker.setDisplayedValues(mHours);
        mHourPicker.setOnValueChangedListener(this);

        mMinutePicker = rootView.findViewById(R.id.np_book_minute);
        mMinutePicker.setDisplayedValues(null);
        mMinutePicker.setMinValue(0);
        mMinutePicker.setMaxValue(1);
        mMinutePicker.setDisplayedValues(mMinutes);
        mMinutePicker.setOnValueChangedListener(this);

        if (mTutorTime != null) {
            String hour = CommonUtil.getCustomDateResult(mTutorTime, "yyyy-MM-dd HH:mm:ss", "HH");
            String minute = CommonUtil.getCustomDateResult(mTutorTime, "yyyy-MM-dd HH:mm:ss", "mm");

            int h = Integer.parseInt(hour);

            int m;
            if (minute.equals("30")) {
                m = 1;
            } else {
                m = 0;
            }

            mHourPicker.setValue(h);
            mMinutePicker.setValue(m);

            mStartTimeTextView.setText(getStartTime());
            mEndTimeTextView.setText(getEndTime());
        }

        toolbarLayout.setBackgroundColor(getContext().getColor(R.color.light_red));
        setDividerColor(mHourPicker, getContext().getColor(R.color.light_red));
        setDividerColor(mMinutePicker, getContext().getColor(R.color.light_red));

        trialTodayTV = rootView.findViewById(R.id.trial_today);
        trialTodayTV.setOnClickListener(this);
        submitTrialTV = rootView.findViewById(R.id.submit);
        submitTrialTV.setTextColor(getContext().getResources().getColor(R.color.pale_red));
        submitTrialTV.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_tutor_time_search_all:
                FlurryAgent.logEvent("Clicking 'Search All Tutors' for Trial");
                Intent intentSearch = new Intent(getActivity(), TutorTimeSearchAllActivity.class);
                if (mTutor != null) {
                    intentSearch.putExtra(TutorTimeSearchAllActivity.EXTRA_TUTOR_ID, mTutor.getTutorId());
                }
                intentSearch.putExtra(TutorTimeSearchAllActivity.EXTRA_COLOR, "light_red");
                intentSearch.putExtra(TutorTimeSearchAllActivity.EXTRA_BOOKING_TYPE, 0);
                intentSearch.putExtra(TutorTimeSearchAllActivity.EXTRA_SELECTED_TIME, CommonUtil.getDateStringFormat(selectedDate.getTime()));
                intentSearch.putExtra(TutorTimeSearchAllActivity.EXTRA_SELECTED_CLASS, mStartTimeTextView.getText().toString());
                intentSearch.putExtra(TutorTimeSearchAllActivity.EXTRA_SELECTED_CLASS_HR_POSITION, mHourPicker.getValue());
                intentSearch.putExtra(TutorTimeSearchAllActivity.EXTRA_SELECTED_CLASS_MIN_POSITION, mMinutePicker.getValue());

                startActivityForResult(intentSearch, 111);
                break;
            case R.id.imv_ic_close:
                onBackPressed();
                break;
            case R.id.trial_today:
                FlurryAgent.logEvent("Clicking Trial today");
                FragmentManager fm = getFragmentManager();
                Fragment frg = fm.findFragmentById(R.id.fl_trial_submit);
                Fragment onlineSupportFragment = new OnlineSupportFragment();
                Bundle bundle = new Bundle();
                bundle.putString("class_type", "trial");
                onlineSupportFragment.setArguments(bundle);
                if (frg == null) {
                    fm.beginTransaction()
                            .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                    R.animator.fragment_out_new, R.animator.fragment_out_old)
                            .addToBackStack(null)
                            .add(R.id.fl_trial_submit, onlineSupportFragment).commit();
                } else {
                    fm.beginTransaction()
                            .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                    R.animator.fragment_out_new, R.animator.fragment_out_old)
                            .addToBackStack(null)
                            .replace(R.id.fl_trial_submit, onlineSupportFragment).commit();
                }
                break;
            case R.id.submit:

                boolean isTutorSelected = false;
                String selectedTutorId = null;

                if (mTutorList == null) {
                    mTutorList = new ArrayList<>();
                    mTutorList = mTutorDbAdapter.getAvailableTutorsForLesson(getStartTimeFull(),
                            getSecondBlock());
                }

                if (!TextUtils.isEmpty(mSelectedTutorId)) {
                    for (Tutor tutor : mTutorList) {
                        if (tutor.getTutorId().equals(mSelectedTutorId)) {
                            isTutorSelected = true;
                            selectedTutorId = mSelectedTutorId;
                        }
                    }
                } else if (mTutor != null) {
                    isTutorSelected = mTutorDbAdapter.checkTutorAvailableForLesson(
                            mTutor.getTutorId(), getStartTimeFull(),
                            getSecondBlock());
                    selectedTutorId = mTutor.getTutorId();
                }

                if (!isTutorSelected) {
                    Toast.makeText(getActivity(), getString(R.string.tu_select_tutor_first), Toast.LENGTH_SHORT).show();
                    break;
                } else {
                    final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage(getString(R.string.bo_dialog_booking));
                    progressDialog.show();
                    try {
                        mServerRequestManager.applyTrialClass(LGCUtil.convertToUTC(getStartTimeFull()), selectedTutorId,
                                new ServerRequestManager.OnRequestFinishedListener() {
                                    @Override
                                    public void onSuccess(Object result) {
                                        progressDialog.dismiss();
                                        if (getActivity() != null) {
                                            //showDialog();
                                            FlurryAgent.logEvent("Submit Trial after 24 hour");
                                            String token = mPreferences.getString("access_token", null);
                                            String accountId = mPreferences.getString("account_id", null);

                                            EditNumberFragment editNumberFragment = new EditNumberFragment();
                                            Bundle bundle = new Bundle();
                                            bundle.putString(EditNumberFragment.sourceFragment, ApplyTrialFragment.class.getSimpleName());
                                            editNumberFragment.setArguments(bundle);

                                            if (!TextUtils.isEmpty(accountId) && !TextUtils.isEmpty(token)) {
                                                AccountAdapter accountAdapter = new AccountAdapter(getActivity());
                                                Account account = accountAdapter.getAccountById(accountId);
                                                if ((account.getStatus() == Account.Status.REQUEST
                                                        || account.getStatus() == Account.Status.REQUEST_TRIAL)
                                                        && TextUtils.isEmpty(account.getPhoneNumber())) {

                                                    getFragmentManager().beginTransaction()
                                                            .replace(R.id.fl_trial_submit, editNumberFragment)
                                                            .addToBackStack(null)
                                                            .commit();
                                                } else {
                                                    getActivity().finish();
                                                }
                                            } else {
                                                getFragmentManager().beginTransaction()
                                                        .replace(R.id.fl_trial_submit, editNumberFragment)
                                                        .addToBackStack(null)
                                                        .commit();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onError(ServerError err) {
                                        progressDialog.dismiss();
                                        FlurryAgent.logEvent("Submitting after 24 hour trial failed");
                                        if (getActivity() != null) {
                                            new AlertDialog.Builder(getActivity())
                                                    .setTitle(getString(R.string.bo_dialog_class_error))
                                                    .setMessage(err.getMessage())
                                                    .setPositiveButton(getString(R.string.bo_dialog_class_ok), null)
                                                    .create()
                                                    .show();
                                        }
                                    }
                                });
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), getString(R.string.bo_parse_exception), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            mSelectedTutorId = data.getStringExtra(EXTRA_TUTOR_ID);
            submitTrialTV.setTextColor(getContext().getResources().getColor(R.color.light_red));
            if (mAdapter == null) {
                mAdapter = new AvailableTutorAdapter();
                rvAvailableTutors.setAdapter(mAdapter);
            } else {
                mAdapter.notifyDataSetChanged();
            }

            mTutorTime = data.getStringExtra(EXTRA_TUTOR_TIME);
            if (mTutorTime != null) {
                selectedDate = GregorianCalendar.getInstance(Locale.getDefault());
                selectedDate.setTime(CommonUtil.convertStringToDate(mTutorTime, "yyyy-MM-dd HH:mm:ss"));

                refreshTutorStatus();

                String hour = CommonUtil.getCustomDateResult(mTutorTime, "yyyy-MM-dd HH:mm:ss", "HH");
                String minute = CommonUtil.getCustomDateResult(mTutorTime, "yyyy-MM-dd HH:mm:ss", "mm");

                int h = Integer.parseInt(hour);

                int m;
                if (minute.equals("30")) {
                    m = 1;
                } else {
                    m = 0;
                }

                mHourPicker.setValue(h);
                mMinutePicker.setValue(m);

                mStartTimeTextView.setText(getStartTime());
                mEndTimeTextView.setText(getEndTime());
            }
        }

        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(getActivity(), TutorInfoActivity.class);
            intent.putExtra(TutorInfoActivity.EXTRA_TUTOR_ID, data.getStringExtra(EXTRA_TUTOR_ID));
            intent.putExtra(TutorInfoActivity.EXTRA_TUTOR_TIME, data.getStringExtra(EXTRA_TUTOR_TIME));
            intent.putExtra(TutorInfoActivity.EXTRA_BOOKING_TYPE, data.getIntExtra(EXTRA_BOOKING_TYPE, 0));
            startActivityForResult(intent, 0);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        setTitle("");
        hideActionBar();

        reloadTutorSchedules();

        refreshTutorStatus();
    }

    private void reloadTutorSchedules() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.tu_dialog_reload_loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            Calendar start = GregorianCalendar.getInstance(Locale.getDefault());
            if (mWeekNo > 0 && mWeekNo < 5) {
                start.setTimeInMillis(CalendarUtil.getFirstDateOfWeek(mWeekNo).getTime());
            } else {
                start.setTimeInMillis(CalendarUtil.getFirstDateOfWeek(vpCalendarWeek.getCurrentItem() + 1).getTime());
            }

            String startDate = CommonUtil.getFormattedDate(start.getTime(), "yyyy-MM-dd", false);
            String endDate = CommonUtil.getFormattedDate(new Date(start.getTimeInMillis()
                    + CalendarUtil.ONE_DAY_IN_MILLI * 7), "yyyy-MM-dd", false);

            int scheduleType = 0;

            mServerRequestManager.downloadTrialTutorSchedules(LGCUtil.convertToUTC(startDate + " 00:00:00"),
                    LGCUtil.convertToUTC(endDate + " 00:00:00"), scheduleType,
                    new ServerRequestManager.OnRequestFinishedListener() {
                        @Override
                        public void onSuccess(Object result) {
                            progressDialog.dismiss();

                            if (getActivity() != null) {
                                //ignore
                                refreshTutorStatus();
                            }
                        }

                        @Override
                        public void onError(ServerError err) {
                            progressDialog.dismiss();
                            if (getActivity() != null) {
                                if (err.getErrorCode() == ServerError.Code.NO_TUTOR_SCHEDULE) {
                                    new AlertDialog.Builder(getActivity())
                                            .setTitle(getString(R.string.tu_dialog_no_schedule_error))
                                            .setMessage(getString(R.string.tu_no_tutor_schedule))
                                            .setPositiveButton(getString(R.string.tu_dialog_no_schedule_ok), null)
                                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                @Override
                                                public void onDismiss(DialogInterface dialogInterface) {
                                                    onBackPressed();
                                                }
                                            })
                                            .create()
                                            .show();
                                } else {
                                    new AlertDialog.Builder(getActivity())
                                            .setTitle(getString(R.string.tu_dialog_schedule_error))
                                            .setMessage(err.getMessage())
                                            .setPositiveButton(getString(R.string.tu_dialog_schedule_ok), null)
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
                        }
                    });
        } catch (ParseException e) {
            progressDialog.dismiss();

            if (getActivity() != null) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.tu_dialog_parse_error))
                        .setMessage(e.getMessage())
                        .setPositiveButton(getString(R.string.tu_dialog_parse_ok), null)
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
    }

    @Override
    public boolean onBackPressed() {
        if (getFragmentManager() != null) {
            FlurryAgent.logEvent("Back to Book Trial or Skip page");
            getFragmentManager().popBackStack();
        }
        return false;
    }

    private void setDividerColor(NumberPicker picker, int color) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException | IllegalAccessException
                        | Resources.NotFoundException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        mStartTimeTextView.setText(getStartTime());
        mEndTimeTextView.setText(getEndTime());

        refreshTutorStatus();
    }

    private String getStartTime() {
        return mHours[mHourPicker.getValue()] + ":" + mMinutes[mMinutePicker.getValue()];
    }

    private String getEndTime() {
        if (mMinutePicker.getValue() == 0) {
            return mHours[mHourPicker.getValue()] + ":50";
        } else {
            return mHours[(mHourPicker.getValue() + 1) % mHours.length] + ":20";
        }

    }

    private String getStartTimeFull() {
        return CommonUtil.getFormattedDate(selectedDate.getTime(), "yyyy-MM-dd ", false) + getStartTime() + ":00";
    }

    private void refreshTutorStatus() {
        boolean isOver24 = CommonUtil.convertStringToDate(getStartTimeFull(), "yyyy-MM-dd HH:mm:ss").getTime()
                > new Date().getTime() + CalendarUtil.ONE_DAY_IN_MILLI;

        if (mTutor != null) {
            boolean isAvailable = mTutorDbAdapter.checkTutorAvailableForLesson(
                    mTutor.getTutorId(), getStartTimeFull(),
                    getSecondBlock());

            if (isAvailable && isOver24) {
                mNotAvailableTextView.setVisibility(View.GONE);
                mAvailableImageView.setVisibility(View.VISIBLE);
                mConfirmButton.setEnabled(true);
            } else {
                mNotAvailableTextView.setVisibility(View.VISIBLE);
                mAvailableImageView.setVisibility(View.GONE);
                mConfirmButton.setEnabled(false);
            }
        } else {
            if (isOver24) {
                mTutorList = mTutorDbAdapter.getAvailableTutorsForLesson(getStartTimeFull(),
                        getSecondBlock());
            } else {
                mTutorList = new ArrayList<>();
            }
            mAdapter.notifyDataSetChanged();
        }

    }

    private String getSecondBlock() {
        long secondTimeBlock = CommonUtil.convertStringToDate(getStartTimeFull(), "yyyy-MM-dd HH:mm:ss").getTime() + 1800000;
        return CommonUtil.getFormattedDate(new Date(secondTimeBlock), "yyyy-MM-dd HH:mm:ss", false);
    }

    private class AvailableTutorAdapter extends RecyclerView.Adapter<AvailableTutorAdapter.ViewHolder> {

        @Override
        public AvailableTutorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_available_tutor, parent, false);

            return new AvailableTutorAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(AvailableTutorAdapter.ViewHolder holder, int position) {
            final Tutor tutor = mTutorList.get(position);

            if (!TextUtils.isEmpty(tutor.getAvatar())) {
                holder.sdvTutorPhoto.setImageURI(Uri.parse(tutor.getAvatar()));
            }

            holder.tvTutorTimeSelectRate.setText(getString(R.string.tu_rate_text, String.valueOf(mTutorList.get(position).getCreditWeight())));

            if (tutor.getTutorId().equals(mSelectedTutorId)) {
                holder.imvSelectedMark.setVisibility(View.VISIBLE);
            } else {
                holder.imvSelectedMark.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), TutorInfoActivity.class);
                    intent.putExtra(TutorInfoActivity.EXTRA_TUTOR_ID, tutor.getTutorId());
                    startActivityForResult(intent, 0);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mTutorList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            SimpleDraweeView sdvTutorPhoto;
            TextView tvTutorTimeSelectRate;
            ImageView imvSelectedMark;

            public ViewHolder(View itemView) {
                super(itemView);

                sdvTutorPhoto = itemView.findViewById(R.id.sdv_tutor_photo);
                tvTutorTimeSelectRate = itemView.findViewById(R.id.tv_tutor_time_select_rate);
                imvSelectedMark = itemView.findViewById(R.id.imv_selected_mark);
            }
        }
    }

    private class WeekPagerAdapter extends PagerAdapter {

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup collection, int position) {
            RecyclerView rvWeek = new RecyclerView(getActivity());
            rvWeek.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 7);

            rvWeek.setLayoutManager(layoutManager);
            rvWeek.setAdapter(new WeekDateAdapter(position));

            collection.addView(rvWeek);
            return rvWeek;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup collection, int position, @NonNull Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            if (mWeekNo > 0 && mWeekNo < 5) {
                return 1;
            } else {
                return 4;
            }
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(R.string.tu_package_title);
        }
    }

    private class WeekDateAdapter extends RecyclerView.Adapter<WeekDateAdapter.ViewHolder> {

        private int mWeekNo; // must be 0 to 5

        public WeekDateAdapter(int weekNo) {
            mWeekNo = weekNo;
        }

        @Override
        public WeekDateAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_week_date, parent, false);

            return new WeekDateAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(WeekDateAdapter.ViewHolder holder, int position) {

            final Calendar date = GregorianCalendar.getInstance(Locale.getDefault());
            date.setTimeInMillis(firstDate.getTimeInMillis()
                    + (CalendarUtil.ONE_DAY_IN_MILLI * mWeekNo * 7)
                    + (CalendarUtil.ONE_DAY_IN_MILLI * position));

            holder.tvDay.setText(CommonUtil.getFormattedDate(date.getTime(), "EEE", false));
            holder.tvDate.setText(String.valueOf(date.get(Calendar.DATE)));

            if (selectedDate.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
                    selectedDate.get(Calendar.DATE) == date.get(Calendar.DATE)) {
                holder.tvDay.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_color_black));
                holder.tvDate.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_color_black));
                holder.vYellowLine.setVisibility(View.VISIBLE);
                holder.vYellowLine.setBackgroundColor(getContext().getResources().getColor(R.color.light_red));
            } else {
                holder.tvDay.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_color_gray));
                holder.tvDate.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_color_gray));
                holder.vYellowLine.setVisibility(View.INVISIBLE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedDate = date;
                    mSelectedMonthTextView.setText(CommonUtil.getFormattedDate(selectedDate.getTime(), "MMMM yyyy", false));
                    mSelectedDateTextView.setText(CommonUtil.getFormattedDate(selectedDate.getTime(), "d", true));

                    mSelectedMonth1.setText(CommonUtil.getFormattedDate(selectedDate.getTime(), "MMM", false));

                    notifyDataSetChanged();

                    mWeekPagerAdapter.notifyDataSetChanged();
                    refreshTutorStatus();
                }
            });
        }

        @Override
        public int getItemCount() {
            return 7;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView tvDay;
            TextView tvDate;
            View vYellowLine;

            public ViewHolder(View itemView) {
                super(itemView);

                tvDay = itemView.findViewById(R.id.tv_day);
                tvDate = itemView.findViewById(R.id.tv_date);
                vYellowLine = itemView.findViewById(R.id.v_yellow_line);
            }
        }
    }
}
