package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import java.util.List;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Course;
import inc.osbay.android.tutormandarin.ui.activity.StudentLevelFilterActivity;

public class CourseListFragment extends Fragment {

    private static final int REQUEST_CODE = 1;
    CurriculumAdapter curriculumAdapter;
    TextView filterTxt;
    ImageView filterIcon;
    boolean selected;
    private List<Course> mCourseList = new ArrayList<>();
    private CourseListAdapter mCourseListAdapter;
    private LinearLayout filterLinearLayout;
    private ServerRequestManager mServerRequestManager;
    private boolean[] selectedLevel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.gc();
        mCourseListAdapter = new CourseListAdapter();
        curriculumAdapter = new CurriculumAdapter(getActivity());
        mServerRequestManager = new ServerRequestManager(getActivity().getApplicationContext());
        mServerRequestManager.downloadCourseList(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                mCourseList = curriculumAdapter.getAllCourses();
                mCourseListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(ServerError err) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), getString(R.string.co_download_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FlurryAgent.logEvent("Course List");
        final View rootView = inflater.inflate(R.layout.fragment_course_list, container, false);


        final RecyclerView rvTutorList = rootView.findViewById(R.id.rv_course_list);
        rvTutorList.setAdapter(mCourseListAdapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvTutorList.setLayoutManager(layoutManager);

        selectedLevel = new boolean[10];
        filterLinearLayout = rootView.findViewById(R.id.filter_ll);
        filterIcon = rootView.findViewById(R.id.filter_icon);
        filterTxt = rootView.findViewById(R.id.filter_txt);
        filterLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selected == true) {
                    Intent intent = new Intent(getContext(), StudentLevelFilterActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putBooleanArray("edit_selected", selectedLevel);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, REQUEST_CODE);
                    getActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                } else {
                    Intent intent = new Intent(getContext(), StudentLevelFilterActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                    getActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                selectedLevel = data.getBooleanArrayExtra(StudentLevelFilterActivity.EXTRA_ID);
                int count = 0;
                for (int i = 0; i < selectedLevel.length; i++) {
                    if (selectedLevel[i] == true) {
                        count++;
                    }
                }
                if (count == 0) {
                    selected = false;
                    mCourseList = curriculumAdapter.getAllCourses();
                    mCourseListAdapter.notifyDataSetChanged();
                    filterTxt.setTextColor(getContext().getColor(R.color.black));
                    filterIcon.setImageDrawable(getContext().getDrawable(R.mipmap.filter));
                } else {
                    selected = true;
                    mCourseList = curriculumAdapter.getCoursesBySelectedLevel(selectedLevel);
                    mCourseListAdapter.notifyDataSetChanged();
                    filterTxt.setTextColor(getContext().getColor(R.color.light_red));
                    filterIcon.setImageDrawable(getContext().getDrawable(R.mipmap.filter_red));
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /*@Override
    public void onStart() {
        super.onStart();
        FlurryAgent.logEvent("Course List");

        mCourseListAdapter.notifyDataSetChanged();
    }*/

    @Override
    public void onResume() {
        super.onResume();
        Log.i("Course List Fragment", "Resumed");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCourseList.clear();
        System.gc();
    }

    private class CourseListAdapter extends RecyclerView.Adapter<CourseListAdapter.ViewHolder> {

        CourseListAdapter() {

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false); //Inflating the layout

            return new ViewHolder(v); // Returning the created object
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Course course = mCourseList.get(position);

            String courseTitle = course.getTitle();


            holder.tvCourseTitle.setText(changeCourseTitleLocale(course.getTitle()));

            String content = course.getLessonCount() + getString(R.string.co_lst_lesson_count) + course.getSupplementCount()
                    + getString(R.string.co_lst_supplement_count);
            holder.tvContent.setText(content);

            if (!TextUtils.isEmpty(course.getCourseIcon())) {
                holder.sdvCourseIcon.setImageURI(Uri.parse(course.getCourseIcon()));
            }

            if (!TextUtils.isEmpty(course.getCoverPhoto())) {
                holder.sdvCoverPhoto.setImageURI(Uri.parse(course.getCoverPhoto()));
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment courseFragment = new CourseFragment();

                    Bundle bundle = new Bundle();
                    bundle.putString(CourseFragment.EXTRA_COURSE_ID, course.getCourseId());
                    courseFragment.setArguments(bundle);

                    FragmentManager fm = getParentFragment().getFragmentManager();
                    Fragment fragment = fm.findFragmentById(R.id.container);
                    if (fragment == null) {
                        fm.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                .addToBackStack(null)
                                .add(R.id.container, courseFragment).commit();
                    } else {
                        fm.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                .addToBackStack(null)
                                .replace(R.id.container, courseFragment).commit();
                    }
                }
            });
        }

        private String changeCourseTitleLocale(String title) {
            String courseTitleLocale;
            switch (title) {
                case "Pinyin":
                    courseTitleLocale = getString(R.string.co_locale_pinyin_1);
                    break;
                case "Beginner 1":
                    courseTitleLocale = getString(R.string.co_locale_beginner_1);
                    break;
                case "Beginner 2":
                    courseTitleLocale = getString(R.string.co_locale_beginner_2);
                    break;
                case "Intermediate 1":
                    courseTitleLocale = getString(R.string.co_locale_intermediate_1);
                    break;
                case "Intermediate 2":
                    courseTitleLocale = getString(R.string.co_locale_intermediate_2);
                    break;
                case "Advanced 1":
                    courseTitleLocale = getString(R.string.co_locale_advanced_1);
                    break;
                case "Advanced 2":
                    courseTitleLocale = getString(R.string.co_locale_advanced_2);
                    break;
                default:
                    courseTitleLocale = title;
            }
            return courseTitleLocale;
        }

        @Override
        public int getItemCount() {
            return mCourseList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private SimpleDraweeView sdvCourseIcon;
            private SimpleDraweeView sdvCoverPhoto;
            private TextView tvCourseTitle;
            private TextView tvContent;

            public ViewHolder(View itemView) {
                super(itemView);

                sdvCourseIcon = itemView.findViewById(R.id.sdv_course_icon);
                sdvCoverPhoto = itemView.findViewById(R.id.sdv_cover_photo);
                tvCourseTitle = itemView.findViewById(R.id.tv_course_title);
                tvContent = itemView.findViewById(R.id.tv_course_content);
            }
        }
    }
}
