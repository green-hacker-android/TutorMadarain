package inc.osbay.android.tutormandarin.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Locale;

import inc.osbay.android.tutormandarin.R;

public class WhatIsPackageFragment extends BackHandledFragment {

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStack();
        return false;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_what_is_package, container, false);

        Toolbar toolBar;
        toolBar = v.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#E45F56"));
        setSupportActionBar(toolBar);

        ImageView imvWhatIsPackage1 = v.findViewById(R.id.imv_what_is_package_1);
        ImageView imvWhatIsPackage2 = v.findViewById(R.id.imv_what_is_package_2);
        ImageView imvWhatIsPackage3 = v.findViewById(R.id.imv_what_is_package_3);
        ImageView imvWhatIsPackage4 = v.findViewById(R.id.imv_what_is_package_4);
        ImageView imvWhatIsPackage5 = v.findViewById(R.id.imv_what_is_package_5);

        String locale = Locale.getDefault().getLanguage();
        if (locale.equals("th")) {
            imvWhatIsPackage1.setImageResource(R.drawable.pic_th_what_is_package_1);
            imvWhatIsPackage2.setImageResource(R.drawable.pic_th_what_is_package_2);
            imvWhatIsPackage3.setImageResource(R.drawable.pic_th_what_is_package_3);
            imvWhatIsPackage4.setImageResource(R.drawable.pic_th_what_is_package_4);
            imvWhatIsPackage5.setImageResource(R.drawable.pic_th_what_is_package_5);
        } else if (locale.equals("de")) {
            imvWhatIsPackage1.setImageResource(R.drawable.pic_de_what_is_package_1);
            imvWhatIsPackage2.setImageResource(R.drawable.pic_de_what_is_package_2);
            imvWhatIsPackage3.setImageResource(R.drawable.pic_de_what_is_package_3);
            imvWhatIsPackage4.setImageResource(R.drawable.pic_de_what_is_package_4);
            imvWhatIsPackage5.setImageResource(R.drawable.pic_de_what_is_package_5);
        } else if (locale.equals("fr")) {
            imvWhatIsPackage1.setImageResource(R.drawable.pic_fr_what_is_package_1);
            imvWhatIsPackage2.setImageResource(R.drawable.pic_fr_what_is_package_2);
            imvWhatIsPackage3.setImageResource(R.drawable.pic_fr_what_is_package_3);
            imvWhatIsPackage4.setImageResource(R.drawable.pic_fr_what_is_package_4);
            imvWhatIsPackage5.setImageResource(R.drawable.pic_fr_what_is_package_5);
        } else if (locale.equals("ja")) {
            imvWhatIsPackage1.setImageResource(R.drawable.pic_ja_what_is_package_1);
            imvWhatIsPackage2.setImageResource(R.drawable.pic_ja_what_is_package_2);
            imvWhatIsPackage3.setImageResource(R.drawable.pic_ja_what_is_package_3);
            imvWhatIsPackage4.setImageResource(R.drawable.pic_ja_what_is_package_4);
            imvWhatIsPackage5.setImageResource(R.drawable.pic_ja_what_is_package_5);
        } else if (locale.equals("ko")) {
            imvWhatIsPackage1.setImageResource(R.drawable.pic_ko_what_is_package_1);
            imvWhatIsPackage2.setImageResource(R.drawable.pic_ko_what_is_package_2);
            imvWhatIsPackage3.setImageResource(R.drawable.pic_ko_what_is_package_3);
            imvWhatIsPackage4.setImageResource(R.drawable.pic_ko_what_is_package_4);
            imvWhatIsPackage5.setImageResource(R.drawable.pic_ko_what_is_package_5);
        } else {
            imvWhatIsPackage1.setImageResource(R.drawable.pic_what_is_package_1);
            imvWhatIsPackage2.setImageResource(R.drawable.pic_what_is_package_2);
            imvWhatIsPackage3.setImageResource(R.drawable.pic_what_is_package_3);
            imvWhatIsPackage4.setImageResource(R.drawable.pic_what_is_package_4);
            imvWhatIsPackage5.setImageResource(R.drawable.pic_what_is_package_5);
        }

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        setTitle(getString(R.string.pk_what_title));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }
}
