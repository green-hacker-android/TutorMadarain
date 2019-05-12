package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Video;
import inc.osbay.android.tutormandarin.ui.activity.VideoInfoActivity;
import inc.osbay.android.tutormandarin.ui.view.RecyclerItemClickListener;
import inc.osbay.android.tutormandarin.util.CommonUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoListFragment extends BackHandledFragment implements View.OnClickListener {
//    private static final String TAG = VideoListFragment.class.getSimpleName();

    private EditText mEdtVideoSearch;
    private TextView mTvSelectVideoTopic;
    private RecyclerView mRvVideoSelectTopic;
    private RelativeLayout mRlVideoSearchBar;
    private ServerRequestManager mServerRequestManager;

    private VideoListAdapter mVideoListAdapter;
    private VideoSelectTopicAdapter mVideoSelectTopicAdapter;

    private List<String> mVideosTopics;
    private List<Video> mVideos;

    private CurriculumAdapter mCurriculumAdapter;

    private String mLocaleSelectedTxt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVideos = new ArrayList<>();
        mVideosTopics = new ArrayList<>();

        mCurriculumAdapter = new CurriculumAdapter(getActivity());
        mVideos = mCurriculumAdapter.getVideoList();
        mVideosTopics = mCurriculumAdapter.getVideoTopics();

        mServerRequestManager = new ServerRequestManager(getActivity().getApplicationContext());
        mVideoListAdapter = new VideoListAdapter();
        mVideoSelectTopicAdapter = new VideoSelectTopicAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_video_list, container, false);

        Toolbar toolBar;
        toolBar = v.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#D3825A"));
        setSupportActionBar(toolBar);

        mLocaleSelectedTxt = "SELECT TOPICS";

        RecyclerView rvVideoList = v.findViewById(R.id.rv_video_list);
        mRvVideoSelectTopic = v.findViewById(R.id.rv_video_select_topic);
        mEdtVideoSearch = v.findViewById(R.id.edt_search_text);
        mTvSelectVideoTopic = v.findViewById(R.id.tv_select_topic);
        mRlVideoSearchBar = v.findViewById(R.id.rl_search_bar);
        RelativeLayout rlSelectVideoTopic = v.findViewById(R.id.rl_select_video_topic);
        rlSelectVideoTopic.setOnClickListener(this);

        ImageView searchCancelImageView = v.findViewById(R.id.imv_search_cancel);
        searchCancelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRlVideoSearchBar.setVisibility(View.INVISIBLE);
                mEdtVideoSearch.setEnabled(false);
                showActionBar();

                if (getActivity() != null)
                    CommonUtil.hideKeyBoard(getActivity(), view);

                refreshVideoList();
            }
        });

        mEdtVideoSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String title = mEdtVideoSearch.getText().toString();
                if (!TextUtils.isEmpty(title)) {
                    mVideos = mCurriculumAdapter.searchVideosByTitle(title);
                    mTvSelectVideoTopic.setText(getString(R.string.vi_select_topic));
                } else {
                    refreshVideoList();
                }
                mVideoSelectTopicAdapter.notifyDataSetChanged();
                mVideoListAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        RecyclerView.LayoutManager llVideoManager = new LinearLayoutManager(getActivity());
        rvVideoList.setLayoutManager(llVideoManager);
        rvVideoList.setAdapter(mVideoListAdapter);

        LinearLayoutManager videoTopicLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRvVideoSelectTopic.setAdapter(mVideoSelectTopicAdapter);
        mRvVideoSelectTopic.setLayoutManager(videoTopicLayoutManager);
        mRvVideoSelectTopic.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position == -1)
                    return;

                String selectedTopic = mVideosTopics.get(position);
                mVideos.clear();
                if ("All".equals(selectedTopic)) {
                    mLocaleSelectedTxt = "SELECT TOPICS";
                    mTvSelectVideoTopic.setText(getString(R.string.vi_select_topic));
                } else {
                    String videoTopicsLocale;

                    switch (selectedTopic) {
                        case "Understanding China":
                            mLocaleSelectedTxt = "Understanding China";
                            videoTopicsLocale = getString(R.string.vi_understanding_china);
                            break;
                        case "Language Learning":
                            mLocaleSelectedTxt = "Language Learning";
                            videoTopicsLocale = getString(R.string.vi_language_learning);
                            break;
                        case "Music":
                            mLocaleSelectedTxt = "Music";
                            videoTopicsLocale = getString(R.string.vi_music);
                            break;
                        case "Funny":
                            mLocaleSelectedTxt = "Funny";
                            videoTopicsLocale = getString(R.string.vi_funny);
                            break;
                        case "News":
                            mLocaleSelectedTxt = "News";
                            videoTopicsLocale = getString(R.string.vi_news);
                            break;
                        default:
                            mLocaleSelectedTxt = "SELECT TOPICS";
                            videoTopicsLocale = getString(R.string.vi_select_topic);
                            break;
                    }
                    mTvSelectVideoTopic.setText(videoTopicsLocale);
                }
                showSelectTopicList();
                refreshVideoList();
                mVideoSelectTopicAdapter.notifyDataSetChanged();
            }
        }));

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        setTitle(getString(R.string.vi_title));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);

        FlurryAgent.logEvent("Video List");
    }

    private void showSelectTopicList() {
        if (mRvVideoSelectTopic.getVisibility() == View.VISIBLE) {
            mRvVideoSelectTopic.setVisibility(View.GONE);
        } else {
            mRvVideoSelectTopic.setVisibility(View.VISIBLE);
        }
    }

    private void refreshVideoList() {
//        String selectedTopic = mTvSelectVideoTopic.getText().toString();
        String selectedTopic = mLocaleSelectedTxt;

        mVideos.clear();
        if ("SELECT TOPICS".equals(selectedTopic)) {
            mVideos = mCurriculumAdapter.getVideoList();
        } else {
            mVideos = mCurriculumAdapter.getVideosByTopic(selectedTopic);
        }
        mVideoListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        setDisplayHomeAsUpEnable(true);
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.vi_dialog_loading));
        if (mVideos.size() == 0) {
            dialog.show();
        }

        mServerRequestManager.downloadVideoList(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(@Nullable Object result) {
                dialog.dismiss();
                mVideos = mCurriculumAdapter.getVideoList();
                mVideoListAdapter.notifyDataSetChanged();

                mVideosTopics = mCurriculumAdapter.getVideoTopics();
                mVideoSelectTopicAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(ServerError err) {
                if (getActivity() != null) {
                    dialog.dismiss();
                    Toast.makeText(getActivity(), getString(R.string.vi_connection_error), Toast.LENGTH_SHORT).show();
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
            case R.id.rl_select_video_topic:
                showSelectTopicList();
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_video_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.opt_search:
                FlurryAgent.logEvent("Search video");

                mRlVideoSearchBar.setVisibility(View.VISIBLE);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                mEdtVideoSearch.setText("");
                mEdtVideoSearch.setEnabled(true);
                mEdtVideoSearch.requestFocus();
                imm.toggleSoftInputFromWindow(mEdtVideoSearch.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                hideActionBar();
                mRvVideoSelectTopic.setVisibility(View.GONE);
                return true;
        }
        return false;
    }

    private class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoHolder> {

        @Override
        public VideoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_video, parent, false);
            return new VideoHolder(v);
        }

        @Override
        public void onBindViewHolder(VideoHolder holder, int position) {
            final Video video = mVideos.get(position);

            holder.tvVideoTitle.setText(mVideos.get(position).getTitle());
            holder.tvVideoLength.setText(mVideos.get(position).getLength());

            DecimalFormat formatter = new DecimalFormat("##,###,###");
            holder.tvVideoViewCount.setText(getString(R.string.vi_viewed_count, formatter.format(video.getViewCount())));

            holder.tvVideoTopic.setText(mVideos.get(position).getTopicName());

            holder.sdvThumbnail.setImageURI(Uri.parse("http://i.ytimg.com/vi/" + video.getYoutubeId() + "/mqdefault.jpg"));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Map<String, String> params = new HashMap<>();
                    params.put("Topic", video.getTopicName());
                    FlurryAgent.logEvent("Choose video", params);

                    if (getActivity() != null)
                        CommonUtil.hideKeyBoard(getActivity(), view);

                    Intent intent = new Intent(getActivity(), VideoInfoActivity.class);
                    intent.putExtra(VideoInfoActivity.EXTRA_VIDEO_ID, video.getVideoId());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mVideos.size();
        }

        class VideoHolder extends RecyclerView.ViewHolder {
            TextView tvVideoTitle;
            TextView tvVideoLength;
            TextView tvVideoViewCount;
            TextView tvVideoTopic;
            SimpleDraweeView sdvThumbnail;

            VideoHolder(View itemView) {
                super(itemView);
                tvVideoTitle = itemView.findViewById(R.id.tv_video_title);
                tvVideoLength = itemView.findViewById(R.id.tv_video_length);
                tvVideoViewCount = itemView.findViewById(R.id.tv_video_view_count);
                tvVideoTopic = itemView.findViewById(R.id.tv_video_topic);
                sdvThumbnail = itemView.findViewById(R.id.sdv_video_thumbnail);
            }
        }
    }

    private class VideoSelectTopicAdapter extends RecyclerView.Adapter<VideoSelectTopicAdapter.VideoSelectTopicHolder> {

        @Override
        public VideoSelectTopicHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_video_topic, parent,
                    false);
            return new VideoSelectTopicHolder(v);
        }

        @Override
        public void onBindViewHolder(VideoSelectTopicHolder holder, int position) {

            String videoTopic = mVideosTopics.get(position);

            String videoTopicsLocale;

            switch (videoTopic) {
                case "Understanding China":
                    videoTopicsLocale = getString(R.string.vi_understanding_china);
                    break;
                case "Language Learning":
                    videoTopicsLocale = getString(R.string.vi_language_learning);
                    break;
                case "Music":
                    videoTopicsLocale = getString(R.string.vi_music);
                    break;
                case "Funny":
                    videoTopicsLocale = getString(R.string.vi_funny);
                    break;
                case "News":
                    videoTopicsLocale = getString(R.string.vi_news);
                    break;
                default:
                    videoTopicsLocale = videoTopic;
                    break;
            }

            if(videoTopicsLocale.equals("All"))
                videoTopicsLocale = getString(R.string.vi_list_all);

            holder.tvVideoSelectTopics.setText(videoTopicsLocale);
            if (videoTopicsLocale.equals(mTvSelectVideoTopic.getText().toString())) {
                holder.imvVideoSelectedMark.setVisibility(View.VISIBLE);
            } else if (getString(R.string.vi_select_topic).equals(mTvSelectVideoTopic.getText().toString()) &&
                    "All".equals(videoTopic)) {
                holder.imvVideoSelectedMark.setVisibility(View.VISIBLE);
            } else {
                holder.imvVideoSelectedMark.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return mVideosTopics.size();
        }

        class VideoSelectTopicHolder extends RecyclerView.ViewHolder {
            TextView tvVideoSelectTopics;
            ImageView imvVideoSelectedMark;

            VideoSelectTopicHolder(View itemView) {
                super(itemView);
                tvVideoSelectTopics = itemView.findViewById(R.id.tv_video_select_topic);
                imvVideoSelectedMark = itemView.findViewById(R.id.imv_video_selected_mark);
            }
        }
    }
}
