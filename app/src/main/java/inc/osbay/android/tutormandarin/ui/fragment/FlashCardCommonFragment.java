package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.CurriculumAdapter;
import inc.osbay.android.tutormandarin.sdk.model.FlashCardDeck;
import inc.osbay.android.tutormandarin.ui.activity.FragmentHolderActivity;

public class FlashCardCommonFragment extends Fragment {
    private List<FlashCardDeck> mFlashCardList;
    private FlashCardRVAdapter mFlashCardAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CurriculumAdapter curriculumAdapter = new CurriculumAdapter(getActivity());
        mFlashCardList = curriculumAdapter.getCommonFCDecks();

        mFlashCardAdapter = new FlashCardRVAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_flash_card_common, container, false);

        RecyclerView rvFlashCardList = (RecyclerView) v.findViewById(R.id.rv_flash_card_common);
        rvFlashCardList.setAdapter(mFlashCardAdapter);

        RecyclerView.LayoutManager rvLayout = new LinearLayoutManager(getActivity());
        rvFlashCardList.setLayoutManager(rvLayout);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        final ServerRequestManager requestManager = new ServerRequestManager(
                getActivity().getApplicationContext());
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.fc_dialog_loading));

        if (mFlashCardList.size() == 0) {
            dialog.show();
        }

        requestManager.downloadFlashCardList(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                if (getActivity() != null) {
                    dialog.dismiss();
                    mFlashCardList = (List<FlashCardDeck>) result;
                    mFlashCardAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(ServerError err) {
                if (getActivity() != null) {
                    dialog.dismiss();
                    Toast.makeText(getActivity(), getString(R.string.fc_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class FlashCardRVAdapter extends RecyclerView.Adapter<FlashCardRVAdapter.ViewHolder> {

        public FlashCardRVAdapter() {
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fcdeck_common, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final FlashCardDeck flashCardDeck = mFlashCardList.get(position);

            holder.tvFlashCardTitle.setText(flashCardDeck.getCardTitle());
            holder.tvFlashCardCount.setText(String.valueOf(flashCardDeck.getCardCount()));

            //TODO
            holder.tvFlashCardLevel.setVisibility(View.VISIBLE);
            holder.tvFlashCardLevel.setText(flashCardDeck.getCardLevel());
            if ((mFlashCardList.size() - 1) == position) {
                holder.vwFlashCard.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), FragmentHolderActivity.class);
                    intent.putExtra(FragmentHolderActivity.EXTRA_DISPLAY_FRAGMENT,
                            FlashCardPagerFragment.class.getSimpleName());
                    intent.putExtra(FlashCardPagerFragment.EXTRA_FLASHCARD_DECK, flashCardDeck);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mFlashCardList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tvFlashCardTitle;

            private TextView tvFlashCardLevel;

            private TextView tvFlashCardCount;

            private View vwFlashCard;

            public ViewHolder(View itemView) {
                super(itemView);

                tvFlashCardTitle = (TextView) itemView.findViewById(R.id.tv_flash_card_title);

                tvFlashCardLevel = (TextView) itemView.findViewById(R.id.tv_flash_card_level);

                tvFlashCardCount = (TextView) itemView.findViewById(R.id.tv_flash_card_count);

                vwFlashCard = itemView.findViewById(R.id.vw_flash_card);

            }
        }
    }
}