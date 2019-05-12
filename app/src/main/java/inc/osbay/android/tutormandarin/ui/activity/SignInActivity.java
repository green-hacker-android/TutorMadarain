package inc.osbay.android.tutormandarin.ui.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.util.Date;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.util.CommonUtil;
import inc.osbay.android.tutormandarin.util.FacebookAPI;

public class SignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    public static final String MAIN_SIGN_IN = "SignInActivity.MAIN_SIGN_IN";
    public static final String PWD_TIMER = "SignInFragment.PWD_TIMER";
    public static final String FIRST_TIME = "SignInFragment.FIRST_TIME";
    public static final String VP_SI_POSITION = "SignUpActivity.VP_SI_POSITION";
    private static int RC_SIGN_IN = 111;
    private static int TYPE_FOR_FACEBOOK = 1;

    private ServerRequestManager mServerRequestManager;
    private SharedPreferences mSharedPreferences;

    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private TextView mInvalidEmailTextView;
    private TextView mInvalidPasswordTextView;
    private boolean isFirstTime;
    private FacebookAPI mFacebookAPI;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        FlurryAgent.logEvent("SignIn Reached");

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        int mainSignIn = getIntent().getExtras().getInt(MAIN_SIGN_IN);

        mFacebookAPI = new FacebookAPI(this);

        mServerRequestManager = new ServerRequestManager(SignInActivity.this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        progressDialog = new ProgressDialog(SignInActivity.this);
        progressDialog.setCancelable(false);

        alertDialog = new AlertDialog.Builder(SignInActivity.this)
                .setPositiveButton(getString(R.string.si_dialog_ok), null)
                .create();
        Typeface typeface = Typeface.createFromAsset(getAssets(),
                "fonts/Montserrat-Bold.ttf");

        findViewById(R.id.imv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mEmailEditText = findViewById(R.id.edt_email);

        mPasswordEditText = findViewById(R.id.edt_password);
        mPasswordEditText.setTypeface(Typeface.DEFAULT);

        mInvalidEmailTextView = findViewById(R.id.tv_invalid_email);
        mInvalidPasswordTextView = findViewById(R.id.tv_invalid_password);
        RelativeLayout rlImvSignInBackground = findViewById(R.id.rl_imv_sign_in_background);
        LinearLayout llSignInBackground = findViewById(R.id.ll_sign_in_background);
        ImageView imvSignInWelcomeBlur = findViewById(R.id.imv_sign_in_welcome_blur);
        ImageView imvFacebookSignUp = findViewById(R.id.imv_facebook_sign_up);
        imvFacebookSignUp.setOnClickListener(this);

        ImageView imvGoogleSignUp = findViewById(R.id.imv_google_sign_up);
        imvGoogleSignUp.setOnClickListener(this);

        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvForgotPassword.setOnClickListener(this);

        Button btnSignIn = findViewById(R.id.btn_sign_in);
        btnSignIn.setTypeface(typeface);
        btnSignIn.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestIdToken(getString(R.string.server_client_id))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        if (mainSignIn == 1) {
            rlImvSignInBackground.setVisibility(View.VISIBLE);
            imvSignInWelcomeBlur.setVisibility(View.GONE);
        } else {
            rlImvSignInBackground.setVisibility(View.GONE);
            imvSignInWelcomeBlur.setVisibility(View.VISIBLE);
            int pgNo = getIntent().getIntExtra(VP_SI_POSITION, 0);

            if (pgNo == 1) {
                llSignInBackground.setVisibility(View.GONE);
                imvSignInWelcomeBlur.setImageResource(R.drawable.bg_welcome_blur_1);
            } else if (pgNo == 2) {
                llSignInBackground.setVisibility(View.GONE);
                imvSignInWelcomeBlur.setImageResource(R.drawable.bg_welcome_blur_2);
            } else if (pgNo == 3) {
                llSignInBackground.setVisibility(View.GONE);
                imvSignInWelcomeBlur.setImageResource(R.drawable.bg_welcome_blur_3);
            } else if (pgNo == 4) {
                llSignInBackground.setVisibility(View.GONE);
                imvSignInWelcomeBlur.setImageResource(R.drawable.bg_welcome_blur_4);
            } else {
                llSignInBackground.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FlurryAgent.logEvent("SignIn Back");
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(final View view) {
        final String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        boolean isValidAll = true;
        if (CommonUtil.validateEmail(email)) {
            mInvalidEmailTextView.setVisibility(View.GONE);
        } else {
            mInvalidEmailTextView.setVisibility(View.VISIBLE);
            isValidAll = false;
        }

        switch (view.getId()) {
            case R.id.tv_forgot_password:
                mInvalidEmailTextView.setVisibility(View.GONE);

                isFirstTime = mSharedPreferences.getBoolean(FIRST_TIME, false);

                long timer = mSharedPreferences.getLong(PWD_TIMER, 0) + 60000;
                Date d = new Date();
                long saveTimer = d.getTime();
                if (!isFirstTime) {
                    timer = 0;
                    saveTimer = 0;
                }

                if (timer <= saveTimer) {
                    View dialog = LayoutInflater.from(this).inflate(R.layout.dialog_forget_password, null);
                    final EditText etForgotEmail = dialog.findViewById(R.id.et_forgot_pwd_email);
                    final TextView tvInvalidEmail = dialog.findViewById(R.id.tv_invalid_email);
                    final AlertDialog forgotAlertDialog = new AlertDialog.Builder(this)
                            .setView(dialog)
                            .setPositiveButton(getString(R.string.si_dialog_forget_pwd_ok), null)
                            .setNegativeButton(getString(R.string.si_dialog_forget_pwd_cancel), null)
                            .create();

                    forgotAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(final DialogInterface dialogInterface) {
                            Button positive = forgotAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            positive.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    String emailForgot = etForgotEmail.getText().toString();
                                    if (CommonUtil.validateEmail(emailForgot)) {
                                        final ProgressDialog progressDialog = new ProgressDialog(SignInActivity.this);
                                        progressDialog.setMessage(getString(R.string.si_dialog_forget_loading));
                                        progressDialog.show();
                                        dialogInterface.dismiss();
                                        tvInvalidEmail.setVisibility(View.GONE);
                                        isFirstTime = true;
                                        mServerRequestManager = new ServerRequestManager(SignInActivity.this);
                                        mServerRequestManager.forgotPassword(emailForgot, new ServerRequestManager.OnRequestFinishedListener() {
                                            @Override
                                            public void onSuccess(Object result) {
                                                progressDialog.dismiss();
                                                Date d = new Date();
                                                mSharedPreferences.edit().putLong(PWD_TIMER, d.getTime()).apply();
                                                mSharedPreferences.edit().putBoolean(FIRST_TIME, true).apply();

                                                AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                                                builder.setTitle(getString(R.string.si_dialog_send_succeed));
                                                builder.setMessage(getString(R.string.si_dialog_send_succeed_txt));
                                                builder.setPositiveButton(getString(R.string.si_dialog_send_succeed_ok), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                    }
                                                });
                                                AlertDialog alertDialog = builder.create();
                                                alertDialog.show();
                                            }

                                            @Override
                                            public void onError(ServerError err) {
                                                dialogInterface.dismiss();
                                                progressDialog.dismiss();

                                                View dialogErr = LayoutInflater.from(SignInActivity.this).inflate(R.layout.dialog_email_error, null);
                                                final EditText etEmailErr = dialogErr.findViewById(R.id.et_email_err);
                                                final TextView tvInvalidEmail = dialogErr.findViewById(R.id.tv_invalid_email);
                                                final AlertDialog forgotErrAlertDialog = new AlertDialog.Builder(SignInActivity.this)
                                                        .setView(dialogErr)
                                                        .setPositiveButton(getString(R.string.si_dialog_forget_pwd_ok), null)
                                                        .setNegativeButton(getString(R.string.si_dialog_forget_pwd_cancel), null).create();

                                                forgotErrAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                                    @Override
                                                    public void onShow(final DialogInterface dialogInterface2) {
                                                        Button btn = forgotErrAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                                        btn.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                String emailError = etEmailErr.getText().toString();
                                                                if (CommonUtil.validateEmail(emailError)) {
                                                                    final ProgressDialog progressDialog1 = new ProgressDialog(SignInActivity.this);
                                                                    progressDialog1.setMessage(getString(R.string.si_dialog_email_error_loading));
                                                                    progressDialog1.show();
                                                                    dialogInterface2.dismiss();
                                                                    tvInvalidEmail.setVisibility(View.GONE);
                                                                    mServerRequestManager = new ServerRequestManager(SignInActivity.this);
                                                                    mServerRequestManager.forgotPassword(emailError, new ServerRequestManager.OnRequestFinishedListener() {
                                                                        @Override
                                                                        public void onSuccess(Object result) {
                                                                            progressDialog1.dismiss();
                                                                            Date d1 = new Date();
                                                                            mSharedPreferences.edit().putLong(PWD_TIMER, d1.getTime()).apply();
                                                                            mSharedPreferences.edit().putBoolean(FIRST_TIME, true).apply();
                                                                            AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                                                                            builder.setTitle(getString(R.string.si_dialog_send_succeed));
                                                                            builder.setMessage(getString(R.string.si_dialog_send_succeed_txt));
                                                                            builder.setPositiveButton(getString(R.string.si_dialog_send_succeed_ok), new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                                    dialogInterface.dismiss();
                                                                                }
                                                                            });
                                                                            AlertDialog alertDialog = builder.create();
                                                                            alertDialog.show();
                                                                        }

                                                                        @Override
                                                                        public void onError(ServerError err) {
                                                                            progressDialog1.dismiss();
                                                                            Toast.makeText(SignInActivity.this, err.getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                } else {
                                                                    tvInvalidEmail.setVisibility(View.VISIBLE);
                                                                }
                                                            }
                                                        });
                                                    }
                                                });

                                                forgotErrAlertDialog.show();
                                            }
                                        });
                                    } else {
                                        tvInvalidEmail.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                    });
                    forgotAlertDialog.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                    builder.setMessage(getString(R.string.si_dialog_reset_time_limit));
                    builder.setPositiveButton(getString(R.string.si_dialog_reset_time_limit_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                break;
            case R.id.btn_sign_in:
                if (CommonUtil.validatePassword(password)) {
                    mInvalidPasswordTextView.setVisibility(View.GONE);
                } else {
                    mInvalidPasswordTextView.setVisibility(View.VISIBLE);
                    isValidAll = false;
                }

                if (isValidAll) {
                    progressDialog.setMessage(getString(R.string.si_signing_in));
                    progressDialog.show();

                    mServerRequestManager.loginNew(email, password,
                            new ServerRequestManager.OnRequestFinishedListener() {
                                @Override
                                public void onSuccess(Object obj) {
                                    FlurryAgent.logEvent("SignIn Success");
                                    progressDialog.dismiss();

                                    mServerRequestManager.downloadFavorites(null);
                                    mServerRequestManager.downloadNotificationList(null);
                                    if (WelcomeTutorialActivity.mInstance != null) {
                                        WelcomeTutorialActivity.mInstance.finish();
                                    }

                                    if (WelcomeActivity.instance != null) {
                                        WelcomeActivity.instance.finish();
                                    }

                                    String accountId = mSharedPreferences.getString("account_id", null);
                                    String token = mSharedPreferences.getString("access_token", null);
                                    if (!TextUtils.isEmpty(accountId) && !TextUtils.isEmpty(token)) {
                                        AccountAdapter accountAdapter = new AccountAdapter(getApplicationContext());
                                        Account account = accountAdapter.getAccountById(accountId);
                                        if (account != null && account.getStatus() == Account.Status.INACTIVE) {
                                            FlurryAgent.logEvent("SignIn Inactive Continue");
                                            Intent mainIntent = new Intent(SignInActivity.this, SignUpContinueActivity.class);
                                            mainIntent.setAction("refresh_fragment");
                                            startActivity(mainIntent);

                                            finish();
                                            return;
                                        }
                                    }

                                    Intent mainIntent = new Intent(SignInActivity.this, MainActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("class_type", "normal");
                                    mainIntent.putExtras(bundle);
                                    startActivity(mainIntent);
                                    finish();
                                }

                                @Override
                                public void onError(ServerError error) {
                                    progressDialog.dismiss();
                                    alertDialog.setTitle(getString(R.string.si_sign_in_error));
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
                    public void onLoginSuccess(final String userId, final String firstName, final String lastName, final String email, String avartarURL) {
                        progressDialog.setMessage(getString(R.string.si_signing_in));
                        progressDialog.show();

                        mServerRequestManager.loginThirdPart(firstName, lastName, userId, String.valueOf(TYPE_FOR_FACEBOOK), email, avartarURL, new ServerRequestManager.OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                progressDialog.dismiss();

                                FlurryAgent.logEvent("Facebook Signed In Completed.");
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

                                    String accountId = mSharedPreferences.getString("account_id", null);
                                    String token = mSharedPreferences.getString("access_token", null);
                                    if (!TextUtils.isEmpty(accountId) && !TextUtils.isEmpty(token)) {
                                        AccountAdapter accountAdapter = new AccountAdapter(getApplicationContext());
                                        Account account = accountAdapter.getAccountById(accountId);
                                        if (account != null && account.getStatus() == Account.Status.INACTIVE) {
                                            FlurryAgent.logEvent("SignIn Inactive Continue(Facebook)");
                                            Intent mainIntent = new Intent(SignInActivity.this, SignUpContinueActivity.class);
                                            mainIntent.setAction("refresh_fragment");
                                            startActivity(mainIntent);

                                            finish();
                                            return;
                                        }
                                    }

                                    Intent mainIntent = new Intent(SignInActivity.this, MainActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("class_type", "normal");
                                    mainIntent.putExtras(bundle);
                                    mainIntent.setAction("refresh_fragment");
                                    startActivity(mainIntent);

                                    finish();
                                } else {
                                    if (WelcomeTutorialActivity.mInstance != null) {
                                        WelcomeTutorialActivity.mInstance.finish();
                                    }
                                    if (WelcomeActivity.instance != null) {
                                        WelcomeActivity.instance.finish();
                                    }

                                    Intent trailIntent = new Intent(SignInActivity.this, SignUpContinueActivity.class);
                                    startActivity(trailIntent);
                                    finish();
                                }
                            }

                            @Override
                            public void onError(ServerError err) {
                                progressDialog.dismiss();

                                alertDialog.setTitle(getString(R.string.si_sign_in_error));
                                alertDialog.setMessage(err.getMessage());
                                alertDialog.show();
                            }
                        });
                    }

                    @Override
                    public void onLoginFail(int errorCode) {
                        alertDialog.setMessage(getString(R.string.si_sign_in_error));
                        alertDialog.show();
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

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                    }
                });
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            revokeAccess();
            GoogleSignInAccount sgi = result.getSignInAccount();
            if (sgi != null) {
                progressDialog.setMessage(getString(R.string.si_signing_in));
                progressDialog.show();
                int TYPE_FOR_GOOGLE = 2;
                mServerRequestManager.loginThirdPart(sgi.getGivenName(), sgi.getFamilyName(), sgi.getId(), String.valueOf(TYPE_FOR_GOOGLE), sgi.getEmail(), String.valueOf(sgi.getPhotoUrl()), new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        progressDialog.dismiss();

                        FlurryAgent.logEvent("Google Signed In Completed.");
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

                            String accountId = mSharedPreferences.getString("account_id", null);
                            String token = mSharedPreferences.getString("access_token", null);
                            if (!TextUtils.isEmpty(accountId) && !TextUtils.isEmpty(token)) {
                                AccountAdapter accountAdapter = new AccountAdapter(getApplicationContext());
                                Account account = accountAdapter.getAccountById(accountId);
                                if (account != null && account.getStatus() == Account.Status.INACTIVE) {
                                    FlurryAgent.logEvent("SignIn Inactive Continue(Google)");
                                    Intent mainIntent = new Intent(SignInActivity.this, SignUpContinueActivity.class);
                                    mainIntent.setAction("refresh_fragment");
                                    startActivity(mainIntent);

                                    finish();
                                    return;
                                }
                            }

                            mGoogleApiClient.disconnect();

                            Intent mainIntent = new Intent(SignInActivity.this, MainActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("class_type", "normal");
                            mainIntent.putExtras(bundle);
                            mainIntent.setAction("refresh_fragment");
                            startActivity(mainIntent);

                            finish();
                        } else {
                            mGoogleApiClient.disconnect();
                            if (WelcomeTutorialActivity.mInstance != null) {
                                WelcomeTutorialActivity.mInstance.finish();
                            }
                            if (WelcomeActivity.instance != null) {
                                WelcomeActivity.instance.finish();
                            }

                            Intent trailIntent = new Intent(SignInActivity.this, SignUpContinueActivity.class);
                            startActivity(trailIntent);
                            finish();
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        progressDialog.dismiss();

                        alertDialog.setTitle(getString(R.string.si_sign_in_error));
                        alertDialog.setMessage(err.getMessage());
                        alertDialog.show();
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.si_google_sign_in_error), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.si_google_result_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), getString(R.string.si_connection_error), Toast.LENGTH_SHORT).show();
    }
}
