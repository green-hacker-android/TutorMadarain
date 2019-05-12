package inc.osbay.android.tutormandarin.sdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import inc.osbay.android.tutormandarin.sdk.model.Account;
import inc.osbay.android.tutormandarin.sdk.model.Badges;
import inc.osbay.android.tutormandarin.sdk.model.Booking;
import inc.osbay.android.tutormandarin.sdk.model.CountryCode;
import inc.osbay.android.tutormandarin.sdk.model.LGCLocation;
import inc.osbay.android.tutormandarin.sdk.model.LGCNotification;
import inc.osbay.android.tutormandarin.sdk.model.Note;
import inc.osbay.android.tutormandarin.sdk.model.PromoCodeEnter;
import inc.osbay.android.tutormandarin.sdk.model.PromoCodeRefer;
import inc.osbay.android.tutormandarin.sdk.model.StudentPackage;

public class AccountAdapter {
    protected static final String TAG = AccountAdapter.class.getSimpleName();

    private DataBaseHelper mDbHelper;

    public AccountAdapter(Context context) {
        mDbHelper = new DataBaseHelper(context);
    }

    public void insertBookings(List<Booking> bookingList) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        try {
            db.beginTransaction();

            db.delete("booking", null, null);

            for (Booking booking : bookingList) {
                ContentValues values = new ContentValues();
                values.put("booking_id", booking.getBookingId());
                values.put("booking_status", booking.getBookingStatus());
                values.put("tutor_id", booking.getTutorId());
                values.put("student_id", booking.getStudentId());
                values.put("curriculum_lesson_id", booking.getLessonId());
//                values.put("curriculum_topic_id", booking.getmCurriculumTopicId());
                values.put("classroom_id", booking.getClassRoomId());
                values.put("booked_date", booking.getBookedDate());
                values.put("booking_type", booking.getBookingType());
                db.insert("booking", null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Cannot insert bookings.", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] activeStatus = {String.valueOf(Booking.Status.ACTIVE)};
        Cursor cursor = db.query("booking", null, "booking_status = ?", activeStatus, null, null, "booked_date ASC");

        Cursor cursor1 = db.query("booking", null, "booking_status <> ?", activeStatus, null, null, "booked_date DESC");
        try {
            if (cursor.moveToFirst()) {
                do {
                    Booking b = new Booking();
                    b.setBookingId(cursor.getString(cursor.getColumnIndex("booking_id")));
                    b.setBookingStatus(cursor.getInt(cursor.getColumnIndex("booking_status")));
                    b.setTutorId(cursor.getString(cursor.getColumnIndex("tutor_id")));
                    b.setStudentId(cursor.getString(cursor.getColumnIndex("student_id")));
                    b.setLessonTopicId(cursor.getString(cursor.getColumnIndex("curriculum_lesson_id")));
                    b.setBookedDate(cursor.getString(cursor.getColumnIndex("booked_date")));
                    b.setBookingType(cursor.getInt(cursor.getColumnIndex("booking_type")));
                    b.setClassRoomId(cursor.getString(cursor.getColumnIndex("classroom_id")));

                    bookings.add(b);
                } while (cursor.moveToNext());
            }

            if (cursor1.moveToFirst()) {
                do {
                    Booking b = new Booking();
                    b.setBookingId(cursor1.getString(cursor1.getColumnIndex("booking_id")));
                    b.setBookingStatus(cursor1.getInt(cursor1.getColumnIndex("booking_status")));
                    b.setTutorId(cursor1.getString(cursor1.getColumnIndex("tutor_id")));
                    b.setStudentId(cursor1.getString(cursor1.getColumnIndex("student_id")));
                    b.setLessonTopicId(cursor1.getString(cursor1.getColumnIndex("curriculum_lesson_id")));
                    b.setBookedDate(cursor1.getString(cursor1.getColumnIndex("booked_date")));
                    b.setBookingType(cursor1.getInt(cursor1.getColumnIndex("booking_type")));
                    b.setClassRoomId(cursor1.getString(cursor1.getColumnIndex("classroom_id")));

                    bookings.add(b);
                } while (cursor1.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Bookings update fail.", e);
        } finally {
            cursor.close();
            cursor1.close();
            db.close();
        }

        return bookings;
    }

    public Booking getBookingById(String bookingId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] selectionArgs = {bookingId};

        String sql = "SELECT * from booking WHERE booking_id = ?";

        Cursor cursor = db.rawQuery(sql, selectionArgs);
        try {
            if (cursor.moveToFirst()) {
                Booking b = new Booking();

                b.setBookingId(cursor.getString(cursor.getColumnIndex("booking_id")));
                b.setBookingStatus(cursor.getInt(cursor.getColumnIndex("booking_status")));
                b.setTutorId(cursor.getString(cursor.getColumnIndex("tutor_id")));
                b.setStudentId(cursor.getString(cursor.getColumnIndex("student_id")));
                b.setLessonTopicId(cursor.getString(cursor.getColumnIndex("curriculum_lesson_id")));
                b.setBookedDate(cursor.getString(cursor.getColumnIndex("booked_date")));
                b.setBookingType(cursor.getInt(cursor.getColumnIndex("booking_type")));
                b.setClassRoomId(cursor.getString(cursor.getColumnIndex("classroom_id")));

                return b;
            }
        } catch (Exception e) {
            Log.e(TAG, "Bookings update fail.", e);
        } finally {
            cursor.close();
            db.close();
        }

        return null;
    }

    public void insertAccount(Account account) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("account_id", account.getAccountId());
        values.put("email", account.getEmail());
        values.put("first_name", account.getFirstName());
        values.put("last_name", account.getLastName());
        values.put("ph_code", account.getPhoneCode());
        values.put("ph_number", account.getPhoneNumber());
        values.put("avatar", account.getAvatar());
        values.put("credit", account.getCredit());
        values.put("status", account.getStatus());
        values.put("gender", account.getGender());
        values.put("country", account.getCountry());
        values.put("city", account.getCity());
        values.put("time_zone", account.getTimeZone());
        values.put("chinese_level", account.getChineseLevel());
        values.put("interest_in", account.getInterestIn());
        values.put("occupation", account.getOccupation());
        values.put("learning_goal", account.getLearningGoal());
        values.put("pronunciation", account.getPronunciation());
        values.put("speaking", account.getSpeaking());
        values.put("speakinglang", account.getSpeakingLang());
        values.put("listening", account.getListening());
        values.put("update_date", account.getUpdateDate());

        try {
            db.beginTransaction();

            db.delete("account", null, null);

            db.insert("account", null, values);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Cannot parse account object.");
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void updateAccountInfo(Account account) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("first_name", account.getFirstName());
        values.put("last_name", account.getLastName());
        values.put("email", account.getEmail());
        values.put("country", account.getCountry());
        values.put("gender", account.getGender());
        values.put("city", account.getCity());
        values.put("time_zone", account.getTimeZone());
        values.put("ph_code", account.getPhoneCode());
        values.put("ph_number", account.getPhoneNumber());
        values.put("interest_in", account.getInterestIn());
        values.put("speaking", account.getSpeaking());
        values.put("speakinglang", account.getSpeakingLang());
        values.put("learning_goal", account.getLearningGoal());

        String whereClause = "account_id = ?";
        String[] whereArgs = {account.getAccountId()};

        db.update("account", values, whereClause, whereArgs);

        db.close();
    }

    public void updateImage(String accountId, String imageUrl) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put("avatar", imageUrl);

            String whereClause = "account_id = ?";
            String[] whereArgs = {accountId};

            db.update("account", values, whereClause, whereArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Cannot parse account object.");
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public Account getAccountById(String accountId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] selectionArgs = {
                accountId
        };

        String sql = "SELECT * FROM account WHERE account_id = ?";
        Cursor mCur = db.rawQuery(sql, selectionArgs);
        try {
            if (mCur.moveToFirst()) {
                Account account = new Account();

                account.setAccountId(mCur.getString(mCur.getColumnIndex("account_id")));
                account.setEmail(mCur.getString(mCur.getColumnIndex("email")));
                account.setFirstName(mCur.getString(mCur.getColumnIndex("first_name")));
                account.setLastName(mCur.getString(mCur.getColumnIndex("last_name")));
                account.setPhoneCode(mCur.getString(mCur.getColumnIndex("ph_code")));
                account.setPhoneNumber(mCur.getString(mCur.getColumnIndex("ph_number")));
                account.setAvatar(mCur.getString(mCur.getColumnIndex("avatar")));
                account.setCredit(mCur.getDouble(mCur.getColumnIndex("credit")));
                account.setStatus(mCur.getInt(mCur.getColumnIndex("status")));
                account.setGender(mCur.getString(mCur.getColumnIndex("gender")));
                account.setCountry(mCur.getString(mCur.getColumnIndex("country")));
                account.setCity(mCur.getString(mCur.getColumnIndex("city")));
                account.setTimeZone(mCur.getString(mCur.getColumnIndex("time_zone")));
                account.setChineseLevel(mCur.getString(mCur.getColumnIndex("chinese_level")));
                account.setInterestIn(mCur.getString(mCur.getColumnIndex("interest_in")));
                account.setOccupation(mCur.getString(mCur.getColumnIndex("occupation")));
                account.setLearningGoal(mCur.getString(mCur.getColumnIndex("learning_goal")));
                account.setPronunciation(mCur.getString(mCur.getColumnIndex("pronunciation")));
                account.setListening(mCur.getString(mCur.getColumnIndex("listening")));
                account.setSpeaking(mCur.getString(mCur.getColumnIndex("speaking")));
                account.setUpdateDate(mCur.getString(mCur.getColumnIndex("update_date")));
                account.setSpeakingLang(mCur.getString(mCur.getColumnIndex("speakinglang")));

                return account;
            }
        } finally {
            mCur.close();
            db.close();
        }

        return null;
    }

    public void updateAccountCredit(String accountId, double credit) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("credit", credit);

        String whereClause = "account_id = ?";
        String[] whereArgs = {accountId};

        db.update("account", values, whereClause, whereArgs);

        db.close();
    }

    public String getUpComingDate(int duration) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String comingDate = "";
        String sql = "SELECT booked_date FROM booking WHERE booking_status = 1 " +
                "AND booked_date > DATETIME('now','localtime', ?) ORDER BY booked_date LIMIT 1";
        String[] selectionArgs = {"-" + duration + " minutes"};
        Cursor mCur = db.rawQuery(sql, selectionArgs);
        try {
            if (mCur.moveToFirst()) {
                comingDate = mCur.getString(mCur.getColumnIndex("booked_date"));
            }
        } finally {
            mCur.close();
            db.close();
        }

        return comingDate;
    }

    public Booking getUpComingBooking(int duration) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String sql = "SELECT * FROM booking WHERE booking_status = 1 " +
                "AND booked_date > DATETIME('now','localtime', ?) ORDER BY booked_date LIMIT 1";
        String[] selectionArgs = {"-" + duration + " minutes"};
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        try {
            if (cursor.moveToFirst()) {
                Booking b = new Booking();

                b.setBookingId(cursor.getString(cursor.getColumnIndex("booking_id")));
                b.setBookingStatus(cursor.getInt(cursor.getColumnIndex("booking_status")));
                b.setTutorId(cursor.getString(cursor.getColumnIndex("tutor_id")));
                b.setStudentId(cursor.getString(cursor.getColumnIndex("student_id")));
                b.setLessonTopicId(cursor.getString(cursor.getColumnIndex("curriculum_lesson_id")));
                b.setBookedDate(cursor.getString(cursor.getColumnIndex("booked_date")));
                b.setBookingType(cursor.getInt(cursor.getColumnIndex("booking_type")));
                b.setClassRoomId(cursor.getString(cursor.getColumnIndex("classroom_id")));

                return b;
            }
        } finally {
            cursor.close();
            db.close();
        }

        return null;
    }

    public void insertStudentPackage(List<StudentPackage> studentPackageList) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            db.beginTransaction();

            db.delete("package", null, null);

            for (StudentPackage studentPackage : studentPackageList) {
                ContentValues values = new ContentValues();
                values.put("package_id", studentPackage.getPackageId());
                values.put("package_name", studentPackage.getPackageName());
                values.put("curriculum_course_id", studentPackage.getCourseId());
                values.put("package_type", studentPackage.getPackageType());
                values.put("package_level", studentPackage.getPackageLevel());
                values.put("package_amount", studentPackage.getPackageAmount());
                values.put("package_create_date", studentPackage.getPackageCreateDate());
                values.put("section", studentPackage.getSectionCount());
                values.put("lesson", studentPackage.getLessonCount());
                values.put("active_expired_date", studentPackage.getActiveExpireDate());
                values.put("packaged_expired_date", studentPackage.getPackageExpireDate());
                values.put("active_date", studentPackage.getActiveDate());
                values.put("purchased_date", studentPackage.getPurchasedDate());
                values.put("finished_date", studentPackage.getFinishedDate());
                values.put("package_status", studentPackage.getStatus());
                values.put("cover_photo", studentPackage.getCoverPhoto());
                values.put("thumb_photo", studentPackage.getThumbPhoto());
                values.put("supplement_count", studentPackage.getSupplementCount());
                values.put("finish_lesson", studentPackage.getFinishedLessonCount());

                db.insert("package", null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Cannot parse student package object.");
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<StudentPackage> getStudentPackageList() {
        List<StudentPackage> studentPackages = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String sql = "SELECT * FROM package";
        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                do {
                    StudentPackage studentPackage = new StudentPackage();

                    studentPackage.setPackageId(mCur.getString(mCur.getColumnIndex("package_id")));
                    studentPackage.setPackageName(mCur.getString(mCur.getColumnIndex("package_name")));
                    studentPackage.setCourseId(mCur.getString(mCur.getColumnIndex("curriculum_course_id")));
                    studentPackage.setPackageType(mCur.getInt(mCur.getColumnIndex("package_type")));
                    studentPackage.setPackageLevel(mCur.getString(mCur.getColumnIndex("package_level")));
                    studentPackage.setPackageAmount(mCur.getDouble(mCur.getColumnIndex("package_amount")));
                    studentPackage.setPackageCreateDate(mCur.getString(mCur.getColumnIndex("package_create_date")));
                    studentPackage.setSectionCount(mCur.getInt(mCur.getColumnIndex("section")));
                    studentPackage.setLessonCount(mCur.getInt(mCur.getColumnIndex("lesson")));
                    studentPackage.setActiveExpireDate(mCur.getString(mCur.getColumnIndex("active_expired_date")));
                    studentPackage.setPackageExpireDate(mCur.getString(mCur.getColumnIndex("packaged_expired_date")));
                    studentPackage.setActiveDate(mCur.getString(mCur.getColumnIndex("active_date")));
                    studentPackage.setPurchasedDate(mCur.getString(mCur.getColumnIndex("purchased_date")));
                    studentPackage.setFinishedDate(mCur.getString(mCur.getColumnIndex("finished_date")));
                    studentPackage.setStatus(mCur.getInt(mCur.getColumnIndex("package_status")));
                    studentPackage.setCoverPhoto(mCur.getString(mCur.getColumnIndex("cover_photo")));
                    studentPackage.setThumbPhoto(mCur.getString(mCur.getColumnIndex("thumb_photo")));
                    studentPackage.setSupplementCount(mCur.getInt(mCur.getColumnIndex("supplement_count")));
                    studentPackage.setFinishedLessonCount(mCur.getInt(mCur.getColumnIndex("finish_lesson")));

                    studentPackages.add(studentPackage);
                } while (mCur.moveToNext());
            }
        } finally {
            db.close();
            mCur.close();
        }
        return studentPackages;
    }

    public StudentPackage getStudentPackage(String packageId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] args = {packageId};
        String sql = "SELECT * FROM package WHERE package_id = ?";
        Cursor mCur = db.rawQuery(sql, args);
        try {
            if (mCur.moveToFirst()) {
                StudentPackage studentPackage = new StudentPackage();

                studentPackage.setPackageId(mCur.getString(mCur.getColumnIndex("package_id")));
                studentPackage.setPackageName(mCur.getString(mCur.getColumnIndex("package_name")));
                studentPackage.setCourseId(mCur.getString(mCur.getColumnIndex("curriculum_course_id")));
                studentPackage.setPackageType(mCur.getInt(mCur.getColumnIndex("package_type")));
                studentPackage.setPackageLevel(mCur.getString(mCur.getColumnIndex("package_level")));
                studentPackage.setPackageAmount(mCur.getDouble(mCur.getColumnIndex("package_amount")));
                studentPackage.setPackageCreateDate(mCur.getString(mCur.getColumnIndex("package_create_date")));
                studentPackage.setSectionCount(mCur.getInt(mCur.getColumnIndex("section")));
                studentPackage.setLessonCount(mCur.getInt(mCur.getColumnIndex("lesson")));
                studentPackage.setActiveExpireDate(mCur.getString(mCur.getColumnIndex("active_expired_date")));
                studentPackage.setPackageExpireDate(mCur.getString(mCur.getColumnIndex("packaged_expired_date")));
                studentPackage.setActiveDate(mCur.getString(mCur.getColumnIndex("active_date")));
                studentPackage.setPurchasedDate(mCur.getString(mCur.getColumnIndex("purchased_date")));
                studentPackage.setFinishedDate(mCur.getString(mCur.getColumnIndex("finished_date")));
                studentPackage.setStatus(mCur.getInt(mCur.getColumnIndex("package_status")));
                studentPackage.setCoverPhoto(mCur.getString(mCur.getColumnIndex("cover_photo")));
                studentPackage.setThumbPhoto(mCur.getString(mCur.getColumnIndex("thumb_photo")));
                studentPackage.setSupplementCount(mCur.getInt(mCur.getColumnIndex("supplement_count")));
                studentPackage.setFinishedLessonCount(mCur.getInt(mCur.getColumnIndex("finish_lesson")));

                return studentPackage;
            }
        } finally {
            db.close();
            mCur.close();
        }
        return null;
    }

    public StudentPackage getStudentPackageLevel() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String sql = "SELECT package_level,lesson,finish_lesson,supplement_count FROM package ORDER BY package_id ASC LIMIT 1";
        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                StudentPackage studentPackage = new StudentPackage();
                studentPackage.setPackageLevel(mCur.getString(mCur.getColumnIndex("package_level")));
                studentPackage.setLessonCount(mCur.getInt(mCur.getColumnIndex("lesson")));
                studentPackage.setFinishedLessonCount(mCur.getInt(mCur.getColumnIndex("finish_lesson")));
                studentPackage.setSupplementCount(mCur.getInt(mCur.getColumnIndex("supplement_count")));
                return studentPackage;
            }
        } finally {
            mCur.close();
            db.close();
        }

        return null;
    }

    public void deleteAllTableData() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        db.delete("account", null, null);
        db.delete("badges", null, null);
        db.delete("booking", null, null);
        db.delete("country", null, null);
        db.delete("course", null, null);
        db.delete("custom_flashcard", null, null);
        db.delete("dialog", null, null);
        db.delete("flashcard", null, null);
        db.delete("flashcard_deck", null, null);
        db.delete("lesson", null, null);
        db.delete("note", null, null);
        db.delete("notification", null, null);
        db.delete("package", null, null);
        db.delete("tutor", null, null);
        db.delete("tutor_schedules", null, null);
        db.delete("video", null, null);
        db.delete("whatson", null, null);
        db.delete("whats_on_vocab", null, null);
        db.delete("topic", null, null);
        db.delete("topic_class", null, null);
        db.delete("trial_class", null, null);
        db.delete("promo_code_referrer", null, null);
        db.delete("promo_code_enter", null, null);
    }

    public void insertNote(List<Note> notes) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            db.beginTransaction();

            db.delete("note", null, null);

            for (Note note : notes) {
                ContentValues values = new ContentValues();
                values.put("note_id", note.getNoteId());
                values.put("note_title", note.getNoteTitle());
                values.put("note_finished_date", note.getNoteFinishDate());
                values.put("note_lesson_title", note.getContentTitle());
                values.put("note_class_summary", note.getClassSummary());
                values.put("note_vocab", note.getVocab());
                values.put("note_grammar", note.getGrammar());
                values.put("note_pronunciation", note.getPronunciation());
                values.put("photo_url", note.getPhotoUrl());
                values.put("note_speaking", note.getNoteSpeaking());
                values.put("note_listening", note.getNoteListening());

                db.insert("note", null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Cannot parse note object.");
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public String getLastNote() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String sql = "SELECT note_title FROM note ORDER BY note_id DESC LIMIT 1";

        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                return mCur.getString(mCur.getColumnIndex("note_title"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Cannot parse note object.");
        } finally {
            mCur.close();
            db.close();
        }
        return null;
    }

    public List<Note> getNotes() {
        List<Note> mNotes = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String sql = "SELECT * FROM note ORDER BY note_finished_date DESC";

        Cursor cursor = db.rawQuery(sql, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Note note = new Note();

                    note.setNoteId(cursor.getInt(cursor.getColumnIndex("note_id")));
                    note.setNoteTitle(cursor.getString(cursor.getColumnIndex("note_title")));
                    note.setNoteFinishDate(cursor.getString(cursor.getColumnIndex("note_finished_date")));
                    note.setNoteLessonTitle(cursor.getString(cursor.getColumnIndex("note_lesson_title")));
                    note.setNoteClassSummary(cursor.getString(cursor.getColumnIndex("note_class_summary")));
                    note.setNoteVocab(cursor.getString(cursor.getColumnIndex("note_vocab")));
                    note.setNoteGrammar(cursor.getString(cursor.getColumnIndex("note_grammar")));
                    note.setNotePronunciation(cursor.getString(cursor.getColumnIndex("note_pronunciation")));
                    note.setPhotoUrl(cursor.getString(cursor.getColumnIndex("photo_url")));
                    note.setNoteSpeaking(cursor.getString(cursor.getColumnIndex("note_speaking")));
                    note.setNoteListening(cursor.getString(cursor.getColumnIndex("note_listening")));

                    mNotes.add(note);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Note select fail.", e);
        } finally {
            cursor.close();
            db.close();
        }
        return mNotes;
    }

    public void insertBadges(List<Badges> badges) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            db.beginTransaction();

            db.delete("badges", null, null);

            for (Badges badge : badges) {
                ContentValues values = new ContentValues();
                values.put("badges_id", badge.getBadgesId());
                values.put("badges_name", badge.getBadgesName());
                values.put("credit", badge.getCredit());
                values.put("award_type", badge.getAwardType());
                values.put("achievement_msg", badge.getAchievementMsg());
                values.put("completed_date", badge.getCompletedDate());
                values.put("completed_percent", badge.getCompletedPercent());
                values.put("badges_category", badge.getBadgesCategory());

                db.insert("badges", null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Cannot parse note object.");
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<Badges> getBadges() {
        List<Badges> mBadges = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String sql = "SELECT * FROM badges";

        Cursor cursor = db.rawQuery(sql, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Badges badge = new Badges();
                    badge.setBadgesId(cursor.getInt(cursor.getColumnIndex("badges_id")));
                    badge.setBadgesName(cursor.getString(cursor.getColumnIndex("badges_name")));
                    badge.setCredit(cursor.getDouble(cursor.getColumnIndex("credit")));
                    badge.setAwardType(cursor.getInt(cursor.getColumnIndex("award_type")));
                    badge.setAchievementMsg(cursor.getString(cursor.getColumnIndex("achievement_msg")));
                    badge.setCompletedDate(cursor.getString(cursor.getColumnIndex("completed_date")));
                    badge.setCompletedPercent(cursor.getInt(cursor.getColumnIndex("completed_percent")));
                    badge.setBadgesCategory(cursor.getInt(cursor.getColumnIndex("badges_category")));

                    mBadges.add(badge);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Badges select fail.", e);
        } finally {
            cursor.close();
            db.close();
        }
        return mBadges;
    }

    public int getBadgesCount() {
        int count;
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String sql = "SELECT * FROM badges WHERE completed_percent = 100";

        Cursor mCur = db.rawQuery(sql, null);
        count = mCur.getCount();
        mCur.close();
        return count;
    }

    public void insertNotification(LGCNotification noti) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("noti_id", noti.getNotiId());
            values.put("type", noti.getType());
            values.put("category", noti.getCategory());
            values.put("level", noti.getLevel());
            values.put("content", noti.getContent());
            values.put("send_date", noti.getSendDate());
            values.put("status", noti.getStatus());

            db.insert("notification", null, values);
        } catch (Exception e) {
            Log.e(TAG, "Cannot parse notification object.");
        } finally {
            db.close();
        }
    }

    public void insertNotifications(List<LGCNotification> notis) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            for (LGCNotification noti : notis) {
                ContentValues values = new ContentValues();
                values.put("noti_id", noti.getNotiId());
                values.put("type", noti.getType());
                values.put("category", noti.getCategory());
                values.put("level", noti.getLevel());
                values.put("content", noti.getContent());
                values.put("send_date", noti.getSendDate());
                values.put("status", noti.getStatus());

                db.insertWithOnConflict("notification", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Cannot parse notification object.");
        } finally {
            db.close();
        }
    }

    public List<LGCNotification> getNotiListByDate(String createDate) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<LGCNotification> notificationList = new ArrayList<>();
        String sql = "SELECT * FROM notification WHERE status <> 2 AND strftime('%Y-%m-%d', send_date) LIKE '" + createDate + "' ORDER BY send_date DESC";
        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                do {
                    LGCNotification noti = new LGCNotification();
                    noti.setNotiId(mCur.getString(mCur.getColumnIndex("noti_id")));
                    noti.setType(mCur.getString(mCur.getColumnIndex("type")));
                    noti.setCategory(mCur.getString(mCur.getColumnIndex("category")));
                    noti.setLevel(mCur.getString(mCur.getColumnIndex("level")));
                    noti.setContent(mCur.getString(mCur.getColumnIndex("content")));
                    noti.setSendDate(mCur.getString(mCur.getColumnIndex("send_date")));
                    noti.setStatus(mCur.getInt(mCur.getColumnIndex("status")));

                    notificationList.add(noti);
                } while (mCur.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Cannot parse notification object.");
        } finally {
            db.close();
            mCur.close();
        }
        return notificationList;
    }

    public void deleteNotiById(String notiId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("status", "2");

        String whereClause = "noti_id = ?";
        String[] whereArgs = {notiId};

        try {
            db.update("notification", values, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    public LGCNotification getNotiById(String notiId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String sql = "SELECT * FROM notification where noti_id = ?";
        String[] args = {notiId};
        Cursor mCur = db.rawQuery(sql, args);
        try {
            if (mCur.moveToFirst()) {
                LGCNotification noti = new LGCNotification();
                noti.setNotiId(mCur.getString(mCur.getColumnIndex("noti_id")));
                noti.setType(mCur.getString(mCur.getColumnIndex("type")));
                noti.setCategory(mCur.getString(mCur.getColumnIndex("category")));
                noti.setLevel(mCur.getString(mCur.getColumnIndex("level")));
                noti.setContent(mCur.getString(mCur.getColumnIndex("content")));
                noti.setSendDate(mCur.getString(mCur.getColumnIndex("send_date")));
                noti.setStatus(mCur.getInt(mCur.getColumnIndex("status")));

                return noti;
            }
        } catch (Exception e) {
            Log.i(TAG, "Cannot parse notification object.");
        } finally {
            mCur.close();
            db.close();
        }
        return null;
    }

    public List<String> getNotificationDate() {
        List<String> dateList = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String sql = "SELECT DISTINCT strftime('%Y-%m-%d', send_date) as send_date FROM notification WHERE status <> 2 ORDER BY send_date DESC";

        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                do {
                    dateList.add(mCur.getString(mCur.getColumnIndex("send_date")));
                } while (mCur.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Cannot parse notification object.");
        } finally {
            mCur.close();
            db.close();
        }
        return dateList;
    }

    public int getNotiCount() {
        String countQuery = "SELECT  * FROM notification WHERE status = ?";
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] args = {"0"};
        Cursor cursor = db.rawQuery(countQuery, args);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    public void setNotiRead(String notiId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        try {
            db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put("status", 1);
            String[] args = {notiId};
            db.update("notification", values, "noti_id = ?", args);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Cannot parse notification object.");
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void insertCountry(List<LGCLocation> locations) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {

            db.beginTransaction();

            db.delete("country", null, null);

            for (LGCLocation location : locations) {
                ContentValues values = new ContentValues();
                values.put("country_id", location.getCountryId());
                values.put("city_name", location.getCityName());
                values.put("country_name", location.getCountryName());
                values.put("time_zone", location.getTimeZone());
                db.insert("country", null, values);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Cannot parse country object.");
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<LGCLocation> getLocations() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<LGCLocation> locationList = new ArrayList<>();
        String sql = "select * from country WHERE substr(time_zone, 5, 1) = '-' order by time_zone DESC";
        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                do {
                    LGCLocation location = new LGCLocation();
                    location.setCountryId(mCur.getInt(mCur.getColumnIndex("country_id")));
                    location.setCityName(mCur.getString(mCur.getColumnIndex("city_name")));
                    location.setCountryName(mCur.getString(mCur.getColumnIndex("country_name")));
                    location.setTimeZone(mCur.getString(mCur.getColumnIndex("time_zone")));

                    locationList.add(location);
                } while (mCur.moveToNext());
            }

            sql = "select * from country WHERE substr(time_zone, 5, 1) = '+' order by time_zone";
            mCur = db.rawQuery(sql, null);
            if (mCur.moveToFirst()) {
                do {
                    LGCLocation location = new LGCLocation();
                    location.setCountryId(mCur.getInt(mCur.getColumnIndex("country_id")));
                    location.setCityName(mCur.getString(mCur.getColumnIndex("city_name")));
                    location.setCountryName(mCur.getString(mCur.getColumnIndex("country_name")));
                    location.setTimeZone(mCur.getString(mCur.getColumnIndex("time_zone")));

                    locationList.add(location);
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }
        return locationList;
    }

    public List<LGCLocation> searchLocationByTitle(String title) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<LGCLocation> locations = new ArrayList<>();

        String sql = "SELECT * from country WHERE country_name LIKE '" + title + "%' OR city_name LIKE '" + title + "%'";

        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                do {
                    LGCLocation location = new LGCLocation();

                    location.setCountryId(mCur.getInt(mCur.getColumnIndex("country_id")));
                    location.setCityName(mCur.getString(mCur.getColumnIndex("city_name")));
                    location.setCountryName(mCur.getString(mCur.getColumnIndex("country_name")));
                    location.setTimeZone(mCur.getString(mCur.getColumnIndex("time_zone")));

                    locations.add(location);
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return locations;
    }

    public String getLocationId(String cityName, String countryName) {
        String locationId = "";
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] args = {cityName, countryName};
        String sql = "SELECT country_id FROM country where city_name = ? AND country_name = ?";
        Cursor mCur = db.rawQuery(sql, args);
        try {
            if (mCur.moveToFirst()) {
                do {
                    locationId = mCur.getString(mCur.getColumnIndex("country_id"));
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }
        return locationId;
    }

    public LGCLocation getLocationById(String locationId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] selectionArgs = {
                locationId
        };

        String sql = "SELECT * FROM country WHERE country_id = ?";
        Cursor mCur = db.rawQuery(sql, selectionArgs);
        try {
            if (mCur.moveToFirst()) {
                LGCLocation location = new LGCLocation();

                location.setCountryId(mCur.getInt(mCur.getColumnIndex("country_id")));
                location.setCityName(mCur.getString(mCur.getColumnIndex("city_name")));
                location.setCountryName(mCur.getString(mCur.getColumnIndex("country_name")));
                location.setTimeZone(mCur.getString(mCur.getColumnIndex("time_zone")));

                return location;
            }
        } finally {
            mCur.close();
            db.close();
        }

        return null;
    }

    public List<CountryCode> getCountryCodes() {
        List<CountryCode> countryCodes = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String sql = "SELECT * FROM country_code WHERE phone_code > 0 ORDER BY nice_name";
        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                do {
                    CountryCode code = new CountryCode();

                    code.setCodeId(mCur.getInt(mCur.getColumnIndex("code_id")));
                    code.setCountry(mCur.getString(mCur.getColumnIndex("nice_name")));
                    code.setCode(mCur.getInt(mCur.getColumnIndex("phone_code")));

                    countryCodes.add(code);
                } while (mCur.moveToNext());

                return countryCodes;
            }
        } finally {
            mCur.close();
            db.close();
        }

        return countryCodes;
    }

    public int getCountryCodeByID(String countryId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String sql = "SELECT * FROM country_code WHERE phone_code > 0 AND iso = '" + countryId + "';";
        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                CountryCode code = new CountryCode();

                code.setCodeId(mCur.getInt(mCur.getColumnIndex("code_id")));
                code.setCountry(mCur.getString(mCur.getColumnIndex("nice_name")));
                code.setCode(mCur.getInt(mCur.getColumnIndex("phone_code")));

                return code.getCode();
            }
        } finally {
            mCur.close();
            db.close();
        }

        return 0;
    }

    public int getCountryCodeIDByID(String countryId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String sql = "SELECT * FROM country_code WHERE phone_code > 0 AND iso = '" + countryId + "';";
        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                CountryCode code = new CountryCode();

                code.setCodeId(mCur.getInt(mCur.getColumnIndex("code_id")));
                code.setCountry(mCur.getString(mCur.getColumnIndex("nice_name")));
                code.setCode(mCur.getInt(mCur.getColumnIndex("phone_code")));

                return code.getCodeId();
            }
        } finally {
            mCur.close();
            db.close();
        }

        return 0;
    }

    /**
     * Insert promo code history of
     *
     * @param promoCodeEnters
     */
    public void insertPromoCodeEnter(List<PromoCodeEnter> promoCodeEnters) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {

            db.beginTransaction();

            db.delete("promo_code_enter", null, null);

            for (PromoCodeEnter promoCodeEnter : promoCodeEnters) {
                ContentValues values = new ContentValues();
                values.put("code", promoCodeEnter.getCode());
                values.put("type", promoCodeEnter.getType());
                values.put("date", promoCodeEnter.getDate());
                values.put("reward", promoCodeEnter.getReward());

                db.insert("promo_code_enter", null, values);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Cannot parse promo code enter object.");
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<PromoCodeEnter> getPromoCodeEnters() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<PromoCodeEnter> promoCodeEnters = new ArrayList<>();

        db.beginTransaction();

        String sql = "SELECT * FROM promo_code_enter";
        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                do {
                    PromoCodeEnter promoCodeEnter = new PromoCodeEnter();
                    promoCodeEnter.setCode(mCur.getString(mCur.getColumnIndex("code")));
                    promoCodeEnter.setType(mCur.getString(mCur.getColumnIndex("type")));
                    promoCodeEnter.setDate(mCur.getString(mCur.getColumnIndex("date")));
                    promoCodeEnter.setReward(mCur.getDouble(mCur.getColumnIndex("reward")));
                    promoCodeEnter.setStatus(mCur.getInt(mCur.getColumnIndex("status")));

                    promoCodeEnters.add(promoCodeEnter);
                } while (mCur.moveToNext());

                return promoCodeEnters;
            }
            db.setTransactionSuccessful();
        } finally {
            mCur.close();
            db.endTransaction();
            db.close();
        }

        return promoCodeEnters;
    }

    public void insertPromoCodeReferer(List<PromoCodeRefer> promoCodeRefers) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {

            db.beginTransaction();

            db.delete("promo_code_referrer", null, null);

            for (PromoCodeRefer promoCodeRefer : promoCodeRefers) {
                ContentValues values = new ContentValues();
                values.put("name", promoCodeRefer.getName());
                values.put("date", promoCodeRefer.getDate());
                values.put("reward", promoCodeRefer.getReward());
                values.put("status", promoCodeRefer.getStatus());

                db.insert("promo_code_referrer", null, values);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Cannot parse promo code refer object.");
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<PromoCodeRefer> getPromoCodeRefers() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<PromoCodeRefer> promoCodeRefers = new ArrayList<>();

        db.beginTransaction();

        String sql = "SELECT * FROM promo_code_referrer";
        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                do {
                    PromoCodeRefer promoCodeRefer = new PromoCodeRefer();
                    promoCodeRefer.setName(mCur.getString(mCur.getColumnIndex("name")));
                    promoCodeRefer.setDate(mCur.getString(mCur.getColumnIndex("date")));
                    promoCodeRefer.setReward(mCur.getDouble(mCur.getColumnIndex("reward")));
                    promoCodeRefer.setStatus(mCur.getInt(mCur.getColumnIndex("status")));

                    promoCodeRefers.add(promoCodeRefer);
                } while (mCur.moveToNext());

                return promoCodeRefers;
            }
            db.setTransactionSuccessful();
        } finally {
            mCur.close();
            db.endTransaction();
            db.close();
        }

        return promoCodeRefers;
    }
}