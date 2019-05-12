package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Lesson implements Serializable {

    private String mLessonId;
    private int mLessonNumber;
    private String mTitle;
    private int mSectionNumber;
    private String mCourseId;
    private int mVocabCount;
    private int mGrammarCount;
    private double mCredit;
    private int mIsSupplement;
    private String mCoverPhoto;
    // 1 - not bought
    // 2 - bought
    private int mIsBought;
    // 0 - not completed
    // 1 - completed
    private int mIsCompleted;
    private String mLevel;
    // 1 - free
    // 2 - not free
    private int mIsFree;
    private int mDuration;
    // 1 - available
    // 2 - not available
    // 3 - available with lock
    private int mIsAvailable;
    private String mUrl;

    public Lesson() {
    }

    public Lesson(JSONObject json) throws JSONException {
        mLessonId = json.getString("lesson_id");
        mLessonNumber = json.getInt("lesson_no");
        mTitle = json.getString("title");
        mSectionNumber = json.getInt("section_no");
        mCourseId = json.getString("course_id");
        mVocabCount = json.getInt("vocab_count");
        mGrammarCount = json.getInt("grammar_count");
        mCredit = json.getDouble("credit");
        mIsSupplement = json.getInt("is_supplement");
        mCoverPhoto = json.getString("cover_photo");
        mIsBought = json.getInt("is_bought");
        mLevel = json.getString("level");
        mIsFree = json.getInt("lesson_type");
        mDuration = json.getInt("duration");

        try {
            mIsAvailable = json.getInt("available");
            mIsCompleted = json.getInt("is_completed");
        } catch (JSONException e) {
            // ignore
        }

        mUrl = json.getString("lesson_url");
    }

    public boolean isAvailable() {
        return mIsAvailable == 1;
    }

    public boolean isBought() {
        return (mIsBought == 1);
    }

    public boolean isFree() {
        return (mIsFree == 1);
    }

    public int getIsAvalilable() {
        return this.mIsAvailable;
    }

    public void setIsAvailable(int isAvailable) {
        this.mIsAvailable = isAvailable;
    }

    public int getIsFree() {
        return mIsFree;
    }

    public void setIsFree(int isFree) {
        mIsFree = isFree;
    }

    public String getLevel() {
        return mLevel;
    }

    public void setLevel(String level) {
        mLevel = level;
    }

    public int getIsSupplement() {
        return mIsSupplement;
    }

    public void setIsSupplement(int isSupplement) {
        mIsSupplement = isSupplement;
    }

    public int getIsBought() {
        return mIsBought;
    }

    public void setIsBought(int isBought) {
        mIsBought = isBought;
    }

    public int getIsCompleted() {
        return mIsCompleted;
    }

    public void setIsCompleted(int isCompleted) {
        mIsCompleted = isCompleted;
    }

    public String getLessonId() {
        return mLessonId;
    }

    public void setLessonId(String lessonId) {
        mLessonId = lessonId;
    }

    public int getLessonNumber() {
        return mLessonNumber;
    }

    public void setLessonNumber(int lessonNumber) {
        mLessonNumber = lessonNumber;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public int getSectionNumber() {
        return mSectionNumber;
    }

    public void setSectionNumber(int sectionNumber) {
        mSectionNumber = sectionNumber;
    }

    public String getCourseId() {
        return mCourseId;
    }

    public void setCourseId(String courseId) {
        mCourseId = courseId;
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

    public double getCredit() {
        return mCredit;
    }

    public void setCredit(double credit) {
        mCredit = credit;
    }

    public boolean isSupplement() {
        return (mIsSupplement == 1);
    }

    public boolean isCompleted() {
        return (mIsCompleted == 1);
    }

    public String getCoverPhoto() {
        return mCoverPhoto;
    }

    public void setCoverPhoto(String coverPhoto) {
        mCoverPhoto = coverPhoto;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }
}
