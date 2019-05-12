package inc.osbay.android.tutormandarin.ui.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import inc.osbay.android.tutormandarin.BuildConfig;
import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.TMApplication;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.sdk.model.Version;
import inc.osbay.android.tutormandarin.service.MessengerService;
import inc.osbay.android.tutormandarin.ui.activity.MainActivity;
import inc.osbay.android.tutormandarin.ui.activity.SignUpContinueActivity;
import inc.osbay.android.tutormandarin.ui.activity.TrialSubmitActivity;
import inc.osbay.android.tutormandarin.ui.activity.WelcomeTutorialActivity;
import inc.osbay.android.tutormandarin.util.WSMessageClient;

/**
 * A placeholder fragment containing a simple view.
 */
public class SplashFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String PREF_WELCOME_TUTORIAL = "MainActivity.PREF_WELCOME_TUTORIAL";
    private static final int PERMISSION_REQUEST_STORAGE = 1;
    private static long downloadReference = 0l;
    private static DownloadManager downloadManager;
    private static RelativeLayout splashRL;
    ServerRequestManager requestManager;
    SharedPreferences prefs;
    private Timer mSplashScreenTimer;
    private String Url = "";
    private boolean downloading = false;
    private String status;
    private int versionCodeServer = 0;
    private int packageVersionCode = 0;
    private int isforce = 0;
    private ProgressDialog progressDialog;
    private File fileInStorage;
    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            progressDialog.dismiss();
            Log.i("Downloading", "Completed");

            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setCancelable(false);
            dialog.setTitle(getContext().getResources().getString(R.string.download_complete));
            dialog.setMessage(getContext().getString(R.string.confirm_update));
            dialog.setPositiveButton(getContext().getString(R.string.update_now),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            File file_internal = new File(getContext().getFilesDir() + "/Download/", "" + versionCodeServer + ".apk");
                            try {
                                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "" + versionCodeServer + ".apk");
                                if (file.exists()) {
                                    fileInStorage = file;
                                } else if (file_internal.exists()) {
                                    fileInStorage = file_internal;
                                } else {
                                    Toast.makeText(getContext(), getContext().getString(R.string.file_not_found), Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Intent intent1 = new Intent(Intent.ACTION_VIEW);
                            Uri apkURI;

                            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                apkURI = FileProvider.getUriForFile(getContext(),
                                        BuildConfig.APPLICATION_ID + ".provider",
                                        fileInStorage);
                                getContext().grantUriPermission("com.android.packageinstaller", apkURI,
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                getContext().grantUriPermission("com.google.android.packageinstaller", apkURI,
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } else {
                                apkURI = Uri.fromFile(fileInStorage);
                            }
                            intent1.setDataAndType(apkURI, "application/vnd.android.package-archive");
                            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent1);
                            try {
                                System.exit(1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            dialogInterface.dismiss();
                        }
                    }).create();
            dialog.show();
        }
    };

    public SplashFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestManager = new ServerRequestManager(
                getActivity().getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(
                getActivity().getApplicationContext());
    }

    public void checkVersion() {
        requestManager.getVersionNumber(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object object) {
                Version versionData = (Version) object;
                versionCodeServer = Integer.parseInt(versionData.getVersionCode());
                isforce = Integer.parseInt(versionData.getIsForce());
                Url = versionData.getServerLink();

                try {
                    PackageInfo pkgInfo = getContext().getPackageManager().getPackageInfo(
                            getContext().getPackageName(), 0);
                    packageVersionCode = pkgInfo.versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                /*** Need to Update ***/
                if (versionCodeServer > packageVersionCode) {

                    File file_internal = new File(getContext().getFilesDir() + "/Download/", "" + versionCodeServer + ".apk");
                    try {
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "" + versionCodeServer + ".apk");
                        if (file.exists()) {
                            common_method(file);
                        } else if (file_internal.exists()) {
                            common_method(file_internal);
                        } else {
                            Download();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                /*** No Need to Update ***/
                else {
                    File file_internal = new File(getContext().getFilesDir() + "/Download/", "" + versionCodeServer + ".apk");
                    try {
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "" + versionCodeServer + ".apk");
                        if (file.exists()) {
                            file.delete();
                            gotoDashboard();
                        } else if (file_internal.exists()) {
                            file_internal.delete();
                            gotoDashboard();
                        } else {
                            gotoDashboard();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onError(ServerError err) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), err.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start to show other layouts.
                checkVersion();
            } else {
                // Permission request was denied.
                Snackbar.make(splashRL, R.string.sh_sd_card_permission,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private boolean checkPermissionForSDCard() {
        int resultReadSD = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int resultWriteSD = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (resultReadSD == PackageManager.PERMISSION_GRANTED) && (resultWriteSD == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissionForSDCard() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(getContext(), getString(R.string.sh_sd_card_permission), Toast.LENGTH_LONG).show();
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        }
    }

    public void Download() {
        /*** Critical Update ***/
        if (isforce == 1) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setCancelable(false);
            dialog.setTitle(getContext().getResources().getString(R.string.critical_update));
            dialog.setMessage(getContext().getString(R.string.update_new_version));
            dialog.setPositiveButton(getContext().getString(R.string.update),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            download_Package();
                        }
                    }).create();
            dialog.show();
        }
        /*** Minor Update ***/
        if (isforce == 2) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setCancelable(false);
            dialog.setTitle(getContext().getResources().getString(R.string.new_version));
            dialog.setMessage(getContext().getString(R.string.new_version_available));
            dialog.setPositiveButton(getContext().getString(R.string.update),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            download_Package();
                        }
                    }).create();
            dialog.setNegativeButton(getContext().getString(R.string.co_fav_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    gotoDashboard();
                }
            });
            dialog.show();
        }
    }

    private void check_status_() {
        try {
            DownloadManager.Query myDownloadQuery = new DownloadManager.Query();
            //set the query filter to our previously Enqueued download
            myDownloadQuery.setFilterById(downloadReference);
            //Query the download manager about downloads that have been requested.
            Cursor cursor = downloadManager.query(myDownloadQuery);
            if (cursor.moveToFirst()) {
                status = checkStatus(cursor);
            }
        } catch (Exception e) {
        }
    }

    public void common_method(final File file) {
        try {
            check_status_();
            if (status != null) {
                if (status.contains("ERROR")) {
                    file.delete();
                    downloadReference = 0;
                    download_Package();
                    return;
                }
                if (!status.equals("STATUS_PENDING") && !status.equals("STATUS_RUNNING") && !status.contains("PAUSED")) {
                    if (packageVersionCode == Integer.parseInt(file.getName().replace(".apk", ""))) {
                        downloadReference = 0;
                    } else {
                        updateDialog(file);
                    }
                }
            } else if (downloadReference == 0) {
                if (packageVersionCode == Integer.parseInt(file.getName().replace(".apk", ""))) {
                    downloadReference = 0;
                } else {
                    updateDialog(file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDialog(final File file) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setCancelable(false);
        dialog.setTitle(getContext().getResources().getString(R.string.update_now));
        dialog.setMessage(getContext().getString(R.string.confirm_update));
        dialog.setPositiveButton(getContext().getString(R.string.update_now),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent1 = new Intent(Intent.ACTION_VIEW);
                        Uri apkURI = null;
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            apkURI = FileProvider.getUriForFile(getContext(),
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    file);
                            getContext().grantUriPermission("com.android.packageinstaller", apkURI,
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            getContext().grantUriPermission("com.google.android.packageinstaller", apkURI,
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } else {
                            apkURI = Uri.fromFile(file);
                        }
                        intent1.setDataAndType(apkURI, "application/vnd.android.package-archive");
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent1);

                        try {
                            System.exit(1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dialogInterface.dismiss();
                    }
                }).create();
        dialog.show();
    }

    public void download_Package() {
        if (downloadReference == 0 && Url != "") {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.downloading));
            progressDialog.show();
            progressDialog.setCancelable(false);

            getContext().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


            Uri Download_Uri = Uri.parse(Url);
            DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
            //Restrict the types of networks over which this download may proceed.
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            //Set whether this download may proceed over a roaming connection.
            request.setAllowedOverRoaming(true);
            request.setVisibleInDownloadsUi(true);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            //Set the title of this download, to be displayed in notifications (if enabled).
            request.setTitle(getContext().getString(R.string.updating_title));
            //Set a description of this download, to be displayed in notifications (if enabled)
            request.setDescription(getContext().getString(R.string.update_desc));

            //Set the local destination for the downloaded file to a path within the application's external files directory
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "" + versionCodeServer + ".apk");
            //Enqueue a new download and same the referenceId
            downloadReference = downloadManager.enqueue(request);
            while (downloading) {
                DownloadManager.Query myDownloadQuery = new DownloadManager.Query();
                //set the query filter to our previously Enqueued download
                myDownloadQuery.setFilterById(downloadReference);
                //Query the download manager about downloads that have been requested.
                Cursor cursor = downloadManager.query(myDownloadQuery);
                if (cursor.moveToFirst()) {
                    checkStatus(cursor);
                }
            }
        } else {
            gotoDashboard();
        }
    }


    private String checkStatus(Cursor cursor) {
        //column for status
        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int status = cursor.getInt(columnIndex);
        //column for reason code if the download failed or paused
        int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
        int reason = cursor.getInt(columnReason);

        String reasonText = "";

        switch (status) {
            case DownloadManager.STATUS_FAILED:
                downloading = false;
                downloadReference = 0;
                switch (reason) {
                    case DownloadManager.ERROR_CANNOT_RESUME:
                        reasonText = "ERROR_CANNOT_RESUME";
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        reasonText = "ERROR_DEVICE_NOT_FOUND";
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        reasonText = "ERROR_FILE_ALREADY_EXISTS";
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        reasonText = "ERROR_FILE_ERROR";
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        reasonText = "ERROR_HTTP_DATA_ERROR";
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                        reasonText = "ERROR_INSUFFICIENT_SPACE";
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        reasonText = "ERROR_TOO_MANY_REDIRECTS";
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        reasonText = "ERROR_UNHANDLED_HTTP_CODE";
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        reasonText = "ERROR_UNKNOWN";
                        break;
                }
                break;
            case DownloadManager.STATUS_PAUSED:
                switch (reason) {
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        reasonText = "PAUSED_QUEUED_FOR_WIFI";
                        break;
                    case DownloadManager.PAUSED_UNKNOWN:
                        reasonText = "PAUSED_UNKNOWN";
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        reasonText = "PAUSED_WAITING_FOR_NETWORK";
                        break;
                    case DownloadManager.PAUSED_WAITING_TO_RETRY:
                        reasonText = "PAUSED_WAITING_TO_RETRY";
                        break;
                }
                break;
            case DownloadManager.STATUS_PENDING:
                reasonText = "STATUS_PENDING";
                break;
            case DownloadManager.STATUS_RUNNING:
                reasonText = "STATUS_RUNNING";
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                downloading = false;
                downloadReference = 0;
                reasonText = "STATUS_SUCCESSFUL";
                break;
        }
        return reasonText;
    }

    private void gotoDashboard() {
        TimerTask mTimerTask = new TimerTask() {
            @Override
            public void run() {
                goToMain();
            }
        };

        String sessionToken = prefs.getString("access_token", null);
        if (TextUtils.isEmpty(sessionToken)) {
            mSplashScreenTimer = new Timer();
            mSplashScreenTimer.schedule(mTimerTask, 1500);
        } else {
            requestManager.getStudentInfo(new ServerRequestManager.OnRequestFinishedListener() {
                @Override
                public void onSuccess(Object result) {
                    requestManager.downloadFavorites(null);
                    requestManager.downloadNotificationList(null);
                    goToMain();
                }

                @Override
                public void onError(ServerError err) {
                    prefs.edit().remove("access_token").apply();
                    prefs.edit().remove("account_id").apply();

                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), getString(R.string.ss_error),
                                Toast.LENGTH_SHORT).show();

                        Message message = Message.obtain(null, MessengerService.MSG_WS_LOGOUT);
                        try {
                            WSMessageClient wsMessageClient = ((TMApplication) getActivity().getApplication()).getWSMessageClient();
                            wsMessageClient.sendMessage(message);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        AccountAdapter accountAdapter = new AccountAdapter(getActivity());
                        accountAdapter.deleteAllTableData();

                        Map<String, String> params = new HashMap<>();
                        params.put("Last Page", "Auto Login Failed.");
                        FlurryAgent.logEvent("Close App", params);

                        goToMain();
                    }
                }
            });
        }
    }

    private void goToMain() {
        boolean b = prefs.getBoolean(PREF_WELCOME_TUTORIAL, false);
        if (!b) {
            FlurryAgent.logEvent("Launch App (Welcome)"); // First time launched after installed
            Log.i("Launch App", "(Welcome)");
            prefs.edit().putBoolean(PREF_WELCOME_TUTORIAL, true).apply();
            Intent intent = new Intent(getActivity(), WelcomeTutorialActivity.class);
            startActivity(intent);
            getActivity().finish();
        } else {
            String accountId = prefs.getString("account_id", null);
            String token = prefs.getString("access_token", null);
            if (!TextUtils.isEmpty(accountId) && !TextUtils.isEmpty(token) && getActivity() != null) {
                AccountAdapter accountAdapter = new AccountAdapter(getActivity());
                Account account = accountAdapter.getAccountById(accountId);
                if (account != null) {
                    if (account.getStatus() == Account.Status.INACTIVE) {
                        Intent profileSetup = new Intent(getActivity(), SignUpContinueActivity.class);
                        startActivity(profileSetup);
                    } else if (account.getStatus() == Account.Status.REQUEST && TextUtils.isEmpty(account.getPhoneNumber())) {
                        Intent profileSetup = new Intent(getActivity(), TrialSubmitActivity.class);
                        startActivity(profileSetup);
                    } else {
                        Intent mainActivity = new Intent(getActivity(), MainActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("class_type", "normal");
                        mainActivity.putExtras(bundle);
                        startActivity(mainActivity);
                    }
                    getActivity().finish();
                } else {
                    Intent mainActivity = new Intent(getActivity(), WelcomeTutorialActivity.class);
                    startActivity(mainActivity);

                    getActivity().finish();
                }
            } else {
                if (getActivity() != null) {
                    Intent mainActivity = new Intent(getActivity(), WelcomeTutorialActivity.class);
                    startActivity(mainActivity);

                    getActivity().finish();
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_splash, container, false);
        splashRL = v.findViewById(R.id.splash_rl);
        downloadManager = (DownloadManager) getContext().getSystemService(getContext().DOWNLOAD_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!checkPermissionForSDCard()) {
                requestPermissionForSDCard();
            } else {
                checkVersion();
            }
        } else {
            checkVersion();
        }
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSplashScreenTimer != null) {
            mSplashScreenTimer.cancel();
        }
    }
}
