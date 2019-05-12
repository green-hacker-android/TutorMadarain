package inc.osbay.android.tutormandarin.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;

public class PayPalPaymentActivity extends Activity {

    public static final String EXTRA_ACCOUNT_ID = "Pay2GoPaymentActivity.EXTRA_ACCOUNT_ID";
    public static final String EXTRA_CREDIT_AMOUNT = "Pay2GoPaymentActivity.EXTRA_CREDIT_AMOUNT";

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String accountId = null;
        int creditAmount = 0;

        Intent intent = getIntent();
        if (intent != null) {
            accountId = intent.getStringExtra(EXTRA_ACCOUNT_ID);
            creditAmount = intent.getIntExtra(EXTRA_CREDIT_AMOUNT, 0);
        }

        final WebView mWebView = new WebView(PayPalPaymentActivity.this);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);

        //mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mWebView.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url == null) {
                    Log.d("Redirect", "host : first page");
                    return;
                }

                String host = Uri.parse(url).getHost();
                if ("www.sandbox.paypal.com".equals(host) || "sandbox.paypal.com".equals(host) ||
                        "www.paypal.com".equals(host) || "paypal.com".equals(host)) {
                    progressDialog.dismiss();
                } else if ("lingo.chat.success".equals(host)) {
                    Toast.makeText(PayPalPaymentActivity.this,
                            "Payment process finished", Toast.LENGTH_LONG).show();
                    Log.d("Redirect", "cached host : lingo.chat.success");
                    PayPalPaymentActivity.this.finish();
                }
                Log.d("Redirect", "host : " + Uri.parse(url).getHost());
            }
        });


        String paypalUrl = CommonConstant.PAYPAL_URL + "?SID=" + accountId + "&CAMT=" + String.valueOf(creditAmount);
        Log.e("Paypal", "Payment url - " + paypalUrl);
        mWebView.loadUrl(paypalUrl);

        setContentView(mWebView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.pp_loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /*private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            if (url == null) {
                Log.d("Redirect", "host : first page");
                return;
            }

            String host = Uri.parse(url).getHost();
            if ("www.sandbox.paypal.com".equals(host) || "sandbox.paypal.com".equals(host) ||
                    "www.paypal.com".equals(host) || "paypal.com".equals(host)) {
                progressDialog.dismiss();
            } else if ("lingo.chat.success".equals(host)) {
                Toast.makeText(PayPalPaymentActivity.this,
                        "Payment process finished", Toast.LENGTH_LONG).show();
                Log.d("Redirect", "cached host : lingo.chat.success");
                PayPalPaymentActivity.this.finish();
            }
            Log.d("Redirect", "host : " + Uri.parse(url).getHost());
        }
    }*/
}
