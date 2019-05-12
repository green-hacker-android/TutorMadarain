package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.facebook.drawee.view.SimpleDraweeView;
import com.flurry.android.FlurryAgent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.TutorAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Booking;
import inc.osbay.android.tutormandarin.sdk.model.StudentPackage;
import inc.osbay.android.tutormandarin.sdk.model.Tutor;
import inc.osbay.android.tutormandarin.sdk.util.LGCUtil;
import inc.osbay.android.tutormandarin.ui.activity.AutoRepeatActivity;
import inc.osbay.android.tutormandarin.ui.activity.FragmentHolderActivity;
import inc.osbay.android.tutormandarin.ui.activity.TutorTimeSelectActivity;
import inc.osbay.android.tutormandarin.util.CalendarUtil;
import inc.osbay.android.tutormandarin.util.CommonUtil;

import static inc.osbay.android.tutormandarin.ui.fragment.TutorialFragment.PREF_BOOKING_PACKAGE_TUTORIAL;

public class BookingPackageFragment extends BackHandledFragment implements View.OnClickListener {
    public static final String EXTRA_PACKAGE = "BookingPackageFragment.EXTRA_PACKAGE";
    public static final String EXTRA_AUTO_REPEAT_SCHEDULES = "BookingPackageFragment.EXTRA_AUTO_REPEAT_SCHEDULES";

    TutorAdapter mTutorAdapter;
    int maxCount = 4;
    private ServerRequestManager mRequestManager;
    private int mSelectedWeek;
    private StudentPackage mStudentPackage;

    private TextView mAutoRepeatStatusTextView;
    private ImageView mAutoRepeatStatusImageView;

    private RecyclerView mWeek1ScheduleRecyclerView;
    private RecyclerView mWeek2ScheduleRecyclerView;
    private RecyclerView mWeek3ScheduleRecyclerView;
    private RecyclerView mWeek4ScheduleRecyclerView;

    private TextView mWeek1CountTextView;
    private TextView mWeek2CountTextView;
    private TextView mWeek3CountTextView;
    private TextView mWeek4CountTextView;

    private LinearLayout mWeek1ContentLinearLayout;
    private LinearLayout mWeek2ContentLinearLayout;
    private LinearLayout mWeek3ContentLinearLayout;
    private LinearLayout mWeek4ContentLinearLayout;

    private List<Map<String, String>> mWeek1Schedules;
    private List<Map<String, String>> mWeek2Schedules;
    private List<Map<String, String>> mWeek3Schedules;
    private List<Map<String, String>> mWeek4Schedules;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mStudentPackage = (StudentPackage) bundle.getSerializable(EXTRA_PACKAGE);
        }

        SharedPreferences preferences = getActivity().getSharedPreferences(PREF_BOOKING_PACKAGE_TUTORIAL, Context.MODE_PRIVATE);
        int packageBookingTutorial = preferences.getInt(PREF_BOOKING_PACKAGE_TUTORIAL, 0);

        if (packageBookingTutorial == 0) {
            Intent intent = new Intent(getActivity(), FragmentHolderActivity.class);
            intent.putExtra(FragmentHolderActivity.EXTRA_DISPLAY_FRAGMENT,
                    TutorialFragment.class.getSimpleName());
            intent.putExtra(TutorialFragment.TUTORIAL_NO, 4);
            startActivity(intent);
        }

        mSelectedWeek = 1; // Default expand week1 schedules

        mWeek1Schedules = new ArrayList<>();
        mWeek2Schedules = new ArrayList<>();
        mWeek3Schedules = new ArrayList<>();
        mWeek4Schedules = new ArrayList<>();

        mRequestManager = new ServerRequestManager(getActivity().getApplicationContext());
        mTutorAdapter = new TutorAdapter(getActivity());

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.bo_course_loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        mRequestManager.getRemainLessonCount(mStudentPackage.getPackageId(),
                new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        progressDialog.dismiss();

                        if (getActivity() != null) {
                            maxCount = Integer.parseInt(String.valueOf(result));

                            if (maxCount > 0) {
                                mWeek1Schedules.add(new HashMap<String, String>());

                                if (maxCount > 3) {
                                    mWeek2Schedules.add(new HashMap<String, String>());
                                    mWeek3Schedules.add(new HashMap<String, String>());
                                    mWeek4Schedules.add(new HashMap<String, String>());

                                    if (maxCount > 7) {
                                        mWeek1Schedules.add(new HashMap<String, String>());
                                        mWeek2Schedules.add(new HashMap<String, String>());
                                        mWeek3Schedules.add(new HashMap<String, String>());
                                        mWeek4Schedules.add(new HashMap<String, String>());
                                    }
                                }
                            }

                            refreshUI();
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        progressDialog.dismiss();

                        if (getActivity() != null) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getString(R.string.bo_course_error))
                                    .setMessage(err.getMessage())
                                    .setPositiveButton(getString(R.string.bo_course_ok), null)
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialogInterface) {
                                            getFragmentManager().popBackStack();
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_booking_package, container, false);

        Toolbar toolBar;
        toolBar = rootView.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#FBB03B"));
        setSupportActionBar(toolBar);

        mAutoRepeatStatusTextView = rootView.findViewById(R.id.tv_auto_repeat_status);
        mAutoRepeatStatusImageView = rootView.findViewById(R.id.imv_auto_repeat_status);

        mWeek1ContentLinearLayout = rootView.findViewById(R.id.ll_week1_content);
        mWeek2ContentLinearLayout = rootView.findViewById(R.id.ll_week2_content);
        mWeek3ContentLinearLayout = rootView.findViewById(R.id.ll_week3_content);
        mWeek4ContentLinearLayout = rootView.findViewById(R.id.ll_week4_content);

        mWeek1ScheduleRecyclerView = rootView.findViewById(R.id.rv_week1_schedules);
        mWeek2ScheduleRecyclerView = rootView.findViewById(R.id.rv_week2_schedules);
        mWeek3ScheduleRecyclerView = rootView.findViewById(R.id.rv_week3_schedules);
        mWeek4ScheduleRecyclerView = rootView.findViewById(R.id.rv_week4_schedules);

        mWeek1CountTextView = rootView.findViewById(R.id.tv_week1_schedule_count);
        mWeek2CountTextView = rootView.findViewById(R.id.tv_week2_schedule_count);
        mWeek3CountTextView = rootView.findViewById(R.id.tv_week3_schedule_count);
        mWeek4CountTextView = rootView.findViewById(R.id.tv_week4_schedule_count);

        TextView mWeek1DurationTextView = rootView.findViewById(R.id.tv_week1_duration);
        mWeek1DurationTextView.setText(String.format(Locale.getDefault(), "%s - %s",
                CalendarUtil.getFirstDateStringOfWeek(1),
                CalendarUtil.getLastDateStringOfWeek(1)));

        TextView mWeek2DurationTextView = rootView.findViewById(R.id.tv_week2_duration);
        mWeek2DurationTextView.setText(String.format(Locale.getDefault(), "%s - %s",
                CalendarUtil.getFirstDateStringOfWeek(2),
                CalendarUtil.getLastDateStringOfWeek(2)));

        TextView mWeek3DurationTextView = rootView.findViewById(R.id.tv_week3_duration);
        mWeek3DurationTextView.setText(String.format(Locale.getDefault(), "%s - %s",
                CalendarUtil.getFirstDateStringOfWeek(3),
                CalendarUtil.getLastDateStringOfWeek(3)));

        TextView mWeek4DurationTextView = rootView.findViewById(R.id.tv_week4_duration);
        mWeek4DurationTextView.setText(String.format(Locale.getDefault(), "%s - %s",
                CalendarUtil.getFirstDateStringOfWeek(4),
                CalendarUtil.getLastDateStringOfWeek(4)));

        RelativeLayout rlWeek1Header = rootView.findViewById(R.id.rl_week1_header);
        rlWeek1Header.setOnClickListener(this);

        RelativeLayout rlWeek2Header = rootView.findViewById(R.id.rl_week2_header);
        rlWeek2Header.setOnClickListener(this);

        RelativeLayout rlWeek3Header = rootView.findViewById(R.id.rl_week3_header);
        rlWeek3Header.setOnClickListener(this);

        RelativeLayout rlWeek4Header = rootView.findViewById(R.id.rl_week4_header);
        rlWeek4Header.setOnClickListener(this);

        rootView.findViewById(R.id.fab_week1_add).setOnClickListener(this);

        rootView.findViewById(R.id.fab_week2_add).setOnClickListener(this);

        rootView.findViewById(R.id.fab_week3_add).setOnClickListener(this);

        rootView.findViewById(R.id.fab_week4_add).setOnClickListener(this);

        LinearLayout llConfirmReserve = rootView.findViewById(R.id.ll_confirm_reserve);
        llConfirmReserve.setOnClickListener(this);

        RelativeLayout rlAutoRepeat = rootView.findViewById(R.id.rl_auto_repeat);
        rlAutoRepeat.setOnClickListener(this);

        refreshUI();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        setTitle(getString(R.string.bo_bp_title));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
    }

    private boolean isSelectedAnySchedule() throws ParseException {
        for (int i = 0; i < mWeek1Schedules.size(); i++) {
            Map<String, String> item = mWeek1Schedules.get(i);
            if (!item.isEmpty())
                return true;
        }

        for (int i = 0; i < mWeek2Schedules.size(); i++) {
            Map<String, String> item = mWeek2Schedules.get(i);
            if (!item.isEmpty())
                return true;
        }

        for (int i = 0; i < mWeek3Schedules.size(); i++) {
            Map<String, String> item = mWeek3Schedules.get(i);
            if (!item.isEmpty())
                return true;
        }

        for (int i = 0; i < mWeek4Schedules.size(); i++) {
            Map<String, String> item = mWeek4Schedules.get(i);
            if (!item.isEmpty())
                return true;
        }

        return false;
    }

    private String calculateEndTime(String startTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date start = dateFormat.parse(startTime);
            start.setTime(start.getTime() + (1000 * 60 * 30));

            return dateFormat.format(start);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String generateScheduleString() throws ParseException {
        List<String> scheduledBlocks = new ArrayList<>();

        String schedules = "";
        boolean isValidAll = true;
        for (int i = 0; i < mWeek1Schedules.size(); i++) {
            Map<String, String> item = mWeek1Schedules.get(i);
            if (item.isEmpty()) {
                isValidAll = false;
                break;
            }

            String firstBlock = item.get("start_time_full");
            String secondBlock = calculateEndTime(firstBlock);
            scheduledBlocks.add(firstBlock);
            scheduledBlocks.add(secondBlock);

            String schedule = LGCUtil.convertToUTC(firstBlock) + "|" + item.get("tutor_id");
            schedules += (schedule + ",");
        }

        for (int i = 0; i < mWeek2Schedules.size(); i++) {
            Map<String, String> item = mWeek2Schedules.get(i);
            if (item.isEmpty()) {
                isValidAll = false;
                break;
            }

            String startTime = item.get("start_time_full");
            String endTime = calculateEndTime(startTime);
            scheduledBlocks.add(startTime);
            scheduledBlocks.add(endTime);

            String schedule = LGCUtil.convertToUTC(startTime) + "|" + item.get("tutor_id");
            schedules += (schedule + ",");
        }

        for (int i = 0; i < mWeek3Schedules.size(); i++) {
            Map<String, String> item = mWeek3Schedules.get(i);
            if (item.isEmpty()) {
                isValidAll = false;
                break;
            }

            String startTime = item.get("start_time_full");
            String endTime = calculateEndTime(startTime);
            scheduledBlocks.add(startTime);
            scheduledBlocks.add(endTime);

            String schedule = LGCUtil.convertToUTC(startTime) + "|" + item.get("tutor_id");
            schedules += (schedule + ",");
        }

        for (int i = 0; i < mWeek4Schedules.size(); i++) {
            Map<String, String> item = mWeek4Schedules.get(i);
            if (item.isEmpty()) {
                isValidAll = false;
                break;
            }

            String startTime = item.get("start_time_full");
            String endTime = calculateEndTime(startTime);
            scheduledBlocks.add(startTime);
            scheduledBlocks.add(endTime);

            String schedule = LGCUtil.convertToUTC(startTime) + "|" + item.get("tutor_id");
            schedules += (schedule);

            if (i < mWeek4Schedules.size() - 1) {
                schedules += ",";
            }
        }

        if (isValidAll) {
            List<String> checkedSchedules = new ArrayList<>();
            for (String schedule : scheduledBlocks) {
                if (checkedSchedules.contains(schedule)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.bo_sd_same_time_error))
                            .setMessage(getString(R.string.bo_sd_same_time_msg))
                            .setPositiveButton(getString(R.string.bo_sd_same_time_ok), null)
                            .create()
                            .show();
                    return null;
                } else {
                    checkedSchedules.add(schedule);
                }
            }
        } else {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.bo_sd_empty_error))
                    .setMessage(getString(R.string.bo_sd_empty_msg))
                    .setPositiveButton(getString(R.string.bo_sd_empty_ok), null)
                    .create()
                    .show();
            return null;
        }

        if (schedules.length() > 0 && schedules.charAt(schedules.length() - 1) == ',') {
            schedules = schedules.substring(0, schedules.length() - 1);
        }

        return schedules;
    }

    private boolean isScheduleLeft() {
        int selectedCount = mWeek1Schedules.size() + mWeek2Schedules.size()
                + mWeek3Schedules.size() + mWeek4Schedules.size();
        if (maxCount == selectedCount) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.bo_no_lesson_error))
                    .setMessage(getString(R.string.bo_no_lesson_msg))
                    .setPositiveButton(getString(R.string.bo_no_lesson_ok), null)
                    .create()
                    .show();
        }
        return maxCount > selectedCount;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_week1_header:
                mSelectedWeek = 1;
                break;
            case R.id.rl_week2_header:
                mSelectedWeek = 2;
                break;
            case R.id.rl_week3_header:
                mSelectedWeek = 3;
                break;
            case R.id.rl_week4_header:
                mSelectedWeek = 4;
                break;

            case R.id.fab_week1_add:
                if (isScheduleLeft()) {
                    mWeek1Schedules.add(new HashMap<String, String>());
                }
                break;
            case R.id.fab_week2_add:
                if (isScheduleLeft()) {
                    mWeek2Schedules.add(new HashMap<String, String>());
                }
                break;
            case R.id.fab_week3_add:
                if (isScheduleLeft()) {
                    mWeek3Schedules.add(new HashMap<String, String>());
                }
                break;
            case R.id.fab_week4_add:
                if (isScheduleLeft()) {
                    mWeek4Schedules.add(new HashMap<String, String>());
                }
                break;
            case R.id.rl_auto_repeat:
                Intent autoRepeatIntent = new Intent(getActivity(), AutoRepeatActivity.class);
                startActivityForResult(autoRepeatIntent, 1000);
                break;
            case R.id.ll_confirm_reserve:
                String schedules = null;
                try {
                    schedules = generateScheduleString();
                } catch (ParseException e) {
                    // ignore
                }
                if (!TextUtils.isEmpty(schedules)) {
                    final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage(getString(R.string.bo_course_loading));
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    mRequestManager.bookCourse(mStudentPackage.getCourseId(), schedules,
                            new ServerRequestManager.OnRequestFinishedListener() {
                                @Override
                                public void onSuccess(Object result) {
                                    progressDialog.dismiss();

                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put("Auto Repeat", mAutoRepeatStatusTextView.getText().toString());
                                    FlurryAgent.logEvent("Booked Package");

                                    if (getActivity() != null) {
                                        final Dialog dialog = new Dialog(getActivity());
                                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                        dialog.setContentView(R.layout.dialog_package_book_success);

                                        int count = (mWeek1Schedules.size() + mWeek1Schedules.size()
                                                + mWeek1Schedules.size() + mWeek1Schedules.size());

                                        TextView tvSubtitle = dialog.findViewById(R.id.tv_subtitle);
                                        tvSubtitle.setText(String.format(Locale.getDefault(),
                                                getString(R.string.bo_class_total) + " %d " + getString(R.string.bo_class_succeed), count));

                                        TextView tvGoToMain = dialog.findViewById(R.id.tv_back_to_main_menu);
                                        tvGoToMain.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                dialog.dismiss();
                                                FlurryAgent.logEvent("Back to Main (Booked package)");

                                                goToMain();
                                            }
                                        });

                                        TextView tvMySchedule = dialog.findViewById(R.id.tv_my_schedule);
                                        tvMySchedule.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                dialog.dismiss();
                                                FlurryAgent.logEvent("Back to Schedules (Booked package)");

                                                FragmentManager frgMgr = getFragmentManager();
                                                Fragment frg = frgMgr.findFragmentById(R.id.container);
                                                Fragment packageListFragment = new ScheduleDetailFragment();

                                                if (frg == null) {
                                                    frgMgr.beginTransaction().add(R.id.container, packageListFragment).commit();
                                                } else {
                                                    frgMgr.beginTransaction().replace(R.id.container, packageListFragment).commit();
                                                }
                                            }
                                        });

                                        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialogInterface) {
                                                goToMain();
                                            }
                                        });

                                        dialog.show();
                                    }
                                }

                                @Override
                                public void onError(ServerError err) {
                                    progressDialog.dismiss();

                                    if (getActivity() != null) {
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle(getString(R.string.bo_course_error))
                                                .setMessage(err.getMessage())
                                                .setPositiveButton(getString(R.string.bo_course_ok), null)
                                                .create()
                                                .show();
                                    }
                                }
                            });
                }
                break;
        }

        refreshUI();
    }

    private void goToMain() {
        FragmentManager frgMgr = getFragmentManager();
        Fragment frg = frgMgr.findFragmentById(R.id.container);
        Fragment packageListFragment = new MainFragment();

        if (frg == null) {
            frgMgr.beginTransaction().add(R.id.container, packageListFragment).commit();
        } else {
            frgMgr.beginTransaction().replace(R.id.container, packageListFragment).commit();
        }
    }

    private void refreshUI() {
        mWeek1ContentLinearLayout.setVisibility(View.GONE);
        mWeek2ContentLinearLayout.setVisibility(View.GONE);
        mWeek3ContentLinearLayout.setVisibility(View.GONE);
        mWeek4ContentLinearLayout.setVisibility(View.GONE);

        ScheduleListAdapter adapter = new ScheduleListAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);

        switch (mSelectedWeek) {
            case 1:
                mWeek1ContentLinearLayout.setVisibility(View.VISIBLE);
                mWeek1ScheduleRecyclerView.setLayoutManager(layoutManager);
                mWeek1ScheduleRecyclerView.setAdapter(adapter);
                break;
            case 2:
                mWeek2ContentLinearLayout.setVisibility(View.VISIBLE);
                mWeek2ScheduleRecyclerView.setLayoutManager(layoutManager);
                mWeek2ScheduleRecyclerView.setAdapter(adapter);
                break;
            case 3:
                mWeek3ContentLinearLayout.setVisibility(View.VISIBLE);
                mWeek3ScheduleRecyclerView.setLayoutManager(layoutManager);
                mWeek3ScheduleRecyclerView.setAdapter(adapter);
                break;
            case 4:
                mWeek4ContentLinearLayout.setVisibility(View.VISIBLE);
                mWeek4ScheduleRecyclerView.setLayoutManager(layoutManager);
                mWeek4ScheduleRecyclerView.setAdapter(adapter);
                break;
        }

        mWeek1CountTextView.setText(String.valueOf(mWeek1Schedules.size()));
        mWeek2CountTextView.setText(String.valueOf(mWeek2Schedules.size()));
        mWeek3CountTextView.setText(String.valueOf(mWeek3Schedules.size()));
        mWeek4CountTextView.setText(String.valueOf(mWeek4Schedules.size()));

        // Change color
        mWeek1CountTextView.setTextColor(Color.parseColor("#00aba0"));
        mWeek1CountTextView.setBackgroundResource(R.drawable.bg_tv_border_green);
        mWeek2CountTextView.setTextColor(Color.parseColor("#00aba0"));
        mWeek2CountTextView.setBackgroundResource(R.drawable.bg_tv_border_green);
        mWeek3CountTextView.setTextColor(Color.parseColor("#00aba0"));
        mWeek3CountTextView.setBackgroundResource(R.drawable.bg_tv_border_green);
        mWeek4CountTextView.setTextColor(Color.parseColor("#00aba0"));
        mWeek4CountTextView.setBackgroundResource(R.drawable.bg_tv_border_green);

        for (Map item : mWeek1Schedules) {
            if (item.isEmpty()) {
                mWeek1CountTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray));
                mWeek1CountTextView.setBackgroundResource(R.drawable.bg_tv_border_gray);
                break;
            }
        }
        for (Map item : mWeek2Schedules) {
            if (item.isEmpty()) {
                mWeek2CountTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray));
                mWeek2CountTextView.setBackgroundResource(R.drawable.bg_tv_border_gray);
                break;
            }
        }
        for (Map item : mWeek3Schedules) {
            if (item.isEmpty()) {
                mWeek3CountTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray));
                mWeek3CountTextView.setBackgroundResource(R.drawable.bg_tv_border_gray);
                break;
            }
        }
        for (Map item : mWeek4Schedules) {
            if (item.isEmpty()) {
                mWeek4CountTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray));
                mWeek4CountTextView.setBackgroundResource(R.drawable.bg_tv_border_gray);
                break;
            }
        }
    }

    private String getSecondBlock(String firstBlock) {
        long secondTimeBlock = CommonUtil.convertStringToDate(firstBlock, "yyyy-MM-dd HH:mm:ss").getTime() + 1800000;
        return CommonUtil.getFormattedDate(new Date(secondTimeBlock), "yyyy-MM-dd HH:mm:ss", false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                mAutoRepeatStatusTextView.setText(getString(R.string.bo_auto_repeat_on));
                mAutoRepeatStatusTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.yellow));
                mAutoRepeatStatusImageView.setImageResource(R.drawable.ic_arrow_head_down_black);

                mWeek1Schedules.clear();
                mWeek2Schedules.clear();
                mWeek3Schedules.clear();
                mWeek4Schedules.clear();

                List<String> startTimes = data.getStringArrayListExtra(EXTRA_AUTO_REPEAT_SCHEDULES);
                int schedulesPerWeek = startTimes.size() / 4;
                for (int i = 0; i < schedulesPerWeek; i++) {

                    Map<String, String> schedule1 = new HashMap<>();
                    String startTime1 = startTimes.get(i);
                    String tutorId1 = mTutorAdapter.getAvailableTutorIdForSchedule(startTime1, getSecondBlock(startTime1));
                    if (!TextUtils.isEmpty(startTime1) && !TextUtils.isEmpty(tutorId1)) {
                        schedule1.put("tutor_id", tutorId1);
                        schedule1.put("start_time_full", startTime1);
                    }
                    mWeek1Schedules.add(schedule1);

                    Map<String, String> schedule2 = new HashMap<>();
                    String startTime2 = startTimes.get(i + schedulesPerWeek);
                    String tutorId2 = mTutorAdapter.getAvailableTutorIdForSchedule(startTime2, getSecondBlock(startTime2));
                    if (!TextUtils.isEmpty(startTime1) && !TextUtils.isEmpty(tutorId2)) {
                        schedule2.put("tutor_id", tutorId2);
                        schedule2.put("start_time_full", startTime2);
                    }
                    mWeek2Schedules.add(schedule2);

                    Map<String, String> schedule3 = new HashMap<>();
                    String startTime3 = startTimes.get(i + schedulesPerWeek * 2);
                    String tutorId3 = mTutorAdapter.getAvailableTutorIdForSchedule(startTime3, getSecondBlock(startTime3));
                    if (!TextUtils.isEmpty(startTime3) && !TextUtils.isEmpty(tutorId3)) {
                        schedule3.put("tutor_id", tutorId3);
                        schedule3.put("start_time_full", startTime3);
                    }
                    mWeek3Schedules.add(schedule3);

                    Map<String, String> schedule4 = new HashMap<>();
                    String startTime4 = startTimes.get(i + schedulesPerWeek * 3);
                    String tutorId4 = mTutorAdapter.getAvailableTutorIdForSchedule(startTime4, getSecondBlock(startTime4));
                    if (!TextUtils.isEmpty(startTime4) && !TextUtils.isEmpty(tutorId4)) {
                        schedule4.put("tutor_id", tutorId4);
                        schedule4.put("start_time_full", startTime4);
                    }
                    mWeek4Schedules.add(schedule4);
                }
                refreshUI();
            } else {
                mAutoRepeatStatusTextView.setText(getString(R.string.bo_auto_repeat_off));
                mAutoRepeatStatusTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_color_gray));
                mAutoRepeatStatusImageView.setImageResource(R.drawable.ic_arrow_head_right_yellow);
            }
        } else {
            if (resultCode == Activity.RESULT_OK) {
                Map<String, String> item = new HashMap<>();
                item.put("tutor_id", data.getStringExtra("EXTRA_TUTOR_ID"));
                item.put("start_time_full", data.getStringExtra("EXTRA_START_TIME"));

                if (mSelectedWeek == 1) {
                    mWeek1Schedules.remove(requestCode);
                    mWeek1Schedules.add(requestCode, item);
                } else if (mSelectedWeek == 2) {
                    mWeek2Schedules.remove(requestCode);
                    mWeek2Schedules.add(requestCode, item);
                } else if (mSelectedWeek == 3) {
                    mWeek3Schedules.remove(requestCode);
                    mWeek3Schedules.add(requestCode, item);
                } else if (mSelectedWeek == 4) {
                    mWeek4Schedules.remove(requestCode);
                    mWeek4Schedules.add(requestCode, item);
                }
            }
        }
        refreshUI();
    }

    @Override
    public boolean onBackPressed() {
        try {
            if (isSelectedAnySchedule()) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.bo_pg_cancel_error))
                        .setMessage(getString(R.string.bo_pg_cancel_msg))
                        .setNegativeButton(getString(R.string.bo_pg_negative_btn), null)
                        .setPositiveButton(getString(R.string.bo_pg_cancel_sure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FlurryAgent.logEvent("Cancel package booking");

                                getFragmentManager().popBackStack();
                            }
                        })
                        .show();
            } else {
                getFragmentManager().popBackStack();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            getFragmentManager().popBackStack();
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_package_booking, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.opt_help:
                FlurryAgent.logEvent("Read package booking rules again");
                Intent intent = new Intent(getActivity(), FragmentHolderActivity.class);
                intent.putExtra(FragmentHolderActivity.EXTRA_DISPLAY_FRAGMENT,
                        TutorialFragment.class.getSimpleName());
                intent.putExtra(TutorialFragment.TUTORIAL_NO, 4);
                startActivity(intent);
                return true;
        }
        return false;
    }

    private class ScheduleListAdapter extends RecyclerView.Adapter<ScheduleListAdapter.ViewHolder> {

        private static final int EMPTY_VIEW = 1;
        private static final int DATA_VIEW = 2;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_package_booking,
                    parent, false);
            return new ViewHolder(v, viewType);
        }

        @Override
        public int getItemViewType(int position) {
            Map<String, String> item = getItem(position);
            if (item != null && !item.isEmpty()) {
                return DATA_VIEW;
            }
            return EMPTY_VIEW;
        }

        public Map<String, String> getItem(int position) {
            Map<String, String> item = null;
            if (mSelectedWeek == 1) {
                item = mWeek1Schedules.get(position);
            } else if (mSelectedWeek == 2) {
                item = mWeek2Schedules.get(position);
            } else if (mSelectedWeek == 3) {
                item = mWeek3Schedules.get(position);
            } else if (mSelectedWeek == 4) {
                item = mWeek4Schedules.get(position);
            }
            return item;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            //TODO
            if (holder.getItemViewType() == EMPTY_VIEW) {
                holder.tvStartTimeLabel.setVisibility(View.GONE);
                holder.llScheduleTime.setVisibility(View.GONE);
            } else {
                holder.tvStartTimeLabel.setVisibility(View.VISIBLE);
                holder.llScheduleTime.setVisibility(View.VISIBLE);

                Map<String, String> itemData = getItem(position);
                String tutorId = itemData.get("tutor_id");
                String startTimeFull = itemData.get("start_time_full");
                Date startTimeDate = CommonUtil.convertStringToDate(startTimeFull, "yyyy-MM-dd HH:mm:ss");

                holder.tvScheduleDate.setText(CommonUtil.getFormattedDate(startTimeDate, "MMMM dd", true));

                holder.tvStartTime.setText(CommonUtil.getFormattedDate(startTimeDate, "HH:mm", false));
                holder.tvEndTime.setText(CommonUtil.getFormattedDate(
                        new Date(startTimeDate.getTime() + 50 * 60 * 1000), "HH:mm", false));

                Tutor tutor = mTutorAdapter.getTutorById(tutorId);
                if (tutor != null && !TextUtils.isEmpty(tutor.getAvatar())) {
                    holder.sdvTutorPhoto.setImageURI(Uri.parse(tutor.getAvatar()));
                }
            }

            holder.slSchedule.setClickToClose(true);
            holder.slSchedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(),
                            TutorTimeSelectActivity.class);
                    intent.putExtra(TutorTimeSelectActivity.EXTRA_WEEK_NO, mSelectedWeek);
                    intent.putExtra(TutorTimeSelectActivity.EXTRA_BOOKING_TYPE, Booking.Type.PACKAGE);
                    startActivityForResult(intent, holder.getAdapterPosition());
                }
            });

            holder.slSchedule.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {
                    layout.setOnClickListener(null);
                }

                @Override
                public void onOpen(SwipeLayout layout) {
                }

                @Override
                public void onStartClose(SwipeLayout layout) {

                }

                @Override
                public void onClose(SwipeLayout layout) {
                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

                }

                @Override
                public void onHandRelease(final SwipeLayout layout, float xvel, float yvel) {
                    if (layout.getOpenStatus() == SwipeLayout.Status.Close) {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            layout.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Intent intent = new Intent(getActivity(),
                                                            TutorTimeSelectActivity.class);
                                                    intent.putExtra(TutorTimeSelectActivity.EXTRA_WEEK_NO, mSelectedWeek);
                                                    intent.putExtra(TutorTimeSelectActivity.EXTRA_BOOKING_TYPE, Booking.Type.PACKAGE);
                                                    startActivityForResult(intent, holder.getAdapterPosition());
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }, 100);
                    }
                }
            });

            holder.imvDelete.setOnClickListener(onDeleteClickListener(holder.getAdapterPosition()));
        }

        @NonNull
        private View.OnClickListener onDeleteClickListener(final int position) {
            int selectedCount = mWeek1Schedules.size() + mWeek2Schedules.size()
                    + mWeek3Schedules.size() + mWeek4Schedules.size();
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (mSelectedWeek) {
                        case 1:
                            if (mWeek1Schedules.size() > 1) {
                                mWeek1Schedules.remove(position);
                                refreshUI();
//                            } else if (maxCount < 4) {
//                                mWeek1Schedules.remove(position);
//                                refreshUI();
                            } else {
                                showCantDeleteAlert();
                            }
                            break;
                        case 2:
                            if (mWeek2Schedules.size() > 1) {
                                mWeek2Schedules.remove(position);
                                refreshUI();
                            } else if (maxCount < 4) {
                                mWeek2Schedules.remove(position);
                                refreshUI();
                            } else {
                                showCantDeleteAlert();
                            }
                            break;
                        case 3:
                            if (mWeek3Schedules.size() > 1) {
                                mWeek3Schedules.remove(position);
                                refreshUI();
                            } else if (maxCount < 4) {
                                mWeek3Schedules.remove(position);
                                refreshUI();
                            } else {
                                showCantDeleteAlert();
                            }
                            break;
                        case 4:
                            if (mWeek4Schedules.size() > 1) {
                                mWeek4Schedules.remove(position);
                                refreshUI();
                            } else if (maxCount < 4) {
                                mWeek4Schedules.remove(position);
                                refreshUI();
                            } else {
                                showCantDeleteAlert();
                            }
                            break;
                    }
                }
            };
        }

        private void showCantDeleteAlert() {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.bo_limited_lesson))
                    .setMessage(getString(R.string.bo_limited_lesson_msg))
                    .setPositiveButton(getString(R.string.bo_limited_lesson_ok), null)
                    .create()
                    .show();
        }

        @Override
        public int getItemCount() {
            switch (mSelectedWeek) {
                case 1:
                    return mWeek1Schedules.size();
                case 2:
                    return mWeek2Schedules.size();
                case 3:
                    return mWeek3Schedules.size();
                case 4:
                    return mWeek4Schedules.size();
            }
            return 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView tvStartTimeLabel;
            TextView tvStartTime;
            TextView tvEndTime;
            TextView tvScheduleDate;
            LinearLayout llScheduleTime;
            SimpleDraweeView sdvTutorPhoto;
            ImageView imvDelete;

            SwipeLayout slSchedule;

            public ViewHolder(View itemView, int itemType) {
                super(itemView);

                tvStartTimeLabel = itemView.findViewById(R.id.tv_start_time_lbl);
                tvStartTime = itemView.findViewById(R.id.tv_start_time);
                tvEndTime = itemView.findViewById(R.id.tv_end_time);
                tvScheduleDate = itemView.findViewById(R.id.tv_schedule_date);
                llScheduleTime = itemView.findViewById(R.id.ll_schedule_time);

                sdvTutorPhoto = itemView.findViewById(R.id.sdv_tutor_photo);
                imvDelete = itemView.findViewById(R.id.imv_delete_schedule);

                slSchedule = itemView.findViewById(R.id.sl_schedule);
            }
        }
    }
}
