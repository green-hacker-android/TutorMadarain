package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Video {
    private String mVideoId;
    private String mTitle;
    private String mVideoTopicName;
    // format mm:ss
    private String mLength;
    private int mViewCount;
    private String mYoutubeId;
    private String mAuthor;
    private String mDescription;

    public Video() {
    }

    public Video(JSONObject jsonObject) throws JSONException {
        mVideoId = jsonObject.getString("video_id");
        mTitle = jsonObject.getString("video_title");
        mVideoTopicName = jsonObject.getString("video_playlist_topic");
        mLength = jsonObject.getString("video_length");
        mYoutubeId = jsonObject.getString("video_youtube_id");
        mAuthor = jsonObject.getString("video_creator");
        mDescription = jsonObject.getString("video_description");
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getVideoId() {
        return mVideoId;
    }

    public void setVideoId(String videoId) {
        mVideoId = videoId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTopicName() {
        return mVideoTopicName;
    }

    public void setVideoTopicName(String videoTopicName) {
        mVideoTopicName = videoTopicName;
    }

    public String getLength() {
        return mLength;
    }

    public void setLength(String length) {
        mLength = length;
    }

    public int getViewCount() {
        return mViewCount;
    }

    public void setViewCount(int viewCount) {
        mViewCount = viewCount;
    }

    public String getYoutubeId() {
        return mYoutubeId;
    }

    public void setYoutubeId(String youtubeId) {
        mYoutubeId = youtubeId;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }
}
