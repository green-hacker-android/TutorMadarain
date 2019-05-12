package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.LGCNotification;
import inc.osbay.android.tutormandarin.util.CommonUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotiListFragment extends BackHandledFragment {

    AccountAdapter accountAdapter;
    NotiRVAdapter mNotiRVAdapter;
    ServerRequestManager requestManager;
    private List<String> mDates;
    private Date mToday;
    private Date mYesterday;
    private UpdateDateAdapter mUpdateDateAdapter;
    private LinearLayout mLlNoUpdateNews;
    private RecyclerView mRvUpdateDate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mToday = new Date();
        long oneDay = 86400000;
        long yesterdayDate = mToday.getTime() - oneDay;
        mYesterday = new Date(yesterdayDate);

        accountAdapter = new AccountAdapter(getActivity());
        mDates = accountAdapter.getNotificationDate();

        requestManager = new ServerRequestManager(getActivity().getApplicationContext());
        mNotiRVAdapter = new NotiRVAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_noti_list, container, false);

        Toolbar toolBar;
        toolBar = (Toolbar) v.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#34404E"));
        setSupportActionBar(toolBar);

        mRvUpdateDate = (RecyclerView) v.findViewById(R.id.rv_update_date);

        LinearLayoutManager updateDateLm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRvUpdateDate.setLayoutManager(updateDateLm);
        mUpdateDateAdapter = new UpdateDateAdapter();
        mRvUpdateDate.setAdapter(mUpdateDateAdapter);

        mLlNoUpdateNews = (LinearLayout) v.findViewById(R.id.ll_no_update_news);
        if (mDates.size() == 0) {
            mLlNoUpdateNews.setVisibility(View.VISIBLE);
            mRvUpdateDate.setVisibility(View.GONE);
        } else {
            mLlNoUpdateNews.setVisibility(View.GONE);
            mRvUpdateDate.setVisibility(View.VISIBLE);
        }
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        setTitle(getString(R.string.nt_title));
        setHasOptionsMenu(true);
        setDisplayHomeAsUpEnable(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        requestManager.downloadNotificationList(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                if (getActivity() != null) {
                    mDates = accountAdapter.getNotificationDate();
                    mNotiRVAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(ServerError err) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return true;
    }

    @Override
    public boolean onBackPressed() {
        getFragmentManager().popBackStack();
        return false;
    }

    private class UpdateDateAdapter extends RecyclerView.Adapter<UpdateDateAdapter.UpdateDateHolder> {

        @Override
        public UpdateDateHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_update_date, parent, false);
            return new UpdateDateHolder(v);
        }

        @Override
        public void onBindViewHolder(UpdateDateHolder holder, int position) {
            if (mDates.get(position).equals(CommonUtil.getTimeStringResult(mToday, "yyyy-MM-dd"))) {
                holder.tvUpdateDate.setText(getString(R.string.nt_today));
            } else if (mDates.get(position).equals(CommonUtil.getTimeStringResult(mYesterday, "yyyy-MM-dd"))) {
                holder.tvUpdateDate.setText(getString(R.string.nt_yesterday));
            } else {
                holder.tvUpdateDate.setText(mDates.get(position));
            }

            LinearLayoutManager llmRvUpdateNews = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            mNotiRVAdapter = new NotiRVAdapter(mDates.get(position));
            holder.rvUpdateNews.setLayoutManager(llmRvUpdateNews);
            holder.rvUpdateNews.setAdapter(mNotiRVAdapter);
        }

        @Override
        public int getItemCount() {
            return mDates.size();
        }

        class UpdateDateHolder extends RecyclerView.ViewHolder {
            TextView tvUpdateDate;
            RecyclerView rvUpdateNews;

            UpdateDateHolder(View itemView) {
                super(itemView);
                tvUpdateDate = (TextView) itemView.findViewById(R.id.tv_update_date);
                rvUpdateNews = (RecyclerView) itemView.findViewById(R.id.rv_update_news);
            }
        }
    }

    private class NotiRVAdapter extends RecyclerView.Adapter<NotiRVAdapter.UpdateNewsHolder> {
        List<LGCNotification> notiList;

        NotiRVAdapter() {

        }

        NotiRVAdapter(String createDate) {
            notiList = new ArrayList<>();
            AccountAdapter acc = new AccountAdapter(getActivity());
            notiList = acc.getNotiListByDate(createDate);
        }

        @Override
        public UpdateNewsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_update_news, parent, false);
            return new UpdateNewsHolder(v);
        }

        @Override
        public void onBindViewHolder(final UpdateNewsHolder holder, int position) {
            final LGCNotification notification = notiList.get(position);

            holder.tvMsgTitle.setText(notification.getContent());

            if (notiList.get(position).getStatus() == 1) {
                holder.llMsgInfo.setBackgroundColor(Color.WHITE);
            } else {
                holder.llMsgInfo.setBackgroundColor(getResources().getColor(R.color.msg_unread));
            }

            switch (notification.getLevel()) {
                case "Alert":
                    holder.imvNotification.setImageResource(R.drawable.ic_noti_alert);
                    break;
                case "Warning":
                    holder.imvNotification.setImageResource(R.drawable.ic_noti_warning);
                    break;
                case "News":
                    holder.imvNotification.setImageResource(R.drawable.ic_noti_info);
                    break;
                case "Message":
                    holder.imvNotification.setImageResource(R.drawable.ic_noti_message);
                    break;
                default:
                    holder.imvNotification.setImageResource(R.drawable.ic_noti_alert);
                    break;
            }

//            switch (notification.getCategory()) { //TODO
//                case "Schedule":
//                    holder.btnMsgStartClass.setVisibility(View.VISIBLE);
//                    break;
//                default:
//                    holder.btnMsgStartClass.setVisibility(View.GONE);
//            }

            holder.slNotification.setOnClickListener(onClickListener(position));
            holder.slNotification.addSwipeListener(new SwipeLayout.SwipeListener() {
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
                                            layout.setOnClickListener(onClickListener(holder.getAdapterPosition()));
                                        }
                                    });
                                }
                            }
                        }, 100);
                    }
                }
            });

            holder.imvDeleteNotification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notiList.remove(holder.getAdapterPosition());
                    deleteNotification(notification.getNotiId());

                    mDates = accountAdapter.getNotificationDate();
                    mRvUpdateDate.setAdapter(mUpdateDateAdapter);

                    if (mDates.size() == 0) {
                        mLlNoUpdateNews.setVisibility(View.VISIBLE);
                        mRvUpdateDate.setVisibility(View.GONE);
                    } else {
                        mLlNoUpdateNews.setVisibility(View.GONE);
                        mRvUpdateDate.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        private void deleteNotification(String notiId) {
            accountAdapter.deleteNotiById(notiId);

            requestManager.deleteNotification(notiId);
        }

        private View.OnClickListener onClickListener(final int position) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager fm = getFragmentManager();
                    Fragment updateNewsDetailFragment = new NotificationFragment();

                    Bundle bund = new Bundle();
                    bund.putString(NotificationFragment.EXTRA_NOTI_ID, notiList.get(position).getNotiId());
                    updateNewsDetailFragment.setArguments(bund);

                    Fragment frg = fm.findFragmentById(R.id.container);

                    if (frg == null) {
                        fm.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                .addToBackStack(null)
                                .add(R.id.container, updateNewsDetailFragment).commit();
                    } else {
                        fm.beginTransaction()
                                .setCustomAnimations(R.animator.fragment_in_new, R.animator.fragment_in_old,
                                        R.animator.fragment_out_new, R.animator.fragment_out_old)
                                .addToBackStack(null)
                                .replace(R.id.container, updateNewsDetailFragment).commit();
                    }
                }
            };
        }

        @Override
        public int getItemCount() {
            return notiList.size();
        }

        class UpdateNewsHolder extends RecyclerView.ViewHolder {
            SwipeLayout slNotification;
            ImageView imvNotification;
            TextView tvMsgTitle;
            TextView btnMsgStartClass;
            ImageView imvDeleteNotification;
            LinearLayout llMsgInfo;

            UpdateNewsHolder(View itemView) {
                super(itemView);
                imvNotification = (ImageView) itemView.findViewById(R.id.imv_notification);
                tvMsgTitle = (TextView) itemView.findViewById(R.id.tv_msg_title);
                btnMsgStartClass = (TextView) itemView.findViewById(R.id.btn_msg_start_class);
                imvDeleteNotification = (ImageView) itemView.findViewById(R.id.imv_delete_notification);
                slNotification = (SwipeLayout) itemView.findViewById(R.id.sl_notification);
                llMsgInfo = (LinearLayout) itemView.findViewById(R.id.ll_msg_info);
            }
        }
    }
}
