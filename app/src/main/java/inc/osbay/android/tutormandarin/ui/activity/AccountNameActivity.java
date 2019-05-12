package inc.osbay.android.tutormandarin.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import inc.osbay.android.tutormandarin.R;

public class AccountNameActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String ACCOUNT_FIRST_NAME = "AccountNameActivity.ACCOUNT_FIRST_NAME";
    public static final String ACCOUNT_LAST_NAME = "AccountNameActivity.ACCOUNT_LAST_NAME";

    private EditText etAccountFirstName;
    private EditText etAccountLastName;

    private String mStFirstName;
    private String mStLastName;

    private LinearLayout mLlNameEmpty;
    private TextView mTvNameEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_name);

        mStFirstName = getIntent().getStringExtra(ACCOUNT_FIRST_NAME);
        mStLastName = getIntent().getStringExtra(ACCOUNT_LAST_NAME);

        etAccountFirstName = (EditText) findViewById(R.id.et_account_first_name);
        etAccountLastName = (EditText) findViewById(R.id.et_account_last_name);

        TextView tvAccountNameSave = (TextView) findViewById(R.id.tv_account_name_save);
        tvAccountNameSave.setOnClickListener(this);

        ImageView imvAccountNameBack = (ImageView) findViewById(R.id.imv_account_name_back);
        imvAccountNameBack.setOnClickListener(this);

        mLlNameEmpty = (LinearLayout) findViewById(R.id.ll_name_empty);
        mTvNameEmpty = (TextView) findViewById(R.id.tv_name_empty);

        if (mStFirstName != null) {
            etAccountFirstName.setText(mStFirstName);
        }

        if (mStLastName != null) {
            etAccountLastName.setText(mStLastName);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imv_account_name_back:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.tv_account_name_save:
                String firstName = etAccountFirstName.getText().toString();
                String lastName = etAccountLastName.getText().toString();

                Intent intent = new Intent();
                if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)) {
                    if (mStFirstName.equals(etAccountFirstName.getText().toString()) && mStLastName.equals(etAccountLastName.getText().toString())) {
                        intent.putExtra(ACCOUNT_FIRST_NAME, "");
                        intent.putExtra(ACCOUNT_LAST_NAME, "");
                    } else {
                        intent.putExtra(ACCOUNT_FIRST_NAME, etAccountFirstName.getText().toString());
                        intent.putExtra(ACCOUNT_LAST_NAME, etAccountLastName.getText().toString());
                    }

                    this.setResult(RESULT_OK, intent);
                    this.finish();

                    mLlNameEmpty.setVisibility(View.GONE);
                } else {
                    mTvNameEmpty.setText(getString(R.string.ac_name_empty));
                    mLlNameEmpty.setVisibility(View.VISIBLE);
                }
                break;
        }
    }
}
