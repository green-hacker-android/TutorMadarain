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
import inc.osbay.android.tutormandarin.sdk.model.PromoCodeEnter;

/**
 * A simple {@link Fragment} subclass.
 */
public class PromoCodeIEnterFragment extends Fragment {

    private List<PromoCodeEnter> mPromoCodeEnters;
    private ServerRequestManager mServerRequestManager;
    private PromoIEnterAdapter mPromoIEnterAdapter;
    private TextView mTvIEnteredNoHistory;
    private RecyclerView mRvPromoIEnter;

    public PromoCodeIEnterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccountAdapter adapter = new AccountAdapter(getActivity());
        mPromoCodeEnters = adapter.getPromoCodeEnters();
        mServerRequestManager = new ServerRequestManager(getActivity());
        mPromoIEnterAdapter = new PromoIEnterAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_promo_code_ienter, container, false);
        mRvPromoIEnter = v.findViewById(R.id.rv_promo_i_enter);
        mTvIEnteredNoHistory = v.findViewById(R.id.tv_i_entered_no_history);

        mRvPromoIEnter.setAdapter(mPromoIEnterAdapter);
        LinearLayoutManager llmPromoIEnter = new LinearLayoutManager(getActivity());
        mRvPromoIEnter.setLayoutManager(llmPromoIEnter);


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.ph_i_entered_loading));

        if (mPromoCodeEnters.size() == 0) {
            progressDialog.show();
        }

        mServerRequestManager.getPromotionHistoryListforEntered(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                progressDialog.dismiss();
                if (getActivity() != null && result != null) {
                    mPromoCodeEnters.clear();
                    List<?> objects = (List<?>) result;
                    for (Object obj : objects) {
                        if (obj instanceof PromoCodeEnter) {
                            mPromoCodeEnters.add((PromoCodeEnter) obj);
                        }
                    }

                    if (mPromoCodeEnters.size() > 0) {
                        mRvPromoIEnter.setVisibility(View.VISIBLE);
                        mTvIEnteredNoHistory.setVisibility(View.GONE);
                    }

                    mPromoIEnterAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(ServerError err) {
                progressDialog.dismiss();

                mRvPromoIEnter.setVisibility(View.GONE);
                mTvIEnteredNoHistory.setVisibility(View.VISIBLE);

                String noPromoHistory;
                if (err.getMessage().equals("No promotion code history.")) {
                    noPromoHistory = getString(R.string.pc_no_promotion_history);
                } else {
                    noPromoHistory = err.getMessage();
                }
                mTvIEnteredNoHistory.setText(noPromoHistory);
            }
        });
    }

    class PromoIEnterAdapter extends RecyclerView.Adapter<PromoIEnterAdapter.PromoIEnterHolder> {
        @Override
        public PromoIEnterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_promo_code_ienter, parent, false);
            return new PromoIEnterHolder(v);
        }

        @Override
        public void onBindViewHolder(PromoIEnterHolder holder, int position) {
            holder.tvPcEnterCode.setText(mPromoCodeEnters.get(position).getCode());
            holder.tvPcEnterType.setText(mPromoCodeEnters.get(position).getType());
            holder.tvPcEnterDate.setText(mPromoCodeEnters.get(position).getDate());

            if (mPromoCodeEnters.get(position).getStatus() == 2)
                holder.tvPcEnterReward.setText(getString(R.string.ph_i_entered_waiting));
            else
                holder.tvPcEnterReward.setText(String.valueOf(mPromoCodeEnters.get(position).getReward()));
        }

        @Override
        public int getItemCount() {
            return mPromoCodeEnters.size();
        }

        class PromoIEnterHolder extends RecyclerView.ViewHolder {
            TextView tvPcEnterCode;
            TextView tvPcEnterType;
            TextView tvPcEnterDate;
            TextView tvPcEnterReward;

            private PromoIEnterHolder(View itemView) {
                super(itemView);
                tvPcEnterCode = itemView.findViewById(R.id.tv_pc_enter_code);
                tvPcEnterType = itemView.findViewById(R.id.tv_pc_enter_type);
                tvPcEnterDate = itemView.findViewById(R.id.tv_pc_enter_date);
                tvPcEnterReward = itemView.findViewById(R.id.tv_pc_enter_reward);
            }
        }
    }

}
