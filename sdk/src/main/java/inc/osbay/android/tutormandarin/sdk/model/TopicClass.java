package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class TopicClass implements Serializable {

    private String mClassId;
    private String mTitle;
    private String mTopicId;
    private double mCredit;
    private String mCoverPhoto;
    private int mIsCompleted;
    private String mLevel;
    private int mDuration;
    private int mIsAvailable;
    // 1 bought
    // 2 no bought
    private int mIsBought;
    private String mUrl;
    private int mVocabCount;
    private int mGrammarCount;

    public TopicClass() {
    }

    public TopicClass(JSONObject json) throws JSONException {
        mClassId = json.getString("class_id");
        mTitle = json.getString("class_title");
        mTopicId = json.getString("topic_id");
        mCredit = json.getDouble("credit");
//        mVocabCount = json.getInt("vocab_count");
//        mGrammarCount = json.getInt("grammar_count");
        mCoverPhoto = json.getString("cover_photo");
        mLevel = json.getString("level");
        mDuration = json.getInt("duration");
        mIsBought = json.getInt("is_bought");

        try {
            mIsAvailable = json.getInt("available");
            mIsCompleted = json.getInt("is_completed");
        } catch (JSONException e) {
            // ignore
        }

        mUrl = json.getString("class_url");
    }

    public boolean isAvailable() {
        return mIsAvailable == 1;
    }

    public boolean isBought() {
        return mIsBought == 1;
    }

    public int getIsBought() {
        return mIsBought;
    }

    public void setIsBought(int isBought) {
        mIsBought = isBought;
    }

    public int getIsAvalilable() {
        return this.mIsAvailable;
    }

    public void setIsAvailable(int isAvailable) {
        this.mIsAvailable = isAvailable;
    }

    public String getLevel() {
        return mLevel;
    }

    public void setLevel(String level) {
        mLevel = level;
    }

    public int getIsCompleted() {
        return mIsCompleted;
    }

    public void setIsCompleted(int isCompleted) {
        mIsCompleted = isCompleted;
    }

    public String getClassId() {
        return mClassId;
    }

    public void setClassId(String lessonId) {
        mClassId = lessonId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTopicId() {
        return mTopicId;
    }

    public void setTopicId(String courseId) {
        mTopicId = courseId;
    }

    public double getCredit() {
        return mCredit;
    }

    public void setCredit(double credit) {
        mCredit = credit;
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

}
