package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

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

public class TopicFragment extends BackHandledFragment {

    public static final String EXTRA_TOPIC_ID = "TopicFragment.EXTRA_TOPIC_ID";
    private Topic mTopic;

    private List<TopicClass> mTopicClassList;
    private ClassListRVAdapter mClassListRVAdapter;
    private ServerRequestManager mServerRequestManager;
    private CurriculumAdapter mCurriculumDbAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mServerRequestManager = new ServerRequestManager(getActivity().getApplicationContext());
        mCurriculumDbAdapter = new CurriculumAdapter(getActivity());

        Bundle bundle = getArguments();
        if (bundle != null) {
            mTopic = mCurriculumDbAdapter.getTopicById(bundle.getString(EXTRA_TOPIC_ID));
        }

        if (mTopic == null) {
            onBackPressed();
            return;
        }

        mTopicClassList = mCurriculumDbAdapter.getTopicClasses(mTopic.getTopicId());
        mClassListRVAdapter = new ClassListRVAdapter();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_topic, container, false);

        Toolbar toolBar;
        toolBar = rootView.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        setSupportActionBar(toolBar);

        SimpleDraweeView sdvCoverPhoto = rootView.findViewById(R.id.sdv_cover_photo);
        if (!TextUtils.isEmpty(mTopic.getPhotoUrl())) {
            sdvCoverPhoto.setImageURI(Uri.parse(mTopic.getPhotoUrl()));
        }

        TextView tvDescription = rootView.findViewById(R.id.tv_description);
        tvDescription.setMovementMethod(new ScrollingMovementMethod());
        tvDescription.setText(Html.fromHtml(changeTopicDescLocale(mTopic.getDescription())));

        RecyclerView rvClasses = rootView.findViewById(R.id.rv_class_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvClasses.setLayoutManager(layoutManager);
        rvClasses.setAdapter(mClassListRVAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getActivity() != null)
            setTitle(changeLocale(mTopic.getTitle()));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.to_class_loading));
        if (mTopicClassList.size() == 0) {
            progressDialog.show();
        }

        mServerRequestManager.downloadClassList(mTopic.getTopicId(),
                new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        progressDialog.dismiss();

                        if (getActivity() != null) {
                            mTopicClassList = mCurriculumDbAdapter.getTopicClasses(mTopic.getTopicId());
                            mClassListRVAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        progressDialog.dismiss();
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), err.getErrorCode() + " - " + err.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return onBackPressed();
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStack();
        return false;
    }

    private void showLimittedDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.to_unavailable_user))
                .setMessage(getString(R.string.to_unavailable_user_msg))
                .setPositiveButton(getString(R.string.to_unavailable_user_ok), null)
                .show();
    }

    private void goToClassFragment(TopicClass topicClass) {
        Bundle bundle = new Bundle();
        bundle.putString(ClassFragment.EXTRA_CLASS_ID, topicClass.getClassId());

        Fragment lessonFragment = new ClassFragment();
        lessonFragment.setArguments(bundle);

        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.container);
        if (fragment == null) {
            fm.beginTransaction()
                    .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                            R.animator.fragment_out_new, R.animator.fragment_out_old)
                    .addToBackStack(null)
                    .add(R.id.container, lessonFragment).commit();
        } else {
            fm.beginTransaction()
                    .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                            R.animator.fragment_out_new, R.animator.fragment_out_old)
                    .addToBackStack(null)
                    .replace(R.id.container, lessonFragment).commit();
        }
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

    private String changeTopicDescLocale(String description) {
        String topicDescLocale;
        switch (description) {
            case "Talk about everything food related in Chinese - dishes, etiquette, ordering, preparation, and more.":
                topicDescLocale = getString(R.string.to_locale_desc_1);
                break;
            case "Discuss how you spend your free time and what you do for entertainment using modern, practical Chinese.":
                topicDescLocale = getString(R.string.to_locale_desc_2);
                break;
            case "Learn how to communicate in Chinese within various types of relationships - romantic, familial, professional, or otherwise.":
                topicDescLocale = getString(R.string.to_locale_desc_3);
                break;
            case "Use Chinese to discuss a variety of hobbies including photography, music, movies, books, and more.":
                topicDescLocale = getString(R.string.to_locale_desc_4);
                break;
            case "Deepen your knowledge about the Chinese culture through related topics, stories, and history.":
                topicDescLocale = getString(R.string.to_locale_desc_5);
                break;
            case "Learn practical Chinese for traveling in China with classes on tickets, transportation, attractions, small talk, and more.":
                topicDescLocale = getString(R.string.to_locale_desc_6);
                break;
            case "Use Chinese to discuss deeper social issues on topics such as environment, economy, politics, and more.":
                topicDescLocale = getString(R.string.to_locale_desc_7);
                break;
            case "Talk about everything exercise related so you can discuss both your own fitness habits as well as popular sports culture.<":
                topicDescLocale = getString(R.string.to_locale_desc_8);
                break;
            default:
                topicDescLocale = description;
        }
        return topicDescLocale;
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
            final TopicClass topicClass = mTopicClassList.get(position);

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


            String content = topicClass.getVocabCount() + getString(R.string.to_class_vocab);
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
                        if (account != null /*&& account.getStatus() == Account.Status.ACTIVE*/) {
                            if (topicClass.isAvailable()) {
                                goToClassFragment(topicClass);
                            } else {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(getString(R.string.to_above_level))
                                        .setMessage(getString(R.string.to_above_level_msg))
                                        .setPositiveButton(getString(R.string.to_above_level_ok), null)
                                        .show();
                            }
                        } else {
                            showLimittedDialog();
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mTopicClassList.size(); // the number of items in the list will be +1 the titles including the header vGreenBar.
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
