package inc.osbay.android.tutormandarin.ui.activity;

import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;

public class CreateFCDeckActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mDeckTitleEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_fcdeck);

        FlurryAgent.logEvent("Create Custom Flashcard Deck");

        mDeckTitleEditText = (EditText) findViewById(R.id.edt_fc_deck_name);

        TextView tvCancel = (TextView) findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(this);

        TextView tvSave = (TextView) findViewById(R.id.tv_save);
        tvSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_cancel:
                FlurryAgent.logEvent("Cancel Custom Flashcard creation");
                CreateFCDeckActivity.this.finish();
                break;
            case R.id.tv_save:
                String deckTitle = mDeckTitleEditText.getText().toString();

                CurriculumAdapter curriculumAdapter = new CurriculumAdapter(CreateFCDeckActivity.this);
                boolean status = curriculumAdapter.insertCustomFCDeck(deckTitle);

                if(!status){
                    new AlertDialog.Builder(CreateFCDeckActivity.this)
                            .setTitle(getString(R.string.fc_create_error))
                            .setMessage(getString(R.string.fc_create_msg))
                            .setPositiveButton(getString(R.string.fc_create_ok), null)
                            .create()
                            .show();
                }else {
                    CreateFCDeckActivity.this.finish();
                }
                break;
        }
    }

}
