package inc.osbay.android.tutormandarin.ui.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import inc.osbay.android.tutormandarin.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class FlashcardTutorialFragment extends Fragment {
    private static final String PAGE_NO = "FlashcardTutorialFragment.PAGE_NO";

    public FlashcardTutorialFragment() {
    }

    public static FlashcardTutorialFragment newInstance(String text) {

        FlashcardTutorialFragment f = new FlashcardTutorialFragment();
        Bundle b = new Bundle();
        b.putString(PAGE_NO, text);

        f.setArguments(b);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_flashcard_tutorial, container, false);

        ImageView imvFlashcardIcon = (ImageView) v.findViewById(R.id.imv_flashcard_icon);
        TextView tvFlashcardMessage = (TextView) v.findViewById(R.id.tv_flashcard_message);
        ImageView imvFlashcardTutorialClose = (ImageView) v.findViewById(R.id.imv_flashcard_tutorial_close);
        tvFlashcardMessage.setMovementMethod(new ScrollingMovementMethod());

        imvFlashcardTutorialClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        String pgNo = getArguments().getString(PAGE_NO);

        if (pgNo != null) {
            if (pgNo.equals("1")) {
                imvFlashcardIcon.setImageResource(R.drawable.ic_flashcard_tutorial_1);
                tvFlashcardMessage.setText(R.string.fc_explain_1);
            }
            if (pgNo.equals("2")) {
                imvFlashcardIcon.setImageResource(R.drawable.ic_flashcard_tutorial_2);
                tvFlashcardMessage.setText(R.string.fc_explain_2);
            }
            if (pgNo.equals("3")) {
                imvFlashcardIcon.setImageResource(R.drawable.ic_flashcard_tutorial_3);
                tvFlashcardMessage.setText(R.string.fc_explain_3);
            }
        }

        return v;
    }

}
