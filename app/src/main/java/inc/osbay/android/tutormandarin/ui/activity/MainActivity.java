package inc.osbay.android.tutormandarin.ui.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.Map;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.TMApplication;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.service.MessengerService;
import inc.osbay.android.tutormandarin.ui.fragment.AccountInfoFragment;
import inc.osbay.android.tutormandarin.ui.fragment.BackHandledFragment;
import inc.osbay.android.tutormandarin.ui.fragment.ChangeBookingFragment;
import inc.osbay.android.tutormandarin.ui.fragment.CourseTopicFragment;
import inc.osbay.android.tutormandarin.ui.fragment.LessonListFragment;
import inc.osbay.android.tutormandarin.ui.fragment.MainFragment;
import inc.osbay.android.tutormandarin.ui.fragment.MyBookingFragment;
import inc.osbay.android.tutormandarin.ui.fragment.NotiListFragment;
import inc.osbay.android.tutormandarin.ui.fragment.TutorListFragment;
import inc.osbay.android.tutormandarin.ui.fragment.TutorTimeSelectFragment;
import inc.osbay.android.tutormandarin.ui.fragment.WhatsOnListFragment;
import inc.osbay.android.tutormandarin.util.DefaultExceptionHandler;
import inc.osbay.android.tutormandarin.util.WSMessageClient;
import me.leolin.shortcutbadger.ShortcutBadger;


public class MainActivity extends AppCompatActivity implements BackHandledFragment.BackHandlerInterface {
    private static final String TAG = MainActivity.class.getSimpleName();
    private BackHandledFragment selectedFragment;
    private InAppAlertNotiReceiver mAppAlertReceiver;
    private SharedPreferences mPreferences;
    private String classType;
    private Bundle bundle;
    private String TRIAL_REQUEST = "MainActivity.TRIAL_REQUEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FlurryAgent.logEvent("Launch App", true); // Every time launched
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(this));

        setContentView(R.layout.activity_main);


        mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        bundle = getIntent().getExtras();
        if (bundle != null) {
            classType = bundle.getString("class_type");
        }
        if (!classType.equalsIgnoreCase("normal")) {
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.edit().putBoolean("app_noti_open", true).apply();

        if (!Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {
            ShortcutBadger.removeCount(this);
        }

        // Show Main Fragment as default
        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.container);
        MainFragment mainFragment = new MainFragment();
        mainFragment.setArguments(bundle);
        if (fragment == null) {
            fm.beginTransaction()
                    .add(R.id.container, mainFragment)
                    .commit();
        } else {
            fm.beginTransaction()
                    .replace(R.id.container, mainFragment)
                    .commit();
        }

        if (getIntent() != null) {
            Log.e(TAG, "OnNewIntent" + getIntent().getAction());

            if ("open-notification".equals(getIntent().getAction())) {
                fm.beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.container, new NotiListFragment())
                        .commit();
            }
        }

        // Receive InAppAlert notification message
        mPreferences.edit().putBoolean("is_app_running", true).apply();
        mAppAlertReceiver = new InAppAlertNotiReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction("InAppAlert");
        filter.addAction("KickOut");
        filter.addAction("Refresh");
        registerReceiver(mAppAlertReceiver, filter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(TAG, "OnNewIntent" + intent.getAction());

        if ("open-notification".equals(intent.getAction())) {
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.container, new NotiListFragment())
                    .commit();

            FlurryAgent.logEvent("Launch App from Banner");
        } else if ("sign-up".equals(intent.getAction())) {
            Intent signUpIntent = new Intent(MainActivity.this, SignUpActivity.class);
            signUpIntent.putExtra(SignUpActivity.MAIN_SIGN_UP, 1);
            startActivity(signUpIntent);
        } else if ("refresh_fragment".equals(intent.getAction())) {
//            FragmentManager fm = getFragmentManager();
//            Fragment currentFragment = fm.findFragmentById(R.id.container);
//            final FragmentTransaction ft = fm.beginTransaction();
//            ft.detach(currentFragment);
//            ft.attach(currentFragment);
//            ft.commitAllowingStateLoss();
            FragmentManager fm = getFragmentManager();
            MainFragment mainFragment = new MainFragment();
            mainFragment.setArguments(bundle);
            fm.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.container, mainFragment)
                    .commit();
        } else if ("booking_refresh".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();

            FragmentManager frgMgr = getFragmentManager();
            MyBookingFragment bookingFragment = new MyBookingFragment();
            bookingFragment.setArguments(bundle);
            Fragment frg = frgMgr.findFragmentById(R.id.container);
            if (frg == null) {
                frgMgr.beginTransaction()
                        .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                R.animator.fragment_out_new, R.animator.fragment_out_old)
                        .addToBackStack(null)
                        .add(R.id.container, bookingFragment).commit();
            } else if (frg instanceof MyBookingFragment) {
                frgMgr.beginTransaction()
                        .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                R.animator.fragment_out_new, R.animator.fragment_out_old)
                        .replace(R.id.container, bookingFragment).commit();
            } else {
                frgMgr.beginTransaction()
                        .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                R.animator.fragment_out_new, R.animator.fragment_out_old)
                        .addToBackStack(null)
                        .replace(R.id.container, bookingFragment).commit();
            }
        } else if ("change_booking_refresh".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();

            FragmentManager frgMgr = getFragmentManager();
            ChangeBookingFragment chBookingFragment = new ChangeBookingFragment();
            chBookingFragment.setArguments(bundle);
            Fragment frg = frgMgr.findFragmentById(R.id.container);
            if (frg == null) {
                frgMgr.beginTransaction()
                        .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                R.animator.fragment_out_new, R.animator.fragment_out_old)
                        .addToBackStack(null)
                        .add(R.id.container, chBookingFragment).commit();
            } else if (frg instanceof ChangeBookingFragment) {
                frgMgr.beginTransaction()
                        .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                R.animator.fragment_out_new, R.animator.fragment_out_old)
                        .replace(R.id.container, chBookingFragment).commit();
            } else {
                frgMgr.beginTransaction()
                        .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                R.animator.fragment_out_new, R.animator.fragment_out_old)
                        .addToBackStack(null)
                        .replace(R.id.container, chBookingFragment).commit();
            }
        } else if ("tutor_time_refresh".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();

            FragmentManager frgMgr = getFragmentManager();
            TutorTimeSelectFragment tutorTimeSelectFragment = new TutorTimeSelectFragment();
            tutorTimeSelectFragment.setArguments(bundle);
            Fragment frg = frgMgr.findFragmentById(R.id.container);
            if (frg == null) {
                frgMgr.beginTransaction()
                        .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                R.animator.fragment_out_new, R.animator.fragment_out_old)
                        .addToBackStack(null)
                        .add(R.id.container, tutorTimeSelectFragment).commit();
            } else if (frg instanceof ChangeBookingFragment) {
                frgMgr.beginTransaction()
                        .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                R.animator.fragment_out_new, R.animator.fragment_out_old)
                        .replace(R.id.container, tutorTimeSelectFragment).commit();
            } else {
                frgMgr.beginTransaction()
                        .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                R.animator.fragment_out_new, R.animator.fragment_out_old)
                        .addToBackStack(null)
                        .replace(R.id.container, tutorTimeSelectFragment).commit();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        FlurryAgent.logEvent("App Visible", true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        FlurryAgent.endTimedEvent("App Visible");
    }

    @Override
    protected void onDestroy() {
        FlurryAgent.endTimedEvent("Launch App");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.edit().putBoolean("app_noti_open", false).apply();
        prefs.edit().putInt("app_noti_count", 0).apply();

        mPreferences.edit().putBoolean("is_app_running", false).apply();
        unregisterReceiver(mAppAlertReceiver);

        if (selectedFragment != null) {
            Map<String, String> params = new HashMap<>();
            params.put("Last Page", selectedFragment.getClass().getSimpleName());
            FlurryAgent.logEvent("Close App", params);
        }

        super.onDestroy();
    }

    @Override
    public void setmSelectedFragment(BackHandledFragment backHandledFragment) {
        if (selectedFragment != null) {
            FlurryAgent.endTimedEvent(selectedFragment.getClass().getSimpleName());
        }

        selectedFragment = backHandledFragment;
        FlurryAgent.logEvent(selectedFragment.getClass().getSimpleName(), true);
    }

    @Override
    public void onBackPressed() {
        selectedFragment.onBackPressed();
    }

    public void closeDrawers() {
        if (selectedFragment instanceof CourseTopicFragment) {
            ((CourseTopicFragment) selectedFragment).closeDrawer();
        } else if (selectedFragment instanceof TutorListFragment) {
            ((TutorListFragment) selectedFragment).closeDrawer();
        } else if (selectedFragment instanceof WhatsOnListFragment) {
            ((WhatsOnListFragment) selectedFragment).closeDrawer();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (selectedFragment instanceof AccountInfoFragment) {
            selectedFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LessonListFragment.PERMISSION_WRITE_REQUEST
                && grantResults[0] == PackageManager.PERMISSION_DENIED) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            alertDialogBuilder.setTitle("Permission");
            alertDialogBuilder.setMessage("Need to check storage permission in App System Setting");
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    private class InAppAlertNotiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ("InAppAlert".equals(intent.getAction())) {
                final String text = intent.getStringExtra("noti_text");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle(getString(R.string.main_alert_notice))
                                .setMessage(text)
                                .setPositiveButton(getString(R.string.main_alert_notice_ok), null)
                                .create();
                        alertDialog.show();
                    }
                });
            } else if ("KickOut".equals(intent.getAction())) {

                mPreferences.edit().remove("access_token").apply();
                mPreferences.edit().remove("account_id").apply();

                AccountAdapter adapter = new AccountAdapter(MainActivity.this);
                adapter.deleteAllTableData();

                Message message = Message.obtain(null, MessengerService.MSG_WS_LOGOUT);
                try {
                    WSMessageClient wsMessageClient = ((TMApplication) getApplication()).getWSMessageClient();
                    wsMessageClient.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                FragmentManager fm = getFragmentManager();
                Fragment currentFragment = fm.findFragmentById(R.id.container);
                final FragmentTransaction ft = fm.beginTransaction();
                ft.detach(currentFragment);
                ft.attach(currentFragment);
                ft.commitAllowingStateLoss();

                Map<String, String> params = new HashMap<>();
                params.put("Last Page", "Kick out by Server");
                FlurryAgent.logEvent("Close App", params);

            } else if ("Refresh".equals(intent.getAction())) {
                FragmentManager fm = getFragmentManager();
                Fragment currentFragment = fm.findFragmentById(R.id.container);
                final FragmentTransaction ft = fm.beginTransaction();
                ft.detach(currentFragment);
                ft.attach(currentFragment);
                ft.commitAllowingStateLoss();
            }
        }
    }
}
