package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flurry.android.FlurryAgent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimerTask;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.TMApplication;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.service.MessengerService;
import inc.osbay.android.tutormandarin.ui.activity.FragmentHolderActivity;
import inc.osbay.android.tutormandarin.ui.activity.MainActivity;
import inc.osbay.android.tutormandarin.ui.activity.ShareActivity;
import inc.osbay.android.tutormandarin.ui.activity.WebViewActivity;
import inc.osbay.android.tutormandarin.ui.activity.WelcomeActivity;
import inc.osbay.android.tutormandarin.ui.activity.WelcomeTutorialActivity;
import inc.osbay.android.tutormandarin.util.CommonUtil;
import inc.osbay.android.tutormandarin.util.WSMessageClient;

public class LeftMenuDrawerFragment extends Fragment implements View.OnClickListener {

    private ImageView imbSetting;
    // Show/Hide these views for
    private LinearLayout mLoggedInLinearLayout;
    private LinearLayout mSignOutLinearLayout;
    private LinearLayout mCreditsLinearLayout;
    private SimpleDraweeView mProfilePhotoDraweeView;
    private TextView mProfileNameTextView;
    private TextView mCreditTextView;
    private TextView mEmailTextView;
    private TextView mTvComingUpDate;
    private TextView mTvComingUpTime;
    private TextView mTvLeftMyLevel;
    private TextView mTvLeftMyNotes;
    private TextView mTvLeftMyBadges;
    private TextView mTvNotiCount;
    private Button mReferFriendButton;
    private String mLocale;

    private AccountAdapter accountAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(
                R.layout.fragment_left_menu_drawer, container, false);
        mLocale = Locale.getDefault().getLanguage();

        mReferFriendButton = rootView.findViewById(R.id.btn_refer_friend);
        mReferFriendButton.setOnClickListener(this);

        LinearLayout llProfile = rootView.findViewById(R.id.ll_account_profile);
        llProfile.setOnClickListener(this);

        LinearLayout llComingUp = rootView.findViewById(R.id.ll_upcoming);
        llComingUp.setOnClickListener(this);

        RelativeLayout rlUpdateNews = rootView.findViewById(R.id.rl_update_news);
        rlUpdateNews.setOnClickListener(this);

        RelativeLayout rlWhatsPricing = rootView.findViewById(R.id.rl_whats_pricing);
        rlWhatsPricing.setOnClickListener(this);

        imbSetting = rootView.findViewById(R.id.imb_setting);
        imbSetting.setOnClickListener(this);

        mLoggedInLinearLayout = rootView.findViewById(R.id.ll_logged_in);
        mCreditsLinearLayout = rootView.findViewById(R.id.ll_credits);

        // TODO: 7/11/2016
        LinearLayout llResume = rootView.findViewById(R.id.ll_resume);
        llResume.setOnClickListener(this);

        mSignOutLinearLayout = rootView.findViewById(R.id.ll_sign_out);
        mSignOutLinearLayout.setOnClickListener(this);

        mProfilePhotoDraweeView = rootView.findViewById(R.id.sdv_profile_photo);
        mProfileNameTextView = rootView.findViewById(R.id.tv_profile_name);
        mCreditTextView = rootView.findViewById(R.id.tv_my_credit);
        mEmailTextView = rootView.findViewById(R.id.tv_email);
        mTvComingUpDate = rootView.findViewById(R.id.tv_upcoming_date);
        mTvComingUpTime = rootView.findViewById(R.id.tv_upcoming_time);
        mTvLeftMyLevel = rootView.findViewById(R.id.tv_left_my_level);
        mTvLeftMyNotes = rootView.findViewById(R.id.tv_left_my_notes);
        mTvLeftMyBadges = rootView.findViewById(R.id.tv_left_my_badges);
        mTvNotiCount = rootView.findViewById(R.id.tv_noti_count);
        RelativeLayout rlWalkThrough = rootView.findViewById(R.id.rl_walk_through);
        rlWalkThrough.setOnClickListener(this);
        RelativeLayout rlFreeTrial = rootView.findViewById(R.id.rl_understand_free_trial);
        rlFreeTrial.setOnClickListener(this);
        accountAdapter = new AccountAdapter(getActivity().getApplicationContext());

        refresh();

        return rootView;
    }

    public String replaceLevelString(String inputValue) {
        inputValue = inputValue.replace(" ", "");
        inputValue = inputValue.replace("Beginner", "B");
        inputValue = inputValue.replace("Intermediate", "I");
        inputValue = inputValue.replace("Advanced", "A");
        return inputValue;
    }


    public void refresh() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new TimerTask() {
                @Override
                public void run() {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String token = prefs.getString("access_token", null);
                    String accountId = prefs.getString("account_id", null);

                    if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
                        mLoggedInLinearLayout.setVisibility(View.VISIBLE);
                        mSignOutLinearLayout.setVisibility(View.VISIBLE);
                        mCreditsLinearLayout.setVisibility(View.VISIBLE);
                        mReferFriendButton.setVisibility(View.VISIBLE);
                        mEmailTextView.setVisibility(View.VISIBLE);
                        imbSetting.setVisibility(View.VISIBLE);

                        Account account = accountAdapter.getAccountById(accountId);

                        if (account != null) {
                            if (!TextUtils.isEmpty(account.getAvatar())) {
                                mProfilePhotoDraweeView.setImageURI(Uri.parse(account.getAvatar()));
                            }

                            mProfileNameTextView.setText(account.getFullName());
                            mCreditTextView.setText(String.format(Locale.getDefault(), "%.1f",
                                    account.getCredit()));
                            mEmailTextView.setText(account.getEmail());
                            mTvLeftMyLevel.setText(replaceLevelString(account.getChineseLevel()));

                            String comingUpDate = accountAdapter.getUpComingDate(50);
                            if (!TextUtils.isEmpty(comingUpDate)) {
                                Date date = null;
                                try {
                                    date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(comingUpDate);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                mTvComingUpTime.setVisibility(View.VISIBLE);
                                mTvComingUpTime.setText(String.format(Locale.getDefault(), "%s~%s",
                                        CommonUtil.getCustomDateResult(comingUpDate, "yyyy-MM-dd HH:mm:ss", "HH:mm"),
                                        CommonUtil.getFormattedDate(new Date(date.getTime() + 3000000), "HH:mm", false)));
                                mTvComingUpDate.setText(CommonUtil.getCustomFormattedDate(
                                        CommonUtil.convertStringToDate(comingUpDate, "yyyy-MM-dd HH:mm:ss"), "MMMM, dd", true));
                            } else {
                                mTvComingUpTime.setVisibility(View.GONE);
                                mTvComingUpDate.setText(getString(R.string.lmd_no_upcoming));
                            }
                            String note = accountAdapter.getLastNote();
                            if (note != null) {
                                mTvLeftMyNotes.setText(note);
                            } else {
                                mTvLeftMyNotes.setText("-");
                            }

                            int badges = accountAdapter.getBadgesCount();
                            mTvLeftMyBadges.setText(String.valueOf(badges));

                            int notiCount = accountAdapter.getNotiCount();
                            if (notiCount == 0) {
                                mTvNotiCount.setVisibility(View.INVISIBLE);
                            } else {
                                mTvNotiCount.setText(String.valueOf(notiCount));
                                mTvNotiCount.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        mProfilePhotoDraweeView.setImageURI("");

                        mLoggedInLinearLayout.setVisibility(View.GONE);
                        mSignOutLinearLayout.setVisibility(View.INVISIBLE);
                        mCreditsLinearLayout.setVisibility(View.GONE);
                        mReferFriendButton.setVisibility(View.GONE);
                        mEmailTextView.setVisibility(View.GONE);
                        imbSetting.setVisibility(View.GONE);

                        mProfileNameTextView.setText(getString(R.string.lmd_sign_up_sign_in));
                    }
                }
            });

        }
    }

    @Override
    public void onClick(View view) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String token = prefs.getString("access_token", null);
        String accountId = prefs.getString("account_id", null);

        final FragmentManager fm = getParentFragment().getFragmentManager();
        final Fragment currentFragment = fm.findFragmentById(R.id.container);
        Fragment newFragment = null;

        switch (view.getId()) {
            case R.id.rl_whats_pricing:
                Intent pricingIntent = new Intent(getActivity(), WebViewActivity.class);
                pricingIntent.putExtra(WebViewActivity.EXTRA_WEB_URL, changePricingLinkLocale("https://www.tutormandarin.net/en/pricing/"));
                startActivity(pricingIntent);
                break;
            case R.id.btn_refer_friend:
                Account account = accountAdapter.getAccountById(accountId);

                Intent intent = new Intent(getActivity(), ShareActivity.class);
                intent.putExtra(ShareActivity.EXTRA_SHARE_ITEM, getString(R.string.lmd_refer_a_friend));
                intent.putExtra(ShareActivity.EXTRA_TITLE, account.getFullName() + getString(R.string.lmd_share_extra_title));
                intent.putExtra(ShareActivity.EXTRA_CONTENT, getString(R.string.lmd_share_extra_content));
                intent.putExtra(ShareActivity.EXTRA_IMAGE_URL, "http://sandboxv2website.lingo.chat/App_Themes/Image/TutorMandarin.png");
                intent.putExtra(ShareActivity.EXTRA_EMAIL_INCLUDE, true);

                startActivity(intent);
                break;
            case R.id.imb_setting:
                if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
                    newFragment = new AccountSettingFragment();
                }
                break;
            case R.id.ll_resume:
                if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
                    newFragment = new ResumeFragment();
                }
                break;
            case R.id.ll_account_profile:
                if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
                    newFragment = new AccountInfoFragment();
                }
                break;
            case R.id.ll_sign_out:
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.lmd_sign_out))
                        .setMessage(getString(R.string.lmd_sign_out_msg))
                        .setNegativeButton(getString(R.string.lmd_sign_out_cancel), null)
                        .setPositiveButton(getString(R.string.lmd_btn_sign_out), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                Map<String, String> params = new HashMap<>();
                                params.put("Last Page", currentFragment.getClass().getSimpleName());
                                FlurryAgent.logEvent("Close App", params);

                                prefs.edit().remove("access_token").apply();
                                prefs.edit().remove("account_id").apply();
                                prefs.edit().remove("PromoCodeFragment.MY_PROMO_CODE_PREF").apply();
                                prefs.edit().remove(CommonConstant.AGENT_PROMO_CODE_PREF).apply();
                                prefs.edit().remove(CommonConstant.USE_PROMO_CODE_PREF).apply();

                                AccountAdapter adapter = new AccountAdapter(getActivity());
                                adapter.deleteAllTableData();
                                refresh();

                                Message message = Message.obtain(null, MessengerService.MSG_WS_LOGOUT);
                                try {
                                    WSMessageClient wsMessageClient = ((TMApplication) getActivity().getApplication()).getWSMessageClient();
                                    wsMessageClient.sendMessage(message);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

//                                final FragmentTransaction ft = fm.beginTransaction();
//                                ft.detach(currentFragment);
//                                ft.attach(currentFragment);
//                                ft.commit();
                                startActivity(new Intent(getActivity(), WelcomeActivity.class));
                                getActivity().finish();
                            }
                        })
                        .show();
                return;
            case R.id.ll_upcoming:
                FlurryAgent.logEvent("Click coming up (Dashboard)");

                if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
                    newFragment = new ScheduleDetailFragment();

                    Bundle bundle = new Bundle();
                    bundle.putBoolean(ScheduleDetailFragment.EXTRA_OPEN_UPCOMING, true);
                    newFragment.setArguments(bundle);
                }
                break;
            case R.id.rl_update_news:
                if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
                    newFragment = new NotiListFragment();
                }
                break;

            case R.id.rl_walk_through:
                FlurryAgent.logEvent("Click walk through (Dashboard)");

                Intent walkThroughIntent = new Intent(getActivity(), WelcomeTutorialActivity.class);

                ((MainActivity) getActivity()).closeDrawers();

                //tod
                startActivity(walkThroughIntent);
                break;

            case R.id.rl_understand_free_trial:
                Intent trailIntent = new Intent(getActivity(), FragmentHolderActivity.class);
                trailIntent.putExtra(FragmentHolderActivity.EXTRA_DISPLAY_FRAGMENT, TrialExplainFragment.class.getSimpleName());
                startActivity(trailIntent);
                break;
        }

        ((MainFragment) getParentFragment()).closeDrawer();

        if (newFragment != null) {
            if (currentFragment != null) {
                fm.beginTransaction()
                        .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                R.animator.fragment_out_new, R.animator.fragment_out_old)
                        .addToBackStack(null)
                        .replace(R.id.container, newFragment).commit();
            } else {
                fm.beginTransaction()
                        .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                R.animator.fragment_out_new, R.animator.fragment_out_old)
                        .addToBackStack(null)
                        .add(R.id.container, newFragment).commit();
            }
        }
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
}
