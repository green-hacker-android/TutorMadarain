package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.ui.activity.SignUpContinueActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpInterestFragment extends Fragment {


    private SignUpContinueActivity parentActivity;
    private InterestRVAdapter interestRVAdapter;
    private Map<Integer, String> selectedIds;

    private String[] topics;

    private int[] icons = {
            R.drawable.ic_interest_phrase, R.drawable.ic_interest_language,
            R.drawable.ic_interest_skill, R.drawable.ic_interest_news, R.drawable.ic_interest_china,
            R.drawable.ic_interest_funny, R.drawable.ic_interest_conversation, R.drawable.ic_interest_music
        };

    public SignUpInterestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        topics = getResources().getStringArray(R.array.su_interests);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FlurryAgent.logEvent("SignUp Interest");
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sign_up_interest, container, false);

        parentActivity = (SignUpContinueActivity) getActivity();
        selectedIds = new HashMap<>();
        interestRVAdapter = new InterestRVAdapter();

        RecyclerView rvLearningGoal = (RecyclerView) rootView.findViewById(R.id.rv_interest_in);
        rvLearningGoal.setAdapter(interestRVAdapter);

        RecyclerView.LayoutManager gridLayout = new GridLayoutManager(getActivity(), 2);
        rvLearningGoal.setLayoutManager(gridLayout);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        resetSelection();
    }

    private class InterestRVAdapter extends RecyclerView.Adapter<InterestRVAdapter.ViewHolder> {


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_grid_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.imvIcon.setImageResource(icons[position]);
            holder.tvTitle.setText(topics[position]);
            holder.select(selectedIds.containsKey(position));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(selectedIds.containsKey(holder.getAdapterPosition())){
                        holder.select(false);
                        selectedIds.remove(holder.getAdapterPosition());
                    }else{
                        holder.select(true);
                        selectedIds.put(holder.getAdapterPosition(),
                                topics[holder.getAdapterPosition()]);
                    }

                    updateSelection();
                }
            });
        }

        @Override
        public int getItemCount() {
            return 8;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView tvTitle;
            private ImageView imvIcon;
            private ImageView imvSelected;

            public ViewHolder(View itemView) {
                super(itemView);

                tvTitle = (TextView) itemView.findViewById(R.id.tv_item_title);
                imvIcon = (ImageView) itemView.findViewById(R.id.imv_item_icon);
                imvSelected = (ImageView) itemView.findViewById(R.id.imv_selected_mark);
            }

            public void select(boolean status) {
                if (status) {
                    imvSelected.setVisibility(View.VISIBLE);
                } else {
                    imvSelected.setVisibility(View.GONE);
                }
            }
        }
    }

    private void resetSelection(){
        selectedIds.clear();

        if(!TextUtils.isEmpty(parentActivity.topics)){
            String[] dts = parentActivity.topics.split(",");

            for(String topic : dts){
                int index = Arrays.asList(topics).indexOf(topic);

                if(index >= 0){
                    selectedIds.put(index, topic);
                }
            }
        }

        interestRVAdapter.notifyDataSetChanged();
    }

    private void updateSelection(){
        String selection = "";
        for(String topic : selectedIds.values()){
            if(!TextUtils.isEmpty(selection)){
                selection += ",";
            }
            selection += topic;
        }

        parentActivity.topics = selection;
    }
}
