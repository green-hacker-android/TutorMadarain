package inc.osbay.android.tutormandarin.ui.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.ui.fragment.BackHandledFragment;
import inc.osbay.android.tutormandarin.ui.fragment.TutorInfoFragment;

public class TutorInfoActivity extends AppCompatActivity implements
        BackHandledFragment.BackHandlerInterface {
    public static final String EXTRA_TUTOR_ID = "TutorInfoActivity.EXTRA_TUTOR_ID";
    public static final String EXTRA_TUTOR_TIME = "TutorInfoActivity.EXTRA_TUTOR_TIME";
    public static final String EXTRA_BOOKING_TYPE = "TutorInfoActivity.EXTRA_BOOKING_TYPE";

    private BackHandledFragment mSelectedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_holder);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        Fragment newFragment = new TutorInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TutorInfoFragment.EXTRA_TUTOR_ID, getIntent().getStringExtra(EXTRA_TUTOR_ID));
        bundle.putString(TutorInfoFragment.EXTRA_TUTOR_TIME, getIntent().getStringExtra(EXTRA_TUTOR_TIME));
        bundle.putInt(TutorInfoFragment.EXTRA_BOOKING_TYPE, getIntent().getIntExtra(EXTRA_BOOKING_TYPE, 0));
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
