package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Topic {
    private String mTopicId;
    private String mTitle;
    private String mLevel;
    private String mPhotoUrl;
    private String mDescription;
    private String mTopicIcon;
    private String mClassCount;

    public Topic() {
    }

    public Topic(JSONObject jsonObject) throws JSONException {
        mTopicId = jsonObject.getString("topic_id");
        mTitle = jsonObject.getString("topic_title");
        mLevel = jsonObject.getString("level");
        mPhotoUrl = jsonObject.getString("photo_url");
        mDescription = jsonObject.getString("description");
        mTopicIcon = jsonObject.getString("icon");
        mClassCount = jsonObject.getString("class_count");
    }

    public String getTopicId() {
        return mTopicId;
    }

    public void setTopicId(String topicId) {
        mTopicId = topicId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String topicTitle) {
        mTitle = topicTitle;
    }

    public String getLevel() {
        return mLevel;
    }

    public void setLevel(String level) {
        mLevel = level;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        mPhotoUrl = photoUrl;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getmClassCount() {
        return mClassCount;
    }

    public void setmClassCount(String mClassCount) {
        this.mClassCount = mClassCount;
    }

    public String getmTopicIcon() {
        return mTopicIcon;
    }

    public void setmTopicIcon(String mTopicIcon) {
        this.mTopicIcon = mTopicIcon;
    }
}
