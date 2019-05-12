package inc.osbay.android.tutormandarin.ui.activity;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.flurry.android.FlurryAgent;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.ui.fragment.BackHandledFragment;
import inc.osbay.android.tutormandarin.ui.fragment.TrialScheduleFragment;

public class TrialSubmitActivity extends AppCompatActivity implements
        BackHandledFragment.BackHandlerInterface {
    public static final String PHONE_VERIFIED = "TrialSubmitActivity.PHONE_VERIFIED";

    public String startTime;
    public String phoneCode;
    public String phoneNo;

    private BackHandledFragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FlurryAgent.logEvent("TrialSubmitActivity");
        setContentView(R.layout.activity_trial_submit);

        int phoneVerified = 0;
        if (getIntent() != null) {
            phoneVerified = getIntent().getIntExtra(PHONE_VERIFIED, 0);
        }

        FragmentManager fm = getFragmentManager();
        TrialScheduleFragment trialScheduleFragment = new TrialScheduleFragment();
        Bundle b = new Bundle();
        b.putInt(PHONE_VERIFIED, phoneVerified);
        trialScheduleFragment.setArguments(b);
        fm.beginTransaction()
                .add(R.id.fl_trial_submit, new TrialScheduleFragment())
                .commit();
    }

    @Override
    public void setmSelectedFragment(BackHandledFragment backHandledFragment) {
        currentFragment = backHandledFragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return currentFragment.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (currentFragment != null) {
            currentFragment.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}
