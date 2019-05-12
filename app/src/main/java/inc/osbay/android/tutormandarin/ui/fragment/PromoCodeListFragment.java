package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.ui.view.FragmentStatePagerAdapter;

public class PromoCodeListFragment extends BackHandledFragment {

    private PromoCodeTabAdapter mPromoCodeTabAdapter;

    public PromoCodeListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        setTitle(getString(R.string.ph_list_title));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPromoCodeTabAdapter = new PromoCodeTabAdapter(getChildFragmentManager());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_promo_code_list, container, false);
        Toolbar toolBar;
        toolBar = v.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#34404E"));
        setSupportActionBar(toolBar);

        TabLayout tlPromoCodeHistory = v.findViewById(R.id.tl_promo_code_history);
        ViewPager vpPromoCodeHistory = v.findViewById(R.id.vp_promo_code_history);
        vpPromoCodeHistory.setAdapter(mPromoCodeTabAdapter);
        tlPromoCodeHistory.setupWithViewPager(vpPromoCodeHistory);

        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStack();
        return false;
    }

    private class PromoCodeTabAdapter extends FragmentStatePagerAdapter {

        private PromoCodeTabAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new PromoCodeIEnterFragment();
                case 1:
                    return new PromoCodeReferByMeFragment();
                default:
                    return new PromoCodeIEnterFragment();
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return FragmentStatePagerAdapter.POSITION_NONE;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.ph_i_entered_title);
                case 1:
                    return getString(R.string.ph_refer_by_me_title);
            }
            return super.getPageTitle(position);
        }
    }
}
