package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.Fragment;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.sdk.model.StudentPackage;
import inc.osbay.android.tutormandarin.util.CommonUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class ResumeStatusFragment extends Fragment {
    AccountAdapter accountAdapter;
    private Account mAccount;
    private StudentPackage mStudentPackage;

    public ResumeStatusFragment() {
        // Required empty public constructor
    }

    public String replaceNoteString(String inputValue) {

        inputValue = inputValue.replace(" ", "");
        inputValue = inputValue.replace("Beginner", "B");
        inputValue = inputValue.replace("Intermediate", "I");
        inputValue = inputValue.replace("Advanced", "A");

        return inputValue;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        accountAdapter = new AccountAdapter(getActivity());
        String accountId = mPreferences.getString("account_id", "1");
        mAccount = accountAdapter.getAccountById(accountId);

        mStudentPackage = accountAdapter.getStudentPackageLevel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_resume_status, container, false);
        TextView mTvResumeStudentName = v.findViewById(R.id.tv_resume_student_name);
        TextView mTvResumeUpdateDate = v.findViewById(R.id.tv_resume_update_date);
        TextView mTvResumeLearningGoal = v.findViewById(R.id.tv_resume_learning_goal);
        TextView mTvResumeLevel = v.findViewById(R.id.tv_resume_level);
        TextView mTvResumeProgress = v.findViewById(R.id.tv_resume_progress);
        TextView mTvResumePronunciation = v.findViewById(R.id.tv_resume_pronunciation);
        TextView mTvResumeSpeaking = v.findViewById(R.id.tv_resume_speaking);
        TextView mTvResumeListening = v.findViewById(R.id.tv_resume_listening);
        SimpleDraweeView mSdvResumeProfilePhoto = v.findViewById(R.id.sdv_resume_profile_photo);

        if (mAccount != null) {

            if (!TextUtils.isEmpty(mAccount.getAvatar())) {
                mSdvResumeProfilePhoto.setImageURI(Uri.parse(mAccount.getAvatar()));
            }
            mTvResumeStudentName.setText(mAccount.getFullName());
            mTvResumeUpdateDate.setText(getString(R.string.re_update_date, CommonUtil.getCustomDateResult(mAccount.getUpdateDate(), "yyyy-MM-dd HH:mm:ss", "MMM dd, yyyy HH:mm")));
            mTvResumeLearningGoal.setText(mAccount.getLearningGoal());
            mTvResumePronunciation.setText(mAccount.getPronunciation());
            mTvResumeListening.setText(mAccount.getListening());
            mTvResumeSpeaking.setText(mAccount.getSpeaking());

            if (TextUtils.isEmpty(mAccount.getPronunciation())) {
                mTvResumePronunciation.setVisibility(View.GONE);
            } else {
                mTvResumePronunciation.setVisibility(View.VISIBLE);
            }

            if (TextUtils.isEmpty(mAccount.getSpeaking())) {
                mTvResumeSpeaking.setVisibility(View.GONE);
            } else {
                mTvResumeSpeaking.setVisibility(View.VISIBLE);
            }

            if (TextUtils.isEmpty(mAccount.getListening())) {
                mTvResumeListening.setVisibility(View.GONE);
            } else {
                mTvResumeListening.setVisibility(View.VISIBLE);
            }

            mTvResumeLevel.setText(replaceNoteString(mAccount.getChineseLevel()));
        } else {
            mTvResumeLevel.setText("-");
        }

        if (mStudentPackage != null) {
            mTvResumeProgress.setText(getString(R.string.re_lessons_complete, String.valueOf(mStudentPackage.getFinishedLessonCount()), String.valueOf(mStudentPackage.getLessonCount() + mStudentPackage.getSupplementCount())));
        } else {
            mTvResumeProgress.setText("-");
        }
        return v;
    }

}
