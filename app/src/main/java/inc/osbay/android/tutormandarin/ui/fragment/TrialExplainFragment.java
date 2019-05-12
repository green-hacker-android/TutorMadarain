package inc.osbay.android.tutormandarin.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Locale;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.ui.activity.MainActivity;

/**
 * Created by Erik on 10/24/2016.
 */
public class TrialExplainFragment extends BackHandledFragment {
    public static final String TAG = TrialExplainFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_trial_explain, container, false);
        ImageView imvFreeTrialProcess1 = v.findViewById(R.id.imv_free_trial_process_1);
        ImageView imvFreeTrialProcess2 = v.findViewById(R.id.imv_free_trial_process_2);
        ImageView imvFreeTrialProcess3 = v.findViewById(R.id.imv_free_trial_process_3);
        ImageView imvFreeTrialProcess4 = v.findViewById(R.id.imv_free_trial_process_4);
        ImageView imvFreeTrialProcess5 = v.findViewById(R.id.imv_free_trial_process_5);

        String locale = Locale.getDefault().getLanguage();
        if (locale.equals("th")) {
            imvFreeTrialProcess1.setImageResource(R.drawable.pic_th_free_trial_process_1);
            imvFreeTrialProcess2.setImageResource(R.drawable.pic_th_free_trial_process_2);
            imvFreeTrialProcess3.setImageResource(R.drawable.pic_th_free_trial_process_3);
            imvFreeTrialProcess4.setImageResource(R.drawable.pic_th_free_trial_process_4);
            imvFreeTrialProcess5.setImageResource(R.drawable.pic_th_free_trial_process_5);
        } else if (locale.equals("de")) {
            imvFreeTrialProcess1.setImageResource(R.drawable.pic_de_free_trial_process_1);
            imvFreeTrialProcess2.setImageResource(R.drawable.pic_de_free_trial_process_2);
            imvFreeTrialProcess3.setImageResource(R.drawable.pic_de_free_trial_process_3);
            imvFreeTrialProcess4.setImageResource(R.drawable.pic_de_free_trial_process_4);
            imvFreeTrialProcess5.setImageResource(R.drawable.pic_de_free_trial_process_5);
        } else if (locale.equals("fr")) {
            imvFreeTrialProcess1.setImageResource(R.drawable.pic_fr_free_trial_process_1);
            imvFreeTrialProcess2.setImageResource(R.drawable.pic_fr_free_trial_process_2);
            imvFreeTrialProcess3.setImageResource(R.drawable.pic_fr_free_trial_process_3);
            imvFreeTrialProcess4.setImageResource(R.drawable.pic_fr_free_trial_process_4);
            imvFreeTrialProcess5.setImageResource(R.drawable.pic_fr_free_trial_process_5);
        } else if (locale.equals("ja")) {
            imvFreeTrialProcess1.setImageResource(R.drawable.pic_ja_free_trial_process_1);
            imvFreeTrialProcess2.setImageResource(R.drawable.pic_ja_free_trial_process_2);
            imvFreeTrialProcess3.setImageResource(R.drawable.pic_ja_free_trial_process_3);
            imvFreeTrialProcess4.setImageResource(R.drawable.pic_ja_free_trial_process_4);
            imvFreeTrialProcess5.setImageResource(R.drawable.pic_ja_free_trial_process_5);
        } else if (locale.equals("ko")) {
            imvFreeTrialProcess1.setImageResource(R.drawable.pic_ko_free_trial_process_1);
            imvFreeTrialProcess2.setImageResource(R.drawable.pic_ko_free_trial_process_2);
            imvFreeTrialProcess3.setImageResource(R.drawable.pic_ko_free_trial_process_3);
            imvFreeTrialProcess4.setImageResource(R.drawable.pic_ko_free_trial_process_4);
            imvFreeTrialProcess5.setImageResource(R.drawable.pic_ko_free_trial_process_5);
        } else {
            imvFreeTrialProcess1.setImageResource(R.drawable.pic_free_trial_process_1);
            imvFreeTrialProcess2.setImageResource(R.drawable.pic_free_trial_process_2);
            imvFreeTrialProcess3.setImageResource(R.drawable.pic_free_trial_process_3);
            imvFreeTrialProcess4.setImageResource(R.drawable.pic_free_trial_process_4);
            imvFreeTrialProcess5.setImageResource(R.drawable.pic_free_trial_process_5);
        }

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        showActionBar();
        setHasOptionsMenu(true);
        setTitle(getString(R.string.free_trail_title));
        setDisplayHomeAsUpEnable(true);
    }

    @Override
    public boolean onBackPressed() {
        Log.e(TAG, "Back stack entry count - " + getFragmentManager().getBackStackEntryCount());
        Intent mainIntent = new Intent(getActivity(), MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("class_type", "normal");
        mainIntent.putExtras(bundle);
        mainIntent.setAction("refresh_fragment");
        startActivity(mainIntent);
        getActivity().finish();
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }
}
