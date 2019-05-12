package inc.osbay.android.tutormandarin.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.ui.fragment.FlashcardTutorialFragment;

public class FlashcardTutorialActivity extends AppCompatActivity {
    private int[] mPages = {1, 2, 3};
    private int mDotsCount;
    private ImageView[] mDots;
    private LinearLayout mLlVpTutorialDotsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_tutorial);
        mLlVpTutorialDotsCount = (LinearLayout) findViewById(R.id.ll_vp_flashcard_tutorial_dots_count);
        ViewPager vpFlashcardTutorial = (ViewPager) findViewById(R.id.vp_flashcard_tutorial);
        vpFlashcardTutorial.setAdapter(new FlashcardTutorialAdapter(getSupportFragmentManager()));
        setUiPageViewController();

        vpFlashcardTutorial.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < mDotsCount; i++) {
                    mDots[i].setImageDrawable(getResources().getDrawable(R.drawable.non_selected_item_dot));
                }

                mDots[position].setImageDrawable(getResources().getDrawable(R.drawable.selected_item_dot));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setUiPageViewController() {
        mDotsCount = mPages.length;
        mDots = new ImageView[mDotsCount];

        for (int i = 0; i < mDotsCount; i++) {
            mDots[i] = new ImageView(this);
            mDots[i].setImageDrawable(getResources().getDrawable(R.drawable.non_selected_item_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(10, 0, 10, 0);

            mLlVpTutorialDotsCount.addView(mDots[i], params);

        }

        mDots[0].setImageDrawable(getResources().getDrawable(R.drawable.selected_item_dot));
    }

    class FlashcardTutorialAdapter extends FragmentStatePagerAdapter {

        FlashcardTutorialAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return FlashcardTutorialFragment.newInstance(String.valueOf(mPages[position]));
        }

        @Override
        public int getCount() {
            return mPages.length;
        }
    }
}
