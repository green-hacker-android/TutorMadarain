package inc.osbay.android.tutormandarin.ui.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.artifex.mupdf.fitz.Document;
import com.artifex.mupdf.fitz.Link;
import com.artifex.mupdf.fitz.Matrix;
import com.artifex.mupdf.fitz.Page;
import com.artifex.mupdf.fitz.android.AndroidDrawDevice;
import com.facebook.common.util.UriUtil;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.sdk.model.Booking;
import inc.osbay.android.tutormandarin.sdk.model.Lesson;
import inc.osbay.android.tutormandarin.ui.fragment.BackHandledFragment;
import inc.osbay.android.tutormandarin.ui.fragment.ChangeBookingFragment;
import inc.osbay.android.tutormandarin.ui.fragment.FavouriteDrawerFragment;
import inc.osbay.android.tutormandarin.ui.fragment.MyBookingFragment;
import inc.osbay.android.tutormandarin.ui.view.CustomViewPager;
import inc.osbay.android.tutormandarin.util.Worker;

public class LessonPdfActivity extends AppCompatActivity implements BackHandledFragment.BackHandlerInterface, View.OnClickListener, SeekBar.OnSeekBarChangeListener, ViewPager.OnPageChangeListener {

    public static final String EXTRA_PDF_LESSON_ID = "LessonPdfActivity.EXTRA_PDF_LESSON_ID";
    public static final String EXTRA_BOOKING_FRAGMENT = "LessonPdfActivity.EXTRA_BOOKING_FRAGMENT";
    public static final String FILE_NAME = "LessonPdfActivity.FILE_NAME";
    public static final String FILE_PATH = CommonConstant.PDF_PATH;
    private static final String PDF_PREF = "PDF_PREF";
    private static final String PDF_PREF_VALUE = "PDF_PREF_VALUE";

    protected boolean wentBack;
    private RelativeLayout mRlBrightnessPopUp;
    private RelativeLayout mRlMenu;
    private LinearLayout mLlPreview;
    private TextView mTvCurrentPage;
    private TextView tvTotalPage;
    private ImageView mImvPdfLock;
    private Lesson mLesson;
    private Account mAccount;
    private BackHandledFragment mSelectedFragment;
    private Worker mWorker;
    private Document mDocument;
    private RecyclerView rvPdf;
    private List<Bitmap> mBitmaps;
    private CustomViewPager mVpPdf;
    private CustomPagerAdapter mCustomPagerAdapter;
    private CustomRvAdapter mCustomRvAdapter;
    private ServerRequestManager requestManager;
    private String mCompletedPath;
    private String mFileName;
    private String mTutorId;
    private String mStartTime;
    private String mBookingId;
    private int mMyBookingFragment;
    private int mCurrentPage;
    private int mBookingType;
    private int pageCount;
    private int canvasW, canvasH;
    private float layoutW, layoutH, layoutEm;
    private float displayDPI;
    private boolean mIsBrightnessClicked = false;
    private boolean hasLoaded;
    private boolean isReflowable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_pdf);

        System.gc();
        requestManager = new ServerRequestManager(this);
        CurriculumAdapter curriculumAdapter = new CurriculumAdapter(this);

        SharedPreferences pdfGuidePref = getSharedPreferences(PDF_PREF, MODE_PRIVATE);
        boolean pdfGuideVal = pdfGuidePref.getBoolean(PDF_PREF_VALUE, false);


        final SimpleDraweeView sdvPdfTutorial = findViewById(R.id.sdv_pdf_tutorial);
        if (!pdfGuideVal) {
            sdvPdfTutorial.setVisibility(View.VISIBLE);
            sdvPdfTutorial.setImageURI(new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                    .path(String.valueOf(R.drawable.pic_pdf_guide))
                    .build());

            sdvPdfTutorial.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (sdvPdfTutorial.getVisibility() == View.VISIBLE) {
                        sdvPdfTutorial.setVisibility(View.GONE);
                    }
                }
            });

            pdfGuidePref.edit().putBoolean(PDF_PREF_VALUE, true).apply();
        } else {
            sdvPdfTutorial.setVisibility(View.GONE);
        }


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String token = prefs.getString("access_token", null);
        String accountId = prefs.getString("account_id", null);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mMyBookingFragment = bundle.getInt(EXTRA_BOOKING_FRAGMENT, 0);
            if (mMyBookingFragment == 1) {
                mTutorId = bundle.getString(MyBookingFragment.EXTRA_TUTOR_ID);
                mStartTime = bundle.getString(MyBookingFragment.EXTRA_START_TIME);
                mBookingType = bundle.getInt(MyBookingFragment.EXTRA_BOOKING_TYPE);
            } else if (mMyBookingFragment == 2) {
                mTutorId = bundle.getString(ChangeBookingFragment.EXTRA_TUTOR_ID);
                mStartTime = bundle.getString(ChangeBookingFragment.EXTRA_START_TIME);
                mBookingType = bundle.getInt(ChangeBookingFragment.EXTRA_BOOKING_TYPE);
                mBookingId = bundle.getString(ChangeBookingFragment.EXTRA_BOOKING_ID);
            }
        }

        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
            AccountAdapter accountAdapter = new AccountAdapter(this);
            mAccount = accountAdapter.getAccountById(accountId);
        }

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        displayDPI = metrics.densityDpi;

        if (getIntent() != null) {
            mFileName = getIntent().getStringExtra(FILE_NAME);
            mLesson = curriculumAdapter.getLessonById(getIntent().getStringExtra(EXTRA_PDF_LESSON_ID));
        }

        mRlBrightnessPopUp = findViewById(R.id.rl_brightness_pop_up);
        mRlMenu = findViewById(R.id.rl_menu);
        mLlPreview = findViewById(R.id.ll_preview);
        mTvCurrentPage = findViewById(R.id.tv_current_page);
        tvTotalPage = findViewById(R.id.tv_total_page);
        mImvPdfLock = findViewById(R.id.imv_lock_pdf);
        Button btnBooking = findViewById(R.id.btn_lesson_booking);
        ImageView imvBrightness = findViewById(R.id.imv_brightness);
        ImageView imvLessonPdfFavorite = findViewById(R.id.imv_lesson_pdf_favorite);
        ImageView imvLessonPdfShare = findViewById(R.id.imv_lesson_pdf_share);
        ImageView imvLessonPdfBack = findViewById(R.id.imv_lesson_pdf_back);
        LinearLayout llPdfLock = findViewById(R.id.ll_pdf_lock);
        SeekBar sbBrightness = findViewById(R.id.sb_brightness);
        sbBrightness.setMax(255);

        sbBrightness.setOnSeekBarChangeListener(this);
        imvBrightness.setOnClickListener(this);
        imvLessonPdfFavorite.setOnClickListener(this);
        imvLessonPdfShare.setOnClickListener(this);
        imvLessonPdfBack.setOnClickListener(this);
        btnBooking.setOnClickListener(this);

        int currentBrightness = 204;
        sbBrightness.setProgress(currentBrightness);

        mTvCurrentPage.setText(String.valueOf(1));
        mCompletedPath = FILE_PATH + File.separator + mFileName;

        mWorker = new Worker(this);
        mWorker.start();

        if (!hasLoaded) {
            hasLoaded = true;
            openDocument();
        }

        mVpPdf = findViewById(R.id.vp_pdf);
        mCustomPagerAdapter = new CustomPagerAdapter();
        mVpPdf.setOffscreenPageLimit(3);
        mVpPdf.addOnPageChangeListener(this);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        canvasW = displayMetrics.widthPixels;
        canvasH = displayMetrics.heightPixels;

        rvPdf = findViewById(R.id.rv_pdf);
        mCustomRvAdapter = new CustomRvAdapter();
        LinearLayoutManager llmPdf = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        rvPdf.setLayoutManager(llmPdf);
        rvPdf.setAdapter(mCustomRvAdapter);


        if (mLesson.isFree()) {
            mImvPdfLock.setVisibility(View.GONE);
            mVpPdf.setVisibility(View.VISIBLE);
            llPdfLock.setVisibility(View.GONE);
            rvPdf.setVisibility(View.VISIBLE);
        } else {
            if (mLesson.isAvailable()) {
                if (mLesson.isBought()) {
                    mImvPdfLock.setVisibility(View.GONE);
                    mVpPdf.setVisibility(View.VISIBLE);
                    llPdfLock.setVisibility(View.GONE);
                    rvPdf.setVisibility(View.VISIBLE);
                } else {
                    mImvPdfLock.setVisibility(View.VISIBLE);
                    mVpPdf.setVisibility(View.GONE);
                    llPdfLock.setVisibility(View.VISIBLE);
                    rvPdf.setVisibility(View.GONE);
                }
            } else {
                mImvPdfLock.setVisibility(View.VISIBLE);
                mVpPdf.setVisibility(View.GONE);
                llPdfLock.setVisibility(View.VISIBLE);
                rvPdf.setVisibility(View.GONE);
            }
        }

        mImvPdfLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRlMenu.getVisibility() == View.VISIBLE) {
                    mRlMenu.setVisibility(View.GONE);
                    mLlPreview.setVisibility(View.GONE);
                } else {
                    mRlMenu.setVisibility(View.VISIBLE);
                    mLlPreview.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    protected void openDocument() {
        mWorker.add(new Worker.Task() {
            public void work() {
                mDocument = Document.openDocument(mCompletedPath);
            }

            public void run() {
                loadDocument();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCustomPagerAdapter.notifyDataSetChanged();
        mVpPdf.invalidate();
        mVpPdf.setOffscreenPageLimit(3);
        mCustomRvAdapter.notifyDataSetChanged();
    }

    protected void loadDocument() {
        mWorker.add(new Worker.Task() {
            public void work() {
                try {
                    isReflowable = mDocument.isReflowable();
                    if (isReflowable) {
                        mDocument.layout(layoutW, layoutH, layoutEm);
                    }
                    pageCount = mDocument.countPages();

                    tvTotalPage.setText(String.valueOf(pageCount));
                } catch (Throwable x) {
                    mDocument = null;
                    pageCount = 1;
                }
            }

            public void run() {
                loadAllPages();
            }
        });
    }

    protected void loadAllPages() {
        mWorker.add(new Worker.Task() {
            Bitmap bitmap;

            public void work() {
                mBitmaps = new ArrayList<>();
                try {
                    for (int i = 0; i < pageCount; i++) {
                        Page page = mDocument.loadPage(i);
                        Matrix ctm = AndroidDrawDevice.fitPageWidth(page, 100);
                        bitmap = AndroidDrawDevice.drawPage(page, ctm);
                        mBitmaps.add(bitmap);
                    }
                } catch (Throwable x) {
                    android.util.Log.e("LessonPdfActivity", x.getMessage());
                }
            }

            public void run() {

                if (mLesson.isFree()) {
                    mVpPdf.setAdapter(mCustomPagerAdapter);
                    mVpPdf.setOffscreenPageLimit(3);
                    rvPdf.setAdapter(mCustomRvAdapter);
                } else {
                    if (mLesson.isAvailable()) {
                        if (mLesson.isBought()) {
                            mVpPdf.setAdapter(mCustomPagerAdapter);
                            mVpPdf.setOffscreenPageLimit(3);
                            rvPdf.setAdapter(mCustomRvAdapter);
                        } else {
                            if (mBitmaps.size() > 0) {
                                loadPage(mImvPdfLock, 0);
                            }
                        }
                    } else {
                        if (mBitmaps.size() > 0) {
                            loadPage(mImvPdfLock, 0);
                        }
                    }
                }
            }
        });
    }

    private void loadPage(final ImageView pv, final int pageNumber) {
        mWorker.add(new Worker.Task() {
            Bitmap bitmap;
            Link[] links;

            public void work() {
                try {
                    Page page = mDocument.loadPage(pageNumber);

                    Matrix ctm = AndroidDrawDevice.fitPage(page, canvasW, canvasH);
                    bitmap = AndroidDrawDevice.drawPage(page, ctm);
                    links = page.getLinks();
                    if (links != null)
                        for (Link link : links)
                            link.bounds.transform(ctm);
                } catch (Throwable x) {
                    android.util.Log.e("LessonPdfActivity", x.getMessage());
                }
            }

            public void run() {
                if (bitmap != null)
                    pv.setImageBitmap(bitmap);
                else
                    android.util.Log.e("LessonPdfActivity", "image error");
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        canvasW = displayMetrics.widthPixels;
        canvasH = displayMetrics.heightPixels;

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mVpPdf.setAdapter(mCustomPagerAdapter);
            mVpPdf.setCurrentItem(mCurrentPage);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mVpPdf.setAdapter(mCustomPagerAdapter);
            mVpPdf.setCurrentItem(mCurrentPage);
        }
    }

    public void onPageViewSizeChanged(int w, int h) {
        canvasW = w;
        canvasH = h;
        layoutW = canvasW / displayDPI;
        layoutH = canvasH / displayDPI;

        if (!hasLoaded) {
            hasLoaded = true;
            openDocument();
        }
    }

    public void toggleThumbnail() {
        if (mRlMenu.getVisibility() == View.VISIBLE) {
            mRlMenu.setVisibility(View.GONE);
            mLlPreview.setVisibility(View.GONE);
        } else {
            mRlMenu.setVisibility(View.VISIBLE);
            mLlPreview.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBitmaps.size() > 0) {
            for (int i = 0; i < mBitmaps.size(); i++) {
                if (mBitmaps.get(i) != null) {
                    mBitmaps.get(i).recycle();
                }
            }
            mBitmaps.clear();
        }
        mImvPdfLock.setImageBitmap(null);
        mImvPdfLock = null;
        mWorker.stop();
        mVpPdf.setAdapter(null);
        rvPdf.setAdapter(null);
        rvPdf = null;
        mRlMenu = null;
        mRlBrightnessPopUp = null;
        mDocument.destroy();
        mDocument = null;
        System.gc();
    }

    protected void loadVpPage(final ImageView imvLoading, final PageView pv, final int pageNumber) {
        mWorker.add(new Worker.Task() {
            Bitmap bitmap;
            Link[] links;

            public void work() {
                try {
                    Page page = mDocument.loadPage(pageNumber);

                    Matrix ctm = AndroidDrawDevice.fitPage(page, canvasW, canvasH);
                    bitmap = AndroidDrawDevice.drawPage(page, ctm);
                    links = page.getLinks();
                    if (links != null)
                        for (Link link : links)
                            link.bounds.transform(ctm);
                } catch (Throwable x) {
                    android.util.Log.e("LessonPdfActivity", x.getMessage());
                }
            }

            public void run() {
                imvLoading.setVisibility(View.GONE);
                if (bitmap != null)
                    pv.setBitmap(bitmap, wentBack, links);
                else
                    pv.setError();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imv_brightness:
                if (mIsBrightnessClicked) {
                    mIsBrightnessClicked = false;
                    mRlBrightnessPopUp.setVisibility(View.GONE);
                } else {
                    mIsBrightnessClicked = true;
                    mRlBrightnessPopUp.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.btn_lesson_booking:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String token = prefs.getString("access_token", null);
                String accountId = prefs.getString("account_id", null);

                if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {

                    /*** Limit for only Active user to buy lessons ***/
                    /*if (mAccount != null && mAccount.getStatus() != Account.Status.ACTIVE) {
                        new AlertDialog.Builder(this)
                                .setTitle(getString(R.string.ls_unavailable_title))
                                .setMessage(getString(R.string.ls_unavailable_msg))
                                .setPositiveButton(getString(R.string.ls_unavailable_ok), null)
                                .show();

                        return;
                    }*/

                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage(getString(R.string.ls_check_lesson_loading));
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    requestManager.checkLessonToBook(mLesson.getLessonId(), 1,
                            new ServerRequestManager.OnRequestFinishedListener() {
                                @Override
                                public void onSuccess(Object result) {
                                    progressDialog.dismiss();

                                    Intent intent = new Intent(LessonPdfActivity.this, MainActivity.class);

                                    Bundle bundle = new Bundle();

                                    if (mMyBookingFragment == 1) {
                                        bundle.putInt(MyBookingFragment.EXTRA_BOOKING_TYPE, Booking.Type.LESSON);
                                        bundle.putString(MyBookingFragment.EXTRA_LESSON_ID, mLesson.getLessonId());
                                        intent.putExtra(MyBookingFragment.EXTRA_TUTOR_ID, mTutorId);
                                        intent.putExtra(MyBookingFragment.EXTRA_START_TIME, mStartTime);
                                        intent.putExtra(MyBookingFragment.EXTRA_BOOKING_TYPE, mBookingType);
                                        intent.setAction("booking_refresh");
                                    } else if (mMyBookingFragment == 2) {
                                        bundle.putInt(ChangeBookingFragment.EXTRA_BOOKING_TYPE, Booking.Type.LESSON);
                                        bundle.putString(ChangeBookingFragment.EXTRA_LESSON_ID, mLesson.getLessonId());
                                        intent.putExtra(ChangeBookingFragment.EXTRA_TUTOR_ID, mTutorId);
                                        intent.putExtra(ChangeBookingFragment.EXTRA_START_TIME, mStartTime);
                                        intent.putExtra(ChangeBookingFragment.EXTRA_BOOKING_TYPE, mBookingType);
                                        intent.putExtra(ChangeBookingFragment.EXTRA_BOOKING_ID, mBookingId);
                                        intent.setAction("change_booking_refresh");
                                    }
                                    bundle.putString("class_type", "normal");
                                    intent.putExtras(bundle);
                                    startActivity(intent);

                                    finish();
                                }

                                @Override
                                public void onError(ServerError err) {
                                    progressDialog.dismiss();
                                    if (err.getErrorCode() == ServerError.Code.LESSON_BOOKED_IN_PACKAGE) {
                                        new AlertDialog.Builder(LessonPdfActivity.this)
                                                .setTitle(getString(R.string.ls_booked_in_package_error))
                                                .setMessage(getString(R.string.ls_booked_in_package_msg))
                                                .setPositiveButton(getString(R.string.ls_booked_in_package_ok), null)
                                                .show();
                                    } else if (err.getErrorCode() == ServerError.Code.HIGHER_PACKAGE_LEVEL) {
                                        new AlertDialog.Builder(LessonPdfActivity.this)
                                                .setTitle(getString(R.string.ls_higher_level_error))
                                                .setMessage(getString(R.string.ls_higher_level_msg))
                                                .setPositiveButton(getString(R.string.ls_higher_level_ok), null)
                                                .show();
                                    } else if (err.getErrorCode() == ServerError.Code.LESSON_CONTAINS_IN_PACKAGE) {
                                        new AlertDialog.Builder(LessonPdfActivity.this)
                                                .setTitle(getString(R.string.ls_already_booked_error))
                                                .setMessage(getString(R.string.ls_already_booked_msg))
                                                .setPositiveButton(getString(R.string.ls_already_booked_ok), null)
                                                .show();
                                    } else if (err.getErrorCode() == ServerError.Code.CHINESE_LEVEL_LOWER) {
                                        new AlertDialog.Builder(LessonPdfActivity.this)
                                                .setTitle(getString(R.string.ls_chinese_level_lower_error))
                                                .setMessage(getString(R.string.ls_chinese_level_lower))
                                                .setPositiveButton(getString(R.string.ls_chinese_level_lower_ok), null)
                                                .show();
                                    } else {
                                        new AlertDialog.Builder(LessonPdfActivity.this)
                                                .setTitle(getString(R.string.ls_server_error))
                                                .setMessage(err.getMessage())
                                                .setPositiveButton(getString(R.string.ls_server_error_ok), null)
                                                .show();
                                    }
                                }
                            });
                }

                break;
            case R.id.imv_lesson_pdf_favorite:
                if (mAccount != null) {
                    CurriculumAdapter curriculumAdapter = new CurriculumAdapter(this);
                    if (curriculumAdapter.checkFavourite(mLesson.getLessonId(), mAccount.getAccountId())) {
                        Toast.makeText(this, getString(R.string.ls_already_added_msg), Toast.LENGTH_SHORT).show();
                    } else {
                        Map<String, String> favourite = new HashMap<>();
                        favourite.put("lesson_tutor_id", mLesson.getLessonId());
                        favourite.put("fav_lesson_user", mAccount.getAccountId());
                        favourite.put("type", String.valueOf(FavouriteDrawerFragment.LESSON_ID));
                        curriculumAdapter.insertFavourite(favourite);
                        Toast.makeText(this, getString(R.string.ls_successfully_added), Toast.LENGTH_SHORT).show();

                        ServerRequestManager serverRequestManager = new ServerRequestManager(this);
                        serverRequestManager.addToFavorite(FavouriteDrawerFragment.LESSON_ID, mLesson.getLessonId(), FavouriteDrawerFragment.FAVOURITE_ID);
                    }
                }
                break;

            case R.id.imv_lesson_pdf_share:
                String content = ShareActivity.getShareLessonTemplate(
                        mLesson.getTitle());

                Intent intent = new Intent(this, ShareActivity.class);
                intent.putExtra(ShareActivity.EXTRA_SHARE_ITEM, "Lesson");
                intent.putExtra(ShareActivity.EXTRA_TITLE, mLesson.getTitle());
                intent.putExtra(ShareActivity.EXTRA_CONTENT, content);
                intent.putExtra(ShareActivity.EXTRA_IMAGE_URL, mLesson.getCoverPhoto());

                startActivity(intent);
                break;

            case R.id.imv_lesson_pdf_back:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        float BackLightValue = (float) progress / 300;

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes(); // Get Params
        layoutParams.screenBrightness = BackLightValue; // Set Value
        getWindow().setAttributes(layoutParams); // Set params
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mIsBrightnessClicked) {
            mIsBrightnessClicked = false;
            mRlBrightnessPopUp.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mTvCurrentPage.setText(String.valueOf(position + 1));
    }

    @Override
    public void onPageSelected(int position) {
        mCurrentPage = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }


    @Override
    public void setmSelectedFragment(BackHandledFragment backHandledFragment) {
        mSelectedFragment = backHandledFragment;
    }

    @Override
    public void onBackPressed() {
        if (mSelectedFragment != null) {
            mSelectedFragment.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    private class CustomRvAdapter extends RecyclerView.Adapter<CustomRvAdapter.CustomRvHolder> {
        @Override
        public CustomRvHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.rv_mu_pdf, parent, false);
            return new CustomRvHolder(v);
        }

        @Override
        public void onBindViewHolder(final CustomRvHolder holder, int position) {
            holder.imvPdf.setImageBitmap(mBitmaps.get(position));
            holder.imvPdf.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mVpPdf.setCurrentItem(holder.getAdapterPosition());
                }
            });
        }

        @Override
        public int getItemCount() {
            return mBitmaps.size();
        }

        class CustomRvHolder extends RecyclerView.ViewHolder {
            ImageView imvPdf;

            CustomRvHolder(View itemView) {
                super(itemView);
                imvPdf = itemView.findViewById(R.id.imv_mu_pdf);
            }
        }
    }

    private class CustomPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return pageCount;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.vw_mu_pdf, container, false);
            PageView pv = v.findViewById(R.id.page_view);
            pv.setActionListener(LessonPdfActivity.this);
            ImageView imvLoading = v.findViewById(R.id.imv_loading);
            loadVpPage(imvLoading, pv, position);
            container.addView(v);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View v = (View) object;
            PageView pv = v.findViewById(R.id.page_view);
            Bitmap bmp = pv.getBitmap();

            if (bmp != null) {
                bmp.recycle();
            }

            container.removeView(v);
            unbindDrawables((View) object);
        }

        private void unbindDrawables(View view) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                ((ViewGroup) view).removeAllViews();
            }
        }
    }
}
