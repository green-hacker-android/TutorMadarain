package inc.osbay.android.tutormandarin.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import inc.osbay.android.tutormandarin.R;

public class StudentLevelFilterActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String EXTRA_ID = "CarActivity.EXTRA_ID";
    ImageView closeImg;
    TextView allLevelTV;
    TextView basicLevelTV;
    TextView kidLevelTV;
    TextView resetLevelFilter;
    TextView[] mTextViews;
    TextView applyTV;
    private boolean[] checked;
    private Bundle mBundle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_level_filter);

        setupUI();
        mBundle = this.getIntent().getExtras();
        if (mBundle != null) {
            Log.i("Intent", "not null");
            checked = mBundle.getBooleanArray("edit_selected");
            for (int i = 0; i < checked.length; i++) {
                if (checked[i] == true) {
                    //checked[i] = true;
                    mTextViews[i].setBackground(getDrawable(R.drawable.filter_level_selected));
                    mTextViews[i].setTextColor(getColor(R.color.light_red));
                }
            }
        }
    }

    public void setupUI() {
        checked = new boolean[10];
        mTextViews = new TextView[10];

        closeImg = findViewById(R.id.close);
        allLevelTV = findViewById(R.id.all_level_tv);
        basicLevelTV = findViewById(R.id.basic_level_tv);
        kidLevelTV = findViewById(R.id.kids_level_tv);
        resetLevelFilter = findViewById(R.id.reset_filter);
        applyTV = findViewById(R.id.apply_tv);

        mTextViews[0] = findViewById(R.id.beginner1_level_tv);
        mTextViews[1] = findViewById(R.id.beginner2_level_tv);
        mTextViews[2] = findViewById(R.id.intermediate1_level_tv);
        mTextViews[3] = findViewById(R.id.intermediate2_level_tv);
        mTextViews[4] = findViewById(R.id.advance1_level_tv);
        mTextViews[5] = findViewById(R.id.advance2_level_tv);
        mTextViews[6] = findViewById(R.id.kid0_level_tv);
        mTextViews[7] = findViewById(R.id.kid1_level_tv);
        mTextViews[8] = findViewById(R.id.kid2_level_tv);
        mTextViews[9] = findViewById(R.id.kid3_level_tv);

        closeImg.setOnClickListener(this);
        resetLevelFilter.setOnClickListener(this);
        allLevelTV.setOnClickListener(this);
        basicLevelTV.setOnClickListener(this);
        kidLevelTV.setOnClickListener(this);

        for (int i = 0; i < 10; i++) {
            mTextViews[i].setOnClickListener(this);
        }
        applyTV.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity();
    }

    public void finishActivity() {
        finish();
        overridePendingTransition(R.anim.slide_out_down, R.anim.slide_in_down);
    }

    public void clickedLevel(int position) {
        if (checked[position] == true) {
            checked[position] = false;
            mTextViews[position].setBackground(getDrawable(R.drawable.filter_level_unselected));
            mTextViews[position].setTextColor(getColor(R.color.gray));
        } else {
            checked[position] = true;
            mTextViews[position].setBackground(getDrawable(R.drawable.filter_level_selected));
            mTextViews[position].setTextColor(getColor(R.color.light_red));
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.all_level_tv) {
            allLevelTV.setBackground(getDrawable(R.drawable.filter_level_selected));
            allLevelTV.setTextColor(getColor(R.color.light_red));
            basicLevelTV.setBackground(getDrawable(R.drawable.filter_level_unselected));
            basicLevelTV.setTextColor(getColor(R.color.gray));
            kidLevelTV.setBackground(getDrawable(R.drawable.filter_level_unselected));
            kidLevelTV.setTextColor(getColor(R.color.gray));

            for (int i = 0; i < 10; i++) {
                mTextViews[i].setClickable(true);
                if (checked[i] == true) {
                    mTextViews[i].setBackground(getDrawable(R.drawable.filter_level_selected));
                    mTextViews[i].setTextColor(getColor(R.color.light_red));
                } else {
                    mTextViews[i].setBackground(getDrawable(R.drawable.filter_level_unselected));
                    mTextViews[i].setTextColor(getColor(R.color.gray));
                }
            }
        }
        if (id == R.id.basic_level_tv) {
            allLevelTV.setBackground(getDrawable(R.drawable.filter_level_unselected));
            allLevelTV.setTextColor(getColor(R.color.gray));
            basicLevelTV.setBackground(getDrawable(R.drawable.filter_level_selected));
            basicLevelTV.setTextColor(getColor(R.color.light_red));
            kidLevelTV.setBackground(getDrawable(R.drawable.filter_level_unselected));
            kidLevelTV.setTextColor(getColor(R.color.gray));

            for (int i = 0; i < 6; i++) {
                mTextViews[i].setClickable(true);
                if (checked[i] == true) {
                    mTextViews[i].setBackground(getDrawable(R.drawable.filter_level_selected));
                    mTextViews[i].setTextColor(getColor(R.color.light_red));
                } else {
                    mTextViews[i].setBackground(getDrawable(R.drawable.filter_level_unselected));
                    mTextViews[i].setTextColor(getColor(R.color.gray));
                }
            }

            for (int i = 6; i < 10; i++) {
                checked[i] = false;
                mTextViews[i].setClickable(false);
                mTextViews[i].setBackground(getDrawable(R.drawable.disabled_background));
                mTextViews[i].setTextColor(getColor(R.color.view_light_gray));
            }
        }
        if (id == R.id.kids_level_tv) {
            allLevelTV.setBackground(getDrawable(R.drawable.filter_level_unselected));
            allLevelTV.setTextColor(getColor(R.color.gray));
            basicLevelTV.setBackground(getDrawable(R.drawable.filter_level_unselected));
            basicLevelTV.setTextColor(getColor(R.color.gray));
            kidLevelTV.setBackground(getDrawable(R.drawable.filter_level_selected));
            kidLevelTV.setTextColor(getColor(R.color.light_red));

            for (int i = 0; i < 6; i++) {
                checked[i] = false;
                mTextViews[i].setClickable(false);
                mTextViews[i].setBackground(getDrawable(R.drawable.disabled_background));
                mTextViews[i].setTextColor(getColor(R.color.view_light_gray));
            }

            for (int i = 6; i < 10; i++) {
                mTextViews[i].setClickable(true);
                if (checked[i] == true) {
                    mTextViews[i].setBackground(getDrawable(R.drawable.filter_level_selected));
                    mTextViews[i].setTextColor(getColor(R.color.light_red));
                } else {
                    mTextViews[i].setBackground(getDrawable(R.drawable.filter_level_unselected));
                    mTextViews[i].setTextColor(getColor(R.color.gray));
                }
            }
        }
        if (id == R.id.reset_filter) {
            for (int i = 0; i < 10; i++) {
                checked[i] = false;
            }
            Intent i = new Intent();
            i.putExtra(EXTRA_ID, checked);
            setResult(RESULT_OK, i);
            finishActivity();
        }
        if (id == R.id.close) {
            finishActivity();
        }
        if (id == R.id.apply_tv) {
            int count = 0;
            for (int i = 0; i < 10; i++) {
                if (checked[i] == true) {
                    count++;
                }
            }/** If nothing is selected **/
            if (count == 0) {
                android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(this);
                dialog.setCancelable(false);
                //dialog.setTitle(getString(R.string.critical_update));
                dialog.setMessage(getString(R.string.select_level));
                dialog.setPositiveButton(getString(R.string.si_dialog_ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create();
                dialog.show();
            } /** If somthing is selected **/
            else {
                Intent i = new Intent();
                i.putExtra(EXTRA_ID, checked);
                setResult(RESULT_OK, i);
                finishActivity();
            }
        }
        if (id == R.id.beginner1_level_tv) {
            clickedLevel(0);
        }
        if (id == R.id.beginner2_level_tv) {
            clickedLevel(1);
        }
        if (id == R.id.intermediate1_level_tv) {
            clickedLevel(2);
        }
        if (id == R.id.intermediate2_level_tv) {
            clickedLevel(3);
        }
        if (id == R.id.advance1_level_tv) {
            clickedLevel(4);
        }
        if (id == R.id.advance2_level_tv) {
            clickedLevel(5);
        }
        if (id == R.id.kid0_level_tv) {
            clickedLevel(6);
        }
        if (id == R.id.kid1_level_tv) {
            clickedLevel(7);
        }
        if (id == R.id.kid2_level_tv) {
            clickedLevel(8);
        }
        if (id == R.id.kid3_level_tv) {
            clickedLevel(9);
        }
    }
}