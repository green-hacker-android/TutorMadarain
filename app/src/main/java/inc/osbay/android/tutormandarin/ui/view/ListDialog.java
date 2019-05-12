package inc.osbay.android.tutormandarin.ui.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import inc.osbay.android.tutormandarin.R;

public class ListDialog extends Dialog {

    Context mContext;
    String[] mTitles;
    int[] mIcons;

    public ListDialog(Context context, String[] titles, int[] icons,
                      final AdapterView.OnItemClickListener listener) {
        super(context, R.style.FullScreenDialog);
        mContext = context;

        mTitles = titles;
        mIcons = icons;

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        final ListView rvCourseList = new ListView(mContext);
        rvCourseList.setDivider(new ColorDrawable(mContext.getResources().getColor(R.color.light_gray)));
        rvCourseList.setDividerHeight(1);
        rvCourseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dismiss();

                if (listener != null) {
                    listener.onItemClick(adapterView, view, i, l);
                }
            }
        });

        rvCourseList.setAdapter(new SelectLevelAdapter());
        this.setContentView(rvCourseList);

        this.setCanceledOnTouchOutside(true);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
//        int width = size.x;

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.width = size.x;
        getWindow().setAttributes(lp);

    }

    @Override
    protected void onStart() {
        super.onStart();

        final View decorView = getWindow()
                .getDecorView();

        int start;
        int end;
        if (getWindow().getAttributes().gravity == Gravity.TOP) {
            start = -1000;
            end = 0;
        } else {
            start = 1000;
            end = 0;
        }

        ObjectAnimator slideDown = ObjectAnimator.ofFloat(decorView, "translationY", start, end);
        slideDown.setDuration(200);
        slideDown.start();
    }

    public void setGravity(int gravity) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.gravity = gravity;
        getWindow().setAttributes(lp);
    }

    @Override
    public void dismiss() {
        final View decorView = getWindow()
                .getDecorView();

        int start;
        int end;
        if (getWindow().getAttributes().gravity == Gravity.TOP) {
            start = 0;
            end = -decorView.getHeight();
        } else {
            start = 0;
            end = decorView.getHeight();
        }

        ObjectAnimator slideDown = ObjectAnimator.ofFloat(decorView, "translationY", start,
                end);
        slideDown.setDuration(200);
        slideDown.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                ListDialog.super.dismiss();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        slideDown.start();
    }

    private class SelectLevelAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.item_list_dialog, viewGroup, false);
            }

            TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
            tvTitle.setText(mTitles[i]);

            ImageView imvSelect = (ImageView) view.findViewById(R.id.imv_icon);
            if (mIcons != null && mIcons.length > i) {
                imvSelect.setVisibility(View.VISIBLE);
                imvSelect.setImageResource(mIcons[i]);
            } else {
                imvSelect.setVisibility(View.GONE);
            }

            return view;
        }
    }

}
