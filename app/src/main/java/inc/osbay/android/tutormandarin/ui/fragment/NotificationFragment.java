package inc.osbay.android.tutormandarin.ui.fragment;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.LGCNotification;
import inc.osbay.android.tutormandarin.util.CommonUtil;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends BackHandledFragment {
    public static final String EXTRA_NOTI_ID = "NotificationFragment.EXTRA_NOTI_ID";

    private LGCNotification mLGCNotification;

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String notiId = getArguments().getString(EXTRA_NOTI_ID);
        AccountAdapter accountAdapter = new AccountAdapter(getActivity());
        mLGCNotification = accountAdapter.getNotiById(notiId);
        if (notiId != null) {
            accountAdapter.setNotiRead(notiId);
        }

        if(mLGCNotification.getStatus() != 1){
            ServerRequestManager requestManager = new ServerRequestManager(getActivity());
            requestManager.readNotification(notiId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_notification, container, false);

        Toolbar toolBar;
        toolBar = (Toolbar) v.findViewById(R.id.tool_bar);
        toolBar.setBackgroundColor(Color.parseColor("#34404E"));
        setSupportActionBar(toolBar);

        TextView mTvNotiDetailDate = (TextView) v.findViewById(R.id.tv_noti_detail_date);
        TextView mTvNotiDetailMsg = (TextView) v.findViewById(R.id.tv_noti_detail_msg);
//        Button mBtnNotiDetailStartClass = (Button) v.findViewById(R.id.btn_noti_detail_start_class);
        ImageView mImvNotiDetail = (ImageView) v.findViewById(R.id.imv_noti_detail);

        mTvNotiDetailDate.setText(CommonUtil.getCustomDateResult(mLGCNotification.getSendDate(), "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm"));

        mTvNotiDetailMsg.setText(mLGCNotification.getContent());

        switch (mLGCNotification.getLevel()){
            case "Alert":
                mImvNotiDetail.setImageResource(R.drawable.ic_noti_alert);
                break;
            case "Warning":
                mImvNotiDetail.setImageResource(R.drawable.ic_noti_warning);
                break;
            case "News":
                mImvNotiDetail.setImageResource(R.drawable.ic_noti_info);
                break;
            case "Message":
                mImvNotiDetail.setImageResource(R.drawable.ic_noti_message);
                break;
            default:
                mImvNotiDetail.setImageResource(R.drawable.ic_noti_alert);
                break;
        }

//        switch (mLGCNotification.getCategory()){  // TODO
//            case "Schedule":
//                mBtnNotiDetailStartClass.setVisibility(View.VISIBLE);
//                break;
//            default:
//                mBtnNotiDetailStartClass.setVisibility(View.GONE);
//        }

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
}
