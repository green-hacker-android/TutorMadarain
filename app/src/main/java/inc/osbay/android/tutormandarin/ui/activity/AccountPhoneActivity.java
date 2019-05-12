package inc.osbay.android.tutormandarin.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.CountryCode;

public class AccountPhoneActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String ACCOUNT_PHONE_CODE = "AccountPhoneActivity.ACCOUNT_PHONE_CODE";
    public static final String ACCOUNT_PHONE_NUMBER = "AccountPhoneActivity.ACCOUNT_PHONE_NUMBER";
    private String mPhoneCodeStr;
    private String mPhoneNoStr;
    private int mSpPosition;
    private Spinner mSpnAccountPhCodes;
    private EditText mEtAccountPhoneNumber;
    private LinearLayout mLlPhoneEmpty;

    private List<CountryCode> mCountryCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_phone);

        AccountAdapter accountAdapter = new AccountAdapter(this);
        mCountryCodes = accountAdapter.getCountryCodes();

        mPhoneCodeStr = getIntent().getStringExtra(ACCOUNT_PHONE_CODE);
        mPhoneNoStr = getIntent().getStringExtra(ACCOUNT_PHONE_NUMBER);

        TextView tvAccountPhoneSave = (TextView) findViewById(R.id.tv_account_phone_save);
        ImageView imvAccountPhoneBack = (ImageView) findViewById(R.id.imv_account_phone_back);
        mEtAccountPhoneNumber = (EditText) findViewById(R.id.edt_account_phone);
        mLlPhoneEmpty = (LinearLayout) findViewById(R.id.ll_phone_empty);

        mSpnAccountPhCodes = (Spinner) findViewById(R.id.spn_account_ph_codes);
        CountryCodeAdapter adapter = new CountryCodeAdapter();
        mSpnAccountPhCodes.setAdapter(adapter);

        for (int i = 0; i < mCountryCodes.size(); i++) {
            if (mCountryCodes.get(i).toString().equals(mPhoneCodeStr)) {
                mSpPosition = i;
                break;
            }
        }

        if (mPhoneCodeStr != null) {
            mEtAccountPhoneNumber.setText(String.valueOf(mPhoneNoStr));
            if (TextUtils.isEmpty(mPhoneCodeStr) || TextUtils.isEmpty(mPhoneNoStr)) {
                mSpnAccountPhCodes.setSelection(0);
            } else {
                mSpnAccountPhCodes.setSelection(mSpPosition);
            }
        }

        imvAccountPhoneBack.setOnClickListener(this);
        tvAccountPhoneSave.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imv_account_phone_back:
                this.setResult(RESULT_CANCELED);
                this.finish();
                break;
            case R.id.tv_account_phone_save:
                Intent intent = new Intent();
                String phNumber = mEtAccountPhoneNumber.getText().toString();
                if (!TextUtils.isEmpty(phNumber)) {
                    if (mPhoneCodeStr.equals(mSpnAccountPhCodes.getSelectedItem().toString()) && mPhoneNoStr.equals(mEtAccountPhoneNumber.getText().toString())) {
                        intent.putExtra(ACCOUNT_PHONE_CODE, "");
                        intent.putExtra(ACCOUNT_PHONE_NUMBER, "");
                    } else {
                        intent.putExtra(ACCOUNT_PHONE_CODE, mSpnAccountPhCodes.getSelectedItem().toString());
                        intent.putExtra(ACCOUNT_PHONE_NUMBER, mEtAccountPhoneNumber.getText().toString());
                    }
                    this.setResult(RESULT_OK, intent);
                    this.finish();
                    mLlPhoneEmpty.setVisibility(View.GONE);
                } else {
                    mLlPhoneEmpty.setVisibility(View.VISIBLE);
                }
                break;
        }
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

            TextView tv = new TextView(AccountPhoneActivity.this);
            tv.setText(String.format(Locale.getDefault(), "+%d", code.getCode()));
            tv.setGravity(Gravity.END);
            tv.setPadding(0,0, 10,0);
            tv.setTextColor(Color.BLACK);

            return tv;
        }

        @Override
        public View getDropDownView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(AccountPhoneActivity.this).inflate(R.layout.item_spinner_country_code,
                        viewGroup, false);
            }

            CountryCode code = mCountryCodes.get(position);

            TextView tvCountry = (TextView) view.findViewById(R.id.tv_country);
            tvCountry.setText(code.getCountry());

            TextView tvCode = (TextView) view.findViewById(R.id.tv_code);
            tvCode.setText(String.format(Locale.getDefault(), "+%d", code.getCode()));

            return view;
        }
    }
}
