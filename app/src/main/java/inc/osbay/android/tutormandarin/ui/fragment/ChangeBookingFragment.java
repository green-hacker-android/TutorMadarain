package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
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
import inc.osbay.android.tutormandarin.util.CalendarUtil;
import inc.osbay.android.tutormandarin.util.CommonUtil;

public class ChangeBookingFragment extends BackHandledFragment implements View.OnClickListener {
    public static final String EXTRA_BOOKING_ID = "ChangeBookingFragment.EXTRA_BOOKING_ID";
    public static final String EXTRA_TUTOR_ID = "MyBookingFragment.EXTRA_TUTOR_ID";
    public static final String EXTRA_START_TIME = "MyBookingFragment.EXTRA_START_TIME";
    public static final String EXTRA_BOOKING_TYPE = "MyBookingFragment.EXTRA_BOOKING_TYPE";
    public static final String EXTRA_LESSON_ID = "ChangeBookingFragment.EXTRA_LESSON_ID";

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
    private TextView tvStartTime;
    private TextView tvEndTime;
    private TextView tvSelectLessonMsg;
    private RelativeLayout rlLessonInfo;
    private TextView tvLessonNo;
    private TextView tvTitle;
    private TextView tvLevel;

    private Booking mBooking;
    private Lesson mLesson;
    private TopicClass mTopicClass;
    private Tutor mTutor;
    private String mStartTimeFull;
    private TextView mMyCreditTextView;
    private SimpleDraweeView mTutorPhotoDraweeView;
    private SimpleDraweeView mLessonPhotoDraweeView;
    private double mCredit;
    private String bookingId;
    private String mTutorId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mServerRequestManager = new ServerRequestManager(getActivity().getApplicationContext());
        mPreferences = PreferenceManager.getDefaultSharedPreferences(
                getActivity().getApplicationContext());

        AccountAdapter accountAdapter = new AccountAdapter(getActivity());
        curriculumAdapter = new CurriculumAdapter(getActivity());
        tutorAdapter = new TutorAdapter(getActivity());


        bookingId = null;
        String lessonId = null;
        Bundle bundle = getArguments();

        if (bundle != null) {
            bookingId = bundle.getString(EXTRA_BOOKING_ID);
            lessonId = bundle.getString(EXTRA_LESSON_ID);
            mTutorId = bundle.getString(EXTRA_TUTOR_ID);
            mStartTimeFull = bundle.getString(EXTRA_START_TIME);
        }

        if (!TextUtils.isEmpty(bookingId)) {
            mBooking = accountAdapter.getBookingById(bookingId);
            if (TextUtils.isEmpty(mTutorId) && TextUtils.isEmpty(mStartTimeFull)) {
                mTutor = tutorAdapter.getTutorById(mBooking.getTutorId());

                mStartTimeFull = mBooking.getBookedDate();
            } else {
                mTutor = tutorAdapter.getTutorById(mTutorId);
            }
        }

        if (lessonId == null) {
            lessonId = mBooking.getLessonId();
        }

        if (mBooking.getBookingType() == Booking.Type.LESSON) {
            mLesson = curriculumAdapter.getLessonById(lessonId);
        }

        if (mBooking.getBookingType() == Booking.Type.TOPIC) {
            mTopicClass = curriculumAdapter.getTopicClassById(mBooking.getLessonId());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_change_booking, container, false);

        Toolbar toolBar;
        toolBar = rootView.findViewById(R.id.tool_bar);
        setSupportActionBar(toolBar);

        Button btnChange = rootView.findViewById(R.id.btn_change_booking);
        btnChange.setOnClickListener(this);

        Button btnCancel = rootView.findViewById(R.id.btn_cancel_booking);
        btnCancel.setOnClickListener(this);

        CardView cvSelectLesson = rootView.findViewById(R.id.cv_select_lesson);
        cvSelectLesson.setOnClickListener(this);

        if (mBooking.getBookingType() == Booking.Type.PACKAGE) {
            toolBar.setBackgroundColor(Color.parseColor("#FBB03B"));
            btnChange.setBackgroundResource(R.drawable.btn_bg_rounded_orange);
            cvSelectLesson.setVisibility(View.GONE);
        } else {
            toolBar.setBackgroundColor(Color.parseColor("#00ABA0"));
            btnChange.setBackgroundResource(R.drawable.btn_bg_rounded_green);
            cvSelectLesson.setVisibility(View.VISIBLE);
        }

        CardView cvTotalCost = rootView.findViewById(R.id.cv_total_cost);
        Date bookedDate = CommonUtil.convertStringToDate(mBooking.getBookedDate(), "yyyy-MM-dd HH:mm:ss");
        if (bookedDate != null && bookedDate.getTime() < new Date().getTime() + CalendarUtil.ONE_DAY_IN_MILLI) {
            cvTotalCost.setVisibility(View.VISIBLE);
        } else {
            cvTotalCost.setVisibility(View.GONE);
        }

        // Flurry Analytics
        Map<String, String> params = new HashMap<>();
        params.put("minutes to start", String.valueOf((bookedDate.getTime() - new Date().getTime()) / 60000));
        FlurryAgent.logEvent("Change/Cancel schedule", params);

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

        rlTutorTimeInfo = rootView.findViewById(R.id.rl_tutor_time_info);

        tvSelectTutorMsg = rootView.findViewById(R.id.tv_select_tutor_msg);

        tvBookDate = rootView.findViewById(R.id.tv_book_date);
        tvStartTime = rootView.findViewById(R.id.tv_start_time);
        tvEndTime = rootView.findViewById(R.id.tv_end_time);
        tvSelectLessonMsg = rootView.findViewById(R.id.tv_select_lesson_msg);
        rlLessonInfo = rootView.findViewById(R.id.rl_lesson_info);
        tvLessonNo = rootView.findViewById(R.id.tv_lesson_number);
        tvTitle = rootView.findViewById(R.id.tv_lesson_title);
        tvLevel = rootView.findViewById(R.id.tv_lesson_level);

        refreshUI();

        return rootView;
    }

    private void refreshUI() {
        if (mTutor != null && !TextUtils.isEmpty(mStartTimeFull) && mLesson != null
                && mBooking.getBookingType() == Booking.Type.LESSON) { // Booking type 1 is credit(lesson/topic)
            mTotalCostLinearLayout.setVisibility(View.INVISIBLE);
            mTotalCostTextView.setVisibility(View.VISIBLE);

            String totalCost = getString(R.string.bo_total_cost, String.format(
                    Locale.getDefault(), "%.0f x %.1f = %.1f", mLesson.getCredit(),
                    mTutor.getCreditWeight(), mLesson.getCredit() * mTutor.getCreditWeight()));
            mTotalCostTextView.setText(totalCost);
        } else if (mTutor != null && !TextUtils.isEmpty(mStartTimeFull) && mTopicClass != null
                && mBooking.getBookingType() == Booking.Type.TOPIC) {
            mTotalCostLinearLayout.setVisibility(View.INVISIBLE);
            mTotalCostTextView.setVisibility(View.VISIBLE);

//            String totalCost = getString(R.string.booking_total_cost, String.format(
//                    Locale.getDefault(), "%.0f x %.1f = %.1f", mTopic.getCredit(),
//                    (float) mTutor.getCreditWeight(), mTopic.getCredit() * mTutor.getCreditWeight()));
//            mTotalCostTextView.setText(totalCost);
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

            tvBookDate.setText(CommonUtil.getFormattedDate(date, "MMMM dd", true));

//            tvTutorCreditWeight.setText("x " + mTutor.getCreditWeight());

            tvStartTime.setText(CommonUtil.getFormattedDate(date, "HH:mm", false));

            String endTime;
            if (mBooking.getBookingType() == Booking.Type.TOPIC) {
                endTime = CommonUtil.getFormattedDate(new Date(date.getTime() + 1500000), "HH:mm", false);
            } else {
                endTime = CommonUtil.getFormattedDate(new Date(date.getTime() + 3000000), "HH:mm", false);
            }

            tvEndTime.setText(endTime);

            if (!TextUtils.isEmpty(mTutor.getAvatar())) {
                mTutorPhotoDraweeView.setImageURI(Uri.parse(mTutor.getAvatar()));
            }
        }

        if (mBooking.getBookingType() == Booking.Type.LESSON && mLesson == null) {
            tvSelectLessonMsg.setText(getString(R.string.bo_select_lesson_supplement));
            tvSelectLessonMsg.setVisibility(View.VISIBLE);
            rlLessonInfo.setVisibility(View.GONE);
        } else if (mBooking.getBookingType() == Booking.Type.TOPIC && mTopicClass == null) {
            tvSelectLessonMsg.setText(getString(R.string.bo_select_topic));
            tvSelectLessonMsg.setVisibility(View.VISIBLE);
            rlLessonInfo.setVisibility(View.GONE);
        } else {
            rlLessonInfo.setVisibility(View.VISIBLE);
            tvSelectLessonMsg.setVisibility(View.GONE);

            if (mBooking.getBookingType() == Booking.Type.LESSON) {
                if (mLesson.isSupplement()) {
                    tvLessonNo.setText(String.format(Locale.getDefault(), getString(R.string.sp_supplement) + " %d", mLesson.getLessonNumber()));
                } else {
                    tvLessonNo.setText(String.format(Locale.getDefault(), getString(R.string.sp_lesson) + " %d", mLesson.getLessonNumber()));
                }

                tvTitle.setText(mLesson.getTitle());

                tvLevel.setText(mLesson.getLevel());

                if (!TextUtils.isEmpty(mLesson.getCoverPhoto())) {
                    mLessonPhotoDraweeView.setImageURI(Uri.parse(mLesson.getCoverPhoto()));
                }
            }

            if (mBooking.getBookingType() == Booking.Type.TOPIC) {
                Topic topic = curriculumAdapter.getTopicById(mTopicClass.getTopicId());
                tvLessonNo.setText(topic.getTitle()); //TODO

                tvTitle.setText(mTopicClass.getTitle());

                tvLevel.setText(getString(R.string.bo_topic_level, mTopicClass.getLevel()));

                if (!TextUtils.isEmpty(mTopicClass.getCoverPhoto())) {
                    mLessonPhotoDraweeView.setImageURI(Uri.parse(mTopicClass.getCoverPhoto()));
                }
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        setTitle(getString(R.string.bo_cg_title));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
    }

    @Override
    public void onResume() {
        super.onResume();

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
        getFragmentManager().beginTransaction().remove(ChangeBookingFragment.this).commit();
        getFragmentManager().popBackStack();
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_add_credit:
                goToStore();
                break;
            case R.id.rl_select_tutor:
                Intent selectTutorIntent = new Intent(getActivity(), TutorTimeSelectActivity.class);
                selectTutorIntent.putExtra(TutorTimeSelectActivity.EXTRA_BOOKING_TYPE, mBooking.getBookingType());
                startActivityForResult(selectTutorIntent, 1);
                break;
            case R.id.cv_select_lesson:
                if (mBooking.getBookingType() == Booking.Type.TOPIC) {
                    // Select class
                    Intent intent = new Intent(getActivity(), FragmentHolderActivity.class);
                    intent.putExtra(LessonPdfActivity.EXTRA_BOOKING_FRAGMENT, 2);
                    intent.putExtra(FragmentHolderActivity.EXTRA_DISPLAY_FRAGMENT,
                            SelectClassFragment.class.getSimpleName());
                    startActivityForResult(intent, 3);
                } else {
                    // Select lesson
                    Intent intent = new Intent(getActivity(), FragmentHolderActivity.class);
                    intent.putExtra(EXTRA_BOOKING_ID, bookingId);

                    if (mTutor != null) {
                        intent.putExtra(EXTRA_TUTOR_ID, mTutor.getTutorId());
                    }
                    intent.putExtra(EXTRA_START_TIME, mStartTimeFull);

                    intent.putExtra(EXTRA_BOOKING_TYPE, mBooking.getBookingType());
                    intent.putExtra(LessonPdfActivity.EXTRA_BOOKING_FRAGMENT, 2);
                    intent.putExtra(FragmentHolderActivity.EXTRA_DISPLAY_FRAGMENT,
                            SelectLessonFragment.class.getSimpleName());
                    startActivityForResult(intent, 2);
                }
                break;
            case R.id.btn_cancel_booking:
                if (isValidAll()) {
                    final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage(getString(R.string.bo_canceling));
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    mServerRequestManager.cancelBooking(mBooking.getBookingType(), mBooking.getBookingId(),
                            new ServerRequestManager.OnRequestFinishedListener() {
                                @Override
                                public void onSuccess(Object result) {
                                    progressDialog.dismiss();
                                    if (getActivity() != null) {
                                        final Dialog successDialog = new Dialog(getActivity());
                                        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                        successDialog.setContentView(R.layout.dialog_cancel_book_success);

                                        DialogItemClickListener clickListener = new DialogItemClickListener(successDialog);

                                        TextView tvMySchedule = successDialog.findViewById(R.id.tv_my_schedule);
                                        tvMySchedule.setOnClickListener(clickListener);

                                        TextView tvBookLesson = successDialog.findViewById(R.id.tv_book_lesson);
                                        tvBookLesson.setOnClickListener(clickListener);

                                        successDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialogInterface) {
                                                getFragmentManager().beginTransaction().remove(ChangeBookingFragment.this).commit();
                                                getFragmentManager().popBackStack();
                                            }
                                        });

                                        successDialog.show();
                                    }
                                }

                                @Override
                                public void onError(ServerError err) {
                                    progressDialog.dismiss();
                                    if (getActivity() != null) {
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle(getString(R.string.bo_cg_error))
                                                .setMessage(err.getMessage())
                                                .setPositiveButton(R.string.bo_cg_ok, null)
                                                .create()
                                                .show();
                                    }
                                }
                            });
                }
                break;
            case R.id.btn_change_booking:
                if (isValidAll()) {
                    final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage(getString(R.string.bo_cg_loading));
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    try {
                        String lessonTopicId = "";
                        if (mBooking.getBookingType() == Booking.Type.TOPIC) {
                            lessonTopicId = mTopicClass.getClassId();
                        }

                        if (mBooking.getBookingType() == Booking.Type.LESSON) {
                            lessonTopicId = mLesson.getLessonId();
                        }

                        mServerRequestManager.changeBooking(mBooking.getBookingType(), mBooking.getBookingId(),
                                mTutor.getTutorId(), lessonTopicId, LGCUtil.convertToUTC(mStartTimeFull),
                                new ServerRequestManager.OnRequestFinishedListener() {
                                    @Override
                                    public void onSuccess(Object result) {
                                        progressDialog.dismiss();
                                        if (getActivity() != null) {
                                            new AlertDialog.Builder(getActivity())
                                                    .setTitle(getString(R.string.bo_cg_success))
                                                    .setMessage(getString(R.string.bo_cg_success_msg))
                                                    .setPositiveButton(getString(R.string.bo_cg_success_ok), null)
                                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                        @Override
                                                        public void onDismiss(DialogInterface dialogInterface) {
                                                            getFragmentManager().beginTransaction().remove(ChangeBookingFragment.this).commit();
                                                            getFragmentManager().popBackStack();
                                                        }
                                                    })
                                                    .create()
                                                    .show();
                                        }
                                    }

                                    @Override
                                    public void onError(ServerError err) {
                                        progressDialog.dismiss();
                                        if (getActivity() != null) {
                                            new AlertDialog.Builder(getActivity())
                                                    .setTitle(getString(R.string.bo_cg_error))
                                                    .setMessage(err.getMessage())
                                                    .setPositiveButton(getString(R.string.bo_cg_ok), null)
                                                    .create()
                                                    .show();
                                        }
                                    }
                                });
                    } catch (ParseException e) {
                        Log.e(ChangeBookingFragment.class.getSimpleName(), "Cannot parse to UTC date.", e);
                    }
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    private boolean isValidAll() {
        boolean isValidAll = true;
        if (mBooking.getBookingType() == Booking.Type.LESSON && mLesson == null) {
            Toast.makeText(getActivity(), getString(R.string.bo_cg_select_lesson), Toast.LENGTH_SHORT).show();
            isValidAll = false;
        }

        if (mBooking.getBookingType() == Booking.Type.TOPIC && mTopicClass == null) {
            Toast.makeText(getActivity(), getString(R.string.bo_cg_select_topic), Toast.LENGTH_SHORT).show();
            isValidAll = false;
        }

        if (mTutor == null || TextUtils.isEmpty(mStartTimeFull)) {
            Toast.makeText(getActivity(), getString(R.string.bo_cg_select_tutor), Toast.LENGTH_SHORT).show();
            isValidAll = false;
        }

        Date bookedDate = CommonUtil.convertStringToDate(mBooking.getBookedDate(), "yyyy-MM-dd HH:mm:ss");
        if (isValidAll && (bookedDate.getTime() < new Date().getTime() + CalendarUtil.ONE_DAY_IN_MILLI)) {
            if (isValidAll && mBooking.getBookingType() == Booking.Type.LESSON) {
                double cost = mTutor.getCreditWeight() * mLesson.getCredit();
                isValidAll = checkCost(cost);
            }

            if (isValidAll && mBooking.getBookingType() == Booking.Type.TOPIC) {
                double cost = mTutor.getCreditWeight() * mTopicClass.getCredit();
                isValidAll = checkCost(cost);
            }
        }
        return isValidAll;
    }

    private boolean checkCost(double cost) {
        if (cost > mCredit) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.bo_low_credit_error))
                    .setMessage(getString(R.string.bo_low_credit_msg))
                    .setNegativeButton(getString(R.string.bo_low_credit_cancel), null)
                    .setPositiveButton(getString(R.string.bo_low_credit_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            goToStore();
                        }
                    })
                    .show();
            return false;
        }
        return true;
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
                    getFragmentManager().beginTransaction().remove(ChangeBookingFragment.this).commit();
                    getFragmentManager().popBackStack();
                    break;
                case R.id.tv_book_lesson:
                    getFragmentManager().beginTransaction().remove(ChangeBookingFragment.this).commit();
                    newFragment = new CourseTopicFragment();
                    break;
            }

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
