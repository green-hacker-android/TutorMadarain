package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.model.FlashCardDeck;
import inc.osbay.android.tutormandarin.ui.activity.CreateFCDeckActivity;
import inc.osbay.android.tutormandarin.ui.activity.FragmentHolderActivity;

public class FlashCardCustomFragment extends Fragment {
    //    public static final String TAG = FlashCardCustomFragment.class.getSimpleName();
    private List<FlashCardDeck> mFlashCardDeckList;
    private CurriculumAdapter mCurriculumAdapter;
    private FlashCardAdapter mFlashcardAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurriculumAdapter = new CurriculumAdapter(getActivity());
        mFlashCardDeckList = mCurriculumAdapter.getCustomFCDecks();

        mFlashcardAdapter = new FlashCardAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_flash_card_custom, container, false);

        RecyclerView rvFlashCardList = (RecyclerView) v.findViewById(R.id.rv_flash_card_custom);
        rvFlashCardList.setAdapter(mFlashcardAdapter);

        RecyclerView.LayoutManager rvLayout = new LinearLayoutManager(getActivity());
        rvFlashCardList.setLayoutManager(rvLayout);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        mFlashCardDeckList = mCurriculumAdapter.getCustomFCDecks();
        mFlashcardAdapter.notifyDataSetChanged();
    }

    private class FlashCardAdapter extends RecyclerView.Adapter<FlashCardAdapter.ViewHolder> {

        public FlashCardAdapter() {
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fcdeck_custom, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final FlashCardDeck deck = mFlashCardDeckList.get(position);

            holder.tvFlashCardTitle.setText(deck.getCardTitle());
            holder.tvFlashCardCount.setText(String.valueOf(deck.getCardCount()));

            final SwipeLayout swipeLayout = (SwipeLayout) holder.itemView;
            swipeLayout.setOnClickListener(onClickListener(deck));
            swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {
                    layout.setOnClickListener(null);
                }

                @Override
                public void onOpen(SwipeLayout layout) {
                }

                @Override
                public void onStartClose(SwipeLayout layout) {

                }

                @Override
                public void onClose(final SwipeLayout layout) {

                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

                }

                @Override
                public void onHandRelease(final SwipeLayout layout, float xvel, float yvel) {
                    if (layout.getOpenStatus() == SwipeLayout.Status.Close) {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            layout.setOnClickListener(onClickListener(deck));
                                        }
                                    });
                                }
                            }
                        }, 100);
                    }
                }
            });

            holder.imvEditPack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_flashcard_edit, null);
                    final EditText etFlashcardTitle = (EditText) view.findViewById(R.id.et_flashcard_title);
                    etFlashcardTitle.setText(deck.getCardTitle());
                    etFlashcardTitle.getBackground().mutate().setColorFilter(getResources().getColor(R.color.primary_color), PorterDuff.Mode.SRC_ATOP);

                    builder.setView(view);

                    builder.setTitle(getString(R.string.fc_rename_pack))
                            .setPositiveButton(getString(R.string.fc_btn_rename), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    if (!TextUtils.isEmpty(etFlashcardTitle.getText().toString())) {
                                        mCurriculumAdapter.updateCustomFCDeck(deck.getDeckId(), etFlashcardTitle.getText().toString());
                                    }

                                    mFlashCardDeckList = mCurriculumAdapter.getCustomFCDecks();
                                    notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton(getString(R.string.fc_btn_rename_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                }
            });

            holder.imvDeletePack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.fc_delete_title))
                            .setMessage(getString(R.string.fc_delete_msg))
                            .setPositiveButton(getString(R.string.fc_btn_delete), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mCurriculumAdapter.deleteCustomFCDeck(deck.getDeckId());

                                    mFlashCardDeckList = mCurriculumAdapter.getCustomFCDecks();
                                    notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton(getString(R.string.fc_btn_delete_cancel), null)
                            .create()
                            .show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mFlashCardDeckList.size();
        }

        private View.OnClickListener onClickListener(final FlashCardDeck deck) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (deck.getCardCount() == 0) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.fc_notice_title))
                                .setMessage(getString(R.string.fc_notice_msg))
                                .setPositiveButton(getString(R.string.fc_btn_notice_ok), null)
                                .create()
                                .show();
                    } else {
                        System.gc();

                        Intent intent = new Intent(getActivity(), FragmentHolderActivity.class);
                        intent.putExtra(FragmentHolderActivity.EXTRA_DISPLAY_FRAGMENT,
                                FlashCardPagerFragment.class.getSimpleName());
                        intent.putExtra(FlashCardPagerFragment.EXTRA_FLASHCARD_DECK, deck);
                        startActivity(intent);
                    }
                }
            };
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tvFlashCardTitle;

            private TextView tvFlashCardCount;

            private ImageView imvDeletePack;

            private ImageView imvEditPack;

            public ViewHolder(View itemView) {
                super(itemView);

                tvFlashCardTitle = (TextView) itemView.findViewById(R.id.tv_flash_card_title);

                tvFlashCardCount = (TextView) itemView.findViewById(R.id.tv_flash_card_count);

                imvDeletePack = (ImageView) itemView.findViewById(R.id.imv_delete_pack);

                imvEditPack = (ImageView) itemView.findViewById(R.id.imv_edit_pack);
            }
        }
    }
}