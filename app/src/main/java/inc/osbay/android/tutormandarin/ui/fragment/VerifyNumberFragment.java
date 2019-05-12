package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.felipecsl.gifimageview.library.GifImageView;
import com.flurry.android.FlurryAgent;
import com.github.clans.fab.FloatingActionButton;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.ui.activity.MainActivity;
import inc.osbay.android.tutormandarin.ui.activity.TrialSubmitActivity;
import inc.osbay.android.tutormandarin.util.CommonUtil;

import static inc.osbay.android.tutormandarin.ui.fragment.EditNumberFragment.sourceFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class VerifyNumberFragment extends BackHandledFragment implements View.OnClickListener {

    private TrialSubmitActivity parentActivity;
    private SmsListener smsReceiver;

    private TextView tvNumber1;
    private TextView tvNumber2;
    private TextView tvNumber3;
    private TextView tvNumber4;
    private EditText edtVerificationCode;
    private FloatingActionButton fabSuccess;
    private FloatingActionButton fabFail;
    private GifImageView givLoading;
    private String fragmentName;
    private LinearLayout trialHeader;
    private View view;

    public VerifyNumberFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FlurryAgent.logEvent("VerifyNumberFragment");
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_verify_number, container, false);

        ImageView imvClose = rootView.findViewById(R.id.imv_ic_close);
        imvClose.setOnClickListener(this);

        trialHeader = rootView.findViewById(R.id.trial_header);
        view = rootView.findViewById(R.id.view);
        fragmentName = getArguments().getString(sourceFragment);
        if (OnlineSupportFragment.class.getSimpleName().equals(fragmentName)) {
            view.setVisibility(View.GONE);
            trialHeader.setVisibility(View.GONE);
        } else if (ApplyTrialFragment.class.getSimpleName().equals(fragmentName)) {
            trialHeader.setVisibility(View.VISIBLE);
            view.setVisibility(View.VISIBLE);
        }

        /*Toolbar toolbar;
        toolbar = rootView.findViewById(R.id.tool_bar);
        toolbar.setBackgroundColor(Color.parseColor("#E45F56"));
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);*/

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
//            actionBar.setHomeAsUpIndicator(R.drawable.ic_cross_white);
        }

        TextView tvTitle = rootView.findViewById(R.id.trial_title);
        tvTitle.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Montserrat-Bold.ttf"));
        tvTitle.setText(getContext().getString(R.string.apply_free_trial));

        parentActivity = (TrialSubmitActivity) getActivity();

        TextView tvVerifyNumber = rootView.findViewById(R.id.tv_verify_number);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvVerifyNumber.setText(Html.fromHtml(getString(R.string.su_verify_number_desc,
                    parentActivity.phoneCode, parentActivity.phoneNo), Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvVerifyNumber.setText(Html.fromHtml(getString(R.string.su_verify_number_desc,
                    parentActivity.phoneCode, parentActivity.phoneNo)));
        }

        givLoading = rootView.findViewById(R.id.giv_loading_sms);

        fabSuccess = rootView.findViewById(R.id.fab_verify_success);
//        fabSuccess.setOnClickListener(this);

        fabFail = rootView.findViewById(R.id.fab_verify_fail);
        fabFail.setOnClickListener(this);

        rootView.findViewById(R.id.tv_resend_code).setOnClickListener(this);

        tvNumber1 = rootView.findViewById(R.id.tv_number_1);
        tvNumber1.setOnClickListener(this);

        tvNumber2 = rootView.findViewById(R.id.tv_number_2);
        tvNumber2.setOnClickListener(this);

        tvNumber3 = rootView.findViewById(R.id.tv_number_3);
        tvNumber3.setOnClickListener(this);

        tvNumber4 = rootView.findViewById(R.id.tv_number_4);
        tvNumber4.setOnClickListener(this);

        edtVerificationCode = rootView.findViewById(R.id.edt_verification_code);
        edtVerificationCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tvNumber4.setText(numberOf(3));
                tvNumber3.setText(numberOf(2));
                tvNumber2.setText(numberOf(1));
                tvNumber1.setText(numberOf(0));

                if (edtVerificationCode.getText().toString().length() == 4
                        && !(fabSuccess.getVisibility() == View.VISIBLE
                        || fabFail.getVisibility() == View.VISIBLE)) {
                    givLoading.setVisibility(View.VISIBLE);
                    givLoading.startAnimation();

                    verifyNumber(edtVerificationCode.getText().toString());
                } else {
                    givLoading.setVisibility(View.GONE);
                    givLoading.stopAnimation();
                }
            }
        });

        AssetManager assetManager = getActivity().getAssets();
        try {
            InputStream is = assetManager.open("loading_sms.gif");
            byte[] fileBytes = new byte[is.available()];

            is.read(fileBytes);
            is.close();

            givLoading.setBytes(fileBytes);
        } catch (IOException e) {
            // handle exception
            e.printStackTrace();
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

//        edtVerificationCode.requestFocus();
//        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(edtVerificationCode, InputMethodManager.SHOW_IMPLICIT);

        smsReceiver = new SmsListener();
        getActivity().registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().unregisterReceiver(smsReceiver);
    }

    private String numberOf(int pos) {
        String code = edtVerificationCode.getText().toString();
        if (!TextUtils.isEmpty(code)
                && edtVerificationCode.length() > pos) {
            return String.valueOf(code.charAt(pos));
        }
        return "";
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imv_ic_close:
                onBackPressed();
                break;

            case R.id.tv_number_1:
            case R.id.tv_number_2:
            case R.id.tv_number_3:
            case R.id.tv_number_4:
                edtVerificationCode.requestFocus();
                edtVerificationCode.setSelection(edtVerificationCode.getText().length());
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(edtVerificationCode, InputMethodManager.SHOW_IMPLICIT);
                break;
            case R.id.tv_resend_code:
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Sending SMS...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                ServerRequestManager requestManager = new ServerRequestManager(getActivity().getApplicationContext());
                requestManager.sendVerificationCode(parentActivity.phoneCode, parentActivity.phoneNo, new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        if (getActivity() != null) {
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        if (getActivity() != null) {
                            progressDialog.dismiss();

                            new AlertDialog.Builder(getActivity())
                                    .setTitle("Error")
                                    .setMessage(err.getMessage())
                                    .setPositiveButton("OK", null)
                                    .create()
                                    .show();
                        }
                    }
                });
                break;
            case R.id.fab_verify_success:
                Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("class_type", "normal");
                mainIntent.putExtras(bundle);
                mainIntent.setAction("refresh_fragment");
                startActivity(mainIntent);
                getActivity().finish();
                break;
            case R.id.fab_verify_fail:
                fabFail.setVisibility(View.GONE);
                edtVerificationCode.setText("");
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        FlurryAgent.logEvent("Verify Number Back");
        getFragmentManager().popBackStack();
        return false;
    }

    /*** Verify Phone Number > Confirm Trial > Update Student Info ***/
    private void verifyNumber(String code) {
        final ServerRequestManager requestManager = new ServerRequestManager(getActivity().getApplicationContext());
        try {
            requestManager.verifyPhoneNumber(code,
                    CommonUtil.changeLocalDateToUTC(), new ServerRequestManager.OnRequestFinishedListener() {
                        @Override
                        public void onSuccess(Object result) {
                            FlurryAgent.logEvent("Verify Number Success");
                            confirmTrial(requestManager);
                        }

                        @Override
                        public void onError(ServerError err) {
                            givLoading.setVisibility(View.GONE);
                            fabFail.setVisibility(View.VISIBLE);

                            String errMsg;

                            if (err.getMessage().equals("Invalid Verification Code")) {
                                errMsg = getString(R.string.et_invalid_code_error_msg);
                            } else {
                                errMsg = err.getMessage();
                            }

                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getString(R.string.et_invalid_code_error_title))
                                    .setMessage(errMsg)
                                    .setPositiveButton("OK", null)
                                    .create()
                                    .show();
                        }
                    });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void confirmTrial(final ServerRequestManager requestManager) {
        if (OnlineSupportFragment.class.getSimpleName().equals(fragmentName)) {
            requestManager.confirmTrialToday(new ServerRequestManager.OnRequestFinishedListener() {
                @Override
                public void onSuccess(Object result) {
                    FlurryAgent.logEvent("Confirm Trial Success!");
                    getStudentInfo(requestManager);
                }

                @Override
                public void onError(ServerError err) {
                    FlurryAgent.logEvent("Confirm Trial Failed!");
                    Toast.makeText(getContext(), err.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if (ApplyTrialFragment.class.getSimpleName().equals(fragmentName)) {
            requestManager.confirmTrial(new ServerRequestManager.OnRequestFinishedListener() {
                @Override
                public void onSuccess(Object result) {
                    FlurryAgent.logEvent("Confirm Trial Success!");
                    getStudentInfo(requestManager);
                }

                @Override
                public void onError(ServerError err) {
                    FlurryAgent.logEvent("Confirm Trial Failed!");
                    Toast.makeText(getContext(), err.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void getStudentInfo(ServerRequestManager requestManager) {
        requestManager.getStudentInfo(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                FlurryAgent.logEvent("Updating Student Info Success");
                givLoading.setVisibility(View.GONE);
                fabSuccess.setVisibility(View.VISIBLE);

                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("class_type", "normal");
                        mainIntent.putExtras(bundle);
                        mainIntent.setAction("refresh_fragment");
                        startActivity(mainIntent);
                        getActivity().finish();
                    }
                };
                new Timer().schedule(task, 800);
            }

            @Override
            public void onError(ServerError err) {
                givLoading.setVisibility(View.GONE);
                fabSuccess.setVisibility(View.VISIBLE);
            }
        });
    }

    public class SmsListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
                SmsMessage[] msgs;
//                String msg_from;
                if (bundle != null) {
                    //---retrieve the SMS message received---
                    try {
                        Object[] pdus = (Object[]) bundle.get("pdus");
                        msgs = new SmsMessage[pdus.length];
                        for (int i = 0; i < msgs.length; i++) {
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
//                            msg_from = msgs[i].getOriginatingAddress();
                            String msgBody = msgs[i].getMessageBody();

                            Pattern p = Pattern.compile("\\d+");
                            Matcher m = p.matcher(msgBody);
                            if (m.find()) {
                                edtVerificationCode.setText(m.group());
                                edtVerificationCode.setSelection(edtVerificationCode.getText().length());
                            }

                        }
                    } catch (Exception e) {
//                            Log.d("Exception caught",e.getMessage());
                    }
                }
            }
        }
    }
}
