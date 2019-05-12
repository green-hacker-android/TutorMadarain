package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;

import inc.osbay.android.tutormandarin.sdk.util.LGCUtil;

/**
 * Created by Erik on 11/13/2017.
 */

public class PromoCodeEnter implements Serializable {

    private int mPromoEnterId;

    /**
     * Use other promo code.
     */
    private String mCode;

    /**
     * Event, Agent, Share, Consultant
     */
    private String mType;

    /**
     * 1. Other promo code enter date
     * 2. Credit get date
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

    public PromoCodeEnter() {

    }

    public PromoCodeEnter(JSONObject jsonObject) throws JSONException, ParseException {
//        mPromoEnterId = jsonObject.getInt("promotion_history_id");
        mCode = jsonObject.getString("invitor_promo_code");
        mType = jsonObject.getString("type");
        mDate = LGCUtil.convertToLocaleNoSecond(jsonObject.getString("purchase_date"));
        mReward = jsonObject.getDouble("refer_earn_credit");
        mStatus = jsonObject.getInt("status");
    }

    public int getPromoEnterId() {
        return mPromoEnterId;
    }

    public void setPromoEnterId(int promoEnterId) {
        mPromoEnterId = promoEnterId;
    }

    public String getCode() {
        return mCode;
    }

    public void setCode(String code) {
        mCode = code;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
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
