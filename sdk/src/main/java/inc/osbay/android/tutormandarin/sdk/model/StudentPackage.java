package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;

import inc.osbay.android.tutormandarin.sdk.util.LGCUtil;

public class StudentPackage implements Serializable {

    private String mPackageId;
    private String mPackageName;
    private String mCourseId;
    private int mPackageType;
    private String mPackageLevel;
    private double mPackageAmount;
    private String mPackageCreateDate;
    private int mSectionCount;
    private int mLessonCount;
    private int mSupplementCount;
    private String mActiveExpireDate;
    private String mPackageExpireDate;
    private String mActiveDate;
    private String mPurchasedDate;
    private String mFinishedDate;
    private String mCoverPhoto;

    public String getThumbPhoto() {
        return mThumbPhoto;
    }

    public void setThumbPhoto(String mThumbPhoto) {
        this.mThumbPhoto = mThumbPhoto;
    }

    private String mThumbPhoto;
    private int mFinishedLessonCount;
    private int mStatus;

    public StudentPackage() {

    }

    public StudentPackage(JSONObject json) throws JSONException, ParseException {
        mPackageId = json.getString("package_id");
        mPackageName = json.getString("package_name");
        mCourseId = json.getString("curriculum_course_id");
        mPackageType = json.getInt("package_type");
        mPackageLevel = json.getString("package_level");
        mPackageAmount = json.getDouble("package_amount");
        mPackageCreateDate = LGCUtil.convertToUTC(json.getString("package_create_date"));
        mSectionCount = json.getInt("section");
        mLessonCount = json.getInt("lesson");
        mSupplementCount = json.getInt("supplement_count");
        mActiveExpireDate = LGCUtil.convertToUTC(json.getString("active_expired_date"));
        mPackageExpireDate = LGCUtil.convertToUTC(json.getString("packaged_expired_date"));
        mActiveDate = LGCUtil.convertToUTC(json.getString("active_date"));
        mPurchasedDate = LGCUtil.convertToUTC(json.getString("purchased_date"));
        mFinishedDate = LGCUtil.convertToUTC(json.getString("finished_date"));
        mCoverPhoto = json.getString("cover_photo");
        mThumbPhoto = json.getString("thumb_photo");
        mFinishedLessonCount = json.getInt("finish_lesson");
        mStatus = json.getInt("package_status");
    }

    public String getCourseId() {
        return mCourseId;
    }

    public void setCourseId(String courseId) {
        mCourseId = courseId;
    }

    public String getPackageId() {
        return mPackageId;
    }

    public void setPackageId(String packageId) {
        mPackageId = packageId;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
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

    public String getActiveExpireDate() {
        return mActiveExpireDate;
    }

    public void setActiveExpireDate(String activeExpireDate) {
        mActiveExpireDate = activeExpireDate;
    }

    public String getPackageExpireDate() {
        return mPackageExpireDate;
    }

    public void setPackageExpireDate(String packageExpireDate) {
        mPackageExpireDate = packageExpireDate;
    }

    public int getFinishedLessonCount() {
        return mFinishedLessonCount;
    }

    public void setFinishedLessonCount(int finishedLessonCount) {
        mFinishedLessonCount = finishedLessonCount;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public int getPackageType() {
        return mPackageType;
    }

    public void setPackageType(int mPackageType) {
        this.mPackageType = mPackageType;
    }

    public String getPackageLevel() {
        return mPackageLevel;
    }

    public void setPackageLevel(String mPackageDescription) {
        this.mPackageLevel = mPackageDescription;
    }

    public double getPackageAmount() {
        return mPackageAmount;
    }

    public void setPackageAmount(double mPackageAmount) {
        this.mPackageAmount = mPackageAmount;
    }

    public String getPackageCreateDate() {
        return mPackageCreateDate;
    }

    public void setPackageCreateDate(String mPackageCreateDate) {
        this.mPackageCreateDate = mPackageCreateDate;
    }

    public int getSectionCount() {
        return mSectionCount;
    }

    public void setSectionCount(int mSectionCount) {
        this.mSectionCount = mSectionCount;
    }

    public String getActiveDate() {
        return mActiveDate;
    }

    public void setActiveDate(String mActiveDate) {
        this.mActiveDate = mActiveDate;
    }

    public String getPurchasedDate() {
        return mPurchasedDate;
    }

    public void setPurchasedDate(String mPurchasedDate) {
        this.mPurchasedDate = mPurchasedDate;
    }

    public String getFinishedDate() {
        return mFinishedDate;
    }

    public void setFinishedDate(String mFinishedDate) {
        this.mFinishedDate = mFinishedDate;
    }

    public String getCoverPhoto() {
        return mCoverPhoto;
    }

    public void setCoverPhoto(String mCoverPhoto) {
        this.mCoverPhoto = mCoverPhoto;
    }

    public static class Status {
        public static final int BOUGHT = 1;
        public static final int ACTIVE = 2;
        public static final int ACTIVE_EXPIRED = 3;
        public static final int PACKAGE_EXPIRED = 4;
    }
}
