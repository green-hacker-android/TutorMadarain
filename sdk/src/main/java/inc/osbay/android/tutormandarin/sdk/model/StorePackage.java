package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class StorePackage implements Serializable {

    private String mPackageId;
    private String mPackageName;
    private double mPackageCredit;

    private String mCourseId;
    private String mTitle;
    private int mLessonCount;
    private int mSupplementCount;
    private String mCoverPhoto;

    private String mThumbPhoto;

    private String mSummary;

    public StorePackage() {

    }

    public StorePackage(JSONObject json) throws JSONException {
        mPackageId = json.getString("store_package_id");
        mPackageName = json.getString("store_package_name");
        mPackageCredit = json.getDouble("amount");

        JSONObject course = json.getJSONObject("course_info");

        mCourseId = course.getString("id");
        mTitle = course.getString("title");
        mLessonCount = course.getInt("lesson");
        mSupplementCount = course.getInt("supplement");
        mCoverPhoto = course.getString("cover_photo");
        mThumbPhoto = course.getString("thumb_photo");

        mSummary = course.getString("summary");
    }

    public String getSummary() {
        return mSummary;
    }

    public String getCoverPhoto() {
        return mCoverPhoto;
    }

    public String getCourseId() {
        return mCourseId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public int getLessonCount() {
        return mLessonCount;
    }

    public int getSupplementCount() {
        return mSupplementCount;
    }

    public String getPackageId() {
        return mPackageId;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public double getPackageCredit() {
        return mPackageCredit;
    }

    public String getThumbPhoto() {
        return mThumbPhoto;
    }

}
