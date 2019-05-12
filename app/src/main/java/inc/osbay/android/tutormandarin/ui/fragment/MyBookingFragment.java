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
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flurry.android.FlurryAgent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.sdk.model.Booking;
import inc.osbay.android.tutormandarin.sdk.model.Lesson;
import inc.osbay.android.tutormandarin.sdk.model.Topic;
import inc.osbay.android.tutormandarin.sdk.model.TopicClass;
import inc.osbay.android.tutormandarin.sdk.model.Tutor;
import inc.osbay.android.tutormandarin.sdk.util.LGCUtil;
import inc.osbay.android.tutormandarin.ui.activity.FragmentHolderActivity;
import inc.osbay.android.tutormandarin.ui.activity.LessonPdfActivity;
import inc.osbay.android.tutormandarin.ui.activity.TutorTimeSelectActivity;
import inc.osbay.android.tutormandarin.util.CommonUtil;

import static inc.osbay.android.tutormandarin.ui.fragment.TutorialFragment.PREF_BOOKING_TUTORIAL;

public class MyBookingFragment extends BackHandledFragment implements View.OnClickListener {
    public static final String EXTRA_LESSON_ID = "MyBookingFragment.EXTRA_LESSON_ID";
    public static final String EXTRA_CLASS_ID = "MyBookingFragment.EXTRA_CLASS_ID";
    public static final String EXTRA_TUTOR_ID = "MyBookingFragment.EXTRA_TUTOR_ID";
    public static final String EXTRA_START_TIME = "MyBookingFragment.EXTRA_START_TIME";
    public static final String EXTRA_BOOKING_TYPE = "MyBookingFragment.EXTRA_BOOKING_TYPE";

    private SharedPreferences mPreferences;
    private ServerRequestManager mServerRequestManager;
    private Timer mTimer;
    private LinearLayout mTotalCostLinearLayout;
    private TextView mTotalCostTextView;
    private CurriculumAdapter curriculumAdapter;
    private TutorAdapter tutorAdapter;
    private TextView tvSelectTutorMsg;
    private RelativeLayout rlTutorTimeInfo;
    private TextView tvBookDate;
    private TextView tvTutorCreditWeight;
    private TextView tvStartTime;
    private TextView tvEndTime;
    private TextView tvSelectLessonMsg;
    private RelativeLayout rlLessonInfo;

    private TextView tvLessonNo;
    private TextView tvTitle;
    private TextView tvLevel;
    private TextView tvCredit;
    private Lesson mLesson;
    private TopicClass mTopicClass;
    private Tutor mTutor;
    private String mStartTimeFull;
    private int mBookingType;
    private TextView mMyCreditTextView;
    private SimpleDraweeView mTutorPhotoDraweeView;
    private SimpleDraweeView mLessonPhotoDraweeView;
    private double mCredit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mServerRequestManager = new ServerRequestManager(getActivity().getApplicationContext());
        mPreferences = PreferenceManager.getDefaultSharedPreferences(
                getActivity().getApplicationContext());
        curriculumAdapter = new CurriculumAdapter(getActivity());
        tutorAdapter = new TutorAdapter(getActivity());

        String lessonId = null;
        String tutorId = null;
        String classId = null;

        Bundle bundle = getArguments();
        if (bundle != null) {
            lessonId = bundle.getString(EXTRA_LESSON_ID);
            classId = bundle.getString(EXTRA_CLASS_ID);
            tutorId = bundle.getString(EXTRA_TUTOR_ID);
            mStartTimeFull = bundle.getString(EXTRA_START_TIME);

            mBookingType = bundle.getInt(EXTRA_BOOKING_TYPE);
        }

        if (!TextUtils.isEmpty(lessonId)) {
            mLesson = curriculumAdapter.getLessonById(lessonId);
        }

        if (!TextUtils.isEmpty(tutorId)) {
            mTutor = tutorAdapter.getTutorById(tutorId);
        }

        if (!TextUtils.isEmpty(classId)) {
            mTopicClass = curriculumAdapter.getTopicClassById(classId);
        }

        SharedPreferences preferences = getActivity().getSharedPreferences(PREF_BOOKING_TUTORIAL, Context.MODE_PRIVATE);
        int scheduleTutorial = preferences.getInt(PREF_BOOKING_TUTORIAL, 0);

        if (scheduleTutorial == 0) {
            Intent intent = new Intent(getActivity(), FragmentHolderActivity.class);
            intent.putExtra(FragmentHolderActivity.EXTRA_DISPLAY_FRAGMENT,
                    TutorialFragment.class.getSimpleName());
            intent.putExtra(TutorialFragment.TUTORIAL_NO, 3);
            startActivity(intent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_booking, container, false);

        Toolbar toolBar;
        toolBar = rootView.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#00ABA0"));
        setSupportActionBar(toolBar);

        mTotalCostLinearLayout = rootView.findViewById(R.id.ll_total_cost);

        mTotalCostTextView = rootView.findViewById(R.id.tv_total_cost);
        mMyCreditTextView = rootView.findViewById(R.id.tv_my_credit);

        mTutorPhotoDraweeView = rootView.findViewById(R.id.sdv_tutor_photo);
        mLessonPhotoDraweeView = rootView.findViewById(R.id.sdv_lesson_photo);

        String accountId = mPreferences.getString("account_id", "1");
        Account account = new AccountAdapter(getActivity()).getAccountById(accountId);

        if (account != null) {
            mCredit = account.getCredit();
            mMyCreditTextView.setText(String.format(Locale.getDefault(), "%.1f", account.getCredit()));
        }

        FloatingActionButton fabAddCredit = rootView.findViewById(R.id.fab_add_credit);
        fabAddCredit.setOnClickListener(this);

        RelativeLayout rlSelectTutor = rootView.findViewById(R.id.rl_select_tutor);
        rlSelectTutor.setOnClickListener(this);

        RelativeLayout rlSelectLesson = rootView.findViewById(R.id.rl_select_lesson);
        rlSelectLesson.setOnClickListener(this);

        LinearLayout llConfirmReserve = rootView.findViewById(R.id.ll_confirm_reserve);
        llConfirmReserve.setOnClickListener(this);

        rlTutorTimeInfo = rootView.findViewById(R.id.rl_tutor_time_info);

        tvSelectTutorMsg = rootView.findViewById(R.id.tv_select_tutor_msg);

        tvBookDate = rootView.findViewById(R.id.tv_book_date);
        tvTutorCreditWeight = rootView.findViewById(R.id.tv_tutor_credit_weight);
        tvStartTime = rootView.findViewById(R.id.tv_start_time);
        tvEndTime = rootView.findViewById(R.id.tv_end_time);
        tvSelectLessonMsg = rootView.findViewById(R.id.tv_select_lesson_msg);
        rlLessonInfo = rootView.findViewById(R.id.rl_lesson_info);
        tvLessonNo = rootView.findViewById(R.id.tv_lesson_number);
        tvTitle = rootView.findViewById(R.id.tv_lesson_title);
        tvLevel = rootView.findViewById(R.id.tv_lesson_level);
        tvCredit = rootView.findViewById(R.id.tv_class_credit);

        refreshUI();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        setTitle(getString(R.string.bo_booking_title));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_booking_help, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.opt_booking_help:
                Intent intent = new Intent(getActivity(), FragmentHolderActivity.class);
                intent.putExtra(FragmentHolderActivity.EXTRA_DISPLAY_FRAGMENT,
                        TutorialFragment.class.getSimpleName());
                intent.putExtra(TutorialFragment.TUTORIAL_NO, 3);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshUI() {
        if (mTutor != null && !TextUtils.isEmpty(mStartTimeFull) && mLesson != null) {
            mTotalCostLinearLayout.setVisibility(View.INVISIBLE);
            mTotalCostTextView.setVisibility(View.VISIBLE);

            String totalCost = getString(R.string.bo_total_cost, String.format(
                    Locale.getDefault(), "%.0f x %.1f = %.1f", mLesson.getCredit(),
                    mTutor.getCreditWeight(), mLesson.getCredit() * mTutor.getCreditWeight()));
            mTotalCostTextView.setText(totalCost);
        } else if (mTutor != null && !TextUtils.isEmpty(mStartTimeFull) && mTopicClass != null) {
            mTotalCostLinearLayout.setVisibility(View.INVISIBLE);
            mTotalCostTextView.setVisibility(View.VISIBLE);

            String totalCost = getString(R.string.bo_total_cost, String.format(
                    Locale.getDefault(), "%.0f x %.1f = %.1f", mTopicClass.getCredit(),
                    mTutor.getCreditWeight(), mTopicClass.getCredit() * mTutor.getCreditWeight()));
            mTotalCostTextView.setText(totalCost);
        } else {
            mTotalCostTextView.setVisibility(View.INVISIBLE);
            mTotalCostLinearLayout.setVisibility(View.VISIBLE);
        }

        if (mTutor == null || TextUtils.isEmpty(mStartTimeFull)) {
            tvSelectTutorMsg.setVisibility(View.VISIBLE);
            rlTutorTimeInfo.setVisibility(View.GONE);
        } else {
            rlTutorTimeInfo.setVisibility(View.VISIBLE);
            tvSelectTutorMsg.setVisibility(View.GONE);

            Date date = new Date();
            try {
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(mStartTimeFull);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            tvBookDate.setText(CommonUtil.getFormattedDate(date, "MMM dd", true));

            tvTutorCreditWeight.setText(String.format(Locale.getDefault(), "x %.1f", mTutor.getCreditWeight()));

            tvStartTime.setText(CommonUtil.getFormattedDate(date, "HH:mm", false));

            String endTime;
            if (mBookingType == Booking.Type.TOPIC) {
                endTime = CommonUtil.getFormattedDate(new Date(date.getTime() + 1500000), "HH:mm", false);
            } else {
                endTime = CommonUtil.getFormattedDate(new Date(date.getTime() + 3000000), "HH:mm", false);
            }

            tvEndTime.setText(endTime);

            if (!TextUtils.isEmpty(mTutor.getAvatar())) {
                mTutorPhotoDraweeView.setImageURI(Uri.parse(mTutor.getAvatar()));
            }
        }

        if (mBookingType == Booking.Type.LESSON && mLesson == null) {
            tvSelectLessonMsg.setText(getString(R.string.bo_select_lesson_supplement));
            tvSelectLessonMsg.setVisibility(View.VISIBLE);
            rlLessonInfo.setVisibility(View.GONE);
        } else if (mBookingType == Booking.Type.TOPIC && mTopicClass == null) {
            tvSelectLessonMsg.setText(getString(R.string.bo_select_topic));
            tvSelectLessonMsg.setVisibility(View.VISIBLE);
            rlLessonInfo.setVisibility(View.GONE);
        } else {
            rlLessonInfo.setVisibility(View.VISIBLE);
            tvSelectLessonMsg.setVisibility(View.GONE);
            if (mBookingType == Booking.Type.TOPIC) {
                Topic topic = curriculumAdapter.getTopicById(mTopicClass.getTopicId());
                tvLessonNo.setText(topic.getTitle()); //TODO

                tvTitle.setText(mTopicClass.getTitle());

                tvLevel.setText(getString(R.string.bo_topic_level, mTopicClass.getLevel()));

                tvCredit.setText(String.format(Locale.getDefault(), "%.0f", mTopicClass.getCredit()));

                if (!TextUtils.isEmpty(mTopicClass.getCoverPhoto())) {
                    mLessonPhotoDraweeView.setImageURI(Uri.parse(mTopicClass.getCoverPhoto()));
                }
            }

            if (mBookingType == Booking.Type.LESSON) {
                if (mLesson.isSupplement()) {
                    tvLessonNo.setText(String.format(Locale.getDefault(), getString(R.string.bo_supplement) + " %d", mLesson.getLessonNumber()));
                } else {
                    tvLessonNo.setText(String.format(Locale.getDefault(), getString(R.string.bo_lesson) + " %d", mLesson.getLessonNumber()));
                }

                tvTitle.setText(mLesson.getTitle());

                tvLevel.setText(String.format(Locale.getDefault(), getString(R.string.bo_topic_level), mLesson.getLevel()));

                tvCredit.setText(String.format(Locale.getDefault(), "%.0f", mLesson.getCredit()));

                if (!TextUtils.isEmpty(mLesson.getCoverPhoto())) {
                    mLessonPhotoDraweeView.setImageURI(Uri.parse(mLesson.getCoverPhoto()));
                }
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        setDisplayHomeAsUpEnable(true);

        mTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                mServerRequestManager.refreshStudentBalance(new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        if (getActivity() != null) {
                            mCredit = Double.parseDouble(String.valueOf(result));
                            mMyCreditTextView.setText(String.format(Locale.getDefault(), "%.1f", mCredit));
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        // ignore
                    }
                });
            }
        };

        mTimer.schedule(task, 2000, 2000);
    }

    @Override
    public void onPause() {
        super.onPause();
        mTimer.cancel();
        mTimer.purge();
    }

    @Override
    public boolean onBackPressed() {
        if (mTutor != null) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.bo_not_booking_title))
                    .setMessage(getString(R.string.bo_not_booking_msg))
                    .setNegativeButton(getString(R.string.bo_not_booking_cancel), null)
                    .setPositiveButton(getString(R.string.bo_not_booking_sure), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FlurryAgent.logEvent("Cancel credit booking");

                            getFragmentManager().beginTransaction().remove(MyBookingFragment.this).commit();
                            getFragmentManager().popBackStack();
                        }
                    })
                    .show();
        } else {
            getFragmentManager().beginTransaction().remove(MyBookingFragment.this).commit();
            getFragmentManager().popBackStack();
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_add_credit:
                Map<String, String> params = new HashMap<>();
                params.put("Credits", String.valueOf(mCredit));
                FlurryAgent.logEvent("Click Add Credit (Credit booking)", params);
                goToStore();
                break;
            case R.id.rl_select_tutor:
                Intent selectTutorIntent = new Intent(getActivity(), TutorTimeSelectActivity.class);
                selectTutorIntent.putExtra(TutorTimeSelectActivity.EXTRA_BOOKING_TYPE, mBookingType);
                startActivityForResult(selectTutorIntent, 1);
                break;
            case R.id.rl_select_lesson:
                if (mBookingType == Booking.Type.TOPIC) {
                    Intent intent = new Intent(getActivity(), FragmentHolderActivity.class);
                    intent.putExtra(LessonPdfActivity.EXTRA_BOOKING_FRAGMENT, 1);
                    intent.putExtra(FragmentHolderActivity.EXTRA_DISPLAY_FRAGMENT,
                            SelectClassFragment.class.getSimpleName());
                    startActivityForResult(intent, 3);
                } else {
                    // Select lesson
                    Intent intent = new Intent(getActivity(), FragmentHolderActivity.class);
                    if (mTutor != null) {
                        intent.putExtra(EXTRA_TUTOR_ID, mTutor.getTutorId());
                    }
                    intent.putExtra(EXTRA_START_TIME, mStartTimeFull);
                    intent.putExtra(EXTRA_BOOKING_TYPE, mBookingType);
                    intent.putExtra(LessonPdfActivity.EXTRA_BOOKING_FRAGMENT, 1);
                    intent.putExtra(FragmentHolderActivity.EXTRA_DISPLAY_FRAGMENT,
                            SelectLessonFragment.class.getSimpleName());
                    startActivityForResult(intent, 2);
                }
                break;
            case R.id.ll_confirm_reserve:
                boolean isValidAll = true;
                if (mBookingType == Booking.Type.LESSON && mLesson == null) {
                    Toast.makeText(getActivity(), getString(R.string.bo_select_lesson_supplement), Toast.LENGTH_SHORT).show();
                    isValidAll = false;
                }

                if (mBookingType == Booking.Type.TOPIC && mTopicClass == null) {
                    Toast.makeText(getActivity(), getString(R.string.bo_cg_select_topic), Toast.LENGTH_SHORT).show();
                    isValidAll = false;
                }

                if (mTutor == null || TextUtils.isEmpty(mStartTimeFull)) {
                    Toast.makeText(getActivity(), getString(R.string.bo_cg_select_tutor), Toast.LENGTH_SHORT).show();
                    isValidAll = false;
                }

                if (isValidAll && mBookingType == Booking.Type.LESSON) {
                    double cost = mTutor.getCreditWeight() * mLesson.getCredit();
                    isValidAll = checkCost(isValidAll, cost);
                }

                if (isValidAll && mBookingType == Booking.Type.TOPIC) {
                    double cost = mTutor.getCreditWeight() * mTopicClass.getCredit();
                    isValidAll = checkCost(isValidAll, cost);
                }

                if (isValidAll) {
                    final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage(getString(R.string.bo_dialog_booking));
                    progressDialog.show();

                    try {
                        if (mLesson != null) {
                            mServerRequestManager.bookTutorForLesson(mLesson.getLessonId(),
                                    LGCUtil.convertToUTC(mStartTimeFull), mTutor.getTutorId(),
                                    new ServerRequestManager.OnRequestFinishedListener() {
                                        @Override
                                        public void onSuccess(Object result) {
                                            progressDialog.dismiss();
                                            if (getActivity() != null) {
                                                showDialog();
                                            }
                                        }

                                        @Override
                                        public void onError(ServerError err) {
                                            progressDialog.dismiss();
                                            if (getActivity() != null) {
                                                new AlertDialog.Builder(getActivity())
                                                        .setTitle(getString(R.string.bo_dialog_lesson_error))
                                                        .setMessage(err.getMessage())
                                                        .setPositiveButton(getString(R.string.bo_dialog_lesson_ok), null)
                                                        .create()
                                                        .show();
                                            }
                                        }
                                    });
                        } else {
                            mServerRequestManager.bookClass(mTopicClass.getClassId(),
                                    LGCUtil.convertToUTC(mStartTimeFull), mTutor.getTutorId(),
                                    new ServerRequestManager.OnRequestFinishedListener() {
                                        @Override
                                        public void onSuccess(Object result) {
                                            progressDialog.dismiss();
                                            if (getActivity() != null) {
                                                showDialog();
                                            }
                                        }

                                        @Override
                                        public void onError(ServerError err) {
                                            progressDialog.dismiss();
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
                        }
                    } catch (ParseException e) {
                        Toast.makeText(getActivity(), getString(R.string.bo_parse_exception), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private boolean checkCost(boolean isValidAll, double cost) {
        if (cost > mCredit) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.bo_check_cost_error))
                    .setMessage(getString(R.string.bo_check_cost_msg))
                    .setNegativeButton(getString(R.string.bo_check_cost_cancel), null)
                    .setPositiveButton(getString(R.string.bo_check_cost_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            goToStore();
                        }
                    })
                    .show();
            isValidAll = false;
        }
        return isValidAll;
    }

    private void showDialog() {
        final Dialog successDialog = new Dialog(getActivity());
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successDialog.setContentView(R.layout.dialog_credit_book_success);

        DialogItemClickListener clickListener = new DialogItemClickListener(successDialog);

        TextView tvMySchedule = successDialog.findViewById(R.id.tv_my_schedule);
        tvMySchedule.setOnClickListener(clickListener);

        TextView tvBookLesson = successDialog.findViewById(R.id.tv_book_lesson);
        tvBookLesson.setOnClickListener(clickListener);

        TextView tvChooseTutor = successDialog.findViewById(R.id.tv_choose_tutor);
        tvChooseTutor.setOnClickListener(clickListener);

        TextView tvBackToMain = successDialog.findViewById(R.id.tv_back_to_main_menu);
        tvBackToMain.setOnClickListener(clickListener);

        successDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                getFragmentManager().beginTransaction()
                        .remove(MyBookingFragment.this)
                        .commit();

                getFragmentManager().popBackStack(getFragmentManager().getBackStackEntryAt(0).getId(),
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        successDialog.show();
    }

    private void goToStore() {
        Fragment newFragment = new CreditStoreFragment();

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                String tutorId = data.getStringExtra("EXTRA_TUTOR_ID");
                mTutor = tutorAdapter.getTutorById(tutorId);

                mStartTimeFull = data.getStringExtra("EXTRA_START_TIME");
            } else if (requestCode == 2) {
                String lessonId = data.getStringExtra("EXTRA_LESSON_ID");
                mLesson = curriculumAdapter.getLessonById(lessonId);
            } else if (requestCode == 3) {
                //todo add topic
                String classId = data.getStringExtra("EXTRA_CLASS_ID");
                mTopicClass = curriculumAdapter.getTopicClassById(classId);
            }

            refreshUI();
        }
    }

    private class DialogItemClickListener implements View.OnClickListener {

        Dialog mDialog;

        public DialogItemClickListener(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public void onClick(View view) {
            mDialog.dismiss();

            Fragment newFragment = null;

            switch (view.getId()) {
                case R.id.tv_my_schedule:
                    FlurryAgent.logEvent("View Schedule (Credit booking)");
                    newFragment = new ScheduleDetailFragment();
                    break;
                case R.id.tv_book_lesson:
                    FlurryAgent.logEvent("Book another lesson (Credit booking)");
                    newFragment = new CourseTopicFragment();
                    break;
                case R.id.tv_choose_tutor:
                    FlurryAgent.logEvent("View other tutors (Credit booking)");
                    newFragment = new TutorListFragment();
                    break;
                case R.id.tv_back_to_main_menu:
                    FlurryAgent.logEvent("Back to Main (Credit booking)");
                    getFragmentManager().beginTransaction()
                            .remove(MyBookingFragment.this)
                            .commit();

                    getFragmentManager().popBackStack(getFragmentManager().getBackStackEntryAt(0).getId(),
                            FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    break;
            }
            getFragmentManager().beginTransaction()
                    .remove(MyBookingFragment.this)
                    .commit();

            if (newFragment != null) {
                FragmentManager fm = getFragmentManager();

                Fragment current = fm.findFragmentById(R.id.container);
                if (current == null) {
                    fm.beginTransaction()
                            .addToBackStack(null)
                            .add(R.id.container, newFragment).commit();
                } else {
                    fm.beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.container, newFragment).commit();
                }
            }
        }
    }
}
