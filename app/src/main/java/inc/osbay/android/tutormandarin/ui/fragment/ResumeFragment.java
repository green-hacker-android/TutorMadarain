package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.ui.view.FragmentStatePagerAdapter;

public class ResumeFragment extends BackHandledFragment {

    private ServerRequestManager mRequestManager;
    private ResumeTabAdapter mResumeTabAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRequestManager = new ServerRequestManager(getActivity()
                .getApplicationContext());
        mResumeTabAdapter = new ResumeTabAdapter(getChildFragmentManager());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_resume, container, false);

        Toolbar toolBar;
        toolBar = (Toolbar) v.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#34404E"));
        setSupportActionBar(toolBar);

        TabLayout mTabLayout = (TabLayout) v.findViewById(R.id.tl_resume_tabs);
        ViewPager mViewPager = (ViewPager) v.findViewById(R.id.vw_resume_pager);
        mViewPager.setAdapter(mResumeTabAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    FlurryAgent.logEvent("Select Notes Tab (Resume)");
                } else if (tab.getPosition() == 2) {
                    FlurryAgent.logEvent("Select Badges Tab (Resume)");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        setDisplayHomeAsUpEnable(true);
        setTitle(getString(R.string.re_title));
        setHasOptionsMenu(true);

        FlurryAgent.logEvent("Resume");
    }

    @Override
    public void onResume() {
        super.onResume();

        mRequestManager.getStudentInfo(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                if (getActivity() != null) {
                    mResumeTabAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(ServerError err) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), err.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mRequestManager.downloadNoteList(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {

                if (getActivity() != null) {
                    mResumeTabAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(ServerError err) {
                if (getActivity() != null && err.getErrorCode() != 10072) { // no class information
                    Toast.makeText(getActivity(), getString(R.string.re_note_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mRequestManager.downloadBadgesList(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                if (getActivity() != null) {
                    mResumeTabAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(ServerError err) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), getString(R.string.re_badges_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return true;
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStack();
        return false;
    }

    private class ResumeTabAdapter extends FragmentStatePagerAdapter {

        public ResumeTabAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ResumeStatusFragment();
                case 1:
                    return new ResumeNoteFragment();
                case 2:
                    return new ResumeBadgesFragment();
                default:
                    return new ResumeStatusFragment();
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.re_status_tab_title);
                case 1:
                    return getString(R.string.re_note_tab_title);
                case 2:
                    return getString(R.string.re_badges_tab_title);
            }
            return super.getPageTitle(position);
        }
    }
}
