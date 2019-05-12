package inc.osbay.android.tutormandarin.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.Map;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.ui.activity.WebViewActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class PackageTutorialFragment extends Fragment implements View.OnClickListener {
    private static final String PAGE_NO = "PackageTutorialFragment.PAGE_NO";
    String pgNo;

    public PackageTutorialFragment() {
        // Required empty public constructor
    }

    public static PackageTutorialFragment newInstance(String text) {

        PackageTutorialFragment f = new PackageTutorialFragment();
        Bundle b = new Bundle();
        b.putString(PAGE_NO, text);

        f.setArguments(b);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_package_tutorial, container, false);

        ImageView imvTutorialIcon = (ImageView) v.findViewById(R.id.imv_tutorial_icon);
        TextView tvBookingRulesTitle = (TextView) v.findViewById(R.id.tv_booking_rules_title);
        TextView tvBookingRulesMessage = (TextView) v.findViewById(R.id.tv_booking_rules_message);
        tvBookingRulesMessage.setMovementMethod(new ScrollingMovementMethod());
        ImageView imvPackageTutorialClose = (ImageView) v.findViewById(R.id.imv_package_tutorial_close);
        imvPackageTutorialClose.setOnClickListener(this);
        Button btnStartTrail = (Button) v.findViewById(R.id.btn_start_trial);
        TextView mTvLearnMore = (TextView) v.findViewById(R.id.tv_learn_more);
        mTvLearnMore.setOnClickListener(this);

        pgNo = getArguments().getString(PAGE_NO);

        if (pgNo != null) {
            if (pgNo.equals("1")) {
                imvTutorialIcon.setImageResource(R.drawable.ic_booking_rules);
                tvBookingRulesTitle.setText(getString(R.string.pk_booking_rule_title));
                tvBookingRulesMessage.setText(getString(R.string.pk_booking_rule));
                btnStartTrail.setVisibility(View.GONE);
                mTvLearnMore.setVisibility(View.INVISIBLE);
            }
            if (pgNo.equals("2")) {
                imvTutorialIcon.setImageResource(R.drawable.ic_course_structure);
                tvBookingRulesTitle.setText(getString(R.string.pk_course_structure_title));
                tvBookingRulesMessage.setText(getString(R.string.pk_course_structure));
                btnStartTrail.setVisibility(View.GONE);
                mTvLearnMore.setVisibility(View.INVISIBLE);
            }
            if (pgNo.equals("3")) {
                imvTutorialIcon.setImageResource(R.drawable.ic_ticket_design);
                tvBookingRulesTitle.setText(getString(R.string.pk_ticket_design_title));
                tvBookingRulesMessage.setText(getString(R.string.pk_ticket_design));
                btnStartTrail.setVisibility(View.GONE);
                mTvLearnMore.setVisibility(View.INVISIBLE);
            }
            if (pgNo.equals("4")) {
                imvTutorialIcon.setImageResource(R.drawable.ic_auto_repeat);
                tvBookingRulesTitle.setText(getString(R.string.pk_auto_repeat_title));
                tvBookingRulesMessage.setText(getString(R.string.pk_auto_repeat));
                btnStartTrail.setVisibility(View.VISIBLE);
                mTvLearnMore.setVisibility(View.VISIBLE);
            }
        }

        btnStartTrail.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imv_package_tutorial_close:
                Map<String, String> params = new HashMap<>();
                params.put("Last Page", String.valueOf(pgNo));

                FlurryAgent.logEvent("Skip package booking rules", params);

                if (getActivity() != null)
                    getActivity().finish();
                break;
            case R.id.tv_learn_more:
                Intent aboutUsIntent = new Intent(getActivity(), WebViewActivity.class);
                aboutUsIntent.putExtra(WebViewActivity.EXTRA_WEB_URL, "http://www.tutormandarin.net/faq-package-booking-and-scheduling/");
                startActivity(aboutUsIntent);
                break;
            case R.id.btn_start_trial:
                FlurryAgent.logEvent("Read all package booking rules");

                if (getActivity() != null)
                    getActivity().finish();
                break;
        }
    }
}
