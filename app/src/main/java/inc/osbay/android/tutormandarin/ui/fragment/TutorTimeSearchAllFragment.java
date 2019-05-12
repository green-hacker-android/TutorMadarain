package inc.osbay.android.tutormandarin.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.TutorAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Booking;
import inc.osbay.android.tutormandarin.sdk.model.Tutor;
import inc.osbay.android.tutormandarin.ui.activity.TutorInfoActivity;
import inc.osbay.android.tutormandarin.ui.activity.TutorTimeSearchAllActivity;
import inc.osbay.android.tutormandarin.util.CalendarUtil;
import inc.osbay.android.tutormandarin.util.CommonUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class TutorTimeSearchAllFragment extends BackHandledFragment implements View.OnClickListener {
    public static final String EXTRA_COLOR = "TutorTimeSearchAllFragment.EXTRA_COLOR";
    public static final String EXTRA_SEARCH_TUTOR_ID = "TutorTimeSearchAllFragment.EXTRA_SEARCH_TUTOR_ID";
    public static final String EXTRA_SEARCH_DATE = "TutorTimeSearchAllFragment.EXTRA_SEARCH_DATE";
    public static final String EXTRA_SEARCH_BOOKING_TYPE = "TutorTimeSearchAllFragment.EXTRA_SEARCH_BOOKING_TYPE";
    public static final String EXTRA_SELECTED_CLASS = "TutorTimeSearchAllFragment.EXTRA_SELECTED_CLASS";
    public static final String EXTRA_SELECTED_CLASS_HR_POSITION = "TutorTimeSearchAllFragment.EXTRA_SELECTED_CLASS_POSITION";
    public static final String EXTRA_SELECTED_CLASS_MIN_POSITION = "TutorTimeSearchAllFragment.EXTRA_SELECTED_CLASS_MIN_POSITION";
    public RecyclerView rvSearchAllTutor;
    int firstOver24 = 0;
    int over24 = 0;
    int over24_ = 0;
    int firstOver24_ = 0;
    private GregorianCalendar mCalMonth;
    private CalendarAdapter mCalendarAdapter;
    private TextView mTvCalendarSelectedMonth;
    private TextView mTvTutorSearchAllDayMonth;
    private TextView mTvTutorSearchAllYear;
    private RelativeLayout mRlBgSearchAll;
    private RelativeLayout mRlSearchAllCalendarView;
    private List<Tutor> mTutorList;
    private List<String> mStartTimes;
    private List<String> mEndTimes;
    private String mToday;
    private String mSearchAllDate;
    private Date mSelectedClass;
    private int mBookingType;
    private int classStartHrPosition;
    private int classStartMinPosition;
    private Tutor mTutor;
    private ServerRequestManager mServerRequestManager;
    private TutorAllScheduleAdapter mTutorAllScheduleAdapter;
    private TutorAdapter mTutorDbAdapter;
    private String toolColor;

    public TutorTimeSearchAllFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mServerRequestManager = new ServerRequestManager(getActivity());
        mTutorDbAdapter = new TutorAdapter(getActivity());

        mStartTimes = new ArrayList<>();
        mEndTimes = new ArrayList<>();
        mTutorList = new ArrayList<>();

        if (getArguments() != null) {
            Bundle bundle = getArguments();
            toolColor = bundle.getString(EXTRA_COLOR);
            mSearchAllDate = bundle.getString(EXTRA_SEARCH_DATE);
            mBookingType = bundle.getInt(EXTRA_SEARCH_BOOKING_TYPE);
            Log.i("Booking Type", String.valueOf(mBookingType));
            String tutorId = bundle.getString(EXTRA_SEARCH_TUTOR_ID);
            classStartHrPosition = bundle.getInt(EXTRA_SELECTED_CLASS_HR_POSITION);
            classStartMinPosition = bundle.getInt(EXTRA_SELECTED_CLASS_MIN_POSITION);
            try {
                mSelectedClass = new SimpleDateFormat("HH:mm").parse(bundle.getString(EXTRA_SELECTED_CLASS));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (!TextUtils.isEmpty(tutorId)) {
                mTutor = mTutorDbAdapter.getTutorById(tutorId);
            }
        }

        // 1. start time
        // 2. end time
        mStartTimes = getScheduleTime(1);
        if (mBookingType == 3)
            mEndTimes = getScheduleTime(3);
        else
            mEndTimes = getScheduleTime(2);
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tutor_time_search_all, container, false);

        Toolbar toolbar = v.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTvTutorSearchAllDayMonth = toolbar.findViewById(R.id.tv_tutor_search_all_day_month);
        mTvTutorSearchAllDayMonth.setText(CommonUtil.getCustomDateResult(mSearchAllDate, "yyyy-MM-dd HH:mm:ss", "MMM dd"));

        if (toolColor.equalsIgnoreCase("light_red")) {
            toolbar.setBackgroundColor(getContext().getColor(R.color.light_red));
        }
        mTvTutorSearchAllYear = toolbar.findViewById(R.id.tv_tutor_search_all_year);
        mTvTutorSearchAllYear.setText(CommonUtil.getCustomDateResult(mSearchAllDate, "yyyy-MM-dd HH:mm:ss", "yyyy"));

        LinearLayout llTutorSearchAllDate = v.findViewById(R.id.ll_tutor_search_all_date);
        llTutorSearchAllDate.setOnClickListener(this);

        mTvCalendarSelectedMonth = v.findViewById(R.id.tv_calendar_selected_month);
        ImageView imvCalendarLeftArrow = v.findViewById(R.id.imv_calendar_left_arrow);
        ImageView imvCalendarRightArrow = v.findViewById(R.id.imv_calendar_right_arrow);
        ImageView imvTutorSearchAllBack = v.findViewById(R.id.imv_tutor_search_all_back);
        mRlBgSearchAll = v.findViewById(R.id.rl_bg_search_all);
        mRlSearchAllCalendarView = v.findViewById(R.id.rl_search_all_calendar_view);

        imvCalendarLeftArrow.setOnClickListener(this);
        imvCalendarRightArrow.setOnClickListener(this);
        imvTutorSearchAllBack.setOnClickListener(this);
        mRlBgSearchAll.setOnClickListener(this);

        Locale.setDefault(Locale.getDefault());

        // calendar view with recycle
        RecyclerView rvDateTitle = v.findViewById(R.id.rv_date_title);

        GridLayoutManager glmDateTitle = new GridLayoutManager(getActivity(), 7, LinearLayoutManager.VERTICAL, false);
        rvDateTitle.setLayoutManager(glmDateTitle);

        mCalMonth = (GregorianCalendar) GregorianCalendar.getInstance();

        mTvCalendarSelectedMonth.setText(android.text.format.DateFormat.format("MMM", mCalMonth));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        mToday = df.format(mCalMonth.getTime());

        mCalendarAdapter = new CalendarAdapter(mCalMonth);

        rvDateTitle.setAdapter(mCalendarAdapter);

        //bind tutor's schedule time list
        rvSearchAllTutor = v.findViewById(R.id.rv_search_all_tutors);
        LinearLayoutManager llmSearchAll = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvSearchAllTutor.setLayoutManager(llmSearchAll);

        mTutorAllScheduleAdapter = new TutorAllScheduleAdapter();
        rvSearchAllTutor.setAdapter(mTutorAllScheduleAdapter);

        for (int i = 0; i < mStartTimes.size(); i++) {
            boolean isOver24 = CommonUtil.convertStringToDate(mStartTimes.get(i), "yyyy-MM-dd HH:mm:ss").getTime()
                    > new Date().getTime() + CalendarUtil.ONE_DAY_IN_MILLI;
            Date matchDate = null;
            String classStart = null;

            if (isOver24) {
                over24_++;

                if (over24_ == 1) {
                    firstOver24_ = i;
                    classStart = CommonUtil.getCustomDateResult(mStartTimes.get(i), "yyyy-MM-dd HH:mm:ss", "HH:mm");

                    try {
                        matchDate = new SimpleDateFormat("HH:mm").parse(classStart);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    //boolean isSelectedGreater = ;

                    if (matchDate.getTime() < mSelectedClass.getTime()) {
                        if (classStartMinPosition == 0) {
                            firstOver24_ = classStartHrPosition * 2;
                        } else {
                            firstOver24_ = classStartHrPosition * 2 + 1;
                        }
                        break;

                    }
                }
            }
        }
        //rvSearchAllTutor.smoothScrollToPosition(firstOver24_);
        rvSearchAllTutor.scrollToPosition(firstOver24_);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
//        refreshTutors();

        getTutorSchedules();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_tutor_search_all_date:
                if (mRlSearchAllCalendarView.getVisibility() == View.GONE) {
                    mRlSearchAllCalendarView.setVisibility(View.VISIBLE);
                    mRlBgSearchAll.setVisibility(View.VISIBLE);
                } else {
                    mRlSearchAllCalendarView.setVisibility(View.GONE);
                    mRlBgSearchAll.setVisibility(View.GONE);
                }
                break;
            case R.id.rl_bg_search_all:
                if (mRlBgSearchAll.getVisibility() == View.VISIBLE) {
                    mRlSearchAllCalendarView.setVisibility(View.GONE);
                    mRlBgSearchAll.setVisibility(View.GONE);
                }
                break;
            case R.id.imv_calendar_left_arrow:
                setPreviousMonth();
                refreshCalendar();
                break;
            case R.id.imv_calendar_right_arrow:
                setNextMonth();
                refreshCalendar();
                break;
            case R.id.imv_tutor_search_all_back:
                onBackPressed();
                break;
        }
    }

    protected void setNextMonth() {
        int m1 = mCalMonth.get(GregorianCalendar.MONTH);
        int m2 = mCalMonth.getActualMaximum(GregorianCalendar.MONTH);

        if (m1 == m2) {
            mCalMonth.set((mCalMonth.get(GregorianCalendar.YEAR) + 1),
                    mCalMonth.getActualMinimum(GregorianCalendar.MONTH), 1);
        } else {
            mCalMonth.set(GregorianCalendar.MONTH,
                    mCalMonth.get(GregorianCalendar.MONTH) + 1);
        }
    }

    protected void setPreviousMonth() {
        int m1 = mCalMonth.get(GregorianCalendar.MONTH);
        int m2 = mCalMonth.getActualMinimum(GregorianCalendar.MONTH);

        if (m1 == m2) {
            mCalMonth.set((mCalMonth.get(GregorianCalendar.YEAR) - 1),
                    mCalMonth.getActualMaximum(GregorianCalendar.MONTH), 1);
        } else {
            mCalMonth.set(GregorianCalendar.MONTH,
                    mCalMonth.get(GregorianCalendar.MONTH) - 1);
        }
    }

    public void refreshCalendar() {
        mCalendarAdapter.refreshDays();
        mCalendarAdapter.notifyDataSetChanged();
        mTvCalendarSelectedMonth.setText(android.text.format.DateFormat.format("MMM", mCalMonth));
    }

    @Override
    public boolean onBackPressed() {
        if (getActivity() instanceof TutorTimeSearchAllActivity) {
            getActivity().finish();
        } else {
            getFragmentManager().popBackStack();
        }
        return false;
    }

    private void getTutorSchedules() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.tu_search_all_loading));
        progressDialog.show();

        Date searchADay = CommonUtil.convertStringToDate(mSearchAllDate, "yyyy-MM-dd HH:mm:ss");

        String startDate = CommonUtil.getFormattedDate(new Date((searchADay.getTime() - CalendarUtil.ONE_DAY_IN_MILLI)), "yyyy-MM-dd", false);
        String endDate = CommonUtil.getFormattedDate(new Date((searchADay.getTime() + CalendarUtil.ONE_DAY_IN_MILLI)), "yyyy-MM-dd", false);

        int scheduleType;

        if (mBookingType == Booking.Type.PACKAGE) {
            scheduleType = 2;
        } else {
            scheduleType = 1;
        }

        mServerRequestManager.downloadTutorSchedules(startDate, endDate, scheduleType, new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                progressDialog.dismiss();
                if (getActivity() != null) {
                    over24 = 0;
                    mTutorAllScheduleAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(ServerError err) {
                progressDialog.dismiss();
                if (getActivity() != null) {
                    over24 = 0;
                    mTutorAllScheduleAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private List<String> getScheduleTime(int type) {
        List<String> scheduleTimes = new ArrayList<>();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(CommonUtil.convertStringToDate(mSearchAllDate, "yyyy-MM-dd HH:mm:ss"));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        if (type == 1)/**** For Lesson Type ***/
            cal.set(Calendar.MINUTE, 0);
        else if (type == 3)/**** For Topic Type EndTime ***/
            cal.set(Calendar.MINUTE, 25);
        else/**** For Lesson Type EndTime ***/
            cal.set(Calendar.MINUTE, 50);
        cal.set(Calendar.SECOND, 0);

        for (int i = 0; i < 48; i++) {
            scheduleTimes.add(df.format(cal.getTime()));
            cal.add(Calendar.MINUTE, 30);
        }

        return scheduleTimes;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (getActivity() instanceof TutorInfoActivity) {
                Intent intent = new Intent();
                intent.putExtra(TutorInfoActivity.EXTRA_TUTOR_ID, data.getStringExtra(TutorTimeSearchAllActivity.EXTRA_TUTOR_ID));
                intent.putExtra(TutorInfoActivity.EXTRA_TUTOR_TIME, data.getStringExtra(TutorTimeSearchAllActivity.EXTRA_SELECTED_TIME));
                intent.putExtra(TutorInfoActivity.EXTRA_BOOKING_TYPE, data.getStringExtra(TutorTimeSearchAllActivity.EXTRA_BOOKING_TYPE));

                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
            }
        }
    }

    private String getEndTime(String startTimes) {
        long secondTimeBlock = CommonUtil.convertStringToDate(startTimes, "yyyy-MM-dd HH:mm:ss").getTime() + 1800000;
        return CommonUtil.getFormattedDate(new Date(secondTimeBlock), "yyyy-MM-dd HH:mm:ss", false);
    }

    private class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarHolder> {
        private GregorianCalendar pMonth;
        private GregorianCalendar pMonthMaxSet;
        private List<String> mDayString;
        private int firstDay;
        private int maxWeekNumber;
        private int maxP;
        private int calMaxP;
        private int monthLength;
        private String itemValue;
        private DateFormat df;
        private java.util.Calendar month;

        CalendarAdapter(GregorianCalendar calMonth) {
            mDayString = new ArrayList<>();

            month = calMonth;
            month.set(GregorianCalendar.DAY_OF_MONTH, 1);

            df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            refreshDays();
        }

        private void refreshDays() {
            mDayString.clear();

            pMonth = (GregorianCalendar) month.clone();

            // get the first day of month
            firstDay = month.get(GregorianCalendar.DAY_OF_WEEK);

            // get total week of month
            maxWeekNumber = month.getActualMaximum(GregorianCalendar.WEEK_OF_MONTH);

            // get total days of month
            monthLength = maxWeekNumber * 7;

            maxP = getMaxP();

            calMaxP = maxP - (firstDay - 1);

            pMonthMaxSet = (GregorianCalendar) pMonth.clone();

            pMonthMaxSet.set(GregorianCalendar.DAY_OF_MONTH, calMaxP + 1);

            for (int n = 0; n < monthLength; n++) {

                itemValue = df.format(pMonthMaxSet.getTime());
                pMonthMaxSet.add(GregorianCalendar.DATE, 1);
                mDayString.add(itemValue);

            }
        }

        private int getMaxP() {
            int maxP;
            int a = month.get(GregorianCalendar.MONTH);
            int b = month.getActualMinimum(GregorianCalendar.MONTH);

            if (a == b) {
                pMonth.set((month.get(GregorianCalendar.YEAR) - 1),
                        month.getActualMaximum(GregorianCalendar.MONTH), 1);
            } else {
                pMonth.set(GregorianCalendar.MONTH, month.get(GregorianCalendar.MONTH) - 1);
            }
            maxP = pMonth.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);

            return maxP;
        }

        @Override
        public CalendarHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_search_all_calendar_date, parent, false);
            return new CalendarHolder(v);
        }

        @Override
        public void onBindViewHolder(final CalendarHolder holder, final int position) {
            String[] separatedTime = mDayString.get(position).split("-");
            final String dayValue = separatedTime[2].replaceFirst("^0*", "");
            holder.tvScheduleDays.setText(dayValue);

            if ((Integer.parseInt(dayValue) > 1) && (position < firstDay)) {
                holder.tvScheduleDays.setTextColor(Color.GRAY);
                holder.tvScheduleDays.setClickable(false);
                holder.tvScheduleDays.setFocusable(false);
            } else if ((Integer.parseInt(dayValue) < 7) && (position > 28)) {
                holder.tvScheduleDays.setTextColor(Color.GRAY);
                holder.tvScheduleDays.setClickable(false);
                holder.tvScheduleDays.setFocusable(false);
            } else {
                holder.tvScheduleDays.setTextColor(Color.BLACK);
            }

            if (mDayString.get(position).equals(mToday)) {
                holder.tvTodayDate.setVisibility(View.VISIBLE);
            } else {
                holder.tvTodayDate.setVisibility(View.INVISIBLE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkNextDate(mDayString.get(holder.getAdapterPosition()));

                    changeDate(mDayString.get(holder.getAdapterPosition()));
                }
            });
        }

        private void changeDate(String inputDate) {
            mStartTimes.clear();
            mEndTimes.clear();

            mSearchAllDate = inputDate + " 00:00:00";
            mStartTimes = getScheduleTime(1);
            if (mBookingType == 3)
                mEndTimes = getScheduleTime(3);
            else
                mEndTimes = getScheduleTime(2);
            getTutorSchedules();
        }

        private void checkNextDate(String inputDate) {
            if (getParseDate(inputDate).getTime() > getParseDate(mToday).getTime()) {
                mTvTutorSearchAllDayMonth.setText(CommonUtil.getCustomDateResult(inputDate, "yyyy-MM-dd", "MMM dd"));
                mTvTutorSearchAllYear.setText(CommonUtil.getCustomDateResult(inputDate, "yyyy-MM-dd", "yyyy"));

                mRlSearchAllCalendarView.setVisibility(View.GONE);
                mRlBgSearchAll.setVisibility(View.GONE);
            } else {
                new AlertDialog.Builder(getActivity())
                        .setMessage(getString(R.string.tu_search_all_expired))
                        .setPositiveButton(getString(R.string.tu_search_all_expired_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create().show();
            }
        }

        private Date getParseDate(String iDate) {
            Date returnDate = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                returnDate = sdf.parse(iDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return returnDate;
        }

        @Override
        public int getItemCount() {
            return mDayString.size();
        }

        class CalendarHolder extends RecyclerView.ViewHolder {
            TextView tvScheduleDays;
            TextView tvTodayDate;

            CalendarHolder(View itemView) {
                super(itemView);
                tvScheduleDays = itemView.findViewById(R.id.tv_schedule_days);
                tvTodayDate = itemView.findViewById(R.id.tv_today_date);
            }
        }
    }

    private class TutorAllScheduleAdapter extends RecyclerView.Adapter<TutorAllScheduleAdapter.TutorAllScheduleHolder> {

        @Override
        public TutorAllScheduleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_tutor_all_schedule, parent, false);
            return new TutorAllScheduleHolder(v);
        }

        @Override
        public void onBindViewHolder(final TutorAllScheduleHolder holder, int position) {
            holder.tvSearchAllStartTime.setText(CommonUtil.getCustomDateResult(mStartTimes.get(position), "yyyy-MM-dd HH:mm:ss", "HH:mm"));
            holder.tvSearchAllEndTime.setText(CommonUtil.getCustomDateResult(mEndTimes.get(position), "yyyy-MM-dd HH:mm:ss", "HH:mm"));
            Log.i("End Time", CommonUtil.getCustomDateResult(mEndTimes.get(position), "yyyy-MM-dd HH:mm:ss", "HH:mm"));


            boolean isOver24 = CommonUtil.convertStringToDate(mStartTimes.get(position), "yyyy-MM-dd HH:mm:ss").getTime()
                    > new Date().getTime() + CalendarUtil.ONE_DAY_IN_MILLI;

            boolean isAvailable;

            /** Dashboard > Tutor **/
            if (mTutor != null) {
                holder.llSearchAllSelectedTutor.setVisibility(View.VISIBLE);
                holder.rvSearchAllAvailableTutors.setVisibility(View.GONE);

                holder.sdvSearchAllTutorPhoto.setImageURI(Uri.parse(mTutor.getAvatar()));
                holder.tvSearchAllTutorRate.setText(getString(R.string.tu_rate_text, String.valueOf(mTutor.getCreditWeight())));

                if (mBookingType == Booking.Type.LESSON) {
                    isAvailable = mTutorDbAdapter.checkTutorAvailableForLesson(
                            mTutor.getTutorId(), mStartTimes.get(position),
                            getEndTime(mStartTimes.get(position)));
                } else {
                    isAvailable = mTutorDbAdapter.checkTutorAvailableForTopic(
                            mTutor.getTutorId(), mStartTimes.get(position));
                }

                holder.sdvSearchAllTutorPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (getActivity() instanceof TutorTimeSearchAllActivity) {
                            Intent data = new Intent();
                            data.putExtra(TutorTimeSelectFragment.EXTRA_TUTOR_ID, mTutor.getTutorId());
                            data.putExtra(TutorTimeSelectFragment.EXTRA_TUTOR_TIME, mStartTimes.get
                                    (holder.getAdapterPosition()));
                            data.putExtra(TutorTimeSelectFragment.EXTRA_BOOKING_TYPE, mBookingType);
                            getActivity().setResult(Activity.RESULT_OK, data);
                            getActivity().finish();
                        }
                    }
                });

                if (isAvailable && isOver24) {
                    holder.rlSearchAllTutorImage.setVisibility(View.VISIBLE);
                    holder.tvSearchAllNotAvailable.setVisibility(View.GONE);
                } else {
                    holder.rlSearchAllTutorImage.setVisibility(View.GONE);
                    holder.tvSearchAllNotAvailable.setVisibility(View.VISIBLE);
                }

                if (isOver24) {
                    over24++;

                    if (over24 == 1) {
                        firstOver24 = position;
                        holder.mStepperImage.setBackgroundResource(R.drawable.ic_stepper_orange_grey);
                    } else {
                        if (position == 47) {
                            holder.mStepperImage.setBackgroundResource(R.drawable.ic_stepper_orange_cut);
                        } else {
                            holder.mStepperImage.setBackgroundResource(R.drawable.ic_stepper_orange);
                        }
                    }
                } else {
                    if (position == 0) {
                        holder.mStepperImage.setBackgroundResource(R.drawable.ic_stepper);
                    } else {
                        holder.mStepperImage.setBackgroundResource(R.drawable.ic_stepper_uncut);
                    }
                }
            }

            /** Dashboard > Package **/
            else {
                holder.llSearchAllSelectedTutor.setVisibility(View.GONE);

                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                holder.rvSearchAllAvailableTutors.setLayoutManager(layoutManager);

                if (isOver24) {
                    if (mBookingType == Booking.Type.LESSON)
                        mTutorList = mTutorDbAdapter.getAvailableTutorsForLesson(mStartTimes.get(position),
                                getEndTime(mStartTimes.get(position)));
                    else
                        mTutorList = mTutorDbAdapter.getAvailableTutorsForTopic(mStartTimes.get(position));
                } else {
                    mTutorList = new ArrayList<>();
                }

                if (mTutorList.size() > 0) {
                    holder.rvSearchAllAvailableTutors.setVisibility(View.VISIBLE);
                    holder.tvTutorsNotAvailable.setVisibility(View.GONE);
                    AvailableTutorAdapter availableTutorAdapter = new AvailableTutorAdapter(mStartTimes.get(position), mTutorList);
                    holder.rvSearchAllAvailableTutors.setAdapter(availableTutorAdapter);
                } else {
                    holder.rvSearchAllAvailableTutors.setVisibility(View.GONE);
                    holder.tvTutorsNotAvailable.setVisibility(View.VISIBLE);
                }

                if (isOver24) {
                    over24++;

                    if (over24 == 1) {
                        firstOver24 = position;
                        holder.mStepperImage.setBackgroundResource(R.drawable.ic_stepper_orange_grey);
                    } else {
                        if (position == 47) {
                            holder.mStepperImage.setBackgroundResource(R.drawable.ic_stepper_orange_cut);
                        } else {
                            holder.mStepperImage.setBackgroundResource(R.drawable.ic_stepper_orange);
                        }
                    }
                } else {
                    if (position == 0) {
                        holder.mStepperImage.setBackgroundResource(R.drawable.ic_stepper);
                    } else {
                        holder.mStepperImage.setBackgroundResource(R.drawable.ic_stepper_uncut);
                    }
                }

            }
        }

        @Override
        public int getItemCount() {
            return 48;
        }

        class TutorAllScheduleHolder extends RecyclerView.ViewHolder {
            TextView tvSearchAllStartTime;
            TextView tvSearchAllEndTime;
            TextView tvSearchAllNotAvailable;
            TextView tvTutorsNotAvailable;
            TextView tvSearchAllTutorRate;
            RecyclerView rvSearchAllAvailableTutors;
            SimpleDraweeView sdvSearchAllTutorPhoto;
            LinearLayout llSearchAllSelectedTutor;
            RelativeLayout rlSearchAllTutorImage;
            ImageView mStepperImage;

            TutorAllScheduleHolder(View itemView) {
                super(itemView);
                tvSearchAllStartTime = itemView.findViewById(R.id.tv_search_all_start_time);
                tvSearchAllEndTime = itemView.findViewById(R.id.tv_search_all_end_time);
                tvSearchAllNotAvailable = itemView.findViewById(R.id.tv_search_all_not_available);
                tvSearchAllTutorRate = itemView.findViewById(R.id.tv_search_all_tutor_rate);
                tvTutorsNotAvailable = itemView.findViewById(R.id.tv_tutors_not_available);
                rvSearchAllAvailableTutors = itemView.findViewById(R.id.rv_search_all_available_tutors);
                sdvSearchAllTutorPhoto = itemView.findViewById(R.id.sdv_search_all_tutor_photo);
                llSearchAllSelectedTutor = itemView.findViewById(R.id.ll_search_all_selected_tutor);
                rlSearchAllTutorImage = itemView.findViewById(R.id.rl_search_all_tutor_image);
                mStepperImage = itemView.findViewById(R.id.stepper1);
            }
        }
    }

    private class AvailableTutorAdapter extends RecyclerView.Adapter<AvailableTutorAdapter.ViewHolder> {
        private String startTime;
        private List<Tutor> availableTutors;

        AvailableTutorAdapter(String startTime, List<Tutor> availableTutors) {
            this.startTime = startTime;
            this.availableTutors = availableTutors;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_available_tutor, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Tutor tutor = availableTutors.get(position);

            if (!TextUtils.isEmpty(tutor.getAvatar())) {
                holder.sdvTutorPhoto.setImageURI(Uri.parse(tutor.getAvatar()));
            }

            holder.tvTutorTimeSelectRate.setText(getString(R.string.tu_rate_text, String.valueOf(tutor.getCreditWeight())));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent data = new Intent();
                    data.putExtra(TutorTimeSelectFragment.EXTRA_TUTOR_ID, tutor.getTutorId());
                    data.putExtra(TutorTimeSelectFragment.EXTRA_TUTOR_TIME, startTime);
                    getActivity().setResult(Activity.RESULT_OK, data);
                    getActivity().finish();
                }
            });
        }

        @Override
        public int getItemCount() {
            return availableTutors.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            SimpleDraweeView sdvTutorPhoto;
            ImageView imvSelectedMark;
            TextView tvTutorTimeSelectRate;

            public ViewHolder(View itemView) {
                super(itemView);

                sdvTutorPhoto = itemView.findViewById(R.id.sdv_tutor_photo);
                imvSelectedMark = itemView.findViewById(R.id.imv_selected_mark);
                tvTutorTimeSelectRate = itemView.findViewById(R.id.tv_tutor_time_select_rate);
            }
        }
    }
}
