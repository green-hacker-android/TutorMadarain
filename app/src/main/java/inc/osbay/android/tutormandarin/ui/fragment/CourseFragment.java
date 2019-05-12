package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.Locale;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Course;
import inc.osbay.android.tutormandarin.ui.view.FragmentStatePagerAdapter;

public class CourseFragment extends BackHandledFragment {

    public static final String EXTRA_COURSE_ID = "CourseFragment.EXTRA_COURSE_ID";
    private Course mCourse;
    private ServerRequestManager mServerRequestManager;
    private ViewPager mVpLessonPager;
    private TabLayout mTabLessonLayout;
    private CurriculumAdapter mCurriculumDbAdapter;
    private LessonPagerAdapter mLessonPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.gc();
        mCurriculumDbAdapter = new CurriculumAdapter(getActivity());
        mServerRequestManager = new ServerRequestManager(getActivity());

        Bundle bundle = getArguments();
        if (bundle != null) {
            mCourse = mCurriculumDbAdapter.getCourseById(bundle.getString(EXTRA_COURSE_ID));
        }

        if (mCourse == null) {
            onBackPressed();
            return;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_course, container, false);

        Toolbar toolBar;
        toolBar = rootView.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        setSupportActionBar(toolBar);

        SimpleDraweeView sdvCoverPhoto = rootView.findViewById(R.id.sdv_cover_photo);
        if (!TextUtils.isEmpty(mCourse.getCoverPhoto())) {
            sdvCoverPhoto.setImageURI(Uri.parse(mCourse.getCoverPhoto()));
        }

        TextView tvVocabCount = rootView.findViewById(R.id.tv_vocab_count);
        tvVocabCount.setText(String.valueOf(mCourse.getVocabCount()));

        TextView tvGrammarCount = rootView.findViewById(R.id.tv_grammar_count);
        tvGrammarCount.setText(String.valueOf(mCourse.getGrammarCount()));

        TextView tvTotalTime = rootView.findViewById(R.id.tv_total_time);
        tvTotalTime.setText(String.format(Locale.getDefault(), "%s hr", Math.round(mCourse.getTotalTime() * 10) / 10.0));

        TextView tvCredit = rootView.findViewById(R.id.tv_course_credit);
        tvCredit.setText(String.format(Locale.getDefault(), "%.0f", mCourse.getCredit()));

        TextView tvDescription = rootView.findViewById(R.id.tv_description);
        tvDescription.setMovementMethod(new ScrollingMovementMethod());
        tvDescription.setText(Html.fromHtml(changeCourseDescLocale(mCourse.getPackageDescription())));

        mTabLessonLayout = rootView.findViewById(R.id.tl_lesson_tabs);
        mVpLessonPager = rootView.findViewById(R.id.vp_lesson_pager);

        String content = getString(R.string.co_content) + mCourse.getLessonCount() + getString(R.string.co_lesson_count) + mCourse.getSupplementCount()
                + getString(R.string.co_supplement_count);

        TextView tvContent = rootView.findViewById(R.id.tv_course_content);
        tvContent.setText(content);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(getActivity()!=null)
        setTitle(changeCourseTitleLocale(mCourse.getTitle()));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return onBackPressed();
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStack();
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.co_dialog_lesson_loading));

        if (mCurriculumDbAdapter.getAllLessonsAndSupplements(mCourse.getCourseId()).size() == 0) {
            progressDialog.show();
        }

        mLessonPagerAdapter = new LessonPagerAdapter(getChildFragmentManager());
        mTabLessonLayout.setupWithViewPager(mVpLessonPager);
        mVpLessonPager.setAdapter(mLessonPagerAdapter);

        mServerRequestManager.downloadLessonList(mCourse.getCourseId(),
                new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        progressDialog.dismiss();

                        if (getActivity() != null) {
                            mLessonPagerAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        progressDialog.dismiss();

                        if ((getActivity() != null)) {
                            if (err.getErrorCode() == 10067) {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(getString(R.string.co_available_soon_title))
                                        .setMessage(getString(R.string.co_available_soon_msg))
                                        .setPositiveButton(getString(R.string.co_available_soon_ok), null)
                                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialogInterface) {
                                                dialogInterface.dismiss();
                                                onBackPressed();
                                            }
                                        })
                                        .create().show();
                            } else {
                                Toast.makeText(getActivity(), err.getErrorCode() + " - " + err.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private class LessonPagerAdapter extends FragmentStatePagerAdapter {

        LessonPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return LessonListFragment.newInstance(mCourse.getCourseId(), 1);
                case 1:
                    return LessonListFragment.newInstance(mCourse.getCourseId(), 2);
                case 2:
                    return LessonListFragment.newInstance(mCourse.getCourseId(), 3);
                default:
                    return LessonListFragment.newInstance(mCourse.getCourseId(), 1);
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.co_tab_all);
                case 1:
                    return getString(R.string.co_tab_lessons);
                case 2:
                    return getString(R.string.co_tab_supplements);
            }
            return super.getPageTitle(position);
        }
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

    private String changeCourseDescLocale(String description) {
        String courseDescLocale;
        switch (description) {
            case "\r\n\u003cp\u003e\r\nSpeaking Chinese starts with Pinyin. Learn how the correct pronunciation fundamentals of Mandarin.\r\n\u003c/p\u003e\r\n":
                courseDescLocale = getString(R.string.co_locale_pinyin_desc);
                break;
            case "\u003cp\u003e\r\nDevelop a strong foundation in Chinese with particular focus on practical speaking and listening.\r\n\u003c/p\u003e":
                courseDescLocale = getString(R.string.co_locale_beginner_1_desc);
                break;
            case "\u003cp\u003e\r\nStrengthen your basic spoken Chinese with fundamental grammar, vocabulary, and more.\r\n\u003c/p\u003e":
                courseDescLocale = getString(R.string.co_locale_beginner_2_desc);
                break;
            case "\u003cp\u003e\r\nLearn more specialized Chinese grammar patterns, phrases, as well as vocabulary relevant to your interests.\r\n\u003c/p\u003e":
                courseDescLocale = getString(R.string.co_locale_intermediate_1_desc);
                break;
            case "\u003cp\u003e\r\nBecome confident with daily conversations and learn more advanced sentence structures based on practical topics.\r\n\u003c/p\u003e":
                courseDescLocale = getString(R.string.co_locale_intermediate_2_desc);
                break;
            case "Advanced 1":
                courseDescLocale = getString(R.string.co_locale_advanced_1);
                break;
            case "Advanced 2":
                courseDescLocale = getString(R.string.co_locale_advanced_2);
                break;
            default:
                courseDescLocale = description;
        }
        return courseDescLocale;
    }
}
