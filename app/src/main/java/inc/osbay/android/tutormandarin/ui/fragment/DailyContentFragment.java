package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.flurry.android.FlurryAgent;

import inc.osbay.android.tutormandarin.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class DailyContentFragment extends BackHandledFragment implements View.OnClickListener {

    public DailyContentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();

        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
        setTitle(getString(R.string.main_daily_content));

        FlurryAgent.logEvent("Daily Content");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_daily_content, container, false);

        Toolbar toolBar;
        toolBar = rootView.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#D3825A"));
        setSupportActionBar(toolBar);

        LinearLayout llWhatsOn = rootView.findViewById(R.id.ll_whats_on);
        llWhatsOn.setOnClickListener(this);

        LinearLayout llVideos = rootView.findViewById(R.id.ll_videos);
        llVideos.setOnClickListener(this);

        LinearLayout llFlashCards = rootView.findViewById(R.id.ll_flash_cards);
        llFlashCards.setOnClickListener(this);

        return rootView;
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStackImmediate();
        return false;
    }

    @Override
    public void onClick(View view) {

        Fragment newFragment = null;
        switch (view.getId()) {
            case R.id.ll_whats_on:
                newFragment = new WhatsOnListFragment();
                break;
            case R.id.ll_videos:
                newFragment = new VideoListFragment();
                break;
            case R.id.ll_flash_cards:
                newFragment = new FlashCardListFragment();
                break;
        }

        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.container);

        if (newFragment != null) {
            if (fragment == null) {
                fm.beginTransaction()
                        .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                R.animator.fragment_out_new, R.animator.fragment_out_old)
                        .addToBackStack(null)
                        .add(R.id.container, newFragment).commit();
            } else {
                fm.beginTransaction()
                        .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                R.animator.fragment_out_new, R.animator.fragment_out_old)
                        .addToBackStack(null)
                        .replace(R.id.container, newFragment).commit();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return true;
    }
}
