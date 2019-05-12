package inc.osbay.android.tutormandarin.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.android.gms.analytics.CampaignTrackingReceiver;

import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;

/**
 * Created by Erik on 11/7/2017.
 */

public class MyCustomCampaignReceiver extends BroadcastReceiver {
    public static final String INSTALL_REFERRER_ACTION = "com.android.vending.INSTALL_REFERRER";
    public static final String REFERRER = "referrer";
    public static final String KEY_UTM_CONTENT = "utm_content";
    public static final String KEY_UTM_CAMPAIGN = "utm_campaign";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();


        if (context != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(
                    context);

            if (!TextUtils.isEmpty(action) && INSTALL_REFERRER_ACTION.equals(action)) {
                String referrer = intent.getStringExtra(REFERRER);
                if (!TextUtils.isEmpty(referrer)) {
                    String[] splitStrings = new String[0];
                    if (referrer.contains("&")) {
                        splitStrings = referrer.split("&");
                    }

                    String utmContent = getUTMString(KEY_UTM_CONTENT, splitStrings);
                    String utmCampaign = getUTMString(KEY_UTM_CAMPAIGN, splitStrings);

                    if (!TextUtils.isEmpty(utmCampaign) && !TextUtils.isEmpty(utmContent)) {

                        preferences.edit().putString(CommonConstant.AGENT_PROMO_CODE_PREF, utmContent).apply();

                        ServerRequestManager serverRequestManager = new ServerRequestManager(context);
                        serverRequestManager.addReferrerPromoCode(utmContent, utmCampaign, new ServerRequestManager.OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                            }

                            @Override
                            public void onError(ServerError err) {
                            }
                        });
                    }
                }
            }

            new CampaignTrackingReceiver().onReceive(context, intent);
        }
    }

    private String getUTMString(String key, String[] strArray) {
        for (String result : strArray) {
            if (result.contains("=")) {
                if (result.contains(key)) {
                    return result.split("=")[1];
                }
            }
        }
        return "";
    }
}
