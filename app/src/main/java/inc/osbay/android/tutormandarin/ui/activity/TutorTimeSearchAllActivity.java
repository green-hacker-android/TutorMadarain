package inc.osbay.android.tutormandarin.ui.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.ui.fragment.BackHandledFragment;
import inc.osbay.android.tutormandarin.ui.fragment.TutorTimeSearchAllFragment;

public class TutorTimeSearchAllActivity extends AppCompatActivity implements
        BackHandledFragment.BackHandlerInterface {

    public static final String EXTRA_TUTOR_ID = "TutorTimeSearchAllActivity.EXTRA_TUTOR_ID";
    public static final String EXTRA_SELECTED_TIME = "TutorTimeSearchAllActivity.EXTRA_SELECTED_TIME";
    public static final String EXTRA_BOOKING_TYPE = "TutorTimeSearchAllActivity.EXTRA_BOOKING_TYPE";
    public static final String EXTRA_SELECTED_CLASS = "TutorTimeSearchAllActivity.EXTRA_SELECTED_CLASS";
    public static final String EXTRA_SELECTED_CLASS_HR_POSITION = "TutorTimeSearchAllActivity.EXTRA_SELECTED_CLASS_POSITION";
    public static final String EXTRA_SELECTED_CLASS_MIN_POSITION = "TutorTimeSearchAllActivity.EXTRA_SELECTED_CLASS_MIN_POSITION";
    public static final String EXTRA_COLOR = "TutorTimeSearchAllActivity.EXTRA_COLOR";

    private BackHandledFragment mSelectedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_time_search_all);

        String tutorSearchId = null;
        String tutorSearchDate = null;
        String classStartTime = null;
        int classStartHrPosition = 0;
        int classStartMinPosition = 0;
        int bookingType = 0;
        String color = null;
        Intent intent = getIntent();
        if (intent != null) {
            color = intent.getStringExtra(EXTRA_COLOR);
            tutorSearchId = intent.getStringExtra(EXTRA_TUTOR_ID);
            tutorSearchDate = intent.getStringExtra(EXTRA_SELECTED_TIME);
            bookingType = intent.getIntExtra(EXTRA_BOOKING_TYPE, 0);
            classStartTime = intent.getStringExtra(EXTRA_SELECTED_CLASS);
            classStartHrPosition = intent.getIntExtra(EXTRA_SELECTED_CLASS_HR_POSITION, 0);
            classStartMinPosition = intent.getIntExtra(EXTRA_SELECTED_CLASS_MIN_POSITION, 0);
        }

        Fragment newFragment = new TutorTimeSearchAllFragment();

        Bundle bundle = new Bundle();
        bundle.putString(TutorTimeSearchAllFragment.EXTRA_COLOR, color);
        bundle.putString(TutorTimeSearchAllFragment.EXTRA_SEARCH_TUTOR_ID, tutorSearchId);
        bundle.putString(TutorTimeSearchAllFragment.EXTRA_SEARCH_DATE, tutorSearchDate);
        bundle.putInt(TutorTimeSearchAllFragment.EXTRA_SEARCH_BOOKING_TYPE, bookingType);
        bundle.putString(TutorTimeSearchAllFragment.EXTRA_SELECTED_CLASS, classStartTime);
        bundle.putInt(TutorTimeSearchAllFragment.EXTRA_SELECTED_CLASS_HR_POSITION, classStartHrPosition);
        bundle.putInt(TutorTimeSearchAllFragment.EXTRA_SELECTED_CLASS_MIN_POSITION, classStartMinPosition);
        newFragment.setArguments(bundle);

        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_holder_container);
        if (fragment == null) {
            fm.beginTransaction().add(R.id.fragment_holder_container, newFragment).commit();
        } else {
            fm.beginTransaction().replace(R.id.fragment_holder_container, newFragment).commit();
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
