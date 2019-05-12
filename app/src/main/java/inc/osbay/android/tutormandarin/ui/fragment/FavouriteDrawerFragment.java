package inc.osbay.android.tutormandarin.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.artifex.mupdf.fitz.Document;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.database.TutorAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.sdk.model.Course;
import inc.osbay.android.tutormandarin.sdk.model.Lesson;
import inc.osbay.android.tutormandarin.sdk.model.Topic;
import inc.osbay.android.tutormandarin.sdk.model.TopicClass;
import inc.osbay.android.tutormandarin.sdk.model.Tutor;
import inc.osbay.android.tutormandarin.sdk.model.WhatsOn;
import inc.osbay.android.tutormandarin.ui.activity.LessonPdfActivity;
import inc.osbay.android.tutormandarin.ui.activity.MainActivity;
import inc.osbay.android.tutormandarin.util.DownloadFile;

public class FavouriteDrawerFragment extends Fragment implements View.OnClickListener {

    public static final int WHATS_ON_ID = 1;
    public static final int LESSON_ID = 2;
    public static final int CLASS_ID = 3;
    public static final int TUTOR_ID = 5;

    public static final int FAVOURITE_ID = 1;
    public static final int UNFAVOURITE_ID = 2;
    public static final int PERMISSION_WRITE_REQUEST = 123;
    ImageView mImvFavouriteEdit;
    private List<String> mCourseFavourites;
    private List<String> mTutorFavourites;
    private List<String> mWhatsOnFavourites;
    private List<String> mClassFavourites;
    private ServerRequestManager mServerRequestManager;
    private Account mAccount;
    private CurriculumAdapter mCurriculumAdapter;
    private CourseListAdapter mCourseListAdapter;
    private TutorListAdapter mTutorListAdapter;
    private WhatsOnListAdapter mWhatsOnListAdapter;
    private ClassListAdapter mClassListAdapter;
    private boolean isEdit = false;
    private String mFileName;
    private Activity mActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get account id
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String token = prefs.getString("access_token", null);
        String accountId = prefs.getString("account_id", null);

        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
            AccountAdapter accountAdapter = new AccountAdapter(getActivity());
            mAccount = accountAdapter.getAccountById(accountId);
        }

        mServerRequestManager = new ServerRequestManager(getActivity());
        mCurriculumAdapter = new CurriculumAdapter(getActivity());

        mCourseFavourites = new ArrayList<>();
        mTutorFavourites = new ArrayList<>();
        mWhatsOnFavourites = new ArrayList<>();
        mClassFavourites = new ArrayList<>();
        if (mAccount != null) {
            mCourseFavourites = mCurriculumAdapter.getFavouriteLessonId(mAccount.getAccountId());
            mTutorFavourites = mCurriculumAdapter.getFavouriteTutorId(mAccount.getAccountId());
            mWhatsOnFavourites = mCurriculumAdapter.getFavouriteWhatsOnId(mAccount.getAccountId());
            mClassFavourites = mCurriculumAdapter.getFavouriteClassId(mAccount.getAccountId());
        }

        mCourseListAdapter = new CourseListAdapter();
        mTutorListAdapter = new TutorListAdapter();
        mWhatsOnListAdapter = new WhatsOnListAdapter();
        mClassListAdapter = new ClassListAdapter();

        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorite_drawer, container, false);


        LinearLayoutManager courseLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        RecyclerView mCourseRecyclerView = rootView.findViewById(R.id.rv_favorite_courses);
        mCourseRecyclerView.setAdapter(mCourseListAdapter);
        mCourseRecyclerView.setLayoutManager(courseLayoutManager);

        LinearLayoutManager tutorLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        RecyclerView mTutorRecyclerView = rootView.findViewById(R.id.rv_favorite_tutors);
        mTutorRecyclerView.setAdapter(mTutorListAdapter);
        mTutorRecyclerView.setLayoutManager(tutorLayoutManager);

        LinearLayoutManager whatsOnLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        RecyclerView mWhatsOnRecyclerView = rootView.findViewById(R.id.rv_favorite_whats_on);
        mWhatsOnRecyclerView.setAdapter(mWhatsOnListAdapter);
        mWhatsOnRecyclerView.setLayoutManager(whatsOnLayoutManager);

        LinearLayoutManager topicLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        RecyclerView topicRecyclerView = rootView.findViewById(R.id.rv_favorite_topic);
        topicRecyclerView.setAdapter(mClassListAdapter);
        topicRecyclerView.setLayoutManager(topicLayoutManager);

        mImvFavouriteEdit = rootView.findViewById(R.id.imv_favourite_edit);
        mImvFavouriteEdit.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imv_favourite_edit:
                if (isEdit) {
                    isEdit = false;
                    mImvFavouriteEdit.setImageResource(R.drawable.ic_pencil);
                } else {
                    isEdit = true;
                    mImvFavouriteEdit.setImageResource(R.drawable.ic_favourite_edit);
                }
                mCourseListAdapter.notifyDataSetChanged();
                mTutorListAdapter.notifyDataSetChanged();
                mWhatsOnListAdapter.notifyDataSetChanged();
                mClassListAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mCourseFavourites.clear();
        mTutorFavourites.clear();
        mClassFavourites.clear();
        mWhatsOnFavourites.clear();
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

    private class CourseListAdapter extends RecyclerView.Adapter<CourseListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_favorite_course,
                    parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Lesson mLesson = mCurriculumAdapter.getLessonById(mCourseFavourites.get(position));

            if (mLesson == null) {
                mServerRequestManager.getCurriculumLessonById(mCourseFavourites.get(position), new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onError(ServerError err) {

                    }
                });
            } else {
                Course course = mCurriculumAdapter.getCourseById(mLesson.getCourseId());
                if (course != null) {
                    holder.tvCourseTitle.setText(mLesson.getTitle());
                    holder.sdvCoursePhoto.setImageURI(Uri.parse(mLesson.getCoverPhoto()));
                    if (isEdit) {
                        holder.imvCourseFavouriteCancel.setVisibility(View.VISIBLE);
                        holder.sdvCourseIcon.setVisibility(View.GONE);
                    } else {
                        holder.imvCourseFavouriteCancel.setVisibility(View.GONE);
                        holder.sdvCourseIcon.setVisibility(View.VISIBLE);
                        holder.sdvCourseIcon.setImageURI(Uri.parse(course.getCourseIcon()));
                    }
                }
            }

            holder.imvCourseFavouriteCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mLesson != null) {
                        mServerRequestManager.addToFavorite(LESSON_ID, mLesson.getLessonId(), UNFAVOURITE_ID);
                        mCurriculumAdapter.removeFavouriteById(mAccount.getAccountId(), mLesson.getLessonId(), String.valueOf(LESSON_ID));
                        mCourseFavourites.remove(holder.getAdapterPosition());
                    }
                    notifyDataSetChanged();
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null && mLesson != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (!checkPermissionForSDCard()) {
                                requestPermissionForSDCard();
                                return;
                            }
                        }

                        String lessonUrl = mLesson.getUrl();
                        mFileName = lessonUrl.substring((lessonUrl.lastIndexOf('/') + 1), lessonUrl.length());
                        String url = CommonConstant.PDF_PATH + File.separator + mFileName;

                        File file = new File(url);
                        if (file.exists()) {
                            try {
                                if (!TextUtils.isEmpty(mFileName)) {
                                    Document mDocument = Document.openDocument(url);
                                    String metaTag = mDocument.getMetaData(Document.META_INFO_TITLE);

                                    if (mDocument.countPages() > 0 && !TextUtils.isEmpty(metaTag)) {
                                        Intent intent = new Intent(getActivity(), LessonPdfActivity.class);
                                        intent.putExtra(LessonPdfActivity.FILE_NAME, mFileName);
                                        intent.putExtra(LessonPdfActivity.EXTRA_PDF_LESSON_ID, mLesson.getLessonId());
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
                                new DownloadFile(getActivity()).execute(lessonUrl, mFileName, mLesson.getLessonId());
                            } else {
                                if (getActivity() != null) {
                                    Toast.makeText(getActivity(), getString(R.string.ls_no_internet), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        ((MainActivity) getActivity()).closeDrawers();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mCourseFavourites.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView tvCourseTitle;
            SimpleDraweeView sdvCoursePhoto;
            SimpleDraweeView sdvCourseIcon;
            ImageView imvCourseFavouriteCancel;

            public ViewHolder(View itemView) {
                super(itemView);
                tvCourseTitle = itemView.findViewById(R.id.tv_course_title);
                sdvCoursePhoto = itemView.findViewById(R.id.sdv_course_photo);
                sdvCourseIcon = itemView.findViewById(R.id.sdv_course_icon);
                imvCourseFavouriteCancel = itemView.findViewById(R.id.imv_course_favourite_cancel);
            }
        }
    }

    private class TutorListAdapter extends RecyclerView.Adapter<TutorListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_favorite_tutor,
                    parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            TutorAdapter tutorAdapter = new TutorAdapter(getActivity());
            final Tutor tutor = tutorAdapter.getTutorById(mTutorFavourites.get(position));
            if (tutor == null) {
                mServerRequestManager.getTutorInfo(mTutorFavourites.get(position),
                        new ServerRequestManager.OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                notifyDataSetChanged();
                            }

                            @Override
                            public void onError(ServerError err) {
                            }
                        });
                return;
            } else {
                holder.tvTutorName.setText(tutor.getName());
                holder.tvTutorRate.setText(String.valueOf(tutor.getRate()));
                holder.sdvTutorPhoto.setImageURI(Uri.parse(tutor.getAvatar()));
                if (isEdit) {
                    holder.imvTutorFavouriteCancel.setVisibility(View.VISIBLE);
                    holder.llTutorRate.setVisibility(View.GONE);
                } else {
                    holder.imvTutorFavouriteCancel.setVisibility(View.GONE);
                    holder.llTutorRate.setVisibility(View.VISIBLE);
                }
            }

            holder.imvTutorFavouriteCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mServerRequestManager.addToFavorite(TUTOR_ID, tutor.getTutorId(), UNFAVOURITE_ID);
                    mCurriculumAdapter.removeFavouriteById(mAccount.getAccountId(), tutor.getTutorId(), String.valueOf(TUTOR_ID));
                    mTutorFavourites.remove(holder.getAdapterPosition());

                    notifyDataSetChanged();
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString(TutorInfoFragment.EXTRA_TUTOR_ID, tutor.getTutorId());

                        Fragment tutorInfoFragment = new TutorInfoFragment();
                        tutorInfoFragment.setArguments(bundle);

                        FragmentManager fm = getParentFragment().getFragmentManager();
                        Fragment fragment = fm.findFragmentById(R.id.container);
                        if (fragment == null) {
                            fm.beginTransaction()
                                    .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                            R.animator.fragment_out_new, R.animator.fragment_out_old)
                                    .addToBackStack(null)
                                    .add(R.id.container, tutorInfoFragment).commit();
                        } else {
                            fm.beginTransaction()
                                    .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                            R.animator.fragment_out_new, R.animator.fragment_out_old)
                                    .addToBackStack(null)
                                    .replace(R.id.container, tutorInfoFragment).commit();
                        }
                    }

                    ((MainActivity) getActivity()).closeDrawers();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mTutorFavourites.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView tvTutorName;
            TextView tvTutorRate;
            SimpleDraweeView sdvTutorPhoto;
            ImageView imvTutorFavouriteCancel;
            LinearLayout llTutorRate;

            public ViewHolder(View itemView) {
                super(itemView);
                tvTutorName = itemView.findViewById(R.id.tv_tutor_name);
                tvTutorRate = itemView.findViewById(R.id.tv_tutor_rate);
                sdvTutorPhoto = itemView.findViewById(R.id.sdv_tutor_photo);
                imvTutorFavouriteCancel = itemView.findViewById(R.id.imv_tutor_favourite_cancel);
                llTutorRate = itemView.findViewById(R.id.ll_tutor_rate);
            }
        }
    }

    private class WhatsOnListAdapter extends RecyclerView.Adapter<WhatsOnListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_favorite_whats_on,
                    parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final WhatsOn whatsOn = mCurriculumAdapter.getWhatsOnById(mWhatsOnFavourites.get(position));

            if (whatsOn == null) {
                mServerRequestManager.getWhatsOnById(mWhatsOnFavourites.get(position), new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onError(ServerError err) {

                    }
                });
                return;
            } else {
                holder.tvWhatsOnTitle.setText(whatsOn.getTitle());
                holder.sdvWhatsOnPhoto.setImageURI(Uri.parse(whatsOn.getCoverPhoto()));
                if (isEdit) {
                    holder.imvWhatsOnFavouriteCancel.setVisibility(View.VISIBLE);
                } else {
                    holder.imvWhatsOnFavouriteCancel.setVisibility(View.GONE);
                }
            }

            holder.imvWhatsOnFavouriteCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mServerRequestManager.addToFavorite(WHATS_ON_ID, whatsOn.getWhatsOnId(), UNFAVOURITE_ID);
                    mCurriculumAdapter.removeFavouriteById(mAccount.getAccountId(), whatsOn.getWhatsOnId(), String.valueOf(WHATS_ON_ID));
                    mWhatsOnFavourites.remove(holder.getAdapterPosition());

                    notifyDataSetChanged();
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString(WhatsOnFragment.EXTRA_WHATS_ON_ID, whatsOn.getWhatsOnId());

                        Fragment whatsOnFragment = new WhatsOnFragment();
                        whatsOnFragment.setArguments(bundle);

                        FragmentManager fm = getParentFragment().getFragmentManager();
                        Fragment fragment = fm.findFragmentById(R.id.container);
                        if (fragment == null) {
                            fm.beginTransaction()
                                    .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                            R.animator.fragment_out_new, R.animator.fragment_out_old)
                                    .addToBackStack(null)
                                    .add(R.id.container, whatsOnFragment).commit();
                        } else {
                            fm.beginTransaction()
                                    .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                            R.animator.fragment_out_new, R.animator.fragment_out_old)
                                    .addToBackStack(null)
                                    .replace(R.id.container, whatsOnFragment).commit();
                        }
                    }

                    ((MainActivity) getActivity()).closeDrawers();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mWhatsOnFavourites.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvWhatsOnTitle;
            SimpleDraweeView sdvWhatsOnPhoto;
            ImageView imvWhatsOnFavouriteCancel;

            public ViewHolder(View itemView) {
                super(itemView);
                tvWhatsOnTitle = itemView.findViewById(R.id.tv_whats_on_favorite_title);
                sdvWhatsOnPhoto = itemView.findViewById(R.id.sdv_whats_on_photo);
                imvWhatsOnFavouriteCancel = itemView.findViewById(R.id.imv_whats_on_favourite_cancel);
            }
        }
    }

    private class ClassListAdapter extends RecyclerView.Adapter<ClassListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_favorite_class,
                    parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final TopicClass topicClass = mCurriculumAdapter.getTopicClassById(mClassFavourites.get(position));

            if (topicClass == null) {
                mServerRequestManager.getCurriculumClassById(mClassFavourites.get(position), new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onError(ServerError err) {

                    }
                });
            } else {
                Topic topic = mCurriculumAdapter.getTopicById(topicClass.getTopicId());
                if (topic != null) {
                    holder.tvTopicTitle.setText(topicClass.getTitle());
                    holder.sdvTopicPhoto.setImageURI(Uri.parse(topicClass.getCoverPhoto()));
                    if (isEdit) {
                        holder.imvTopicFavouriteCancel.setVisibility(View.VISIBLE);
                        holder.sdvTopicIcon.setVisibility(View.GONE);
                    } else {
                        holder.imvTopicFavouriteCancel.setVisibility(View.GONE);
                        holder.sdvTopicIcon.setVisibility(View.VISIBLE);
                        holder.sdvTopicIcon.setImageURI(Uri.parse(topic.getmTopicIcon()));
                    }
                }
            }

            holder.imvTopicFavouriteCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (topicClass != null) {
                        mServerRequestManager.addToFavorite(CLASS_ID, topicClass.getClassId(), UNFAVOURITE_ID);
                        mCurriculumAdapter.removeFavouriteById(mAccount.getAccountId(), topicClass.getClassId(), String.valueOf(CLASS_ID));
                        mClassFavourites.remove(holder.getAdapterPosition());
                    }
                    notifyDataSetChanged();
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null && topicClass != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString(ClassFragment.EXTRA_CLASS_ID, topicClass.getClassId());

                        Fragment classFragment = new ClassFragment();
                        classFragment.setArguments(bundle);

                        FragmentManager fm = getParentFragment().getFragmentManager();
                        Fragment fragment = fm.findFragmentById(R.id.container);
                        if (fragment == null) {
                            fm.beginTransaction()
                                    .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                            R.animator.fragment_out_new, R.animator.fragment_out_old)
                                    .addToBackStack(null)
                                    .add(R.id.container, classFragment).commit();
                        } else {
                            fm.beginTransaction()
                                    .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                            R.animator.fragment_out_new, R.animator.fragment_out_old)
                                    .addToBackStack(null)
                                    .replace(R.id.container, classFragment).commit();
                        }
                    }

                    ((MainActivity) getActivity()).closeDrawers();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mClassFavourites.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView tvTopicTitle;
            SimpleDraweeView sdvTopicPhoto;
            SimpleDraweeView sdvTopicIcon;
            ImageView imvTopicFavouriteCancel;

            public ViewHolder(View itemView) {
                super(itemView);
                tvTopicTitle = itemView.findViewById(R.id.tv_topic_title);
                sdvTopicPhoto = itemView.findViewById(R.id.sdv_topic_photo);
                sdvTopicIcon = itemView.findViewById(R.id.sdv_topic_icon);
                imvTopicFavouriteCancel = itemView.findViewById(R.id.imv_topic_favourite_cancel);
            }
        }
    }
}
