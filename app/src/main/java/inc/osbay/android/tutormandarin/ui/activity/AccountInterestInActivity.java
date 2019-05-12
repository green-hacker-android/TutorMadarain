package inc.osbay.android.tutormandarin.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import inc.osbay.android.tutormandarin.R;

public class AccountInterestInActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String ACCOUNT_INTEREST_IN = "AccountInterestInActivity.ACCOUNT_INTEREST_IN";
    String interestIn;
    private List<String> mInterestInList;
    private List<String> mSelectedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_interest_in);

        mInterestInList = Arrays.asList(getResources().getStringArray(R.array.ic_interest_in_list));

        interestIn = getIntent().getStringExtra(ACCOUNT_INTEREST_IN);

        ImageView imvInterestInBack = (ImageView) findViewById(R.id.imv_interest_in_back);
        TextView tvInterestInSave = (TextView) findViewById(R.id.tv_interest_in_save);
        RecyclerView rvInterestIn = (RecyclerView) findViewById(R.id.rv_interest_in);
        LinearLayoutManager llmInterestIn = new LinearLayoutManager(this);
        rvInterestIn.setLayoutManager(llmInterestIn);

        mSelectedList = new ArrayList<>();
        for (int k = 0; k < mInterestInList.size(); k++) {
            mSelectedList.add("");
        }

        if (interestIn != null) {
            List<String> splitInterestInList = new ArrayList<>(Arrays.asList(interestIn.split(", ")));
            for (int i = 0; i < splitInterestInList.size(); i++) {
                for (int j = 0; j < mInterestInList.size(); j++) {
                    if (mInterestInList.get(j).equals(splitInterestInList.get(i))) {
                        mSelectedList.set(j, mInterestInList.get(j));
                    }
                }
            }
        }

        rvInterestIn.setAdapter(new InterestInAdapter());

        imvInterestInBack.setOnClickListener(this);
        tvInterestInSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imv_interest_in_back:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.tv_interest_in_save:
                String interestStr = "";
                for (int i = 0; i < mSelectedList.size(); i++) {
                    if (!mSelectedList.get(i).equals("")) {
                        interestStr += mSelectedList.get(i) + ", ";
                    }
                }

                Intent intent = new Intent();
                if (interestStr.equals("")) {
                    intent.putExtra(ACCOUNT_INTEREST_IN, "");
                } else {
                    intent.putExtra(ACCOUNT_INTEREST_IN, interestStr.substring(0, interestStr.lastIndexOf(", ")));
                }
                this.setResult(RESULT_OK, intent);
                this.finish();
                break;
        }
    }

    private class InterestInAdapter extends RecyclerView.Adapter<InterestInAdapter.InterestInHolder> {
        @Override
        public InterestInHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_interest_and_learning, parent, false);
            return new InterestInHolder(v);
        }

        @Override
        public void onBindViewHolder(final InterestInHolder holder, int position) {
            if (mInterestInList.get(position).equals(mSelectedList.get(position))) {
                holder.imvInterestLearningCheck.setImageResource(R.drawable.ic_checked);
            }

            holder.tvInterestLearningTitle.setText(mInterestInList.get(position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mInterestInList.get(holder.getAdapterPosition()).equals(mSelectedList.get(holder.getAdapterPosition()))) {
                        holder.imvInterestLearningCheck.setImageResource(R.drawable.ic_unchecked);
                        mSelectedList.set(holder.getAdapterPosition(), "");
                    } else {
                        holder.imvInterestLearningCheck.setImageResource(R.drawable.ic_checked);
                        mSelectedList.set(holder.getAdapterPosition(), mInterestInList.get(holder.getAdapterPosition()));
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mInterestInList.size();
        }

        class InterestInHolder extends RecyclerView.ViewHolder {
            TextView tvInterestLearningTitle;
            ImageView imvInterestLearningCheck;

            public InterestInHolder(View itemView) {
                super(itemView);
                tvInterestLearningTitle = (TextView) itemView.findViewById(R.id.tv_interest_learning_title);
                imvInterestLearningCheck = (ImageView) itemView.findViewById(R.id.imv_interest_learning_check);
            }
        }
    }
}
