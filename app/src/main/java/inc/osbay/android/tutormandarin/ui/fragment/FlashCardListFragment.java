package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.ui.activity.CreateFCDeckActivity;
import inc.osbay.android.tutormandarin.ui.activity.FlashcardTutorialActivity;
import inc.osbay.android.tutormandarin.ui.view.FragmentStatePagerAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class FlashCardListFragment extends BackHandledFragment {

    private FloatingActionButton mAddCustomDeckFAB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_flash_card_list, container, false);

        Toolbar toolBar;
        toolBar = rootView.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#d87e5f"));
        setSupportActionBar(toolBar);

        TabLayout mTabLayout = rootView.findViewById(R.id.tl_flashcard_tabs);
        ViewPager mViewPager = rootView.findViewById(R.id.vw_flashcard_pager);
        mViewPager.setAdapter(new FlashCardsTabAdapter(getFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 1){
                    mAddCustomDeckFAB.setVisibility(View.VISIBLE);
                }else{
                    mAddCustomDeckFAB.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mAddCustomDeckFAB = rootView.findViewById(R.id.fab_add_custom_deck);
        mAddCustomDeckFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), CreateFCDeckActivity.class));
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        setTitle(getString(R.string.fc_title));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_flashcard_explain, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.opt_flashcard_help:
                Intent intent = new Intent(getActivity(), FlashcardTutorialActivity.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStack();
        return false;
    }

    private class FlashCardsTabAdapter extends FragmentStatePagerAdapter {

        public FlashCardsTabAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new FlashCardCommonFragment();
                case 1:
                    return new FlashCardCustomFragment();
                default:
                    return new FlashCardCommonFragment();
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.fc_common);
                case 1:
                    return getString(R.string.fc_custom);
            }
            return super.getPageTitle(position);
        }
    }

}
