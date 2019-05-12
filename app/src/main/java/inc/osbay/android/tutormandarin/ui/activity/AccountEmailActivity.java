package inc.osbay.android.tutormandarin.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.util.CommonUtil;

public class AccountEmailActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String ACCOUNT_EMAIL = "AccountEmailActivity.ACCOUNT_EMAIL";
    private EditText mEtAccountEmail;
    private String mStAccountEmail;
    private LinearLayout mLlInvalidEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_email);
        mStAccountEmail = getIntent().getStringExtra(ACCOUNT_EMAIL);

        TextView tvAccountEmailSave = (TextView) findViewById(R.id.tv_account_email_save);
        mEtAccountEmail = (EditText) findViewById(R.id.et_account_email);
        ImageView imvAccountEmailBack = (ImageView) findViewById(R.id.imv_account_email_back);
        mLlInvalidEmail = (LinearLayout) findViewById(R.id.ll_invalid_email);

        tvAccountEmailSave.setOnClickListener(this);
        imvAccountEmailBack.setOnClickListener(this);
        if (mStAccountEmail != null) {
            mEtAccountEmail.setText(mStAccountEmail);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_account_email_save:
                Intent intent = new Intent();
                String email = mEtAccountEmail.getText().toString();
                if (CommonUtil.validateEmail(email)) {
                    mLlInvalidEmail.setVisibility(View.GONE);
                    if (mStAccountEmail.equals(mEtAccountEmail.getText().toString())) {
                        intent.putExtra(ACCOUNT_EMAIL, "");
                    } else {
                        intent.putExtra(ACCOUNT_EMAIL, mEtAccountEmail.getText().toString());
                    }
                    this.setResult(RESULT_OK, intent);
                    this.finish();
                } else {
                    mLlInvalidEmail.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.imv_account_email_back:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }
}
