package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Topic;
import inc.osbay.android.tutormandarin.ui.view.RecyclerItemClickListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class TopicListFragment extends Fragment implements View.OnClickListener {
    public static final String EXTRA_TOPIC_LEVEL = "TopicListFragment.EXTRA_TOPIC_LEVEL";
    private TopicListRVAdapter mTopicListRVAdapter;
    private LevelsRVAdapter mLevelsRVAdapter;
    private List<Topic> mTopics = new ArrayList<>();
    private List<String> mTopicLevel = new ArrayList<>();
    private RecyclerView mRvLevelTopic;
    private SharedPreferences mSharedPreferences;
    private TextView tvTopicLevel;
    private String mSavedLevel;
    private CurriculumAdapter curriculumAdapter;
    private ServerRequestManager mServerRequestManager;
    private String mSelectedLevel;

    public TopicListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTopicListRVAdapter = new TopicListRVAdapter();
        mLevelsRVAdapter = new LevelsRVAdapter();
        curriculumAdapter = new CurriculumAdapter(getActivity());

        mServerRequestManager = new ServerRequestManager(getActivity().getApplicationContext());
        mServerRequestManager.downloadTopicList(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                mTopics = curriculumAdapter.getTopicList();
                mTopicLevel = curriculumAdapter.getTopicLevels();
                refreshTopicList();
                if (getActivity() != null) {
                    /*mCourseTopicTabAdapter.notifyDataSetChanged();

                    for (int i = 0; i < mTabLayout.getTabCount(); i++) {
                        if (i == 0) {
                            mTabLayout.getTabAt(i).setIcon(R.drawable.ic_tab_course);
                        } else {
                            mTabLayout.getTabAt(i).setIcon(R.drawable.ic_tab_topic);
                        }
                    }*/
                }
            }

            @Override
            public void onError(ServerError err) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), getString(R.string.co_download_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*@Override
    public void onStart() {
        super.onStart();

        mSelectedLevel = mSavedLevel;
        refreshTopicList();
        tvTopicLevel.setText(changeTopicLevelLocale(mSavedLevel));
        mLevelsRVAdapter.notifyDataSetChanged();
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_topic_list, container, false);

        mSharedPreferences = getActivity().getSharedPreferences(EXTRA_TOPIC_LEVEL, Context.MODE_PRIVATE);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mSavedLevel = mSharedPreferences.getString(EXTRA_TOPIC_LEVEL, "All");

        tvTopicLevel = v.findViewById(R.id.tv_topic_level);
        RecyclerView rvTopicList = v.findViewById(R.id.rv_topic_list);
        mRvLevelTopic = v.findViewById(R.id.rv_level_topic);
        RelativeLayout rlSelectTopicLevel = v.findViewById(R.id.rl_select_topic_level);

        LinearLayoutManager llmLevelTopic = new LinearLayoutManager(getActivity());
        LinearLayoutManager llmTopicList = new LinearLayoutManager(getActivity());
        rvTopicList.setLayoutManager(llmTopicList);
        mRvLevelTopic.setLayoutManager(llmLevelTopic);
        rvTopicList.setAdapter(mTopicListRVAdapter);

        mTopicLevel = curriculumAdapter.getTopicLevels();
        mRvLevelTopic.setAdapter(mLevelsRVAdapter);

        mRvLevelTopic.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String mLevelChoose = mTopicLevel.get(position);
                if ("All".equals(mLevelChoose)) {
                    mSelectedLevel = "All";
                } else {
                    mSelectedLevel = mLevelChoose;
                }

                mSharedPreferences.edit().putString(EXTRA_TOPIC_LEVEL, mSelectedLevel).apply();

                refreshTopicList();
                tvTopicLevel.setText(changeTopicLevelLocale(mSelectedLevel));
                showTopicLevelList();
                mLevelsRVAdapter.notifyDataSetChanged();
            }
        }));

        rlSelectTopicLevel.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_select_topic_level:
                showTopicLevelList();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void showTopicLevelList() {
        if (mRvLevelTopic.getVisibility() == View.VISIBLE) {
            mRvLevelTopic.setVisibility(View.GONE);
        } else {
            mRvLevelTopic.setVisibility(View.VISIBLE);
        }
    }

    private void refreshTopicList() {
        mTopics.clear();

        if (!TextUtils.isEmpty(mSelectedLevel) && !mSelectedLevel.equals("All")) {
            mTopics = curriculumAdapter.getTopicByLevel(mSelectedLevel);
        } else {
            mTopics = curriculumAdapter.getTopicList();
        }

        mTopicListRVAdapter.notifyDataSetChanged();
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

    private class TopicListRVAdapter extends RecyclerView.Adapter<TopicListRVAdapter.TopicListHolder> {
        @Override
        public TopicListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_topic, parent, false);
            return new TopicListHolder(v);
        }

        @Override
        public void onBindViewHolder(final TopicListHolder holder, int position) {
            final Topic topic = mTopics.get(position);

            holder.tvTopicTitle.setText(changeLocale(topic.getTitle()));

            if (topic.getmClassCount().equals("0") || topic.getmClassCount().equals("1")) {
                holder.tvTopicCount.setText(getString(R.string.to_class_count, topic.getmClassCount()));
            } else {
                holder.tvTopicCount.setText(getString(R.string.to_class_counts, topic.getmClassCount()));
            }


            if (!TextUtils.isEmpty(topic.getPhotoUrl())) {
                holder.sdvTopicCoverPhoto.setImageURI(Uri.parse(topic.getPhotoUrl()));
            }

            if (!TextUtils.isEmpty(topic.getmTopicIcon())) {
                holder.sdvTopicIcon.setImageURI(Uri.parse(topic.getmTopicIcon()));
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment topicFragment = new TopicFragment();

                    Bundle bundle = new Bundle();
                    bundle.putString(TopicFragment.EXTRA_TOPIC_ID, topic.getTopicId());
                    topicFragment.setArguments(bundle);

                    FragmentManager fm = getParentFragment().getFragmentManager();
                    Fragment fragment = fm.findFragmentById(R.id.container);
                    if (fragment == null) {
                        fm.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                .addToBackStack(null)
                                .add(R.id.container, topicFragment).commit();
                    } else {
                        fm.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                .addToBackStack(null)
                                .replace(R.id.container, topicFragment).commit();
                    }
                }
            });
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

        @Override
        public int getItemCount() {
            return mTopics.size();
        }

        class TopicListHolder extends RecyclerView.ViewHolder {
            TextView tvTopicTitle;
            TextView tvTopicCount;
            SimpleDraweeView sdvTopicCoverPhoto;
            SimpleDraweeView sdvTopicIcon;

            TopicListHolder(View itemView) {
                super(itemView);
                tvTopicTitle = itemView.findViewById(R.id.tv_topic_title);
                tvTopicCount = itemView.findViewById(R.id.tv_topic_count);
                sdvTopicCoverPhoto = itemView.findViewById(R.id.sdv_topic_cover_photo);
                sdvTopicIcon = itemView.findViewById(R.id.sdv_topic_icon);
            }
        }
    }

    private class LevelsRVAdapter extends RecyclerView.Adapter<LevelsRVAdapter.LevelCategoryTopicHolder> {

        @Override
        public LevelCategoryTopicHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_level_topic, parent, false);
            return new LevelCategoryTopicHolder(v);
        }

        @Override
        public void onBindViewHolder(LevelCategoryTopicHolder holder, int position) {
            String selectedTopic = mTopicLevel.get(position);

            if (selectedTopic.equals(mSelectedLevel)) {
                holder.imvLevelSelectedMark.setVisibility(View.VISIBLE);
            } else if ("All".equals(mSelectedLevel) &&
                    "All".equals(selectedTopic)) {
                holder.imvLevelSelectedMark.setVisibility(View.VISIBLE);
            } else {
                holder.imvLevelSelectedMark.setVisibility(View.INVISIBLE);
            }
            holder.tvLevelSelectTopic.setText(changeTopicLevelLocale(selectedTopic));

        }

        @Override
        public int getItemCount() {
            return mTopicLevel.size();
        }

        class LevelCategoryTopicHolder extends RecyclerView.ViewHolder {
            ImageView imvLevelSelectedMark;
            TextView tvLevelSelectTopic;

            LevelCategoryTopicHolder(View itemView) {
                super(itemView);
                imvLevelSelectedMark = itemView.findViewById(R.id.imv_level_selected_mark);
                tvLevelSelectTopic = itemView.findViewById(R.id.tv_topic_select_level);
            }
        }
    }
}
