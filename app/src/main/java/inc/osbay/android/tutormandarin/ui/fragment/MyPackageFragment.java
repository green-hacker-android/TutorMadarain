package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Lesson;
import inc.osbay.android.tutormandarin.sdk.model.StudentPackage;
import inc.osbay.android.tutormandarin.ui.activity.PackageTutorialActivity;

public class MyPackageFragment extends BackHandledFragment implements View.OnClickListener {
    public static final String EXTRA_PACKAGE = "MyPackageFragment.EXTRA_PACKAGE";
    public static final String PREF_TUTORIAL = "MyPackageFragment.PREF_TUTORIAL";

    private static final int MENU_ITEM_TUTORIAL = 1;

    private StudentPackage mStudentPackage;

    private List<Integer> mSectionCounts;

    private CurriculumAdapter curriculumAdapter;

    private SectionListAdapter mSectionListAdapter;

    public MyPackageFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        mSectionCounts = new ArrayList<>();
        curriculumAdapter = new CurriculumAdapter(getActivity().getApplicationContext());

        if (bundle != null) {
            mStudentPackage = (StudentPackage) bundle.getSerializable(EXTRA_PACKAGE);
            mSectionCounts = curriculumAdapter.getBookingSectionNumber(mStudentPackage.getCourseId());
        }

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREF_TUTORIAL, Context.MODE_PRIVATE);
        boolean b = sharedPreferences.getBoolean(PREF_TUTORIAL, false);
        if (!b) {
            sharedPreferences.edit().putBoolean(PREF_TUTORIAL, true).apply();
            Intent intent = new Intent(getActivity(), PackageTutorialActivity.class);
            startActivity(intent);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        setTitle(getString(R.string.pk_title));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_package, container, false);

        Toolbar toolBar;
        toolBar = rootView.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#E45F56"));
        setSupportActionBar(toolBar);

        TextView tvCourseBookingPackageName = rootView.findViewById(R.id.tv_course_booking_package_name);
        TextView tvBookingLessonCount = rootView.findViewById(R.id.tv_booking_lesson_count);
        TextView tvBookingSupplementCount = rootView.findViewById(R.id.tv_booking_supplement_count);
        TextView tvBookingFinishedLessons = rootView.findViewById(R.id.tv_booking_finished_lessons);
        TextView tvBookingTotalLesson = rootView.findViewById(R.id.tv_booking_total_lesson);
        TextView tvBookingCourseExpiredDate = rootView.findViewById(R.id.tv_booking_course_expired_date);

        ProgressBar pbAchievementLesson = rootView.findViewById(R.id.pb_achievement_lesson);

        SimpleDraweeView sdvCoverPhoto = rootView.findViewById(R.id.sdv_cover_photo);
        if (!TextUtils.isEmpty(mStudentPackage.getThumbPhoto())) {
            sdvCoverPhoto.setImageURI(Uri.parse(mStudentPackage.getThumbPhoto()));
        }

        if (mStudentPackage != null) {
            tvCourseBookingPackageName.setText(mStudentPackage.getPackageName());
            tvBookingLessonCount.setText(String.valueOf(mStudentPackage.getLessonCount()));
            tvBookingSupplementCount.setText(String.valueOf(mStudentPackage.getSupplementCount()));
            tvBookingFinishedLessons.setText(String.valueOf(mStudentPackage.getFinishedLessonCount()));
            tvBookingTotalLesson.setText(String.valueOf((mStudentPackage.getLessonCount() + mStudentPackage.getSupplementCount())));
            if (mStudentPackage.getStatus() == StudentPackage.Status.BOUGHT) {
                tvBookingCourseExpiredDate.setText(getString(R.string.pk_booking_expired_date, mStudentPackage.getPackageExpireDate()));
            } else if (mStudentPackage.getStatus() == StudentPackage.Status.ACTIVE) {
                tvBookingCourseExpiredDate.setText(getString(R.string.pk_booking_expired_date, mStudentPackage.getActiveExpireDate()));
            }
            pbAchievementLesson.setMax(mStudentPackage.getLessonCount() + mStudentPackage.getSupplementCount());
            pbAchievementLesson.setProgress(mStudentPackage.getFinishedLessonCount());
        }

        Button btnBooking = rootView.findViewById(R.id.btn_booking);
        btnBooking.setOnClickListener(this);

        RecyclerView rvSectionList = rootView.findViewById(R.id.rv_section_list);
        mSectionListAdapter = new SectionListAdapter();
        rvSectionList.setAdapter(mSectionListAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        rvSectionList.setLayoutManager(llm);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        ServerRequestManager serverRequestManager = new ServerRequestManager(getActivity());
        serverRequestManager.downloadLessonList(mStudentPackage.getCourseId(), new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                mSectionCounts = curriculumAdapter.getBookingSectionNumber(mStudentPackage.getCourseId());
                mSectionListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(ServerError err) {

            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.add(0, MENU_ITEM_TUTORIAL, Menu.NONE, "").setIcon(R.drawable.question_mark)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case MENU_ITEM_TUTORIAL:
                Intent intent = new Intent(getActivity(), PackageTutorialActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_booking:
                Bundle bundle = new Bundle();
                Fragment newFragment;
                if (mStudentPackage.getStatus() == StudentPackage.Status.BOUGHT) {
                    newFragment = new BookingPackageFragment();
                    bundle.putSerializable(BookingPackageFragment.EXTRA_PACKAGE, mStudentPackage);
                } else {
                    newFragment = new CurrentPlanFragment();
                    bundle.putSerializable(CurrentPlanFragment.EXTRA_PACKAGE, mStudentPackage);
                }

                FragmentManager frgMgr = getFragmentManager();
                Fragment frg = frgMgr.findFragmentById(R.id.container);

                newFragment.setArguments(bundle);

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
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStack();
        return false;
    }

    private class SectionListAdapter extends RecyclerView.Adapter<SectionListAdapter.SectionHolder> {

        @Override
        public SectionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_section_list, parent, false);
            return new SectionHolder(v);
        }

        @Override
        public void onBindViewHolder(SectionHolder holder, int position) {
            holder.tvSection.setText(getString(R.string.pk_section_title, String.valueOf(mSectionCounts.get(position))));
            LinearLayoutManager llBookingLessons = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            holder.rvBookingLessons.setLayoutManager(llBookingLessons);

            LessonListAdapter mLessonListAdapter = new LessonListAdapter(mSectionCounts.get(position));
            holder.rvBookingLessons.setAdapter(mLessonListAdapter);
        }

        @Override
        public int getItemCount() {
            return mSectionCounts.size();
        }

        class SectionHolder extends RecyclerView.ViewHolder {
            RecyclerView rvBookingLessons;
            TextView tvSection;

            public SectionHolder(View itemView) {
                super(itemView);
                tvSection = itemView.findViewById(R.id.tv_section_title);

                rvBookingLessons = itemView.findViewById(R.id.rv_booking_lessons);
            }
        }
    }

    private class LessonListAdapter extends RecyclerView.Adapter<LessonListAdapter.LessonHolder> {
        List<Lesson> mSectionLessons;

        public LessonListAdapter(int sectionNumber) {
            CurriculumAdapter curriculumAdapter = new CurriculumAdapter(getActivity());
            mSectionLessons = curriculumAdapter.getLessonBySectionNumber(mStudentPackage.getCourseId(), sectionNumber);
        }

        @Override
        public LessonHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_section_lesson_list,
                    parent, false);
            return new LessonHolder(v);
        }

        @Override
        public void onBindViewHolder(LessonHolder holder, int position) {
            if (mSectionLessons.get(position).getIsSupplement() == 1) {
                holder.tvLessonName.setText(getString(R.string.pk_lesson_name, getString(R.string.pk_supplement_number), ""));
                holder.imgvIsSupplement.setImageResource(R.drawable.ic_supplement);
            } else {
                holder.tvLessonName.setText(getString(R.string.pk_lesson_name, getString(R.string.pk_lesson_number), String.valueOf(mSectionLessons.get(position).getLessonNumber())));
                holder.imgvIsSupplement.setImageResource(R.drawable.ic_lesson_black);
            }
            holder.tvLessonMins.setText(getString(R.string.pk_lesson_mins, String.valueOf(mSectionLessons.get(position).getDuration())));
        }

        @Override
        public int getItemCount() {
            return mSectionLessons.size();
        }

        class LessonHolder extends RecyclerView.ViewHolder {
            TextView tvLessonName;
            TextView tvLessonMins;
            ImageView imgvIsSupplement;

            public LessonHolder(View itemView) {
                super(itemView);
                tvLessonName = itemView.findViewById(R.id.tv_lesson_name);
                tvLessonMins = itemView.findViewById(R.id.tv_lesson_mins);
                imgvIsSupplement = itemView.findViewById(R.id.imgv_is_supplement);
            }
        }
    }

}
