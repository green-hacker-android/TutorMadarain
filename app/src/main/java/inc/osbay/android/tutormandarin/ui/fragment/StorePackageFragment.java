package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Lesson;
import inc.osbay.android.tutormandarin.sdk.model.StorePackage;
import inc.osbay.android.tutormandarin.sdk.model.StudentPackage;

/**
 * A simple {@link Fragment} subclass.
 */
public class StorePackageFragment extends BackHandledFragment implements View.OnClickListener {

    public static final String EXTRA_PACKAGE = "StorePackageFragment.EXTRA_PACKAGE";

    private StorePackage mStorePackage;

    private List<Integer> mSectionCounts;

    private CurriculumAdapter curriculumAdapter;

    private SectionListAdapter mSectionListAdapter;

    private TextView tvBuyNewPackage;

    private boolean mIsClicked = false;

    public StorePackageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mSectionCounts = new ArrayList<>();

        if (bundle != null) {
            mStorePackage = (StorePackage) bundle.getSerializable(EXTRA_PACKAGE);
            curriculumAdapter = new CurriculumAdapter(getActivity().getApplicationContext());
            mSectionCounts = curriculumAdapter.getBookingSectionNumber(mStorePackage.getCourseId());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_store_package, container, false);

        Toolbar toolBar;
        toolBar = rootView.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#E45F56"));
        setSupportActionBar(toolBar);

        TextView tvNewPackageDetailTitle = rootView.findViewById(R.id.tv_new_package_detail_title);
        tvNewPackageDetailTitle.setText(mStorePackage.getPackageName());

        TextView tvNewPackageLesson = rootView.findViewById(R.id.tv_new_package_lesson);
        tvNewPackageLesson.setText(getString(R.string.sp_lessons, String.valueOf(mStorePackage.getLessonCount())));

        TextView tvNewPackageSupplements = rootView.findViewById(R.id.tv_new_package_supplements);
        tvNewPackageSupplements.setText(getString(R.string.sp_supplements, String.valueOf(mStorePackage.getSupplementCount())));

        tvBuyNewPackage = rootView.findViewById(R.id.txt_buy_new_package);
        tvBuyNewPackage.setText(String.format(Locale.getDefault(), "%.0f " + getString(R.string.sp_credits), mStorePackage.getPackageCredit()));
        tvBuyNewPackage.setOnClickListener(this);

        WebView wvRichText = rootView.findViewById(R.id.wv_new_package_summary);
        wvRichText.loadData(mStorePackage.getSummary(), "text/html; charset=utf-8", "UTF-8");
        wvRichText.setBackgroundColor(Color.TRANSPARENT);

        RecyclerView rvSectionList = rootView.findViewById(R.id.rv_section_list);
        mSectionListAdapter = new SectionListAdapter();
        rvSectionList.setAdapter(mSectionListAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        rvSectionList.setLayoutManager(llm);

        SimpleDraweeView sdvCoverPhoto = rootView.findViewById(R.id.sdv_cover_photo);
        if (!TextUtils.isEmpty(mStorePackage.getThumbPhoto())) {
            sdvCoverPhoto.setImageURI(Uri.parse(mStorePackage.getThumbPhoto()));
        }
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        setTitle(mStorePackage.getPackageName());
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        ServerRequestManager serverRequestManager = new ServerRequestManager(getActivity());
        serverRequestManager.downloadLessonList(mStorePackage.getCourseId(), new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                mSectionCounts = curriculumAdapter.getBookingSectionNumber(mStorePackage.getCourseId());
                mSectionListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(ServerError err) {
                if (getActivity() != null) {
                    if (err.getErrorCode() == 10067) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.sp_course_coming_soon))
                                .setMessage(getString(R.string.sp_course_coming_soon_msg))
                                .setPositiveButton(getString(R.string.sp_course_coming_soon_ok), null)
                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        dialogInterface.dismiss();
                                        onBackPressed();
                                    }
                                })
                                .create().show();
                    } else {
                        Toast.makeText(getActivity(), err.getErrorCode() + " - " + err.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStack();
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_buy_new_package:
                if (mIsClicked) {
                    final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage(getString(R.string.sp_buy_package_loading));
                    progressDialog.show();

                    final Dialog dialog = new Dialog(getActivity());
                    dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.dialog_purchase_package);
                    final TextView tvOrder = dialog.findViewById(R.id.tv_my_order);
                    final TextView tvStartBookingNow = dialog.findViewById(R.id.tv_start_booking_now);
                    final TextView tvBackToMainMenu = dialog.findViewById(R.id.tv_back_to_main_menu);

                    ServerRequestManager requestManager = new ServerRequestManager(getActivity().getApplicationContext());
                    requestManager.buyPackages(mStorePackage.getPackageId(),
                            new ServerRequestManager.OnRequestFinishedListener() {
                                @Override
                                public void onSuccess(Object result) {
                                    final String studentPackageId = String.valueOf(result);

                                    tvOrder.setText(getString(R.string.sp_my_order, mStorePackage.getPackageName()));
                                    ServerRequestManager requestManager = new ServerRequestManager(getActivity().getApplicationContext());
                                    requestManager.downloadStudentPackageList(new ServerRequestManager.OnRequestFinishedListener() {
                                        @Override
                                        public void onSuccess(Object result) {
                                            FlurryAgent.logEvent("Bought package");

                                            if (getActivity() != null) {
                                                progressDialog.dismiss();
                                                dialog.show();

                                                tvStartBookingNow.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialog.dismiss();
                                                        FlurryAgent.logEvent("Start booking (Bought Package)");

                                                        Fragment bookingPackageFragment = new BookingPackageFragment();

                                                        AccountAdapter accountAdapter = new AccountAdapter(getActivity());
                                                        StudentPackage studentPackage = accountAdapter.getStudentPackage(studentPackageId);

                                                        Bundle bundle = new Bundle();
                                                        bundle.putSerializable(BookingPackageFragment.EXTRA_PACKAGE, studentPackage);
                                                        bookingPackageFragment.setArguments(bundle);

                                                        changeFragment(bookingPackageFragment);
                                                    }
                                                });

                                                tvBackToMainMenu.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialog.dismiss();
                                                        FlurryAgent.logEvent("Back to Main (Bought Package)");

                                                        getFragmentManager().beginTransaction()
                                                                .remove(StorePackageFragment.this)
                                                                .commit();

                                                        getFragmentManager().popBackStack(getFragmentManager().getBackStackEntryAt(0).getId(),
                                                                FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onError(ServerError err) {
                                            if (getActivity() != null) {
                                                Toast.makeText(getActivity(), err.getErrorCode() +
                                                                " - " + err.getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }

                                private void changeFragment(Fragment newFragment) {
                                    FragmentManager fm = getFragmentManager();
                                    Fragment fragment = fm.findFragmentById(R.id.container);
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

                                @Override
                                public void onError(ServerError err) {
                                    progressDialog.dismiss();

                                    if (getActivity() != null) {
                                        if (err.getErrorCode() == ServerError.Code.INSUFFICIENT_BALANCE) {
                                            new AlertDialog.Builder(getActivity())
                                                    .setTitle(getString(R.string.sp_insufficient_title))
                                                    .setMessage(getString(R.string.sp_insufficient_msg))
                                                    .setNegativeButton(getString(R.string.sp_insufficient_cancel), null)
                                                    .setPositiveButton(getString(R.string.sp_insufficient_ok), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            changeFragment(new CreditStoreFragment());
                                                        }
                                                    })
                                                    .show();
                                        } else if (err.getErrorCode() == ServerError.Code.CHINESE_LEVEL_LOWER) {
                                            new AlertDialog.Builder(getActivity())
                                                    .setTitle(getString(R.string.sp_level_lower_title))
                                                    .setMessage(getString(R.string.sp_level_lower_msg))
                                                    .setPositiveButton(getString(R.string.sp_level_lower_ok), null)
                                                    .create()
                                                    .show();
                                        } else {
                                            new AlertDialog.Builder(getActivity())
                                                    .setTitle(getString(R.string.sp_server_error))
                                                    .setMessage(err.getMessage())
                                                    .setPositiveButton(getString(R.string.sp_server_error_ok), null)
                                                    .create()
                                                    .show();
                                        }
                                    }
                                }
                            });
                } else {
                    mIsClicked = true;
                    tvBuyNewPackage.setBackgroundResource(R.drawable.btn_bg_rounded_orange);
                    tvBuyNewPackage.setTextColor(Color.WHITE);
                    tvBuyNewPackage.setText(getString(R.string.sp_package_buy));
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    private class SectionListAdapter extends RecyclerView.Adapter<SectionListAdapter.SectionHolder> {

        @Override
        public SectionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_section_list, parent, false);
            return new SectionHolder(v);
        }

        @Override
        public void onBindViewHolder(SectionHolder holder, int position) {
            holder.tvSection.setText(getString(R.string.sp_section_title, String.valueOf(mSectionCounts.get(position))));
            LinearLayoutManager llBookingLessons = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            holder.rvBookingLessons.setLayoutManager(llBookingLessons);
            LessonListAdapter mLessonListAdapter = new LessonListAdapter(mSectionCounts.get(position));
            holder.rvBookingLessons.setAdapter(mLessonListAdapter);
        }

        @Override
        public int getItemCount() {
            return mSectionCounts.size();
        }

        class SectionHolder extends RecyclerView.ViewHolder {
            RecyclerView rvBookingLessons;
            TextView tvSection;

            public SectionHolder(View itemView) {
                super(itemView);
                tvSection = itemView.findViewById(R.id.tv_section_title);

                rvBookingLessons = itemView.findViewById(R.id.rv_booking_lessons);
            }
        }
    }

    private class LessonListAdapter extends RecyclerView.Adapter<LessonListAdapter.LessonHolder> {
        List<Lesson> mSectionLessons;

        public LessonListAdapter(int sectionNumber) {
            CurriculumAdapter curriculumAdapter = new CurriculumAdapter(getActivity());
            mSectionLessons = curriculumAdapter.getLessonBySectionNumber(mStorePackage.getCourseId(), sectionNumber);
        }

        @Override
        public LessonHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_section_lesson_list,
                    parent, false);
            return new LessonHolder(v);
        }

        @Override
        public void onBindViewHolder(LessonHolder holder, int position) {
            if (mSectionLessons.get(position).getIsSupplement() == 1) {
                holder.tvLessonName.setText(getString(R.string.sp_lesson_name, getString(R.string.sp_supplement), ""));
                holder.imgvIsSupplement.setImageResource(R.drawable.ic_supplement);
            } else {
                holder.tvLessonName.setText(getString(R.string.sp_lesson_name, getString(R.string.sp_lesson), String.valueOf(mSectionLessons.get(position).getLessonNumber())));
                holder.imgvIsSupplement.setImageResource(R.drawable.ic_lesson_black);
            }
            holder.tvLessonMins.setText(getString(R.string.sp_lesson_mins, String.valueOf(mSectionLessons.get(position).getDuration())));
        }

        @Override
        public int getItemCount() {
            return mSectionLessons.size();
        }

        class LessonHolder extends RecyclerView.ViewHolder {
            TextView tvLessonName;
            TextView tvLessonMins;
            ImageView imgvIsSupplement;

            public LessonHolder(View itemView) {
                super(itemView);
                tvLessonName = itemView.findViewById(R.id.tv_lesson_name);
                tvLessonMins = itemView.findViewById(R.id.tv_lesson_mins);
                imgvIsSupplement = itemView.findViewById(R.id.imgv_is_supplement);
            }
        }
    }
}
