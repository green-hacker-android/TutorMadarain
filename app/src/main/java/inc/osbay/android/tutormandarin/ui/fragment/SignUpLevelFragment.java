package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.kyleduo.switchbutton.SwitchButton;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.ui.activity.SignUpContinueActivity;
import inc.osbay.android.tutormandarin.ui.view.LevelSelectorLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpLevelFragment extends Fragment {

    private static final String TAG = SignUpLevelFragment.class.getSimpleName();

    private TextView tvLevelTitle;
    private TextView tvLevelDescription;

    private SignUpContinueActivity parentActivity;
    private SwitchButton userLevelSwitch;

    public SignUpLevelFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FlurryAgent.logEvent("SignUp Level");
        View rootView = inflater.inflate(R.layout.fragment_sign_up_level, container, false);

        parentActivity = (SignUpContinueActivity) getActivity();

        tvLevelTitle = (TextView) rootView.findViewById(R.id.tv_level_title);
        tvLevelDescription = (TextView) rootView.findViewById(R.id.tv_level_desc);

        userLevelSwitch = rootView.findViewById(R.id.level_switch);
        userLevelSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b == true) {
                    parentActivity.courseCategory = "2";
                } else {
                    parentActivity.courseCategory = "1";
                }
            }
        });

        LevelSelectorLayout levelSelector = (LevelSelectorLayout) rootView.findViewById(R.id.lsl_level);
        levelSelector.setOnLevelSelectedListener(new LevelSelectorLayout.OnLevelSelectedListener() {
            @Override
            public void onSelected(String level) {
                Log.e(TAG, "Selected level = " + level);
                switch (level) {
                    case "B1":
                        parentActivity.level = "B1";

                        tvLevelTitle.setText(getString(R.string.su_beginner_1));
                        tvLevelDescription.setText(getString(R.string.su_beginner_1_desc));
                        break;
                    case "B2":
                        parentActivity.level = "B2";

                        tvLevelTitle.setText(getString(R.string.su_beginner_2));
                        tvLevelDescription.setText(getString(R.string.su_beginner_2_desc));
                        break;
                    case "I1":
                        parentActivity.level = "I1";

                        tvLevelTitle.setText(getString(R.string.su_intermediate_1));
                        tvLevelDescription.setText(getString(R.string.su_intermediate_1_desc));
                        break;
                    case "I2":
                        parentActivity.level = "I2";

                        tvLevelTitle.setText(getString(R.string.su_intermediate_2));
                        tvLevelDescription.setText(getString(R.string.su_intermediate_2_desc));
                        break;
                    case "A1":
                        parentActivity.level = "A1";

                        tvLevelTitle.setText(getString(R.string.su_advanced_1));
                        tvLevelDescription.setText(getString(R.string.su_advanced_1_desc));
                        break;
                    case "A2":
                        parentActivity.level = "A2";

                        tvLevelTitle.setText(getString(R.string.su_advanced_2));
                        tvLevelDescription.setText(getString(R.string.su_advanced_2_desc));
                        break;
                }
            }
        });

        levelSelector.selectLevel(parentActivity.level);
        tvLevelTitle.setText(getString(R.string.su_beginner_1));
        tvLevelDescription.setText(getString(R.string.su_beginner_1_desc));

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
