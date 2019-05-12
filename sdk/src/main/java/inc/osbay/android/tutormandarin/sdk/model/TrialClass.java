package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by osbay on 10/4/16.
 */
public class TrialClass {
    private String mClassId;
    private String mTitle;
    private String mArticle;
    private String mImageUrl;

    public TrialClass() {
    }

    public TrialClass(JSONObject json) throws JSONException {
        mClassId = json.getString("trial_class_id");
        mTitle = json.getString("trial_class_title");
        mArticle = json.getString("trial_class_article");
        mImageUrl = json.getString("trial_class_image");
    }

    public String getClassId() {
        return mClassId;
    }

    public void setClassId(String classId) {
        this.mClassId = classId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getArticle() {
        return mArticle;
    }

    public void setArticle(String article) {
        this.mArticle = article;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.mImageUrl = imageUrl;
    }
}
