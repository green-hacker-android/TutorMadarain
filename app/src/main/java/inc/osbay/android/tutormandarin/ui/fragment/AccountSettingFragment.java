package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flurry.android.FlurryAgent;

import java.util.Locale;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.ui.activity.WebViewActivity;
import inc.osbay.android.tutormandarin.ui.activity.WelcomeTutorialActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountSettingFragment extends BackHandledFragment implements View.OnClickListener {

    private static final String TAG = AccountSettingFragment.class.getSimpleName();

    private Account mAccount;

    private String mLocale;

    public AccountSettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AccountAdapter accountAdapter = new AccountAdapter(getActivity());
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String accountId = preferences.getString("account_id", "1");
        mAccount = accountAdapter.getAccountById(accountId);

        if (mAccount == null) {
            onBackPressed();
        }

        mLocale = Locale.getDefault().getLanguage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_account_setting, container, false);

        Toolbar toolBar;
        toolBar = rootView.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#34404E"));
        setSupportActionBar(toolBar);

        SimpleDraweeView sdvProfilePhoto = rootView.findViewById(R.id.sdv_profile_photo);
        if (!TextUtils.isEmpty(mAccount.getAvatar())) {
            sdvProfilePhoto.setImageURI(Uri.parse(mAccount.getAvatar()));
        }

        TextView tvProfileName = rootView.findViewById(R.id.tv_profile_name);
        tvProfileName.setText(mAccount.getFullName());

        TextView tvEmail = rootView.findViewById(R.id.tv_email);
        tvEmail.setText(mAccount.getEmail());

        TextView tvVersion = rootView.findViewById(R.id.tv_app_version);
        PackageManager pm = getActivity().getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getActivity().getPackageName(), 0);
            if (CommonConstant.SERVER_ID == 1) {
                tvVersion.setText(packageInfo.versionName + " " + getContext().getString(R.string.sandbox));
            } else {
                tvVersion.setText(packageInfo.versionName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "application not found. - " + getActivity().getPackageName(), e);
        }

        RelativeLayout rvAccountInfo = rootView.findViewById(R.id.rv_account_info);
        rvAccountInfo.setOnClickListener(this);

        RelativeLayout rlAboutUs = rootView.findViewById(R.id.rl_about_us);
        rlAboutUs.setOnClickListener(this);

        RelativeLayout rlWebSite = rootView.findViewById(R.id.rl_web_site);
        rlWebSite.setOnClickListener(this);

        RelativeLayout rlWalkThroughSetting = rootView.findViewById(R.id.rl_walk_through_setting);
        rlWalkThroughSetting.setOnClickListener(this);

        RelativeLayout rlRate = rootView.findViewById(R.id.rl_rate);
        rlRate.setOnClickListener(this);

        RelativeLayout rlFaqSetting = rootView.findViewById(R.id.rl_faq);
        rlFaqSetting.setOnClickListener(this);

        RelativeLayout rlTermOfUse = rootView.findViewById(R.id.rl_term_of_use);
        rlTermOfUse.setOnClickListener(this);

        RelativeLayout rlAcknowledgement = rootView.findViewById(R.id.rl_acknowledgement);
        rlAcknowledgement.setOnClickListener(this);

        RelativeLayout rlPrivacyPolicy = rootView.findViewById(R.id.rl_privacy_policy);
        rlPrivacyPolicy.setOnClickListener(this);

        RelativeLayout rlGroupBoard = rootView.findViewById(R.id.rl_group_board);
        rlGroupBoard.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        setTitle(getString(R.string.ac_setting_title));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStack();
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rv_account_info:
                Fragment accountInfo = new AccountInfoFragment();
                FragmentManager fm = getFragmentManager();
                Fragment frg = fm.findFragmentById(R.id.container);
                if (frg == null) {
                    fm.beginTransaction()
                            .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                    R.animator.fragment_out_new, R.animator.fragment_out_old)
                            .addToBackStack(null)
                            .add(R.id.container, accountInfo).commit();
                } else {
                    fm.beginTransaction()
                            .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                    R.animator.fragment_out_new, R.animator.fragment_out_old)
                            .addToBackStack(null)
                            .replace(R.id.container, accountInfo).commit();
                }
                break;
            case R.id.rl_about_us:
                Intent aboutUsIntent = new Intent(getActivity(), WebViewActivity.class);
                aboutUsIntent.putExtra(WebViewActivity.EXTRA_WEB_URL, changeWebsiteLocale("https://www.globaltravelassistant.com/en/", 1));

                startActivity(aboutUsIntent);
                break;
            case R.id.rl_web_site:
                Intent websiteIntent = new Intent(getActivity(), WebViewActivity.class);
                websiteIntent.putExtra(WebViewActivity.EXTRA_WEB_URL, changeWebsiteLocale("https://www.tutormandarin.net/en/", 2));
                startActivity(websiteIntent);
                break;
            case R.id.rl_walk_through_setting:
                Intent intent = new Intent(getActivity(), WelcomeTutorialActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_rate:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(CommonConstant.PLAY_STORE_APP_URL)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(CommonConstant.PLAY_STORE_WEB_URL)));
                }
                break;
            case R.id.rl_faq:
                FlurryAgent.logEvent("View FAQs");

                Intent faqIntent = new Intent(getActivity(), WebViewActivity.class);
                faqIntent.putExtra(WebViewActivity.EXTRA_WEB_URL, "http://www.tutormandarin.net/faq/");
                startActivity(faqIntent);
                break;
            case R.id.rl_term_of_use:
                FlurryAgent.logEvent("View terms of use");

                Intent termOfUseIntent = new Intent(getActivity(), WebViewActivity.class);
                termOfUseIntent.putExtra(WebViewActivity.EXTRA_WEB_URL, "http://service.tutormandarin.net/CommonHtml/TutorMandarin-TermsofUse.htm");
                startActivity(termOfUseIntent);
                break;
            case R.id.rl_acknowledgement:
                Intent acknowledgeIntent = new Intent(getActivity(), WebViewActivity.class);
                acknowledgeIntent.putExtra(WebViewActivity.EXTRA_WEB_URL, "http://service.tutormandarin.net/CommonHtml/TutorMandarin-Acknowledgements.htm");
                startActivity(acknowledgeIntent);
                break;
            case R.id.rl_privacy_policy:
                Intent privacyPolicyIntent = new Intent(getActivity(), WebViewActivity.class);
                privacyPolicyIntent.putExtra(WebViewActivity.EXTRA_WEB_URL, "http://service.tutormandarin.net/CommonHtml/TutorMandarin-PrivacyPolicy.htm");
                startActivity(privacyPolicyIntent);
                break;
            case R.id.rl_group_board:
                Intent groupBoardIntent = new Intent(getActivity(), WebViewActivity.class);
                groupBoardIntent.putExtra(WebViewActivity.EXTRA_WEB_URL, "https://www.groupboard.com");
                startActivity(groupBoardIntent);
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

    private String changeWebsiteLocale(String inputWebLink, int linkId) {
        String changeWebLink;
        switch (mLocale) {
            case "th":
                if (linkId == 1) {
                    changeWebLink = "https://www.globaltravelassistant.com/th/";
                } else {
                    changeWebLink = inputWebLink;
                }
                break;
            case "de":
                if (linkId == 2) {
                    changeWebLink = "https://www.tutormandarin.net/de/";
                } else {
                    changeWebLink = inputWebLink;
                }
                break;
            case "fr":
                if (linkId == 1) {
                    changeWebLink = "https://www.globaltravelassistant.com/fr/";
                } else {
                    changeWebLink = inputWebLink;
                }

                break;
            case "ja":
                if (linkId == 1) {
                    changeWebLink = "https://www.globaltravelassistant.com/ja/";
                } else if (linkId == 2) {
                    changeWebLink = "https://www.tutormandarin.net/ja/";
                } else {
                    changeWebLink = inputWebLink;
                }
                break;
            case "zh":
                if (linkId == 1) {
                    changeWebLink = "https://www.globaltravelassistant.com/cn/";
                } else if (linkId == 2) {
                    changeWebLink = "https://www.tutormandarin.net/zh/";
                } else {
                    changeWebLink = inputWebLink;
                }
                break;
            case "ko":
                if (linkId == 2) {
                    changeWebLink = "https://www.tutormandarin.net/ko/";
                } else {
                    changeWebLink = inputWebLink;
                }
                break;
            default:
                changeWebLink = inputWebLink;
                break;
        }
        return changeWebLink;
    }
}
