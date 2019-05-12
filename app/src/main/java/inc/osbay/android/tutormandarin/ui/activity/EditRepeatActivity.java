package inc.osbay.android.tutormandarin.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import inc.osbay.android.tutormandarin.R;

public class EditRepeatActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String[] WEEK_DAYS = {"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY",
            "THURSDAY", "FRIDAY", "SATURDAY"};
    private static final String[] HOURS = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12",
            "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"};
    private static final String[] MINUTES = {"00", "30"};

    private NumberPicker mWeekDayPicker;
    private NumberPicker mHourPicker;
    private NumberPicker mMinutePicker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_repeat);

        mWeekDayPicker = (NumberPicker) findViewById(R.id.np_book_day);
        mWeekDayPicker.setMinValue(0);
        mWeekDayPicker.setMaxValue(6);
        mWeekDayPicker.setDisplayedValues(WEEK_DAYS);
//        mWeekDayPicker.setOnValueChangedListener(this);
        setDividerColor(mWeekDayPicker, Color.parseColor("#f6b512"));


        mHourPicker = (NumberPicker) findViewById(R.id.np_book_hour);
        mHourPicker.setMinValue(0);
        mHourPicker.setMaxValue(23);
        mHourPicker.setDisplayedValues(HOURS);
//        mHourPicker.setOnValueChangedListener(this);
        setDividerColor(mHourPicker, Color.parseColor("#f6b512"));

        mMinutePicker = (NumberPicker) findViewById(R.id.np_book_minute);
        mMinutePicker.setDisplayedValues(null);
        mMinutePicker.setMinValue(0);
        mMinutePicker.setMaxValue(1);
        mMinutePicker.setDisplayedValues(MINUTES);
//        mMinutePicker.setOnValueChangedListener(this);
        setDividerColor(mMinutePicker, Color.parseColor("#f6b512"));

        TextView tvCancel = (TextView) findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(this);

        Button btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_cancel:
                onBackPressed();
                break;
            case R.id.btn_save:
                Intent data = new Intent();
                data.putExtra(AutoRepeatActivity.EXTRA_WEEK_DAY, WEEK_DAYS[mWeekDayPicker.getValue()]);
                data.putExtra(AutoRepeatActivity.EXTRA_START_TIME, HOURS[mHourPicker.getValue()] +
                ":" + MINUTES[mMinutePicker.getValue()]);

                setResult(RESULT_OK, data);
                EditRepeatActivity.this.finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setDividerColor(NumberPicker picker, int color) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

}
