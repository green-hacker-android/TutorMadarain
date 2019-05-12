package inc.osbay.android.tutormandarin;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.facebook.FacebookContentProvider;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.applinks.AppLinkData;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.flurry.android.FlurryAgent;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import inc.osbay.android.tutormandarin.sdk.TutorMandarinSDK;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.listener.StatusListener;
import inc.osbay.android.tutormandarin.service.MessengerService;
import inc.osbay.android.tutormandarin.util.WSMessageClient;
import io.fabric.sdk.android.Fabric;

public class TMApplication extends Application {
    private static final String TAG = TMApplication.class.getSimpleName();
    private static TMApplication sInstance;
    private WSMessageClient mWSMessageClient;

    public static TMApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        // Initialize the SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        AppLinkData.fetchDeferredAppLinkData(this,
                new AppLinkData.CompletionHandler() {
                    @Override
                    public void onDeferredAppLinkDataFetched(AppLinkData appLinkData) {
                        // Process app link data
                        if (appLinkData != null)
                            Log.e(TAG, "Facebook App Deep Link - " + appLinkData.getTargetUri());
                    }
                }
        );

        // Initialize Twitter SDK
        TwitterAuthConfig authConfig = new TwitterAuthConfig("xQdgo3nwPtBLbuYh7gtwBnbW8",
                "8zjq3oleknlQ6CT2RMefg4UFjKmUm9Q7sYzLYni4M4ub1JoIqB");
        Fabric.with(this, new TwitterCore(authConfig));

        // Initialize Fresco SDK
        Fresco.initialize(getApplicationContext());

        // Initialize Flurry Analytics SDK
        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, "YXFW98PSS3GVRQM6B734");

        String deviceId = Settings.Secure
                .getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        FlurryAgent.setUserId(deviceId);

        // Initialize TutorMandarin SDK
        TutorMandarinSDK.initialize(getApplicationContext(), new StatusListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "TutorMandarin sdk initialized.");
            }

            @Override
            public void onError(int errorCode) {
                Log.e(TAG, "TutorMandarin sdk initialize error.");
            }
        });

        CurriculumAdapter adapter = new CurriculumAdapter(getApplicationContext());
        adapter.getTopicLevels();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            startForegroundService(new Intent(getApplicationContext(), MessengerService.class));
        } else {
            startService(new Intent(getApplicationContext(), MessengerService.class));
        }

        startService(new Intent(getApplicationContext(), MessengerService.class));

        mWSMessageClient = new WSMessageClient(getApplicationContext());
        mWSMessageClient.doBindService();

        // Setup handler for crashing or uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                handleUncaughtException (thread, e);
            }
        });
    }

    public void handleUncaughtException (Thread thread, Throwable e)
    {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically

        Intent intent = new Intent ();
        intent.setAction ("inc.osbay.android.tutormandarin.SEND_LOG");
        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        startActivity (intent);

        System.exit(1); // kill off the crashed app
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mWSMessageClient.doUnbindService();
    }

    public WSMessageClient getWSMessageClient() {
        return mWSMessageClient;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
