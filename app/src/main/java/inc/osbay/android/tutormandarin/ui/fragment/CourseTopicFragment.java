package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.ui.view.FragmentStatePagerAdapter;

public class CourseTopicFragment extends BackHandledFragment {

    public static final String COURSE_TOPIC_PREF = "CourseTopicFragment.COURSE_TOPIC_PREF";
    private static final String TAG = CourseTopicFragment.class.getSimpleName();
    TabLayout mTabLayout;
    ViewPager mViewPager;
    SharedPreferences sharedPreferences;
    SharedPreferences mTopicListPreferences;
    private CourseTopicTabAdapter mCourseTopicTabAdapter;
    private int mPage;
    private DrawerLayout mDrawerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "On Create");
        System.gc();
        mCourseTopicTabAdapter = new CourseTopicTabAdapter(getChildFragmentManager());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_course_topic, container, false);

        Toolbar toolBar;
        toolBar = rootView.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#95CFCB"));
        setSupportActionBar(toolBar);

        sharedPreferences = getActivity().getSharedPreferences(COURSE_TOPIC_PREF, Context.MODE_PRIVATE);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mTopicListPreferences = getActivity().getSharedPreferences(TopicListFragment.EXTRA_TOPIC_LEVEL, Context.MODE_PRIVATE);

        mTopicListPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mPage = sharedPreferences.getInt(COURSE_TOPIC_PREF, 0);

        mTabLayout = rootView.findViewById(R.id.tl_course_topic_tabs);
        mViewPager = rootView.findViewById(R.id.vw_course_topic_pager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                sharedPreferences.edit().putInt(COURSE_TOPIC_PREF, position).apply();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setAdapter(mCourseTopicTabAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        // Implement Right drawer
        mDrawerLayout = rootView.findViewById(R.id.drawer_layout);
        FavouriteDrawerFragment mFavouriteDrawerFragment = new FavouriteDrawerFragment();

        FragmentManager fragmentManager = getChildFragmentManager();
        Fragment oldDrawer = fragmentManager.findFragmentById(R.id.right_favorite_drawer);
        if (oldDrawer == null) {
            fragmentManager.beginTransaction().add(R.id.right_favorite_drawer, mFavouriteDrawerFragment)
                    .commitAllowingStateLoss();
        } else {
            fragmentManager.beginTransaction().replace(R.id.right_favorite_drawer, mFavouriteDrawerFragment)
                    .commitAllowingStateLoss();
        }

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                float moveFactor = (getResources().getDimension(R.dimen.navigation_drawer_width) * slideOffset);
                CoordinatorLayout frame = rootView.findViewById(R.id.cl_main_content);

                frame.setTranslationX(-moveFactor);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                FlurryAgent.logEvent("Favorite");
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "On Start");

        setTitle(getString(R.string.co_title));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);

        mCourseTopicTabAdapter.notifyDataSetChanged();

        String token = sharedPreferences.getString("access_token", null);
        String accountId = sharedPreferences.getString("account_id", null);
        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            if (i == 0) {
                mTabLayout.getTabAt(i).setIcon(R.drawable.ic_tab_course);
            } else {
                mTabLayout.getTabAt(i).setIcon(R.drawable.ic_tab_topic);
            }
        }

        TabLayout.Tab tab = mTabLayout.getTabAt(mPage);
        if (tab != null) {
            tab.select();
        }
    }

    public void openRightDrawer() {

        String token = sharedPreferences.getString("access_token", null);
        String accountId = sharedPreferences.getString("account_id", null);
        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
            mDrawerLayout.openDrawer(GravityCompat.END);
        }
    }

    public void closeDrawer() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
            mDrawerLayout.closeDrawer(GravityCompat.END, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "On Resume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "On Pause");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_course_topic, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.opt_favourite:
                openRightDrawer();
                break;
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawers();
        } else {
            if (getFragmentManager().getBackStackEntryCount() > 1) {
                getFragmentManager().beginTransaction()
                        .remove(CourseTopicFragment.this)
                        .commit();

                getFragmentManager().popBackStack(getFragmentManager().getBackStackEntryAt(0).getId(),
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else {
                getFragmentManager().popBackStack();
            }
            sharedPreferences.edit().putInt(COURSE_TOPIC_PREF, 0).apply();
            mTopicListPreferences.edit().putString(TopicListFragment.EXTRA_TOPIC_LEVEL, "All").apply();
        }
        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e(TAG, "On Attach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.e(TAG, "On Detached");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "On Destroy");
    }

    private class CourseTopicTabAdapter extends FragmentStatePagerAdapter {

        public CourseTopicTabAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new CourseListFragment();
                case 1:
                    return new TopicListFragment();
                default:
                    return new CourseListFragment();
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
                    return getString(R.string.co_tab_title);
                case 1:
                    return getString(R.string.to_tab_title);
            }
            return super.getPageTitle(position);
        }
    }
}
