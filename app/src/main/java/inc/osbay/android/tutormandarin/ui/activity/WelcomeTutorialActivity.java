package inc.osbay.android.tutormandarin.ui.activity;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.ui.fragment.BackHandledFragment;
import inc.osbay.android.tutormandarin.ui.fragment.WelcomeTutorialFragment;
import inc.osbay.android.tutormandarin.ui.view.FragmentStatePagerAdapter;

public class WelcomeTutorialActivity extends AppCompatActivity implements BackHandledFragment.BackHandlerInterface, View.OnClickListener {
    public static WelcomeTutorialActivity mInstance;
    private int[] mPages = {1, 2, 3, 4, 5};
    private int mDotsCount;
    private int mDotsPosition;
    private ImageView[] mDots;
    private LinearLayout mLlVpTutorialDotsCount;
    private RelativeLayout mRlDotsCount;
    private ViewPager vpWelcomeTutorial;
    private boolean mHasAccount;
    private TextView mTvWtSignUp;
    private TextView mTvWtSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_tutorial);

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        mInstance = this;

        mLlVpTutorialDotsCount = findViewById(R.id.ll_vp_welcome_tutorial_dots_count);
        mRlDotsCount = findViewById(R.id.rl_dots_count);
        final TextView mTvWelcomeSkip = findViewById(R.id.tv_welcome_skip);
        final TextView mTvWelcomeNext = findViewById(R.id.tv_welcome_next);
        mTvWtSignUp = findViewById(R.id.tv_wt_sign_up);
        mTvWtSignIn = findViewById(R.id.tv_wt_sign_in);

        mTvWelcomeSkip.setOnClickListener(this);
        mTvWelcomeNext.setOnClickListener(this);
        mTvWtSignUp.setOnClickListener(this);
        mTvWtSignIn.setOnClickListener(this);

        LinearLayout llWtSignInUp = findViewById(R.id.ll_wt_sign_in_up);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String token = prefs.getString("access_token", null);
        String accountId = prefs.getString("account_id", null);

        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(accountId)) {
            AccountAdapter accountAdapter = new AccountAdapter(this);
            Account account = accountAdapter.getAccountById(accountId);
            if (account != null) {
                mHasAccount = true;
                llWtSignInUp.setVisibility(View.GONE);

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mRlDotsCount.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                mRlDotsCount.setLayoutParams(params);
                mTvWelcomeSkip.setVisibility(View.VISIBLE);
                mTvWelcomeNext.setVisibility(View.VISIBLE);
            } else {
                mHasAccount = false;
                llWtSignInUp.setVisibility(View.VISIBLE);
                mTvWelcomeSkip.setVisibility(View.GONE);
                mTvWelcomeNext.setVisibility(View.GONE);
            }
        } else {
            mHasAccount = false;
            llWtSignInUp.setVisibility(View.VISIBLE);
        }

        vpWelcomeTutorial = findViewById(R.id.vp_welcome_tutorial);
        vpWelcomeTutorial.setAdapter(new WelcomeTutorialAdapter(getFragmentManager()));
        setUiPageViewController();

        mDotsPosition = 0;

        vpWelcomeTutorial.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mDotsPosition = position;
                for (int i = 0; i < mDotsCount; i++) {
                    mDots[i].setImageDrawable(ContextCompat.getDrawable(
                            WelcomeTutorialActivity.this, R.drawable.non_selected_item_dot));
                }

                mDots[position].setImageDrawable(ContextCompat.getDrawable(
                        WelcomeTutorialActivity.this, R.drawable.selected_item_dot));

                if (mDotsPosition == (mDotsCount - 1) && mHasAccount) {
                    mTvWelcomeNext.setVisibility(View.GONE);
                    mRlDotsCount.setVisibility(View.VISIBLE);
                    mTvWelcomeSkip.setVisibility(View.VISIBLE);
                } else if (mDotsPosition == (mDotsCount - 1) && !mHasAccount) {
                    mRlDotsCount.setVisibility(View.VISIBLE);
                } else if (mDotsPosition != (mDotsCount - 1) && !mHasAccount) {
                    mTvWelcomeNext.setVisibility(View.GONE);
                    mRlDotsCount.setVisibility(View.VISIBLE);
                } else {
                    mTvWelcomeNext.setVisibility(View.VISIBLE);
                    mRlDotsCount.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setUiPageViewController() {
        mDotsCount = mPages.length;
        mDots = new ImageView[mDotsCount];

        for (int i = 0; i < mDotsCount; i++) {
            mDots[i] = new ImageView(this);
            mDots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.non_selected_item_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(10, 0, 10, 0);

            mLlVpTutorialDotsCount.addView(mDots[i], params);
        }

        mDots[0].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.selected_item_dot));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_welcome_next:
                for (int i = 0; i < mDotsCount; i++) {
                    mDots[i].setImageDrawable(ContextCompat.getDrawable(
                            WelcomeTutorialActivity.this, R.drawable.non_selected_item_dot));
                }
                ++mDotsPosition;
                if (mDotsPosition < mDotsCount) {
                    if (mDotsPosition == (mDotsCount - 1)) {
                        mRlDotsCount.setVisibility(View.GONE);
                    } else {
                        mRlDotsCount.setVisibility(View.VISIBLE);
                    }
                    mDots[mDotsPosition].setImageDrawable(ContextCompat.getDrawable(
                            WelcomeTutorialActivity.this, R.drawable.selected_item_dot));

                    vpWelcomeTutorial.setCurrentItem(mDotsPosition);
                } else {
                    finish();
                }
                break;
            case R.id.tv_welcome_skip:
                finish();
                break;
            case R.id.tv_wt_sign_up:
                Intent signUpIntent = new Intent(this, SignUpActivity.class);
                signUpIntent.putExtra(SignUpActivity.VP_SU_POSITION, mDotsPosition);
                signUpIntent.putExtra(SignUpActivity.MAIN_SIGN_UP, 0);
                startActivity(signUpIntent);
                break;
            case R.id.tv_wt_sign_in:
                Intent signInIntent = new Intent(this, SignInActivity.class);
                signInIntent.putExtra(SignInActivity.VP_SI_POSITION, mDotsPosition);
                signInIntent.putExtra(SignInActivity.MAIN_SIGN_IN, 0);
                startActivity(signInIntent);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTvWtSignUp.setVisibility(View.GONE);
        mTvWtSignIn.setVisibility(View.GONE);
        mLlVpTutorialDotsCount.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTvWtSignUp.setVisibility(View.VISIBLE);
        mTvWtSignIn.setVisibility(View.VISIBLE);
        mLlVpTutorialDotsCount.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    @Override
    public void setmSelectedFragment(BackHandledFragment backHandledFragment) {

    }

    private class WelcomeTutorialAdapter extends FragmentStatePagerAdapter {

        WelcomeTutorialAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return WelcomeTutorialFragment.newInstance(String.valueOf(mPages[position]));
        }

        @Override
        public int getCount() {
            return mPages.length;
        }
    }
}
