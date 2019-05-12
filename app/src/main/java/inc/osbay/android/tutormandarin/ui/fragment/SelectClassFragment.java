package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.sdk.model.Topic;
import inc.osbay.android.tutormandarin.sdk.model.TopicClass;

public class SelectClassFragment extends BackHandledFragment {

    private CurriculumAdapter curriculumAdapter;

    private List<String> mLevelList;
    private List<Topic> mTopicList;
    private List<TopicClass> mTopicClasses;
    private Dialog mSelectLevelDialog;
    private Dialog mSelectTopicDialog;
    private TextView mSelectLevelTextView;
    private TextView mSelectTopicTextView;

    private String mSelectedLevel;
    private Topic mSelectedTopic;
    private ClassListRVAdapter mClassListRVAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        curriculumAdapter = new CurriculumAdapter(getActivity());
        mLevelList = curriculumAdapter.getTopicLevels();
        mTopicList = curriculumAdapter.getTopicList();
        mTopicClasses = new ArrayList<>();

        mClassListRVAdapter = new ClassListRVAdapter();

        // Level Select Dialog
        mSelectLevelDialog = new Dialog(getActivity());
        mSelectLevelDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        ListView rvLevelList = new ListView(getActivity());
        rvLevelList.setAdapter(new SelectLevelRVAdapter());
        mSelectLevelDialog.setContentView(rvLevelList);

        // Topic Select Dialog
        mSelectTopicDialog = new Dialog(getActivity());
        mSelectTopicDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        ListView rvTopicList = new ListView(getActivity());
        rvTopicList.setAdapter(new SelectTopicRVAdapter()); //TODO
        mSelectTopicDialog.setContentView(rvTopicList);
    }

    @Override
    public void onStart() {
        super.onStart();

        showActionBar();
        setTitle(getString(R.string.to_class_title));
        setDisplayHomeAsUpEnable(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_select_class, container, false);

        mSelectLevelTextView = rootView.findViewById(R.id.tv_select_level);
        mSelectTopicTextView = rootView.findViewById(R.id.tv_select_topic);

        if (mSelectedLevel != null) {
            mSelectLevelTextView.setText(changeTopicLevelLocale(mSelectedLevel));
            mSelectTopicTextView.setText(changeLocale(mSelectedTopic.getTitle()));
        }

        mSelectLevelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLevelList.size() != 0) {
                    mSelectLevelDialog.show();
                }
            }
        });

        mSelectTopicTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTopicList.size() != 0) {
                    mSelectTopicDialog.show();
                }
            }
        });

        RecyclerView rvClassList = rootView.findViewById(R.id.rv_class_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvClassList.setLayoutManager(layoutManager);
        rvClassList.setAdapter(mClassListRVAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        ServerRequestManager serverRequestManager = new ServerRequestManager(getActivity());
        serverRequestManager.downloadTopicList(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {

                if (getActivity() != null) {
                    mLevelList = curriculumAdapter.getTopicLevels();
                    mSelectedLevel = mLevelList.get(0);
                    mTopicList = curriculumAdapter.getTopicByLevel(mSelectedLevel);
                    mSelectedTopic = mTopicList.get(0);

                    mTopicClasses = curriculumAdapter.getTopicClasses(mSelectedTopic.getTopicId());
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

//        if (mLevelList.size() == 0 || mTopicList.size() == 0) {
//            mLevelList = curriculumAdapter.getTopicLevels();
//            mSelectedLevel = mLevelList.get(0);
//
//            mTopicList = curriculumAdapter.getTopicByLevel(mSelectedLevel);
//            mSelectedTopic = mTopicList.get(0);
//
//            mTopicClasses = curriculumAdapter.getTopicClasses(mSelectedTopic.getTopicId());
//        }
    }

    @Override
    public boolean onBackPressed() {
        getActivity().finish();
        return false;
    }

    private void goToClassFragment(TopicClass topicClass) {
        Bundle bundle = new Bundle();
        bundle.putString(ClassFragment.EXTRA_CLASS_ID, topicClass.getClassId());

        Fragment classFragment = new ClassFragment();
        classFragment.setArguments(bundle);

        FragmentManager frgMgr = getFragmentManager();
        Fragment frg = frgMgr.findFragmentById(R.id.fragment_holder_container);
        if (frg == null) {
            frgMgr.beginTransaction()
                    .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                            R.animator.fragment_out_new, R.animator.fragment_out_old)
                    .addToBackStack(null)
                    .add(R.id.fragment_holder_container, classFragment).commit();
        } else {
            frgMgr.beginTransaction()
                    .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                            R.animator.fragment_out_new, R.animator.fragment_out_old)
                    .addToBackStack(null)
                    .replace(R.id.fragment_holder_container, classFragment).commit();
        }
    }

    private void showLimitedDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.to_unavailable_title))
                .setMessage(getString(R.string.to_unavailable_msg))
                .setPositiveButton(getString(R.string.to_unavailable_ok), null)
                .show();
    }

    private String changeLocale(String title) {
        String titleLocale;

        switch (title) {
            case "Food":
                titleLocale = getString(R.string.to_locale_food);
                break;
            case "Entertainment":
                titleLocale = getString(R.string.to_locale_entertainment);
                break;
            case "Relationships":
                titleLocale = getString(R.string.to_locale_relationships);
                break;
            case "Hobbies":
                titleLocale = getString(R.string.to_locale_hobbies);
                break;
            case "Travel":
                titleLocale = getString(R.string.to_locale_travel);
                break;
            case "Culture":
                titleLocale = getString(R.string.to_locale_culture);
                break;
            case "Social":
                titleLocale = getString(R.string.to_locale_social);
                break;
            case "Exercise":
                titleLocale = getString(R.string.to_locale_exercise);
                break;
            default:
                titleLocale = title;
                break;
        }
        return titleLocale;
    }

    private String changeTopicLevelLocale(String level) {
        String selectedLevel;

        switch (level) {
            case "All":
                selectedLevel = getString(R.string.co_locale_all);
                break;
            case "Beginner 1":
                selectedLevel = getString(R.string.co_locale_beginner_1);
                break;
            case "Beginner 2":
                selectedLevel = getString(R.string.co_locale_beginner_2);
                break;
            case "Intermediate 1":
                selectedLevel = getString(R.string.co_locale_intermediate_1);
                break;
            case "Intermediate 2":
                selectedLevel = getString(R.string.co_locale_intermediate_2);
                break;
            case "Advanced 1":
                selectedLevel = getString(R.string.co_locale_advanced_1);
                break;
            case "Advanced 2":
                selectedLevel = getString(R.string.co_locale_advanced_2);
                break;
            default:
                selectedLevel = level;
                break;
        }

        return selectedLevel;
    }

    private class SelectLevelRVAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mLevelList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.item_whats_on_topic, viewGroup, false);
            }

            final String level = mLevelList.get(i);

            TextView tvTitle = view.findViewById(R.id.tv_select_topic);
            tvTitle.setText(changeTopicLevelLocale(level));
            tvTitle.setGravity(Gravity.CENTER);

            ImageView imvSelect = view.findViewById(R.id.imv_selected_mark);
            imvSelect.setVisibility(View.GONE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectLevelDialog.dismiss();
                    mSelectedLevel = level;

                    mTopicList = curriculumAdapter.getTopicByLevel(mSelectedLevel);
                    try {
                        mSelectedTopic = mTopicList.get(0);
                    } catch (IndexOutOfBoundsException e) {
                        return;
                    }

                    mTopicClasses = curriculumAdapter.getTopicClasses(mSelectedTopic.getTopicId());

                    final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage(getString(R.string.to_class_dialog_loading));
                    progressDialog.setCancelable(false);

                    if (mTopicClasses.size() == 0) {
                        progressDialog.show();
                    }

                    ServerRequestManager serverRequestManager = new ServerRequestManager(getActivity());
                    serverRequestManager.downloadClassList(mSelectedTopic.getTopicId(),
                            new ServerRequestManager.OnRequestFinishedListener() {
                                @Override
                                public void onSuccess(Object result) {
                                    progressDialog.dismiss();

                                    if (getActivity() != null) {
                                        mTopicClasses = curriculumAdapter.getTopicClasses(mSelectedTopic.getTopicId());

                                        mSelectLevelTextView.setText(changeTopicLevelLocale(mSelectedLevel));
                                        mSelectTopicTextView.setText(changeLocale(mSelectedTopic.getTitle()));
                                        mClassListRVAdapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onError(ServerError err) {
                                    progressDialog.dismiss();

                                    if (getActivity() != null) {
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle(getString(R.string.to_class_dialog_download_error))
                                                .setMessage(err.getMessage())
                                                .setPositiveButton(getString(R.string.to_class_dialog_download_ok), null)
                                                .create()
                                                .show();
                                    }
                                }
                            });

                    mSelectLevelTextView.setText(changeTopicLevelLocale(mSelectedLevel));
                    mSelectTopicTextView.setText(changeLocale(mSelectedTopic.getTitle()));
                    mClassListRVAdapter.notifyDataSetChanged();
                }
            });

            return view;
        }
    }

    private class SelectTopicRVAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mTopicList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.item_whats_on_topic, viewGroup, false);
            }

            final Topic topic = mTopicList.get(i);

            TextView tvTitle = view.findViewById(R.id.tv_select_topic);
            tvTitle.setText(changeLocale(topic.getTitle()));
            tvTitle.setGravity(Gravity.CENTER);

            ImageView imvSelect = view.findViewById(R.id.imv_selected_mark);
            imvSelect.setVisibility(View.GONE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectTopicDialog.dismiss();
                    mSelectedTopic = topic;

                    mTopicClasses = curriculumAdapter.getTopicClasses(mSelectedTopic.getTopicId());

                    final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage(getString(R.string.to_class_loading));
                    progressDialog.setCancelable(false);

                    if (mTopicClasses.size() == 0) {
                        progressDialog.show();
                    }

                    ServerRequestManager serverRequestManager = new ServerRequestManager(getActivity());
                    serverRequestManager.downloadClassList(mSelectedTopic.getTopicId(),
                            new ServerRequestManager.OnRequestFinishedListener() {
                                @Override
                                public void onSuccess(Object result) {
                                    progressDialog.dismiss();

                                    if (getActivity() != null) {
                                        mTopicClasses = curriculumAdapter.getTopicClasses(mSelectedTopic.getTopicId());

                                        mSelectLevelTextView.setText(changeTopicLevelLocale(mSelectedLevel));
                                        mSelectTopicTextView.setText(changeLocale(mSelectedTopic.getTitle()));
                                        mClassListRVAdapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onError(ServerError err) {
                                    progressDialog.dismiss();

                                    if (getActivity() != null) {
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle(getString(R.string.to_class_dialog_download_error))
                                                .setMessage(err.getMessage())
                                                .setPositiveButton(getString(R.string.to_class_dialog_download_ok), null)
                                                .create()
                                                .show();
                                    }
                                }
                            });

                    mSelectLevelTextView.setText(changeTopicLevelLocale(mSelectedLevel));
                    mSelectTopicTextView.setText(changeLocale(mSelectedTopic.getTitle()));
                    mClassListRVAdapter.notifyDataSetChanged();
                }
            });

            return view;
        }
    }

    private class ClassListRVAdapter extends RecyclerView.Adapter<ClassListRVAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class,
                    parent, false); //Inflating the layout

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final TopicClass topicClass = mTopicClasses.get(position);

            holder.tvTitle.setText(topicClass.getTitle());

            holder.tvCredit.setText(String.format(Locale.getDefault(), "%.0f", topicClass.getCredit()));

            if (!TextUtils.isEmpty(topicClass.getCoverPhoto())) {
                holder.sdvCoverPhoto.setImageURI(Uri.parse(topicClass.getCoverPhoto()));
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final String token = prefs.getString("access_token", null);
            final String accountId = prefs.getString("account_id", null);

            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
                AccountAdapter accountAdapter = new AccountAdapter(getActivity());
                Account account = accountAdapter.getAccountById(accountId);
                if (account != null /*&& account.getStatus() == 4*/) {
                    if (topicClass.isAvailable()) {
                        holder.imvTopicOverlay.setImageDrawable(null);
                    } else {
                        holder.imvTopicOverlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock));
                    }
                } else {
                    holder.imvTopicOverlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock));
                }
            }


            String content = topicClass.getVocabCount() + getString(R.string.to_class_vocab_count);
//                        + ", " + lesson.getGrammarCount() + " Grammar";

            holder.tvContent.setText(content);

            if (topicClass.isCompleted()) {
                holder.imvCompletedMark.setVisibility(View.VISIBLE);

            } else {
                holder.imvCompletedMark.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
                        AccountAdapter accountAdapter = new AccountAdapter(getActivity());
                        Account account = accountAdapter.getAccountById(accountId);
                        if (account != null /*&& account.getStatus() == 4*/) {
                            if (topicClass.isAvailable()) {
                                goToClassFragment(topicClass);
                            } else {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(getString(R.string.cl_topic_level_high))
                                        .setMessage(getString(R.string.cl_topic_level_high_msg))
                                        .setPositiveButton(getString(R.string.cl_topic_level_high_ok), null)
                                        .show();
                            }
                        } else {
                            showLimitedDialog();
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mTopicClasses.size(); // the number of items in the list will be +1 the titles including the header vGreenBar.
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            SimpleDraweeView sdvCoverPhoto;
            ImageView imvTopicOverlay;
            ImageView imvCompletedMark;
            TextView tvTitle;
            TextView tvContent;
            TextView tvCredit;

            public ViewHolder(View itemView) {
                super(itemView);

                sdvCoverPhoto = itemView.findViewById(R.id.sdv_cover_photo);
                imvTopicOverlay = itemView.findViewById(R.id.imv_topic_overlay);
                imvCompletedMark = itemView.findViewById(R.id.imv_completed_mark);
                tvTitle = itemView.findViewById(R.id.tv_class_title);
                tvContent = itemView.findViewById(R.id.tv_class_content);
                tvCredit = itemView.findViewById(R.id.tv_class_credit);
            }
        }
    }
}
