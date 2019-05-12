package inc.osbay.android.tutormandarin.ui.fragment;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import java.util.Locale;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.sdk.model.Course;
import inc.osbay.android.tutormandarin.sdk.model.Lesson;
import inc.osbay.android.tutormandarin.ui.activity.LessonPdfActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class LessonListFragment extends Fragment {
    public static final int PERMISSION_WRITE_REQUEST = 123;
    private static final String COURSE_ID = "LessonListFragment.COURSE_ID";
    private static final String LESSON_TYPE = "LessonListFragment.LESSON_TYPE";
    private Course mCourse;
    private LessonListAdapter mLessonListAdapter;
    private List<Lesson> mLessons;
    private String mCourseId;
    private int mType;
    private CurriculumAdapter mCurriculumDbAdapter;
    private Activity mActivity;

    public LessonListFragment() {
        // Required empty public constructor
    }

    public static LessonListFragment newInstance(String courseId, int type) {
        LessonListFragment lessonListFragment = new LessonListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(COURSE_ID, courseId);
        bundle.putInt(LESSON_TYPE, type);
        lessonListFragment.setArguments(bundle);
        return lessonListFragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mType == 1) {
            mLessons = mCurriculumDbAdapter.getAllLessonsAndSupplements(mCourseId);
        } else if (mType == 2) {
            mLessons = mCurriculumDbAdapter.getAllLessons(mCourseId);
        } else {
            mLessons = mCurriculumDbAdapter.getAllSupplements(mCourseId);
        }

        mLessonListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.gc();
        mCourseId = getArguments().getString(COURSE_ID);
        mType = getArguments().getInt(LESSON_TYPE);
        mCurriculumDbAdapter = new CurriculumAdapter(getActivity());
        if (mCourseId != null) {
            mCourse = mCurriculumDbAdapter.getCourseById(mCourseId);
        }
        mLessonListAdapter = new LessonListAdapter();
        mLessons = new ArrayList<>();
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_lesson_list, container, false);

        RecyclerView rvLessons = (RecyclerView) v.findViewById(R.id.rv_lessons);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvLessons.setLayoutManager(layoutManager);
        rvLessons.setAdapter(mLessonListAdapter);
        return v;
    }

    private void showLimitedDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.ls_unavailable_title))
                .setMessage(getString(R.string.ls_unavailable_msg))
                .setPositiveButton(getString(R.string.ls_unavailable_ok), null)
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLessons.clear();
        mCourse = null;
        System.gc();
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

    private class LessonListAdapter extends RecyclerView.Adapter<LessonListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lesson, parent, false); //Inflating the layout

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Lesson mLesson = mLessons.get(position);

            holder.tvTitle.setText(mLesson.getTitle());

            holder.tvCredit.setText(String.format(Locale.getDefault(), "%.0f", mLesson.getCredit()));

            if (!TextUtils.isEmpty(mLesson.getCoverPhoto())) {
                holder.sdvCoverPhoto.setImageURI(Uri.parse(mLesson.getCoverPhoto()));
            }

            int total;
            if (mType == 1) {
                total = mCourse.getLessonCount() + mCourse.getSupplementCount();
            } else if (mType == 2) {
                total = mCourse.getLessonCount();
            } else {
                total = mCourse.getSupplementCount();
            }
            String content = getString(R.string.ls_lesson_no) + (holder.getAdapterPosition() + 1) + "/" + total
                    + ", " + mLesson.getVocabCount() + getString(R.string.ls_vocab_count);
            holder.tvContent.setText(content);

            if (mLesson.isFree()) {
                holder.imvLessonOverLay.setImageDrawable(null);
            } else {
                if (mLesson.isAvailable()) {
                    if (mLesson.isBought()) {
                        holder.imvLessonOverLay.setImageDrawable(null);
                    } else {
                        holder.imvLessonOverLay.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock));
                    }
                } else {
                    holder.imvLessonOverLay.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock));
                }
            }

            if (mLesson.isCompleted()) {
                holder.imvCompletedMark.setVisibility(View.VISIBLE);
            } else {
                holder.imvCompletedMark.setVisibility(View.GONE);
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

                    if (mLesson.isFree()) {
                        goToLessonFragment(mLesson, (holder.getAdapterPosition() + 1));
                    } else {
                        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
                            AccountAdapter accountAdapter = new AccountAdapter(getActivity());
                            Account account = accountAdapter.getAccountById(accountId);
                            if (account != null /*&& account.getStatus() == 4*/) {
                                if (mLesson.isAvailable()) {
                                    goToLessonFragment(mLesson, (holder.getAdapterPosition() + 1));
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

        private void goToLessonFragment(Lesson lesson, int lessonNo) {
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
                            intent.putExtra(LessonPdfActivity.EXTRA_BOOKING_FRAGMENT, 1);
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

        class ViewHolder extends RecyclerView.ViewHolder {

            SimpleDraweeView sdvCoverPhoto;
            ImageView imvCompletedMark;
            ImageView imvLessonOverLay;
            TextView tvTitle;
            TextView tvContent;
            TextView tvCredit;

            public ViewHolder(View itemView) {
                super(itemView);

                sdvCoverPhoto =  itemView.findViewById(R.id.sdv_cover_photo);
                imvLessonOverLay =  itemView.findViewById(R.id.imv_lesson_overlay);
                imvCompletedMark = itemView.findViewById(R.id.imv_completed_mark);
                tvTitle = itemView.findViewById(R.id.tv_lesson_title);
                tvContent =  itemView.findViewById(R.id.tv_lesson_content);
                tvCredit = itemView.findViewById(R.id.tv_class_credit);
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
                        intent.putExtra(LessonPdfActivity.EXTRA_BOOKING_FRAGMENT, 1);
                        startActivity(intent);
                    }
                } else {
                    if (mActivity != null) {
                        Toast.makeText(mActivity, mActivity.getString(R.string.ls_no_file), Toast.LENGTH_SHORT).show();
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
}
