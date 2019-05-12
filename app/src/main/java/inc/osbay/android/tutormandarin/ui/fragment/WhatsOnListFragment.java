package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.model.WhatsOn;
import inc.osbay.android.tutormandarin.ui.view.RecyclerItemClickListener;
import inc.osbay.android.tutormandarin.util.CommonUtil;

public class WhatsOnListFragment extends BackHandledFragment implements View.OnClickListener {
    private List<WhatsOn> mWhatsOnList;
    private ServerRequestManager mServerRequestManager;
    private WhatsOnListAdapter mWhatsOnListAdapter;
    private WhatsOnSelectTopicAdapter mWhatsOnTopicAdapter;
    private List<String> mSelectTopics;
    private RecyclerView rvWhatsOnSelectTopic;
    private CurriculumAdapter mCurriculumAdapter;

    private TextView mSelectTopicTextView;

    private RelativeLayout mSearchBarRelativeLayout;
    private EditText mSearchEditText;

    private DrawerLayout mDrawerLayout;

    private SharedPreferences sharedPreferences;

    private String mLocaleSelectedTxt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mCurriculumAdapter = new CurriculumAdapter(getActivity());

        mWhatsOnList = mCurriculumAdapter.getAllWhatsOn();
        mSelectTopics = mCurriculumAdapter.getWhatsOnTopics();

        mServerRequestManager = new ServerRequestManager(getActivity().getApplicationContext());
        mWhatsOnListAdapter = new WhatsOnListAdapter();
        mWhatsOnTopicAdapter = new WhatsOnSelectTopicAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_whats_on_list, container, false);

        Toolbar toolBar;
        toolBar = rootView.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#D3825A"));
        setSupportActionBar(toolBar);
//        setStatusBarColor("#B46640");

        mLocaleSelectedTxt = "SELECT TOPICS";

        mSearchBarRelativeLayout = rootView.findViewById(R.id.rl_search_bar);
        mSelectTopicTextView = rootView.findViewById(R.id.tv_select_topic);

        RelativeLayout rlSelectTopic = rootView.findViewById(R.id.rl_select_whats_on_topic);
        rlSelectTopic.setOnClickListener(this);

        ImageView searchCancelImageView = rootView.findViewById(R.id.imv_search_cancel);
        searchCancelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchBarRelativeLayout.setVisibility(View.INVISIBLE);
                mSearchEditText.setEnabled(false);
                showActionBar();

                if (getActivity() != null)
                    CommonUtil.hideKeyBoard(getActivity(), view);

                refreshWhatsOnList();
            }
        });

        mSearchEditText = rootView.findViewById(R.id.edt_search_text);
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String title = mSearchEditText.getText().toString();
                if (!TextUtils.isEmpty(title)) {
                    mWhatsOnList = mCurriculumAdapter.searchWhatsOnByTitle(title);
                    mSelectTopicTextView.setText(getString(R.string.wo_select_topic));
                } else {
                    refreshWhatsOnList();
                }
                mWhatsOnTopicAdapter.notifyDataSetChanged();
                mWhatsOnListAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        RecyclerView rvWhatsOnPicList = rootView.findViewById(R.id.rv_whats_on_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvWhatsOnPicList.setLayoutManager(layoutManager);
        rvWhatsOnPicList.setAdapter(mWhatsOnListAdapter);

        rvWhatsOnSelectTopic = rootView.findViewById(R.id.rv_topic_list);
        LinearLayoutManager topicLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvWhatsOnSelectTopic.setLayoutManager(topicLayoutManager);
        rvWhatsOnSelectTopic.setAdapter(mWhatsOnTopicAdapter);
        rvWhatsOnSelectTopic.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position == -1)
                    return;

                String selectedTopic = mSelectTopics.get(position);
                mWhatsOnList.clear();
                if ("All".equals(selectedTopic)) {
                    mLocaleSelectedTxt = "SELECT TOPICS";
                    mSelectTopicTextView.setText(getString(R.string.wo_select_topic));
                } else {
                    String whatsOnTopicsLocale;
                    switch (selectedTopic) {
                        case "Phrases":
                            mLocaleSelectedTxt = "Phrases";
                            whatsOnTopicsLocale = getString(R.string.wo_phrases);
                            break;
                        case "Language Learning":
                            mLocaleSelectedTxt = "Language Learning";
                            whatsOnTopicsLocale = getString(R.string.wo_language_learning);
                            break;
                        case "Conversations":
                            mLocaleSelectedTxt = "Conversations";
                            whatsOnTopicsLocale = getString(R.string.wo_conversation);
                            break;
                        case "Survival Skills":
                            mLocaleSelectedTxt = "Survival Skills";
                            whatsOnTopicsLocale = getString(R.string.wo_survival_skills);
                            break;
                        case "Understanding China":
                            mLocaleSelectedTxt = "Understanding China";
                            whatsOnTopicsLocale = getString(R.string.wo_understanding_china);
                            break;
                        case "Funny":
                            mLocaleSelectedTxt = "Funny";
                            whatsOnTopicsLocale = getString(R.string.wo_funny);
                            break;
                        case "Readable Chinese":
                            mLocaleSelectedTxt = "Readable Chinese";
                            whatsOnTopicsLocale = getString(R.string.wo_readable_chinese);
                            break;
                        default:
                            mLocaleSelectedTxt = "SELECT TOPICS";
                            whatsOnTopicsLocale = getString(R.string.wo_select_topic);
                            break;
                    }
                    mSelectTopicTextView.setText(whatsOnTopicsLocale);
                }
                showSelectTopicList();
                refreshWhatsOnList();
                mWhatsOnTopicAdapter.notifyDataSetChanged();
            }
        }));

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

        setTitle(getString(R.string.wo_title));
        setHasOptionsMenu(true);

        FlurryAgent.logEvent("What's On List");

        String token = sharedPreferences.getString("access_token", null);
        String accountId = sharedPreferences.getString("account_id", null);
        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    public void refreshWhatsOnList() {
//        String selectedTopic = mSelectTopicTextView.getText().toString();
        String selectedTopic = mLocaleSelectedTxt;

        mWhatsOnList.clear();
        if ("SELECT TOPICS".equals(selectedTopic)) {
            mWhatsOnList = mCurriculumAdapter.getAllWhatsOn();
        } else {
            mWhatsOnList = mCurriculumAdapter.getWhatsOnByTopic(selectedTopic);
        }
        mWhatsOnListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        setDisplayHomeAsUpEnable(true);
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.wo_dialog_loading));
        if (mWhatsOnList.size() == 0) {
            dialog.show();
        }

        mServerRequestManager.getWhatsOn(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                dialog.dismiss();

                mWhatsOnList = mCurriculumAdapter.getAllWhatsOn();
                mSelectTopics = mCurriculumAdapter.getWhatsOnTopics();

                mWhatsOnListAdapter.notifyDataSetChanged();
                mWhatsOnTopicAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(ServerError err) {
                if (getActivity() != null) {
                    dialog.dismiss();
                    Toast.makeText(getActivity(), getString(R.string.wo_connection_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWhatsOnList.clear();
        rvWhatsOnSelectTopic.setAdapter(null);
        rvWhatsOnSelectTopic = null;
        System.gc();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_whats_on_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.opt_search:
                FlurryAgent.logEvent("Search what's on");

                mSearchBarRelativeLayout.setVisibility(View.VISIBLE);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                mSearchEditText.setText("");
                mSearchEditText.setEnabled(true);
                mSearchEditText.requestFocus();
                imm.toggleSoftInputFromWindow(mSearchEditText.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                hideActionBar();
                rvWhatsOnSelectTopic.setVisibility(View.GONE);
                return true;
            case R.id.opt_favourite:
                openRightDrawer();
                return true;
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawers();
        } else {
            getFragmentManager().popBackStack();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_select_whats_on_topic:
                showSelectTopicList();
                break;
        }
    }

    private void showSelectTopicList() {
        if (rvWhatsOnSelectTopic.getVisibility() == View.VISIBLE) {
            rvWhatsOnSelectTopic.setVisibility(View.GONE);
        } else {
            rvWhatsOnSelectTopic.setVisibility(View.VISIBLE);
        }
    }

    private class WhatsOnSelectTopicAdapter extends RecyclerView.Adapter<WhatsOnSelectTopicAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_whats_on_topic, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String whatsOnTopic = mSelectTopics.get(position);
            String whatsOnTopicsLocale;

            switch (whatsOnTopic) {
                case "Phrases":
                    whatsOnTopicsLocale = getString(R.string.wo_phrases);
                    break;
                case "Language Learning":
                    whatsOnTopicsLocale = getString(R.string.wo_language_learning);
                    break;
                case "Conversations":
                    whatsOnTopicsLocale = getString(R.string.wo_conversation);
                    break;
                case "Survival Skills":
                    whatsOnTopicsLocale = getString(R.string.wo_survival_skills);
                    break;
                case "Understanding China":
                    whatsOnTopicsLocale = getString(R.string.wo_understanding_china);
                    break;
                case "Funny":
                    whatsOnTopicsLocale = getString(R.string.wo_funny);
                    break;
                case "Readable Chinese":
                    whatsOnTopicsLocale = getString(R.string.wo_readable_chinese);
                    break;
                default:
                    whatsOnTopicsLocale = whatsOnTopic;
                    break;
            }

            if (whatsOnTopicsLocale.equals("All"))
                whatsOnTopicsLocale = getString(R.string.wo_list_all);

            holder.tvSelectTopics.setText(whatsOnTopicsLocale);

            if (whatsOnTopicsLocale.equals(mSelectTopicTextView.getText().toString())) {
                holder.imvSelectedMark.setVisibility(View.VISIBLE);
            } else if (getString(R.string.wo_select_topic).equals(mSelectTopicTextView.getText().toString()) &&
                    "All".equals(whatsOnTopic)) {
                holder.imvSelectedMark.setVisibility(View.VISIBLE);
            } else {
                holder.imvSelectedMark.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return mSelectTopics.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSelectTopics;
            ImageView imvSelectedMark;

            public ViewHolder(View itemView) {
                super(itemView);
                tvSelectTopics = itemView.findViewById(R.id.tv_select_topic);

                imvSelectedMark = itemView.findViewById(R.id.imv_selected_mark);
            }
        }
    }

    private class WhatsOnListAdapter extends RecyclerView.Adapter<WhatsOnListAdapter.ViewHolder> {

        WhatsOnListAdapter() {
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_whats_on, parent, false); //Inflating the layout

            return new ViewHolder(v); // Returning the created object
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final WhatsOn whatsOn = mWhatsOnList.get(position);

            holder.tvWhatsOnTitle.setText(mWhatsOnList.get(position).getTitle());

            if (whatsOn.getmWhatsOnPostedDate() != null) {
                holder.tvWhatsOnPostedDate.setText(CommonUtil.getCustomDateResult(whatsOn.getmWhatsOnPostedDate(), "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd"));
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, String> params = new HashMap<>();
                    params.put("Topic", whatsOn.getTopicName());
                    FlurryAgent.logEvent("Choose what's on", params);

                    if (getActivity() != null)
                        CommonUtil.hideKeyBoard(getActivity(), v);

                    Fragment courseFragment = new WhatsOnFragment();

                    FragmentManager fm = getFragmentManager();
                    Fragment fragment = fm.findFragmentById(R.id.container);

                    Bundle bundle = new Bundle();
                    bundle.putString(WhatsOnFragment.EXTRA_WHATS_ON_ID, whatsOn.getWhatsOnId());
                    courseFragment.setArguments(bundle);

                    if (fragment == null) {
                        fm.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                .addToBackStack(null)
                                .add(R.id.container, courseFragment).commit();
                    } else {
                        fm.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                .addToBackStack(null)
                                .replace(R.id.container, courseFragment).commit();
                    }
                }
            });

            holder.sdvCoverPhoto.setImageURI(Uri.parse(whatsOn.getCoverPhoto()));
        }

        @Override
        public int getItemCount() {
            return mWhatsOnList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView tvWhatsOnTitle;

            private TextView tvWhatsOnPostedDate;

            private SimpleDraweeView sdvCoverPhoto;

            public ViewHolder(View itemView) {
                super(itemView);

                tvWhatsOnTitle = itemView.findViewById(R.id.tv_whats_on_title);

                tvWhatsOnPostedDate = itemView.findViewById(R.id.tv_posted_date);

                sdvCoverPhoto = itemView.findViewById(R.id.sdv_cover_photo);
            }
        }
    }
}
