package inc.osbay.android.tutormandarin.sdk.client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Model class of Server Response.
 *
 * @author Ambrose
 */
class ServerResponse {

    private String mActionName;

    private JSONArray mResult;

    private String mLanguage;

    private String mNewSessionToken;

    private int mStatusCode;

    private String mDateTime;

//    public String getmActionName() {
//        return mActionName;
//    }

    public void setmActionName(String actionName) {
        this.mActionName = actionName;
    }

    public JSONObject getJSONObjResult() {
        if (mResult.length() == 1) {
            try {
                return mResult.getJSONObject(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public JSONArray getResult() {
        return mResult;
    }

    public void setmResult(JSONArray result) {
        this.mResult = result;
    }

//    public String getmLanguage() {
//        return mLanguage;
//    }

    public void setmLanguage(String language) {
        this.mLanguage = language;
    }

    public String getmNewSessionToken() {
        return mNewSessionToken;
    }

    public void setmNewSessionToken(String newSessionToken) {
        this.mNewSessionToken = newSessionToken;
    }

    public int getmStatusCode() {
        return mStatusCode;
    }

    public void setmStatusCode(int statusCode) {
        this.mStatusCode = statusCode;
    }

//    public String getmDateTime() {
//        return mDateTime;
//    }

    public void setmDateTime(String dateTime) {
        this.mDateTime = dateTime;
    }

    /**
     * Status Code of Server Response.
     *
     * @author Ambrose
     */
    public final class Status {

        /**
         * Status code for SUCCESS case.
         */
        public static final int SUCCESS = 200;

        /**
         * Status code for NEED REFRESH case to refresh token.
         */
        public static final int NEED_REFRESH = 202;

        private Status() {
        }

    }
}
