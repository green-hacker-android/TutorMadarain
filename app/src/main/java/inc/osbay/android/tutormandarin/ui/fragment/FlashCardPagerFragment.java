package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.model.FlashCard;
import inc.osbay.android.tutormandarin.sdk.model.FlashCardDeck;
import inc.osbay.android.tutormandarin.sdk.model.Grammar;
import inc.osbay.android.tutormandarin.sdk.model.Vocab;
import inc.osbay.android.tutormandarin.ui.activity.AddToCustomFCDeckActivity;
import inc.osbay.android.tutormandarin.ui.activity.ShareActivity;
import inc.osbay.android.tutormandarin.ui.view.ListDialog;
import inc.osbay.android.tutormandarin.ui.view.PagerContainer;

public class FlashCardPagerFragment extends BackHandledFragment {

    public static final String EXTRA_FLASHCARD_DECK = "FlashCardPagerFragment.EXTRA_FLASHCARD_DECK";
    public static final String EXTRA_CARD_POSITION = "FlashCardPagerFragment.EXTRA_CARD_POSITION";

    private List<FlashCard> mFlashCardList;
    private ViewPager mViewPager;
    private FlashCardPagerAdapter mPagerAdapter;

    private CurriculumAdapter curriculumAdapter;
    private FlashCardDeck mFlashCardDeck;

    private TextView mCurrentTextView;
    private TextView mTotalTextView;

    private MediaPlayer mMediaPlayer;
    private int mDefaultPosition;

    public FlashCardPagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.reset();
            }
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            mFlashCardDeck = (FlashCardDeck) bundle.getSerializable(EXTRA_FLASHCARD_DECK);
            mDefaultPosition = bundle.getInt(EXTRA_CARD_POSITION);
        }

        if (mFlashCardDeck == null) {
            onBackPressed();
            return;
        }

        setTitle(mFlashCardDeck.getCardTitle());

        curriculumAdapter = new CurriculumAdapter(getActivity());

        switch (mFlashCardDeck.getType()) {
            case FlashCardDeck.Type.VIDEO_VOCAB:
                mFlashCardList = curriculumAdapter.getVideoFlashCards(mFlashCardDeck.getCourseId());
                break;
            case FlashCardDeck.Type.WHATS_ON_VOCAB:
                mFlashCardList = curriculumAdapter.getWhatsOnFlashCards(mFlashCardDeck.getCourseId());
                break;
            case FlashCardDeck.Type.CUSTOM:
                mFlashCardList = curriculumAdapter.getCustomFlashCardsByDeck(
                        String.valueOf(mFlashCardDeck.getDeckId()));
                break;
            default:
                mFlashCardList = curriculumAdapter.getCommonFlashCardsByDeck(
                        mFlashCardDeck.getCourseId(), mFlashCardDeck.getType());
                break;
        }

        mPagerAdapter = new FlashCardPagerAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_flashcard_pager, container, false);

        PagerContainer pagerContainer = (PagerContainer) rootView.findViewById(R.id.pager_container);

        mCurrentTextView = (TextView) rootView.findViewById(R.id.tv_current_no);
        mTotalTextView = (TextView) rootView.findViewById(R.id.tv_total_no);

        mViewPager = pagerContainer.getViewPager();
        mViewPager.setAdapter(mPagerAdapter);

        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.vp_page_margin));
        mViewPager.setClipChildren(false);

        mViewPager.setCurrentItem(mDefaultPosition);
        mCurrentTextView.setText(String.valueOf(mDefaultPosition + 1));

        mTotalTextView.setText(String.valueOf(mFlashCardList.size()));

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentTextView.setText(String.valueOf(position + 1));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        setDisplayHomeAsUpEnable(true);

        if (mFlashCardDeck.getType() == FlashCardDeck.Type.CUSTOM ||
                mFlashCardDeck.getType() == FlashCardDeck.Type.VIDEO_VOCAB ||
                mFlashCardDeck.getType() == FlashCardDeck.Type.WHATS_ON_VOCAB) {
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.fc_download_loading));
        progressDialog.setCancelable(false);

        if (mFlashCardList.size() == 0) {
            progressDialog.show();
        }

        ServerRequestManager requestManager = new ServerRequestManager(getActivity().getApplicationContext());
        requestManager.downloadFlashCardById(mFlashCardDeck.getCourseId(), mFlashCardDeck.getType(),
                new ServerRequestManager.OnRequestFinishedListener() {
                    @Override
                    public void onSuccess(Object result) {
                        if (getActivity() != null) {
                            progressDialog.dismiss();

                            mFlashCardList = curriculumAdapter.getCommonFlashCardsByDeck(
                                    mFlashCardDeck.getCourseId(), mFlashCardDeck.getType());
                            mPagerAdapter.notifyDataSetChanged();
                            mViewPager.setOffscreenPageLimit(3);

                            mTotalTextView.setText(String.valueOf(mFlashCardList.size()));
                        }
                    }

                    @Override
                    public void onError(ServerError err) {
                        if (getActivity() != null) {
                            progressDialog.dismiss();
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getString(R.string.fc_download_error))
                                    .setMessage(err.getMessage())
                                    .setPositiveButton(getString(R.string.fc_download_ok), null)
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialogInterface) {
                                            getActivity().finish();
                                        }
                                    })
                                    .create()
                                    .show();
                        }
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();

        mMediaPlayer.release();
    }

    @Override
    public boolean onBackPressed() {
        Map<String, String> params = new HashMap<>();
        params.put("Title", mFlashCardDeck.getCardTitle());
        params.put("Viewed cards", (mViewPager.getCurrentItem() + 1) + "/" + mFlashCardDeck.getCardCount());
        if(mFlashCardDeck.getType() == FlashCardDeck.Type.CUSTOM) {
            params.put("Type", "Custom");
        }else{
            params.put("Type", "Common");
        }
        FlurryAgent.logEvent("View flashcard pack", params);

        getActivity().finish();
        return false;
    }

    private class FlashCardPagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            final FlashCard flashCard = mFlashCardList.get(position);

            if (flashCard.getType() != FlashCard.Type.GRAMMAR) {
                final Vocab vocab = (Vocab) flashCard;

                ViewGroup layout = (ViewGroup) LayoutInflater.from(getActivity())
                        .inflate(R.layout.item_vp_vocab, collection, false);

                TextView tvChinese = (TextView) layout.findViewById(R.id.tv_chinese);
                tvChinese.setText(vocab.getChinese());

                TextView tvPinyin = (TextView) layout.findViewById(R.id.tv_pinyin);
                tvPinyin.setText(String.valueOf(vocab.getPinyin()));

                TextView tvPartOfSpeech = (TextView) layout.findViewById(R.id.tv_part_of_speech);
                tvPartOfSpeech.setText(vocab.getPartOfSpeech());

                TextView tvDefinition = (TextView) layout.findViewById(R.id.tv_definition);
                tvDefinition.setText(Html.fromHtml(vocab.getDefinition()));

                TextView tvExample = (TextView) layout.findViewById(R.id.tv_example);
                tvExample.setText(Html.fromHtml(vocab.getExample()));

                SimpleDraweeView sdvPhoto = (SimpleDraweeView) layout.findViewById(R.id.sdv_vocab_photo);
                if (!TextUtils.isEmpty(vocab.getPhoto())) {
                    sdvPhoto.setImageURI(Uri.parse(vocab.getPhoto()));
                }

                ImageView imvMore = (ImageView) layout.findViewById(R.id.imv_opt_more);
                imvMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String[] menuItems = {getString(R.string.fc_add_to_custom_list), getString(R.string.fc_share), getString(R.string.fc_cancel)};
                        int[] menuIcons = {R.drawable.ic_menu_add_to_custom,
                                R.drawable.ic_menu_share, R.drawable.ic_menu_cancel};
                        ListDialog listDialog = new ListDialog(getActivity(), menuItems, menuIcons, onItemClickListener(vocab));
                        listDialog.setGravity(Gravity.BOTTOM);
                        listDialog.show();
                    }
                });

                if(flashCard.getType() != FlashCard.Type.VOCAB){
                    imvMore.setVisibility(View.INVISIBLE);
                }else{
                    imvMore.setVisibility(View.VISIBLE);
                }

//                FloatingActionButton fabPlay = (FloatingActionButton) layout.findViewById(R.id.fab_vocab_play);
//                fabPlay.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                        if (!TextUtils.isEmpty(vocab.getVoice())) {
//
//                            String fileName = vocab.getVoice().substring(vocab.getVoice().lastIndexOf('/') + 1, vocab.getVoice().length());
//                            final File file = new File(CommonConstant.MEDIA_PATH, fileName);
//
//                            if (mMediaPlayer.isPlaying()) {
//                                mMediaPlayer.stop();
//                                mMediaPlayer.reset();
//                            }
//
//                            if (file.exists()) {
//
//                                try {
//                                    mMediaPlayer.setDataSource(file.getAbsolutePath());
//                                    mMediaPlayer.prepare();
//                                    mMediaPlayer.start();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//
//                            } else {
//
//                                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
//                                progressDialog.setMessage("Loading...");
//                                progressDialog.setCancelable(false);
//                                progressDialog.show();
//
//                                FileDownloader.downloadImage(vocab.getVoice(), new FileDownloader.OnDownloadFinishedListener() {
//                                    @Override
//                                    public void onSuccess() {
//                                        progressDialog.dismiss();
//                                        try {
//                                            mMediaPlayer.setDataSource(file.getAbsolutePath());
//                                            mMediaPlayer.prepare();
//                                            mMediaPlayer.start();
//                                        } catch (IOException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onError() {
//                                        progressDialog.dismiss();
//                                        Toast.makeText(getActivity(), "Can't download video file.", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                            }
//                        }
//
//                    }
//                });

                collection.addView(layout);
                return layout;
            } else {
                final Grammar grammar = (Grammar) flashCard;

                ViewGroup layout = (ViewGroup) LayoutInflater.from(getActivity())
                        .inflate(R.layout.item_vp_grammar, collection, false);

                TextView tvChinese = (TextView) layout.findViewById(R.id.tv_grammar);
                tvChinese.setText(grammar.getChinese());

                TextView tvPinyin = (TextView) layout.findViewById(R.id.tv_pinyin);
                tvPinyin.setText(String.valueOf(grammar.getEnglish()));

                TextView tvPartOfSpeech = (TextView) layout.findViewById(R.id.tv_part_of_speech);
                tvPartOfSpeech.setText(grammar.getPartOfSpeech());

                TextView tvDefinition = (TextView) layout.findViewById(R.id.tv_definition);
                tvDefinition.setText(grammar.getDescription());

                TextView tvExample = (TextView) layout.findViewById(R.id.tv_example);
                tvExample.setText(Html.fromHtml(grammar.getExample()));

                ImageView imvMore = (ImageView) layout.findViewById(R.id.imv_opt_more);
                imvMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String[] menuItems = {getString(R.string.fc_add_to_custom_list), getString(R.string.fc_share), getString(R.string.fc_cancel)};
                        int[] menuIcons = {R.drawable.ic_menu_add_to_custom, R.drawable.ic_menu_share,
                                R.drawable.ic_menu_cancel};
                        ListDialog listDialog = new ListDialog(getActivity(), menuItems, menuIcons, onItemClickListener(grammar));
                        listDialog.setGravity(Gravity.BOTTOM);
                        listDialog.show();
                    }
                });

                collection.addView(layout);
                return layout;
            }
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object obj) {
            View view = (View) obj;
            collection.removeView(view);
        }

        @Override
        public int getCount() {
            return mFlashCardList.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(R.string.fc_package_title);
        }

        private AdapterView.OnItemClickListener onItemClickListener(final FlashCard flashCard) {
            return new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    switch (i) {
                        case 0:
                            Intent intent = new Intent(getActivity(), AddToCustomFCDeckActivity.class);
                            intent.putExtra(AddToCustomFCDeckActivity.EXTRA_FLASHCARD_ID, flashCard.getFlashCardId());
                            startActivity(intent);
                            break;
                        case 1:
                            if (flashCard.getType() == 1) {
                                Vocab vocab = (Vocab) flashCard;
                                String text = ShareActivity.getShareFlashCardTemplate(flashCard.getChinese());

                                Intent shareIntent = new Intent(getActivity(), ShareActivity.class);
                                shareIntent.putExtra(ShareActivity.EXTRA_SHARE_ITEM, "Flashcard");
                                shareIntent.putExtra(ShareActivity.EXTRA_TITLE, vocab.getChinese());
                                shareIntent.putExtra(ShareActivity.EXTRA_CONTENT, text);
                                shareIntent.putExtra(ShareActivity.EXTRA_IMAGE_URL, vocab.getPhoto());

                                startActivity(shareIntent);
                            }
                            break;
                    }
                }
            };
        }
    }
}
