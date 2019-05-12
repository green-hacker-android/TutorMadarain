package inc.osbay.android.tutormandarin.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import inc.osbay.android.tutormandarin.R;

public class AccountGenderActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String ACCOUNT_GENDER = "AccountGenderActivity.ACCOUNT_GENDER";
    private RadioGroup rgAcGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_gender);

        String gender = getIntent().getStringExtra(ACCOUNT_GENDER);

        rgAcGender = findViewById(R.id.rg_ac_gender);
        RadioButton rbGenderMale = findViewById(R.id.rb_gender_male);
        RadioButton rbGenderFemale = findViewById(R.id.rb_gender_female);

        TextView tvAccountGenderSave = findViewById(R.id.tv_account_gender_save);
        ImageView imvAccountGenderBack = findViewById(R.id.imv_account_gender_back);

        tvAccountGenderSave.setOnClickListener(this);
        imvAccountGenderBack.setOnClickListener(this);

        if (gender.equalsIgnoreCase("Female")) {
            rbGenderFemale.setChecked(true);
        } else {
            rbGenderMale.setChecked(true);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_account_gender_save:
                Intent intent = new Intent();
                int selectedId = rgAcGender.getCheckedRadioButtonId();
                if (selectedId == R.id.rb_gender_female) {
                    intent.putExtra(ACCOUNT_GENDER, "2");
                } else {
                    intent.putExtra(ACCOUNT_GENDER, "1");
                }

                this.setResult(RESULT_OK, intent);
                this.finish();

                break;
            case R.id.imv_account_gender_back:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }
}
