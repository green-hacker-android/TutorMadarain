package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.ui.activity.MainActivity;
import inc.osbay.android.tutormandarin.ui.activity.TrialSubmitActivity;
import inc.osbay.android.tutormandarin.util.CalendarUtil;
import inc.osbay.android.tutormandarin.util.CommonUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrialScheduleFragment extends BackHandledFragment implements View.OnClickListener {

    private TrialSubmitActivity parentActivity;
    private Calendar selectedDate = null;
    private Calendar firstDate = null;
    private WeekDayRVAdapter weekDayRVAdapter;
    private TimeSlotRVAdapter timeSlotRVAdapter;
    private SharedPreferences mPreferences;

    private String[] mHours = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12",
            "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"};
    private String[] mMinutes = {"00", "30"};

    private RelativeLayout rlTrialInfo;

    public TrialScheduleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FlurryAgent.logEvent("Trial booking or skip trial");
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_trial_schedule, container, false);

        Toolbar toolbar;
        toolbar = rootView.findViewById(R.id.tool_bar);
        toolbar.setBackgroundColor(Color.parseColor("#E45F56"));
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
//            actionBar.setHomeAsUpIndicator(R.drawable.ic_cross_white);
        }

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Montserrat-Bold.ttf");
        TextView tvTitle = toolbar.findViewById(R.id.tv_toolbar_title);
        tvTitle.setTypeface(typeface);
        tvTitle.setText(getString(R.string.ts_title));
        tvTitle.setCompoundDrawables(null, null, null, null);
        tvTitle.setOnClickListener(null);

        TextView tvTrialSubmitTitle = rootView.findViewById(R.id.tv_trial_submit_title);
        TextView tvTrialSubmitText = rootView.findViewById(R.id.tv_trial_submit_text);
        TextView tvApplyTrialNow = rootView.findViewById(R.id.tv_apply_trial_now);
        TextView tvSkip = rootView.findViewById(R.id.tv_skip);
        tvApplyTrialNow.setOnClickListener(this);
        tvSkip.setOnClickListener(this);

        tvTrialSubmitTitle.setTypeface(typeface);
        tvTrialSubmitText.setTypeface(typeface);
        tvApplyTrialNow.setTypeface(typeface);
        tvSkip.setTypeface(typeface);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvTrialSubmitTitle.setText(Html.fromHtml(getString(R.string.su_trial_submit_title), Html.FROM_HTML_MODE_LEGACY));
            tvTrialSubmitText.setText(Html.fromHtml(getString(R.string.su_trial_submit_text), Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvTrialSubmitTitle.setText(Html.fromHtml(getString(R.string.su_trial_submit_title)));
            tvTrialSubmitText.setText(Html.fromHtml(getString(R.string.su_trial_submit_text)));
        }

        parentActivity = (TrialSubmitActivity) getActivity();
        firstDate = GregorianCalendar.getInstance(Locale.getDefault());
        firstDate.setTimeInMillis(CalendarUtil.getFirstDateOfWeek(1).getTime());
        selectedDate = firstDate;

        rlTrialInfo = rootView.findViewById(R.id.rl_trial_info);

        rootView.findViewById(R.id.tv_skip_trial).setOnClickListener(this);
        rootView.findViewById(R.id.tv_submit_trial).setOnClickListener(this);
        rootView.findViewById(R.id.imv_trial_info).setOnClickListener(this);
        rootView.findViewById(R.id.imv_close_trial_info).setOnClickListener(this);

        // Week days adapter
        RecyclerView rvWeek = rootView.findViewById(R.id.rv_weekdays);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 7);
        rvWeek.setLayoutManager(layoutManager);

        weekDayRVAdapter = new WeekDayRVAdapter();
        rvWeek.setAdapter(weekDayRVAdapter);

        // Start times adapter
        RecyclerView rvStartTime = rootView.findViewById(R.id.rv_start_time);

        GridLayoutManager layoutManager1 = new GridLayoutManager(getActivity(), 4);
        rvStartTime.setLayoutManager(layoutManager1);

        timeSlotRVAdapter = new TimeSlotRVAdapter();
        rvStartTime.setAdapter(timeSlotRVAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // TODO redisplay selected time zone and start time block.
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imv_trial_info:
                rlTrialInfo.setOnClickListener(null);
                rlTrialInfo.setVisibility(View.VISIBLE);
                break;
            case R.id.imv_close_trial_info:
                /*FlurryAgent.logEvent("Submit Trial Schedule");
                onBackPressed();*/
                getActivity().finish();
                FlurryAgent.logEvent("Skipping trial and entering dashboard");
                Intent intent1 = new Intent(getContext(), MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("class_type", "trial");
                intent1.putExtras(bundle);
                startActivity(intent1);
                break;
            case R.id.tv_skip_trial:
                onBackPressed();
                break;
            case R.id.tv_apply_trial_now:
                FragmentManager fm = getFragmentManager();
                Fragment frg = fm.findFragmentById(R.id.fl_trial_submit);
                Fragment applyTrialFragment = new ApplyTrialFragment();
                if (frg == null) {
                    fm.beginTransaction()
                            .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                    R.animator.fragment_out_new, R.animator.fragment_out_old)
                            .addToBackStack(null)
                            .add(R.id.fl_trial_submit, applyTrialFragment).commit();
                } else {
                    fm.beginTransaction()
                            .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                    R.animator.fragment_out_new, R.animator.fragment_out_old)
                            .addToBackStack(null)
                            .replace(R.id.fl_trial_submit, applyTrialFragment).commit();
                }
                break;
            case R.id.tv_skip:
                getActivity().finish();
                FlurryAgent.logEvent("Skipping trial and entering dashboard");
                Intent intent2 = new Intent(getContext(), MainActivity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putString("class_type", "trial");
                intent2.putExtras(bundle1);
                startActivity(intent2);
                break;
            /*case R.id.tv_submit_trial:
                FlurryAgent.logEvent("Submit Trial Schedule");
                if (TextUtils.isEmpty(parentActivity.startTime)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.ts_notice))
                            .setMessage(getString(R.string.ts_select_time_msg))
                            .setPositiveButton(getString(R.string.ts_select_time_ok), null)
                            .create()
                            .show();
                    return;
                }

                getFragmentManager().beginTransaction()
                        .replace(R.id.fl_trial_submit, new EditNumberFragment())
                        .addToBackStack(null)
                        .commit();
                break;*/
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        FlurryAgent.logEvent("TrailSchedule Back");
        getActivity().finish();
//        Intent mainIntent = new Intent(getActivity(), MainActivity.class);
//        mainIntent.setAction("refresh_fragment");
//        startActivity(mainIntent);
//        getActivity().finish();
        /*String token = mPreferences.getString("access_token", null);
        String accountId = mPreferences.getString("account_id", null);

        if (!TextUtils.isEmpty(accountId) && !TextUtils.isEmpty(token)) {
            AccountAdapter accountAdapter = new AccountAdapter(getActivity());
            Account account = accountAdapter.getAccountById(accountId);
            if (account.getStatus() == Account.Status.REQUEST && TextUtils.isEmpty(account.getPhoneNumber())) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.fl_trial_submit, new EditNumberFragment())
                        .addToBackStack(null)
                        .commit();
            } else {
                getActivity().finish();
            }
        } else {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fl_trial_submit, new EditNumberFragment())
                    .addToBackStack(null)
                    .commit();
        }*/
        return false;
    }

    private class WeekDayRVAdapter extends RecyclerView.Adapter<WeekDayRVAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_week_date, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            final Calendar date = GregorianCalendar.getInstance(Locale.getDefault());
            date.setTimeInMillis(firstDate.getTimeInMillis()
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
//                    mSelectedMonthTextView.setText(CommonUtil.getFormattedDate(selectedDate.getTime(), "MMMM yyyy", false));
//                    mSelectedDateTextView.setText(CommonUtil.getFormattedDate(selectedDate.getTime(), "MMMM d", true));
//                    notifyDataSetChanged();
//
                    weekDayRVAdapter.notifyDataSetChanged();
                    timeSlotRVAdapter.notifyDataSetChanged();
//                    refreshTutorStatus();
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

    private class TimeSlotRVAdapter extends RecyclerView.Adapter<TimeSlotRVAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_time_slot, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String fromHour;
            String toHour;

            String fromMin;
            String toMin;

            int hourIndex;

            hourIndex = position / 8 + (position % 4) * 6;

            fromHour = mHours[hourIndex];

            if (position / 4 % 2 == 0) {
                fromMin = mMinutes[0];
                toMin = mMinutes[1];

                toHour = mHours[hourIndex];
            } else {
                fromMin = mMinutes[1];
                toMin = mMinutes[0];

                toHour = mHours[(hourIndex + 1) % 24];
            }

            holder.tvStartTime.setText(String.format("%s:%s-%s:%s", fromHour, fromMin, toHour, toMin));

            // Coloring
            final String startTimeFull = CommonUtil.getFormattedDate(selectedDate.getTime(), "yyyy-MM-dd ", false) + fromHour + ":" + fromMin + ":00";
            boolean isOver24 = CommonUtil.convertStringToDate(startTimeFull, "yyyy-MM-dd HH:mm:ss").getTime()
                    > new Date().getTime() + CalendarUtil.ONE_DAY_IN_MILLI;
            if (isOver24) {
                if (startTimeFull.equals(parentActivity.startTime)) {
                    holder.itemView.setBackgroundColor(Color.parseColor("#e4645c"));
                } else {
                    holder.itemView.setBackgroundResource(R.drawable.time_slot_border);
                }
                holder.tvStartTime.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_color_black));

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        parentActivity.startTime = startTimeFull;
                        notifyDataSetChanged();
                    }
                });
            } else {
                holder.tvStartTime.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_color_gray));
                holder.itemView.setOnClickListener(null);
            }
        }

        @Override
        public int getItemCount() {
            return 48;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView tvStartTime;

            public ViewHolder(View itemView) {
                super(itemView);

                tvStartTime = itemView.findViewById(R.id.tv_start_time);
            }
        }
    }
}
