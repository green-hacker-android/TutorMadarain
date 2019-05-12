package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.plus.PlusShare;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.util.Locale;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;
import inc.osbay.android.tutormandarin.ui.activity.WebViewActivity;
import inc.osbay.android.tutormandarin.util.PromoCodeDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class PromoCodeFragment extends BackHandledFragment implements View.OnClickListener {
    public static final String MY_PROMO_CODE_PREF = "PromoCodeFragment.MY_PROMO_CODE_PREF";

    private Dialog dialog;

    private ServerRequestManager mServerRequestManager;

    private RelativeLayout mRlPromoCode;

    private RelativeLayout mRlPromoCodeBlur;

    private SharedPreferences mSharedPreferences;

    private String mMyPromoCode;

    private String mLocale;

    public PromoCodeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mServerRequestManager = new ServerRequestManager(getActivity());
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mLocale = Locale.getDefault().getLanguage();
    }

    @Override
    public void onStart() {
        super.onStart();
        setTitle(getString(R.string.pc_promo_code_title));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_promo_code, container, false);

        Toolbar toolBar;
        toolBar = v.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#34404E"));
        setSupportActionBar(toolBar);

        TextView tvReferAFriend = v.findViewById(R.id.tv_refer_a_friend);
        TextView tvHavePromoCode = v.findViewById(R.id.tv_have_promo_code);
        final TextView tvMyPromoCode = v.findViewById(R.id.tv_my_promo_code);
        TextView tvMyPromoCodeExplanation = v.findViewById(R.id.tv_my_promo_code_explanation);

        tvReferAFriend.setOnClickListener(this);
        tvHavePromoCode.setOnClickListener(this);

        mRlPromoCode = v.findViewById(R.id.rl_promo_code);
        mRlPromoCodeBlur = v.findViewById(R.id.rl_promo_code_blur);

        mMyPromoCode = mSharedPreferences.getString(MY_PROMO_CODE_PREF, null);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.pc_get_promo_code_loading));
        progressDialog.show();

        if (mMyPromoCode == null) {
            mServerRequestManager.getPromoCode(new ServerRequestManager.OnRequestFinishedListener() {
                @Override
                public void onSuccess(Object result) {
                    progressDialog.dismiss();
                    mMyPromoCode = (String) result;
                    mSharedPreferences.edit().putString(MY_PROMO_CODE_PREF, mMyPromoCode).apply();
                    tvMyPromoCode.setText(mMyPromoCode);
                }

                @Override
                public void onError(ServerError err) {
                    progressDialog.dismiss();
                    mMyPromoCode = err.getMessage();
                    tvMyPromoCode.setText(mMyPromoCode);
                }
            });
        } else {
            progressDialog.dismiss();
            tvMyPromoCode.setText(mMyPromoCode);
        }
//        Send friends a free Chinese class and get up to 5 classes for yourself, worth $100! Details.
        SpannableStringBuilder stBuilder = new SpannableStringBuilder(getString(R.string.pc_promo_code_explanation));
        stBuilder.append(getString(R.string.pc_promo_code_details));
        stBuilder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    Intent referAFriendIntent = new Intent(getActivity(), WebViewActivity.class);
                    referAFriendIntent.putExtra(WebViewActivity.EXTRA_WEB_URL, changeReferLocale(CommonConstant.EXTRA_REFER_A_FRIEND));
                    startActivity(referAFriendIntent);
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                if (getActivity() != null)
                    ds.setColor(ContextCompat.getColor(getActivity(), R.color.btn_bg_rounded_green_normal));
            }
        }, stBuilder.length() - getString(R.string.pc_promo_code_details).length(), stBuilder.length(), 0);

        tvMyPromoCodeExplanation.setMovementMethod(LinkMovementMethod.getInstance());
        tvMyPromoCodeExplanation.setText(stBuilder, TextView.BufferType.SPANNABLE);

        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_refer_a_friend:
                mRlPromoCode.setVisibility(View.GONE);
                mRlPromoCodeBlur.setVisibility(View.VISIBLE);
                mRlPromoCodeBlur.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_promo_blur));

                dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_refer_friend);

                LinearLayout llFacebook = dialog.findViewById(R.id.ll_facebook);
                LinearLayout llTwitter = dialog.findViewById(R.id.ll_twitter);
                LinearLayout llGooglePlus = dialog.findViewById(R.id.ll_google_plus);
                LinearLayout llEmail = dialog.findViewById(R.id.ll_email);

                llFacebook.setOnClickListener(this);
                llTwitter.setOnClickListener(this);
                llGooglePlus.setOnClickListener(this);
                llEmail.setOnClickListener(this);
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        mRlPromoCode.setVisibility(View.VISIBLE);
                        mRlPromoCodeBlur.setVisibility(View.GONE);
                        mRlPromoCodeBlur.setBackground(null);
                    }
                });

                dialog.show();
                break;
            case R.id.ll_facebook:
                dialog.dismiss();
                shareFacebook();
                break;
            case R.id.ll_twitter:
                dialog.dismiss();
                shareTwitter();
                break;
            case R.id.ll_google_plus:
                dialog.dismiss();
                shareGooglePlus();
                break;
            case R.id.ll_email:
                dialog.dismiss();
                shareEmail();
                break;
            case R.id.tv_have_promo_code:
                mRlPromoCodeBlur.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_promo_blur));
                PromoCodeDialog promoCodeDialog = new PromoCodeDialog();
                promoCodeDialog.havePromoCode(getActivity(), mRlPromoCode, mRlPromoCodeBlur);
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStack();
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_promo_code_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.opt_promo_code_list:
                Fragment promoCodeFrg = new PromoCodeListFragment();
                FragmentManager fmPromoCode = getFragmentManager();
                Fragment frgPromoCode = fmPromoCode.findFragmentById(R.id.container);
                if (frgPromoCode == null) {
                    fmPromoCode.beginTransaction()
                            .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                    R.animator.fragment_out_new, R.animator.fragment_out_old)
                            .addToBackStack(null)
                            .add(R.id.container, promoCodeFrg).commit();
                } else {
                    fmPromoCode.beginTransaction()
                            .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                    R.animator.fragment_out_new, R.animator.fragment_out_old)
                            .addToBackStack(null)
                            .replace(R.id.container, promoCodeFrg).commit();
                }
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareFacebook() {
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(changeWebsiteLocale("https://www.tutormandarin.net/en/")))
                .setQuote(getString(R.string.pc_share_promo_code, mMyPromoCode))
                .build();

        ShareDialog shareDialog = new ShareDialog(getActivity());
        shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
    }

    private void shareTwitter() {
        if (getActivity() != null) {
            TweetComposer.Builder builder = new TweetComposer.Builder(getActivity())
                    .text(getString(R.string.pc_share_promo_code, mMyPromoCode) + " " + changeWebsiteLocale("https://www.tutormandarin.net/en/"));
            builder.show();
        }
    }

    private void shareGooglePlus() {
        Intent shareIntent = new PlusShare.Builder(getActivity())
                .setType("text/plain")
                .setText(getString(R.string.pc_share_promo_code, mMyPromoCode))
                .setContentUrl(Uri.parse(changeWebsiteLocale("https://www.tutormandarin.net/en/")))
                .getIntent();

        startActivityForResult(shareIntent, 0);
    }

    private void shareEmail() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_SUBJECT, "Sharing my promo code");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(getString(R.string.pc_share_promo_code, mMyPromoCode), Html.FROM_HTML_MODE_LEGACY));
        } else {
            i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(getString(R.string.pc_share_promo_code, mMyPromoCode)));
        }

        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            if (getActivity() != null)
                Toast.makeText(getActivity(), getString(R.string.sh_email_not_install), Toast.LENGTH_SHORT).show();
        }
    }

    private String changeReferLocale(String inputWebLink) {
        String changeWebLink;
        switch (mLocale) {
            case "ko":
                changeWebLink = "https://www.tutormandarin.net/ko/refer-a-friend-kr/";
                break;
            default:
                changeWebLink = inputWebLink;
                break;
        }
        return changeWebLink;
    }

    private String changeWebsiteLocale(String inputWebLink) {
        String changeWebLink;
        switch (mLocale) {
            case "de":
                changeWebLink = "https://www.tutormandarin.net/de/";
                break;
            case "ja":
                changeWebLink = "https://www.tutormandarin.net/ja/";
                break;
            case "zh":
                changeWebLink = "https://www.tutormandarin.net/zh/";
                break;
            case "ko":
                changeWebLink = "https://www.tutormandarin.net/ko/";
                break;
            default:
                changeWebLink = inputWebLink;
                break;
        }
        return changeWebLink;
    }
}
