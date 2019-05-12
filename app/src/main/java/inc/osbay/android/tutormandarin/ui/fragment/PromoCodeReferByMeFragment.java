package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.PromoCodeRefer;

/**
 * A simple {@link Fragment} subclass.
 */
public class PromoCodeReferByMeFragment extends Fragment {

    private List<PromoCodeRefer> mPromoCodeRefers;
    private ServerRequestManager mServerRequestManager;
    private PromoCodeReferAdapter mPromoCodeReferAdapter;
    private TextView mTvReferByMeNoHistory;
    private RecyclerView mRvPcRefers;


    public PromoCodeReferByMeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccountAdapter dbAdapter = new AccountAdapter(getActivity());
        mPromoCodeRefers = dbAdapter.getPromoCodeRefers();
        mPromoCodeReferAdapter = new PromoCodeReferAdapter();
        mServerRequestManager = new ServerRequestManager(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_promo_code_refer_by_me, container, false);


        mRvPcRefers = v.findViewById(R.id.rv_pc_refers);
        mRvPcRefers.setAdapter(new PromoCodeReferAdapter());
        LinearLayoutManager llmPcRefers = new LinearLayoutManager(getActivity());
        mRvPcRefers.setLayoutManager(llmPcRefers);

        mTvReferByMeNoHistory = v.findViewById(R.id.tv_refer_by_me_no_history);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.ph_refer_loading));

        if (mPromoCodeRefers.size() == 0) {
            progressDialog.show();
        }

        mServerRequestManager.getPromotionHistoryListForInvitor(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                progressDialog.dismiss();
                if (getActivity() != null && result != null) {
                    mPromoCodeRefers.clear();
                    List<?> objects = (List<?>) result;
                    for (Object obj : objects) {
                        if (obj instanceof PromoCodeRefer) {
                            mPromoCodeRefers.add((PromoCodeRefer) obj);
                        }
                    }

                    if (mPromoCodeRefers.size() > 0) {
                        mRvPcRefers.setVisibility(View.VISIBLE);
                        mTvReferByMeNoHistory.setVisibility(View.GONE);
                    }

                    mPromoCodeReferAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(ServerError err) {
                progressDialog.dismiss();

                mRvPcRefers.setVisibility(View.GONE);
                mTvReferByMeNoHistory.setVisibility(View.VISIBLE);

                String noPromoHistory;
                if (err.getMessage().equals("No promotion code history.")) {
                    noPromoHistory = getString(R.string.pc_no_promotion_history);
                } else {
                    noPromoHistory = err.getMessage();
                }

                mTvReferByMeNoHistory.setText(noPromoHistory);
            }
        });
    }

    class PromoCodeReferAdapter extends RecyclerView.Adapter<PromoCodeReferAdapter.PromoCodeReferHolder> {
        @Override
        public PromoCodeReferHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_promo_code_refer, parent, false);
            return new PromoCodeReferHolder(v);
        }

        @Override
        public void onBindViewHolder(PromoCodeReferHolder holder, int position) {
            holder.tvPcReferName.setText(mPromoCodeRefers.get(position).getName());
            holder.tvPcReferDate.setText(mPromoCodeRefers.get(position).getDate());

            if (mPromoCodeRefers.get(position).getStatus() == 2)
                holder.tvPcReferReward.setText(getString(R.string.ph_refer_waiting));
            else
                holder.tvPcReferReward.setText(String.valueOf(mPromoCodeRefers.get(position).getReward()));
        }

        @Override
        public int getItemCount() {
            return mPromoCodeRefers.size();
        }

        class PromoCodeReferHolder extends RecyclerView.ViewHolder {
            TextView tvPcReferName;
            TextView tvPcReferDate;
            TextView tvPcReferReward;

            public PromoCodeReferHolder(View itemView) {
                super(itemView);
                tvPcReferName = itemView.findViewById(R.id.tv_pc_refer_name);
                tvPcReferDate = itemView.findViewById(R.id.tv_pc_refer_date);
                tvPcReferReward = itemView.findViewById(R.id.tv_pc_refer_reward);
            }
        }
    }
}
