package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flurry.android.FlurryAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.TMApplication;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.sdk.model.ChatMessage;
import inc.osbay.android.tutormandarin.service.MessengerService;
import inc.osbay.android.tutormandarin.ui.activity.CallSupportActivity;
import inc.osbay.android.tutormandarin.util.CustomizedExceptionHandler;
import inc.osbay.android.tutormandarin.util.WSMessageClient;

import static inc.osbay.android.tutormandarin.sdk.util.LGCUtil.convertUTCToLocale;
import static inc.osbay.android.tutormandarin.sdk.util.LGCUtil.getStartTimeFromUTC;
import static inc.osbay.android.tutormandarin.sdk.util.LGCUtil.isScheduledTrialClassExpired;
import static inc.osbay.android.tutormandarin.service.MessengerService.NORMAL_MESSAGE_TYPE;
import static inc.osbay.android.tutormandarin.service.MessengerService.TRIAL_COMFIRM_TYPE;
import static inc.osbay.android.tutormandarin.service.MessengerService.TRIAL_MESSAGE_TYPE;

public class OnlineSupportFragment extends BackHandledFragment implements View.OnClickListener {

    private static List<ChatMessage> mChatMessages;
    private static ChatMessageRVAdapter mMessageRVAdapter;
    private static Context mContext;
    public String classType;
    private RecyclerView rvChatMessages;
    private ServerRequestManager mServerRequestManager;
    private MenuItem mCallMenuItem;
    private String mAccountId;
    private String token;
    private String mConsultantId;
    private WSMessageClient mWSMessageClient;
    private Messenger mMessenger;
    //    private RelativeLayout mChatInputRelativeLayout;
    private EditText mInputEditText;
    private ProgressDialog mProgressDialog;
    private Timer mTimer;
    private SharedPreferences prefs;

    public static void showError(int errCode) {
        //trigger when Call Center Manager isn't online
        if (errCode == 106) {
            ChatMessage chatMessage = new ChatMessage();
            if (mContext != null)
                chatMessage.setBody(mContext.getString(R.string.os_no_consultants));
            else
                chatMessage.setBody("Sorry, TutorMandarin consultants are not available to take your " +
                        "call right now. Please leave a written message in the window and we'll " +
                        "get back to you as soon as we can.");
            chatMessage.setSender("C_auto");
            chatMessage.setMessageType("im_online");
            mChatMessages.add(chatMessage);

            mMessageRVAdapter.notifyDataSetChanged();
        }
        if (errCode == 107) {
            ChatMessage chatMessage = new ChatMessage();
            if (mContext != null)
                chatMessage.setBody(mContext.getString(R.string.os_call_ended));
            else
                chatMessage.setBody("The call was ended.");

            chatMessage.setSender("C_auto");
            chatMessage.setMessageType("im_online");
            mChatMessages.add(chatMessage);

            mMessageRVAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new CustomizedExceptionHandler(
                "/storage/emulated/0/Download", getContext()));

        mServerRequestManager = new ServerRequestManager(getActivity());

        mContext = getActivity();
        classType = getArguments().getString("class_type");

        mMessenger = new Messenger(new IncomingHandler());
        mWSMessageClient = ((TMApplication) getActivity().getApplication()).getWSMessageClient();
        mWSMessageClient.addMessenger(mMessenger);

        mChatMessages = new ArrayList<>();

        mMessageRVAdapter = new ChatMessageRVAdapter();

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getString(R.string.os_dialog_msg_loading));
        mProgressDialog.setCancelable(false);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mAccountId = prefs.getString("account_id", null);
        token = prefs.getString("access_token", null);

        if (TextUtils.isEmpty(mAccountId)) {
            mAccountId = Settings.Secure
                    .getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        final ChatMessage chatMessage = new ChatMessage();

        if (classType.equalsIgnoreCase("trial")) {
            mServerRequestManager.initializeTrialConsultant(mAccountId, new ServerRequestManager.OnRequestFinishedListener() {
                @Override
                public void onSuccess(Object result) {
                    showIntroMsg(chatMessage);
                    List<?> obj = (List<?>) result;
                    for (int i = 0; i < obj.size(); i++) {
                        mChatMessages.add((ChatMessage) obj.get(i));
                    }
                    mMessageRVAdapter.notifyDataSetChanged();
                }

                @Override
                public void onError(ServerError err) {
                    showIntroMsg(chatMessage);
                    mMessageRVAdapter.notifyDataSetChanged();
                }
            });
        } else {
            mServerRequestManager.initializeOnlineSupport(mAccountId, new ServerRequestManager.OnRequestFinishedListener() {
                @Override
                public void onSuccess(Object result) {
                    showIntroMsg(chatMessage);
                    List<?> obj = (List<?>) result;
                    for (int i = 0; i < obj.size(); i++) {
                        mChatMessages.add((ChatMessage) obj.get(i));
                    }
                    mMessageRVAdapter.notifyDataSetChanged();
                }

                @Override
                public void onError(ServerError err) {
                    showIntroMsg(chatMessage);
                    mMessageRVAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void showIntroMsg(ChatMessage chatMessage) {
        if (classType.equalsIgnoreCase("trial")) {
            chatMessage.setBody(getString(R.string.trial_request_welcome));
            chatMessage.setMessageType("trial_request");
        } else {
            chatMessage.setBody(getString(R.string.os_msg_body));
            chatMessage.setMessageType("im_online");
        }
        chatMessage.setSender("C_auto");
        mChatMessages.add(chatMessage);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_online_support, container, false);

        Toolbar toolBar;
        toolBar = rootView.findViewById(R.id.tool_bar);
        setSupportActionBar(toolBar);

        mInputEditText = rootView.findViewById(R.id.edt_input_text);

        TextView tvSendButton = rootView.findViewById(R.id.tv_send_button);
        if (classType.equalsIgnoreCase("demo") || classType.equalsIgnoreCase("classroom")) {
            toolBar.setBackgroundColor(Color.parseColor("#00ABA0"));
            tvSendButton.setTextColor(Color.parseColor("#00ABA0"));
        } else {
            toolBar.setBackgroundColor(Color.parseColor("#DE615B"));
            tvSendButton.setTextColor(Color.parseColor("#DE615B"));
        }
        tvSendButton.setOnClickListener(this);

        rvChatMessages = rootView.findViewById(R.id.rv_chat_messages);
        rvChatMessages.setAdapter(mMessageRVAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        rvChatMessages.setLayoutManager(layoutManager);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mAccountId = prefs.getString("account_id", null);
        if (TextUtils.isEmpty(mAccountId)) {
            mAccountId = Settings.Secure
                    .getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);

            Message message = Message.obtain(null, MessengerService.MSG_WS_LOGIN);
            Bundle bundle = new Bundle();
            bundle.putString("user_id", "S_" + mAccountId);
            message.setData(bundle);
            try {
                mWSMessageClient.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mProgressDialog.show();

        if (classType.equalsIgnoreCase("trial")) {
            getAvailableConsultant();
        } else {
            getAvailableCallCenterManager();
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        setHasOptionsMenu(true);
        if (classType.equalsIgnoreCase("trial")) {
            setTitle(getString(R.string.consultant));
        } else {
            setTitle(getString(R.string.os_title));
        }
        setDisplayHomeAsUpEnable(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!classType.equalsIgnoreCase("trial") && !classType.equalsIgnoreCase("demo")
                && !classType.equalsIgnoreCase("classroom")) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mCallMenuItem != null) {
                                if (CallSupportActivity.sIsTalking) {
                                    mCallMenuItem.setIcon(R.drawable.ic_calling);
                                } else {
                                    mCallMenuItem.setIcon(R.drawable.ic_call);
                                }
                            }
                        }
                    });
                }
            }, 1000, 1000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!classType.equalsIgnoreCase("trial") && !classType.equalsIgnoreCase("demo")
                && !classType.equalsIgnoreCase("classroom")) {
            mTimer.cancel();
        }
    }

    @Override
    public void onDestroy() {
        mWSMessageClient.removeMessenger(mMessenger);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_send_button:
                String text = mInputEditText.getText().toString();

                if (TextUtils.isEmpty(text)) {
                    break;
                }
                rvChatMessages.smoothScrollToPosition(0);
                android.os.Message message;
                Bundle bundle = new Bundle();
                if (classType.equalsIgnoreCase("trial")) {
                    mServerRequestManager.saveMessageHistoryConsultant(text, mConsultantId, new ServerRequestManager.OnRequestFinishedListener() {
                        @Override
                        public void onSuccess(Object result) {
                        }

                        @Override
                        public void onError(ServerError err) {
                        }
                    });
                    message = android.os.Message.obtain(null, MessengerService.MSG_IM_TRIAL_CONSULTANT);
                    bundle.putString("send_to", "G_" + mConsultantId);
                } else {
                    mServerRequestManager.saveMessageHistory(text, mConsultantId, new ServerRequestManager.OnRequestFinishedListener() {
                        @Override
                        public void onSuccess(Object result) {
                        }

                        @Override
                        public void onError(ServerError err) {
                        }
                    });
                    message = android.os.Message.obtain(null, MessengerService.MSG_IM_CONSULTANT);
                    bundle.putString("send_to", "C_" + mConsultantId);
                }

                bundle.putString("message_content", text);
                message.setData(bundle);
                try {
                    mWSMessageClient.sendMessage(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                mInputEditText.setText("");

                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Disabling call function for Trial Request
        if (!classType.equalsIgnoreCase("trial") && !classType.equalsIgnoreCase("demo")
                && !classType.equalsIgnoreCase("classroom")) {
            inflater.inflate(R.menu.menu_support_call, menu);
            mCallMenuItem = menu.findItem(R.id.opt_call_support);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.opt_call_support:
                if (!CallSupportActivity.sIsTalking) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.os_call_ready))
                            .setMessage(getString(R.string.os_call_ready_msg))
                            .setPositiveButton(getString(R.string.os_call_ready_confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mProgressDialog.show();

                                    initializeTwilioSDK();
                                }
                            })
                            .setNegativeButton(getString(R.string.os_call_ready_cancel), null)
                            .create()
                            .show();

                } else {
                    Intent callIntent = new Intent(getActivity(), CallSupportActivity.class);
                    startActivity(callIntent);
                }
                break;
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        if (classType.equalsIgnoreCase("trial")) {
            FlurryAgent.logEvent("Consultant back");
        }
        getFragmentManager().popBackStack();
        if (classType.equalsIgnoreCase("classroom")) {
            getActivity().finish();
        }
        return false;
    }

    private void getAvailableCallCenterManager() {
        mServerRequestManager.GetOnlineCallCenterManager(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                mProgressDialog.dismiss();
                mConsultantId = String.valueOf(result);
            }

            @Override
            public void onError(ServerError err) {
                mProgressDialog.dismiss();

                if (getActivity() != null) {
                    // TODO: 1/5/2018 Testing open chat box.
//                    mChatInputRelativeLayout.setVisibility(View.GONE);

                    ChatMessage chatMessage = new ChatMessage();
                    if (isChineseNewYearHoliday()) {
                        chatMessage.setBody("Happy Chinese New Year from TutorMandarin! " +
                                "We will have limited operating hours for Online Support from " +
                                "January 27th to February 1st (China Standard Time). " +
                                "Regular operating hours will resume on February 2nd. See you " +
                                "again in the Year of the Rooster!");
                    } else {
                        chatMessage.setBody(getString(R.string.os_working_period));
                    }
                    chatMessage.setSender("C_auto");
                    chatMessage.setMessageType("im_online");
                    mChatMessages.add(chatMessage);

                    mMessageRVAdapter.notifyDataSetChanged();

                    if (err.getErrorCode() == ServerError.Code.NO_OPERATOR_ONLINE) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.os_no_online_support_title))
                                .setMessage(getString(R.string.os_no_online_support_msg))
                                .setPositiveButton(getString(R.string.os_no_online_support_ok), null)
                                .create()
                                .show();
                    } else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.os_server_error))
                                .setMessage(err.getMessage())
                                .setPositiveButton(getString(R.string.os_server_error_ok), null)
                                .create()
                                .show();
                    }
                }
            }
        });
    }

    private void getAvailableConsultant() {
        mServerRequestManager.GetOnlineConsultant(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                mProgressDialog.dismiss();
                mConsultantId = String.valueOf(result);
            }

            @Override
            public void onError(ServerError err) {
                mProgressDialog.dismiss();

                if (getActivity() != null) {
                    // TODO: 1/5/2018 Testing open chat box.
//                    mChatInputRelativeLayout.setVisibility(View.GONE);

                    ChatMessage chatMessage = new ChatMessage();
                    if (isChineseNewYearHoliday()) {
                        chatMessage.setBody("Happy Chinese New Year from TutorMandarin! " +
                                "We will have limited operating hours for Online Support from " +
                                "January 27th to February 1st (China Standard Time). " +
                                "Regular operating hours will resume on February 2nd. See you " +
                                "again in the Year of the Rooster!");
                    } else {
                        chatMessage.setBody(getString(R.string.os_working_period));
                    }
                    chatMessage.setSender("C_auto");
                    chatMessage.setMessageType("trial_request");
                    mChatMessages.add(chatMessage);

                    mMessageRVAdapter.notifyDataSetChanged();

                    if (err.getErrorCode() == ServerError.Code.NO_OPERATOR_ONLINE) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.os_no_online_support_title))
                                .setMessage(getString(R.string.os_no_online_support_msg))
                                .setPositiveButton(getString(R.string.os_no_online_support_ok), null)
                                .create()
                                .show();
                    } else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.os_server_error))
                                .setMessage(err.getMessage())
                                .setPositiveButton(getString(R.string.os_server_error_ok), null)
                                .create()
                                .show();
                    }
                }
            }
        });
    }

    private boolean isChineseNewYearHoliday() {
        long chinaTimeMillis = GregorianCalendar.getInstance(TimeZone.getDefault()).getTimeInMillis();
        Calendar startHolidayCalendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
        startHolidayCalendar.set(2017, Calendar.JANUARY, 27, 0, 0, 0);
        Calendar endHolidayCalendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
        endHolidayCalendar.set(2017, Calendar.FEBRUARY, 2, 0, 0, 0);
        return chinaTimeMillis > startHolidayCalendar.getTimeInMillis() && chinaTimeMillis < endHolidayCalendar.getTimeInMillis();
    }

    private void initializeTwilioSDK() {
        mServerRequestManager.GetOnlineCallCenterManager(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                mConsultantId = String.valueOf(result);

                mServerRequestManager.getTwilioAccessToken(new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        mProgressDialog.dismiss();

                        if (getActivity() != null) {
                            String videoToken = String.valueOf(result);

//                            TwilioVideoUtil.getInstance().initialize(
//                                    getActivity().getApplicationContext(), videoToken);

                            Intent intent = new Intent(getActivity(), CallSupportActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(CallSupportActivity.EXTRA_CONSULTANT_ID, mConsultantId);
                            intent.putExtra(CallSupportActivity.EXTRA_ACCESS_TOKEN, videoToken);
                            startActivity(intent);

                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        mProgressDialog.dismiss();

                        if (getActivity() != null) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getString(R.string.os_twilio_access_error))
                                    .setMessage(err.getMessage())
                                    .setPositiveButton(getString(R.string.os_twilio_access_error_ok), null)
                                    .create()
                                    .show();
                        }
                    }
                });
            }

            @Override
            public void onError(ServerError err) {
                mProgressDialog.dismiss();

                if (getActivity() != null) {
                    if (err.getErrorCode() == ServerError.Code.NO_OPERATOR_ONLINE) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.os_not_available_title))
                                .setMessage(getString(R.string.os_not_available_msg))
                                .setPositiveButton(getString(R.string.os_not_available_ok), null)
                                .create()
                                .show();
                    } else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.os_no_operator_error))
                                .setMessage(err.getMessage())
                                .setPositiveButton(getString(R.string.os_no_operator_error_ok), null)
                                .create()
                                .show();
                    }
                }
            }
        });
    }

    /**
     * Handler of incoming messages from service.
     */
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MessengerService.MSG_IM_CONSULTANT:
                    Bundle chatBundle = msg.getData();

                    ChatMessage message = new ChatMessage(chatBundle.getString("data")); //{"account_id":"nicolas","message_type":"4","send_to":"aty","message_content":"HQQQ"}

                    /*** C_ is for Call Center Manager ***/
                    if (message.getSender().startsWith("C_")) {
                        mServerRequestManager.updateMessageHistory(new ServerRequestManager.OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                            }

                            @Override
                            public void onError(ServerError err) {
                            }
                        });
                    }

                    mChatMessages.add(message);
                    mMessageRVAdapter.notifyDataSetChanged();
                    rvChatMessages.smoothScrollToPosition(0);
                    break;
                case MessengerService.MSG_IM_TRIAL_CONSULTANT:
                    Bundle chatBundle1 = msg.getData();

                    ChatMessage message1 = new ChatMessage(chatBundle1.getString("data")); //{"account_id":"nicolas","message_type":"4","send_to":"aty","message_content":"HQQQ"}

                    /*** G_ is for Consultant ***/
                    if (message1.getSender().startsWith("G_")) {
                        mServerRequestManager.updateMessageHistoryConsultant(new ServerRequestManager.OnRequestFinishedListener() {
                            @Override
                            public void onSuccess(Object result) {
                                mServerRequestManager.getStudentInfo(new ServerRequestManager.OnRequestFinishedListener() {
                                    @Override
                                    public void onSuccess(Object result) {
                                        if (getActivity() != null) {
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
                            public void onError(ServerError err) {
                            }
                        });
                    }

                    mChatMessages.add(message1);
                    mMessageRVAdapter.notifyDataSetChanged();
                    rvChatMessages.smoothScrollToPosition(0);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private class ChatMessageRVAdapter extends RecyclerView.Adapter<ChatMessageRVAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_chat_support,
                    parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            ChatMessage msg = mChatMessages.get(mChatMessages.size() - ++position);

            Log.e("OnlineSupport", "account id - " + mAccountId + ", sender - " + msg.getSender());

            // Messages sent by Student
            if (mAccountId.equals(msg.getSender().split("_")[1])) {
                holder.llChatLayout.setGravity(Gravity.END);
                holder.imvSupportIcon.setVisibility(View.GONE);
                holder.tvSendMessage.setText(msg.getBody());
                holder.tvSendMessage.setVisibility(View.VISIBLE);
                holder.tvReceivedMessage.setVisibility(View.GONE);
            }
            // Messages sent by Consultant or Call Center Manager
            else {
                holder.llChatLayout.setGravity(Gravity.START);
                holder.tvSendMessage.setVisibility(View.GONE);
                if (msg.getMessageType().equalsIgnoreCase(TRIAL_MESSAGE_TYPE)
                        || msg.getMessageType().equalsIgnoreCase(NORMAL_MESSAGE_TYPE)
                        || msg.getMessageType().equalsIgnoreCase("")) {
                    holder.trialConfirmLayout.setVisibility(View.GONE);
                    holder.tvReceivedMessage.setText(msg.getBody());
                    holder.tvReceivedMessage.setVisibility(View.VISIBLE);
                    holder.imvSupportIcon.setVisibility(View.VISIBLE);
                    holder.llChatLayout.setVisibility(View.VISIBLE);
                }
                if (msg.getMessageType().equalsIgnoreCase(TRIAL_COMFIRM_TYPE)) {
                    FlurryAgent.logEvent("Trial today scheduled");
                    holder.trialConfirmLayout.setVisibility(View.VISIBLE);
                    holder.llChatLayout.setVisibility(View.GONE);
                    String messageContent = msg.getBody();
                    try {
                        JSONObject msgJson = new JSONObject(messageContent);
                        final String bookStartDateTime = msgJson.getString("bookstart_datetime");
                        String bookEndDateTime = msgJson.getString("bookend_datetime");
                        String tutorName = msgJson.getString("tutor_name");
                        String tutorAvatar = msgJson.getString("tutor_avatar");

                        holder.tutorName.setText(tutorName);
                        holder.tutorImg.setImageURI(tutorAvatar);
                        holder.scheduleDate.setText(convertUTCToLocale(bookStartDateTime));
                        holder.scheduleTime.setText(getStartTimeFromUTC(bookStartDateTime) + " - " + getStartTimeFromUTC(bookEndDateTime));
                        if (isScheduledTrialClassExpired(bookEndDateTime)) {
                            holder.confirm.setClickable(false);
                            holder.confirm.setTextColor(getResources().getColor(R.color.view_light_gray));
                            holder.confirm.setBackground(getResources().getDrawable(R.drawable.trial_class_confirm_btn_disabled_bg));
                        } else {
                            holder.confirm.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    FlurryAgent.logEvent("Click confirm trial today");
                                    String token = prefs.getString("access_token", null);
                                    String accountId = prefs.getString("account_id", null);

                                    EditNumberFragment editNumberFragment = new EditNumberFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putString(EditNumberFragment.sourceFragment, OnlineSupportFragment.class.getSimpleName());
                                    editNumberFragment.setArguments(bundle);

                                    if (!TextUtils.isEmpty(accountId) && !TextUtils.isEmpty(token)) {
                                        AccountAdapter accountAdapter = new AccountAdapter(getActivity());
                                        Account account = accountAdapter.getAccountById(accountId);
                                        if (account.getStatus() == Account.Status.REQUEST_TRIAL && TextUtils.isEmpty(account.getPhoneNumber())) {
                                            getFragmentManager().beginTransaction()
                                                    .replace(R.id.fl_trial_submit, editNumberFragment)
                                                    .addToBackStack(null)
                                                    .commit();
                                        } else {
                                            getActivity().finish();
                                        }
                                    } else {
                                        getFragmentManager().beginTransaction()
                                                .replace(R.id.fl_trial_submit, editNumberFragment)
                                                .addToBackStack(null)
                                                .commit();
                                    }
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

        @Override
        public int getItemCount() {
            return mChatMessages.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private ImageView imvSupportIcon;
            private TextView tvReceivedMessage;
            private TextView tvSendMessage;
            private LinearLayout llChatLayout;
            private LinearLayout trialConfirmLayout;
            private TextView tutorName;
            private TextView scheduleDate;
            private TextView scheduleTime;
            private TextView confirm;
            private SimpleDraweeView tutorImg;

            public ViewHolder(View itemView) {
                super(itemView);

                tutorImg = itemView.findViewById(R.id.sdv_tutor_photo);
                imvSupportIcon = itemView.findViewById(R.id.imv_support_icon);
                tvReceivedMessage = itemView.findViewById(R.id.tv_receive_message);
                tvSendMessage = itemView.findViewById(R.id.tv_send_message);
                tutorName = itemView.findViewById(R.id.tutor_name);
                scheduleDate = itemView.findViewById(R.id.schedult_date);
                scheduleTime = itemView.findViewById(R.id.schedult_time);
                confirm = itemView.findViewById(R.id.confirm_trial);
                llChatLayout = itemView.findViewById(R.id.ll_chat_layout);
                trialConfirmLayout = itemView.findViewById(R.id.trial_request_message_layout);
            }
        }
    }
}
