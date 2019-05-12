package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Course implements Serializable {
    private String mCourseId;
    private String mTitle;
    private String mDescription;
    private int mLessonCount;
    private int mSupplementCount;
    private int mVocabCount;
    private int mGrammarCount;
    private int hsk;
    private double mTotalTime;
    private double mCredit;
    private String mCourseIcon;
    private String mCoverPhoto;

    public Course(){}

    public Course(JSONObject json) throws JSONException {
        mCourseId = json.getString("id");
        mTitle = json.getString("title");
        mDescription = json.getString("description");
        mLessonCount = json.getInt("lesson");
        mSupplementCount = json.getInt("supplement");
        hsk = json.getInt("hsk");
        mVocabCount = json.getInt("vocabulary");
        mGrammarCount = json.getInt("grammar");
        mTotalTime = json.getDouble("total_time");
        mCredit = json.getDouble("credit");
        mCourseIcon = json.getString("icon");
        mCoverPhoto = json.getString("cover_photo");
    }

    public String getCourseId() {
        return mCourseId;
    }

    public void setCourseId(String courseId) {
        mCourseId = courseId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getPackageDescription() {
        return mDescription;
    }

    public void setPackageDescription(String description) {
        mDescription = description;
    }

    public int getLessonCount() {
        return mLessonCount;
    }

    public void setLessonCount(int lessonCount) {
        mLessonCount = lessonCount;
    }

    public int getSupplementCount() {
        return mSupplementCount;
    }

    public void setSupplementCount(int supplementCount) {
        mSupplementCount = supplementCount;
    }

    public int getVocabCount() {
        return mVocabCount;
    }

    public void setVocabCount(int vocabCount) {
        mVocabCount = vocabCount;
    }

    public int getGrammarCount() {
        return mGrammarCount;
    }

    public void setGrammarCount(int grammarCount) {
        mGrammarCount = grammarCount;
    }

    public double getTotalTime() {
        return mTotalTime;
    }

    public void setTotalTime(double totalTime) {
        mTotalTime = totalTime;
    }

    public double getCredit() {
        return mCredit;
    }

    public void setCredit(double credit) {
        mCredit = credit;
    }

    public String getCourseIcon() {
        return mCourseIcon;
    }

    public void setCourseIcon(String courseIcon) {
        mCourseIcon = courseIcon;
    }

    public String getCoverPhoto() {
        return mCoverPhoto;
    }

    public void setCoverPhoto(String coverPhoto) {
        mCoverPhoto = coverPhoto;
    }

    public int getHsk() {
        return hsk;
    }

    public void setHsk(int hsk) {
        this.hsk = hsk;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }
}
