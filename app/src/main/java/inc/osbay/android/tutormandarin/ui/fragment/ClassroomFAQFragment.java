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
import android.widget.LinearLayout;

import com.flurry.android.FlurryAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import inc.osbay.android.tutormandarin.R;

public class ClassroomFAQFragment extends BackHandledFragment {

    public String classType;
    @BindView(R.id.arrow1)
    ImageView arrow1;
    @BindView(R.id.arrow2)
    ImageView arrow2;
    @BindView(R.id.arrow3)
    ImageView arrow3;
    @BindView(R.id.arrow4)
    ImageView arrow4;
    @BindView(R.id.arrow5)
    ImageView arrow5;
    @BindView(R.id.arrow6)
    ImageView arrow6;
    @BindView(R.id.arrow7)
    ImageView arrow7;
    @BindView(R.id.arrow8)
    ImageView arrow8;
    @BindView(R.id.arrow9)
    ImageView arrow9;
    @BindView(R.id.arrow10)
    ImageView arrow10;
    @BindView(R.id.arrow11)
    ImageView arrow11;
    @BindView(R.id.arrow12)
    ImageView arrow12;
    @BindView(R.id.arrow13)
    ImageView arrow13;
    @BindView(R.id.arrow14)
    ImageView arrow14;
    @BindView(R.id.arrow15)
    ImageView arrow15;
    @BindView(R.id.arrow16)
    ImageView arrow16;
    @BindView(R.id.arrow17)
    ImageView arrow17;
    @BindView(R.id.arrow18)
    ImageView arrow18;
    @BindView(R.id.faq1_content_ll)
    LinearLayout faq1ContentLL;
    @BindView(R.id.faq2_content_ll)
    LinearLayout faq2ContentLL;
    @BindView(R.id.faq3_content_ll)
    LinearLayout faq3ContentLL;
    @BindView(R.id.faq4_content_ll)
    LinearLayout faq4ContentLL;
    @BindView(R.id.faq5_content_ll)
    LinearLayout faq5ContentLL;
    @BindView(R.id.faq6_content_ll)
    LinearLayout faq6ContentLL;
    @BindView(R.id.faq7_content_ll)
    LinearLayout faq7ContentLL;
    @BindView(R.id.faq8_content_ll)
    LinearLayout faq8ContentLL;
    @BindView(R.id.faq9_content_ll)
    LinearLayout faq9ContentLL;
    @BindView(R.id.faq10_content_ll)
    LinearLayout faq10ContentLL;
    @BindView(R.id.faq11_content_ll)
    LinearLayout faq11ContentLL;
    @BindView(R.id.faq12_content_ll)
    LinearLayout faq12ContentLL;
    @BindView(R.id.faq13_content_ll)
    LinearLayout faq13ContentLL;
    @BindView(R.id.faq14_content_ll)
    LinearLayout faq14ContentLL;
    @BindView(R.id.faq15_content_ll)
    LinearLayout faq15ContentLL;
    @BindView(R.id.faq16_content_ll)
    LinearLayout faq16ContentLL;
    @BindView(R.id.faq17_content_ll)
    LinearLayout faq17ContentLL;
    @BindView(R.id.faq18_content_ll)
    LinearLayout faq18ContentLL;
    @BindView(R.id.tool_bar)
    Toolbar toolBar;
    boolean[] isShow = new boolean[18];

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        classType = getArguments().getString("class_type");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_classroom_faq, container, false);
        ButterKnife.bind(this, view);

        toolBar.setBackgroundColor(Color.parseColor("#00ABA0"));
        setSupportActionBar(toolBar);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
        setTitle(getString(R.string.ac_faq));
        setDisplayHomeAsUpEnable(true);
    }

    @Override
    public boolean onBackPressed() {
        FlurryAgent.logEvent("Classroom FAQ back");
        getFragmentManager().popBackStack();
        if (classType.equalsIgnoreCase("classroom")) {
            getActivity().finish();
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return false;
    }

    @OnClick(R.id.faq1_rl)
    void clickFaq1() {
        if (isShow[0] == false) {
            isShow[0] = true;
            faq1ContentLL.setVisibility(View.VISIBLE);
            arrow1.setImageDrawable(getContext().getDrawable(R.drawable.ic_resume_up_arrow));
        } else {
            isShow[0] = false;
            faq1ContentLL.setVisibility(View.GONE);
            arrow1.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_head_down_black));
        }
    }

    @OnClick(R.id.faq2_rl)
    void clickFaq2() {
        if (isShow[1] == false) {
            isShow[1] = true;
            faq2ContentLL.setVisibility(View.VISIBLE);
            arrow2.setImageDrawable(getContext().getDrawable(R.drawable.ic_resume_up_arrow));
        } else {
            isShow[1] = false;
            faq2ContentLL.setVisibility(View.GONE);
            arrow2.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_head_down_black));
        }
    }

    @OnClick(R.id.faq3_rl)
    void clickFaq3() {
        if (isShow[2] == false) {
            isShow[2] = true;
            faq3ContentLL.setVisibility(View.VISIBLE);
            arrow3.setImageDrawable(getContext().getDrawable(R.drawable.ic_resume_up_arrow));
        } else {
            isShow[3] = false;
            faq3ContentLL.setVisibility(View.GONE);
            arrow3.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_head_down_black));
        }
    }

    @OnClick(R.id.faq4_rl)
    void clickFaq4() {
        if (isShow[3] == false) {
            isShow[3] = true;
            faq4ContentLL.setVisibility(View.VISIBLE);
            arrow4.setImageDrawable(getContext().getDrawable(R.drawable.ic_resume_up_arrow));
        } else {
            isShow[3] = false;
            faq4ContentLL.setVisibility(View.GONE);
            arrow4.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_head_down_black));
        }
    }

    @OnClick(R.id.faq5_rl)
    void clickFaq5() {
        if (isShow[4] == false) {
            isShow[4] = true;
            faq5ContentLL.setVisibility(View.VISIBLE);
            arrow5.setImageDrawable(getContext().getDrawable(R.drawable.ic_resume_up_arrow));
        } else {
            isShow[4] = false;
            faq5ContentLL.setVisibility(View.GONE);
            arrow5.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_head_down_black));
        }
    }

    @OnClick(R.id.faq6_rl)
    void clickFaq6() {
        if (isShow[5] == false) {
            isShow[5] = true;
            faq6ContentLL.setVisibility(View.VISIBLE);
            arrow6.setImageDrawable(getContext().getDrawable(R.drawable.ic_resume_up_arrow));
        } else {
            isShow[5] = false;
            faq6ContentLL.setVisibility(View.GONE);
            arrow6.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_head_down_black));
        }
    }

    @OnClick(R.id.faq7_rl)
    void clickFaq7() {
        if (isShow[6] == false) {
            isShow[6] = true;
            faq7ContentLL.setVisibility(View.VISIBLE);
            arrow7.setImageDrawable(getContext().getDrawable(R.drawable.ic_resume_up_arrow));
        } else {
            isShow[6] = false;
            faq7ContentLL.setVisibility(View.GONE);
            arrow7.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_head_down_black));
        }
    }

    @OnClick(R.id.faq8_rl)
    void clickFaq8() {
        if (isShow[7] == false) {
            isShow[7] = true;
            faq8ContentLL.setVisibility(View.VISIBLE);
            arrow8.setImageDrawable(getContext().getDrawable(R.drawable.ic_resume_up_arrow));
        } else {
            isShow[7] = false;
            faq8ContentLL.setVisibility(View.GONE);
            arrow8.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_head_down_black));
        }
    }

    @OnClick(R.id.faq9_rl)
    void clickFaq9() {
        if (isShow[8] == false) {
            isShow[8] = true;
            faq9ContentLL.setVisibility(View.VISIBLE);
            arrow9.setImageDrawable(getContext().getDrawable(R.drawable.ic_resume_up_arrow));
        } else {
            isShow[8] = false;
            faq9ContentLL.setVisibility(View.GONE);
            arrow9.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_head_down_black));
        }
    }

    @OnClick(R.id.faq10_rl)
    void clickFaq10() {
        if (isShow[9] == false) {
            isShow[9] = true;
            faq10ContentLL.setVisibility(View.VISIBLE);
            arrow10.setImageDrawable(getContext().getDrawable(R.drawable.ic_resume_up_arrow));
        } else {
            isShow[9] = false;
            faq10ContentLL.setVisibility(View.GONE);
            arrow10.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_head_down_black));
        }
    }

    @OnClick(R.id.faq11_rl)
    void clickFaq11() {
        if (isShow[10] == false) {
            isShow[10] = true;
            faq11ContentLL.setVisibility(View.VISIBLE);
            arrow11.setImageDrawable(getContext().getDrawable(R.drawable.ic_resume_up_arrow));
        } else {
            isShow[10] = false;
            faq11ContentLL.setVisibility(View.GONE);
            arrow11.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_head_down_black));
        }
    }

    @OnClick(R.id.faq12_rl)
    void clickFaq12() {
        if (isShow[11] == false) {
            isShow[11] = true;
            faq12ContentLL.setVisibility(View.VISIBLE);
            arrow12.setImageDrawable(getContext().getDrawable(R.drawable.ic_resume_up_arrow));
        } else {
            isShow[11] = false;
            faq12ContentLL.setVisibility(View.GONE);
            arrow12.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_head_down_black));
        }
    }

    @OnClick(R.id.faq13_rl)
    void clickFaq13() {
        if (isShow[12] == false) {
            isShow[12] = true;
            faq13ContentLL.setVisibility(View.VISIBLE);
            arrow13.setImageDrawable(getContext().getDrawable(R.drawable.ic_resume_up_arrow));
        } else {
            isShow[12] = false;
            faq13ContentLL.setVisibility(View.GONE);
            arrow13.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_head_down_black));
        }
    }

    @OnClick(R.id.faq14_rl)
    void clickFaq14() {
        if (isShow[13] == false) {
            isShow[13] = true;
            faq14ContentLL.setVisibility(View.VISIBLE);
            arrow14.setImageDrawable(getContext().getDrawable(R.drawable.ic_resume_up_arrow));
        } else {
            isShow[13] = false;
            faq14ContentLL.setVisibility(View.GONE);
            arrow14.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_head_down_black));
        }
    }

    @OnClick(R.id.faq15_rl)
    void clickFaq15() {
        if (isShow[14] == false) {
            isShow[14] = true;
            faq15ContentLL.setVisibility(View.VISIBLE);
            arrow15.setImageDrawable(getContext().getDrawable(R.drawable.ic_resume_up_arrow));
        } else {
            isShow[14] = false;
            faq15ContentLL.setVisibility(View.GONE);
            arrow15.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_head_down_black));
        }
    }

    @OnClick(R.id.faq16_rl)
    void clickFaq16() {
        if (isShow[15] == false) {
            isShow[15] = true;
            faq16ContentLL.setVisibility(View.VISIBLE);
            arrow16.setImageDrawable(getContext().getDrawable(R.drawable.ic_resume_up_arrow));
        } else {
            isShow[15] = false;
            faq16ContentLL.setVisibility(View.GONE);
            arrow16.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_head_down_black));
        }
    }

    @OnClick(R.id.faq17_rl)
    void clickFaq17() {
        if (isShow[16] == false) {
            isShow[16] = true;
            faq17ContentLL.setVisibility(View.VISIBLE);
            arrow17.setImageDrawable(getContext().getDrawable(R.drawable.ic_resume_up_arrow));
        } else {
            isShow[16] = false;
            faq17ContentLL.setVisibility(View.GONE);
            arrow17.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_head_down_black));
        }
    }

    @OnClick(R.id.faq18_rl)
    void clickFaq18() {
        if (isShow[17] == false) {
            isShow[17] = true;
            faq18ContentLL.setVisibility(View.VISIBLE);
            arrow18.setImageDrawable(getContext().getDrawable(R.drawable.ic_resume_up_arrow));
        } else {
            isShow[17] = false;
            faq18ContentLL.setVisibility(View.GONE);
            arrow18.setImageDrawable(getContext().getDrawable(R.drawable.ic_arrow_head_down_black));
        }
    }
}
