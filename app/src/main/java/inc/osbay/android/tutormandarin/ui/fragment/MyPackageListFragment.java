package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flurry.android.FlurryAgent;

import java.util.List;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.sdk.model.StudentPackage;
import inc.osbay.android.tutormandarin.ui.activity.PackageTutorialActivity;
import inc.osbay.android.tutormandarin.ui.activity.SignUpActivity;
import inc.osbay.android.tutormandarin.ui.view.PagerContainer;
import inc.osbay.android.tutormandarin.util.CommonUtil;

public class MyPackageListFragment extends BackHandledFragment implements View.OnClickListener {

    private List<StudentPackage> mPackages;
    private ViewPager mPackageViewPager;
    private PackagePagerAdapter mPagerAdapter;
    private RelativeLayout mMyPackageRelativeLayout;
    private RelativeLayout mRlMyPkListPopUp;
    private AccountAdapter mAccountAdapter;

    public MyPackageListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPagerAdapter = new PackagePagerAdapter();

        mAccountAdapter = new AccountAdapter(getActivity());
        mPackages = mAccountAdapter.getStudentPackageList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_package_list, container, false);

        Toolbar toolBar;
        toolBar = rootView.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#E45F56"));
        setSupportActionBar(toolBar);

        LinearLayout llMyPkWhatIsPackage = rootView.findViewById(R.id.ll_my_pk_what_is_package);
        LinearLayout llMyPkBookingRules = rootView.findViewById(R.id.ll_my_pk_booking_rule);
        LinearLayout llBuyNewPackage = rootView.findViewById(R.id.ll_buy_new_package);

        mRlMyPkListPopUp = rootView.findViewById(R.id.rl_my_pk_list_pop_up);

        PagerContainer pagerContainer = rootView.findViewById(R.id.pager_container);
        mMyPackageRelativeLayout = rootView.findViewById(R.id.rl_my_package);

        mPackageViewPager = pagerContainer.getViewPager();

        llBuyNewPackage.setOnClickListener(this);
        llMyPkWhatIsPackage.setOnClickListener(this);
        llMyPkBookingRules.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        setTitle(getString(R.string.pk_title));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String token = prefs.getString("access_token", null);
        String accountId = prefs.getString("account_id", null);
        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {

            Account account = mAccountAdapter.getAccountById(accountId);
            if (account != null /*&& account.getStatus() == 4*/) {

                mMyPackageRelativeLayout.setVisibility(View.VISIBLE);

                mPackageViewPager.setAdapter(mPagerAdapter);

                mPackageViewPager.setOffscreenPageLimit(mPagerAdapter.getCount());
                mPackageViewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.vp_page_margin));
                mPackageViewPager.setClipChildren(false);

                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setCancelable(false);
                progressDialog.setMessage(getString(R.string.pk_my_lst_loading));

                if (mPackages.size() == 0) {
                    progressDialog.show();
                }

                ServerRequestManager requestManager = new ServerRequestManager(getActivity().getApplicationContext());
                requestManager.downloadStudentPackageList(new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        progressDialog.dismiss();
                        mPackages.clear();

                        if (getActivity() != null) {
                            List<?> objects = (List<?>) result;
                            for (Object obj : objects) {
                                if (obj instanceof StudentPackage) {
                                    mPackages.add((StudentPackage) obj);
                                }
                            }

                            if (mPackages.size() > 0) {
                                mPagerAdapter.notifyDataSetChanged();
                                mPackageViewPager.setOffscreenPageLimit(mPagerAdapter.getCount());
                            } else {
                                mMyPackageRelativeLayout.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), err.getErrorCode() + " - " + err.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                return;
            }
        }

        mMyPackageRelativeLayout.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_package_help, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.opt_package_help:
                if (mRlMyPkListPopUp.getVisibility() == View.VISIBLE)
                    mRlMyPkListPopUp.setVisibility(View.GONE);
                else
                    mRlMyPkListPopUp.setVisibility(View.VISIBLE);
                break;
        }

        return true;
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStackImmediate();
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_buy_new_package:
                FlurryAgent.logEvent("Click purchase package");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String token = prefs.getString("access_token", null);
                String accountId = prefs.getString("account_id", null);

                Fragment newFragment = null;

                if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {

                    Account account = mAccountAdapter.getAccountById(accountId);
                    if (account != null /*&& account.getStatus() == 4*/) {
                        newFragment = new PackageStoreFragment();
                    } /*else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.pk_user_unavailable_title))
                                .setMessage(getString(R.string.pk_user_unavailable_msg))
                                .setPositiveButton(getString(R.string.pk_user_unavailable_ok), null)
                                .show();
                    }*/
                } else {
                    FlurryAgent.logEvent("Sign Up(buy package)");
                    Intent signUpIntent = new Intent(getActivity(), SignUpActivity.class);
                    signUpIntent.putExtra(SignUpActivity.MAIN_SIGN_UP, 1);
                    startActivity(signUpIntent);
                }

                if (newFragment != null) {
                    FragmentManager frgMgr = getFragmentManager();
                    Fragment frg = frgMgr.findFragmentById(R.id.container);

                    if (frg == null) {
                        frgMgr.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                .addToBackStack(null)
                                .add(R.id.container, newFragment).commit();
                    } else {
                        frgMgr.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                .addToBackStack(null)
                                .replace(R.id.container, newFragment).commit();
                    }
                }
                break;
            case R.id.ll_my_pk_what_is_package:
                FlurryAgent.logEvent("Click what is a package");

                mRlMyPkListPopUp.setVisibility(View.GONE);

                FragmentManager fm = getFragmentManager();
                Fragment whatIsPackageFragment = new WhatIsPackageFragment();
                Fragment frg = fm.findFragmentById(R.id.container);

                if (frg == null) {
                    fm.beginTransaction()
                            .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                    R.animator.fragment_out_new, R.animator.fragment_out_old)
                            .addToBackStack(null)
                            .add(R.id.container, whatIsPackageFragment).commit();
                } else {
                    fm.beginTransaction()
                            .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                    R.animator.fragment_out_new, R.animator.fragment_out_old)
                            .addToBackStack(null)
                            .replace(R.id.container, whatIsPackageFragment).commit();
                }
                break;

            case R.id.ll_my_pk_booking_rule:
                FlurryAgent.logEvent("Click package booking rules");

                mRlMyPkListPopUp.setVisibility(View.GONE);

                Intent intent = new Intent(getActivity(), PackageTutorialActivity.class);
                startActivity(intent);
                break;
        }
    }

    private class PackagePagerAdapter extends PagerAdapter {

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup collection, final int position) {
            final StudentPackage studentPackage = mPackages.get(position);

            ViewGroup layout = (ViewGroup) LayoutInflater.from(getActivity())
                    .inflate(R.layout.item_vp_package, collection, false);

            TextView tvPackageName = layout.findViewById(R.id.tv_package_name);
            tvPackageName.setText(studentPackage.getPackageName());

            TextView tvFinishedLesson = layout.findViewById(R.id.tv_finished_lesson);
            tvFinishedLesson.setText(String.valueOf(studentPackage.getFinishedLessonCount()));

            TextView tvTotalLesson = layout.findViewById(R.id.tv_total_lesson);
            tvTotalLesson.setText(String.valueOf(studentPackage.getLessonCount()
                    + studentPackage.getSupplementCount()));

            SimpleDraweeView sdvPhoto = layout.findViewById(R.id.sdv_package_photo);
            if (!TextUtils.isEmpty(studentPackage.getCoverPhoto())) {
                sdvPhoto.setImageURI(Uri.parse(studentPackage.getCoverPhoto()));
            }

            ProgressBar pbFinishedLesson = layout.findViewById(R.id.pb_achievement_lesson);
            pbFinishedLesson.setMax(studentPackage.getLessonCount() + studentPackage.getSupplementCount());
            pbFinishedLesson.setProgress(studentPackage.getFinishedLessonCount());

            TextView tvExpiredDate = layout.findViewById(R.id.tv_expired_date);
            if (studentPackage.getStatus() == 1) {
                tvExpiredDate.setText(getString(R.string.pk_lst_booking_expired_date,
                        CommonUtil.getCustomDateResult(studentPackage.getPackageExpireDate(), "yyyy-MM-dd HH:mm:ss", "MM/dd/yyyy")));
            } else if (studentPackage.getStatus() == 2) {
                tvExpiredDate.setText(getString(R.string.pk_lst_booking_expired_date,
                        CommonUtil.getCustomDateResult(studentPackage.getActiveExpireDate(), "yyyy-MM-dd HH:mm:ss", "MM/dd/yyyy")));
            }

            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment packageDetailFragment = new MyPackageFragment();

                    Bundle bundle = new Bundle();
                    bundle.putSerializable(MyPackageFragment.EXTRA_PACKAGE, studentPackage);
                    packageDetailFragment.setArguments(bundle);

                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, packageDetailFragment)
                            .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                    R.animator.fragment_out_new, R.animator.fragment_out_old)
                            .addToBackStack(null)
                            .commit();
                }
            });

            collection.addView(layout);

            return layout;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup collection, int position, @NonNull Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return mPackages.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(R.string.pk_package_title);
        }

    }
}
