package inc.osbay.android.tutormandarin.ui.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.ui.fragment.BackHandledFragment;
import inc.osbay.android.tutormandarin.ui.fragment.ChangeBookingFragment;
import inc.osbay.android.tutormandarin.ui.fragment.FlashCardPagerFragment;
import inc.osbay.android.tutormandarin.ui.fragment.MyBookingFragment;
import inc.osbay.android.tutormandarin.ui.fragment.SelectClassFragment;
import inc.osbay.android.tutormandarin.ui.fragment.SelectLessonFragment;
import inc.osbay.android.tutormandarin.ui.fragment.TrialExplainFragment;
import inc.osbay.android.tutormandarin.ui.fragment.TutorTimeSearchAllFragment;
import inc.osbay.android.tutormandarin.ui.fragment.TutorialFragment;

public class FragmentHolderActivity extends AppCompatActivity implements
        BackHandledFragment.BackHandlerInterface {
    public static final String EXTRA_DISPLAY_FRAGMENT = "TutorInfoActivity.EXTRA_DISPLAY_FRAGMENT";

    private BackHandledFragment mSelectedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_holder);

        Toolbar toolBar;
        toolBar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolBar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        Fragment newFragment = null;

        String fragmentName = getIntent().getStringExtra(EXTRA_DISPLAY_FRAGMENT);
        if (FlashCardPagerFragment.class.getSimpleName().equals(fragmentName)) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(FlashCardPagerFragment.EXTRA_FLASHCARD_DECK,
                    getIntent().getSerializableExtra(FlashCardPagerFragment.EXTRA_FLASHCARD_DECK));
            bundle.putInt(FlashCardPagerFragment.EXTRA_CARD_POSITION,
                    getIntent().getIntExtra(FlashCardPagerFragment.EXTRA_CARD_POSITION, 0));

            newFragment = new FlashCardPagerFragment();
            newFragment.setArguments(bundle);
        } else if (SelectLessonFragment.class.getSimpleName().equals(fragmentName)) {
            int mFrgType = getIntent().getIntExtra(LessonPdfActivity.EXTRA_BOOKING_FRAGMENT, 0);

            Bundle bundle = new Bundle();
            if (mFrgType == 1) {
                bundle.putString(MyBookingFragment.EXTRA_TUTOR_ID,
                        getIntent().getStringExtra(MyBookingFragment.EXTRA_TUTOR_ID));
                bundle.putString(MyBookingFragment.EXTRA_START_TIME,
                        getIntent().getStringExtra(MyBookingFragment.EXTRA_START_TIME));
                bundle.putInt(MyBookingFragment.EXTRA_BOOKING_TYPE,
                        getIntent().getIntExtra(MyBookingFragment.EXTRA_BOOKING_TYPE, 0));
            } else if (mFrgType == 2) {
                bundle.putString(ChangeBookingFragment.EXTRA_TUTOR_ID,
                        getIntent().getStringExtra(ChangeBookingFragment.EXTRA_TUTOR_ID));
                bundle.putString(ChangeBookingFragment.EXTRA_START_TIME,
                        getIntent().getStringExtra(ChangeBookingFragment.EXTRA_START_TIME));
                bundle.putInt(ChangeBookingFragment.EXTRA_BOOKING_TYPE,
                        getIntent().getIntExtra(ChangeBookingFragment.EXTRA_BOOKING_TYPE, 0));
                bundle.putString(ChangeBookingFragment.EXTRA_BOOKING_ID, getIntent().getStringExtra(ChangeBookingFragment.EXTRA_BOOKING_ID));
            }
            bundle.putInt(LessonPdfActivity.EXTRA_BOOKING_FRAGMENT, mFrgType);

            newFragment = new SelectLessonFragment();
            newFragment.setArguments(bundle);
        } else if (SelectClassFragment.class.getSimpleName().equals(fragmentName)) {
            newFragment = new SelectClassFragment();
        } else if (TutorialFragment.class.getSimpleName().equals(fragmentName)) {
            newFragment = new TutorialFragment();
        } else if (TrialExplainFragment.class.getSimpleName().equals(fragmentName)) {
            newFragment = new TrialExplainFragment();
        } else if (TutorTimeSearchAllFragment.class.getSimpleName().equals(fragmentName)) {
            newFragment = new TutorTimeSearchAllFragment();
        }

        if (newFragment != null) {
            FragmentManager fm = getFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.fragment_holder_container);
            if (fragment == null) {
                fm.beginTransaction()
                        .add(R.id.fragment_holder_container, newFragment).commit();
            } else {
                fm.beginTransaction()
                        .replace(R.id.fragment_holder_container, newFragment).commit();
            }
        } else {
            FragmentHolderActivity.this.finish();
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
