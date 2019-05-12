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

public class AccountLearningPurposeActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String ACCOUNT_LEARNING_PURPOSE = "AccountLearningPurposeActivity.ACCOUNT_LEARNING_PURPOSE";
    String learningPurpose;
    private List<String> mLearningList;
    private List<String> mSelectedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_learning_purpose);

        mLearningList = Arrays.asList(getResources().getStringArray(R.array.ic_learning_purpose_list));

        learningPurpose = getIntent().getStringExtra(ACCOUNT_LEARNING_PURPOSE);

        ImageView imvLearningPurposeBack = (ImageView) findViewById(R.id.imv_learning_purpose_back);
        TextView tvLearningPurposeSave = (TextView) findViewById(R.id.tv_learning_purpose_save);
        RecyclerView rvLearningPurpose = (RecyclerView) findViewById(R.id.rv_learning_purpose);
        LinearLayoutManager llmLearningPurpose = new LinearLayoutManager(this);
        rvLearningPurpose.setLayoutManager(llmLearningPurpose);

        mSelectedList = new ArrayList<>();
        for (int k = 0; k < mLearningList.size(); k++) {
            mSelectedList.add("");
        }


        if (learningPurpose != null) {
            List<String> splitInterestInList = new ArrayList<>(Arrays.asList(learningPurpose.split(", ")));
            for (int i = 0; i < splitInterestInList.size(); i++) {
                for (int j = 0; j < mLearningList.size(); j++) {
                    if (mLearningList.get(j).equals(splitInterestInList.get(i))) {
                        mSelectedList.set(j, mLearningList.get(j));
                    }
                }
            }
        }

        rvLearningPurpose.setAdapter(new LearningPurposeAdapter());

        imvLearningPurposeBack.setOnClickListener(this);
        tvLearningPurposeSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_learning_purpose_save:
                String learningPurposeStr = "";
                for (int i = 0; i < mSelectedList.size(); i++) {
                    if (!mSelectedList.get(i).equals("")) {
                        learningPurposeStr += mSelectedList.get(i) + ", ";
                    }
                }

                Intent intent = new Intent();
                if (learningPurposeStr.equals("")) {
                    intent.putExtra(ACCOUNT_LEARNING_PURPOSE, "");
                } else {
                    intent.putExtra(ACCOUNT_LEARNING_PURPOSE, learningPurposeStr.substring(0, learningPurposeStr.lastIndexOf(", ")));
                }
                this.setResult(RESULT_OK, intent);
                this.finish();
                break;
            case R.id.imv_learning_purpose_back:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }

    private class LearningPurposeAdapter extends RecyclerView.Adapter<LearningPurposeAdapter.LearningPurposeHolder> {

        @Override
        public LearningPurposeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_interest_and_learning, parent, false);
            return new LearningPurposeHolder(v);
        }

        @Override
        public void onBindViewHolder(final LearningPurposeHolder holder, int position) {
            if (mLearningList.get(position).equals(mSelectedList.get(position))) {
                holder.imvInterestLearningCheck.setImageResource(R.drawable.ic_checked);
            }

            holder.tvInterestLearningTitle.setText(mLearningList.get(position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mLearningList.get(holder.getAdapterPosition()).equals(mSelectedList.get(holder.getAdapterPosition()))) {
                        holder.imvInterestLearningCheck.setImageResource(R.drawable.ic_unchecked);
                        mSelectedList.set(holder.getAdapterPosition(), "");
                    } else {
                        holder.imvInterestLearningCheck.setImageResource(R.drawable.ic_checked);
                        mSelectedList.set(holder.getAdapterPosition(), mLearningList.get(holder.getAdapterPosition()));
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mLearningList.size();
        }

        class LearningPurposeHolder extends RecyclerView.ViewHolder {

            TextView tvInterestLearningTitle;
            ImageView imvInterestLearningCheck;

            public LearningPurposeHolder(View itemView) {
                super(itemView);
                tvInterestLearningTitle = (TextView) itemView.findViewById(R.id.tv_interest_learning_title);
                imvInterestLearningCheck = (ImageView) itemView.findViewById(R.id.imv_interest_learning_check);
            }
        }
    }
}
