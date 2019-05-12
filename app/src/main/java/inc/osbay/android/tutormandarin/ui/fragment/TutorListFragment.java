package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;
import inc.osbay.android.tutormandarin.sdk.database.TutorAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Tutor;
import inc.osbay.android.tutormandarin.sdk.util.FileDownloader;
import inc.osbay.android.tutormandarin.util.CommonUtil;

public class TutorListFragment extends BackHandledFragment {

    SharedPreferences sharedPreferences;
    boolean isPlaying;
    private ServerRequestManager mServerRequestManager;
    private TutorAdapter mTutorDbAdapter;
    private TutorListAdapter mTutorListAdapter;
    private List<Tutor> mTutorList;
    private RelativeLayout mSearchBarRelativeLayout;
    private EditText mSearchEditText;
    private MediaPlayer mMediaPlayer;
    private String mCurrentPlayingVoice;
    private DrawerLayout mDrawerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mServerRequestManager = new ServerRequestManager(getActivity().getApplicationContext());
        mTutorDbAdapter = new TutorAdapter(getActivity());

        mTutorList = mTutorDbAdapter.getAllTutor();
        mTutorListAdapter = new TutorListAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_tutor_list, container, false);

        Toolbar toolBar;
        toolBar = rootView.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#F2C666"));
        setSupportActionBar(toolBar);

        final RecyclerView rvTutorList = rootView.findViewById(R.id.rv_tutor_list);
        rvTutorList.setAdapter(mTutorListAdapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvTutorList.setLayoutManager(layoutManager);

        mSearchBarRelativeLayout = rootView.findViewById(R.id.rl_search_bar);

        ImageView searchCancelImageView = rootView.findViewById(R.id.imv_search_cancel);
        searchCancelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchBarRelativeLayout.setVisibility(View.INVISIBLE);
                mSearchEditText.setEnabled(false);

                if (getActivity() != null)
                    CommonUtil.hideKeyBoard(getActivity(), view);

                showActionBar();
            }
        });

        mSearchEditText = rootView.findViewById(R.id.edt_search_text);
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String tutorName = mSearchEditText.getText().toString();
                if (!TextUtils.isEmpty(tutorName)) {
                    mTutorList = mTutorDbAdapter.searchTutorByName(tutorName);
                } else {
                    mTutorList = mTutorDbAdapter.getAllTutor();
                }
                mTutorListAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

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
                RelativeLayout frame = rootView.findViewById(R.id.rl_main_content);

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

        FlurryAgent.logEvent("Tutor List");

        setTitle(getString(R.string.tu_lst_title));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);

        String token = sharedPreferences.getString("access_token", null);
        String accountId = sharedPreferences.getString("account_id", null);
        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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
        setDisplayHomeAsUpEnable(true);
        setTitle(getString(R.string.tu_lst_title));

        if (mMediaPlayer != null && isPlaying) {
            mMediaPlayer.start();
            isPlaying = false;
        }

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.tu_lst_loading));

        if (mTutorList.size() == 0) {
            progressDialog.show();
        }

        mServerRequestManager.downloadTutorList(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                progressDialog.dismiss();

                if (getActivity() != null && result != null) {
                    mTutorList.clear();

                    List<?> objects = (List<?>) result;
                    for (Object obj : objects) {
                        if (obj instanceof Tutor) {
                            mTutorList.add((Tutor) obj);
                        }
                    }
                    mTutorListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(ServerError err) {
                progressDialog.dismiss();

                if (getActivity() != null) {
                    Toast.makeText(getActivity(), getString(R.string.tu_lst_refresh_failed), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    @Override
    public void onPause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            isPlaying = true;
        }

        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_tutor_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.opt_search:
                FlurryAgent.logEvent("Search tutor");

                mSearchBarRelativeLayout.setVisibility(View.VISIBLE);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                mSearchEditText.setEnabled(true);
                mSearchEditText.requestFocus();
                imm.toggleSoftInputFromWindow(mSearchEditText.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);

                hideActionBar();
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
            if (getFragmentManager().getBackStackEntryCount() > 1) {
                getFragmentManager().beginTransaction()
                        .remove(TutorListFragment.this)
                        .commit();

                getFragmentManager().popBackStack(getFragmentManager().getBackStackEntryAt(0).getId(),
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else {
                getFragmentManager().popBackStack();
            }
        }
        return false;
    }

    private class TutorListAdapter extends RecyclerView.Adapter<TutorListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tutor, parent, false); //Inflating the layout

            return new ViewHolder(v, viewType);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final Tutor tutor = mTutorList.get(position);

            holder.tvTutorName.setText(tutor.getName());
            holder.tvTutorExp.setText(tutor.getTeachingExp());
            holder.tvTutorRate.setText(String.valueOf(tutor.getRate()));
            holder.tvCreditWeight.setText(String.format(Locale.getDefault(), "%.1f", tutor.getCreditWeight()));
            holder.tvTutorLocation.setText(tutor.getLocation());

            if (tutor.getAvatar() != null) {
                holder.sdvTutorPhoto.setImageURI(Uri.parse(tutor.getAvatar()));
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null)
                        CommonUtil.hideKeyBoard(getActivity(), v);

                    Fragment mainFragment = new TutorInfoFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(TutorInfoFragment.EXTRA_TUTOR_ID, tutor.getTutorId());
                    mainFragment.setArguments(bundle);

                    FragmentManager fm = getFragmentManager();
                    Fragment fragment = fm.findFragmentById(R.id.container);
                    if (fragment == null) {
                        fm.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                .addToBackStack(null)
                                .add(R.id.container, mainFragment).commit();
                    } else {
                        fm.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                .addToBackStack(null)
                                .replace(R.id.container, mainFragment).commit();
                    }
                }
            });

            if (mMediaPlayer != null && mMediaPlayer.isPlaying() && tutor.getTutorId().equals(mCurrentPlayingVoice)) {
                holder.imvPlayVoice.setImageResource(R.drawable.ic_pause_voice);
            } else {
                holder.imvPlayVoice.setImageResource(R.drawable.ic_play_voice);
            }

            holder.imvPlayVoice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FlurryAgent.logEvent("Click listen button (tutor's voice)");

                    if (!TextUtils.isEmpty(tutor.getIntroVoice())) {

                        if (mMediaPlayer != null &&
                                !tutor.getTutorId().equals(mCurrentPlayingVoice)) {

                            if (mMediaPlayer.isPlaying())
                                mMediaPlayer.stop();

                            mMediaPlayer.reset();
                            mMediaPlayer.release();

                            mMediaPlayer = null;
                            mCurrentPlayingVoice = null;
                        }

                        String fileName = tutor.getIntroVoice().substring(tutor.getIntroVoice().lastIndexOf('/') + 1, tutor.getIntroVoice().length());
                        final File file = new File(CommonConstant.MEDIA_PATH, fileName);

                        if (file.exists()) {
                            if (mMediaPlayer == null) {
                                mMediaPlayer = new MediaPlayer();
                                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mediaPlayer) {
                                        mMediaPlayer.stop();
                                        mMediaPlayer.reset();
                                        mMediaPlayer = null;

                                        mCurrentPlayingVoice = null;

                                        notifyDataSetChanged();
                                    }
                                });
                                try {
                                    mMediaPlayer.setDataSource(file.getAbsolutePath());
                                    mMediaPlayer.prepare();
                                    mMediaPlayer.start();

                                    mCurrentPlayingVoice = tutor.getTutorId();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {

                                if (mMediaPlayer.isPlaying()) {
                                    mMediaPlayer.pause();
                                } else {
                                    mMediaPlayer.start();

                                    mCurrentPlayingVoice = tutor.getTutorId();
                                }
                            }

                            notifyDataSetChanged();
                        } else {

                            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                            progressDialog.setMessage(getString(R.string.tu_lst_media_loading));
                            progressDialog.setCancelable(false);
                            progressDialog.show();

                            FileDownloader.downloadImage(tutor.getIntroVoice(), new FileDownloader.OnDownloadFinishedListener() {
                                @Override
                                public void onSuccess() {
                                    progressDialog.dismiss();

                                    if (getActivity() != null) {
                                        mMediaPlayer = new MediaPlayer();
                                        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                            @Override
                                            public void onCompletion(MediaPlayer mediaPlayer) {
                                                mMediaPlayer.stop();
                                                mMediaPlayer.reset();
                                                mMediaPlayer = null;

                                                mCurrentPlayingVoice = null;

                                                notifyDataSetChanged();
                                            }
                                        });

                                        try {

                                            mMediaPlayer.setDataSource(file.getAbsolutePath());
                                            mMediaPlayer.prepare();
                                            mMediaPlayer.start();

                                            mCurrentPlayingVoice = tutor.getTutorId();
                                            notifyDataSetChanged();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                @Override
                                public void onError() {
                                    progressDialog.dismiss();

                                    if (getActivity() != null) {
                                        Toast.makeText(getActivity(), getString(R.string.tu_lst_cant_download_video), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                    }

                }
            });

        }

        @Override
        public int getItemCount() {
            return mTutorList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            int itemType;

            TextView tvTutorName;

            TextView tvTutorRate;

            TextView tvTutorExp;

            TextView tvCreditWeight;

            TextView tvTutorLocation;

            ImageView imvPlayVoice;

            SimpleDraweeView sdvTutorPhoto;

            View vwSpeaker;

            public ViewHolder(View itemView, int viewType) {
                super(itemView);
                itemType = viewType;

                tvTutorName = itemView.findViewById(R.id.tv_course_title);

                tvTutorExp = itemView.findViewById(R.id.tv_tutor_exp);

                tvTutorRate = itemView.findViewById(R.id.tv_tutor_rate);

                tvCreditWeight = itemView.findViewById(R.id.tv_credit_weight);

                tvTutorLocation = itemView.findViewById(R.id.tv_tutor_location);

                vwSpeaker = itemView.findViewById(R.id.vw_speaker);

                imvPlayVoice = itemView.findViewById(R.id.imv_play_voice);

                sdvTutorPhoto = itemView.findViewById(R.id.sdv_tutor_photo);

            }


        }

    }
}
