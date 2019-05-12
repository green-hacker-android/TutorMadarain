package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import inc.osbay.android.tutormandarin.R;

public abstract class BackHandledFragment extends Fragment {
    protected BackHandlerInterface backHandlerInterface;

    public abstract boolean onBackPressed();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!(getActivity() instanceof BackHandlerInterface)) {
            throw new ClassCastException(
                    "Hosting activity must implement BackHandlerInterface");
        } else {
            backHandlerInterface = (BackHandlerInterface) getActivity();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Mark this fragment as the selected Fragment.
        backHandlerInterface.setmSelectedFragment(this);
    }

    public void setSupportActionBar(Toolbar toolbar) {
        AppCompatActivity compatActivity = (AppCompatActivity) getActivity();
        compatActivity.setSupportActionBar(toolbar);

        ActionBar actionBar = compatActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

    }

    public void setStatusBarColor(String color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
// clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
// finally change the color

            window.setStatusBarColor(Color.parseColor(color));
        }
    }

    public void setTitle(String title) {
        Toolbar toolbar = getActivity().findViewById(R.id.tool_bar);
        if (toolbar != null) {
            TextView tvTitle = toolbar.findViewById(R.id.tv_toolbar_title);
            tvTitle.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Montserrat-Bold.ttf"));
            tvTitle.setText(title);
            tvTitle.setCompoundDrawables(null, null, null, null);
            tvTitle.setOnClickListener(null);
        }
    }

    public void setTitle(String title, final OnTitleTextClickListener listener) {
        Toolbar toolbar = getActivity().findViewById(R.id.tool_bar);
        if (toolbar != null) {
            TextView tvTitle = toolbar.findViewById(R.id.tv_toolbar_title);
            tvTitle.setText(title);

            tvTitle.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    ContextCompat.getDrawable(getActivity(), R.drawable.ic_expand_arrow), null);

            tvTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onTitleTextClicked();
                }
            });
        }
    }

    public void showActionBar() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }

    public void hideActionBar() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    public void setDisplayHomeAsUpEnable(boolean status) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(status);
            actionBar.setHomeButtonEnabled(status);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cross_white);
        }
    }

    public interface BackHandlerInterface {
        void setmSelectedFragment(BackHandledFragment backHandledFragment);
    }

    public interface OnTitleTextClickListener {
        void onTitleTextClicked();
    }
}
