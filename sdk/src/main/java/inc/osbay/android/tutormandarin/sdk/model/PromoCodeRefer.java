package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;

import inc.osbay.android.tutormandarin.sdk.util.LGCUtil;

/**
 * Created by Erik on 11/13/2017.
 */

public class PromoCodeRefer implements Serializable {
    private int mReferrerId;

    /**
     * Referrer name who use my promo code
     */
    private String mName;

    /**
     * Date of other person use my promo code
     */
    private String mDate;

    /**
     * Get credit amount
     */
    private double mReward;

    /**
     * 1. Credit Received
     * 2. Waiting
     */
    private int mStatus;

    public PromoCodeRefer() {
    }

    public PromoCodeRefer(JSONObject jsonObject) throws JSONException, ParseException {
//        mPromoEnterId = jsonObject.getInt("promotion_history_id");
        mName = jsonObject.getString("refer_name");
        mDate = LGCUtil.convertToLocaleNoSecond(jsonObject.getString("purchase_date"));
        mReward = jsonObject.getDouble("invitor_earn_money");
        mStatus = jsonObject.getInt("status");
    }

    public int getReferrerId() {
        return mReferrerId;
    }

    public void setReferrerId(int referrerId) {
        mReferrerId = referrerId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public double getReward() {
        return mReward;
    }

    public void setReward(double reward) {
        mReward = reward;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }
}
