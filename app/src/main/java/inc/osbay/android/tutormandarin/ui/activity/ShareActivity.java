package inc.osbay.android.tutormandarin.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.flurry.android.FlurryAgent;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.util.ImageDownloader;

public class ShareActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_SHARE_ITEM = "ShareActivity.EXTRA_SHARE_ITEM";
    public static final String EXTRA_TITLE = "ShareActivity.EXTRA_TITLE";
    public static final String EXTRA_IMAGE_URL = "ShareActivity.EXTRA_IMAGE_URL";
    public static final String EXTRA_CONTENT = "ShareActivity.EXTRA_CONTENT";
    public static final String EXTRA_EMAIL_INCLUDE = "ShareActivity.EXTRA_EMAIL_INCLUDE";

    private static final String APP_URL = "https://www.tutormandarin.net/en/";

    private static final String APP_SHORT_URL = "goo.gl/deEaiw";

    private String mShareItem;
    private String mTitle;
    private String mImageUrl;
    private String mShareContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        boolean isEmailInclude = false;

        Intent intent = getIntent();
        if (intent != null) {
            mShareItem = intent.getStringExtra(EXTRA_SHARE_ITEM);
            mTitle = intent.getStringExtra(EXTRA_TITLE);
            mImageUrl = intent.getStringExtra(EXTRA_IMAGE_URL);
            mShareContent = intent.getStringExtra(EXTRA_CONTENT);

            isEmailInclude = intent.getBooleanExtra(EXTRA_EMAIL_INCLUDE, false);
        }

        ImageView imvClose = (ImageView) findViewById(R.id.imv_ic_close);
        imvClose.setOnClickListener(this);

        LinearLayout llFbShare = (LinearLayout) findViewById(R.id.ll_fb_share);
        llFbShare.setOnClickListener(this);

        LinearLayout llTwitterShare = (LinearLayout) findViewById(R.id.ll_twitter_share);
        llTwitterShare.setOnClickListener(this);

        LinearLayout llPinterestShare = (LinearLayout) findViewById(R.id.ll_pinterest_share);
        llPinterestShare.setOnClickListener(this);

        LinearLayout llMailShare = (LinearLayout) findViewById(R.id.ll_email_share);
        llMailShare.setOnClickListener(this);

        if (isEmailInclude) {
            llMailShare.setVisibility(View.VISIBLE);
            llPinterestShare.setVisibility(View.GONE);
        } else {
            llMailShare.setVisibility(View.GONE);
            llPinterestShare.setVisibility(View.VISIBLE);
        }

//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    "com.mtechidea.android.sharetest",
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!checkPermissionForSDCard()) {
                requestPermissionForSDCard();
            }
        }
    }

    @Override
    public void onClick(View view) {
        Map<String, String> shareParams = new HashMap<>();
        shareParams.put("Item", mShareItem);

        final ProgressDialog progressDialog = new ProgressDialog(ShareActivity.this);
        progressDialog.setMessage(getString(R.string.sh_downloading));
        progressDialog.setCancelable(false);

        switch (view.getId()) {
            case R.id.imv_ic_close:
                ShareActivity.this.finish();
                return;
            case R.id.ll_fb_share:
                shareParams.put("Method", "Facebook");

                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(APP_URL))
                        .setImageUrl(Uri.parse(mImageUrl))
                        .setContentTitle(mTitle)
                        .setContentDescription(mShareContent)
                        .build();

                ShareDialog shareDialog = new ShareDialog(ShareActivity.this);
                shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
                break;
            case R.id.ll_twitter_share:
                shareParams.put("Method", "Twitter");
                progressDialog.show();

                final String imageUrl = Environment.getExternalStorageDirectory() + File.separator +
                        "temp.jpg";

                final String text = String.format("%s %s", mShareContent.substring(0,
                        (mShareContent.length() > 120) ? 120 : mShareContent.length()),
                        APP_SHORT_URL);

                ImageDownloader.downloadImage(mImageUrl,
                        imageUrl, new ImageDownloader.OnDownloadFinishedListener() {
                            @Override
                            public void onSuccess() {
                                progressDialog.dismiss();

                                TweetComposer.Builder builder = new TweetComposer.Builder(ShareActivity.this)
                                        .text(text)
                                        .image(Uri.fromFile(new File(imageUrl)));
                                builder.show();

                            }

                            @Override
                            public void onError() {
                                progressDialog.dismiss();
                            }
                        });
                break;
            case R.id.ll_pinterest_share:
                shareParams.put("Method", "Pinterest");

                try {
                    shareOnPinterest();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.ll_email_share:
                shareParams.put("Method", "Email");

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_SUBJECT, mTitle);
                i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(mShareContent));
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(ShareActivity.this, getString(R.string.sh_email_not_install), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                return;
        }

        FlurryAgent.logEvent("Share", shareParams);
    }

    private void shareOnPinterest() throws UnsupportedEncodingException {
        String shareUrl = URLEncoder.encode(APP_URL, "UTF-8");
        String mediaUrl = URLEncoder.encode(mImageUrl, "UTF-8");

        String text = URLEncoder.encode(String.format(Locale.getDefault(), "%s %s", mShareContent,
                "#TutorMandarin #ChineseLearning #1on1Tutor"));

        String url = String.format(
                "https://www.pinterest.com/pin/create/button/?url=%s&media=%s&title=%s&description=%s",
                shareUrl, mediaUrl, mTitle, text);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        Intent instaIntent = getPackageManager()
                .getLaunchIntentForPackage("com.pinterest");
        if (instaIntent != null) {
            intent.setPackage("com.pinterest");
        }

        startActivity(intent);
    }

    private boolean checkPermissionForSDCard() {
        int resultReadSD = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int resultWriteSD = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (resultReadSD == PackageManager.PERMISSION_GRANTED) && (resultWriteSD == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissionForSDCard() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, getString(R.string.sh_sd_card_permission), Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    public static String getShareLessonTemplate(String lessonTitle) {
        return String.format("I’m studying “%s” with TutorMandarin. Care to join me at link?", lessonTitle);
    }

    public static String getShareFlashCardTemplate(String vocab) {
        return String.format("I learned “%s” with TutorMandarin. Care to join me at link?", vocab);
    }

    public static String getShareTutorTemplate(String tutorName) {
        return String.format("I studied with “%s” from TutorMandarin. You should too!", tutorName);
    }
}
