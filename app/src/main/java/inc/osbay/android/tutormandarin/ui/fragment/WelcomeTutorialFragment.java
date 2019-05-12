package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.Fragment;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.percent.PercentRelativeLayout;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.felipecsl.gifimageview.library.GifImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import inc.osbay.android.tutormandarin.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeTutorialFragment extends Fragment {
    private static final String PAGE_NO = "WelcomeTutorialFragment.PAGE_NO";
    private GifImageView givGifBackground;
    private PercentRelativeLayout prlTutorialLayout;
    private String mLocale;
    private String pgNo;

    public WelcomeTutorialFragment() {
    }

    public static WelcomeTutorialFragment newInstance(String text) {

        WelcomeTutorialFragment f = new WelcomeTutorialFragment();
        Bundle b = new Bundle();
        b.putString(PAGE_NO, text);

        f.setArguments(b);

        return f;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(WelcomeTutorialFragment.class.getSimpleName(), "On Destroy");
        givGifBackground.stopAnimation();
        givGifBackground.clear();
    }

    @Override
    public void onPause() {
        super.onPause();
        prlTutorialLayout.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pgNo != null && pgNo.equals("1")) {
            prlTutorialLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocale = Locale.getDefault().getLanguage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_welcome_tutorial, container, false);

        // binding layout
        SimpleDraweeView sdvWelcomeBackground = v.findViewById(R.id.sdv_welcome_background);
        ImageView imvWelcomeTitle = v.findViewById(R.id.imv_welcome_title);
        ImageView imvWelcomeTxt = v.findViewById(R.id.imv_welcome_txt);
        TextView tvWelcomeTxt = v.findViewById(R.id.tv_welcome_txt);
        prlTutorialLayout = v.findViewById(R.id.prl_tutorial_layout);
        RelativeLayout rlWelcome = v.findViewById(R.id.rl_welcome);
        LinearLayout llMainBackground = v.findViewById(R.id.ll_main_background);
        givGifBackground = v.findViewById(R.id.giv_gif_background);

        tvWelcomeTxt.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Montserrat-Bold.ttf"));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvWelcomeTxt.setText(Html.fromHtml(getString(R.string.wt_welcome_1_txt), Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvWelcomeTxt.setText(Html.fromHtml(getString(R.string.wt_welcome_1_txt)));
        }

        AssetManager assetManager = getActivity().getAssets();
        try {
            InputStream is = assetManager.open("main_background.gif");
            byte[] fileBytes = new byte[is.available()];

            is.read(fileBytes);
            is.close();

            givGifBackground.setBytes(fileBytes);
        } catch (IOException e) {
            // handle exception
            e.printStackTrace();
        }

        pgNo = getArguments().getString(PAGE_NO);

        if (pgNo != null) {
            if (pgNo.equals("1")) {
                givGifBackground.setVisibility(View.VISIBLE);
                givGifBackground.startAnimation();
                rlWelcome.setVisibility(View.GONE);
                llMainBackground.setVisibility(View.VISIBLE);
                prlTutorialLayout.setVisibility(View.VISIBLE);
            }
            if (pgNo.equals("2")) {
                givGifBackground.setVisibility(View.GONE);
                llMainBackground.setVisibility(View.GONE);
                rlWelcome.setVisibility(View.VISIBLE);
                prlTutorialLayout.setVisibility(View.GONE);

                sdvWelcomeBackground.setImageURI(new Uri.Builder()
                        .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                        .path(String.valueOf(R.drawable.ic_welcome_1))
                        .build());

                if (mLocale.equals("th")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_th_welcome_title_1);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_th_welcome_txt_1);
                } else if (mLocale.equals("de")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_de_welcome_title_1);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_de_welcome_txt_1);
                } else if (mLocale.equals("fr")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_welcome_title_1);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_fr_welcome_txt_1);
                } else if (mLocale.equals("ja")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_ja_welcome_title_1);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_ja_welcome_txt_1);
                } else if (mLocale.equals("ko")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_ko_welcome_title_1);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_ko_welcome_txt_1);
                } else {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_welcome_title_1);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_welcome_txt_1);
                }
            }
            if (pgNo.equals("3")) {
                givGifBackground.setVisibility(View.GONE);
                llMainBackground.setVisibility(View.GONE);
                rlWelcome.setVisibility(View.VISIBLE);
                prlTutorialLayout.setVisibility(View.GONE);

                sdvWelcomeBackground.setImageURI(new Uri.Builder()
                        .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                        .path(String.valueOf(R.drawable.ic_welcome_2))
                        .build());

                if (mLocale.equals("th")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_th_welcome_title_2);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_th_welcome_txt_2);
                } else if (mLocale.equals("de")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_de_welcome_title_2);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_de_welcome_txt_2);
                } else if (mLocale.equals("fr")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_fr_welcome_title_2);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_fr_welcome_txt_2);
                } else if (mLocale.equals("ja")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_ja_welcome_title_2);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_ja_welcome_txt_2);
                } else if (mLocale.equals("ko")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_ko_welcome_title_2);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_ko_welcome_txt_2);
                } else {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_welcome_title_2);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_welcome_txt_2);
                }
            }
            if (pgNo.equals("4")) {
                givGifBackground.setVisibility(View.GONE);
                llMainBackground.setVisibility(View.GONE);
                rlWelcome.setVisibility(View.VISIBLE);
                prlTutorialLayout.setVisibility(View.GONE);

                sdvWelcomeBackground.setImageURI(new Uri.Builder()
                        .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                        .path(String.valueOf(R.drawable.ic_welcome_3))
                        .build());

                if (mLocale.equals("th")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_th_welcome_title_3);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_th_welcome_txt_3);
                } else if (mLocale.equals("de")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_de_welcome_title_3);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_de_welcome_txt_3);
                } else if (mLocale.equals("fr")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_fr_welcome_title_3);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_fr_welcome_txt_3);
                } else if (mLocale.equals("ja")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_ja_welcome_title_3);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_ja_welcome_txt_3);
                } else if (mLocale.equals("ko")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_ko_welcome_title_3);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_ko_welcome_txt_3);
                } else {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_welcome_title_3);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_welcome_txt_3);
                }
            }
            if (pgNo.equals("5")) {
                givGifBackground.setVisibility(View.GONE);
                llMainBackground.setVisibility(View.GONE);
                rlWelcome.setVisibility(View.VISIBLE);
                prlTutorialLayout.setVisibility(View.GONE);

                sdvWelcomeBackground.setImageURI(new Uri.Builder()
                        .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                        .path(String.valueOf(R.drawable.ic_welcome_4))
                        .build());

                if (mLocale.equals("th")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_th_welcome_title_4);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_welcome_txt_4);
                } else if (mLocale.equals("de")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_de_welcome_title_4);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_de_welcome_txt_4);
                } else if (mLocale.equals("fr")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_fr_welcome_title_4);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_fr_welcome_txt_4);
                } else if (mLocale.equals("ja")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_ja_welcome_title_4);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_ja_welcome_txt_4);
                } else if (mLocale.equals("ko")) {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_ko_welcome_title_4);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_ko_welcome_txt_4);
                } else {
                    imvWelcomeTitle.setImageResource(R.drawable.ic_welcome_title_4);
                    imvWelcomeTxt.setImageResource(R.drawable.ic_welcome_txt_4);
                }
            }
        }
        return v;
    }
}
