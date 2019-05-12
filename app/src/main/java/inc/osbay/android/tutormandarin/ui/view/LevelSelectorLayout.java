package inc.osbay.android.tutormandarin.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import inc.osbay.android.tutormandarin.R;


/**
 * TODO: document your custom view class.
 */
public class LevelSelectorLayout extends RelativeLayout implements View.OnTouchListener, View.OnClickListener {
    private static final String TAG = LevelSelectorLayout.class.getSimpleName();

    private LayoutInflater mInflater;
    private Bitmap rotorImage;

    private float mAngleDown;
    private float mAngleUp;
    private String mLastSelectedLevel;

    private ImageView ivRotor;
    private OnLevelSelectedListener mLevelSelectListener;

    private TextView tvLvlB1;
    private TextView tvLvlB2;
    private TextView tvLvlI1;
    private TextView tvLvlI2;
    private TextView tvLvlA1;
    private TextView tvLvlA2;

    private GestureDetector levelSelectDetector;

    public LevelSelectorLayout(Context context) {
        super(context);

        mInflater = LayoutInflater.from(context);
        init(context);
    }

    public LevelSelectorLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mInflater = LayoutInflater.from(context);
        init(context);
    }

    public LevelSelectorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        init(context);
    }

    private void init(Context context) {
        View v = mInflater.inflate(R.layout.level_selector, this, true);

        ivRotor = (ImageView) v.findViewById(R.id.imv_level_rotor);

        tvLvlB1 = (TextView) v.findViewById(R.id.tv_level_b1);
        tvLvlB1.setOnClickListener(this);

        tvLvlB2 = (TextView) v.findViewById(R.id.tv_level_b2);
        tvLvlB2.setOnClickListener(this);

        tvLvlI1 = (TextView) v.findViewById(R.id.tv_level_i1);
        tvLvlI1.setOnClickListener(this);

        tvLvlI2 = (TextView) v.findViewById(R.id.tv_level_i2);
        tvLvlI2.setOnClickListener(this);

        tvLvlA1 = (TextView) v.findViewById(R.id.tv_level_a1);
        tvLvlA1.setOnClickListener(this);

        tvLvlA2 = (TextView) v.findViewById(R.id.tv_level_a2);
        tvLvlA2.setOnClickListener(this);

        levelSelectDetector = new GestureDetector(context, new LevelSelectDetector());
        ivRotor.setOnTouchListener(this);
        rotorImage = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_level_rotor);

        setRotorPosAngle(270);
    }

    public void setOnLevelSelectedListener(OnLevelSelectedListener listener) {
        mLevelSelectListener = listener;
    }

    private float cartesianToPolar(float x, float y) {
        return (float) -Math.toDegrees(Math.atan2(x - 0.5f, y - 0.5f));
    }

    private void setRotorPosAngle(float deg) {
        if (deg >= 270 || deg <= 90) {
            int wh = rotorImage.getWidth();
            String level = "";

            if (deg <= 90) deg += 90;
            if (deg >= 270) deg = Math.abs(270 - deg);

            if (deg <= 18) {
                deg = 0;
                level = "B1";
            } else if (deg > 18 && deg <= 54) {
                deg = 36;
                wh *= 1.4;
                level = "B2";
            } else if (deg > 54 && deg <= 90) {
                deg = 72;
                wh *= 1.26;
                level = "I1";
            } else if (deg > 90 && deg <= 126) {
                deg = 108;
                wh *= 1.26;
                level = "I2";
            } else if (deg > 126 && deg <= 162) {
                deg = 144;
                wh *= 1.4;
                level = "A1";
            } else if (deg > 162) {
                deg = 180;
                level = "A2";
            }

            if (!level.equals(mLastSelectedLevel)) {
                mLastSelectedLevel = level;
                changeSelectedColor(level);
                if (mLevelSelectListener != null) {
                    mLevelSelectListener.onSelected(level);
                }

                Matrix matrix = new Matrix();
                matrix.postRotate(deg);
                Bitmap rotatedBitmap = Bitmap.createBitmap(rotorImage, 0, 0, rotorImage.getWidth(), rotorImage.getHeight(), matrix, true);
                if (wh > rotorImage.getWidth()) {
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap, wh, wh, true);

                    int xy = (wh - rotorImage.getWidth()) / 2;
                    Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, xy, xy, rotorImage.getWidth(), rotorImage.getWidth());
                    ivRotor.setImageBitmap(croppedBitmap);
                } else {
                    ivRotor.setImageBitmap(rotatedBitmap);
                }
            }
        }
    }

    public void selectLevel(String level) {
        int posDegree = 270;
        switch (level) {
            case "B1":
                posDegree = 270;
                break;
            case "B2":
                posDegree = 306;
                break;
            case "I1":
                posDegree = 342;
                break;
            case "I2":
                posDegree = 18;
                break;
            case "A1":
                posDegree = 54;
                break;
            case "A2":
                posDegree = 90;
                break;
        }
        setRotorPosAngle(posDegree);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_level_b1:
                selectLevel("B1");
                break;
            case R.id.tv_level_b2:
                selectLevel("B2");
                break;
            case R.id.tv_level_i1:
                selectLevel("I1");
                break;
            case R.id.tv_level_i2:
                selectLevel("I2");
                break;
            case R.id.tv_level_a1:
                selectLevel("A1");
                break;
            case R.id.tv_level_a2:
                selectLevel("A2");
                break;
        }
    }

    private void changeSelectedColor(String level){
        tvLvlB1.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_gray));
        tvLvlB2.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_gray));
        tvLvlI1.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_gray));
        tvLvlI2.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_gray));
        tvLvlA1.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_gray));
        tvLvlA2.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_gray));

        switch (level) {
            case "B1":
                tvLvlB1.setTextColor(ContextCompat.getColor(getContext(), R.color.light_red));
                break;
            case "B2":
                tvLvlB2.setTextColor(ContextCompat.getColor(getContext(), R.color.light_red));
                break;
            case "I1":
                tvLvlI1.setTextColor(ContextCompat.getColor(getContext(), R.color.light_red));
                break;
            case "I2":
                tvLvlI2.setTextColor(ContextCompat.getColor(getContext(), R.color.light_red));
                break;
            case "A1":
                tvLvlA1.setTextColor(ContextCompat.getColor(getContext(), R.color.light_red));
                break;
            case "A2":
                tvLvlA2.setTextColor(ContextCompat.getColor(getContext(), R.color.light_red));
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
//        Log.e(TAG, "OnTouch");
        levelSelectDetector.onTouchEvent(motionEvent);
        return true;
    }

    private class LevelSelectDetector extends GestureDetector.SimpleOnGestureListener {

        public boolean onDown(MotionEvent event) {
//            Log.e(TAG, "OnDown");

            float x = event.getX() / ((float) getWidth());
            float y = event.getY() / ((float) getHeight());
            mAngleDown = cartesianToPolar(1 - x, 1 - y);// 1- to correct our custom axis direction
            return true;
        }

        public boolean onSingleTapUp(MotionEvent e) {
//            Log.e(TAG, "OnSingleTapUp");

            float x = e.getX() / ((float) getWidth());
            float y = e.getY() / ((float) getHeight());
            mAngleUp = cartesianToPolar(1 - x, 1 - y);// 1- to correct our custom axis direction

            // if we click up the same place where we clicked down, it's just a button press
            if (!Float.isNaN(mAngleDown) && !Float.isNaN(mAngleUp) && Math.abs(mAngleUp - mAngleDown) < 10) {
//                SetState(!mState);
//                if (m_listener != null) m_listener.onStateChange(mState);
            }
            return true;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            Log.e(TAG, "OnScroll");

            float x = e2.getX() / ((float) getWidth());
            float y = e2.getY() / ((float) getHeight());
            float rotDegrees = cartesianToPolar(1 - x, 1 - y);// 1- to correct our custom axis direction

            if (!Float.isNaN(rotDegrees)) {
                // instead of getting 0-> 180, -180 0 , we go for 0 -> 360
                float posDegrees = rotDegrees;
                if (rotDegrees < 0) posDegrees = 360 + rotDegrees;

                // deny full rotation, start start and stop point, and get a linear scale
                if (posDegrees >= 270 || posDegrees <= 90) {
                    // rotate our imageview
                    setRotorPosAngle(posDegrees);
                    // get a linear scale
//                    float scaleDegrees = rotDegrees + 150; // given the current parameters, we go from 0 to 300
                    // get position percent
//                    int percent = (int) (scaleDegrees / 3);
                    return true; //consumed
                } else
                    return false;
            } else
                return false; // not consumed
        }

    }

    public interface OnLevelSelectedListener {
        void onSelected(String level);
    }
}
