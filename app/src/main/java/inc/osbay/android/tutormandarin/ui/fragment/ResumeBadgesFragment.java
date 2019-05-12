package inc.osbay.android.tutormandarin.ui.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.Badges;
import inc.osbay.android.tutormandarin.util.CommonUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class ResumeBadgesFragment extends Fragment {

    private List<Badges> mBadges1;
    private List<Badges> mBadges2;
    private List<Badges> mBadges3;
    private List<Badges> mBadges4;

    public ResumeBadgesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBadges1 = new ArrayList<>();
        mBadges2 = new ArrayList<>();
        mBadges3 = new ArrayList<>();
        mBadges4 = new ArrayList<>();

        AccountAdapter accountAdapter = new AccountAdapter(getActivity());
        List<Badges> mBadges = accountAdapter.getBadges();

        for (int i = 0; i < mBadges.size(); i++) {
            int categoryId = mBadges.get(i).getBadgesCategory();
            if (categoryId == Badges.Category.TIME_BASED) {
                mBadges1.add(mBadges.get(i));

            } else if (categoryId == Badges.Category.COURSE_BASED) {
                mBadges2.add(mBadges.get(i));

            } else if (categoryId == Badges.Category.SOCIAL_BASED) {
                mBadges3.add(mBadges.get(i));

            } else if (categoryId == Badges.Category.RATING_AND_REVIEWS) {
                mBadges4.add(mBadges.get(i));

            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_resume_badges, container, false);
        RecyclerView rvBadgesTimeBasedList = (RecyclerView) v.findViewById(R.id.rv_badges_time_based_list);
        RecyclerView rvBadgesCourseBasedList = (RecyclerView) v.findViewById(R.id.rv_badges_course_based_list);
        RecyclerView rvBadgesSocialBasedList = (RecyclerView) v.findViewById(R.id.rv_badges_social_based_list);
        RecyclerView rvRatingAndReviewsList = (RecyclerView) v.findViewById(R.id.rv_rate_and_reviews_list);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvBadgesTimeBasedList.setLayoutManager(llm);
        BadgesListAdapter mBadgesListAdapter1 = new BadgesListAdapter(mBadges1);
        rvBadgesTimeBasedList.setAdapter(mBadgesListAdapter1);

        LinearLayoutManager llm1 = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvBadgesCourseBasedList.setLayoutManager(llm1);
        BadgesListAdapter mBadgesListAdapter2 = new BadgesListAdapter(mBadges2);
        rvBadgesCourseBasedList.setAdapter(mBadgesListAdapter2);

        LinearLayoutManager llm2 = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvBadgesSocialBasedList.setLayoutManager(llm2);
        BadgesListAdapter mBadgesListAdapter3 = new BadgesListAdapter(mBadges3);
        rvBadgesSocialBasedList.setAdapter(mBadgesListAdapter3);

        LinearLayoutManager llm3 = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvRatingAndReviewsList.setLayoutManager(llm3);
        BadgesListAdapter mBadgesListAdapter4 = new BadgesListAdapter(mBadges4);
        rvRatingAndReviewsList.setAdapter(mBadgesListAdapter4);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private class BadgesListAdapter extends RecyclerView.Adapter<BadgesListAdapter.BadgesHolder> {
        private List<Badges> mBadgesList;

        public BadgesListAdapter(List<Badges> badgesList) {
            mBadgesList = badgesList;
        }

        @Override
        public BadgesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_badges, parent, false);
            return new BadgesHolder(v);
        }

        @Override
        public void onBindViewHolder(BadgesHolder holder, int position) {
            holder.tvAchievementMsg.setText(mBadgesList.get(position).getAchievementMsg());

            if (mBadgesList.get(position).getAwardType() == Badges.AwardType.CREDITS) {
                if (mBadgesList.get(position).getCompletedPercent() == 100) {
                    holder.llCompletePercent.setVisibility(View.GONE);
                    holder.tvCompletedDate.setVisibility(View.GONE);
                    holder.tvCompletedDate.setVisibility(View.VISIBLE);
                    holder.llBadgesCredit.setVisibility(View.VISIBLE);
                    holder.tvBadgesName.setVisibility(View.VISIBLE);

                    holder.imgvBadges.setImageResource(R.drawable.ic_badges_complete);
                    holder.tvCompletedDate.setText(CommonUtil.getCustomDateResult(mBadgesList.get(position).getCompletedDate(), "yyyy-MM-dd HH:mm:ss", "MMM dd,yyyy"));
                    holder.tvBadgesName.setText(mBadgesList.get(position).getBadgesName());
                    holder.tvBadgesCredit.setText(String.format(Locale.getDefault(), "%.0f", mBadgesList.get(position).getCredit()));
                } else {
                    holder.tvCompletedDate.setVisibility(View.GONE);
                    holder.tvBadgesName.setVisibility(View.GONE);
                    holder.llBadgesCredit.setVisibility(View.VISIBLE);
                    holder.llCompletePercent.setVisibility(View.VISIBLE);

                    holder.imgvBadges.setImageResource(R.drawable.ic_badges_no_complete);
                    holder.tvBadgesCredit.setText(String.format(Locale.getDefault(), "%.0f", mBadgesList.get(position).getCredit()));
                    holder.tvCompletedPercent.setText(String.valueOf(mBadgesList.get(position).getCompletedPercent()));
                }
            } else {
                if (mBadgesList.get(position).getCompletedPercent() == 100) {
                    holder.tvCompletedDate.setVisibility(View.VISIBLE);
                    holder.tvBadgesName.setVisibility(View.VISIBLE);
                    holder.llBadgesCredit.setVisibility(View.GONE);
                    holder.llCompletePercent.setVisibility(View.GONE);

                    holder.imgvBadges.setImageResource(R.drawable.ic_badges_complete);
                    holder.tvBadgesName.setText(mBadgesList.get(position).getBadgesName());
                    holder.tvCompletedDate.setText(CommonUtil.getCustomDateResult(mBadgesList.get(position).getCompletedDate(), "yyyy-MM-dd HH:mm:ss", "MMM dd,yyyy"));
                } else {
                    holder.tvCompletedDate.setVisibility(View.GONE);
                    holder.tvBadgesName.setVisibility(View.GONE);
                    holder.llBadgesCredit.setVisibility(View.GONE);
                    holder.llCompletePercent.setVisibility(View.VISIBLE);

                    holder.imgvBadges.setImageResource(R.drawable.ic_badges_no_complete);
                    holder.tvCompletedPercent.setText(String.valueOf(mBadgesList.get(position).getCompletedPercent()));
                }
            }
        }

        @Override
        public int getItemCount() {
            return mBadgesList.size();
        }

        class BadgesHolder extends RecyclerView.ViewHolder {
            TextView tvCompletedDate;
            TextView tvBadgesName;
            TextView tvAchievementMsg;
            TextView tvBadgesCredit;
            TextView tvCompletedPercent;

            ImageView imgvBadges;

            LinearLayout llCompletePercent;
            LinearLayout llBadgesCredit;

            public BadgesHolder(View itemView) {
                super(itemView);
                tvCompletedDate = (TextView) itemView.findViewById(R.id.tv_completed_date);
                tvBadgesName = (TextView) itemView.findViewById(R.id.tv_badges_name);
                tvAchievementMsg = (TextView) itemView.findViewById(R.id.tv_achievement_msg);
                tvBadgesCredit = (TextView) itemView.findViewById(R.id.tv_badges_credit);
                tvCompletedPercent = (TextView) itemView.findViewById(R.id.tv_completed_percent);
                imgvBadges = (ImageView) itemView.findViewById(R.id.imgv_badges);
                llCompletePercent = (LinearLayout) itemView.findViewById(R.id.ll_completed_percent);
                llBadgesCredit = (LinearLayout) itemView.findViewById(R.id.ll_badges_credit);
            }
        }
    }

}
