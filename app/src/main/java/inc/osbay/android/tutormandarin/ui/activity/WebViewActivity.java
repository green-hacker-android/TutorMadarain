package inc.osbay.android.tutormandarin.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import inc.osbay.android.tutormandarin.R;

public class WebViewActivity extends AppCompatActivity {

    public static final String EXTRA_WEB_URL = "WebViewActivity.EXTRA_WEB_URL";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        final WebView webView = findViewById(R.id.wv_show_info);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.wv_loading));

        Intent intent = getIntent();
        if (intent != null) {
            String webUrl = intent.getStringExtra(EXTRA_WEB_URL);

            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    webView.loadUrl(url);
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);

                    progressDialog.dismiss();
                }
            });

            webView.loadUrl(webUrl);

            progressDialog.show();
        } else {
            this.finish();
        }
    }
}
