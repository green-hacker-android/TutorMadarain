package inc.osbay.android.tutormandarin.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONObject;

import java.util.Arrays;

/**
 * Facebook API class for TutorMandarin.
 *
 * @author Erik
 */
public class FacebookAPI {

    private CallbackManager callbackManager;

    private Activity mContext;

    private OnLoginFinishListener mLoginFinishListener;

    private ProfileTracker mProfileTracker;

    /**
     * Construct FacebookAPI.
     *
     * @param cont activity
     */
    public FacebookAPI(Activity cont) {
        mContext = cont;
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONObject object,
                                            GraphResponse response) {
                                        try {
//                                            String gender = "0";
//                                            if (object.has("gender")) {
//                                                gender = object.getString("gender");
//                                                if ("male".equals(gender)) {
//                                                    gender = "1";
//                                                } else if ("female".equals(gender)) {
//                                                    gender = "2";
//                                                } else {
//                                                    gender = "0";
//                                                }
//                                            }

                                            String email = "";
                                            if (object.has("email")) {
                                                email = object.getString("email");
                                            }

                                            Profile profile = Profile.getCurrentProfile();

                                            final String avatarURL = String.valueOf(profile.getProfilePictureUri(500, 500));
                                            Log.i("AvartarURL", avatarURL);
                                            if (profile == null) {
                                                final String finalEmail = email;
                                                mProfileTracker = new ProfileTracker() {
                                                    @Override
                                                    protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                                                        mProfileTracker.stopTracking();
                                                        if (mLoginFinishListener != null) {
                                                            mLoginFinishListener.onLoginSuccess(profile2.getId(),
                                                                    profile2.getFirstName() + profile2.getMiddleName(),
                                                                    profile2.getLastName(), finalEmail,
                                                                    String.valueOf(profile2.getProfilePictureUri(250, 250)));
                                                        }
                                                    }
                                                };
                                            } else {
                                                if (mLoginFinishListener != null) {
                                                    mLoginFinishListener.onLoginSuccess(profile.getId(),
                                                            profile.getFirstName() + profile.getMiddleName(), profile.getLastName(), email, avatarURL);
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "picture, id, name, gender, email, user_link");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        if (mLoginFinishListener != null) {
                            mLoginFinishListener.onLoginFail(Error.PERMISSION_DENINED);
                        }
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        if (mLoginFinishListener != null) {
                            mLoginFinishListener.onLoginFail(Error.NETWORK_OR_INVALID_CREDENTIAL);
                        }
                    }
                });

    }

    /**
     * Facebook Login.
     *
     * @param listener LoginFinishListener
     */
    @SuppressWarnings("deprecation")
    public final void login(OnLoginFinishListener listener) {
        mLoginFinishListener = listener;
        LoginManager.getInstance().logInWithReadPermissions(mContext, Arrays.asList("public_profile", "email"));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Login Finish Listener's interface.
     */
    public interface OnLoginFinishListener {
        /**
         * If login success, get Facebook id.
         *
         * @param accountId Faceook id
         */
        void onLoginSuccess(String accountId, String firstName, String lastName, String email, String avartarURL);

        /**
         * If login fails, get error code.
         *
         * @param errorCode Error Code
         */
        void onLoginFail(int errorCode);
    }

    /**
     * Login's Error.
     */
    public final class Error {
        /**
         * Permission is not granted to login.
         */
        static final int PERMISSION_DENINED = 1;

        /**
         * Connection Fail.
         */
        static final int NETWORK_OR_INVALID_CREDENTIAL = 2;

        private Error() {

        }

    }
}
