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
public class SignUpGoalFragment extends Fragment {

    private GoalRVAdapter goalRVAdapter;
    private Map<Integer, String> selectedIds;

    private SignUpContinueActivity parentActivity;

    private String[] goals;

    private int[] icons = {
            R.drawable.ic_goal_travel, R.drawable.ic_goal_business, R.drawable.ic_goal_test,
            R.drawable.ic_goal_education, R.drawable.ic_goal_communication
    };

    public SignUpGoalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        goals = getResources().getStringArray(R.array.su_goals);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FlurryAgent.logEvent("SignUp Goal");
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sign_up_goal, container, false);

        parentActivity = (SignUpContinueActivity) getActivity();
        selectedIds = new HashMap<>();
        goalRVAdapter = new GoalRVAdapter();

        RecyclerView rvLearningGoal = (RecyclerView) rootView.findViewById(R.id.rv_learning_goal);
        rvLearningGoal.setAdapter(goalRVAdapter);

        RecyclerView.LayoutManager gridLayout = new GridLayoutManager(getActivity(), 2);
        rvLearningGoal.setLayoutManager(gridLayout);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        resetSelection();
    }

    private class GoalRVAdapter extends RecyclerView.Adapter<GoalRVAdapter.ViewHolder> {


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_grid_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.tvTitle.setText(goals[position]);
            holder.imvIcon.setImageResource(icons[position]);
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
                                goals[holder.getAdapterPosition()]);
                    }

                    updateSelection();
                }
            });
        }

        @Override
        public int getItemCount() {
            return 5;
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

        if(!TextUtils.isEmpty(parentActivity.goals)){
            String[] dgs = parentActivity.goals.split(",");

            for(String goal : dgs){
                int index = Arrays.asList(goals).indexOf(goal);

                if(index >= 0){
                    selectedIds.put(index, goal);
                }
            }
        }

        goalRVAdapter.notifyDataSetChanged();
    }

    private void updateSelection(){
        String selection = "";
        for(String goal : selectedIds.values()){
            if(!TextUtils.isEmpty(selection)){
                selection += ",";
            }
            selection += goal;
        }

        parentActivity.goals = selection;
    }
}
