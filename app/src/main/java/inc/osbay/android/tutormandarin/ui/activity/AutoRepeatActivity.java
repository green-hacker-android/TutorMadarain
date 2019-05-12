package inc.osbay.android.tutormandarin.ui.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.client.ServerError;
import inc.osbay.android.tutormandarin.sdk.client.ServerRequestManager;
import inc.osbay.android.tutormandarin.sdk.model.Booking;
import inc.osbay.android.tutormandarin.sdk.util.LGCUtil;
import inc.osbay.android.tutormandarin.ui.fragment.BookingPackageFragment;
import inc.osbay.android.tutormandarin.util.CalendarUtil;
import inc.osbay.android.tutormandarin.util.CommonUtil;

public class AutoRepeatActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_WEEK_DAY = "AutoRepeatActivity.EXTRA_WEEK_DAY";
    public static final String EXTRA_START_TIME = "AutoRepeatActivity.EXTRA_START_TIME";

    private List<Map<String, String>> mSchedules;

    private AutoRepeatRVAdapter mAutoRepeatRVAdapter;
    private TextView mNoticeTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_repeat);

        mSchedules = new ArrayList<>();
        mSchedules.add(new HashMap<String, String>());
        mSchedules.add(new HashMap<String, String>());

        mAutoRepeatRVAdapter = new AutoRepeatRVAdapter();

        mNoticeTextView = (TextView) findViewById(R.id.tv_auto_repeat_notice);

        RecyclerView rvSchedules = (RecyclerView) findViewById(R.id.rv_auto_repeat);
        rvSchedules.setAdapter(mAutoRepeatRVAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvSchedules.setLayoutManager(layoutManager);

        TextView tvAutoRepeatOff = (TextView) findViewById(R.id.tv_auto_repeat_off);
        tvAutoRepeatOff.setOnClickListener(this);

        Button btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(this);

        Button btnApply = (Button) findViewById(R.id.btn_apply);
        btnApply.setOnClickListener(this);

        FloatingActionButton fabAddSchedule = (FloatingActionButton) findViewById(R.id.fab_add_auto_repeat);
        fabAddSchedule.setOnClickListener(this);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.ar_download_tutor_loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            final String startDate = LGCUtil.convertToUTC(CalendarUtil.getFirstDateStringOfWeek(1), "yyyy/MM/dd");
            final String endDate = LGCUtil.convertToUTC(CalendarUtil.getLastDateStringOfWeek(4), "yyyy/MM/dd");

            final ServerRequestManager requestManager = new ServerRequestManager(this);
            requestManager.downloadTutorList(new ServerRequestManager.OnRequestFinishedListener() {
                @Override
                public void onSuccess(Object result) {
                    if (AutoRepeatActivity.this != null) {
                        requestManager.downloadTutorSchedules(startDate,
                                endDate, Booking.Type.PACKAGE, new ServerRequestManager.OnRequestFinishedListener() {
                                    @Override
                                    public void onSuccess(Object result) {
                                        progressDialog.dismiss();
                                    }

                                    @Override
                                    public void onError(ServerError err) {
                                        progressDialog.dismiss();

                                        if (AutoRepeatActivity.this != null) {
                                            if (err.getErrorCode() == 10065) {
                                                new AlertDialog.Builder(AutoRepeatActivity.this)
                                                        .setTitle(getString(R.string.ar_unavailable_tutor_title))
                                                        .setMessage(getString(R.string.ar_unavailable_tutor_msg))
                                                        .setPositiveButton(getString(R.string.ar_unavailable_tutor_ok), null)
                                                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                            @Override
                                                            public void onDismiss(DialogInterface dialogInterface) {
                                                                AutoRepeatActivity.this.finish();
                                                            }
                                                        })
                                                        .create()
                                                        .show();
                                            } else {
                                                new AlertDialog.Builder(AutoRepeatActivity.this)
                                                        .setTitle(getString(R.string.ar_server_error))
                                                        .setMessage(err.getMessage())
                                                        .setPositiveButton(getString(R.string.ar_server_error_ok), null)
                                                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                            @Override
                                                            public void onDismiss(DialogInterface dialogInterface) {
                                                                AutoRepeatActivity.this.finish();
                                                            }
                                                        })
                                                        .create()
                                                        .show();
                                            }
                                        }
                                    }
                                });
                    }
                }

                @Override
                public void onError(ServerError err) {
                    progressDialog.dismiss();

                    if (AutoRepeatActivity.this != null) {
                        new AlertDialog.Builder(AutoRepeatActivity.this)
                                .setTitle(getString(R.string.ar_server_error))
                                .setMessage(err.getMessage())
                                .setPositiveButton(getString(R.string.ar_server_error_ok), null)
                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        AutoRepeatActivity.this.finish();
                                    }
                                })
                                .create()
                                .show();
                    }
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }

        refreshUI();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_auto_repeat_off:
            case R.id.btn_cancel:
                onBackPressed();
                break;
            case R.id.btn_apply:
                if (isValid()) {
                    Intent data = new Intent();
                    data.putExtra(BookingPackageFragment.EXTRA_AUTO_REPEAT_SCHEDULES, getSchedules());

                    setResult(RESULT_OK, data);
                    AutoRepeatActivity.this.finish();
                } else {
                    new AlertDialog.Builder(AutoRepeatActivity.this)
                            .setTitle(getString(R.string.ar_sd_fill_title))
                            .setMessage(getString(R.string.ar_sd_fill_msg))
                            .setPositiveButton(getString(R.string.ar_sd_fill_ok), null)
                            .create()
                            .show();
                }
                break;
            case R.id.fab_add_auto_repeat:
                mSchedules.add(new HashMap<String, String>());
                refreshUI();
                break;
        }
    }

    private boolean isValid() {
        for (Map<String, String> item : mSchedules) {
            if (item.isEmpty())
                return false;
        }
        return true;
    }

    private ArrayList<String> getSchedules() {
        ArrayList<String> schedules = new ArrayList<>();

        Calendar calendar = GregorianCalendar.getInstance(Locale.getDefault());
        for (int i = 0; i < 28; i++) {
            calendar.add(Calendar.DATE, 1);

            for (Map<String, String> item : mSchedules) {
                int weekDay = 0;
                switch (item.get("week_day")) {
                    case "SUNDAY":
                        weekDay = Calendar.SUNDAY;
                        break;
                    case "MONDAY":
                        weekDay = Calendar.MONDAY;
                        break;
                    case "TUESDAY":
                        weekDay = Calendar.TUESDAY;
                        break;
                    case "WEDNESDAY":
                        weekDay = Calendar.WEDNESDAY;
                        break;
                    case "THURSDAY":
                        weekDay = Calendar.THURSDAY;
                        break;
                    case "FRIDAY":
                        weekDay = Calendar.FRIDAY;
                        break;
                    case "SATURDAY":
                        weekDay = Calendar.SATURDAY;
                        break;
                }

                if (calendar.get(Calendar.DAY_OF_WEEK) == weekDay) {
                    schedules.add(CommonUtil.getFormattedDate(calendar.getTime(), "yyyy-MM-dd", false)
                            + " " + item.get("start_time") + ":00");
                }
            }
        }

        return schedules;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String weekDay = data.getStringExtra(EXTRA_WEEK_DAY);
            String startTime = data.getStringExtra(EXTRA_START_TIME);

            Map<String, String> schedule = new HashMap<>();
            schedule.put("week_day", weekDay);
            schedule.put("start_time", startTime);

            mSchedules.set(requestCode, schedule);
            refreshUI();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        AutoRepeatActivity.this.finish();
    }

    private void refreshUI() {
        mAutoRepeatRVAdapter.notifyDataSetChanged();
        if (mSchedules.size() > 1) {
            mNoticeTextView.setVisibility(View.GONE);
        } else {
            mNoticeTextView.setVisibility(View.VISIBLE);
        }
    }

    private class AutoRepeatRVAdapter extends RecyclerView.Adapter<AutoRepeatRVAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(AutoRepeatActivity.this).inflate(R.layout.item_auto_repeat,
                    parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Map<String, String> item = mSchedules.get(position);

            if (item.isEmpty()) {
                holder.tvStartTime.setVisibility(View.GONE);
                holder.tvStartLabel.setVisibility(View.GONE);

                holder.tvWeekDay.setTextColor(ContextCompat.getColor(AutoRepeatActivity.this, R.color.text_color_gray));
                holder.tvWeekDay.setText(getString(R.string.ar_select_date));
            } else {
                holder.tvStartTime.setVisibility(View.VISIBLE);
                holder.tvStartLabel.setVisibility(View.VISIBLE);

                holder.tvWeekDay.setTextColor(ContextCompat.getColor(AutoRepeatActivity.this, R.color.text_color_black));
                // TODO show selected day and time
                holder.tvWeekDay.setText(item.get("week_day"));
                holder.tvStartTime.setText(item.get("start_time"));
            }

            final SwipeLayout swipeLayout = (SwipeLayout) holder.itemView;
            swipeLayout.setOnClickListener(onClickListener(position));
            swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
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
                                if (AutoRepeatActivity.this != null) {
                                    runOnUiThread(new Runnable() {
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

            holder.imvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mSchedules.size() > 1) {
                        mSchedules.remove(holder.getAdapterPosition());
                        refreshUI();
                    } else {
                        new AlertDialog.Builder(AutoRepeatActivity.this)
                                .setTitle(getString(R.string.ar_book_class_error))
                                .setMessage(getString(R.string.ar_book_class_msg))
                                .setPositiveButton(getString(R.string.ar_book_class_ok), null)
                                .create()
                                .show();
                    }
                }
            });
        }

        private View.OnClickListener onClickListener(final int position) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent editRepeatIntent = new Intent(AutoRepeatActivity.this, EditRepeatActivity.class);
                    startActivityForResult(editRepeatIntent, position);
                }
            };
        }

        @Override
        public int getItemCount() {
            return mSchedules.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView tvStartLabel;
            TextView tvWeekDay;
            TextView tvStartTime;
            ImageView imvDelete;

            public ViewHolder(View itemView) {
                super(itemView);

                tvStartLabel = (TextView) itemView.findViewById(R.id.tv_start_lbl);
                tvWeekDay = (TextView) itemView.findViewById(R.id.tv_week_day);
                tvStartTime = (TextView) itemView.findViewById(R.id.tv_start_time);
                imvDelete = (ImageView) itemView.findViewById(R.id.imv_delete_schedule);
            }
        }
    }
}
