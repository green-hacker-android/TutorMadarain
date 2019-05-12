package inc.osbay.android.tutormandarin.ui.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.util.CommonUtil;
import inc.osbay.android.tutormandarin.util.FacebookAPI;


public class SignUpActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    public static final String VP_SU_POSITION = "SignUpActivity.VP_SU_POSITION";
    public static final String MAIN_SIGN_UP = "SignUpActivity.MAIN_SIGN_UP";

    private static final String TAG = SignUpActivity.class.getSimpleName();
    public static int RC_SIGN_IN = 111;
    private static int TYPE_FOR_FACEBOOK = 1;
    private ServerRequestManager mServerRequestManager;
    private SharedPreferences mPreferences;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private FacebookAPI mFacebookAPI;
    private TextView mInvalidNameTextView;
    private TextView mInvalidEmailTextView;
    private TextView mInvalidPasswordTextView;
    private TextView mTermsOfServiceTextView;
    private EditText mFirstNameEditText;
    private EditText mLastNameEditText;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private GoogleApiClient mGoogleApiClient;

    private Button mSignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        FlurryAgent.logEvent("SignUp Reached");

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        int mainSignUp = getIntent().getExtras().getInt(MAIN_SIGN_UP);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        alertDialog = new AlertDialog.Builder(this)
                .setPositiveButton(getString(R.string.su_dialog_ok), null)
                .create();

        mFacebookAPI = new FacebookAPI(this);
        mServerRequestManager = new ServerRequestManager(getApplicationContext());
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Typeface typeface = Typeface.createFromAsset(getAssets(),
                "fonts/Montserrat-Bold.ttf");

        findViewById(R.id.imv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        RelativeLayout rlImgSignUpBackground = findViewById(R.id.rl_img_sign_up_background);
        LinearLayout llSignUpBackground = findViewById(R.id.ll_sign_up_background);

        mInvalidNameTextView = findViewById(R.id.tv_invalid_name);
        mInvalidEmailTextView = findViewById(R.id.tv_invalid_email);
        mInvalidPasswordTextView = findViewById(R.id.tv_invalid_password);
        mTermsOfServiceTextView = findViewById(R.id.tv_terms_of_service);

        mFirstNameEditText = findViewById(R.id.edt_first_name);
        mLastNameEditText = findViewById(R.id.edt_last_name);
        mEmailEditText = findViewById(R.id.edt_email);

        mPasswordEditText = findViewById(R.id.edt_password);
        mPasswordEditText.setTypeface(Typeface.DEFAULT);

        ImageView imvSignUpWelcomeBlur = findViewById(R.id.imv_sign_up_welcome_blur);

        ImageView imvFacebookSignUp = findViewById(R.id.imv_facebook_sign_up);
        imvFacebookSignUp.setOnClickListener(this);

        ImageView imvGoogleSignUp = findViewById(R.id.imv_google_sign_up);
        imvGoogleSignUp.setOnClickListener(this);

        mSignUpButton = findViewById(R.id.btn_sign_up);
        mSignUpButton.setTypeface(typeface);
        mSignUpButton.setOnClickListener(this);

        RadioButton rbTermsOfService = findViewById(R.id.rb_terms_of_service);
        rbTermsOfService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mSignUpButton.setEnabled(true);
                } else {
                    mSignUpButton.setEnabled(false);
                }
            }
        });

        if (mainSignUp == 1) {
            rlImgSignUpBackground.setVisibility(View.VISIBLE);
            imvSignUpWelcomeBlur.setVisibility(View.GONE);
        } else {
            rlImgSignUpBackground.setVisibility(View.GONE);
            imvSignUpWelcomeBlur.setVisibility(View.VISIBLE);
            int pgNo = getIntent().getIntExtra(VP_SU_POSITION, 0);

            if (pgNo == 1) {
                llSignUpBackground.setVisibility(View.GONE);
                imvSignUpWelcomeBlur.setImageResource(R.drawable.bg_welcome_blur_1);
            } else if (pgNo == 2) {
                llSignUpBackground.setVisibility(View.GONE);
                imvSignUpWelcomeBlur.setImageResource(R.drawable.bg_welcome_blur_2);
            } else if (pgNo == 3) {
                llSignUpBackground.setVisibility(View.GONE);
                imvSignUpWelcomeBlur.setImageResource(R.drawable.bg_welcome_blur_3);
            } else if (pgNo == 4) {
                llSignUpBackground.setVisibility(View.GONE);
                imvSignUpWelcomeBlur.setImageResource(R.drawable.bg_welcome_blur_4);
            } else {
                llSignUpBackground.setVisibility(View.VISIBLE);
            }
        }

        customTextView();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_sign_up:
                String firstName = mFirstNameEditText.getText().toString();
                String lastName = mLastNameEditText.getText().toString();
                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                boolean isValidAll = true;

                if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)) {
                    mInvalidNameTextView.setVisibility(View.GONE);
                } else {
                    mInvalidNameTextView.setVisibility(View.VISIBLE);
                    isValidAll = false;
                }

                if (CommonUtil.validateEmail(email)) {
                    mInvalidEmailTextView.setVisibility(View.GONE);
                } else {
                    mInvalidEmailTextView.setVisibility(View.VISIBLE);
                    isValidAll = false;
                }

                if (CommonUtil.validatePassword(password)) {
                    mInvalidPasswordTextView.setVisibility(View.GONE);
                } else {
                    mInvalidPasswordTextView.setVisibility(View.VISIBLE);
                    isValidAll = false;
                }

                if (view.getId() == R.id.btn_sign_up && isValidAll) {
                    progressDialog.setMessage(getString(R.string.su_signing_up));
                    progressDialog.show();

                    mServerRequestManager.signUpPartOneNew(firstName, lastName, email, password,
                            new ServerRequestManager.OnRequestFinishedListener() {
                                @Override
                                public void onSuccess(Object obj) {
                                    progressDialog.dismiss();

                                    FlurryAgent.logEvent("Signed Up Completed.");
                                    mServerRequestManager.downloadFavorites(null);
                                    mServerRequestManager.downloadNotificationList(null);

                                    Intent partTwoIntent = new Intent(SignUpActivity.this, SignUpContinueActivity.class);
                                    startActivity(partTwoIntent);
                                    if (WelcomeTutorialActivity.mInstance != null) {
                                        WelcomeTutorialActivity.mInstance.finish();
                                    }
                                    if (WelcomeActivity.instance != null) {
                                        WelcomeActivity.instance.finish();
                                    }
                                    finish();
                                }

                                @Override
                                public void onError(ServerError error) {
                                    progressDialog.dismiss();

                                    alertDialog.setTitle(getString(R.string.su_sign_up_error));
                                    alertDialog.setMessage(error.getMessage());
                                    alertDialog.show();
                                }
                            });
                }
                break;

            case R.id.imv_facebook_sign_up:
                mInvalidEmailTextView.setVisibility(View.GONE);
                mFacebookAPI.login(new FacebookAPI.OnLoginFinishListener() {

                    @Override
                    public void onLoginSuccess(final String userId, final String firstName, final String lastName, final String email, final String avatarURL) {
                        progressDialog.setMessage(getString(R.string.su_signing_up));
                        progressDialog.show();

                        mServerRequestManager.loginThirdPart(firstName, lastName, userId, String.valueOf(TYPE_FOR_FACEBOOK), email, avatarURL, new ServerRequestManager.OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                }
                                FlurryAgent.logEvent("Facebook Signed Up Completed.");
                                mServerRequestManager.downloadFavorites(null);
                                mServerRequestManager.downloadNotificationList(null);

                                int status = (int) result;
                                if (status == 1) {
                                    if (WelcomeTutorialActivity.mInstance != null) {
                                        WelcomeTutorialActivity.mInstance.finish();
                                    }
                                    if (WelcomeActivity.instance != null) {
                                        WelcomeActivity.instance.finish();
                                    }

                                    String accountId = mPreferences.getString("account_id", null);
                                    String token = mPreferences.getString("access_token", null);
                                    if (!TextUtils.isEmpty(accountId) && !TextUtils.isEmpty(token)) {
                                        AccountAdapter accountAdapter = new AccountAdapter(getApplicationContext());
                                        Account account = accountAdapter.getAccountById(accountId);
                                        if (account != null && account.getStatus() == Account.Status.INACTIVE) {
                                            FlurryAgent.logEvent("SignUp Inactive Continue(Facebook)");

                                            Intent mainIntent = new Intent(SignUpActivity.this, SignUpContinueActivity.class);
                                            mainIntent.setAction("refresh_fragment");
                                            startActivity(mainIntent);

                                            finish();
                                            return;
                                        }
                                    }

                                    Intent mainIntent = new Intent(SignUpActivity.this, MainActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("class_type", "normal");
                                    mainIntent.putExtras(bundle);
                                    mainIntent.setAction("refresh_fragment");
                                    startActivity(mainIntent);

                                    finish();
                                } else {
                                    FlurryAgent.logEvent("User Inactive After Sign Up(Facebook)");
                                    Intent partTwoIntent = new Intent(SignUpActivity.this, SignUpContinueActivity.class);
                                    startActivity(partTwoIntent);
                                    if (WelcomeTutorialActivity.mInstance != null) {
                                        WelcomeTutorialActivity.mInstance.finish();
                                    }
                                    if (WelcomeActivity.instance != null) {
                                        WelcomeActivity.instance.finish();
                                    }
                                    finish();
                                }
                            }

                            @Override
                            public void onError(ServerError err) {
                                progressDialog.dismiss();

                                alertDialog.setTitle(getString(R.string.su_sign_up_error));
                                alertDialog.setMessage(err.getMessage());
                                alertDialog.show();
                            }
                        });
                    }

                    @Override
                    public void onLoginFail(int errorCode) {
                        Log.i(TAG, "Login Error");
                    }
                });
                break;

            case R.id.imv_google_sign_up:
                mInvalidEmailTextView.setVisibility(View.GONE);
                signIn();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult gsi = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(gsi);
        } else {
            mFacebookAPI.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void customTextView() {
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(
                getString(R.string.su_term_and_agreement_1));
        spanTxt.append(getString(R.string.su_term_and_agreement_2));
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent termOfUseIntent = new Intent(getApplicationContext(), WebViewActivity.class);
                termOfUseIntent.putExtra(WebViewActivity.EXTRA_WEB_URL, "http://service.tutormandarin.net/CommonHtml/TutorMandarin-TermsofUse.htm");
                startActivity(termOfUseIntent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                ds.setUnderlineText(true);
            }
        }, spanTxt.length() - getString(R.string.su_term_and_agreement_2).length(), spanTxt.length(), 0);
        spanTxt.append(getString(R.string.su_term_and_agreement_3));
        spanTxt.append(getString(R.string.su_term_and_agreement_4));
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent privacyPolicyIntent = new Intent(getApplicationContext(), WebViewActivity.class);
                privacyPolicyIntent.putExtra(WebViewActivity.EXTRA_WEB_URL, "http://service.tutormandarin.net/CommonHtml/TutorMandarin-PrivacyPolicy.htm");
                startActivity(privacyPolicyIntent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                ds.setUnderlineText(true);
            }
        }, spanTxt.length() - getString(R.string.su_term_and_agreement_4).length(), spanTxt.length(), 0);
        spanTxt.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.text_color_white)), 0, spanTxt.length(), 0);
        mTermsOfServiceTextView.setMovementMethod(LinkMovementMethod.getInstance());
        mTermsOfServiceTextView.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                    }
                });
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            revokeAccess();
            GoogleSignInAccount sgi = result.getSignInAccount();
            if (sgi != null) {
                progressDialog.setMessage(getString(R.string.su_signing_up));
                progressDialog.show();

                int TYPE_FOR_GOOGLE = 2;
                mServerRequestManager.loginThirdPart(sgi.getGivenName(), sgi.getFamilyName(), sgi.getId(), String.valueOf(TYPE_FOR_GOOGLE), sgi.getEmail(), String.valueOf(sgi.getPhotoUrl()), new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        progressDialog.dismiss();

                        FlurryAgent.logEvent("Google Signed Up Completed.");
                        mServerRequestManager.downloadFavorites(null);
                        mServerRequestManager.downloadNotificationList(null);

                        int status = (int) result;
                        if (status == 1) {
                            if (WelcomeTutorialActivity.mInstance != null) {
                                WelcomeTutorialActivity.mInstance.finish();
                            }
                            if (WelcomeActivity.instance != null) {
                                WelcomeActivity.instance.finish();
                            }

                            String accountId = mPreferences.getString("account_id", null);
                            String token = mPreferences.getString("access_token", null);
                            if (!TextUtils.isEmpty(accountId) && !TextUtils.isEmpty(token)) {
                                FlurryAgent.logEvent("SignUp Inactive Continue(Google)");
                                AccountAdapter accountAdapter = new AccountAdapter(getApplicationContext());
                                Account account = accountAdapter.getAccountById(accountId);
                                if (account != null && account.getStatus() == Account.Status.INACTIVE) {

                                    Intent mainIntent = new Intent(SignUpActivity.this, SignUpContinueActivity.class);
                                    mainIntent.setAction("refresh_fragment");
                                    startActivity(mainIntent);

                                    finish();
                                    return;
                                }
                            }

                            Intent mainIntent = new Intent(SignUpActivity.this, MainActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("class_type", "normal");
                            mainIntent.putExtras(bundle);
                            mainIntent.setAction("refresh_fragment");
                            startActivity(mainIntent);

                            finish();
                        } else {
                            FlurryAgent.logEvent("User Inactive After Sign Up(Google)");
                            Intent partTwoIntent = new Intent(SignUpActivity.this, SignUpContinueActivity.class);
                            startActivity(partTwoIntent);
                            if (WelcomeTutorialActivity.mInstance != null) {
                                WelcomeTutorialActivity.mInstance.finish();
                            }
                            if (WelcomeActivity.instance != null) {
                                WelcomeActivity.instance.finish();
                            }
                            finish();
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        progressDialog.dismiss();

                        alertDialog.setTitle(getString(R.string.su_sign_up_error));
                        alertDialog.setMessage(err.getMessage());
                        alertDialog.show();
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.su_google_sign_in_error), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.su_google_result_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), getString(R.string.su_connection_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FlurryAgent.logEvent("SignUp Back");
        finish();
    }
}
