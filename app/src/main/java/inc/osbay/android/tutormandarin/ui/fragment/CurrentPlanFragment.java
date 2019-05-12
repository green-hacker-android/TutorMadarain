package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.TutorAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Booking;
import inc.osbay.android.tutormandarin.sdk.model.StudentPackage;
import inc.osbay.android.tutormandarin.sdk.model.Tutor;
import inc.osbay.android.tutormandarin.sdk.util.LGCUtil;
import inc.osbay.android.tutormandarin.ui.activity.TutorTimeSelectActivity;
import inc.osbay.android.tutormandarin.util.CommonUtil;

public class CurrentPlanFragment extends BackHandledFragment implements View.OnClickListener {
    public static final String EXTRA_PACKAGE = "CurrentPlanFragment.EXTRA_PACKAGE";

    private ServerRequestManager mServerRequestManager;
    private StudentPackage mStudentPackage;

    private ProgressDialog progressDialog;
    private List<Map<String, String>> mCurrentPlanSchedules;
    private ScheduleListAdapter mSchedulesRVAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mStudentPackage = (StudentPackage) bundle.getSerializable(EXTRA_PACKAGE);
        }

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.cp_loading));
        progressDialog.setCancelable(false);

        mCurrentPlanSchedules = new ArrayList<>();

        mServerRequestManager = new ServerRequestManager(getActivity().getApplicationContext());

        progressDialog.show();
        loadCurrentPlans();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_current_plan, container, false);

        Toolbar toolBar;
        toolBar = (Toolbar) rootView.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#FBB03B"));
        setSupportActionBar(toolBar);

        RecyclerView rvCurrentPlans = (RecyclerView) rootView.findViewById(R.id.rv_current_schedules);

        mSchedulesRVAdapter = new ScheduleListAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        rvCurrentPlans.setLayoutManager(layoutManager);
        rvCurrentPlans.setAdapter(mSchedulesRVAdapter);

        rootView.findViewById(R.id.fab_add_schedule).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        setTitle(getString(R.string.cp_title));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
    }

    private void loadCurrentPlans() {
        mServerRequestManager.getCurrentPlans(mStudentPackage.getPackageId(),
                new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        progressDialog.dismiss();

                        if (getActivity() != null) {
                            mCurrentPlanSchedules = (List<Map<String, String>>) result;

                            if (mCurrentPlanSchedules.size() > 0) {
                                mSchedulesRVAdapter.notifyDataSetChanged();
                            } else {
                                goToPackageBooking();
                            }
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        progressDialog.dismiss();

                        if (getActivity() != null) {
                            if (err.getErrorCode() == ServerError.Code.NO_CURRENT_PLAN) {
                                goToPackageBooking();
                            } else {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(getString(R.string.cp_error))
                                        .setMessage(err.getMessage())
                                        .setPositiveButton(getString(R.string.cp_error_ok), null)
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
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_add_schedule:
                Intent intent = new Intent(getActivity(),
                        TutorTimeSelectActivity.class);
                intent.putExtra(TutorTimeSelectActivity.EXTRA_BOOKING_TYPE, Booking.Type.PACKAGE);
                startActivityForResult(intent, 0);
                //TODO
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String tutorId = data.getStringExtra("EXTRA_TUTOR_ID");
            String startTime = data.getStringExtra("EXTRA_START_TIME");

            String schedule;
            try {
                schedule = LGCUtil.convertToUTC(startTime) + "|" + tutorId;
            } catch (ParseException e) {
                e.printStackTrace();
                return;
            }
            //TODO submit new schedule info to server

            progressDialog.show();
            mServerRequestManager.bookCourse(mStudentPackage.getCourseId(), schedule,
                    new ServerRequestManager.OnRequestFinishedListener() {
                        @Override
                        public void onSuccess(Object result) {

                            if (getActivity() != null) {
                                loadCurrentPlans();
                            }
                        }

                        @Override
                        public void onError(ServerError err) {
                            progressDialog.dismiss();

                            if (getActivity() != null) {
                                new android.app.AlertDialog.Builder(getActivity())
                                        .setTitle(getString(R.string.cp_error))
                                        .setMessage(err.getMessage())
                                        .setPositiveButton(getString(R.string.cp_error_ok), null)
                                        .create()
                                        .show();
                            }
                        }
                    });
        }
    }

    public boolean goToPackageBooking() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(BookingPackageFragment.EXTRA_PACKAGE, mStudentPackage);

        FragmentManager frgMgr = getFragmentManager();
        Fragment frg = frgMgr.findFragmentById(R.id.container);
        Fragment bookPackageFragment = new BookingPackageFragment();
        bookPackageFragment.setArguments(bundle);

        if (frg == null) {
            frgMgr.beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.container, bookPackageFragment).commit();
        } else {
            frgMgr.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.container, bookPackageFragment).commit();
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStack();
        return false;
    }

    private class ScheduleListAdapter extends RecyclerView.Adapter<ScheduleListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_current_plan,
                    parent, false);
            return new ViewHolder(v);
        }

        public Map<String, String> getItem(int position) {
            return mCurrentPlanSchedules.get(position);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            Map<String, String> itemData = getItem(position);
            String tutorId = itemData.get("tutor_id");
            String startTimeFull = itemData.get("start_time_full");
            Date startTimeDate = CommonUtil.convertStringToDate(startTimeFull, "yyyy-MM-dd HH:mm:ss");

            holder.tvScheduleDate.setText(CommonUtil.getFormattedDate(startTimeDate, "MMM dd, yyyy", false));

            holder.tvStartTime.setText(CommonUtil.getFormattedDate(startTimeDate, "HH:mm", false));
            holder.tvEndTime.setText(CommonUtil.getFormattedDate(
                    new Date(startTimeDate.getTime() + 50 * 60 * 1000), "HH:mm", false));

            TutorAdapter tutorAdapter = new TutorAdapter(getActivity());
            Tutor tutor = tutorAdapter.getTutorById(tutorId);
            if (tutor != null && !TextUtils.isEmpty(tutor.getAvatar())) {
                holder.sdvTutorPhoto.setImageURI(Uri.parse(tutor.getAvatar()));
            }

        }


        @Override
        public int getItemCount() {
            return mCurrentPlanSchedules.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView tvStartTime;
            TextView tvEndTime;
            TextView tvScheduleDate;
            LinearLayout llScheduleTime;
            SimpleDraweeView sdvTutorPhoto;
            ImageView imvDelete;

            public ViewHolder(View itemView) {
                super(itemView);

                tvStartTime = (TextView) itemView.findViewById(R.id.tv_start_time);
                tvEndTime = (TextView) itemView.findViewById(R.id.tv_end_time);
                tvScheduleDate = (TextView) itemView.findViewById(R.id.tv_schedule_date);
                llScheduleTime = (LinearLayout) itemView.findViewById(R.id.ll_schedule_time);

                sdvTutorPhoto = (SimpleDraweeView) itemView.findViewById(R.id.sdv_tutor_photo);
                imvDelete = (ImageView) itemView.findViewById(R.id.imv_delete_schedule);

            }
        }
    }
}
