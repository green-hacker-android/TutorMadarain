package inc.osbay.android.tutormandarin.sdk.model;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import inc.osbay.android.tutormandarin.sdk.util.LGCUtil;

public class Account {
    private String mAccountId;
    private String mFirstName;
    private String mLastName;
    private String mPhoneCode;
    private String mPhoneNumber;
    private String mEmail;
    private int mStatus;
    private String mGender;
    private String mAvatar;
    private String mTimeZone;
    private String mCity;
    private String mCountry;
    private String mChineseLevel;
    private String mInterestIn;
    private String mOccupation;
    private String mLearningGoal;
    private String mPronunciation;
    private String mSpeakingLang;
    private String mSpeaking;
    private String mListening;
    private double mCredit;
    private String mUpdateDate;

    public Account() {
    }

    public Account(JSONObject json) throws JSONException, ParseException {
        mAccountId = json.getString("student_id");
        mFirstName = json.getString("first_name");
        mLastName = json.getString("last_name");
        mEmail = json.getString("email");
        mPhoneCode = json.getString("ph_code");
        mPhoneNumber = json.getString("ph_number");
        mGender = LGCUtil.getGenderName(json.getInt("gender"));
        mCountry = json.getString("country_name");
        mCity = json.getString("city_name");
        mTimeZone = json.getString("time_zone");
        mChineseLevel = json.getString("chinese_level");
        mInterestIn = json.getString("interest_in");
        mCredit = json.getDouble("credit");
        mStatus = json.getInt("status");
        mAvatar = json.getString("avatar");
        mOccupation = json.getString("occupation");
        mLearningGoal = json.getString("learning_goal");
        mPronunciation = json.getString("pronounciation");
        mSpeaking = json.getString("speaking");
        mSpeakingLang = json.getString("speakinglang");
        mListening = json.getString("listening");
        mUpdateDate = LGCUtil.convertToUTC(json.getString("update_date"));
    }

    public String getAccountId() {
        return mAccountId;
    }

    public void setAccountId(String accountId) {
        mAccountId = accountId;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        mLastName = lastName;
    }

    public String getPhoneCode() {
        return mPhoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        mPhoneCode = phoneCode;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public String getGender() {
        return mGender;
    }

    public void setGender(String gender) {
        mGender = gender;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public void setAvatar(String avatar) {
        mAvatar = avatar;
    }

    public String getTimeZone() {
        return mTimeZone;
    }

    public void setTimeZone(String timeZone) {
        mTimeZone = timeZone;
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String city) {
        mCity = city;
    }

    public String getCountry() {
        return mCountry;
    }

    public void setCountry(String country) {
        mCountry = country;
    }

    public String getChineseLevel() {
        return mChineseLevel;
    }

    public void setChineseLevel(String chineseLevel) {
        mChineseLevel = chineseLevel;
    }

    public String getInterestIn() {
        return mInterestIn;
    }

    public void setInterestIn(String interestIn) {
        mInterestIn = interestIn;
    }

    public String getOccupation() {
        return mOccupation;
    }

    public void setOccupation(String occupation) {
        mOccupation = occupation;
    }

    public String getLearningGoal() {
        return mLearningGoal;
    }

    public void setLearningGoal(String learningGoal) {
        mLearningGoal = learningGoal;
    }

    public String getPronunciation() {
        return mPronunciation;
    }

    public void setPronunciation(String pronunciation) {
        mPronunciation = pronunciation;
    }

    public String getSpeaking() {
        return mSpeaking;
    }

    public void setSpeaking(String speaking) {
        mSpeaking = speaking;
    }

    public String getListening() {
        return mListening;
    }

    public void setListening(String listening) {
        mListening = listening;
    }

    public double getCredit() {
        return mCredit;
    }

    public void setCredit(double credit) {
        mCredit = credit;
    }

    public String getUpdateDate() {
        return mUpdateDate;
    }

    public void setUpdateDate(String updateDate) {
        mUpdateDate = updateDate;
    }

    public String getFullName() {
        return mFirstName + " " + mLastName;
    }

    public String getSpeakingLang() {
        return mSpeakingLang;
    }

    public void setSpeakingLang(String speakingLang) {
        mSpeakingLang = speakingLang;
    }

    public String getLocation() {
        if (!TextUtils.isEmpty(mCity) && !TextUtils.isEmpty(mCountry) && !TextUtils.isEmpty(mTimeZone)) {
            return mCity + ", " + mCountry + "\n" + mTimeZone;
        } else {
            return "";
        }
    }

    public static class Status {
        public static final int INACTIVE = 1;
        public static final int REQUEST = 2;
        public static final int TRIAL = 3;
        public static final int ACTIVE = 4;
        public static final int REQUEST_TRIAL = 5;
        public static final int NO_TRIAL_ACTIVE = 6;
        public static final int TRIAL_NOT_SHOW = 7;
    }
}
