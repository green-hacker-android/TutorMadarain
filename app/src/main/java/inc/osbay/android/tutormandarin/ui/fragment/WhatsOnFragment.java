package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flurry.android.FlurryAgent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.sdk.model.WhatsOn;
import inc.osbay.android.tutormandarin.ui.activity.ShareActivity;
import inc.osbay.android.tutormandarin.ui.view.FragmentStatePagerAdapter;
import inc.osbay.android.tutormandarin.ui.view.ListDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class WhatsOnFragment extends BackHandledFragment {
    public static final String EXTRA_WHATS_ON_ID = "whats_on_id";

    private WhatsOn mWhatsOn;

    private Account mAccount;

    private ServerRequestManager mServerRequestManager;
    private WhatsOnTabAdapter mWhatsOnTabAdapter;

    public WhatsOnFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mServerRequestManager = new ServerRequestManager(getActivity());

        String whatsOnId = getArguments().getString(EXTRA_WHATS_ON_ID);

        CurriculumAdapter mCurriculumAdapter = new CurriculumAdapter(getActivity());
        mWhatsOn = mCurriculumAdapter.getWhatsOnById(whatsOnId);

        mWhatsOnTabAdapter = new WhatsOnTabAdapter(getChildFragmentManager());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String token = prefs.getString("access_token", null);
        String accountId = prefs.getString("account_id", null);

        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
            AccountAdapter accountAdapter = new AccountAdapter(getActivity());
            mAccount = accountAdapter.getAccountById(accountId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_whats_on, container, false);

        Toolbar toolBar;
        toolBar = v.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        setSupportActionBar(toolBar);

        TextView tvWhatsOnDetailTitle = v.findViewById(R.id.tv_whats_on_detail_title);
        tvWhatsOnDetailTitle.setText(mWhatsOn.getTitle());

        SimpleDraweeView sdvCoverPhoto = v.findViewById(R.id.sdv_cover_photo);
        if (!TextUtils.isEmpty(mWhatsOn.getCoverPhoto())) {
            sdvCoverPhoto.setImageURI(Uri.parse(mWhatsOn.getCoverPhoto()));
        }

        TabLayout mTlWhatsOnTab = v.findViewById(R.id.tl_whats_on_tabs);

        ViewPager mWhatsOnTabPager = v.findViewById(R.id.vp_whats_on_pager);
        mWhatsOnTabPager.setAdapter(mWhatsOnTabAdapter);
        mTlWhatsOnTab.setupWithViewPager(mWhatsOnTabPager);

        mTlWhatsOnTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    FlurryAgent.logEvent("View what's on vocab");
                } else if (tab.getPosition() == 2) {
                    FlurryAgent.logEvent("View what's on grammar");
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

        setTitle("");
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        mServerRequestManager.downloadWhatsOnVocabList(mWhatsOn.getWhatsOnId(),
                new ServerRequestManager.OnRequestFinishedListener() {

                    @Override
                    public void onSuccess(Object result) {
                        if (getActivity() != null) {
                            mWhatsOnTabAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), err.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_lesson, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.opt_more:
                String[] menuItems = {getString(R.string.wo_add_to_favourite), getString(R.string.wo_share)};
                int[] menuIcons = {R.drawable.ic_add_to_favorite, R.drawable.ic_menu_share};

                ListDialog customMenu = new ListDialog(getActivity(), menuItems, menuIcons,
                        new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                switch (i) {
                                    case 0:
                                        if (mAccount != null) {
                                            CurriculumAdapter curriculumAdapter = new CurriculumAdapter(getActivity());
                                            if (curriculumAdapter.checkFavourite(mWhatsOn.getWhatsOnId(), mAccount.getAccountId())) {
                                                Toast.makeText(getActivity(), getString(R.string.wo_already_added), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Map<String, String> favourite = new HashMap<>();
                                                favourite.put("lesson_tutor_id", mWhatsOn.getWhatsOnId());
                                                favourite.put("fav_lesson_user", mAccount.getAccountId());
                                                favourite.put("type", String.valueOf(FavouriteDrawerFragment.WHATS_ON_ID));
                                                curriculumAdapter.insertFavourite(favourite);
                                                Toast.makeText(getActivity(), getString(R.string.wo_successfully_added), Toast.LENGTH_SHORT).show();
                                                ServerRequestManager serverRequestManager = new ServerRequestManager(getActivity().getApplicationContext());
                                                serverRequestManager.addToFavorite(FavouriteDrawerFragment.WHATS_ON_ID, mWhatsOn.getWhatsOnId(), FavouriteDrawerFragment.FAVOURITE_ID);
                                            }
                                        }
                                        break;

                                    case 1:
                                        String content = mWhatsOn.getArticle().substring(0,
                                                (mWhatsOn.getArticle().length() > 400) ? 400 : mWhatsOn.getArticle().length());

                                        Intent shareIntent = new Intent(getActivity(), ShareActivity.class);
                                        shareIntent.putExtra(ShareActivity.EXTRA_SHARE_ITEM, "What's on");
                                        shareIntent.putExtra(ShareActivity.EXTRA_TITLE, mWhatsOn.getTitle());
                                        shareIntent.putExtra(ShareActivity.EXTRA_CONTENT, content);
                                        shareIntent.putExtra(ShareActivity.EXTRA_IMAGE_URL, mWhatsOn.getCoverPhoto());

                                        startActivity(shareIntent);
                                        break;
                                }
                            }
                        });
                customMenu.setGravity(Gravity.TOP);
                customMenu.show();
                return true;
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStack();
        return false;
    }

    private class WhatsOnTabAdapter extends FragmentStatePagerAdapter {

        WhatsOnTabAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return WhatsOnArticleFragment.newInstance(mWhatsOn.getArticle());
                case 1:
                    return WhatsOnGrammarVocabFragment.newInstance(mWhatsOn.getWhatsOnId(), 1);
                case 2:
                    return WhatsOnGrammarVocabFragment.newInstance(mWhatsOn.getWhatsOnId(), 2);
                default:
                    return WhatsOnArticleFragment.newInstance(mWhatsOn.getArticle());
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.wo_article);
                case 1:
                    return getString(R.string.wo_vocab);
                case 2:
                    return getString(R.string.wo_grammar);
            }
            return super.getPageTitle(position);
        }
    }
}
