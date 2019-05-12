package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import inc.osbay.android.tutormandarin.sdk.util.LGCUtil;

/**
 * Created by Erik on 7/14/2016.
 */
public class Badges {
    private int mBadgesId;
    private String mBadgesName;
    private double mCredit;
    private int mAwardType;
    private String mAchievementMsg;
    private String mCompletedDate;
    private int mCompletedPercent;
    private int mBadgesCategory;

    public Badges() {
    }

    public Badges(JSONObject json) throws JSONException, ParseException {
        mBadgesId = json.getInt("badges_id");
        mBadgesName = json.getString("badges_name");
        mCredit = json.getDouble("credit");
        mAwardType = json.getInt("award_type");
        mAchievementMsg = json.getString("achievement_msg");
        mCompletedDate = LGCUtil.convertToUTC(json.getString("completed_date"));
        mCompletedPercent = json.getInt("completed_percent");
        mBadgesCategory = json.getInt("badges_category");
    }

    public int getBadgesId() {
        return mBadgesId;
    }

    public void setBadgesId(int badgesId) {
        mBadgesId = badgesId;
    }

    public String getBadgesName() {
        return mBadgesName;
    }

    public void setBadgesName(String badgesName) {
        mBadgesName = badgesName;
    }

    public double getCredit() {
        return mCredit;
    }

    public void setCredit(double credit) {
        mCredit = credit;
    }

    public int getAwardType() {
        return mAwardType;
    }

    public void setAwardType(int awardType) {
        mAwardType = awardType;
    }

    public String getAchievementMsg() {
        return mAchievementMsg;
    }

    public void setAchievementMsg(String achievementMsg) {
        mAchievementMsg = achievementMsg;
    }

    public String getCompletedDate() {
        return mCompletedDate;
    }

    public void setCompletedDate(String completedDate) {
        mCompletedDate = completedDate;
    }

    public int getCompletedPercent() {
        return mCompletedPercent;
    }

    public void setCompletedPercent(int completedPercent) {
        mCompletedPercent = completedPercent;
    }

    public int getBadgesCategory() {
        return mBadgesCategory;
    }

    public void setBadgesCategory(int badgesCategory) {
        mBadgesCategory = badgesCategory;
    }

    public static class AwardType {
        public static int CREDITS = 1;

        public static int LEARNING_GIFT = 2;

        public static int REAL_GIFT = 3;

        public static int REFUND = 4;

        public static int GENERAL = 5;
    }

    public static class Category {
        public static int TIME_BASED = 1;

        public static int COURSE_BASED = 2;

        public static int SOCIAL_BASED = 3;

        public static int RATING_AND_REVIEWS = 4;
    }

//    enum Category {
//        TIME_BASED, COURSE_BASED
//    }
}
