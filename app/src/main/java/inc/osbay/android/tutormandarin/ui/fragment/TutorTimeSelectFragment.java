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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flurry.android.FlurryAgent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.TutorAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Booking;
import inc.osbay.android.tutormandarin.sdk.model.Tutor;
import inc.osbay.android.tutormandarin.sdk.util.LGCUtil;
import inc.osbay.android.tutormandarin.ui.activity.FragmentHolderActivity;
import inc.osbay.android.tutormandarin.ui.activity.TutorInfoActivity;
import inc.osbay.android.tutormandarin.ui.activity.TutorTimeSearchAllActivity;
import inc.osbay.android.tutormandarin.ui.activity.TutorTimeSelectActivity;
import inc.osbay.android.tutormandarin.util.CalendarUtil;
import inc.osbay.android.tutormandarin.util.CommonUtil;

import static inc.osbay.android.tutormandarin.sdk.client.ServerError.Code.BOOKING_DATE_NOT_AVAILABLE;
import static inc.osbay.android.tutormandarin.ui.fragment.TutorialFragment.PREF_TUTOR_TUTORIAL;

public class TutorTimeSelectFragment extends BackHandledFragment implements
        NumberPicker.OnValueChangeListener, View.OnClickListener {
    public static final String EXTRA_WEEK_NO = "TutorTimeSelectFragment.EXTRA_WEEK_NO";
    public static final String EXTRA_TUTOR_ID = "TutorTimeSelectFragment.EXTRA_TUTOR_ID";
    public static final String EXTRA_BOOKING_TYPE = "TutorTimeSelectFragment.EXTRA_BOOKING_TYPE";
    public static final String EXTRA_TUTOR_TIME = "TutorTimeSelectFragment.EXTRA_TUTOR_TIME";

    private NumberPicker mHourPicker;
    private NumberPicker mMinutePicker;
    private ServerRequestManager mServerRequestManager;
    private TutorAdapter mTutorDbAdapter;

    private AvailableTutorAdapter mAdapter;
    private List<Tutor> mTutorList;
    private RecyclerView rvAvailableTutors;
    private Tutor mTutor;
    private String mSelectedTutorId;

    private int mWeekNo;
    private int mBookingType;
    private String mTutorTime;

    private Calendar selectedDate = null;
    private Calendar firstDate = null;

    private String[] mHours = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12",
            "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"};
    private String[] mMinutes = {"00", "30"};
    private TextView mStartTimeTextView;
    private TextView mEndTimeTextView;
    private TextView mSelectedMonth1;

    private ViewPager vpCalendarWeek;
    private Button mConfirmButton;
    private ImageView mAvailableImageView;
    private TextView mNotAvailableTextView;
    private TextView mSelectedMonthTextView;
    private TextView mSelectedDateTextView;
    private WeekPagerAdapter mWeekPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWeekPagerAdapter = new WeekPagerAdapter();
        mServerRequestManager = new ServerRequestManager(getActivity().getApplicationContext());
        mTutorDbAdapter = new TutorAdapter(getActivity().getApplicationContext());

        SharedPreferences preferences = getActivity().getSharedPreferences(PREF_TUTOR_TUTORIAL, Context.MODE_PRIVATE);
        int scheduleTutorial = preferences.getInt(PREF_TUTOR_TUTORIAL, 0);

        if (scheduleTutorial == 0) {
            Intent intent = new Intent(getActivity(), FragmentHolderActivity.class);
            intent.putExtra(FragmentHolderActivity.EXTRA_DISPLAY_FRAGMENT,
                    TutorialFragment.class.getSimpleName());
            intent.putExtra(TutorialFragment.TUTORIAL_NO, 2);
            startActivity(intent);
        }

        Bundle bundle = getArguments();
        if (bundle != null) {
            mWeekNo = bundle.getInt(EXTRA_WEEK_NO);
            mBookingType = bundle.getInt(EXTRA_BOOKING_TYPE);
            mTutorTime = bundle.getString(EXTRA_TUTOR_TIME);

            String tutorId = bundle.getString(EXTRA_TUTOR_ID);
            if (!TextUtils.isEmpty(tutorId)) {
                mTutor = mTutorDbAdapter.getTutorById(tutorId);
            }
        }

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

        mServerRequestManager.downloadTutorList(new ServerRequestManager.OnRequestFinishedListener() {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tutor_time_select, container, false);

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

        ImageView imvRule = rootView.findViewById(R.id.imv_booking_rule);
        imvRule.setOnClickListener(this);

        mConfirmButton = rootView.findViewById(R.id.btn_confirm);
        mConfirmButton.setOnClickListener(this);

        mSelectedMonthTextView = rootView.findViewById(R.id.tv_selected_month);
        mSelectedMonthTextView.setText(CommonUtil.getFormattedDate(selectedDate.getTime(), "MMMM yyyy", false));

        mSelectedDateTextView = rootView.findViewById(R.id.tv_selected_date);
        mSelectedMonth1 = rootView.findViewById(R.id.tv_selected_month_1);

        mSelectedDateTextView.setText(CommonUtil.getFormattedDate(selectedDate.getTime(), "d", true));
        mSelectedMonth1.setText(CommonUtil.getFormattedDate(selectedDate.getTime(), "MMM", false));

        TextView tvTimeZone = rootView.findViewById(R.id.tv_time_zone);
        tvTimeZone.setText(TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT));

        TextView tvCurrentTime = rootView.findViewById(R.id.tv_current_time);
        tvCurrentTime.setText(CommonUtil.getFormattedDate(new Date(), "HH:mm", false));

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
        if (mBookingType == Booking.Type.LESSON) {
            mEndTimeTextView.setText("00:50");
        } else {
            mEndTimeTextView.setText("00:25");
        }

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

        setDividerColor(mHourPicker, Color.parseColor("#f6b512"));
        setDividerColor(mMinutePicker, Color.parseColor("#f6b512"));

        return rootView;
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

            int scheduleType;

            if (mBookingType == Booking.Type.PACKAGE) {
                scheduleType = 2;
            } else {
                scheduleType = 1;
            }

            mServerRequestManager.downloadTutorSchedules(LGCUtil.convertToUTC(startDate + " 00:00:00"),
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
        if (getActivity() instanceof TutorTimeSelectActivity) {
            getActivity().finish();
        } else {
            if (getFragmentManager() != null)
                getFragmentManager().popBackStack();
        }
        return false;
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
        mStartTimeTextView.setText(getStartTime());
        mEndTimeTextView.setText(getEndTime());

        refreshTutorStatus();
    }

    private void refreshTutorStatus() {
        boolean isOver24 = CommonUtil.convertStringToDate(getStartTimeFull(), "yyyy-MM-dd HH:mm:ss").getTime()
                > new Date().getTime() + CalendarUtil.ONE_DAY_IN_MILLI;

        if (mTutor != null) {
            boolean isAvailable;
            if (mBookingType == Booking.Type.LESSON) {
                isAvailable = mTutorDbAdapter.checkTutorAvailableForLesson(
                        mTutor.getTutorId(), getStartTimeFull(),
                        getSecondBlock());
            } else {
                isAvailable = mTutorDbAdapter.checkTutorAvailableForTopic(
                        mTutor.getTutorId(), getStartTimeFull());
            }

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
                if (mBookingType == Booking.Type.LESSON) {
                    mTutorList = mTutorDbAdapter.getAvailableTutorsForLesson(getStartTimeFull(),
                            getSecondBlock());
                } else {
                    mTutorList = mTutorDbAdapter.getAvailableTutorsForTopic(getStartTimeFull());
                }
            } else {
                mTutorList = new ArrayList<>();
            }
            mAdapter.notifyDataSetChanged();

            // Enable/Disable confirm button
            boolean isTutorSelected = false;
            if (!TextUtils.isEmpty(mSelectedTutorId)) {
                for (Tutor tutor : mTutorList) {
                    if (tutor.getTutorId().equals(mSelectedTutorId)) {
                        isTutorSelected = true;
                    }
                }
            }

            if (isTutorSelected && isOver24) {
                mConfirmButton.setEnabled(true);
            } else {
                mConfirmButton.setEnabled(false);
            }
        }

    }

    private String getStartTimeFull() {
        return CommonUtil.getFormattedDate(selectedDate.getTime(), "yyyy-MM-dd ", false) + getStartTime() + ":00";
    }

    private String getSecondBlock() {
        long secondTimeBlock = CommonUtil.convertStringToDate(getStartTimeFull(), "yyyy-MM-dd HH:mm:ss").getTime() + 1800000;
        return CommonUtil.getFormattedDate(new Date(secondTimeBlock), "yyyy-MM-dd HH:mm:ss", false);
    }

    private String getStartTime() {
        return mHours[mHourPicker.getValue()] + ":" + mMinutes[mMinutePicker.getValue()];
    }

    private String getEndTime() {
        if (mBookingType == Booking.Type.TOPIC) {
            if (mMinutePicker.getValue() == 0) {
                return mHours[mHourPicker.getValue()] + ":25";
            } else {
                return mHours[mHourPicker.getValue()] + ":55";
            }
        } else {
            if (mMinutePicker.getValue() == 0) {
                return mHours[mHourPicker.getValue()] + ":50";
            } else {
                return mHours[(mHourPicker.getValue() + 1) % mHours.length] + ":20";
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_tutor_time_search_all:
                Intent intentSearch = new Intent(getActivity(), TutorTimeSearchAllActivity.class);
                if (mTutor != null) {
                    intentSearch.putExtra(TutorTimeSearchAllActivity.EXTRA_TUTOR_ID, mTutor.getTutorId());
                }
                intentSearch.putExtra(TutorTimeSearchAllActivity.EXTRA_COLOR, "yellow");
                intentSearch.putExtra(TutorTimeSearchAllActivity.EXTRA_BOOKING_TYPE, mBookingType);
                intentSearch.putExtra(TutorTimeSearchAllActivity.EXTRA_SELECTED_TIME, CommonUtil.getDateStringFormat(selectedDate.getTime()));
                intentSearch.putExtra(TutorTimeSearchAllActivity.EXTRA_SELECTED_CLASS, mStartTimeTextView.getText().toString());
                intentSearch.putExtra(TutorTimeSearchAllActivity.EXTRA_SELECTED_CLASS_HR_POSITION, mHourPicker.getValue());
                intentSearch.putExtra(TutorTimeSearchAllActivity.EXTRA_SELECTED_CLASS_MIN_POSITION, mMinutePicker.getValue());

                startActivityForResult(intentSearch, 111);
                break;
            case R.id.imv_ic_close:
                onBackPressed();
                break;
            case R.id.imv_booking_rule:
                FlurryAgent.logEvent("Read credit booking rule");
                Intent intent = new Intent(getActivity(), FragmentHolderActivity.class);
                intent.putExtra(FragmentHolderActivity.EXTRA_DISPLAY_FRAGMENT,
                        TutorialFragment.class.getSimpleName());
                intent.putExtra(TutorialFragment.TUTORIAL_NO, 2);
                startActivity(intent);
                break;
            case R.id.btn_confirm:
                boolean isTutorSelected = false;
                String selectedTutorId = null;

                if (mTutorList == null) {
                    mTutorList = new ArrayList<>();
                    if (mBookingType == Booking.Type.LESSON) {
                        mTutorList = mTutorDbAdapter.getAvailableTutorsForLesson(getStartTimeFull(),
                                getSecondBlock());
                    } else {
                        mTutorList = mTutorDbAdapter.getAvailableTutorsForTopic(getStartTimeFull());
                    }
                }

                if (!TextUtils.isEmpty(mSelectedTutorId)) {
                    for (Tutor tutor : mTutorList) {
                        if (tutor.getTutorId().equals(mSelectedTutorId)) {
                            isTutorSelected = true;
                            selectedTutorId = mSelectedTutorId;
                        }
                    }
                } else if (mTutor != null) {
                    if (mBookingType == Booking.Type.LESSON) {
                        isTutorSelected = mTutorDbAdapter.checkTutorAvailableForLesson(
                                mTutor.getTutorId(), getStartTimeFull(),
                                getSecondBlock());
                    } else {
                        isTutorSelected = mTutorDbAdapter.checkTutorAvailableForTopic(
                                mTutor.getTutorId(), getStartTimeFull());
                    }
                    selectedTutorId = mTutor.getTutorId();
                }

                if (!isTutorSelected) {
                    Toast.makeText(getActivity(), getString(R.string.tu_select_tutor_first), Toast.LENGTH_SHORT).show();
                    break;
                } else {
                    try {
                        selectTimeAndTutor(selectedTutorId);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.e("Error", "Cannot parse start time.");
                    }
                }
                break;
        }
    }

    private void selectTimeAndTutor(final String selectedTutorId) throws ParseException {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.tu_dialog_select_tutor_time_loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        mServerRequestManager.checkTimeToBook(LGCUtil.convertToUTC(getStartTimeFull()), mBookingType,
                new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        progressDialog.dismiss();

                        if (getActivity() != null) {
                            if (getActivity() instanceof TutorTimeSelectActivity) {
                                Intent data = new Intent();
                                data.putExtra("EXTRA_TUTOR_ID", selectedTutorId);
                                data.putExtra("EXTRA_START_TIME", getStartTimeFull());

                                getActivity().setResult(Activity.RESULT_OK, data);
                                getActivity().finish();
                            } else {
                                Fragment newFragment = new MyBookingFragment();

                                Bundle bundle = new Bundle();
                                bundle.putString(MyBookingFragment.EXTRA_TUTOR_ID, selectedTutorId);
                                bundle.putString(MyBookingFragment.EXTRA_START_TIME, getStartTimeFull());
                                bundle.putInt(MyBookingFragment.EXTRA_BOOKING_TYPE, mBookingType);
                                newFragment.setArguments(bundle);

                                FragmentManager fm = getFragmentManager();
                                Fragment fragment = fm.findFragmentById(R.id.container);
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
                    }

                    @Override
                    public void onError(ServerError err) {
                        progressDialog.dismiss();

                        if (getActivity() != null) {
                            if (err.getErrorCode() == BOOKING_DATE_NOT_AVAILABLE) {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(getString(R.string.tu_dialog_already_booked))
                                        .setMessage(getString(R.string.tu_dialog_already_booked_msg))
                                        .setPositiveButton(getString(R.string.tu_dialog_already_booked_ok), null)
                                        .create()
                                        .show();
                            } else {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(getString(R.string.tu_dialog_booking_check_time_error))
                                        .setMessage(err.getMessage())
                                        .setPositiveButton(getString(R.string.tu_dialog_booking_check_time_ok), null)
                                        .create()
                                        .show();
                            }
                        }
                    }
                }
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            mSelectedTutorId = data.getStringExtra(EXTRA_TUTOR_ID);
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

    private class AvailableTutorAdapter extends RecyclerView.Adapter<AvailableTutorAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_available_tutor, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
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
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_week_date, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

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
