package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Booking implements Serializable {

    private String mBookingId;

    private int mBookingType;  // 1.Credit Booking, 2.Package Booking 3.Topic Booking

    private String mTutorId;

    private String mStudentId;

    private String mLessonTopicId;

    private String mBookedDate;

    private int mBookingStatus;

    private String mClassRoomId;

    public Booking() {
    }

    public Booking(JSONObject json, int bookingType) throws JSONException, ParseException {
        mBookingType = bookingType;
        mBookingId = json.getString("booking_id");
        mBookingStatus = json.getInt("status");
        mTutorId = json.getString("tutor_id");
        mStudentId = json.getString("student_id");
        mClassRoomId = json.getString("classroom_id");
        setBookedDateUTC(json.getString("booked_date"));

        if (mBookingType == Type.TOPIC || mBookingType == Type.TRIAL) {
            mLessonTopicId = json.getString("curriculum_class_id");
        } else {
            mLessonTopicId = json.getString("curriculum_lesson_id");
        }
    }

    public String getBookingId() {
        return mBookingId;
    }

    public void setBookingId(String bookingId) {
        mBookingId = bookingId;
    }

    public int getBookingType() {
        return mBookingType;
    }

    public void setBookingType(int bookingType) {
        mBookingType = bookingType;
    }

    public String getTutorId() {
        return mTutorId;
    }

    public void setTutorId(String tutorId) {
        mTutorId = tutorId;
    }

    public String getStudentId() {
        return mStudentId;
    }

    public void setStudentId(String studentId) {
        mStudentId = studentId;
    }

    public String getLessonId() {
        return mLessonTopicId;
    }

    public void setLessonTopicId(String lessonTopicId) {
        mLessonTopicId = lessonTopicId;
    }

    public String getBookedDate() {
        return mBookedDate;
    }

    public void setBookedDate(String bookedDate) {
        mBookedDate = bookedDate;
    }

    public int getBookingStatus() {
        return mBookingStatus;
    }

    public void setBookingStatus(int bookingStatus) {
        mBookingStatus = bookingStatus;
    }

    public String getClassRoomId() {
        return mClassRoomId;
    }

    public void setClassRoomId(String classRoomId) {
        mClassRoomId = classRoomId;
    }

    public String getBookedDateUTC() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date localDate = dateFormat.parse(mBookedDate);

        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(localDate);
    }

    public void setBookedDateUTC(String bookedDateUTC) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date utcDate = dateFormat.parse(bookedDateUTC);

        dateFormat.setTimeZone(TimeZone.getDefault());
        mBookedDate = dateFormat.format(utcDate);
    }

    public static class Status {
        public static final int ACTIVE = 1;
//        public static final int CHANGE = 2;
        public static final int CANCEL = 3;
        public static final int FINISH = 4;
        public static final int MISS = 5;
    }

    public static class Type {
        public static final int LESSON = 1;
        public static final int PACKAGE = 2;
        public static final int TOPIC = 3;
        public static final int TRIAL = 4;
    }
}
