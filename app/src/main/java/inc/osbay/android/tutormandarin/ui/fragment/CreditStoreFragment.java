package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.ads.conversiontracking.AdWordsConversionReporter;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.ui.activity.PayPalPaymentActivity;
import inc.osbay.android.tutormandarin.ui.activity.TrialSubmitActivity;
import inc.osbay.android.tutormandarin.ui.activity.WebViewActivity;

public class CreditStoreFragment extends BackHandledFragment implements View.OnClickListener {

    ServerRequestManager requestManager;
    int minAmount = 4;
    private SharedPreferences mPreferences;
    private TextView mMyCreditTextView;
    private TextView mSelectedCreditTextView;
    private TextView mTotalCostTextView;
    private SeekBar mSelectCreditSeekBar;
    private Timer mTimer;
    private String mLocale;
    private Account mAccount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestManager = new ServerRequestManager(
                getActivity().getApplicationContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_credit_store, container, false);

        Toolbar toolBar;
        toolBar = rootView.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#e45f56"));
        setSupportActionBar(toolBar);

        mLocale = Locale.getDefault().getLanguage();

        mMyCreditTextView = rootView.findViewById(R.id.tv_my_credit);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String accountId = mPreferences.getString("account_id", "1");
        Account account = new AccountAdapter(getActivity()).getAccountById(accountId);

        if (account != null) {
            mMyCreditTextView.setText(String.format(Locale.getDefault(), "%.1f", account.getCredit()));
        }

        Button btnContactNow = rootView.findViewById(R.id.btn_contact_now);
        btnContactNow.setOnClickListener(this);

        mSelectedCreditTextView = rootView.findViewById(R.id.tv_selected_credit);
        mTotalCostTextView = rootView.findViewById(R.id.tv_total_cost);

        TextView tvPricingTier = rootView.findViewById(R.id.tv_pricing_tier);
        SpannableString content = new SpannableString(getString(R.string.cs_whats_price));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        tvPricingTier.setText(content);
        tvPricingTier.setOnClickListener(this);

        Button btnBuyCredit = rootView.findViewById(R.id.btn_buy_credits);
        btnBuyCredit.setOnClickListener(this);

        ImageView imvRefreshCredit = rootView.findViewById(R.id.imv_refresh_credit);
        imvRefreshCredit.setOnClickListener(this);

        mSelectCreditSeekBar = rootView.findViewById(R.id.sb_select_credit);
        mSelectCreditSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int progress = i;
                if (progress < minAmount) {
                    progress = minAmount;
                    seekBar.setProgress(progress);
                }
                mSelectedCreditTextView.setText(String.format(Locale.getDefault(), "%s " +
                                getString(R.string.cs_credits),
                        progress));

                double amount = progress * 10.0;

                mTotalCostTextView.setText(String.format(Locale.getDefault(), getString(R.string.cs_total_price)
                        + " %.2f", amount));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mSelectCreditSeekBar.setProgress(minAmount);
        mSelectedCreditTextView.setText(String.format(Locale.getDefault(), "%s " + getString(R
                        .string.cs_credits),
                minAmount));
        mTotalCostTextView.setText(String.format(Locale.getDefault(), getString(R.string.cs_total_price) + " " +
                "%.2f", minAmount *
                10.0));

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        setTitle(getString(R.string.cs_title));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.cs_minimum_credit_loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        requestManager.getMinimumCreditAmount(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                if (getActivity() != null) {
                    progressDialog.dismiss();
                    minAmount = (int) result;
                    mSelectCreditSeekBar.setProgress(minAmount);
                }
            }

            @Override
            public void onError(ServerError err) {
                if (getActivity() != null) {
                    progressDialog.dismiss();
                    minAmount = 4;
                    mSelectCreditSeekBar.setProgress(minAmount);
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        mTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                requestManager.refreshStudentBalance(new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        if (getActivity() != null) {
                            mMyCreditTextView.setText(String.format(Locale.getDefault(),
                                    "%.1f", result));
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        // ignore
                    }
                });
            }
        };

        mTimer.schedule(task, 2000, 2000);
    }

    @Override
    public void onPause() {
        super.onPause();
        mTimer.cancel();
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStack();
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_pricing_tier:
                FlurryAgent.logEvent("Click pricing tiers");

                Intent pricingIntent = new Intent(getActivity(), WebViewActivity.class);
                pricingIntent.putExtra(WebViewActivity.EXTRA_WEB_URL, changePricingLinkLocale("https://www.tutormandarin.net/en/pricing/"));
                startActivity(pricingIntent);
                break;
            case R.id.btn_buy_credits:
                FlurryAgent.logEvent("Click buy credits");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                String token = prefs.getString("access_token", null);
                String accountId = prefs.getString("account_id", null);

                if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
                    AccountAdapter accountAdapter = new AccountAdapter(getContext());
                    mAccount = accountAdapter.getAccountById(accountId);
                }

                // Clicking Buy Credit

                if (mAccount != null &&
                        mAccount.getStatus() != Account.Status.ACTIVE &&
                        mAccount.getStatus() != Account.Status.TRIAL &&
                        mAccount.getStatus() != Account.Status.NO_TRIAL_ACTIVE) {
                    new AlertDialog.Builder(getContext())
                            .setCancelable(false)
                            .setTitle(getString(R.string.giveup))
                            .setMessage(getString(R.string.giving_up_trial))
                            .setPositiveButton(getString(R.string.giveup), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    requestManager.giveUpTrial(new ServerRequestManager.OnRequestFinishedListener() {
                                        @Override
                                        public void onSuccess(Object result) {
                                            if (getActivity() != null) {
                                                //Update Student Info
                                                FlurryAgent.logEvent("Giveup trial");
                                                getStudentInfo();
                                            }
                                        }

                                        @Override
                                        public void onError(ServerError err) {
                                            // ignore
                                        }
                                    });
                                }
                            })
                            .setNegativeButton(getString(R.string.keep_trial), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getActivity().finish();
                                    Intent trialSubmit = new Intent(getActivity(), TrialSubmitActivity.class);
                                    startActivity(trialSubmit);
                                }
                            })
                            .show();
                    //return;
                }
                /*** Not showing Giving up Trial Dialog for 3 Status***/
                if (mAccount != null &&
                        (mAccount.getStatus() == Account.Status.TRIAL
                                || mAccount.getStatus() == Account.Status.ACTIVE
                                || mAccount.getStatus() == Account.Status.NO_TRIAL_ACTIVE)) {
                    openPaypalActivity();
                }
                break;

            case R.id.btn_contact_now:
                FlurryAgent.logEvent("Click contact now (Credit Store)");

                FragmentManager fm = getFragmentManager();
                Fragment frg = fm.findFragmentById(R.id.container);
                Fragment onlineSupportFragment = new OnlineSupportFragment();
                Bundle bundle = new Bundle();
                bundle.putString("class_type", "normal");
                onlineSupportFragment.setArguments(bundle);
                if (frg == null) {
                    fm.beginTransaction()
                            .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                    R.animator.fragment_out_new, R.animator.fragment_out_old)
                            .addToBackStack(null)
                            .add(R.id.container, onlineSupportFragment).commit();
                } else {
                    fm.beginTransaction()
                            .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                    R.animator.fragment_out_new, R.animator.fragment_out_old)
                            .addToBackStack(null)
                            .replace(R.id.container, onlineSupportFragment).commit();
                }
                break;
            case R.id.imv_refresh_credit:
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage(getString(R.string.cs_refresh_loading));
                progressDialog.show();

                requestManager.refreshStudentBalance(new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        progressDialog.dismiss();

                        if (getActivity() != null) {
                            mMyCreditTextView.setText(String.format(Locale.getDefault(),
                                    "%.1f", result));
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        progressDialog.dismiss();
                    }
                });
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    private String changePricingLinkLocale(String inputWebLink) {
        String changeWebLink;
        switch (mLocale) {
            case "de":
                changeWebLink = "https://www.tutormandarin.net/de/preise/";
                break;
            case "ja":
                changeWebLink = "https://www.tutormandarin.net/ja/price/";
                break;
            case "ko":
                changeWebLink = "https://www.tutormandarin.net/ko/pricing-2/";
                break;
            default:
                changeWebLink = inputWebLink;
                break;
        }
        return changeWebLink;
    }

    private void openPaypalActivity() {
        AdWordsConversionReporter.reportWithConversionId(getActivity().getApplicationContext(),
                "944170633", "RqKrCOeThm8Qic2bwgM", "0.00", true);

        Intent intent = new Intent(getActivity(), PayPalPaymentActivity.class);
        intent.putExtra(PayPalPaymentActivity.EXTRA_ACCOUNT_ID, mPreferences.getString("account_id", "1"));
        intent.putExtra(PayPalPaymentActivity.EXTRA_CREDIT_AMOUNT, mSelectCreditSeekBar.getProgress());
        startActivity(intent);
    }

    private void getStudentInfo() {
        requestManager.getStudentInfo(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                openPaypalActivity();
            }

            @Override
            public void onError(ServerError err) {
                if (getActivity() != null) {
                    getStudentInfo();
                    Toast.makeText(getActivity(), err.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
