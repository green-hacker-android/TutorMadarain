package inc.osbay.android.tutormandarin.ui.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.model.Booking;
import inc.osbay.android.tutormandarin.ui.fragment.BackHandledFragment;
import inc.osbay.android.tutormandarin.ui.fragment.ClassroomFAQFragment;
import inc.osbay.android.tutormandarin.ui.fragment.DemoClassroomFragment;
import inc.osbay.android.tutormandarin.ui.fragment.MainFragment;
import inc.osbay.android.tutormandarin.ui.fragment.OnlineSupportFragment;

public class ClassroomAssistantActivity extends AppCompatActivity implements
        BackHandledFragment.BackHandlerInterface {

    private String fragmentName;
    private Fragment newFragment = null;
    private BackHandledFragment mSelectedFragment;
    private Booking mBooking;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classroom_assistant);

        fragmentName = getIntent().getStringExtra("fragment");
        Bundle bundle = new Bundle();
        if (OnlineSupportFragment.class.getSimpleName().equals(fragmentName)) {
            newFragment = new OnlineSupportFragment();
            bundle.putString("class_type", "classroom");
            newFragment.setArguments(bundle);
        } else if (ClassroomFAQFragment.class.getSimpleName().equals(fragmentName)) {
            newFragment = new ClassroomFAQFragment();
            bundle.putString("class_type", "classroom");
            newFragment.setArguments(bundle);
        } else if (MainFragment.class.getSimpleName().equals(fragmentName)) {
            newFragment = new DemoClassroomFragment();
        }

        if (newFragment != null) {
            FragmentManager fm = getFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.frame_layout);
            if (fragment == null) {
                fm.beginTransaction()
                        .add(R.id.frame_layout, newFragment)
                        .commit();
            } else {
                fm.beginTransaction()
                        .replace(R.id.frame_layout, newFragment)
                        .addToBackStack(null)
                        .commit();
            }
        }
    }

    @Override
    public void setmSelectedFragment(BackHandledFragment backHandledFragment) {
        mSelectedFragment = backHandledFragment;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}