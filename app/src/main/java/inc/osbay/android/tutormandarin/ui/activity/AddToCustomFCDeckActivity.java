package inc.osbay.android.tutormandarin.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.model.FlashCardDeck;

public class AddToCustomFCDeckActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String EXTRA_FLASHCARD_ID = "AddToCustomFCDeckActivity.EXTRA_FLASHCARD_ID";

    private List<FlashCardDeck> mFlashCardDeckList;
    private CurriculumAdapter mCurriculumAdapter;
    private FlashCardRVAdapter mFlashcardAdapter;

    private String mFlashCardId;
    private List<String> mCustomDeckIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_custom_fcdeck);

        Intent intent = getIntent();
        if (intent != null) {
            mFlashCardId = intent.getStringExtra(EXTRA_FLASHCARD_ID);
        }

        mCustomDeckIds = new ArrayList<>();

        mCurriculumAdapter = new CurriculumAdapter(this);
        mFlashCardDeckList = mCurriculumAdapter.getCustomFCDecks();

        mFlashcardAdapter = new FlashCardRVAdapter();

        TextView tvCancel = (TextView) findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(this);

        TextView tvSave = (TextView) findViewById(R.id.tv_save);
        tvSave.setOnClickListener(this);

        RecyclerView rvFlashCardList = (RecyclerView) findViewById(R.id.rv_custom_fcdeck_list);
        rvFlashCardList.setAdapter(mFlashcardAdapter);

        RecyclerView.LayoutManager rvLayout = new LinearLayoutManager(this);
        rvFlashCardList.setLayoutManager(rvLayout);


    }

    @Override
    public void onResume() {
        super.onResume();

        mFlashCardDeckList = mCurriculumAdapter.getCustomFCDecks();
        mFlashcardAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                AddToCustomFCDeckActivity.this.finish();
                break;
            case R.id.tv_save:
//TODO add card to custom list
                mCurriculumAdapter.addToCustomFCDeck(mFlashCardId, mCustomDeckIds);

                AddToCustomFCDeckActivity.this.finish();
                break;
        }
    }

    private class FlashCardRVAdapter extends RecyclerView.Adapter<FlashCardRVAdapter.ViewHolder> {

        private static final int ITEM_FCDECK = 1;
        private static final int ITEM_ADD_NEW = 2;

        public FlashCardRVAdapter() {
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;
            if (viewType == ITEM_FCDECK) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fcdeck_custom, parent, false);
            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fcdeck_add_new, parent, false);
            }

            return new ViewHolder(v, viewType);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (position < mFlashCardDeckList.size()) {
                final FlashCardDeck deck = mFlashCardDeckList.get(position);

                holder.tvFlashCardTitle.setText(deck.getCardTitle());

                if (mCustomDeckIds.contains(String.valueOf(deck.getDeckId()))) {
                    holder.rbSelectDeck.setChecked(true);
                } else {
                    holder.rbSelectDeck.setChecked(false);
                }

                holder.rbSelectDeck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (mCustomDeckIds.contains(String.valueOf(deck.getDeckId()))) {
                            holder.rbSelectDeck.setChecked(false);
                        } else {
                            holder.rbSelectDeck.setChecked(true);
                        }

//                        holder.rbSelectDeck.setChecked(!holder.rbSelectDeck.isChecked());
                        if (holder.rbSelectDeck.isChecked()) {
                            mCustomDeckIds.add(String.valueOf(deck.getDeckId()));
                            notifyDataSetChanged();
                        } else {
                            mCustomDeckIds.remove(String.valueOf(deck.getDeckId()));
                            notifyDataSetChanged();
                        }
                    }
                });
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.getItemViewType() == ITEM_ADD_NEW) {
                        startActivity(new Intent(AddToCustomFCDeckActivity.this, CreateFCDeckActivity.class));
                    }
                }
            });
        }

        @Override
        public int getItemViewType(int position) {
            return (position == mFlashCardDeckList.size()) ? ITEM_ADD_NEW : ITEM_FCDECK;
        }

        @Override
        public int getItemCount() {
            return mFlashCardDeckList.size() + 1;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView tvFlashCardTitle;
            private RadioButton rbSelectDeck;

            public ViewHolder(View itemView, int itemType) {
                super(itemView);

                if (itemType == ITEM_FCDECK) {
                    LinearLayout llCredit = (LinearLayout) itemView.findViewById(R.id.ll_card_count);
                    llCredit.setVisibility(View.GONE);

                    tvFlashCardTitle = (TextView) itemView.findViewById(R.id.tv_flash_card_title);

                    rbSelectDeck = (RadioButton) itemView.findViewById(R.id.rb_select_deck);
                    rbSelectDeck.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
