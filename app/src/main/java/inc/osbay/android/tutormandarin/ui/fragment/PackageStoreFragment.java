package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.sdk.model.StorePackage;
import inc.osbay.android.tutormandarin.ui.activity.PackageTutorialActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class PackageStoreFragment extends BackHandledFragment implements View.OnClickListener {
    private List<StorePackage> mRecommendedPackages;
    private List<StorePackage> mNormalPackages;
    private List<StorePackage> mKidsPackages;

    private RecommendedRVAdapter mRecommendedRVAdapter;
    private NormalRVAdapter mNormalRVAdapter;
    private KidRVAdapter mKidRVAdapter;

    private TextView mMyCreditTextView;
    private RelativeLayout mRlPkStorePopUp;

    private ServerRequestManager mServerRequestManager;
    private Timer mTimer;

    private LinearLayout packageStoreLL;
    private TextView basicTitleTextView;
    private TextView kidTitleTextView;

    public PackageStoreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRecommendedPackages = new ArrayList<>();
        mNormalPackages = new ArrayList<>();
        mKidsPackages = new ArrayList<>();

        mServerRequestManager = new ServerRequestManager(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();

        setTitle(getString(R.string.pk_store_title));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_package_store, container, false);

        Toolbar toolBar;
        toolBar = v.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#E45F56"));
        setSupportActionBar(toolBar);

        RecyclerView rvRecommendedPackage = v.findViewById(R.id.rv_recommended_package);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rvRecommendedPackage.setLayoutManager(llm);
        mRecommendedRVAdapter = new RecommendedRVAdapter();
        rvRecommendedPackage.setAdapter(mRecommendedRVAdapter);

        mMyCreditTextView = v.findViewById(R.id.tv_student_credit);
        basicTitleTextView = v.findViewById(R.id.basic_title);
        kidTitleTextView = v.findViewById(R.id.kid_title);

        mRlPkStorePopUp = v.findViewById(R.id.rl_pk_store_pop_up);
        LinearLayout llPkStoreWhatIsPackage = v.findViewById(R.id.ll_pk_store_what_is_package);
        LinearLayout llPkStoreBookingRule = v.findViewById(R.id.ll_pk_store_booking_rule);
        llPkStoreWhatIsPackage.setOnClickListener(this);
        llPkStoreBookingRule.setOnClickListener(this);

        FloatingActionButton fabAddCredit = v.findViewById(R.id.fab_add_credit);
        fabAddCredit.setOnClickListener(this);

        RecyclerView rvBasicPackage = v.findViewById(R.id.rv_basic_package);
        LinearLayoutManager llm1 = new
                LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rvBasicPackage.setLayoutManager(llm1);
        mNormalRVAdapter = new NormalRVAdapter();
        rvBasicPackage.setAdapter(mNormalRVAdapter);

        RecyclerView rvKidsPackage = v.findViewById(R.id.rv_kids_package);
        LinearLayoutManager llm2 = new
                LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rvKidsPackage.setLayoutManager(llm2);
        mKidRVAdapter = new KidRVAdapter();
        rvKidsPackage.setAdapter(mKidRVAdapter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String accountId = prefs.getString("account_id", "1");
        Account account = new AccountAdapter(getActivity()).getAccountById(accountId);

        if (account != null) {
            mMyCreditTextView.setText(String.format(Locale.getDefault(), "%.1f", account.getCredit()));
        }

        packageStoreLL = v.findViewById(R.id.package_store_ll);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.pk_store_dialog_loading));
        progressDialog.show();

        mServerRequestManager.downloadStorePackageList(
                new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        progressDialog.dismiss();

                        Map<String, List<StorePackage>> map = (Map<String, List<StorePackage>>) result;

                        mRecommendedPackages = map.get("recommended");
                        mNormalPackages = map.get("normal");
                        mKidsPackages = map.get("kids");

                        mRecommendedRVAdapter.notifyDataSetChanged();
                        mNormalRVAdapter.notifyDataSetChanged();
                        mKidRVAdapter.notifyDataSetChanged();

                        basicTitleTextView.setText(getContext().getString(R.string.basic) + " (" + mNormalPackages.size() + ")");
                        kidTitleTextView.setText(getContext().getString(R.string.kids) + " (" + mKidsPackages.size() + ")");
                        packageStoreLL.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(ServerError err) {
                        progressDialog.dismiss();
                        packageStoreLL.setVisibility(View.GONE);
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), getString(R.string.pk_download_packages_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        mTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                mServerRequestManager.refreshStudentBalance(new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        if (getActivity() != null) {
                            mMyCreditTextView.setText(String.format(Locale.getDefault(), "%.1f",
                                    Double.parseDouble(String.valueOf(result))));
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        // ignore
                    }
                });
            }
        };

        mTimer.schedule(task, 2000, 2000);
    }

    @Override
    public void onPause() {
        super.onPause();
        mTimer.cancel();
        mTimer.purge();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_add_credit:
                FlurryAgent.logEvent("Click add credits (Package Store)");

                FragmentManager frgMgr = getFragmentManager();
                Fragment storeFragment = new CreditStoreFragment();
                Fragment frg = frgMgr.findFragmentById(R.id.container);
                if (frg == null) {
                    frgMgr.beginTransaction()
                            .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                    R.animator.fragment_out_new, R.animator.fragment_out_old)
                            .addToBackStack(null)
                            .add(R.id.container, storeFragment).commit();
                } else {
                    frgMgr.beginTransaction()
                            .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                    R.animator.fragment_out_new, R.animator.fragment_out_old)
                            .addToBackStack(null)
                            .replace(R.id.container, storeFragment).commit();
                }
                break;
            case R.id.ll_pk_store_what_is_package:
                FlurryAgent.logEvent("Click what is a package");

                mRlPkStorePopUp.setVisibility(View.GONE);

                FragmentManager fm = getFragmentManager();
                Fragment whatIsPackageFragment = new WhatIsPackageFragment();
                Fragment fragment = fm.findFragmentById(R.id.container);

                if (fragment == null) {
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

            case R.id.ll_pk_store_booking_rule:
                FlurryAgent.logEvent("Click package booking rules");

                mRlPkStorePopUp.setVisibility(View.GONE);

                Intent intent = new Intent(getActivity(), PackageTutorialActivity.class);
                startActivity(intent);
                break;
        }
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
                if (mRlPkStorePopUp.getVisibility() == View.VISIBLE)
                    mRlPkStorePopUp.setVisibility(View.GONE);
                else
                    mRlPkStorePopUp.setVisibility(View.VISIBLE);
                break;
        }

        return true;
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStack();
        return false;
    }

    private class RecommendedRVAdapter extends RecyclerView.Adapter<RecommendedRVAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_store_package,
                    parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final StorePackage storePackage = mRecommendedPackages.get(position);

            if (!TextUtils.isEmpty(storePackage.getThumbPhoto())) {
                holder.sdvCoverPhoto.setImageURI(Uri.parse(storePackage.getThumbPhoto()));
            }

            holder.tvNewPackageTitle.setText(storePackage.getTitle());

            holder.tvNewPackageCredit.setText(getString(R.string.pk_new_package_credit,
                    String.format(Locale.getDefault(), "%.0f", storePackage.getPackageCredit())));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FlurryAgent.logEvent("Select " + storePackage.getTitle() + "(Store)");

                    Bundle bundle = new Bundle();
                    bundle.putSerializable(StorePackageFragment.EXTRA_PACKAGE, storePackage);

                    FragmentManager frgMgr = getFragmentManager();
                    Fragment frg = frgMgr.findFragmentById(R.id.container);
                    Fragment bookingNewPackageDetailFragment = new StorePackageFragment();
                    bookingNewPackageDetailFragment.setArguments(bundle);

                    if (frg == null) {
                        frgMgr.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                .addToBackStack(null)
                                .add(R.id.container, bookingNewPackageDetailFragment).commit();
                    } else {
                        frgMgr.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                .addToBackStack(null)
                                .replace(R.id.container, bookingNewPackageDetailFragment).commit();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mRecommendedPackages.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private SimpleDraweeView sdvCoverPhoto;
            private TextView tvNewPackageTitle;
            private TextView tvNewPackageCredit;

            public ViewHolder(View itemView) {
                super(itemView);
                sdvCoverPhoto = itemView.findViewById(R.id.sdv_store_package);
                tvNewPackageTitle = itemView.findViewById(R.id.tv_new_package_title);
                tvNewPackageCredit = itemView.findViewById(R.id.tv_new_package_credit);
            }
        }
    }

    private class NormalRVAdapter extends RecyclerView.Adapter<NormalRVAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_store_package,
                    parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final StorePackage storePackage = mNormalPackages.get(position);

            if (!TextUtils.isEmpty(storePackage.getThumbPhoto())) {
                holder.sdvCoverPhoto.setImageURI(Uri.parse(storePackage.getThumbPhoto()));
            }

            holder.tvNewPackageTitle.setText(storePackage.getTitle());

            holder.tvNewPackageCredit.setText(getString(R.string.pk_new_package_credit,
                    String.format(Locale.getDefault(), "%.0f", storePackage.getPackageCredit())));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(StorePackageFragment.EXTRA_PACKAGE, storePackage);

                    FragmentManager frgMgr = getFragmentManager();
                    Fragment frg = frgMgr.findFragmentById(R.id.container);
                    Fragment storePackageFragment = new StorePackageFragment();
                    storePackageFragment.setArguments(bundle);

                    if (frg == null) {
                        frgMgr.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                .addToBackStack(null)
                                .add(R.id.container, storePackageFragment).commit();
                    } else {
                        frgMgr.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                .addToBackStack(null)
                                .replace(R.id.container, storePackageFragment).commit();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mNormalPackages.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private SimpleDraweeView sdvCoverPhoto;
            private TextView tvNewPackageTitle;
            private TextView tvNewPackageCredit;

            public ViewHolder(View itemView) {
                super(itemView);
                sdvCoverPhoto = itemView.findViewById(R.id.sdv_store_package);
                tvNewPackageTitle = itemView.findViewById(R.id.tv_new_package_title);
                tvNewPackageCredit = itemView.findViewById(R.id.tv_new_package_credit);
            }
        }
    }

    private class KidRVAdapter extends RecyclerView.Adapter<KidRVAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_store_package,
                    parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final StorePackage storePackage = mKidsPackages.get(position);

            if (!TextUtils.isEmpty(storePackage.getThumbPhoto())) {
                holder.sdvCoverPhoto.setImageURI(Uri.parse(storePackage.getThumbPhoto()));
            }

            holder.tvNewPackageTitle.setText(storePackage.getTitle());

            holder.tvNewPackageCredit.setText(getString(R.string.pk_new_package_credit,
                    String.format(Locale.getDefault(), "%.0f", storePackage.getPackageCredit())));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(StorePackageFragment.EXTRA_PACKAGE, storePackage);

                    FragmentManager frgMgr = getFragmentManager();
                    Fragment frg = frgMgr.findFragmentById(R.id.container);
                    Fragment storePackageFragment = new StorePackageFragment();
                    storePackageFragment.setArguments(bundle);

                    if (frg == null) {
                        frgMgr.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                .addToBackStack(null)
                                .add(R.id.container, storePackageFragment).commit();
                    } else {
                        frgMgr.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                .addToBackStack(null)
                                .replace(R.id.container, storePackageFragment).commit();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mKidsPackages.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private SimpleDraweeView sdvCoverPhoto;
            private TextView tvNewPackageTitle;
            private TextView tvNewPackageCredit;

            public ViewHolder(View itemView) {
                super(itemView);
                sdvCoverPhoto = itemView.findViewById(R.id.sdv_store_package);
                tvNewPackageTitle = itemView.findViewById(R.id.tv_new_package_title);
                tvNewPackageCredit = itemView.findViewById(R.id.tv_new_package_credit);
            }
        }
    }
}
