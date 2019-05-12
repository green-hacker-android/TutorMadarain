package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flurry.android.FlurryAgent;
import com.mtechidea.android.cardfoldanimation.FoldableCardLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.database.TutorAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Booking;
import inc.osbay.android.tutormandarin.sdk.model.Lesson;
import inc.osbay.android.tutormandarin.sdk.model.Topic;
import inc.osbay.android.tutormandarin.sdk.model.TopicClass;
import inc.osbay.android.tutormandarin.sdk.model.TrialClass;
import inc.osbay.android.tutormandarin.sdk.model.Tutor;
import inc.osbay.android.tutormandarin.ui.activity.ClassRoomActivity;
import inc.osbay.android.tutormandarin.ui.activity.ClassroomAssistantActivity;
import inc.osbay.android.tutormandarin.ui.activity.FragmentHolderActivity;
import inc.osbay.android.tutormandarin.util.CalendarUtil;
import inc.osbay.android.tutormandarin.util.CommonUtil;

import static inc.osbay.android.tutormandarin.ui.fragment.TutorialFragment.PREF_SCHEDULE_TUTORIAL;

public class ScheduleDetailFragment extends BackHandledFragment implements View.OnClickListener {
    public static final String EXTRA_OPEN_UPCOMING = "ScheduleDetailFragment.EXTRA_OPEN_UPCOMING";
    ProgressDialog progressDialog;
    private List<Booking> mBookingList;
    private Booking mComingUpBooking;
    private ServerRequestManager mServerRequestManager;
    private LinearLayoutManager mBookingScheduleLayoutManager;
    private BookingScheduleRVAdapter mBookingScheduleRVAdapter;
    private CalendarDateAdapter mCalendarDateRVAdapter;
    private Calendar c = null;
    private Calendar selectedCalendar = null;
    private AccountAdapter mAccountDbAdapter;
    private CurriculumAdapter mCurriculumAdapter;
    private TutorAdapter mTutorAdapter;
    private List<String> mScheduledDates;
    private TextView mSelectedMonthTextView;
    private TextView mSelectedDateTextView;
    private TextView mEmptyTextView;
    private ImageView imvScheduleList;
    private ImageView imvScheduleCalendar;
    private RelativeLayout mCalendarViewRelativeLayout;
    private String mStartedClassId;
    private boolean isOpenedUpcoming;
    private Booking mSelectedBooking;
    private boolean isCalendarOpened;
    private Timer mClassroomStatusTimer;
    private Date mSelectedDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getActivity().getSharedPreferences(PREF_SCHEDULE_TUTORIAL, Context.MODE_PRIVATE);
        int scheduleTutorial = preferences.getInt(PREF_SCHEDULE_TUTORIAL, 0);

        if (scheduleTutorial == 0) {
            Intent intent = new Intent(getActivity(), FragmentHolderActivity.class);
            intent.putExtra(FragmentHolderActivity.EXTRA_DISPLAY_FRAGMENT,
                    TutorialFragment.class.getSimpleName());
            intent.putExtra(TutorialFragment.TUTORIAL_NO, 1);
            startActivity(intent);
        }

        Bundle bundle = getArguments();
        if (bundle != null) {
            isOpenedUpcoming = bundle.getBoolean(EXTRA_OPEN_UPCOMING);
        }

        mBookingList = new ArrayList<>();
        mScheduledDates = new ArrayList<>();

        mServerRequestManager = new ServerRequestManager(getActivity().getApplicationContext());

        mAccountDbAdapter = new AccountAdapter(getActivity());
        mCurriculumAdapter = new CurriculumAdapter(getActivity());
        mTutorAdapter = new TutorAdapter(getActivity());

        c = GregorianCalendar.getInstance(Locale.getDefault());
        int days = c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek();
        c.setTimeInMillis(c.getTimeInMillis() - (CalendarUtil.ONE_DAY_IN_MILLI * days));

        isCalendarOpened = true;

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.sd_schedule_loading));
        progressDialog.setCancelable(false);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_schedule_detail, container, false);

        Toolbar toolBar;
        toolBar = v.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#00AA9F"));
        setSupportActionBar(toolBar);

        RecyclerView rvDateTitle = v.findViewById(R.id.rv_date_title);

        mEmptyTextView = v.findViewById(R.id.tv_empty);
        mSelectedMonthTextView = v.findViewById(R.id.tv_selected_month);
        mSelectedDateTextView = v.findViewById(R.id.tv_selected_date);
        mCalendarViewRelativeLayout = v.findViewById(R.id.rl_calendar_view);

        imvScheduleList = v.findViewById(R.id.imv_schedule_list);
        imvScheduleList.setOnClickListener(this);

        imvScheduleCalendar = v.findViewById(R.id.imv_schedule_calendar);
        imvScheduleCalendar.setOnClickListener(this);

        GridLayoutManager dateTitleGridLayout = new GridLayoutManager(getActivity(), 7);
        rvDateTitle.setLayoutManager(dateTitleGridLayout);

        mBookingList = mAccountDbAdapter.getAllBookings();

        selectedCalendar = GregorianCalendar.getInstance();

        RecyclerView rvScheduleDetailList = v.findViewById(R.id.rv_schedule_detail_list);

        mBookingScheduleLayoutManager = new LinearLayoutManager(getActivity());
        rvScheduleDetailList.setLayoutManager(mBookingScheduleLayoutManager);

        mBookingScheduleRVAdapter = new BookingScheduleRVAdapter();
        rvScheduleDetailList.setAdapter(mBookingScheduleRVAdapter);

        mCalendarDateRVAdapter = new CalendarDateAdapter();
        rvDateTitle.setAdapter(mCalendarDateRVAdapter);

        mBookingScheduleRVAdapter.setSelectedDate(CommonUtil.convertStringToDate(
                CommonUtil.getFormattedDate(selectedCalendar.getTime(), "yyyy-MM-dd", false), "yyyy-MM-dd"));

        if (!isCalendarOpened) {
            imvScheduleList.setImageResource(R.drawable.ic_black_todo_list);
            imvScheduleCalendar.setImageResource(R.drawable.ic_grey_schedule);

            mBookingScheduleRVAdapter.setSelectedDate(null);
            mCalendarViewRelativeLayout.setVisibility(View.GONE);
        }

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        setTitle(getString(R.string.sd_schedule_title));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);

        FlurryAgent.logEvent("Schedule");
        refreshAdapter();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_schedule_help, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.opt_schedule_help:
                Intent intent = new Intent(getActivity(), FragmentHolderActivity.class);
                intent.putExtra(FragmentHolderActivity.EXTRA_DISPLAY_FRAGMENT,
                        TutorialFragment.class.getSimpleName());
                // 1. schedule
                intent.putExtra(TutorialFragment.TUTORIAL_NO, 1);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        progressDialog.show();

        mServerRequestManager = new ServerRequestManager(getActivity().getApplicationContext());
        mServerRequestManager.getScheduleForStudent(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                progressDialog.dismiss();

                if (getActivity() != null) {
                    mBookingList = mAccountDbAdapter.getAllBookings();
                    Log.e("Schedules", "booking count - " + mBookingList.size());

                    refreshAdapter();
                }
            }

            @Override
            public void onError(ServerError err) {
                progressDialog.dismiss();

                if (getActivity() != null) {
                    mEmptyTextView.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), err.getMessage(), Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mClassroomStatusTimer != null) {
            mClassroomStatusTimer.cancel();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 1) {
            getFragmentManager().beginTransaction()
                    .remove(ScheduleDetailFragment.this)
                    .commit();

            getFragmentManager().popBackStack(getFragmentManager().getBackStackEntryAt(0).getId(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            getFragmentManager().popBackStack();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        isOpenedUpcoming = false;
        switch (v.getId()) {
            case R.id.imv_schedule_list:
                if (isCalendarOpened) {
                    isCalendarOpened = false;
                    imvScheduleList.setImageResource(R.drawable.ic_black_todo_list);
                    imvScheduleCalendar.setImageResource(R.drawable.ic_grey_schedule);

                    mBookingScheduleRVAdapter.setSelectedDate(null);

                    ScaleAnimation closeAnim = new ScaleAnimation(1, 1, 1, 0, mCalendarViewRelativeLayout.getWidth() / 2, 0);
                    closeAnim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mCalendarViewRelativeLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    closeAnim.setDuration(300);
                    mCalendarViewRelativeLayout.setAnimation(closeAnim);

                    mCalendarViewRelativeLayout.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.imv_schedule_calendar:
                if (!isCalendarOpened) {
                    isCalendarOpened = true;
                    imvScheduleList.setImageResource(R.drawable.ic_grey_todo_list);
                    imvScheduleCalendar.setImageResource(R.drawable.ic_black_schedule);

                    mBookingScheduleRVAdapter.setSelectedDate(CommonUtil.convertStringToDate(
                            CommonUtil.getFormattedDate(selectedCalendar.getTime(), "yyyy-MM-dd", false), "yyyy-MM-dd"));

                    ScaleAnimation openAnim = new ScaleAnimation(1, 1, 0, 1, mCalendarViewRelativeLayout.getWidth() / 2, 0);
                    openAnim.setDuration(300);
                    mCalendarViewRelativeLayout.setAnimation(openAnim);

                    mCalendarViewRelativeLayout.setVisibility(View.VISIBLE);

                    break;
                }
        }
        refreshAdapter();
    }

    private void refreshAdapter() {

        int upcomingIndex = -1;
        mComingUpBooking = mAccountDbAdapter.getUpComingBooking(50); //TODO

        for (int i = 0; i < mBookingList.size(); i++) {
            Booking booking = mBookingList.get(i);
            String scheduleDate = booking.getBookedDate().substring(0, 10);

            if (!mScheduledDates.contains(scheduleDate)) {
                mScheduledDates.add(scheduleDate);
            }

            if (mComingUpBooking != null && booking.getBookingStatus() == Booking.Status.ACTIVE &&
                    booking.getBookedDate().equals(mComingUpBooking.getBookedDate())) {
                upcomingIndex = i;
            }
        }

        mBookingScheduleRVAdapter.setSelectedDate(mSelectedDate);

        if (mBookingScheduleRVAdapter.getSelectedScheduleCount() > 0) {
            mEmptyTextView.setVisibility(View.GONE);
        } else {
            mEmptyTextView.setVisibility(View.VISIBLE);
        }

        if (isOpenedUpcoming) {
            imvScheduleList.setImageResource(R.drawable.ic_black_todo_list);
            imvScheduleCalendar.setImageResource(R.drawable.ic_grey_schedule);
            mCalendarViewRelativeLayout.setVisibility(View.GONE);

            mBookingScheduleRVAdapter.setSelectedDate(null);
            mBookingScheduleRVAdapter.setExtendedItemPosition(upcomingIndex);

            isCalendarOpened = false;
        }

        mBookingScheduleRVAdapter.setSelectedDate(mSelectedDate);
        mCalendarDateRVAdapter.notifyDataSetChanged();
        mBookingScheduleRVAdapter.notifyDataSetChanged();

        mBookingScheduleLayoutManager.scrollToPositionWithOffset(upcomingIndex, 10);
    }

    private class CalendarDateAdapter extends RecyclerView.Adapter<CalendarDateAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_calendar_date, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Calendar currentCalender = GregorianCalendar.getInstance(Locale.getDefault());
            long realTime = c.getTimeInMillis() + (position * CalendarUtil.ONE_DAY_IN_MILLI);
            currentCalender.setTimeInMillis(realTime);

            final String calendarStr = CommonUtil.getFormattedDate(currentCalender.getTime(), "yyyy-MM-dd", false);

            if (selectedCalendar.get(Calendar.DATE) == currentCalender.get(Calendar.DATE) && selectedCalendar.get(Calendar.MONTH) == currentCalender.get(Calendar.MONTH)) {
                holder.vwScheduleDays.setVisibility(View.VISIBLE);
                holder.vwSelectedScheduleDays.setVisibility(View.INVISIBLE);
            } else {
                holder.vwScheduleDays.setVisibility(View.INVISIBLE);
                if (mScheduledDates.contains(calendarStr)) {
                    holder.vwSelectedScheduleDays.setVisibility(View.VISIBLE);
                } else {
                    holder.vwSelectedScheduleDays.setVisibility(View.INVISIBLE);
                }
            }

            holder.tvScheduleDays.setText(String.valueOf(currentCalender.get(Calendar.DATE)));

            Calendar today = GregorianCalendar.getInstance();
            if (currentCalender.get(Calendar.MONTH) == today.get(Calendar.MONTH) && currentCalender.get(Calendar.DATE) == today.get(Calendar.DATE)) {
                holder.tvScheduleDays.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_color));
            } else if (currentCalender.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
                holder.tvScheduleDays.setTextColor(Color.BLACK);
            } else {
                holder.tvScheduleDays.setTextColor(Color.LTGRAY);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedCalendar = currentCalender;

                    Date selectedDate = CommonUtil.convertStringToDate(CommonUtil.getFormattedDate(selectedCalendar.getTime(), "yyyy-MM-dd", false), "yyyy-MM-dd");
                    mBookingScheduleRVAdapter.setSelectedDate(selectedDate);
                    mBookingScheduleRVAdapter.notifyDataSetChanged();

                    if (mScheduledDates.contains(calendarStr)) {
                        mEmptyTextView.setVisibility(View.GONE);
                    } else {
                        mEmptyTextView.setVisibility(View.VISIBLE);
                    }

                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return 42;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvScheduleDays;
            View vwScheduleDays;
            View vwSelectedScheduleDays;

            public ViewHolder(View itemView) {
                super(itemView);
                tvScheduleDays = itemView.findViewById(R.id.tv_schedule_days);
                vwScheduleDays = itemView.findViewById(R.id.vw_schedule_days);
                vwSelectedScheduleDays = itemView.findViewById(R.id.vw_selected_schedule_days);
            }
        }
    }

    private class BookingScheduleRVAdapter extends RecyclerView.Adapter<BookingScheduleRVAdapter.ViewHolder> {

        private static final int ITEM_CLOSED = 1;
        private static final int ITEM_OPENED = 2;

        private FoldableCardLayout lastSelectedItem;

        private List<Booking> mSelectedBookingList = new ArrayList<>();

        private int extendedItemPosition;

        public BookingScheduleRVAdapter() {
            setSelectedDate(null);
        }

        public void setSelectedDate(Date selectedDate) {
            mSelectedDate = selectedDate;
            if (selectedDate != null) {
                mSelectedBookingList = new ArrayList<>();
                for (int i = 0; i < mBookingList.size(); i++) {
                    if (selectedDate.equals(CommonUtil.convertStringToDate(mBookingList.get(i).getBookedDate(), "yyyy-MM-dd"))) {
                        mSelectedBookingList.add(mBookingList.get(i));
                    }
                }

                mSelectedMonthTextView.setText(CommonUtil.getFormattedDate(selectedDate, "MMMM yyyy", false));

                if (selectedCalendar.get(Calendar.DAY_OF_YEAR) == GregorianCalendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
                    mSelectedDateTextView.setText(getString(R.string.sd_today));
                } else {
                    mSelectedDateTextView.setText(CommonUtil.getFormattedDate(selectedDate, "dd", false));
                }
            } else {
                mSelectedBookingList = mBookingList;
            }
            extendedItemPosition = mSelectedBookingList.size();
        }

        public void setExtendedItemPosition(int extendedItemPosition) {
            this.extendedItemPosition = extendedItemPosition;
        }

        public int getSelectedScheduleCount() {
            return mSelectedBookingList.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_booking_schedule,
                    parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final Booking booking = mSelectedBookingList.get(position);
            final Date bookedDate = CommonUtil.convertStringToDate(booking.getBookedDate(),
                    "yyyy-MM-dd HH:mm:ss");


            Tutor tutor = mTutorAdapter.getTutorById(booking.getTutorId());

            TopicClass topicClass = null;
            Lesson lesson = null;
            TrialClass trialClass = null;
            if (booking.getBookingType() == Booking.Type.TRIAL) {
                trialClass = mCurriculumAdapter.getTrialClassById(booking.getLessonId());
            } else if (booking.getBookingType() == Booking.Type.TOPIC) {
                topicClass = mCurriculumAdapter.getTopicClassById(booking.getLessonId());
            } else {
                lesson = mCurriculumAdapter.getLessonById(booking.getLessonId());
            }

            if (topicClass == null && booking.getBookingType() == Booking.Type.TOPIC) {
                mServerRequestManager.getCurriculumClassById(booking.getLessonId(),
                        new ServerRequestManager.OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                progressDialog.dismiss();
                                notifyDataSetChanged();
                            }

                            @Override
                            public void onError(ServerError err) {
                            }
                        });

                if (!progressDialog.isShowing())
                    progressDialog.show();

                return;
            } else if (trialClass == null && booking.getBookingType() == Booking.Type.TRIAL) {
                mServerRequestManager.getTrialClassById(booking.getLessonId(),
                        new ServerRequestManager.OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                progressDialog.dismiss();
                                notifyDataSetChanged();
                            }

                            @Override
                            public void onError(ServerError err) {
                            }
                        });

                if (!progressDialog.isShowing())
                    progressDialog.show();
                return;
            } else if (lesson == null && (booking.getBookingType() == Booking.Type.LESSON ||
                    booking.getBookingType() == Booking.Type.PACKAGE)) {
                mServerRequestManager.getCurriculumLessonById(booking.getLessonId(), new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        progressDialog.dismiss();
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onError(ServerError err) {
                    }
                });

                if (!progressDialog.isShowing())
                    progressDialog.show();
                return;
            }

            if (tutor == null) {
                mServerRequestManager.getTutorInfo(booking.getTutorId(),
                        new ServerRequestManager.OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                progressDialog.dismiss();
                                notifyDataSetChanged();
                            }

                            @Override
                            public void onError(ServerError err) {
                            }
                        });

                if (!progressDialog.isShowing())
                    progressDialog.show();
                return;
            }

            if (booking.getBookingType() == Booking.Type.PACKAGE) {
                holder.tvBookingType.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.yellow));
                holder.vColorBar.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.yellow));
            } else {
                holder.tvBookingType.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.primary_color));
                holder.vColorBar.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.primary_color));
            }

            if (!TextUtils.isEmpty(tutor.getAvatar())) {
                holder.sdvTutorPhoto.setImageURI(Uri.parse(tutor.getAvatar()));
                holder.sdvTutorPhoto1.setImageURI(Uri.parse(tutor.getAvatar()));
            }

            if (lesson != null) {
                if (!TextUtils.isEmpty(lesson.getCoverPhoto())) {
                    holder.sdvLessonPhoto.setImageURI(Uri.parse(lesson.getCoverPhoto()));
                    holder.sdvLessonPhoto1.setImageURI(Uri.parse(lesson.getCoverPhoto()));
                }
            }

            if (topicClass != null) {
                if (!TextUtils.isEmpty(topicClass.getCoverPhoto())) {
                    holder.sdvLessonPhoto.setImageURI(Uri.parse(topicClass.getCoverPhoto()));
                    holder.sdvLessonPhoto1.setImageURI(Uri.parse(topicClass.getCoverPhoto()));
                }
            }

            if (trialClass != null) {
                if (!TextUtils.isEmpty(trialClass.getImageUrl())) {
                    holder.sdvLessonPhoto.setImageURI(Uri.parse(trialClass.getImageUrl()));
                    holder.sdvLessonPhoto1.setImageURI(Uri.parse(trialClass.getImageUrl()));
                }
            }

            holder.tvStartTime.setText(CommonUtil.getFormattedDate(bookedDate, "HH:mm", false));
            holder.tvStartTime1.setText(CommonUtil.getFormattedDate(bookedDate, "HH:mm", false));

            if (bookedDate != null) {
                if (booking.getBookingType() == Booking.Type.TOPIC) {
                    holder.tvEndTime.setText(CommonUtil.getFormattedDate(new Date(bookedDate.getTime()
                            + 1500000), "HH:mm", false));
                    holder.tvEndTime1.setText(CommonUtil.getFormattedDate(new Date(bookedDate.getTime()
                            + 1500000), "HH:mm", false));
                } else {
                    holder.tvEndTime.setText(CommonUtil.getFormattedDate(new Date(bookedDate.getTime()
                            + 3000000), "HH:mm", false));
                    holder.tvEndTime1.setText(CommonUtil.getFormattedDate(new Date(bookedDate.getTime()
                            + 3000000), "HH:mm", false));
                }
            }

            holder.fcItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isOpenedUpcoming = false;
                    if (extendedItemPosition == holder.getAdapterPosition()) {
                        extendedItemPosition = mSelectedBookingList.size();

                        if (isCalendarOpened) {
                            mCalendarViewRelativeLayout.setVisibility(View.VISIBLE);
                            holder.fcItem.fold(true);
                        } else {
                            holder.fcItem.fold(false);
                        }
                    } else {
                        Map<String, String> params = new HashMap<>();
                        if (isCalendarOpened) {
                            params.put("Screen", "Calendar View");
                        } else {
                            params.put("Screen", "List View");
                        }
                        FlurryAgent.logEvent("Open schedule ticket", params);

                        mCalendarViewRelativeLayout.setVisibility(View.GONE);
                        extendedItemPosition = holder.getAdapterPosition();

                        if (lastSelectedItem != null) {
                            lastSelectedItem.fold(true);
                        }

                        mBookingScheduleLayoutManager.scrollToPositionWithOffset(
                                holder.getAdapterPosition(), 10);

                        holder.fcItem.unfold(false);

                        lastSelectedItem = holder.fcItem;

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mBookingScheduleLayoutManager.scrollToPositionWithOffset(
                                                    holder.getAdapterPosition(), 10);

                                        }
                                    });
                                }
                            }
                        }, 1000);
                    }
                }
            });

            holder.tvBookDate.setText(CommonUtil.getFormattedDate(
                    bookedDate, "MMMM d", true));

            if (booking.getBookingStatus() == Booking.Status.ACTIVE) {
                holder.imvFinished.setVisibility(View.GONE);
                if (mComingUpBooking != null && booking.getBookedDate().equals(mComingUpBooking.getBookedDate())) {
                    holder.tvStatus.setVisibility(View.VISIBLE);
                    holder.tvStatus.setText(getString(R.string.sd_coming_up));
                } else {
                    holder.tvStatus.setVisibility(View.GONE);
                }
            } else {
                holder.tvStatus.setVisibility(View.VISIBLE);
                if (booking.getBookingStatus() == Booking.Status.FINISH) { // Finished
                    holder.imvFinished.setVisibility(View.VISIBLE);
                    holder.tvStatus.setText(getString(R.string.sd_finished));
                } else if (booking.getBookingStatus() == Booking.Status.CANCEL) {
                    holder.imvFinished.setVisibility(View.GONE);
                    holder.tvStatus.setText(getString(R.string.sd_canceled));
                } else if (booking.getBookingStatus() == Booking.Status.MISS) {
                    holder.imvFinished.setVisibility(View.GONE);
                    holder.tvStatus.setText(getString(R.string.sd_missed));
                }
            }

            holder.tvBookDate.setText(CommonUtil.getCustomFormattedDate(
                    bookedDate, "MMMM d", false));
            holder.tvBookDate1.setText(CommonUtil.getCustomFormattedDate(
                    bookedDate, "MMMM d", false));

            //Credit Booking
            if (booking.getBookingType() == Booking.Type.PACKAGE) {
                holder.tvBookingType.setText(getString(R.string.sd_package_booking));
            } else {
                holder.tvBookingType.setText(getString(R.string.sd_credit_booking));
            }

            holder.tvTutorName.setText(tutor.getName());
            holder.tvTutorRate.setText(String.valueOf(tutor.getRate()));


            if (booking.getBookingType() == Booking.Type.TRIAL && trialClass != null) {
                holder.tvLessonName.setText(getString(R.string.sd_lesson_name));
                holder.tvLessonTitle.setText(trialClass.getTitle());
                holder.tvLessonLevel.setText(getString(R.string.sd_lesson_level));
            } else if (booking.getBookingType() == Booking.Type.TOPIC && topicClass != null) {
                Topic topic = mCurriculumAdapter.getTopicById(topicClass.getTopicId());

                if (topic != null) {
                    holder.tvLessonName.setText(topic.getTitle()); //TODO
                } else {
                    holder.tvLessonName.setText(getString(R.string.sd_topic_name));
                }

                holder.tvLessonTitle.setText(topicClass.getTitle());
                holder.tvLessonLevel.setText(topicClass.getLevel());
            } else if (lesson != null) {
                if (lesson.isSupplement()) {
                    holder.tvLessonName.setText(String.format(Locale.getDefault(), getString(R.string.sd_supplement_no) + "%d",
                            lesson.getLessonNumber()));
                } else {
                    holder.tvLessonName.setText(String.format(Locale.getDefault(), getString(R.string.sd_lesson_no) + "%d",
                            lesson.getLessonNumber()));
                }
                holder.tvLessonTitle.setText(lesson.getTitle());
                holder.tvLessonLevel.setText(lesson.getLevel());
            }

            if (booking.getBookingType() == Booking.Type.PACKAGE) {
                holder.btnStartClass.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.btn_bg_rounded_orange));
            } else {
                holder.btnStartClass.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.btn_bg_rounded_green));
            }
            holder.btnStartClass.setPadding(50, 0, 50, 0);

            if (mComingUpBooking != null && booking.getBookedDate().equals(mComingUpBooking.getBookedDate()) &&
                    bookedDate.getTime() < new Date().getTime() + 1000 * 60 * 30) {
                if (booking.getClassRoomId().equals(mStartedClassId)) {
                    holder.btnStartClass.setEnabled(true);
                } else {
                    holder.btnStartClass.setEnabled(true);
                    if (mClassroomStatusTimer != null) {
                        mClassroomStatusTimer.cancel();
                        mClassroomStatusTimer = null;
                    }

                    mClassroomStatusTimer = new Timer();
                    mClassroomStatusTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mServerRequestManager.checkClassRoomReady(booking.getClassRoomId(),
                                    new ServerRequestManager.OnRequestFinishedListener() {
                                        @Override
                                        public void onSuccess(Object result) {

                                            if (getActivity() != null) {
                                                mStartedClassId = booking.getClassRoomId();
                                                holder.btnStartClass.setEnabled(true);

                                                if (mClassroomStatusTimer != null) {
                                                    mClassroomStatusTimer.cancel();
                                                    mClassroomStatusTimer = null;
                                                }
                                            }
                                        }

                                        @Override
                                        public void onError(ServerError err) {
                                            // ignore
                                            mStartedClassId = null;
                                        }
                                    });
                        }
                    }, 0, 5000);
                }
            } else {
                holder.btnStartClass.setEnabled(true);
            }

            holder.btnStartClass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectedBooking = booking;

                    System.gc();

                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.sd_network_notice))
                            .setMessage(getString(R.string.sd_network_msg))
                            .setPositiveButton(getString(R.string.sd_network_continue), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(getActivity(), ClassRoomActivity.class);
                                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    //intent.putExtra("fragment", ClassRoomActivity.class.getSimpleName());
                                    intent.putExtra(ClassRoomActivity.EXTRA_BOOKING, mSelectedBooking);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton(getString(R.string.sd_network_cancel), null)
                            .create()
                            .show();
                }
            });

            holder.tvChangeBooking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (bookedDate.getTime() < new Date().getTime() ||
                            booking.getBookingStatus() != Booking.Status.ACTIVE) {
                        return;
                    }

                    //Account account = mAccountDbAdapter.getAccountById(booking.getStudentId());
                    /*if (account != null && account.getStatus() != Account.Status.ACTIVE) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.sd_unavailable_title))
                                .setMessage(getString(R.string.sd_unavailable_msg))
                                .setPositiveButton(getString(R.string.sd_unavailable_ok), null)
                                .create()
                                .show();
                        return;
                    }*/

                    AlertDialog alert = new AlertDialog.Builder(getActivity())
                            .setNegativeButton(getString(R.string.sd_change_booking_cancel), null)
                            .setPositiveButton(getString(R.string.sd_change_booking_sure), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (getActivity() != null) {
                                        FragmentManager frgMgr = getFragmentManager();
                                        Fragment changeFragment = new ChangeBookingFragment();
                                        Bundle bundle = new Bundle();
                                        bundle.putString(ChangeBookingFragment.EXTRA_BOOKING_ID, booking.getBookingId());
                                        changeFragment.setArguments(bundle);

                                        Fragment frg = frgMgr.findFragmentById(R.id.container);

                                        if (frg == null) {
                                            frgMgr.beginTransaction()
                                                    .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                                            R.animator.fragment_out_new, R.animator.fragment_out_old)
                                                    .addToBackStack(null)
                                                    .add(R.id.container, changeFragment).commit();
                                        } else {
                                            frgMgr.beginTransaction()
                                                    .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                                            R.animator.fragment_out_new, R.animator.fragment_out_old)
                                                    .addToBackStack(null)
                                                    .replace(R.id.container, changeFragment).commit();
                                        }
                                    }
                                }
                            })
                            .create();

                    if (bookedDate.getTime() < new Date().getTime() + CalendarUtil.ONE_DAY_IN_MILLI) {
                        alert.setTitle(getString(R.string.sd_class_change_cancel_title));
                        alert.setMessage(getString(R.string.sd_class_change_cancel_msg));
                    } else {
                        alert.setTitle(getString(R.string.sd_change_time_cancel_title));
                        alert.setMessage(getString(R.string.sd_change_time_cancel_msg));
                    }

                    alert.show();
                }
            });

            if (holder.getItemViewType() == ITEM_OPENED) {
                lastSelectedItem = holder.fcItem;
                Log.e("Open Upcoming - ", "opened - " + position);

                holder.fcItem.unfold(true);
            } else {
                holder.fcItem.fold(true);
            }

        }

        @Override
        public int getItemCount() {
            return mSelectedBookingList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (mComingUpBooking != null && mSelectedBookingList.get(position).getBookedDate()
                    .equals(mComingUpBooking.getBookedDate()) && isOpenedUpcoming &&
                    mSelectedBookingList.get(position).getBookingStatus() == Booking.Status.ACTIVE) {
                return ITEM_OPENED;
            }
            return ITEM_CLOSED;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            // BOTH
            private FoldableCardLayout fcItem;

            private SimpleDraweeView sdvTutorPhoto;
            private SimpleDraweeView sdvLessonPhoto;
            private TextView tvStartTime;
            private TextView tvEndTime;
            private TextView tvBookDate;

            private SimpleDraweeView sdvTutorPhoto1;
            private SimpleDraweeView sdvLessonPhoto1;
            private TextView tvStartTime1;
            private TextView tvEndTime1;
            private TextView tvBookDate1;

            // ITEM CLOSED
            private ImageView imvFinished;
            private TextView tvStatus;
            private View vColorBar;

            // ITEM OPENED
            private TextView tvBookingType;
            private TextView tvTutorName;
            private TextView tvTutorRate;
            private TextView tvLessonName;
            private TextView tvLessonTitle;
            private TextView tvLessonLevel;
            private Button btnStartClass;
            private TextView tvChangeBooking;

            public ViewHolder(View itemView) {
                super(itemView);

                fcItem = itemView.findViewById(R.id.folding_cell);

                sdvTutorPhoto = itemView.findViewById(R.id.sdv_tutor_photo);
                sdvLessonPhoto = itemView.findViewById(R.id.sdv_lesson_photo);
                tvBookDate = itemView.findViewById(R.id.tv_book_date);
                tvStartTime = itemView.findViewById(R.id.tv_start_time);
                tvEndTime = itemView.findViewById(R.id.tv_end_time);

                sdvTutorPhoto1 = itemView.findViewById(R.id.sdv_tutor_photo1);
                sdvLessonPhoto1 = itemView.findViewById(R.id.sdv_lesson_photo1);
                tvBookDate1 = itemView.findViewById(R.id.tv_book_date1);
                tvStartTime1 = itemView.findViewById(R.id.tv_start_time1);
                tvEndTime1 = itemView.findViewById(R.id.tv_end_time1);

                imvFinished = itemView.findViewById(R.id.imv_finished);
                tvStatus = itemView.findViewById(R.id.tv_booking_status);
                vColorBar = itemView.findViewById(R.id.v_color_bar);

                tvBookingType = itemView.findViewById(R.id.tv_booking_type);
                tvTutorName = itemView.findViewById(R.id.tv_tutor_name);
                tvTutorRate = itemView.findViewById(R.id.tv_tutor_rate);
                tvLessonName = itemView.findViewById(R.id.tv_lesson_name);
                tvLessonTitle = itemView.findViewById(R.id.tv_lesson_title);
                tvLessonLevel = itemView.findViewById(R.id.tv_lesson_level);
                btnStartClass = itemView.findViewById(R.id.btn_start_class);
                tvChangeBooking = itemView.findViewById(R.id.tv_change_booking);
                tvChangeBooking.setText(Html.fromHtml(getString(R.string.sd_change_booking_time_msg)));
            }
        }
    }

}
