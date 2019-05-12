package inc.osbay.android.tutormandarin.sdk.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;
import inc.osbay.android.tutormandarin.sdk.util.LGCUtil;

/**
 * Server request to send request to server.
 *
 * @author Ambrose
 */
public class ServerRequest extends Request<ServerResponse> {
    private static final String TAG = "ServerRequest";

    SharedPreferences mSharedPreferences;
    private Context mContext;
    private Response.Listener<ServerResponse> mListener;

    private Map<String, String> mParams;

    /**
     * Constructor with json object parameter.
     *
     * @param requestObj       Request object to parse to server.
     * @param responseListener Server response listener.
     * @param errorListener    Request error listener.
     */
    public ServerRequest(Context context, String command, JSONObject requestObj,
                         Response.Listener<ServerResponse> responseListener,
                         Response.ErrorListener errorListener) {
        super(Method.POST, CommonConstant.WEB_SERVICE_URL, errorListener);

        mContext = context;
        this.mListener = responseListener;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

//        // LogUtil.showDebugLog(TAG, "Request Object =" + requestObj.toString());
        JSONObject queryObject = new JSONObject();
        try {
            String sessionToken = mSharedPreferences.getString("access_token", "");

            String deviceId = Settings.Secure
                    .getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            PackageInfo pkgInfo = mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), 0);

            String mLocale = Locale.getDefault().getLanguage();

            queryObject.put("Command", command);
            queryObject.put("Request", requestObj);
            queryObject.put("Language", mLocale);
            queryObject.put("Device_Type", 1);
            queryObject.put("Session_token", sessionToken);
            queryObject.put("Time", LGCUtil.getCurrentUTCTimeString());

            queryObject.put("Device_Id", deviceId);
            queryObject.put("Version_Number", pkgInfo.versionCode);
        } catch (JSONException | PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Request string - " + queryObject.toString());

        mParams = new HashMap<>();
        mParams.put("query", queryObject.toString());
        setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 10,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    protected final Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }

    @Override
    public final Response<ServerResponse> parseNetworkResponse(
            NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));

            // Log.showDebugLog(TAG, "Response Object = " + jsonString);
            Log.d(TAG, "Response string - " + jsonString);

            JSONObject jsonObject = new JSONObject(jsonString);

            ServerResponse responseObj = new ServerResponse();

            int statusCode = jsonObject.getInt("Status");
            String sessionToken = jsonObject
                    .getString("New_Session_Token");
            if (!TextUtils.isEmpty(sessionToken)) {
                mSharedPreferences.edit().putString("access_token", sessionToken).apply();
            }

            responseObj.setmActionName(jsonObject.getString("Command"));
            responseObj.setmLanguage(jsonObject.getString("Language"));
            responseObj.setmNewSessionToken(sessionToken);
            responseObj.setmStatusCode(statusCode);
            responseObj.setmDateTime(jsonObject.getString("Time"));

            if (statusCode == ServerResponse.Status.SUCCESS) {
                responseObj.setmResult(jsonObject.getJSONArray("Result"));

                return Response.success(responseObj,
                        HttpHeaderParser.parseCacheHeaders(response));
            } else {
                ServerError serverError;
                try {
                    serverError = new ServerError(jsonObject
                            .getJSONObject("Error"));
                } catch (JSONException jse) {
                    // Log.showErrorLog(TAG, "Cannot get Error Object", jse);
                    serverError = new ServerError(ServerError.Code.JSON_EXCEPTION, "Cannot parse JSON error");
                }

                return Response.error(serverError);
            }
        } catch (UnsupportedEncodingException uee) {
            Log.e(TAG, "Unsupported Encoding Exception", uee);
            return Response.error(new ParseError(uee));
        } catch (JSONException je) {
            Log.e(TAG, "JSON Exception", je);
            return Response.error(new ServerError(ServerError.Code.JSON_EXCEPTION, "JSON exception"));
        }
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        int errorCode = ((volleyError.networkResponse != null) ? volleyError.networkResponse.statusCode : ServerError.Code.NETWORK_ERROR);
        Log.e(TAG, "Parse Network Error with error code - " + errorCode, volleyError);
        return new ServerError(errorCode, "Parse Network Error");
    }

    @Override
    protected final void deliverResponse(ServerResponse response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }
}
