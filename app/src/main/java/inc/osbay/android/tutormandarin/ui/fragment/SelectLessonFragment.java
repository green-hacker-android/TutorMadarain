package inc.osbay.android.tutormandarin.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.artifex.mupdf.fitz.Document;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.sdk.model.Course;
import inc.osbay.android.tutormandarin.sdk.model.Lesson;
import inc.osbay.android.tutormandarin.ui.activity.LessonPdfActivity;

public class SelectLessonFragment extends BackHandledFragment {

    public static final int PERMISSION_WRITE_REQUEST = 123;
    CurriculumAdapter curriculumAdapter;
    private List<Course> mCourseList;
    private Dialog mSelectCourseDialog;
    private TextView mSelectCourseTextView;
    private Course mSelectedCourse;
    private LessonListAdapter mLessonListAdapter;
    private List<Lesson> mLessons;
    private String mTutorId;
    private String mStartTime;
    private int mBookingType;
    private int mCourseIdPosition;
    private int mBookingFragmentType;
    private String mBookingId;
    private Activity mActivity;

    @Override
    public boolean onBackPressed() {
        getActivity().finish();
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showActionBar();
        setTitle(getString(R.string.ls_select_course_title));

        curriculumAdapter = new CurriculumAdapter(getActivity());
        mCourseList = curriculumAdapter.getAllCourses();
        mLessons = new ArrayList<>();

        mLessonListAdapter = new LessonListAdapter();

        mSelectCourseDialog = new Dialog(getActivity());
        mSelectCourseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        ListView rvCourseList = new ListView(getActivity());
        rvCourseList.setAdapter(new SelectLevelAdapter());
        mSelectCourseDialog.setContentView(rvCourseList);

        Bundle bundle = getArguments();
        if (bundle != null) {
            int frgType = bundle.getInt(LessonPdfActivity.EXTRA_BOOKING_FRAGMENT, 0);
            if (frgType == 1) {
                mTutorId = bundle.getString(MyBookingFragment.EXTRA_TUTOR_ID);
                mStartTime = bundle.getString(MyBookingFragment.EXTRA_START_TIME);
                mBookingType = bundle.getInt(MyBookingFragment.EXTRA_BOOKING_TYPE, 0);
            } else {
                mTutorId = bundle.getString(ChangeBookingFragment.EXTRA_TUTOR_ID);
                mStartTime = bundle.getString(ChangeBookingFragment.EXTRA_START_TIME);
                mBookingId = bundle.getString(ChangeBookingFragment.EXTRA_BOOKING_ID);
                mBookingType = bundle.getInt(ChangeBookingFragment.EXTRA_BOOKING_TYPE, 0);
            }
            mBookingFragmentType = bundle.getInt(LessonPdfActivity.EXTRA_BOOKING_FRAGMENT, 0);
        }

        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_select_lesson, container, false);

        mSelectCourseTextView = rootView.findViewById(R.id.tv_select_course);

        if (mSelectedCourse != null) {
            mSelectCourseTextView.setText(mSelectedCourse.getTitle());
        }

        mSelectCourseTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCourseList.size() != 0) {
                    mSelectCourseDialog.show();
                }
            }
        });

        RecyclerView lessonRecyclerView = rootView.findViewById(R.id.rv_lesson_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        lessonRecyclerView.setLayoutManager(layoutManager);
        lessonRecyclerView.setAdapter(mLessonListAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        setDisplayHomeAsUpEnable(true);

        final ServerRequestManager serverRequestManager = new ServerRequestManager(getActivity());
        serverRequestManager.downloadCourseList(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                //mCourseList = (List<Course>) result;
                mCourseList = curriculumAdapter.getAllCourses();
                mSelectedCourse = mCourseList.get(mCourseIdPosition);
                serverRequestManager.downloadLessonList(mSelectedCourse.getCourseId(), new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        mLessons = curriculumAdapter.getAllLessonsAndSupplements(mSelectedCourse.getCourseId());
                        if (mLessons.isEmpty()) {
                            mSelectCourseTextView.setText(getString(R.string.ls_select_course));
                        } else {
                            mSelectCourseTextView.setText(changeCourseTitleLocale(mSelectedCourse.getTitle()));
                        }
                        mLessonListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(ServerError err) {

                    }
                });
            }

            @Override
            public void onError(ServerError err) {

            }
        });

        if (mCourseList.size() > 0) {
            mSelectedCourse = mCourseList.get(0);
            mSelectCourseTextView.setText(changeCourseTitleLocale(mSelectedCourse.getTitle()));
            mLessons = curriculumAdapter.getAllLessonsAndSupplements(mSelectedCourse.getCourseId());
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private boolean checkPermissionForSDCard() {
        int resultReadSD = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int resultWriteSD = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (resultReadSD == PackageManager.PERMISSION_GRANTED) && (resultWriteSD == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissionForSDCard() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    getActivity());

            alertDialogBuilder.setTitle("Permission");
            alertDialogBuilder.setMessage("Need to check storage permission in App System Setting");
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_REQUEST);
        }
    }

    private void showLimitedDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.ls_unavailable_title))
                .setMessage(getString(R.string.ls_unavailable_msg))
                .setPositiveButton(getString(R.string.ls_unavailable_ok), null)
                .show();
    }

    private void goToLessonFragment(Lesson lesson) {
        String lessonUrl = lesson.getUrl();
        String fileName = lessonUrl.substring((lessonUrl.lastIndexOf('/') + 1), lessonUrl.length());
        String url = CommonConstant.PDF_PATH + File.separator + fileName;

        File file = new File(url);
        if (file.exists()) {
            try {
                if (!TextUtils.isEmpty(fileName)) {
                    Document mDocument = Document.openDocument(url);
                    String metaTag = mDocument.getMetaData(Document.META_INFO_TITLE);

                    if (mDocument.countPages() > 0 && !TextUtils.isEmpty(metaTag)) {
                        Intent intent = new Intent(getActivity(), LessonPdfActivity.class);
                        intent.putExtra(LessonPdfActivity.FILE_NAME, fileName);
                        intent.putExtra(LessonPdfActivity.EXTRA_PDF_LESSON_ID, lesson.getLessonId());
                        if (mBookingFragmentType == 1) {
                            intent.putExtra(MyBookingFragment.EXTRA_TUTOR_ID, mTutorId);
                            intent.putExtra(MyBookingFragment.EXTRA_START_TIME, mStartTime);
                            intent.putExtra(MyBookingFragment.EXTRA_BOOKING_TYPE, mBookingType);
                        } else if (mBookingFragmentType == 2) {
                            intent.putExtra(ChangeBookingFragment.EXTRA_TUTOR_ID, mTutorId);
                            intent.putExtra(ChangeBookingFragment.EXTRA_START_TIME, mStartTime);
                            intent.putExtra(ChangeBookingFragment.EXTRA_BOOKING_TYPE, mBookingType);
                            intent.putExtra(ChangeBookingFragment.EXTRA_BOOKING_ID, mBookingId);
                        }
                        intent.putExtra(LessonPdfActivity.EXTRA_BOOKING_FRAGMENT, mBookingFragmentType);
                        startActivity(intent);
                    } else {
                        if (file.exists()) {
                            boolean isDeleted = file.delete();
                            if (isDeleted) {
                                Toast.makeText(mActivity, mActivity.getString(R.string.ls_file_incorrect), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), getString(R.string.ls_no_file), Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                if (file.exists()) {
                    boolean isDeleted = file.delete();
                    if (isDeleted) {
                        Toast.makeText(mActivity, mActivity.getString(R.string.ls_file_incorrect), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else {
            if (isNetworkConnected()) {
                new DownloadFile().execute(lessonUrl, fileName, lesson.getLessonId());
            } else {
                Toast.makeText(getActivity(), getString(R.string.ls_no_internet), Toast.LENGTH_SHORT).show();
            }
        }

    }

    private class SelectLevelAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mCourseList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.item_whats_on_topic, viewGroup, false);
            }

            final Course course = mCourseList.get(i);

            TextView tvTitle = view.findViewById(R.id.tv_select_topic);
            tvTitle.setText(changeCourseTitleLocale(course.getTitle()));
            tvTitle.setGravity(Gravity.CENTER);

            ImageView imvSelect = view.findViewById(R.id.imv_selected_mark);
            imvSelect.setVisibility(View.GONE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectCourseDialog.dismiss();
                    mCourseIdPosition = i;

                    mSelectedCourse = course;

                    mLessons = curriculumAdapter.getAllLessonsAndSupplements(mSelectedCourse.getCourseId());

                    final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage(getString(R.string.ls_lst_download_loading));
                    progressDialog.setCancelable(false);

                    if (mLessons.size() == 0) {
                        progressDialog.show();
                    }

                    ServerRequestManager serverRequestManager = new ServerRequestManager(getActivity());
                    serverRequestManager.downloadLessonList(mSelectedCourse.getCourseId(), new ServerRequestManager.OnRequestFinishedListener() {
                        @Override
                        public void onSuccess(Object result) {
                            progressDialog.dismiss();

                            if (getActivity() != null) {
                                mLessons = curriculumAdapter.getAllLessonsAndSupplements(mSelectedCourse.getCourseId());
                                mSelectCourseTextView.setText(changeCourseTitleLocale(mSelectedCourse.getTitle()));
                                mLessonListAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onError(ServerError err) {
                            progressDialog.dismiss();

                            if (getActivity() != null) {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(getString(R.string.ls_lst_download_error))
                                        .setMessage(err.getMessage())
                                        .setPositiveButton(getString(R.string.ls_lst_download_ok), null)
                                        .create()
                                        .show();
                            }
                        }
                    });

                    mSelectCourseTextView.setText(changeCourseTitleLocale(mSelectedCourse.getTitle()));
                    mLessonListAdapter.notifyDataSetChanged();
                }
            });

            return view;
        }
    }

    private class LessonListAdapter extends RecyclerView.Adapter<LessonListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lesson, parent, false); //Inflating the layout

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Lesson lesson = mLessons.get(position);

            holder.tvTitle.setText(lesson.getTitle());

            holder.tvCredit.setText(String.format("%.0f", lesson.getCredit()));

            String content;
            if (lesson.isSupplement()) {
                holder.imvCompletedMark.setVisibility(View.VISIBLE);

                content = getString(R.string.ls_select_supplement_count) + lesson.getLessonNumber() + "/"
                        + mSelectedCourse.getSupplementCount() + ", " + lesson.getVocabCount() + getString(R.string.ls_select_vocab_count)
                        + lesson.getGrammarCount() + getString(R.string.ls_select_grammar_count);
            } else {
                holder.imvCompletedMark.setVisibility(View.GONE);

                content = getString(R.string.ls_select_lesson_count) + lesson.getLessonNumber() + "/" + mSelectedCourse.getLessonCount()
                        + ", " + lesson.getVocabCount() + getString(R.string.ls_select_vocab_count) + lesson.getGrammarCount()
                        + getString(R.string.ls_select_grammar_count);
            }
            holder.tvContent.setText(content);

            if (lesson.isFree()) {
                holder.imvLessonOverlay.setImageDrawable(null);
            } else {
                if (lesson.isAvailable()) {
                    if (lesson.isBought()) {
                        holder.imvLessonOverlay.setImageDrawable(null);
                    } else {
                        holder.imvLessonOverlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock));
                    }
                } else {
                    holder.imvLessonOverlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock));
                }
            }

            if (lesson.isCompleted()) {
                holder.imvCompletedMark.setVisibility(View.VISIBLE);
            } else {
                holder.imvCompletedMark.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(lesson.getCoverPhoto())) {
                holder.sdvLessonPhoto.setImageURI(Uri.parse(lesson.getCoverPhoto()));
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (!checkPermissionForSDCard()) {
                            requestPermissionForSDCard();
                            return;
                        }
                    }
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String token = prefs.getString("access_token", null);
                    String accountId = prefs.getString("account_id", null);

                    if (lesson.isFree()) {
                        goToLessonFragment(lesson);
                    } else {
                        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
                            AccountAdapter accountAdapter = new AccountAdapter(getActivity());
                            Account account = accountAdapter.getAccountById(accountId);
                            if (account != null /*&& account.getStatus() == 4*/) {
                                if (lesson.isAvailable()) {
                                    goToLessonFragment(lesson);
                                } else {
                                    new AlertDialog.Builder(getActivity())
                                            .setTitle(getString(R.string.ls_paid_user_title))
                                            .setMessage(getString(R.string.ls_paid_user_msg))
                                            .setPositiveButton(getString(R.string.ls_paid_user_ok), null)
                                            .show();
                                }
                            } else {
                                showLimitedDialog();
                            }
                        } else {
                            showLimitedDialog();
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mLessons.size(); // the number of items in the list will be +1 the titles including the header vGreenBar.
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            ImageView imvCompletedMark;
            ImageView imvLessonOverlay;
            TextView tvTitle;
            TextView tvContent;
            TextView tvCredit;
            SimpleDraweeView sdvLessonPhoto;

            public ViewHolder(View itemView) {
                super(itemView);

                imvCompletedMark = itemView.findViewById(R.id.imv_completed_mark);
                imvLessonOverlay = itemView.findViewById(R.id.imv_lesson_overlay);
                tvTitle = itemView.findViewById(R.id.tv_lesson_title);
                tvContent = itemView.findViewById(R.id.tv_lesson_content);
                tvCredit = itemView.findViewById(R.id.tv_class_credit);
                sdvLessonPhoto = itemView.findViewById(R.id.sdv_cover_photo);
            }
        }
    }

    private class DownloadFile extends AsyncTask<String, Void, Void> {

        ProgressDialog mDialog = new ProgressDialog(getActivity());
        String mLessonId;
        String fileUrl;
        String fileName;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Downloading....");
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            fileUrl = strings[0];
            fileName = strings[1];
            mLessonId = strings[2];

            File folder = new File(CommonConstant.PDF_PATH);

            if (!folder.exists()) {
                folder.mkdirs();
            }

            File pdfFile = new File(folder, fileName);

            try {
                pdfFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileDownloader fileDownloader = new FileDownloader();
            fileDownloader.downloadFile(fileUrl, pdfFile);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mDialog.dismiss();
            String url = CommonConstant.PDF_PATH + File.separator + fileName;
            File file = new File(url);
            try {
                if (!TextUtils.isEmpty(fileName)) {
                    Document mDocument = Document.openDocument(url);

                    if (mDocument.countPages() > 0) {
                        Intent intent = new Intent(getActivity(), LessonPdfActivity.class);
                        intent.putExtra(LessonPdfActivity.FILE_NAME, fileName);
                        intent.putExtra(LessonPdfActivity.EXTRA_PDF_LESSON_ID, mLessonId);
                        if (mBookingFragmentType == 1) {
                            intent.putExtra(MyBookingFragment.EXTRA_TUTOR_ID, mTutorId);
                            intent.putExtra(MyBookingFragment.EXTRA_START_TIME, mStartTime);
                            intent.putExtra(MyBookingFragment.EXTRA_BOOKING_TYPE, mBookingType);
                        } else if (mBookingFragmentType == 2) {
                            intent.putExtra(ChangeBookingFragment.EXTRA_TUTOR_ID, mTutorId);
                            intent.putExtra(ChangeBookingFragment.EXTRA_START_TIME, mStartTime);
                            intent.putExtra(ChangeBookingFragment.EXTRA_BOOKING_TYPE, mBookingType);
                            intent.putExtra(ChangeBookingFragment.EXTRA_BOOKING_ID, mBookingId);
                        }
                        intent.putExtra(LessonPdfActivity.EXTRA_BOOKING_FRAGMENT, mBookingFragmentType);
                        startActivity(intent);
                    }
                } else {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), getString(R.string.ls_no_file), Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                if (file.exists()) {
                    boolean isDeleted = file.delete();
                    if (isDeleted) {
                        Toast.makeText(mActivity, mActivity.getString(R.string.ls_file_incorrect), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private class FileDownloader {
        private static final int MEGABYTE = 1024 * 1024;

        void downloadFile(String fileUrl, File directory) {
            try {

                URL url = new URL(fileUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                //urlConnection.setRequestMethod("GET");
                //urlConnection.setDoOutput(true);
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(directory);
                int totalSize = urlConnection.getContentLength();

                byte[] buffer = new byte[MEGABYTE];
                int bufferLength = 0;
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, bufferLength);
                }
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
}
