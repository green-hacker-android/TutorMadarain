package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.List;
import java.util.Locale;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.CountryCode;
import inc.osbay.android.tutormandarin.ui.activity.TrialSubmitActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditNumberFragment extends BackHandledFragment implements View.OnClickListener {

    public static String sourceFragment = "EditNumberFragment.SOURCE_FRAGMENT";
    private TrialSubmitActivity parentActivity;
    private List<CountryCode> mCountryCodes;
    private Spinner mPhoneCodeSpinner;
    private EditText mPhoneNoEditText;
    private String fragmentName;
    private LinearLayout trialHeader;
    private View view;

    public EditNumberFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FlurryAgent.logEvent("EditNumberFragment");
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_edit_number, container, false);

        ImageView imvClose = rootView.findViewById(R.id.imv_ic_close);
        imvClose.setOnClickListener(this);

        trialHeader = rootView.findViewById(R.id.trial_header);
        view = rootView.findViewById(R.id.view);
        fragmentName = getArguments().getString(sourceFragment);
        if (OnlineSupportFragment.class.getSimpleName().equals(fragmentName)) {
            trialHeader.setVisibility(View.GONE);
            view.setVisibility(View.GONE);
        } else if (ApplyTrialFragment.class.getSimpleName().equals(fragmentName)) {
            trialHeader.setVisibility(View.VISIBLE);
            view.setVisibility(View.VISIBLE);
        }
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
//            actionBar.setHomeAsUpIndicator(R.drawable.ic_cross_white);
        }

        TextView tvTitle = rootView.findViewById(R.id.trial_title);
        tvTitle.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Montserrat-Bold.ttf"));
        tvTitle.setText(getString(R.string.apply_free_trial));

        parentActivity = (TrialSubmitActivity) getActivity();
        AccountAdapter accountAdapter = new AccountAdapter(getActivity());
        mCountryCodes = accountAdapter.getCountryCodes();

        mPhoneCodeSpinner = rootView.findViewById(R.id.spn_ph_codes);
        mPhoneNoEditText = rootView.findViewById(R.id.edt_ph_number);

        rootView.findViewById(R.id.fab_verify_number).setOnClickListener(this);

        CountryCodeAdapter adapter = new CountryCodeAdapter();
        mPhoneCodeSpinner.setAdapter(adapter);
        showDefaultCode();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        showDefaultCode();
        if (!TextUtils.isEmpty(parentActivity.phoneNo))
            mPhoneNoEditText.setText(parentActivity.phoneNo);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imv_ic_close:
                onBackPressed();
                break;
            case R.id.fab_verify_number:
                String phoneCode = mPhoneCodeSpinner.getSelectedItem().toString();
                String phoneNo = mPhoneNoEditText.getText().toString();

                if (!validatePhoneNumber(phoneCode.concat(phoneNo))) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.et_valid_phone_notice))
                            .setMessage(getString(R.string.et_valid_phone_msg))
                            .setPositiveButton(getString(R.string.et_valid_phone_ok), null)
                            .create()
                            .show();
                    return;
                }

                parentActivity.phoneCode = phoneCode;
                parentActivity.phoneNo = phoneNo;

                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage(getString(R.string.et_dialog_sending_sms));
                progressDialog.setCancelable(false);
                progressDialog.show();

                ServerRequestManager requestManager = new ServerRequestManager(getActivity().getApplicationContext());
                requestManager.sendVerificationCode(phoneCode, phoneNo, new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        if (getActivity() != null) {
                            FlurryAgent.logEvent("Send Verification Code Success");
                            progressDialog.dismiss();

                            VerifyNumberFragment verifyNumberFragment = new VerifyNumberFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString(sourceFragment, fragmentName);
                            verifyNumberFragment.setArguments(bundle);

                            getFragmentManager().beginTransaction()
                                    .replace(R.id.fl_trial_submit, verifyNumberFragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        if (getActivity() != null) {
                            progressDialog.dismiss();

                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getString(R.string.et_verification_code_error))
                                    .setMessage(err.getMessage())
                                    .setPositiveButton(getString(R.string.et_verification_code_ok), null)
                                    .create()
                                    .show();
                        }
                    }
                });
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
        if (getView() != null) {
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
        FlurryAgent.logEvent("Edit number fragment back");
        getFragmentManager().popBackStack();
        return false;
    }

    public void showDefaultCode() {
        int countryCode;
        int countryCodeId = 0;
        if (TextUtils.isEmpty(parentActivity.phoneCode)) {
            TelephonyManager manager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            //getNetworkCountryIso
            String countryId = manager.getSimCountryIso().toUpperCase().trim();

            AccountAdapter accountAdapter = new AccountAdapter(getActivity());
            countryCode = accountAdapter.getCountryCodeByID(countryId);
            countryCodeId = accountAdapter.getCountryCodeIDByID(countryId);

            if (countryCode == 0) {
                countryCode = 1;
                countryCodeId = 226;
            }
        } else {
            countryCode = Integer.parseInt(parentActivity.phoneCode.replace("+", ""));
        }


        for (int i = 0; i < mCountryCodes.size(); i++) {
            if (mCountryCodes.get(i).getCode() == countryCode && mCountryCodes.get(i).getCodeId() == countryCodeId) {
                mPhoneCodeSpinner.setSelection(i);
                break;
            }
        }
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        Log.e("PhoneValidation", "validate number - " + phoneNumber);
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phoneNumberProto = phoneUtil.parse(phoneNumber, "");
            boolean isValid = phoneUtil.isValidNumber(phoneNumberProto); // returns true if valid
            if (isValid) {
                Log.e("PhoneValidation", "Valid");
                return true;
            }
        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }
        Log.e("PhoneValidation", "Invalid");
        return false;
    }

    private class CountryCodeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mCountryCodes.size();
        }

        @Override
        public Object getItem(int position) {
            return mCountryCodes.get(position);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            CountryCode code = mCountryCodes.get(position);

            TextView tv = new TextView(getActivity());
            tv.setText(String.format(Locale.getDefault(), "+%d %s", code.getCode(), code.getCountry()));
//            tv.setGravity(Gravity.END);
            tv.setPadding(20, 0, 20, 0);

            return tv;
        }

        @Override
        public View getDropDownView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.item_spinner_country_code,
                        viewGroup, false);
            }

            CountryCode code = mCountryCodes.get(position);

            TextView tvCountry = view.findViewById(R.id.tv_country);
            tvCountry.setText(code.getCountry());

            TextView tvCode = view.findViewById(R.id.tv_code);
            tvCode.setText(String.format(Locale.getDefault(), "+%d", code.getCode()));

            return view;
        }
    }
}
