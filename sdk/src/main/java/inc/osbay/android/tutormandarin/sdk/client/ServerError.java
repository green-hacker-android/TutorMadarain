package inc.osbay.android.tutormandarin.sdk.client;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Model class of Server Error Object.
 *
 * @author Ambrose
 */
public class ServerError extends VolleyError {

    private int mErrorCode;

    private String mErrorMessage;

    /**
     * No argument constructor.
     */
    public ServerError() {
        super();
    }

    /**
     * Constructor class for Server Error object.
     *
     * @param code Error code.
     * @param msg  Error message.
     */
    public ServerError(int code, String msg) {
        super();
        mErrorCode = code;
        mErrorMessage = msg;
    }

    /**
     * Constructor class for Server Error object. Parse json object to Server
     * Error object.
     *
     * @param json Error object with json format.
     * @throws JSONException If key name is not exists in json object, will
     *                       throw.
     */
    public ServerError(JSONObject json) throws JSONException {
        mErrorCode = json.getInt("err_code");
        mErrorMessage = json.getString("err_msg");
    }

    public String getMessage() {
        return mErrorMessage;
    }

    public void setMessage(String errorMessage) {
        mErrorMessage = errorMessage;
    }

    /**
     * Getter method of error code.
     *
     * @return Error code.
     */
    public final int getErrorCode() {
        return mErrorCode;
    }

    public void setErrorCode(int errorCode) {
        mErrorCode = errorCode;
    }

    @Override
    public final String toString() {
        return "ServerError (code=" + mErrorCode + ", msg=" + mErrorMessage
                + ")";
    }

    /**
     * Server Error Codes.
     *
     * @author Ambrose
     */
    public final class Code {

        /**
         * Client request token is expired.
         */
        public static final int TOKEN_EXPIRED = 10029;

        public static final int JSON_EXCEPTION = 1;

        public static final int NETWORK_ERROR = 2;

        public static final int NO_CURRENT_PLAN = 10066;

        public static final int LESSON_BOOKED_IN_PACKAGE = 10086;

        public static final int CLASS_ALREADY_BOOKED = 10086;

        public static final int BOOKING_DATE_NOT_AVAILABLE = 10087;

        public static final int TOPIC_ALREADY_BOOKED = 10090;

        public static final int HIGHER_TOPIC_LEVEL = 10091;

        public static final int HIGHER_PACKAGE_LEVEL = 10076;

        public static final int LESSON_CONTAINS_IN_PACKAGE = 10097;

        public static final int INSUFFICIENT_BALANCE = 10052;

        public static final int CHINESE_LEVEL_LOWER = 10076;

        public static final int NO_TUTOR_SCHEDULE = 10065;

        public static final int NO_OPERATOR_ONLINE = 10077;

        /**
         * Email address is already exist. Cannot use this email to register new
         * account.
         */
        // public static final int EMAIL_ALREADY_EXISTS = 10005;
        private Code() {
        }

    }
}
