package inc.osbay.android.tutormandarin.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.database.AccountAdapter;
import inc.osbay.android.tutormandarin.sdk.model.LGCLocation;

public class AccountLocationActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String ACCOUNT_LOCATION_ID = "AccountLocationActivity.ACCOUNT_LOCATION_ID";

    AccountAdapter accountAdapter;
    private List<LGCLocation> mLocations;
    private LocationAdapter mLocationAdapter;

    private TextView mTvLocationEmpty;

    private LinearLayout mLlLocationEmpty;

    private String mRetrievedId;

    private String mSelectedId;

    private boolean b = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_location);

        mLocations = new ArrayList<>();

        accountAdapter = new AccountAdapter(this);
        mLocations = accountAdapter.getLocations();


        final ProgressDialog dialog = new ProgressDialog(AccountLocationActivity.this);
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.ac_dialog_location_loading));

        if (mLocations.size() == 0) {
            dialog.show();
        }
        ServerRequestManager serverRequestManager = new ServerRequestManager(AccountLocationActivity.this);
        serverRequestManager.downloadCountry(new ServerRequestManager.OnRequestFinishedListener() {
            @Override
            public void onSuccess(Object result) {
                dialog.dismiss();

                mLocations = accountAdapter.getLocations();
                mLocationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(ServerError err) {
                dialog.dismiss();
            }
        });

        mRetrievedId = getIntent().getStringExtra(ACCOUNT_LOCATION_ID);

        mSelectedId = mRetrievedId;

        ImageView imvAccountLocationBack = (ImageView) findViewById(R.id.imv_account_location_back);
        TextView tvAccountLocationSave = (TextView) findViewById(R.id.tv_account_location_save);
        RecyclerView rvLocations = (RecyclerView) findViewById(R.id.rv_locations);
        LinearLayoutManager llmLocation = new LinearLayoutManager(this);
        rvLocations.setLayoutManager(llmLocation);
        mLocationAdapter = new LocationAdapter();
        rvLocations.setAdapter(mLocationAdapter);

        mTvLocationEmpty = (TextView) findViewById(R.id.tv_location_empty);
        mLlLocationEmpty = (LinearLayout) findViewById(R.id.ll_location_empty);

        final EditText etSearchLocation = (EditText) findViewById(R.id.et_search_location);
        etSearchLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mRetrievedId = mSelectedId;
                AccountAdapter accountAdapter = new AccountAdapter(getApplicationContext());
                String title = etSearchLocation.getText().toString();

                if (!TextUtils.isEmpty(title)) {
                    mLocations = accountAdapter.searchLocationByTitle(title);
                } else {
                    mLocations = accountAdapter.getLocations();
                }

                for (int j = 0; j < mLocations.size(); j++) {
                    String locationId = accountAdapter.getLocationId(mLocations.get(j).getCityName(), mLocations.get(j).getCountryName());

                    if (locationId.equals(mRetrievedId)) {
                        b = true;
                        break;
                    } else {
                        b = false;
                    }
                }

                if (b) {
                    mRetrievedId = mSelectedId;
                } else {
                    mRetrievedId = String.valueOf(0);
                }

                mLocationAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        imvAccountLocationBack.setOnClickListener(this);
        tvAccountLocationSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imv_account_location_back:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.tv_account_location_save:
                Intent intent = new Intent();
                if (mLocations.size() == 0) {
                    mLlLocationEmpty.setVisibility(View.VISIBLE);
                    mTvLocationEmpty.setText(getString(R.string.ac_no_search_location));
                } else {
                    if (mRetrievedId.equals(String.valueOf(0))) {
                        mLlLocationEmpty.setVisibility(View.VISIBLE);
                        mTvLocationEmpty.setText(getString(R.string.ac_choose_location));
                    } else {
                        mLlLocationEmpty.setVisibility(View.GONE);
                        intent.putExtra(ACCOUNT_LOCATION_ID, String.valueOf(mRetrievedId));
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
                break;
        }
    }

    private class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationHolder> {
        LocationHolder tempHolder;

        @Override
        public LocationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_location, parent, false);
            return new LocationHolder(v);
        }

        @Override
        public void onBindViewHolder(final LocationHolder holder, int position) {
            holder.imvCountryCheck.setImageResource(R.drawable.ic_unchecked);
            holder.tvCityName.setText(Html.fromHtml("<b>" + mLocations.get(position).getCityName() + "</b> " + mLocations.get(position).getCountryName()));
            holder.tvCountryTimeZone.setText(mLocations.get(position).getTimeZone());

            if (mRetrievedId.equals(String.valueOf(mLocations.get(position).getCountryId()))) {
                tempHolder = holder;
                tempHolder.imvCountryCheck.setImageResource(R.drawable.ic_checked);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tempHolder == null) {
                        holder.imvCountryCheck.setImageResource(R.drawable.ic_checked);
                    } else {
                        tempHolder.imvCountryCheck.setImageResource(R.drawable.ic_unchecked);
                        holder.imvCountryCheck.setImageResource(R.drawable.ic_checked);
                    }
                    mRetrievedId = String.valueOf(mLocations.get(holder.getAdapterPosition()).getCountryId());
                    tempHolder = holder;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mLocations.size();
        }

        class LocationHolder extends RecyclerView.ViewHolder {
            TextView tvCityName;
            TextView tvCountryTimeZone;
            ImageView imvCountryCheck;

            public LocationHolder(View itemView) {
                super(itemView);
                tvCityName = (TextView) itemView.findViewById(R.id.tv_city_name);
                tvCountryTimeZone = (TextView) itemView.findViewById(R.id.tv_country_time_zone);
                imvCountryCheck = (ImageView) itemView.findViewById(R.id.imv_country_check);
            }
        }
    }
}
