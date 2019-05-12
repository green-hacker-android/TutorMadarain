package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Schedule {
    private String mScheduleId;
    private String mTutorId;
    private String mStartTime;
    private String mEndTime;

    public Schedule(){}

    public Schedule(String tutorId, JSONObject jsonObject) throws JSONException, ParseException {
        mTutorId = tutorId;
        mScheduleId = jsonObject.getString("schedule_id");
        setStartTimeUTC(jsonObject.getString("start_date"));
        setEndTimeUTC(jsonObject.getString("end_date"));
    }

    public String getScheduleId() {
        return mScheduleId;
    }

    public void setScheduleId(String scheduleId) {
        mScheduleId = scheduleId;
    }

    public String getTutorId() {
        return mTutorId;
    }

    public void setTutorId(String tutorId) {
        mTutorId = tutorId;
    }

    public String getStartTime() {
        return mStartTime;
    }

    public void setStartTime(String startTime) {
        mStartTime = startTime;
    }

    public String getEndTime() {
        return mEndTime;
    }

    public void setEndTime(String endTime) {
        mEndTime = endTime;
    }

    public String getStartTimeUTC() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date localDate = dateFormat.parse(mStartTime);

        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(localDate);
    }

    public void setStartTimeUTC(String startTimeUTC) throws ParseException {
        Log.e("Schedule Model", "start time utc - " + startTimeUTC);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date utcDate = dateFormat.parse(startTimeUTC);

        dateFormat.setTimeZone(TimeZone.getDefault());
        mStartTime = dateFormat.format(utcDate);

        Log.e("Schedule Model", "start time local - " + mStartTime);
    }

    public String getEndTimeUTC() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date localDate = dateFormat.parse(mEndTime);

        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(localDate);
    }

    public void setEndTimeUTC(String endTimeUTC) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date utcDate = dateFormat.parse(endTimeUTC);

        dateFormat.setTimeZone(TimeZone.getDefault());
        mEndTime = dateFormat.format(utcDate);
    }
}
