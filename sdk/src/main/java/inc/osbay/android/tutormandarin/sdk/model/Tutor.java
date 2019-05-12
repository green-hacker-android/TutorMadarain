package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Tutor implements Serializable {
    private String mTutorId;
    private String mName;
    private float mRate;
    private String mTeachingExp;
    private double mCreditWeight;
    private String mIntroVoice;
    private String mAvatar;
    private String mIntroText;
    private int mLike;
    private String mTopics;
    private String mLocation;

    public Tutor() {
    }

    public Tutor(JSONObject json) throws JSONException {
        mTutorId = json.getString("tutor_id");
        mName = json.getString("tutor_name");
        mRate = json.getInt("tutor_rate");
        mTeachingExp = json.getString("tutor_experience");
        mCreditWeight = json.getDouble("tutor_credit_weight");
        mIntroVoice = json.getString("tutor_intro_voice");
        mAvatar = json.getString("tutor_avatar");
        mLike = json.getInt("tutor_like");
        mIntroText = json.getString("tutor_intro_text");
        mTopics = json.getString("tutor_topics");
        mLocation = json.getString("tutor_location");
    }

    public String getIntroVoice() {
        return mIntroVoice;
    }

    public void setIntroVoice(String introVoice) {
        mIntroVoice = introVoice;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public void setAvatar(String avatar) {
        mAvatar = avatar;
    }

    public int getLike() {
        return mLike;
    }

    public boolean isLiked() {
        return (mLike == 1);
    }

    public void setLike(int like) {
        mLike = like;
    }

    public String getTopics() {
        return mTopics;
    }

    public void setTopics(String topics) {
        mTopics = topics;
    }

    public String getIntroText() {
        return mIntroText;
    }

    public void setIntroText(String introText) {
        mIntroText = introText;
    }

//    public Tutor(String mId, String mTutorName, String mTutorRate, String mTeachingExp) {
//        this.mId = mId;
//        this.mTutorName = mTutorName;
//        this.mTutorRate = mTutorRate;
//        this.mTeachingExp = mTeachingExp;
//    }

    public String getTutorId() {
        return mTutorId;
    }

    public void setTutorId(String tutorId) {
        mTutorId = tutorId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public float getRate() {
        return mRate;
    }

    public void setRate(float rate) {
        mRate = rate;
    }

    public String getTeachingExp() {
        return mTeachingExp;
    }

    public void setTeachingExp(String teachingExp) {
        mTeachingExp = teachingExp;
    }

    public double getCreditWeight() {
        return mCreditWeight;
    }

    public void setCreditWeight(double creditWeight) {
        mCreditWeight = creditWeight;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String mLocation) {
        this.mLocation = mLocation;
    }
}

