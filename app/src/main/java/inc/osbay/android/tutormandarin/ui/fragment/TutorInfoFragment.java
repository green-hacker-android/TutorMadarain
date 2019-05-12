package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.database.TutorAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.sdk.model.Booking;
import inc.osbay.android.tutormandarin.sdk.model.Tutor;
import inc.osbay.android.tutormandarin.sdk.util.FileDownloader;
import inc.osbay.android.tutormandarin.ui.activity.ShareActivity;
import inc.osbay.android.tutormandarin.ui.activity.TrialSubmitActivity;
import inc.osbay.android.tutormandarin.ui.activity.TutorInfoActivity;
import inc.osbay.android.tutormandarin.ui.view.ListDialog;
import jp.wasabeef.fresco.processors.BlurPostprocessor;

public class TutorInfoFragment extends BackHandledFragment implements View.OnClickListener {
    public static final String EXTRA_TUTOR_ID = "TutorInfoFragment.EXTRA_TUTOR_ID";
    public static final String EXTRA_TUTOR_TIME = "TutorInfoFragment.EXTRA_TUTOR_TIME";
    public static final String EXTRA_BOOKING_TYPE = "TutorInfoFragment.EXTRA_BOOKING_TYPE";

    private MediaPlayer mMediaPlayer;
    private ProgressBar mMusicProgressBar;
    private Timer mTimer;

    private ImageView imvIntroVoice;

    private boolean isPlaying;
    private String mTutorTime;
    private int mBookingType;

    private Account mAccount;
    private Tutor mTutor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TutorAdapter adapter = new TutorAdapter(getActivity());

        Bundle bundle = getArguments();
        if (bundle != null) {
            mTutor = adapter.getTutorById(bundle.getString(EXTRA_TUTOR_ID));
            mTutorTime = bundle.getString(EXTRA_TUTOR_TIME);
            mBookingType = bundle.getInt(EXTRA_BOOKING_TYPE);
        }

        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String token = mPreferences.getString("access_token", null);
        String accountId = mPreferences.getString("account_id", null);

        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
            AccountAdapter accountAdapter = new AccountAdapter(getActivity());
            mAccount = accountAdapter.getAccountById(accountId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tutor_info, container, false);

        Toolbar toolBar;
        toolBar = rootView.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        setSupportActionBar(toolBar);

        mMusicProgressBar = rootView.findViewById(R.id.pb_intro_voice);

        TextView tvTutorName = rootView.findViewById(R.id.tv_course_title);
        tvTutorName.setText(mTutor.getName());

        RatingBar rbRate = rootView.findViewById(R.id.rb_tutor_rate);
        rbRate.setRating(mTutor.getRate());

        TextView tvRate = rootView.findViewById(R.id.tv_tutor_rate);
        tvRate.setText(String.valueOf(mTutor.getRate()));

        TextView tvIntroText = rootView.findViewById(R.id.tv_intro_text);
        tvIntroText.setText(mTutor.getIntroText());

        TextView tvCreditWeight = rootView.findViewById(R.id.tv_credit_weight);
        tvCreditWeight.setText(String.format(Locale.getDefault(), "%.1f", mTutor.getCreditWeight()));

        TextView tvTopics = rootView.findViewById(R.id.tv_special_topics);
        tvTopics.setText(getString(R.string.tu_special_topic, mTutor.getTopics()));

        TextView tvExp = rootView.findViewById(R.id.tv_teaching_exp);
        tvExp.setText(getString(R.string.tu_exp, mTutor.getTeachingExp()));

        SimpleDraweeView sdvProfileBlurPhoto = rootView.findViewById(R.id.sdv_tutor_photo_blur);

        SimpleDraweeView sdvTutorPhoto = rootView.findViewById(R.id.sdv_tutor_photo);
        if (mTutor.getAvatar() != null) {
            sdvTutorPhoto.setImageURI(Uri.parse(mTutor.getAvatar()));

            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(mTutor.getAvatar()))
                    .setPostprocessor(new BlurPostprocessor(getActivity()))
                    .build();
            PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                    .setOldController(sdvProfileBlurPhoto.getController())
                    .setImageRequest(request)
                    .build();
            sdvProfileBlurPhoto.setController(controller);
        }

        imvIntroVoice = rootView.findViewById(R.id.imv_intro_voice);
        imvIntroVoice.setOnClickListener(this);

        RelativeLayout rlBookTutor = rootView.findViewById(R.id.rl_book_tutor);
        rlBookTutor.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        showActionBar();
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        setDisplayHomeAsUpEnable(true);

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMusicProgressBar.setMax(mMediaPlayer.getDuration() / 1000);
                    mMusicProgressBar.setProgress(mMediaPlayer.getCurrentPosition() / 1000);
                }
            }
        }, 1000, 1000);

        if (mMediaPlayer != null && isPlaying) {
            mMediaPlayer.start();
            isPlaying = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mTimer.cancel();

        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            isPlaying = true;
        }
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
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_lesson, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.opt_more:
                String[] menuItems = {getString(R.string.tu_add_to_favorite), getString(R.string.tu_share)};
                int[] menuIcons = {R.drawable.ic_add_to_favorite, R.drawable.ic_menu_share};

                ListDialog customMenu = new ListDialog(getActivity(), menuItems, menuIcons,
                        new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                switch (i) {
                                    case 0:
                                        if (mAccount != null) {
                                            CurriculumAdapter curriculumAdapter = new CurriculumAdapter(getActivity());

                                            if (curriculumAdapter.checkFavourite(mTutor.getTutorId(), mAccount.getAccountId())) {
                                                Toast.makeText(getActivity(), getString(R.string.tu_already_added), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Map<String, String> favourite = new HashMap<>();
                                                favourite.put("lesson_tutor_id", mTutor.getTutorId());
                                                favourite.put("fav_lesson_user", mAccount.getAccountId());
                                                favourite.put("type", String.valueOf(FavouriteDrawerFragment.TUTOR_ID));
                                                curriculumAdapter.insertFavourite(favourite);
                                                Toast.makeText(getActivity(), getString(R.string.tu_successfully_added), Toast.LENGTH_SHORT).show();

                                                ServerRequestManager serverRequestManager = new ServerRequestManager(getActivity().getApplicationContext());
                                                serverRequestManager.addToFavorite(FavouriteDrawerFragment.TUTOR_ID, mTutor.getTutorId(), FavouriteDrawerFragment.FAVOURITE_ID);
                                            }
                                        }
                                        break;
                                    case 1:
                                        Intent shareIntent = new Intent(getActivity(), ShareActivity.class);
                                        shareIntent.putExtra(ShareActivity.EXTRA_SHARE_ITEM, "Tutor");
                                        shareIntent.putExtra(ShareActivity.EXTRA_TITLE, mTutor.getName());
                                        shareIntent.putExtra(ShareActivity.EXTRA_CONTENT,
                                                ShareActivity.getShareTutorTemplate(mTutor.getName()));
                                        shareIntent.putExtra(ShareActivity.EXTRA_IMAGE_URL, mTutor.getAvatar());

                                        startActivity(shareIntent);
                                        break;
                                }
                            }
                        });
                customMenu.setGravity(Gravity.TOP);
                customMenu.show();
                return true;
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        if (getActivity() instanceof TutorInfoActivity) {
            getActivity().finish();
        } else {
            getFragmentManager().popBackStack();
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_book_tutor:
                if (getActivity() instanceof TutorInfoActivity) {
                    Intent data = new Intent();
                    data.putExtra(TutorTimeSelectFragment.EXTRA_TUTOR_ID, mTutor.getTutorId());
                    data.putExtra(TutorTimeSelectFragment.EXTRA_TUTOR_TIME, mTutorTime);
                    data.putExtra(TutorTimeSelectFragment.EXTRA_BOOKING_TYPE, mBookingType);
                    getActivity().setResult(Activity.RESULT_OK, data);
                    getActivity().finish();
                } else {
                    if (mAccount != null) {
                        //if (mAccount.getStatus() == Account.Status.ACTIVE) {
                            final Dialog dialog = new Dialog(getActivity());
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.dialog_choose_booking_type);
                            TextView tvBookingLessons = dialog.findViewById(R.id.tv_booking_lessons);
                            TextView tvBookingTopics = dialog.findViewById(R.id.tv_booking_topics);

                            tvBookingLessons.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Fragment bookTutorFragment = new TutorTimeSelectFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putString(TutorTimeSelectFragment.EXTRA_TUTOR_ID, mTutor.getTutorId());
                                    bundle.putInt(TutorTimeSelectFragment.EXTRA_BOOKING_TYPE, Booking.Type.LESSON);

                                    bundle.putString(TutorTimeSelectFragment.EXTRA_TUTOR_TIME, mTutorTime);

                                    bookTutorFragment.setArguments(bundle);

                                    FragmentManager fm = getFragmentManager();
                                    Fragment fragment = fm.findFragmentById(R.id.container);
                                    if (fragment == null) {
                                        fm.beginTransaction()
                                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                                .addToBackStack(null)
                                                .add(R.id.container, bookTutorFragment).commit();
                                    } else {
                                        fm.beginTransaction()
                                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                                .addToBackStack(null)
                                                .replace(R.id.container, bookTutorFragment).commit();
                                    }
                                    dialog.dismiss();
                                }
                            });

                            tvBookingTopics.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Fragment bookTutorFragment = new TutorTimeSelectFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putString(TutorTimeSelectFragment.EXTRA_TUTOR_ID, mTutor.getTutorId());
                                    bundle.putInt(TutorTimeSelectFragment.EXTRA_BOOKING_TYPE, Booking.Type.TOPIC);
                                    bundle.putString(TutorTimeSelectFragment.EXTRA_TUTOR_TIME, mTutorTime);

                                    bookTutorFragment.setArguments(bundle);

                                    FragmentManager fm = getFragmentManager();
                                    Fragment fragment = fm.findFragmentById(R.id.container);
                                    if (fragment == null) {
                                        fm.beginTransaction()
                                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                                .addToBackStack(null)
                                                .add(R.id.container, bookTutorFragment).commit();
                                    } else {
                                        fm.beginTransaction()
                                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                                .addToBackStack(null)
                                                .replace(R.id.container, bookTutorFragment).commit();
                                    }
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                        /*} else {
                            final Dialog dialog = new Dialog(getActivity());
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.dialog_unavailable_user);
                            TextView tvUnavailableUserTxt = dialog.findViewById(R.id.tv_unavailable_user_txt);
                            TextView tvUnavailableUserOk = dialog.findViewById(R.id.tv_unavailable_user_ok);

                            tvUnavailableUserOk.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });

                            SpannableStringBuilder spanTxt = new SpannableStringBuilder(getString(R.string.tu_unavailable_user_msg));
                            spanTxt.append(getString(R.string.tu_sign_up_now));
                            spanTxt.setSpan(new ClickableSpan() {
                                @Override
                                public void onClick(View widget) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(getActivity(), TrialSubmitActivity.class);
                                    startActivity(intent);
                                }

                                @Override
                                public void updateDrawState(TextPaint ds) {
                                    ds.setColor(ContextCompat.getColor(getActivity(), R.color.black));
                                    ds.setUnderlineText(true);
                                }
                            }, spanTxt.length() - getString(R.string.tu_sign_up_now).length(), spanTxt.length(), 0);
                            spanTxt.append(".");
                            tvUnavailableUserTxt.setMovementMethod(LinkMovementMethod.getInstance());
                            tvUnavailableUserTxt.setText(spanTxt, TextView.BufferType.SPANNABLE);

                            dialog.show();
                        }*/
                    }
                }
                break;
            case R.id.imv_intro_voice:
                if (!TextUtils.isEmpty(mTutor.getIntroVoice())) {

                    String fileName = mTutor.getIntroVoice().substring(mTutor.getIntroVoice().lastIndexOf('/') + 1, mTutor.getIntroVoice().length());
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

                                    imvIntroVoice.setImageResource(R.drawable.ic_play_voice);
                                    mMusicProgressBar.setProgress(0);
                                }
                            });
                            try {
                                mMediaPlayer.setDataSource(file.getAbsolutePath());
                                mMediaPlayer.prepare();
                                mMediaPlayer.start();

                                imvIntroVoice.setImageResource(R.drawable.ic_pause_voice);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {

                            if (mMediaPlayer.isPlaying()) {
                                mMediaPlayer.pause();

                                imvIntroVoice.setImageResource(R.drawable.ic_play_voice);
                            } else {
                                mMediaPlayer.start();

                                imvIntroVoice.setImageResource(R.drawable.ic_pause_voice);
                            }
                        }
                    } else {

                        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setMessage(getString(R.string.tu_lst_media_loading));
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        FileDownloader.downloadImage(mTutor.getIntroVoice(), new FileDownloader.OnDownloadFinishedListener() {
                            @Override
                            public void onSuccess() {
                                progressDialog.dismiss();

                                mMediaPlayer = new MediaPlayer();
                                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mediaPlayer) {
                                        mMediaPlayer.stop();
                                        mMediaPlayer.reset();
                                        mMediaPlayer = null;

                                        imvIntroVoice.setImageResource(R.drawable.ic_play_voice);
                                        mMusicProgressBar.setProgress(0);
                                    }
                                });
                                try {
                                    mMediaPlayer.setDataSource(file.getAbsolutePath());
                                    mMediaPlayer.prepare();
                                    mMediaPlayer.start();

                                    imvIntroVoice.setImageResource(R.drawable.ic_pause_voice);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError() {
                                progressDialog.dismiss();
                                Toast.makeText(getActivity(), getString(R.string.tu_lst_cant_download_video), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                break;
        }
    }
}
