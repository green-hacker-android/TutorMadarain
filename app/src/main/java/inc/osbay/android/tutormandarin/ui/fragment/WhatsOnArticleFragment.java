package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import inc.osbay.android.tutormandarin.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class WhatsOnArticleFragment extends Fragment {
    public static final String ARTICLE = "WhatsOnArticleFragment.ARTICLE";

    public WhatsOnArticleFragment() {
        // Required empty public constructor
    }

    public static WhatsOnArticleFragment newInstance(String mWhatsOnDetailArticle) {
        WhatsOnArticleFragment whatsOnArticleFragment = new WhatsOnArticleFragment();
        Bundle bund = new Bundle();
        bund.putString(ARTICLE, mWhatsOnDetailArticle);
        whatsOnArticleFragment.setArguments(bund);
        return whatsOnArticleFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_whats_on_article, container, false);

        String article = getArguments().getString(ARTICLE);

        TextView tvWhatsOnDetailArticle = (TextView) v.findViewById(R.id.tv_whats_on_detail_article);
        tvWhatsOnDetailArticle.setText(Html.fromHtml(article));
        tvWhatsOnDetailArticle.setMovementMethod(LinkMovementMethod.getInstance());

        return v;
    }
}
