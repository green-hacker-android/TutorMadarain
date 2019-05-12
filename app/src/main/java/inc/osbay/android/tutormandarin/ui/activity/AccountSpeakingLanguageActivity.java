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

public class AccountSpeakingLanguageActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String ACCOUNT_SPEAKING_LANGUAGE = "AccountSpeakingLanguageActivity.ACCOUNT_SPEAKING_LANGUAGE";
    private RadioGroup rgAcSpeaking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_speaking_language);
        String speaking = getIntent().getStringExtra(ACCOUNT_SPEAKING_LANGUAGE);

        rgAcSpeaking = findViewById(R.id.rg_ac_speaking);
        RadioButton rbSpeakingEnglish = findViewById(R.id.rb_speaking_english);
        RadioButton rbSpeakingFrench = findViewById(R.id.rb_speaking_french);
        RadioButton rbSpeakingGerman = findViewById(R.id.rb_speaking_german);
        RadioButton rbSpeakingJapan = findViewById(R.id.rb_speaking_japan);
        RadioButton rbSpeakingKorea = findViewById(R.id.rb_speaking_korea);
        RadioButton rbSpeakingThai = findViewById(R.id.rb_speaking_thai);
        RadioButton rbSpeakingOther = findViewById(R.id.rb_speaking_other);

        TextView tvAccountSpeakingSave = findViewById(R.id.tv_account_speaking_save);
        ImageView imvAccountSpeakingBack = findViewById(R.id.imv_account_speaking_back);

        tvAccountSpeakingSave.setOnClickListener(this);
        imvAccountSpeakingBack.setOnClickListener(this);

        if (speaking.equalsIgnoreCase("fr")) {
            rbSpeakingFrench.setChecked(true);
        } else if (speaking.equalsIgnoreCase("de")) {
            rbSpeakingGerman.setChecked(true);
        } else if (speaking.equalsIgnoreCase("ja")) {
            rbSpeakingJapan.setChecked(true);
        } else if (speaking.equalsIgnoreCase("ko")) {
            rbSpeakingKorea.setChecked(true);
        } else if (speaking.equalsIgnoreCase("th")) {
            rbSpeakingThai.setChecked(true);
        } else if (speaking.equalsIgnoreCase("en")) {
            rbSpeakingEnglish.setChecked(true);
        } else {
            rbSpeakingOther.setChecked(true);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_account_speaking_save:
                Intent intent = new Intent();
                int selectedId = rgAcSpeaking.getCheckedRadioButtonId();
                if (selectedId == R.id.rb_speaking_english) {
                    intent.putExtra(ACCOUNT_SPEAKING_LANGUAGE, "en");
                } else if (selectedId == R.id.rb_speaking_french) {
                    intent.putExtra(ACCOUNT_SPEAKING_LANGUAGE, "fr");
                } else if (selectedId == R.id.rb_speaking_german) {
                    intent.putExtra(ACCOUNT_SPEAKING_LANGUAGE, "de");
                } else if (selectedId == R.id.rb_speaking_japan) {
                    intent.putExtra(ACCOUNT_SPEAKING_LANGUAGE, "ja");
                } else if (selectedId == R.id.rb_speaking_korea) {
                    intent.putExtra(ACCOUNT_SPEAKING_LANGUAGE, "ko");
                } else if (selectedId == R.id.rb_speaking_thai) {
                    intent.putExtra(ACCOUNT_SPEAKING_LANGUAGE, "th");
                } else {
                    intent.putExtra(ACCOUNT_SPEAKING_LANGUAGE, "en");
                }

                this.setResult(RESULT_OK, intent);
                this.finish();

                break;
            case R.id.imv_account_speaking_back:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }
}