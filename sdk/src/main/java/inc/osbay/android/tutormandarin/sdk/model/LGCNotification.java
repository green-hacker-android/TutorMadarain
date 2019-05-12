package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Locale;

import inc.osbay.android.tutormandarin.sdk.util.LGCUtil;

public class LGCNotification {

    private String mNotiId;

    private String mType;

    private String mLevel;

    private String mCategory;

    private String mContent;

    private String mSendDate;

    // 0. unread
    // 1. read
    // 2. delete
    private int mStatus; //status

    public LGCNotification() {
    }

    public LGCNotification(JSONObject json) throws JSONException, ParseException {
        mNotiId = json.getString("id");
        mType = json.getString("notification_type");
        mCategory = json.getString("notification_category");
        mLevel = json.getString("priority_level");
        mStatus = json.getInt("status");
        mSendDate = LGCUtil.convertToLocale(json.getString("send_date"));
        try {
            JSONObject msgObj = new JSONObject(json.getString("content"));

            mContent = String.format(Locale.getDefault(),
                    msgObj.getString("message_template"),
                    LGCUtil.convertToLocale(msgObj.getString("utc_date_time").replace("UTC|", "")));
        } catch (JSONException e) {
            mContent = json.getString("content");
        }
    }

    public String getNotiId() {
        return mNotiId;
    }

    public void setNotiId(String notiId) {
        this.mNotiId = notiId;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        this.mType = type;
    }

    public String getLevel() {
        return mLevel;
    }

    public void setLevel(String level) {
        this.mLevel = level;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        this.mCategory = category;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public String getSendDate() {
        return mSendDate;
    }

    public void setSendDate(String sendDate) {
        this.mSendDate = sendDate;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        this.mStatus = status;
    }
}
