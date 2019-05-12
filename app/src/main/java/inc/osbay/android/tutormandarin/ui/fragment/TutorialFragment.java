package inc.osbay.android.tutormandarin.ui.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import inc.osbay.android.tutormandarin.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class TutorialFragment extends BackHandledFragment implements View.OnClickListener {
    public static final String PREF_SCHEDULE_TUTORIAL = "TutorialFragment.PREF_SCHEDULE_TUTORIAL";
    public static final String PREF_TUTOR_TUTORIAL = "TutorialFragment.PREF_TUTOR_TUTORIAL";
    public static final String PREF_BOOKING_TUTORIAL = "TutorialFragment.PREF_BOOKING_TUTORIAL";
    public static final String PREF_BOOKING_PACKAGE_TUTORIAL = "TutorialFragment.PREF_BOOKING_PACKAGE_TUTORIAL";
    public static final String TUTORIAL_NO = "TutorialFragment.TUTORIAL_NO";
    public static final String TRIAL_REQUEST = "TutorialFragment.TRIAL_REQUEST";
    int clickno = 0;
    int clicktype = 0;
    private ImageView imvTutorial;

    public TutorialFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        setTitle("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tutorial, container, false);
        imvTutorial = v.findViewById(R.id.imv_tutorial);
        imvTutorial.setOnClickListener(this);
        SharedPreferences sPreferences = getActivity().getSharedPreferences(PREF_SCHEDULE_TUTORIAL, Context.MODE_PRIVATE);
        SharedPreferences tPreferences = getActivity().getSharedPreferences(PREF_TUTOR_TUTORIAL, Context.MODE_PRIVATE);
        SharedPreferences bPreferences = getActivity().getSharedPreferences(PREF_BOOKING_TUTORIAL, Context.MODE_PRIVATE);
        SharedPreferences bpPreferences = getActivity().getSharedPreferences(PREF_BOOKING_PACKAGE_TUTORIAL, Context.MODE_PRIVATE);
        SharedPreferences trPreferences = getActivity().getSharedPreferences(TRIAL_REQUEST, Context.MODE_PRIVATE);
        int scheduleNo = getActivity().getIntent().getIntExtra(TUTORIAL_NO, 0);

        if (scheduleNo == 1) {
            sPreferences.edit().putInt(PREF_SCHEDULE_TUTORIAL, 1).apply();
            imvTutorial.setImageDrawable(getResources().getDrawable(R.drawable.bg_schedule_go_through_01));
            clickno = 1;
            clicktype = 1;
        } else if (scheduleNo == 2) {
            tPreferences.edit().putInt(PREF_TUTOR_TUTORIAL, 2).apply();
            imvTutorial.setImageDrawable(getResources().getDrawable(R.drawable.bg_booking_go_through_01));
            clickno = 1;
            clicktype = 2;
        } else if (scheduleNo == 3) {
            bPreferences.edit().putInt(PREF_BOOKING_TUTORIAL, 3).apply();
            imvTutorial.setImageDrawable(getResources().getDrawable(R.drawable.bg_booking_go_through_04));
            clickno = 1;
            clicktype = 3;
        } else if (scheduleNo == 4) {
            bpPreferences.edit().putInt(PREF_BOOKING_PACKAGE_TUTORIAL, 4).apply();
            imvTutorial.setImageDrawable(getResources().getDrawable(R.drawable.bg_package_booking_01));
            clickno = 1;
            clicktype = 4;
        } else if (scheduleNo == 5) {
            trPreferences.edit().putInt(TRIAL_REQUEST, 5).apply();
            imvTutorial.setImageDrawable(getResources().getDrawable(R.drawable.trial_explain_1));
            clickno = 1;
            clicktype = 5;
        }
        return v;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imv_tutorial:
                if (clicktype == 1) {
                    if (clickno == 1) {
                        imvTutorial.setImageDrawable(getResources().getDrawable(R.drawable.bg_schedule_go_through_02));
                        clickno = 2;
                    } else {
                        getActivity().finish();
                    }
                } else if (clicktype == 2) {
                    if (clickno == 1) {
                        imvTutorial.setImageDrawable(getResources().getDrawable(R.drawable.bg_booking_go_through_02));
                        clickno = 2;
                    } else if (clickno == 2) {
                        imvTutorial.setImageDrawable(getResources().getDrawable(R.drawable.bg_booking_go_through_03));
                        clickno = 3;
                    } else {
                        getActivity().finish();
                    }
                } else if (clicktype == 3) {
                    if (clickno == 1) {
                        getActivity().finish();
                    }
                } else if (clicktype == 4) {
                    if (clickno == 1) {
                        imvTutorial.setImageDrawable(getResources().getDrawable(R.drawable.bg_package_booking_02));
                        clickno = 2;
                    } else if (clickno == 2) {
                        imvTutorial.setImageDrawable(getResources().getDrawable(R.drawable.bg_package_booking_03));
                        clickno = 3;
                    } else if (clickno == 3) {
                        imvTutorial.setImageDrawable(getResources().getDrawable(R.drawable.bg_package_booking_04));
                        clickno = 4;
                    } else {
                        getActivity().finish();
                    }
                } else if (clicktype == 5) {
                    if (clickno == 1) {
                        imvTutorial.setImageDrawable(getResources().getDrawable(R.drawable.trial_explain_2));
                        clickno = 2;
                    } else {
                        getActivity().finish();
                    }
                }
                break;
        }
    }
}
