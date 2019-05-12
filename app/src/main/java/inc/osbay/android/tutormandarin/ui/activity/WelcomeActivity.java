package inc.osbay.android.tutormandarin.ui.activity;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.ads.conversiontracking.AdWordsConversionReporter;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.ui.fragment.BackHandledFragment;
import inc.osbay.android.tutormandarin.ui.fragment.SplashFragment;

public class WelcomeActivity extends AppCompatActivity implements BackHandledFragment.BackHandlerInterface {

    public static WelcomeActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        instance = this;

        Intent intent = getIntent();
        String pcode = null;
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            pcode = bundle.getString("pcode");
        }

        if (!TextUtils.isEmpty(pcode)) {
            ServerRequestManager serverRequestManager = new ServerRequestManager(this);
            serverRequestManager.addReferrerPromoCode(pcode, "Launcher", new ServerRequestManager.OnRequestFinishedListener() {
                @Override
                public void onSuccess(Object result) {
                }

                @Override
                public void onError(ServerError err) {
                }
            });
        }

        loadSplashFragment();
    }

    public void loadSplashFragment() {
        // TutorMandarin Installs
        // Google Android first open conversion tracking snippet
        AdWordsConversionReporter.reportWithConversionId(this.getApplicationContext(),
                "944170633", "NcayCJnq624Qic2bwgM", "0.00", false);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        FragmentManager fm = getFragmentManager();
        Fragment splashFragment = new SplashFragment();
        Fragment currentFragment = fm.findFragmentById(R.id.container);
        if (currentFragment == null) {
            fm.beginTransaction().add(R.id.container, splashFragment).commit();
        } else {
            fm.beginTransaction().replace(R.id.container, splashFragment).commit();
        }
    }

    @Override
    public void setmSelectedFragment(BackHandledFragment backHandledFragment) {
        // Ignore
    }
}
