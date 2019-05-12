package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.model.FlashCardDeck;
import inc.osbay.android.tutormandarin.sdk.model.WhatsOn;
import inc.osbay.android.tutormandarin.sdk.model.WhatsOnVocab;
import inc.osbay.android.tutormandarin.ui.activity.FragmentHolderActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class WhatsOnGrammarVocabFragment extends Fragment {
    private static final String WHATS_ON_ID = "WhatsOnGrammarVocabFragment.WHATS_ON_ID";
    private static final String WHATS_ON_TYPE = "WhatsOnGrammarVocabFragment.WHATS_ON_TYPE";

    private WhatsOn mWhatsOn;
    private List<WhatsOnVocab> mWhatsOnVocabs;
    private List<String> mWhatsOnGrammars;

    private int mType;

    public WhatsOnGrammarVocabFragment() {
        // Required empty public constructor
    }

    public static WhatsOnGrammarVocabFragment newInstance(String whatsOnId, int type) {
        WhatsOnGrammarVocabFragment fragment = new WhatsOnGrammarVocabFragment();
        Bundle bundle = new Bundle();
        bundle.putString(WHATS_ON_ID, whatsOnId);
        bundle.putInt(WHATS_ON_TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String mWhatsOnId = getArguments().getString(WHATS_ON_ID);
        mType = getArguments().getInt(WHATS_ON_TYPE);
        CurriculumAdapter mCurriculumAdapter = new CurriculumAdapter(getActivity());

        mWhatsOn = mCurriculumAdapter.getWhatsOnById(mWhatsOnId);
        mWhatsOnGrammars = mCurriculumAdapter.retrieveWhatsOnGrammar(mWhatsOnId);
        mWhatsOnVocabs = mCurriculumAdapter.retrieveWhatsOnVocab(mWhatsOnId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_whats_on_grammar_vocab, container, false);
        RecyclerView mRvWhatsOnGrammarVocab = (RecyclerView) v.findViewById(R.id.rv_whats_on_grammar_vocab);
        LinearLayoutManager llmWhatsOnGrammar = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRvWhatsOnGrammarVocab.setLayoutManager(llmWhatsOnGrammar);
        WhatsOnGrammarVocabAdapter mWhatsOnGrammarVocabAdapter = new WhatsOnGrammarVocabAdapter();
        mRvWhatsOnGrammarVocab.setAdapter(mWhatsOnGrammarVocabAdapter);

        TextView mTvGrammarVocabEmpty = (TextView) v.findViewById(R.id.tv_grammar_vocab_empty);

        if (mWhatsOnVocabs.size() == 0 && mType == 1) {
            mRvWhatsOnGrammarVocab.setVisibility(View.GONE);
            mTvGrammarVocabEmpty.setVisibility(View.VISIBLE);
            mTvGrammarVocabEmpty.setText(getString(R.string.wo_no_vocab));
        } else if (mWhatsOnGrammars.size() == 0 && mType == 2) {
            mRvWhatsOnGrammarVocab.setVisibility(View.GONE);
            mTvGrammarVocabEmpty.setVisibility(View.VISIBLE);
            mTvGrammarVocabEmpty.setText(getString(R.string.wo_no_grammar));
        } else {
            mRvWhatsOnGrammarVocab.setVisibility(View.VISIBLE);
            mTvGrammarVocabEmpty.setVisibility(View.GONE);
        }
        return v;
    }

    private class WhatsOnGrammarVocabAdapter extends RecyclerView.Adapter<WhatsOnGrammarVocabAdapter.WhatsOnGrammarVocabHolder> {

        @Override
        public WhatsOnGrammarVocabHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_whats_on_grammar_vocab, parent, false);
            return new WhatsOnGrammarVocabHolder(v);
        }

        @Override
        public void onBindViewHolder(final WhatsOnGrammarVocabHolder holder, int position) {
            if (mType == 1) {
                final WhatsOnVocab vocab = mWhatsOnVocabs.get(position);

                holder.tvWhatsOnGrammarVocabTitle.setText(vocab.getChatacter());

                String definition = String.format(Locale.getDefault(), "%s / %s / %s",
                        vocab.getPartOfSpeech(), vocab.getPinyin(), vocab.getDefinition());
                holder.tvWhatsOnGrammarVocab.setText(definition);

                holder.vwWhatsOnGrammarVocab.setVisibility(View.VISIBLE);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FlashCardDeck deck = new FlashCardDeck();
                        deck.setCardTitle(mWhatsOn.getTitle());
                        deck.setType(FlashCardDeck.Type.WHATS_ON_VOCAB);
                        deck.setCourseId(mWhatsOn.getWhatsOnId());

                        Intent intent = new Intent(getActivity(), FragmentHolderActivity.class);
                        intent.putExtra(FragmentHolderActivity.EXTRA_DISPLAY_FRAGMENT,
                                FlashCardPagerFragment.class.getSimpleName());
                        intent.putExtra(FlashCardPagerFragment.EXTRA_FLASHCARD_DECK, deck);
                        intent.putExtra(FlashCardPagerFragment.EXTRA_CARD_POSITION, holder.getAdapterPosition());
                        startActivity(intent);
                    }
                });
            } else {
                holder.tvWhatsOnGrammarVocabTitle.setText(Html.fromHtml(mWhatsOnGrammars.get(position)));
                holder.vwWhatsOnGrammarVocab.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            if (mType == 1) {
                return mWhatsOnVocabs.size();
            } else {
                return mWhatsOnGrammars.size();
            }
        }

        class WhatsOnGrammarVocabHolder extends RecyclerView.ViewHolder {
            TextView tvWhatsOnGrammarVocabTitle;
            TextView tvWhatsOnGrammarVocab;
            View vwWhatsOnGrammarVocab;

            public WhatsOnGrammarVocabHolder(View itemView) {
                super(itemView);
                tvWhatsOnGrammarVocabTitle = (TextView) itemView.findViewById(R.id.tv_whats_on_grammar_vocab_title);
                tvWhatsOnGrammarVocab = (TextView) itemView.findViewById(R.id.tv_whats_on_grammar_vocab);
                vwWhatsOnGrammarVocab = itemView.findViewById(R.id.vw_whats_on_grammar_vocab);
            }
        }
    }
}
