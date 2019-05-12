package inc.osbay.android.tutormandarin.ui.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.ui.fragment.BackHandledFragment;
import inc.osbay.android.tutormandarin.ui.fragment.TutorTimeSelectFragment;

public class TutorTimeSelectActivity extends AppCompatActivity implements
        BackHandledFragment.BackHandlerInterface {
    public static final String EXTRA_WEEK_NO = "TutorTimeSelectActivity.EXTRA_WEEK_NO";
    public static final String EXTRA_BOOKING_TYPE = "TutorTimeSelectActivity.EXTRA_BOOKING_TYPE";

    public static final String EXTRA_TUTOR_ID = "TutorTimeSelectActivity.EXTRA_TUTOR_ID";
    public static final String EXTRA_TUTOR_TIME = "TutorTimeSelectActivity.EXTRA_TUTOR_TIME";

    private BackHandledFragment mSelectedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_time_select);

        Toolbar toolBar;
        toolBar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolBar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        int weekNo = 0;
        int bookingType = 0;
        String tutorId = null;
        String tutorSelectTime = null;
        Intent intent = getIntent();
        if (intent != null) {
            weekNo = intent.getIntExtra(EXTRA_WEEK_NO, 0);
            bookingType = intent.getIntExtra(EXTRA_BOOKING_TYPE, 0);
            tutorId = intent.getStringExtra(EXTRA_TUTOR_ID);
            tutorSelectTime = intent.getStringExtra(EXTRA_TUTOR_TIME);
        }

        Fragment newFragment = new TutorTimeSelectFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(TutorTimeSelectFragment.EXTRA_WEEK_NO, weekNo);
        bundle.putInt(TutorTimeSelectFragment.EXTRA_BOOKING_TYPE, bookingType);
        bundle.putString(TutorTimeSelectFragment.EXTRA_TUTOR_ID, tutorId);
        bundle.putString(TutorTimeSelectFragment.EXTRA_TUTOR_TIME, tutorSelectTime);
        newFragment.setArguments(bundle);

        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.tutor_select_container);
        if (fragment == null) {
            fm.beginTransaction().add(R.id.tutor_select_container, newFragment).commit();
        } else {
            fm.beginTransaction().replace(R.id.tutor_select_container, newFragment).commit();
        }
    }

    @Override
    public void setmSelectedFragment(BackHandledFragment backHandledFragment) {
        mSelectedFragment = backHandledFragment;
    }

    @Override
    public void onBackPressed() {
        if (mSelectedFragment != null) {
            mSelectedFragment.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return false;
    }
}
