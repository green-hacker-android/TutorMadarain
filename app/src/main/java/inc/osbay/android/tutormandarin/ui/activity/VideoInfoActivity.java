package inc.osbay.android.tutormandarin.ui.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.model.FlashCardDeck;
import inc.osbay.android.tutormandarin.sdk.model.Video;
import inc.osbay.android.tutormandarin.sdk.model.VideoVocab;
import inc.osbay.android.tutormandarin.ui.fragment.FlashCardPagerFragment;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Sample activity showing how to properly enable custom fullscreen behavior.
 * <p/>
 * This is the preferred way of handling fullscreen because the default fullscreen implementation
 * will cause re-buffering of the video.
 */
public class VideoInfoActivity extends YouTubeFailureRecoveryActivity implements
        YouTubePlayer.OnFullscreenListener {

    public static final String EXTRA_VIDEO_ID = "VideoInfoActivity.EXTRA_VIDEO_ID";
    private LinearLayout baseLayout;
    private YouTubePlayerView playerView;
    private YouTubePlayer player;
    private View otherViews;
    private boolean fullscreen;
    private Video mVideo;
    private VideoVocabAdapter mVideoVocabAdapter;
    private List<VideoVocab> mVideoVocabs;
    private ServerRequestManager mServerRequestManager;
    private CurriculumAdapter mCurriculumAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_info);

        mServerRequestManager = new ServerRequestManager(this);

        Intent intent = getIntent();
        if (intent != null) {
            String videoId = intent.getStringExtra(EXTRA_VIDEO_ID);

            mCurriculumAdapter = new CurriculumAdapter(this);
            mVideo = mCurriculumAdapter.getVideoById(videoId);
            mVideoVocabs = mCurriculumAdapter.retrieveVideoVocab(videoId);
        }

        if (mVideo == null) {
            this.finish();
            return;
        }

        TextView title = (TextView) findViewById(R.id.tv_title_text);
        title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Montserrat-Bold.ttf"));
        title.setText(mVideo.getTitle());

        mVideoVocabAdapter = new VideoVocabAdapter();

        baseLayout = (LinearLayout) findViewById(R.id.layout);
        playerView = (YouTubePlayerView) findViewById(R.id.player);
        otherViews = findViewById(R.id.other_views);

        playerView.initialize(CommonConstant.GOOGLE_API_KEY, this);

        doLayout();

        TextView tvVideoTitle = (TextView) findViewById(R.id.tv_video_title);
        tvVideoTitle.setText(mVideo.getTitle());

        TextView tvAuthor = (TextView) findViewById(R.id.tv_author);
        tvAuthor.setText(mVideo.getAuthor());

        TextView tvViewCount = (TextView) findViewById(R.id.tv_video_view_count);
        DecimalFormat formatter = new DecimalFormat("##,###,###");
        tvViewCount.setText(getString(R.string.vi_viewed_count, formatter.format(mVideo.getViewCount())));

        Log.e("Video Description", mVideo.getDescription());

        TabLayout mTlVideoTabs = (TabLayout) findViewById(R.id.tl_video_tabs);

        ViewPager mVideoTabPager = (ViewPager) findViewById(R.id.vp_video_tabs);
        mVideoTabPager.setAdapter(new VideoTabPagerAdapter());
        mTlVideoTabs.setupWithViewPager(mVideoTabPager);

        ImageView imvBack = (ImageView) findViewById(R.id.imv_back);
        imvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        mServerRequestManager.downloadVideoVocabList(mVideo.getVideoId(), new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(@Nullable Object result) {
                mVideoVocabs = mCurriculumAdapter.retrieveVideoVocab(mVideo.getVideoId());
                mVideoVocabAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(ServerError err) {
                if (VideoInfoActivity.this != null) {
                    Toast.makeText(VideoInfoActivity.this, err.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        this.player = player;
//        setControlsEnabled();
        // Specify that we want to handle fullscreen behavior ourselves.
        player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE);
        player.setOnFullscreenListener(this);
        if (!wasRestored) {
            player.cueVideo(mVideo.getYoutubeId());
        } else {
            player.play();
        }
    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return playerView;
    }

    @Override
    public void onBackPressed() {
        if (fullscreen) {
            player.setFullscreen(!fullscreen);
        } else {
            super.onBackPressed();
        }
    }

    private void doLayout() {
        LayoutParams playerParams =
                (LayoutParams) playerView.getLayoutParams();
        if (fullscreen) {
            // When in fullscreen, the visibility of all other views than the player should be set to
            // GONE and the player should be laid out across the whole screen.
            playerParams.width = LayoutParams.MATCH_PARENT;
            playerParams.height = LayoutParams.MATCH_PARENT;

            otherViews.setVisibility(View.GONE);
        } else {
            // This layout is up to you - this is just a simple example (vertically stacked boxes in
            // portrait, horizontally stacked in landscape).
            otherViews.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams otherViewsParams = otherViews.getLayoutParams();
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                playerParams.width = otherViewsParams.width = 0;
                playerParams.height = WRAP_CONTENT;
                otherViewsParams.height = MATCH_PARENT;
                playerParams.weight = 1;
                baseLayout.setOrientation(LinearLayout.HORIZONTAL);
            } else {
                playerParams.width = otherViewsParams.width = MATCH_PARENT;
                playerParams.height = WRAP_CONTENT;
                playerParams.weight = 0;
                otherViewsParams.height = 0;
                baseLayout.setOrientation(LinearLayout.VERTICAL);
            }
//            setControlsEnabled();
        }
    }

//    private void setControlsEnabled() {
//        checkbox.setEnabled(player != null
//                && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
//        fullscreenButton.setEnabled(player != null);
//    }

    @Override
    public void onFullscreen(boolean isFullscreen) {
        fullscreen = isFullscreen;
        doLayout();

        if (isFullscreen) {
            FlurryAgent.logEvent("Change video to full screen");
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        doLayout();
    }

    private class VideoTabPagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            View layout = null;
            if (position == 0) {
                NestedScrollView nestedScrollView = new NestedScrollView(VideoInfoActivity.this);
                nestedScrollView.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));

                TextView tvDescription = new TextView(VideoInfoActivity.this);
                tvDescription.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));
                tvDescription.setTextColor(ContextCompat.getColor(VideoInfoActivity.this, R.color.text_color_gray));
                tvDescription.setText(mVideo.getDescription().replace("\\n", "\n"));
                tvDescription.setMovementMethod(new ScrollingMovementMethod());

                nestedScrollView.addView(tvDescription);
                layout = nestedScrollView;
            } else if (position == 1) {
                RecyclerView recyclerView = new RecyclerView(VideoInfoActivity.this);

                LinearLayoutManager llVideoLayout = new LinearLayoutManager(VideoInfoActivity.this,
                        LinearLayoutManager.VERTICAL, false);
                recyclerView.setLayoutManager(llVideoLayout);
                recyclerView.setAdapter(mVideoVocabAdapter);

                layout = recyclerView;
            }
            collection.addView(layout);

            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.vi_description);
            } else if (position == 1) {
                return getString(R.string.vi_vocab);
            }
            return super.getPageTitle(position);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

    private class VideoVocabAdapter extends RecyclerView.Adapter<VideoVocabAdapter.VideoInfoHolder> {

        @Override
        public VideoInfoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_video_vocab,
                    parent, false);
            return new VideoInfoHolder(v);
        }

        @Override
        public void onBindViewHolder(final VideoInfoHolder holder, int position) {
            final VideoVocab videoVocab = mVideoVocabs.get(position);

            holder.tvCharacter.setText(videoVocab.getChatacter());

            String definition = String.format(Locale.getDefault(), "%s / %s / %s",
                    videoVocab.getPartOfSpeech(), videoVocab.getPinyin(), videoVocab.getDefinition());
            holder.tvVideoVocab.setText(definition);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FlashCardDeck deck = new FlashCardDeck();
                    deck.setCardTitle(mVideo.getTitle());
                    deck.setType(FlashCardDeck.Type.VIDEO_VOCAB);
                    deck.setCourseId(mVideo.getVideoId());

                    Intent intent = new Intent(VideoInfoActivity.this, FragmentHolderActivity.class);
                    intent.putExtra(FragmentHolderActivity.EXTRA_DISPLAY_FRAGMENT,
                            FlashCardPagerFragment.class.getSimpleName());
                    intent.putExtra(FlashCardPagerFragment.EXTRA_FLASHCARD_DECK, deck);
                    intent.putExtra(FlashCardPagerFragment.EXTRA_CARD_POSITION, holder.getAdapterPosition());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mVideoVocabs.size();
        }

        class VideoInfoHolder extends RecyclerView.ViewHolder {

            TextView tvVideoVocab;
            TextView tvCharacter;

            public VideoInfoHolder(View itemView) {
                super(itemView);

                tvCharacter = (TextView) itemView.findViewById(R.id.tv_vocab_character);
                tvVideoVocab = (TextView) itemView.findViewById(R.id.tv_vocab_definition);
            }
        }
    }
}
