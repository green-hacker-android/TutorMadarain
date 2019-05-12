package inc.osbay.android.tutormandarin.service;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import org.apache.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import inc.osbay.android.tutormandarin.sdk.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.LGCNotification;
import inc.osbay.android.tutormandarin.sdk.util.LGCUtil;
import inc.osbay.android.tutormandarin.ui.activity.WelcomeActivity;
import inc.osbay.android.tutormandarin.util.Log4jHelper;
import me.leolin.shortcutbadger.ShortcutBadger;

public class MessengerService extends Service {

    public static final int MSG_IM_CLASS = 4;
    public static final int MSG_REGISTER_CLIENT = 11;
    public static final int MSG_UNREGISTER_CLIENT = 12;
    public static final int MSG_WS_LOGIN = 13;
    public static final int MSG_WS_LOGOUT = 15;
    public static final int MSG_SIGNAL = 2;
    public static final int MSG_IM_CONSULTANT = 5;
    public static final int MSG_IM_TRIAL_CONSULTANT = 3;
    public static final int MSG_READY = 14;

    public static final String TAG = MessengerService.class.getSimpleName();

    public static final String IM_CLASS = "im_class";
    public static final String NORMAL_MESSAGE_TYPE = "im_online";
    public static final String INITIATE = "establish";
    public static final String HEART_BEAT = "heartbeat";
    public static final String TRIAL_MESSAGE_TYPE = "trial_request";
    public static final String TRIAL_COMFIRM_TYPE = "trial_confirm";
    public static final String CALL_LOG = "call_msg";
    public static final String AUDIO_PLAY_TYPE = "audio_play";
    public static final String AUDIO_PAUSE_TYPE = "audio_pause";
    public static final String AUDIO_RESUME_TYPE = "audio_resume";
    public static final String AUDIO_STOP_TYPE = "audio_stop";
    //    private static final String TUTOR_READY = "ready";
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    public int notiCount;
    /**
     * For showing and hiding our notification.
     */
    NotificationManager mNM;
    /**
     * Keeps track of all current registered clients.
     */
    ArrayList<Messenger> mClients = new ArrayList<>();
    WebSocketClient mWebSocketClient;
    String mUserId;
    // sandbox or live
    String mMode;
    boolean isConnected;
    Timer mTimer;
    ScreenReceiver mReceiver;
    private Logger mLog;
    private Map<String, String> messages = new HashMap<>();
    private int mMissedHeartBeatCount;
    TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            boolean b;
            if (cm.getActiveNetworkInfo() != null) {
                b = cm.getActiveNetworkInfo().isConnected();
            } else {
                b = false;
            }

            if (b) {
                if (!isConnected) {
                    broadcastMessage(Message.obtain(null, MSG_WS_LOGIN, 2, 0));
                    login(mUserId);
                } else {
                    broadcastMessage(Message.obtain(null, MSG_WS_LOGIN, 1, 0));
                    if (mWebSocketClient != null && isConnected) {
                        mMissedHeartBeatCount++;
                        if (mMissedHeartBeatCount > 3) {
                            Log.e(TAG, "Missed heart beat count - " + mMissedHeartBeatCount);
                        }

                        try {
                            JSONObject json = new JSONObject();
                            // use at old version.
                            // json.put("message_id", UUID.randomUUID().toString());
                            json.put("account_id", mUserId);
                            json.put("signal_type", HEART_BEAT);
                            json.put("mode", mMode);

                            mWebSocketClient.send(json.toString());
                            Log.i(TAG, "Websocket Connected");
                        } catch (Exception e) {
                            FlurryAgent.logEvent("Websocket disconnected");
                            Log.e(TAG, "Websocket didn't connected to send message.", e);
                            isConnected = false;
                            login(mUserId);
                        }
                    }
                }
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.
//        showNotification();

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);

        mTimer = new Timer();
        mTimer.schedule(mTimerTask, 1, 5000);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            startForeground(1, new Notification());
        }
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(123);

        // Tell the user we stopped.
        Toast.makeText(this, "Remote Service Stopped.", Toast.LENGTH_SHORT).show();

        unregisterReceiver(mReceiver);
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification(int icon, String text) {
        Intent notificationIntent = new Intent(getApplicationContext(), WelcomeActivity.class);
        notificationIntent.setAction("open-notification");
//        //**add this line**
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        //**edit this line to put requestID as requestCode**
        PendingIntent contentIntent = PendingIntent.getActivity(this, new Random().nextInt(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(icon)  // the status icon
                .setLargeIcon(((BitmapDrawable) ContextCompat.getDrawable(getApplicationContext(),
                        R.drawable.ic_noti_green)).getBitmap())
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getString(R.string.app_name))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        mNM.notify(new Random().nextInt(), notification);
    }

    private void showAlertDialog(final String text) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isAppRunning = prefs.getBoolean("is_app_running", false);
        if (isAppRunning) {
            Intent intent = new Intent("InAppAlert");
            intent.putExtra("noti_text", text);
            sendBroadcast(intent);
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext())
                            .setTitle("Notice")
                            .setMessage(text)
                            .setPositiveButton("OK", null)
                            .create();

                    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

                    alertDialog.show();
                }
            });
        }
    }

    private void logout() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isAppRunning = prefs.getBoolean("is_app_running", false);

        Map<String, String> params = new HashMap<>();
        params.put("Last Page", "Kick out by Server");
        FlurryAgent.logEvent("Close App", params);

        prefs.edit().remove("access_token").apply();
        prefs.edit().remove("account_id").apply();

        mUserId = null;
        if (mWebSocketClient != null) {
            mWebSocketClient.close();
        }
        isConnected = false;
        mWebSocketClient = null;

        AccountAdapter adapter = new AccountAdapter(getApplicationContext());
        adapter.deleteAllTableData();

        if (isAppRunning) {
            Intent intent = new Intent("Refresh");
            sendBroadcast(intent);
        }

    }

    public void login(String userName) {
        Log.e(TAG, "login with user name - " + userName);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String socketUrl = prefs.getString("socket_url", "");
        //String socketUrl = "ws://192.168.1.22:8005";
        mMode = prefs.getString("socket_mode", "");

        if (TextUtils.isEmpty(socketUrl)) {
            ServerRequestManager requestManager = new ServerRequestManager(getApplicationContext());
            requestManager.getWebSocketComponent(new ServerRequestManager.OnRequestFinishedListener() {
                @Override
                public void onSuccess(Object result) {

                }

                @Override
                public void onError(ServerError err) {

                }
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (checkPermissionForSDCard()) {
                Log4jHelper log4jHelper = new Log4jHelper();
                if (mLog != null)
                    mLog = log4jHelper.getLogger("MessengerService");
            }
        } else {
            Log4jHelper log4jHelper = new Log4jHelper();
            mLog = log4jHelper.getLogger("MessengerService");
        }

        if (TextUtils.isEmpty(userName)) {
            userName = prefs.getString("account_id", null);
            String token = prefs.getString("access_token", null);

            Log.e(TAG, "account id - " + userName + ", token - " + token);

            if (TextUtils.isEmpty(token)) {
//                userName = "F_" + Settings.Secure
//                        .getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                return;
            } else {
                userName = "S_" + userName;
            }

            Log.e(TAG, "generated id - " + userName);
        }

        if (isConnected && userName.equals(mUserId)) {
            return;
        }
        mUserId = userName;

        Log.e(TAG, "Connect websocket with id - " + mUserId);

        URI uri;
        try {
            uri = new URI(socketUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri, new Draft_6455()) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                if (mLog != null) {
                    mLog.debug("WebSocket Opened. - " + serverHandshake.toString());
                }

                try {
                    JSONObject json = new JSONObject();
                    json.put("message_id", UUID.randomUUID().toString());
                    json.put("signal_type", INITIATE);
                    json.put("account_id", mUserId);
                    json.put("mode", mMode);

                    if (mLog != null) {
                        mLog.debug("Send ws : " + json.toString());
                    }

                    mWebSocketClient.send(json.toString());
                } catch (Exception e) {
                    if (mLog != null) {
                        mLog.error("Websocket didn't connected to send message. (Open)" + e.getMessage());
                        FlurryAgent.logEvent("Websocket disconnected");
                    }
                }
            }

            @Override
            public void onMessage(String message) {
                try {
                    Log.i("ReceivedMessage", message);
                    JSONObject obj = new JSONObject(message);
                    String signalType = obj.getString("signal_type");
                    String messageType = obj.getString("message_type");
                    if (HEART_BEAT.equals(signalType)) {
                        mMissedHeartBeatCount = 0;
                    } else {
                        Log.d(TAG, "Received - " + message);
                        switch (signalType) {
                            case INITIATE:
                                int resultStatus = obj.getInt("status");
                                if (resultStatus == 200) {
                                    if (mLog != null) {
                                        mLog.debug("WebSocket Connected.");
                                        FlurryAgent.logEvent("Websocket connected");
                                    }

                                    isConnected = true;
                                    broadcastMessage(Message.obtain(null, MSG_WS_LOGIN, 1, 0));
                                } else {
                                    if (mLog != null) {
                                        mLog.debug("WebSocket Disconnected.");
                                        FlurryAgent.logEvent("Websocket disconnected");
                                    }
                                    mWebSocketClient.close();
                                    broadcastMessage(Message.obtain(null, MSG_WS_LOGIN, 2, 0));
                                }
                                break;
                            case "developer":
                                if ("StudentResume".equals(messageType)) {
                                    ServerRequestManager requestManager = new ServerRequestManager(getApplicationContext());
                                    requestManager.getStudentInfo(new ServerRequestManager.OnRequestFinishedListener() {
                                        @Override
                                        public void onSuccess(Object result) {
//                                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                                            boolean isAppRunning = prefs.getBoolean("is_app_running", false);
//                                            if (isAppRunning) {
                                            Intent intent = new Intent("Refresh");
                                            sendBroadcast(intent);
//                                            }
                                        }

                                        @Override
                                        public void onError(ServerError err) {
                                            // ignore
                                        }
                                    });
                                } else if ("KickOut".equals(messageType)) {
                                    logout();
                                }
                                break;
                            case "classroom":
                                //if (obj.getString("account_id").equals("Server")) {
                                    if (IM_CLASS.equals(messageType)
                                            || NORMAL_MESSAGE_TYPE.equals(messageType)
                                            || AUDIO_PLAY_TYPE.equals(messageType)
                                            || AUDIO_RESUME_TYPE.equals(messageType)
                                            || AUDIO_PAUSE_TYPE.equals(messageType)
                                            || AUDIO_STOP_TYPE.equals(messageType)) {
                                        String msgobj = messages.get(obj.getString("message_id"));
                                        if (msgobj == null) {
                                            receivedChatMessage(message, MSG_IM_CLASS);
                                        } else {
                                            receivedChatMessage(msgobj, MSG_IM_CLASS);
                                        }
                                    } else if ("ready".equals(messageType)) {
                                        if (!"Server".equals(obj.getString("account_id"))) {
                                            broadcastMessage(Message.obtain(null, MSG_READY, 0, 0));
                                        }
                                    }
                                //}

                                break;
                            case "onlinesupport":
                                if (NORMAL_MESSAGE_TYPE.equals(messageType)) {
                                    String msgobj = messages.get(obj.getString("message_id"));
                                    if (msgobj == null) {
                                        receivedChatMessage(message, MSG_IM_CONSULTANT);
                                    } else {
                                        receivedChatMessage(msgobj, MSG_IM_CONSULTANT);
                                    }
                                }
                                if (TRIAL_MESSAGE_TYPE.equals(messageType) || TRIAL_COMFIRM_TYPE.equals(messageType)) {
                                    String msgobj = messages.get(obj.getString("message_id"));
                                    if (msgobj == null) {
                                        receivedChatMessage(message, MSG_IM_TRIAL_CONSULTANT);
                                    } else {
                                        receivedChatMessage(msgobj, MSG_IM_TRIAL_CONSULTANT);
                                    }
                                }
                                break;
                            case "notification":
                                AccountAdapter accountAdapter = new AccountAdapter(getApplicationContext());

                                String messageContent;
                                try {
                                    JSONObject msgObj = new JSONObject(obj.getString("message_content"));

                                    messageContent = String.format(Locale.getDefault(),
                                            msgObj.getString("message_template"),
                                            LGCUtil.convertToLocale(msgObj.getString("utc_date_time").replace("UTC|", "")));
                                } catch (JSONException e) {
                                    messageContent = obj.getString("message_content");
                                }

                                LGCNotification lgcNoti = new LGCNotification();
                                lgcNoti.setNotiId(obj.getString("notification_history_id"));
                                lgcNoti.setType(messageType);
                                lgcNoti.setCategory(obj.getString("message_category"));
                                lgcNoti.setLevel(obj.getString("priority_level"));
                                lgcNoti.setContent(messageContent);
                                lgcNoti.setSendDate(LGCUtil.convertToLocale(obj.getString("send_date")));
                                lgcNoti.setStatus(0);
//// TODO: 11/22/2017 send broadcast
                                accountAdapter.insertNotification(lgcNoti);

                                Intent intent = new Intent("inc.osbay.android.tutormandarin.NOTIFICATION");
                                sendBroadcast(intent);

                                if ("UpdateNews".equals(lgcNoti.getType()) ||
                                        "GlobalBanner".equals(lgcNoti.getType())) {
                                    showNotification(R.drawable.ic_tutor_mandarin, messageContent);

                                    if (!Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {
                                        if (getApplicationContext() != null) {
                                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                            boolean isAppNotiOpened = prefs.getBoolean("app_noti_open", false);
                                            int notiTotalCount = prefs.getInt("app_noti_count", 0);

                                            if (!isAppNotiOpened) {
                                                notiTotalCount += 1;
                                                notiCount = notiTotalCount;
                                                prefs.edit().putInt("app_noti_count", notiCount).apply();
                                                ShortcutBadger.applyCount(getApplicationContext(), notiCount);
                                            }
                                        }
                                    }
                                } else {
                                    showAlertDialog(lgcNoti.getContent());
                                }
                        }
                    }
                } catch (JSONException | ParseException e) {
                    if (mLog != null) {
                        mLog.error("Couldn't parse message" + e.getMessage());
                    }
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                isConnected = false;
                mWebSocketClient = null;
                if (mLog != null) {
                    mLog.debug("WebSocket Closed. " + i + s + b);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "WebSocket Error - ", e);
                FlurryAgent.logEvent("WebSocket Erorr - " + e.getMessage());
                for (int i = mClients.size() - 1; i >= 0; i--) {
                    try {
                        mClients.get(i).send(Message.obtain(null, MSG_WS_LOGIN, 2, 0));
                    } catch (RemoteException re) {
                        mClients.remove(i);
                    }
                }
            }
        };

        SSLContext sslContext;

        if (socketUrl.indexOf("wss") == 0) {
            try {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
                SSLSocketFactory factory = sslContext.getSocketFactory();
                mWebSocketClient.setSocket(factory.createSocket());
            } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
                e.printStackTrace();
            }
        }
        mWebSocketClient.connect();
    }

    private void broadcastMessage(Message message) {

        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                mClients.get(i).send(message);
            } catch (RemoteException e) {
                // The client is dead.  Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    private void receivedChatMessage(String text, int msgType) {
        Message message = Message.obtain(null, msgType);
        Bundle bundle = new Bundle();
        bundle.putString("data", text);
        message.setData(bundle);

        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                mClients.get(i).send(message);
            } catch (RemoteException e) {
                // The client is dead.  Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    private void sendWSMessage(JSONObject json) throws JSONException {
        if (!isConnected) {
            if (mLog != null) {
                mLog.error("Websocket not connected to send data");
                FlurryAgent.logEvent("Websocket disconnected");
            }

            isConnected = false;
            login(mUserId);
            return;
        }

        if (mLog != null) {
            mLog.debug("Send ws : " + json.toString());
        }
        messages.put(json.getString("message_id"), json.toString());
        mWebSocketClient.send(json.toString());
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Log.e(TAG, "Task removed");

        PendingIntent service = PendingIntent.getService(
                getApplicationContext(),
                1001,
                new Intent(getApplicationContext(), MessengerService.class),
                PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, service);
    }

    private boolean checkPermissionForSDCard() {
        int resultReadSD = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int resultWriteSD = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (resultReadSD == PackageManager.PERMISSION_GRANTED) && (resultWriteSD == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Handler of incoming messages from clients.
     */
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_WS_LOGIN:
                    Bundle userBundle = msg.getData();
                    String userId = userBundle.getString("user_id");
                    login(userId);
                    break;
                case MSG_WS_LOGOUT:
                    mUserId = null;

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    prefs.edit().remove("account_id").apply();
                    prefs.edit().remove("access_token").apply();

                    if (mWebSocketClient != null) {
                        mWebSocketClient.close();
                    }
                    isConnected = false;
                    mWebSocketClient = null;
                    break;
                case MSG_IM_CLASS:
//                    String message = msg.obj.toString();
                    Bundle bundle = msg.getData();

                    JSONObject chatMsg = new JSONObject();

                    try {
                        chatMsg.put("message_id", UUID.randomUUID().toString());
                        chatMsg.put("signal_type", "classroom");
                        chatMsg.put("message_type", IM_CLASS);
                        chatMsg.put("account_id", mUserId);
                        chatMsg.put("account_name", bundle.getString("account_name"));
                        chatMsg.put("send_to", bundle.getString("send_to"));
                        chatMsg.put("message_content", bundle.getString("message_content"));
                        chatMsg.put("classroom_id", bundle.getString("classroom_id"));
                        chatMsg.put("mode", mMode);

                        sendWSMessage(chatMsg);
                    } catch (JSONException e) {
//                        Log.e(TAG, "Create message fail", e);

                        if (mLog != null) {
                            mLog.error("Create message fail" + e.getMessage());
                        }
                    }

                    break;
                case MSG_IM_CONSULTANT:
//                    String message = msg.obj.toString();
                    Bundle bundle1 = msg.getData();

                    JSONObject chatMsg1 = new JSONObject();

                    try {
                        chatMsg1.put("message_id", UUID.randomUUID().toString());
                        chatMsg1.put("signal_type", "onlinesupport");
                        chatMsg1.put("message_type", NORMAL_MESSAGE_TYPE);
                        chatMsg1.put("account_id", mUserId);
                        chatMsg1.put("send_to", bundle1.getString("send_to"));
                        chatMsg1.put("message_content", bundle1.getString("message_content"));
                        chatMsg1.put("mode", mMode);

                        sendWSMessage(chatMsg1);
                    } catch (JSONException e) {
                        if (mLog != null) {
                            mLog.error("Create message fail" + e.getMessage());
                        }
                    }

                    break;
                case MSG_IM_TRIAL_CONSULTANT:
                    Bundle bundle2 = msg.getData();

                    JSONObject chatMsg2 = new JSONObject();

                    try {
                        chatMsg2.put("message_id", UUID.randomUUID().toString());
                        chatMsg2.put("signal_type", "onlinesupport");
                        chatMsg2.put("message_type", TRIAL_MESSAGE_TYPE);
                        chatMsg2.put("account_id", mUserId);
                        chatMsg2.put("send_to", bundle2.getString("send_to"));
                        chatMsg2.put("message_content", bundle2.getString("message_content"));
                        chatMsg2.put("mode", mMode);

                        sendWSMessage(chatMsg2);
                    } catch (JSONException e) {
                        if (mLog != null) {
                            mLog.error("Create message fail" + e.getMessage());
                        }
                    }
                    break;
                case MSG_SIGNAL:
                    Bundle signal = msg.getData();
                    JSONObject studentReady = new JSONObject();
                    try {
                        studentReady.put("message_id", UUID.randomUUID().toString());
                        studentReady.put("message_type", "ready");
                        studentReady.put("account_id", mUserId);
                        studentReady.put("signal_type", "classroom");
                        studentReady.put("send_to", signal.getString("tutor_id"));
                        studentReady.put("classroom_id", signal.getString("classroom_id"));
                        studentReady.put("mode", mMode);

                        sendWSMessage(studentReady);
                    } catch (JSONException e) {
                        if (mLog != null) {
                            mLog.error("Create message fail" + e.getMessage());
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
//                Log.i(TAG, "Screen Off");
//            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
//                Log.i(TAG, "Screen On");
//            }
        }

    }
}