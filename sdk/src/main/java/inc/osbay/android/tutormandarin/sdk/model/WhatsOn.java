package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import inc.osbay.android.tutormandarin.sdk.util.LGCUtil;

/**
 * Created by Erik on 3/18/2016.
 */
public class WhatsOn {
    private String mWhatsOnId;

    private String mWhatsOnTitle;

    private String mWhatsOnTitleImage;

    private String mWhatsOnPostedDate;

    private int mWhatsOnViewCount;

    private int mWhatsOnFavouriteCount;

    private String mWhatsOnArticle;

    private String mWhatsOnTopicName;

    private String mWhatsOnGrammar;

    public WhatsOn() {

    }

    public WhatsOn(JSONObject jsonObject) throws JSONException, ParseException {
        mWhatsOnId = jsonObject.getString("whatson_id");
        mWhatsOnTitle = jsonObject.getString("whatson_title");
        mWhatsOnTitleImage = jsonObject.getString("whatson_title_image");
        mWhatsOnPostedDate = LGCUtil.convertToLocale(jsonObject.getString("create_date"));
        mWhatsOnViewCount = jsonObject.getInt("viewed_count");
        mWhatsOnFavouriteCount = jsonObject.getInt("favourited_count");
        mWhatsOnArticle = jsonObject.getString("whatson_article");
        mWhatsOnTopicName = jsonObject.getString("whatson_topic_name");
        mWhatsOnGrammar = jsonObject.getString("grammar");
    }

    public String getWhatsOnId() {
        return mWhatsOnId;
    }

    public void setmWhatsOnId(String mWhatsOnId) {
        this.mWhatsOnId = mWhatsOnId;
    }

    public String getTitle() {
        return mWhatsOnTitle;
    }

    public void setmWhatsOnTitle(String mWhatsOnTitle) {
        this.mWhatsOnTitle = mWhatsOnTitle;
    }

    public String getCoverPhoto() {
        return mWhatsOnTitleImage;
    }

    public void setmWhatsOnTitleImage(String mWhatsOnTitleImage) {
        this.mWhatsOnTitleImage = mWhatsOnTitleImage;
    }

    public int getmWhatsOnViewCount() {
        return mWhatsOnViewCount;
    }

    public void setmWhatsOnViewCount(int mWhatsOnViewCount) {
        this.mWhatsOnViewCount = mWhatsOnViewCount;
    }

    public int getmWhatsOnFavouriteCount() {
        return mWhatsOnFavouriteCount;
    }

    public void setmWhatsOnFavouriteCount(int mWhatsOnFavouriteCount) {
        this.mWhatsOnFavouriteCount = mWhatsOnFavouriteCount;
    }

    public String getmWhatsOnPostedDate() {
        return mWhatsOnPostedDate;
    }

    public void setmWhatsOnPostedDate(String mWhatsOnPostedDate) {
        this.mWhatsOnPostedDate = mWhatsOnPostedDate;
    }

    public String getArticle() {
        return mWhatsOnArticle;
    }

    public void setmWhatsOnArticle(String mWhatsOnArticle) {
        this.mWhatsOnArticle = mWhatsOnArticle;
    }

    public String getTopicName() {
        return mWhatsOnTopicName;
    }

    public void setmWhatsOnTopicName(String mWhatsOnTopicName) {
        this.mWhatsOnTopicName = mWhatsOnTopicName;
    }

    public String getmWhatsOnGrammar() {
        return mWhatsOnGrammar;
    }

    public void setmWhatsOnGrammar(String mWhatsOnGrammar) {
        this.mWhatsOnGrammar = mWhatsOnGrammar;
    }
}
