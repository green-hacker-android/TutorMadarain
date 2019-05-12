package inc.osbay.android.tutormandarin.ui.activity;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;
import inc.osbay.android.tutormandarin.ui.fragment.SignUpGoalFragment;
import inc.osbay.android.tutormandarin.ui.fragment.SignUpInterestFragment;
import inc.osbay.android.tutormandarin.ui.fragment.SignUpLevelFragment;
import inc.osbay.android.tutormandarin.util.PromoCodeDialog;

public class SignUpContinueActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = SignUpContinueActivity.class.getSimpleName();

    public String level = "B1";
    public String goals = "";
    public String topics = "";
    public String courseCategory = "1";
    private int mSignUpStep;
    private TextView mStepTextView;
    private LinearLayout mBackLinearLayout;
    private RelativeLayout mRlSuPromoCode;
    private RelativeLayout mRlSuPromoCodeBlur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_continue);

        FlurryAgent.logEvent("SignUpContinue Reach");

        Toolbar toolbar;
        toolbar = findViewById(R.id.tool_bar);
        toolbar.setBackgroundColor(Color.parseColor("#E45F56"));
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(
                this);

        String agentPromoCode = preferences.getString(CommonConstant.AGENT_PROMO_CODE_PREF, "");
        boolean isUsedPromoCode = preferences.getBoolean(CommonConstant.USE_PROMO_CODE_PREF, false);

        if (!TextUtils.isEmpty(agentPromoCode) && !isUsedPromoCode) {
            ServerRequestManager requestManager = new ServerRequestManager(getApplicationContext());
            requestManager.checkPromotionCode(agentPromoCode, new ServerRequestManager.OnRequestFinishedListener() {
                @Override
                public void onSuccess(Object result) {
                    preferences.edit().putBoolean(CommonConstant.USE_PROMO_CODE_PREF, true).apply();
                }

                @Override
                public void onError(ServerError err) {
                    Log.e(TAG, err.getMessage());
                }
            });
        }


        TextView tvTitle = toolbar.findViewById(R.id.tv_toolbar_title);
        tvTitle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Montserrat-Bold.ttf"));
        tvTitle.setText(getString(R.string.su_tutormandarin_account));
        tvTitle.setCompoundDrawables(null, null, null, null);
        tvTitle.setOnClickListener(null);

        mStepTextView = findViewById(R.id.tv_profile_step);
        mBackLinearLayout = findViewById(R.id.ll_back);
        mRlSuPromoCode = findViewById(R.id.rl_su_promo_code);
        mRlSuPromoCodeBlur = findViewById(R.id.rl_su_promo_code_blur);

        TextView tvIHavePromoCode = findViewById(R.id.tv_i_have_promo_code);
        LinearLayout nextLinearLayout = findViewById(R.id.ll_next);

        mBackLinearLayout.setOnClickListener(this);
        nextLinearLayout.setOnClickListener(this);
        findViewById(R.id.imv_profile_info).setOnClickListener(this);
        tvIHavePromoCode.setOnClickListener(this);

        mSignUpStep = 1;
        updateNavigation();
    }

    private void updateNavigation() {
        mStepTextView.setText(getString(R.string.su_step_of_three, mSignUpStep));

        if (mSignUpStep > 1) {
            mBackLinearLayout.setVisibility(View.VISIBLE);
        } else {
            mBackLinearLayout.setVisibility(View.INVISIBLE);
        }

        Fragment newFragment = null;
        if (mSignUpStep == 1) {
            newFragment = new SignUpLevelFragment();
        } else if (mSignUpStep == 2) {
            newFragment = new SignUpGoalFragment();
        } else if (mSignUpStep == 3) {
            newFragment = new SignUpInterestFragment();
        }

        if (newFragment == null)
            return;

        FragmentManager fm = getFragmentManager();
        Fragment old = fm.findFragmentById(R.id.fl_sign_up);
        if (old == null) {
            fm.beginTransaction()
                    .add(R.id.fl_sign_up, newFragment)
                    .commit();
        } else {
            fm.beginTransaction()
                    .replace(R.id.fl_sign_up, newFragment)
                    .commit();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imv_profile_info:
                if (mSignUpStep == 1) {
                    new AlertDialog.Builder(SignUpContinueActivity.this)
                            .setTitle(getString(R.string.su_chinese_level_title))
                            .setMessage(getString(R.string.su_chinese_level_msg))
                            .setPositiveButton(getString(R.string.su_chinese_level_ok), null)
                            .create()
                            .show();
                } else if (mSignUpStep == 2) {
                    new AlertDialog.Builder(SignUpContinueActivity.this)
                            .setTitle(getString(R.string.su_your_goals_title))
                            .setMessage(getString(R.string.su_your_goals_msg))
                            .setPositiveButton(getString(R.string.su_your_goals_ok), null)
                            .create()
                            .show();
                } else if (mSignUpStep == 3) {
                    new AlertDialog.Builder(SignUpContinueActivity.this)
                            .setTitle(getString(R.string.su_your_interests_title))
                            .setMessage(getString(R.string.su_your_interests_msg))
                            .setPositiveButton(getString(R.string.su_your_interests_ok), null)
                            .create()
                            .show();
                }
                break;
            case R.id.ll_back:
                if (mSignUpStep > 1) {
                    mSignUpStep--;
                    updateNavigation();
                }
                break;
            case R.id.ll_next:
                if ((mSignUpStep == 2 && TextUtils.isEmpty(goals)) ||
                        (mSignUpStep == 3 && TextUtils.isEmpty(topics))) {
                    new AlertDialog.Builder(SignUpContinueActivity.this)
                            .setTitle(getString(R.string.su_choose_request_notice))
                            .setMessage(getString(R.string.su_choose_request_msg))
                            .setPositiveButton(getString(R.string.su_choose_request_ok), null)
                            .create()
                            .show();
                    return;
                }

                if (mSignUpStep < 3) {
                    mSignUpStep++;
                    updateNavigation();
                } else if (mSignUpStep == 3) {
                    final ProgressDialog progressDialog = new ProgressDialog(SignUpContinueActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage(getString(R.string.su_updating));
                    progressDialog.show();

                    final ServerRequestManager requestManager = new ServerRequestManager(getApplicationContext());
                    requestManager.signUpPartTwo(level, topics, goals, courseCategory, new ServerRequestManager.OnRequestFinishedListener() {
                        @Override
                        public void onSuccess(Object result) {
                            requestManager.getStudentInfo(new ServerRequestManager.OnRequestFinishedListener() {
                                @Override
                                public void onSuccess(Object result) {
                                    progressDialog.dismiss();
                                    FlurryAgent.logEvent("SignUpPartTwo Success, Go To TrialSubmit");
                                    Intent trialSubmit = new Intent(SignUpContinueActivity.this, TrialSubmitActivity.class);
                                    startActivity(trialSubmit);
                                    SignUpContinueActivity.this.finish();
                                }

                                @Override
                                public void onError(ServerError err) {
                                    progressDialog.dismiss();
                                    FlurryAgent.logEvent("SignUpPartTwo Error, Go To TrialSubmit");

                                    Intent trialSubmit = new Intent(SignUpContinueActivity.this, TrialSubmitActivity.class);
                                    startActivity(trialSubmit);
                                    SignUpContinueActivity.this.finish();
                                }
                            });
                        }

                        @Override
                        public void onError(ServerError err) {
                            progressDialog.dismiss();

                            new AlertDialog.Builder(SignUpContinueActivity.this)
                                    .setTitle(getString(R.string.su_sign_up_error))
                                    .setMessage(err.getMessage())
                                    .setPositiveButton(getString(R.string.su_dialog_ok), null)
                                    .create()
                                    .show();
                        }
                    });
                }
                break;
            case R.id.tv_i_have_promo_code:
                if (mSignUpStep == 1)
                    mRlSuPromoCodeBlur.setBackground(getResources().getDrawable(R.drawable.bg_su_promo_1));
                else if (mSignUpStep == 2)
                    mRlSuPromoCodeBlur.setBackground(getResources().getDrawable(R.drawable.bg_su_promo_2));
                else if (mSignUpStep == 3)
                    mRlSuPromoCodeBlur.setBackground(getResources().getDrawable(R.drawable.bg_su_promo_3));
                else
                    mRlSuPromoCodeBlur.setBackground(null);

                PromoCodeDialog promoCodeDialog = new PromoCodeDialog();
                promoCodeDialog.havePromoCode(this, mRlSuPromoCode, mRlSuPromoCodeBlur);
                break;
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
