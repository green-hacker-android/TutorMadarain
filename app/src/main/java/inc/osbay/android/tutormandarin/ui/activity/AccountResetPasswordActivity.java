package inc.osbay.android.tutormandarin.ui.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.TMApplication;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.service.MessengerService;
import inc.osbay.android.tutormandarin.util.CommonUtil;
import inc.osbay.android.tutormandarin.util.WSMessageClient;

public class AccountResetPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String FIRST_TIME = "AccountResetPasswordActivity.FIRST_TIME";
    public static final String PWD_TIMER = "AccountResetPasswordActivity.PWD_TIMER";
    private EditText mEtConfirmNewPassword;
    private EditText mEtOldPassword;
    private EditText mEtNewPassword;
    private LinearLayout mLlNewPassword;
    private LinearLayout mLlOldPassword;
    private TextView mTvNewPassword;
    private TextView mTvOldPassword;
    private boolean isFirstTime;
    private SharedPreferences mSharedPreferences;
    private ServerRequestManager mServerRequestManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_reset_password);
        ImageView imvResetPasswordBack = findViewById(R.id.imv_reset_password_back);
        mEtOldPassword = findViewById(R.id.et_old_password);
        mEtOldPassword.setTypeface(Typeface.DEFAULT);
        mEtNewPassword = findViewById(R.id.et_new_password);
        mEtNewPassword.setTypeface(Typeface.DEFAULT);
        mEtConfirmNewPassword = findViewById(R.id.et_confirm_new_password);
        mEtConfirmNewPassword.setTypeface(Typeface.DEFAULT);
        Button btnResetPasswordSubmit = findViewById(R.id.btn_reset_password_submit);
        mLlNewPassword = findViewById(R.id.ll_new_password);
        mLlOldPassword = findViewById(R.id.ll_old_password);
        mTvNewPassword = findViewById(R.id.tv_new_password);
        mTvOldPassword = findViewById(R.id.tv_old_password);
        TextView tvAcForgotPassword = findViewById(R.id.tv_ac_forgot_password);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mServerRequestManager = new ServerRequestManager(this);

        imvResetPasswordBack.setOnClickListener(this);
        btnResetPasswordSubmit.setOnClickListener(this);
        tvAcForgotPassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_ac_forgot_password:
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
                                        final ProgressDialog progressDialog = new ProgressDialog(AccountResetPasswordActivity.this);
                                        progressDialog.setMessage(getString(R.string.si_dialog_forget_loading));
                                        progressDialog.show();
                                        dialogInterface.dismiss();
                                        tvInvalidEmail.setVisibility(View.GONE);
                                        isFirstTime = true;
                                        mServerRequestManager = new ServerRequestManager(AccountResetPasswordActivity.this);
                                        mServerRequestManager.forgotPassword(emailForgot, new ServerRequestManager.OnRequestFinishedListener() {
                                            @Override
                                            public void onSuccess(Object result) {
                                                progressDialog.dismiss();
                                                Date d = new Date();
                                                mSharedPreferences.edit().putLong(PWD_TIMER, d.getTime()).apply();
                                                mSharedPreferences.edit().putBoolean(FIRST_TIME, true).apply();

                                                AlertDialog.Builder builder = new AlertDialog.Builder(AccountResetPasswordActivity.this);
                                                builder.setTitle(getString(R.string.si_dialog_send_succeed));
                                                builder.setMessage(getString(R.string.si_dialog_send_succeed_txt));
                                                builder.setPositiveButton(getString(R.string.si_dialog_send_succeed_ok), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        logout();
                                                    }
                                                });
                                                AlertDialog alertDialog = builder.create();
                                                alertDialog.show();
                                            }

                                            @Override
                                            public void onError(ServerError err) {
                                                dialogInterface.dismiss();
                                                progressDialog.dismiss();

                                                View dialogErr = LayoutInflater.from(AccountResetPasswordActivity.this).inflate(R.layout.dialog_email_error, null);
                                                final EditText etEmailErr = dialogErr.findViewById(R.id.et_email_err);
                                                final TextView tvInvalidEmail = dialogErr.findViewById(R.id.tv_invalid_email);
                                                final AlertDialog forgotErrAlertDialog = new AlertDialog.Builder(AccountResetPasswordActivity.this)
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
                                                                    final ProgressDialog progressDialog1 = new ProgressDialog(AccountResetPasswordActivity.this);
                                                                    progressDialog1.setMessage(getString(R.string.si_dialog_email_error_loading));
                                                                    progressDialog1.show();
                                                                    dialogInterface2.dismiss();
                                                                    tvInvalidEmail.setVisibility(View.GONE);
                                                                    mServerRequestManager = new ServerRequestManager(AccountResetPasswordActivity.this);
                                                                    mServerRequestManager.forgotPassword(emailError, new ServerRequestManager.OnRequestFinishedListener() {
                                                                        @Override
                                                                        public void onSuccess(Object result) {
                                                                            progressDialog1.dismiss();
                                                                            Date d1 = new Date();
                                                                            mSharedPreferences.edit().putLong(PWD_TIMER, d1.getTime()).apply();
                                                                            mSharedPreferences.edit().putBoolean(FIRST_TIME, true).apply();
                                                                            AlertDialog.Builder builder = new AlertDialog.Builder(AccountResetPasswordActivity.this);
                                                                            builder.setTitle(getString(R.string.si_dialog_send_succeed));
                                                                            builder.setMessage(getString(R.string.si_dialog_send_succeed_txt));
                                                                            builder.setPositiveButton(getString(R.string.si_dialog_send_succeed_ok), new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                                    logout();
                                                                                }
                                                                            });
                                                                            AlertDialog alertDialog = builder.create();
                                                                            alertDialog.show();
                                                                        }

                                                                        @Override
                                                                        public void onError(ServerError err) {
                                                                            progressDialog1.dismiss();
                                                                            Toast.makeText(AccountResetPasswordActivity.this, err.getMessage(), Toast.LENGTH_SHORT).show();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(AccountResetPasswordActivity.this);
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
            case R.id.imv_reset_password_back:
                finish();
                break;
            case R.id.btn_reset_password_submit:
                String newPassword = mEtNewPassword.getText().toString();

                if (TextUtils.isEmpty(mEtOldPassword.getText().toString())) {
                    mLlNewPassword.setVisibility(View.GONE);
                    mLlOldPassword.setVisibility(View.VISIBLE);
                    mTvOldPassword.setText(getString(R.string.ac_old_password_empty));
                } else if (TextUtils.isEmpty(mEtNewPassword.getText().toString())) {
                    mLlOldPassword.setVisibility(View.GONE);
                    mTvNewPassword.setText(getString(R.string.ac_new_password_empty));
                    mLlNewPassword.setVisibility(View.VISIBLE);
                } else if (TextUtils.isEmpty(mEtConfirmNewPassword.getText().toString())) {
                    mLlOldPassword.setVisibility(View.GONE);
                    mTvNewPassword.setText(R.string.ac_confirm_password_empty);
                    mLlNewPassword.setVisibility(View.VISIBLE);
                } else if (CommonUtil.validatePassword(newPassword)) {
                    mLlNewPassword.setVisibility(View.GONE);

                    if (mEtNewPassword.getText().toString().equals(mEtConfirmNewPassword.getText().toString())) {
                        ServerRequestManager serverRequestManager = new ServerRequestManager(getApplicationContext());
                        serverRequestManager.changePassword(mEtOldPassword.getText().toString(), mEtNewPassword.getText().toString(), new ServerRequestManager.OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                if (AccountResetPasswordActivity.this != null) {
                                    finish();
                                }
                            }

                            @Override
                            public void onError(ServerError err) {
                                if (AccountResetPasswordActivity.this != null) {
                                    mLlOldPassword.setVisibility(View.VISIBLE);
                                    mTvOldPassword.setText(getString(R.string.ac_old_password_error));
                                }
                            }
                        });
                    } else {
                        mTvNewPassword.setText(getString(R.string.ac_no_match_password));
                        mLlNewPassword.setVisibility(View.VISIBLE);
                    }
                } else {
                    mTvNewPassword.setText(getString(R.string.ac_invalid_password));
                    mLlNewPassword.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void logout() {
        Map<String, String> params = new HashMap<>();
        params.put("Reset Password LogOut Page", AccountResetPasswordActivity.class.getSimpleName());
        FlurryAgent.logEvent("Close App", params);

        mSharedPreferences.edit().remove("access_token").apply();
        mSharedPreferences.edit().remove("account_id").apply();
        mSharedPreferences.edit().remove("PromoCodeFragment.MY_PROMO_CODE_PREF").apply();
        mSharedPreferences.edit().remove(CommonConstant.AGENT_PROMO_CODE_PREF).apply();
        mSharedPreferences.edit().remove(CommonConstant.USE_PROMO_CODE_PREF).apply();

        AccountAdapter adapter = new AccountAdapter(this);
        adapter.deleteAllTableData();

        Message message = Message.obtain(null, MessengerService.MSG_WS_LOGOUT);
        try {
            WSMessageClient wsMessageClient = ((TMApplication) getApplication()).getWSMessageClient();
            wsMessageClient.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ActivityCompat.finishAffinity(this);

        Intent intent = new Intent(AccountResetPasswordActivity.this, WelcomeActivity.class);
        startActivity(intent);
    }
}
