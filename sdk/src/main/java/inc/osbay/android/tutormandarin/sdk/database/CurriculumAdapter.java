package inc.osbay.android.tutormandarin.sdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import inc.osbay.android.tutormandarin.sdk.model.Course;
import inc.osbay.android.tutormandarin.sdk.model.FlashCard;
import inc.osbay.android.tutormandarin.sdk.model.FlashCardDeck;
import inc.osbay.android.tutormandarin.sdk.model.Grammar;
import inc.osbay.android.tutormandarin.sdk.model.Lesson;
import inc.osbay.android.tutormandarin.sdk.model.Topic;
import inc.osbay.android.tutormandarin.sdk.model.TopicClass;
import inc.osbay.android.tutormandarin.sdk.model.TrialClass;
import inc.osbay.android.tutormandarin.sdk.model.Video;
import inc.osbay.android.tutormandarin.sdk.model.VideoVocab;
import inc.osbay.android.tutormandarin.sdk.model.Vocab;
import inc.osbay.android.tutormandarin.sdk.model.WhatsOn;
import inc.osbay.android.tutormandarin.sdk.model.WhatsOnVocab;

public class CurriculumAdapter {
    protected static final String TAG = CurriculumAdapter.class.getSimpleName();

    private DataBaseHelper mDbHelper;

    public CurriculumAdapter(Context context) {
        mDbHelper = new DataBaseHelper(context);
    }

    public void insertCourses(List<Course> courses, SQLiteDatabase db) {
        try {
            for (Course course : courses) {
                ContentValues values = new ContentValues();
                values.put("course_id", course.getCourseId());
                values.put("title", course.getTitle());
                values.put("lesson_count", course.getLessonCount());
                values.put("supplement_count", course.getSupplementCount());
                values.put("cover_photo", course.getCoverPhoto());
                values.put("course_icon", course.getCourseIcon());
                values.put("description", course.getPackageDescription());
                values.put("vocab_count", course.getVocabCount());
                values.put("grammar_count", course.getGrammarCount());
                values.put("total_time", course.getTotalTime());
                values.put("credit", course.getCredit());
                values.put("hsk", course.getHsk());

                db.insert("course", null, values);
            }
            Log.d(TAG, "Insert Courses Successful.");
        } catch (SQLException e) {
            Log.e(TAG, "Insert Courses Failed.");
        }
    }

    public void insertCourseCategory(String categoryID, String courseCategory, SQLiteDatabase db) {
        try {
            ContentValues values = new ContentValues();
            values.put("category_id", categoryID);
            values.put("name", courseCategory);

            db.insert("course_category", null, values);
            Log.d(TAG, "Insert Course Category Successful.");
        } catch (SQLException e) {
            Log.e(TAG, "Insert Course Category Failed.");
        }
    }

    public void insertCourseRelation(String categoryID, String courseID, SQLiteDatabase db) {
        try {
            ContentValues values = new ContentValues();
            values.put("category_id", categoryID);
            values.put("course_id", courseID);

            db.insert("course_category_relationship", null, values);
            Log.d(TAG, "Insert Course Relationship Successful.");
        } catch (SQLException e) {
            Log.e(TAG, "Insert Course Relationship Failed.");
        }
    }

    public void insertCourse(Course course) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("course_id", course.getCourseId());
            values.put("title", course.getTitle());
            values.put("lesson_count", course.getLessonCount());
            values.put("supplement_count", course.getSupplementCount());
            values.put("cover_photo", course.getCoverPhoto());
            values.put("course_icon", course.getCourseIcon());
            values.put("description", course.getPackageDescription());
            values.put("vocab_count", course.getVocabCount());
            values.put("grammar_count", course.getGrammarCount());
            values.put("total_time", course.getTotalTime());
            values.put("credit", course.getCredit());

            db.insertWithOnConflict("course", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            Log.e(TAG, "Insert Course Failed.", e);
        } finally {
            db.close();
        }
    }

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String sql = "SELECT * FROM course";
        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                do {
                    Course course = new Course();

                    course.setCourseId(mCur.getString(mCur.getColumnIndex("course_id")));
                    course.setTitle(mCur.getString(mCur.getColumnIndex("title")));
                    course.setPackageDescription(mCur.getString(mCur.getColumnIndex("description")));
                    course.setLessonCount(mCur.getInt(mCur.getColumnIndex("lesson_count")));
                    course.setSupplementCount(mCur.getInt(mCur.getColumnIndex("supplement_count")));
                    course.setVocabCount(mCur.getInt(mCur.getColumnIndex("vocab_count")));
                    course.setGrammarCount(mCur.getInt(mCur.getColumnIndex("grammar_count")));
                    course.setTotalTime(mCur.getDouble(mCur.getColumnIndex("total_time")));
                    course.setCredit(mCur.getInt(mCur.getColumnIndex("credit")));

                    course.setCourseIcon(mCur.getString(mCur.getColumnIndex("course_icon")));
                    course.setCoverPhoto(mCur.getString(mCur.getColumnIndex("cover_photo")));

                    courses.add(course);
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return courses;
    }

    public List<Course> getCoursesBySelectedLevel(boolean[] selectedLevel) {
        List<Course> courses = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String sql = "SELECT * FROM course a INNER JOIN course_category_relationship b " +
                "ON a.course_id=b.course_id WHERE b.category_id=? AND a.hsk=?";
        for (int i = 0; i < 6; i++) {
            if (selectedLevel[i] == true) {
                //String sql = "SELECT * FROM course";
                Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(1), String.valueOf(i+1)});
                addCoursetoList(cursor, courses);
            }
        }

        for (int i = 6; i < 10; i++) {
            if (selectedLevel[i] == true) {
                Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(2), String.valueOf(i-5)});
                addCoursetoList(cursor, courses);
            }
        }
        db.close();
        return courses;
    }

    public void addCoursetoList(Cursor mCur, List<Course> courses) {
        if (mCur.moveToFirst()) {
            do {
                Course course = new Course();

                course.setCourseId(mCur.getString(mCur.getColumnIndex("course_id")));
                course.setTitle(mCur.getString(mCur.getColumnIndex("title")));
                course.setPackageDescription(mCur.getString(mCur.getColumnIndex("description")));
                course.setLessonCount(mCur.getInt(mCur.getColumnIndex("lesson_count")));
                course.setSupplementCount(mCur.getInt(mCur.getColumnIndex("supplement_count")));
                course.setVocabCount(mCur.getInt(mCur.getColumnIndex("vocab_count")));
                course.setGrammarCount(mCur.getInt(mCur.getColumnIndex("grammar_count")));
                course.setTotalTime(mCur.getDouble(mCur.getColumnIndex("total_time")));
                course.setCredit(mCur.getInt(mCur.getColumnIndex("credit")));

                course.setCourseIcon(mCur.getString(mCur.getColumnIndex("course_icon")));
                course.setCoverPhoto(mCur.getString(mCur.getColumnIndex("cover_photo")));

                courses.add(course);
            } while (mCur.moveToNext());
        }
        mCur.close();
    }

    public Course getCourseById(String courseId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] selectionArgs = {courseId};

        String sql = "SELECT * FROM course WHERE course_id = ?";
        Cursor mCur = db.rawQuery(sql, selectionArgs);
        try {
            if (mCur.moveToFirst()) {
                Course course = new Course();

                course.setCourseId(mCur.getString(mCur.getColumnIndex("course_id")));
                course.setTitle(mCur.getString(mCur.getColumnIndex("title")));
                course.setPackageDescription(mCur.getString(mCur.getColumnIndex("description")));
                course.setLessonCount(mCur.getInt(mCur.getColumnIndex("lesson_count")));
                course.setSupplementCount(mCur.getInt(mCur.getColumnIndex("supplement_count")));
                course.setVocabCount(mCur.getInt(mCur.getColumnIndex("vocab_count")));
                course.setGrammarCount(mCur.getInt(mCur.getColumnIndex("grammar_count")));
                course.setTotalTime(mCur.getDouble(mCur.getColumnIndex("total_time")));
                course.setCredit(mCur.getInt(mCur.getColumnIndex("credit")));

                course.setCourseIcon(mCur.getString(mCur.getColumnIndex("course_icon")));
                course.setCoverPhoto(mCur.getString(mCur.getColumnIndex("cover_photo")));

                return course;
            }
        } finally {
            mCur.close();
            db.close();
        }

        return null;
    }

    public void insertLessons(List<Lesson> lessons, String courseId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.beginTransaction();

        String whereClause = "course_id=?";
        String[] whereArgs = {courseId};
        db.delete("lesson", whereClause, whereArgs);

        try {
            for (Lesson lesson : lessons) {
                ContentValues values = new ContentValues();
                values.put("lesson_id", lesson.getLessonId());
                values.put("course_id", lesson.getCourseId());
                values.put("lesson_number", lesson.getLessonNumber());
                values.put("title", lesson.getTitle());
                values.put("section_number", lesson.getSectionNumber());
                values.put("credit", lesson.getCredit());
                values.put("vocab_count", lesson.getVocabCount());
                values.put("grammar_count", lesson.getGrammarCount());
                values.put("is_supplement", lesson.isSupplement());
                values.put("cover_photo", lesson.getCoverPhoto());
                values.put("is_bought", lesson.isBought());
                values.put("is_completed", lesson.isCompleted());
                values.put("level", lesson.getLevel());
                values.put("is_free", lesson.getIsFree());
                values.put("duration", lesson.getDuration());
                values.put("available", lesson.getIsAvalilable());
                values.put("url", lesson.getUrl());

                db.insertWithOnConflict("lesson", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "Insert Failed.");
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<Lesson> getAllLessonsAndSupplements(String courseId) {
        List<Lesson> lessons = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] selectionArgs = {courseId};

        String sql = "SELECT * FROM lesson WHERE course_id = ? ORDER BY section_number, lesson_number";
        Cursor mCur = db.rawQuery(sql, selectionArgs);
        try {
            if (mCur.moveToFirst()) {
                do {
                    Lesson lesson = new Lesson();

                    lesson.setLessonId(mCur.getString(mCur.getColumnIndex("lesson_id")));
                    lesson.setLessonNumber(mCur.getInt(mCur.getColumnIndex("lesson_number")));
                    lesson.setTitle(mCur.getString(mCur.getColumnIndex("title")));
                    lesson.setSectionNumber(mCur.getInt(mCur.getColumnIndex("section_number")));
                    lesson.setCourseId(mCur.getString(mCur.getColumnIndex("course_id")));
                    lesson.setVocabCount(mCur.getInt(mCur.getColumnIndex("vocab_count")));
                    lesson.setGrammarCount(mCur.getInt(mCur.getColumnIndex("grammar_count")));
                    lesson.setCredit(mCur.getInt(mCur.getColumnIndex("credit")));
                    lesson.setIsSupplement(mCur.getInt(mCur.getColumnIndex("is_supplement")));
                    lesson.setIsBought(mCur.getInt(mCur.getColumnIndex("is_bought")));
                    lesson.setIsFree(mCur.getInt(mCur.getColumnIndex("is_free")));
                    lesson.setIsCompleted(mCur.getInt(mCur.getColumnIndex("is_completed")));
                    lesson.setLevel(mCur.getString(mCur.getColumnIndex("level")));
                    lesson.setDuration(mCur.getInt(mCur.getColumnIndex("duration")));
                    lesson.setCoverPhoto(mCur.getString(mCur.getColumnIndex("cover_photo")));
                    lesson.setIsAvailable(mCur.getInt(mCur.getColumnIndex("available")));
                    lesson.setUrl(mCur.getString(mCur.getColumnIndex("url")));

                    lessons.add(lesson);
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return lessons;
    }

    public List<Lesson> getAllLessons(String courseId) {
        List<Lesson> lessons = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] selectionArgs = {courseId};

        String sql = "SELECT * FROM lesson WHERE course_id = ? AND is_supplement = 0";
        Cursor mCur = db.rawQuery(sql, selectionArgs);
        try {
            if (mCur.moveToFirst()) {
                do {
                    Lesson lesson = new Lesson();

                    lesson.setLessonId(mCur.getString(mCur.getColumnIndex("lesson_id")));
                    lesson.setLessonNumber(mCur.getInt(mCur.getColumnIndex("lesson_number")));
                    lesson.setTitle(mCur.getString(mCur.getColumnIndex("title")));
                    lesson.setSectionNumber(mCur.getInt(mCur.getColumnIndex("section_number")));
                    lesson.setCourseId(mCur.getString(mCur.getColumnIndex("course_id")));
                    lesson.setVocabCount(mCur.getInt(mCur.getColumnIndex("vocab_count")));
                    lesson.setGrammarCount(mCur.getInt(mCur.getColumnIndex("grammar_count")));
                    lesson.setCredit(mCur.getInt(mCur.getColumnIndex("credit")));
                    lesson.setIsSupplement(mCur.getInt(mCur.getColumnIndex("is_supplement")));
                    lesson.setIsBought(mCur.getInt(mCur.getColumnIndex("is_bought")));
                    lesson.setIsFree(mCur.getInt(mCur.getColumnIndex("is_free")));
                    lesson.setIsCompleted(mCur.getInt(mCur.getColumnIndex("is_completed")));
                    lesson.setLevel(mCur.getString(mCur.getColumnIndex("level")));

                    lesson.setCoverPhoto(mCur.getString(mCur.getColumnIndex("cover_photo")));
                    lesson.setIsAvailable(mCur.getInt(mCur.getColumnIndex("available")));
                    lesson.setUrl(mCur.getString(mCur.getColumnIndex("url")));

                    lessons.add(lesson);
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return lessons;
    }

    public List<Lesson> getAllSupplements(String courseId) {
        List<Lesson> lessons = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] selectionArgs = {courseId};

        String sql = "SELECT * FROM lesson WHERE course_id = ? AND is_supplement = 1";
        Cursor mCur = db.rawQuery(sql, selectionArgs);
        try {
            if (mCur.moveToFirst()) {
                do {
                    Lesson lesson = new Lesson();

                    lesson.setLessonId(mCur.getString(mCur.getColumnIndex("lesson_id")));
                    lesson.setLessonNumber(mCur.getInt(mCur.getColumnIndex("lesson_number")));
                    lesson.setTitle(mCur.getString(mCur.getColumnIndex("title")));
                    lesson.setSectionNumber(mCur.getInt(mCur.getColumnIndex("section_number")));
                    lesson.setCourseId(mCur.getString(mCur.getColumnIndex("course_id")));
                    lesson.setVocabCount(mCur.getInt(mCur.getColumnIndex("vocab_count")));
                    lesson.setGrammarCount(mCur.getInt(mCur.getColumnIndex("grammar_count")));
                    lesson.setCredit(mCur.getInt(mCur.getColumnIndex("credit")));
                    lesson.setIsSupplement(mCur.getInt(mCur.getColumnIndex("is_supplement")));
                    lesson.setIsBought(mCur.getInt(mCur.getColumnIndex("is_bought")));
                    lesson.setIsFree(mCur.getInt(mCur.getColumnIndex("is_free")));
                    lesson.setIsCompleted(mCur.getInt(mCur.getColumnIndex("is_completed")));
                    lesson.setLevel(mCur.getString(mCur.getColumnIndex("level")));
                    lesson.setDuration(mCur.getInt(mCur.getColumnIndex("duration")));

                    lesson.setCoverPhoto(mCur.getString(mCur.getColumnIndex("cover_photo")));
                    lesson.setIsAvailable(mCur.getInt(mCur.getColumnIndex("available")));
                    lesson.setUrl(mCur.getString(mCur.getColumnIndex("url")));

                    lessons.add(lesson);
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return lessons;
    }

    public Lesson getLessonById(String lessonId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] selectionArgs = {lessonId};

        String sql = "SELECT * FROM lesson WHERE lesson_id = ?";
        Cursor mCur = db.rawQuery(sql, selectionArgs);
        try {
            if (mCur.moveToFirst()) {
                Lesson lesson = new Lesson();

                lesson.setLessonId(mCur.getString(mCur.getColumnIndex("lesson_id")));
                lesson.setLessonNumber(mCur.getInt(mCur.getColumnIndex("lesson_number")));
                lesson.setTitle(mCur.getString(mCur.getColumnIndex("title")));
                lesson.setSectionNumber(mCur.getInt(mCur.getColumnIndex("section_number")));
                lesson.setCourseId(mCur.getString(mCur.getColumnIndex("course_id")));
                lesson.setVocabCount(mCur.getInt(mCur.getColumnIndex("vocab_count")));
                lesson.setGrammarCount(mCur.getInt(mCur.getColumnIndex("grammar_count")));
                lesson.setCredit(mCur.getInt(mCur.getColumnIndex("credit")));
                lesson.setIsSupplement(mCur.getInt(mCur.getColumnIndex("is_supplement")));
                lesson.setIsBought(mCur.getInt(mCur.getColumnIndex("is_bought")));
                lesson.setIsFree(mCur.getInt(mCur.getColumnIndex("is_free")));
                lesson.setIsCompleted(mCur.getInt(mCur.getColumnIndex("is_completed")));
                lesson.setLevel(mCur.getString(mCur.getColumnIndex("level")));
                lesson.setDuration(mCur.getInt(mCur.getColumnIndex("duration")));
                lesson.setCoverPhoto(mCur.getString(mCur.getColumnIndex("cover_photo")));
                lesson.setIsAvailable(mCur.getInt(mCur.getColumnIndex("available")));
                lesson.setUrl(mCur.getString(mCur.getColumnIndex("url")));

                return lesson;
            }
        } finally {
            mCur.close();
            db.close();
        }

        return null;
    }


    public void insertLesson(Lesson lesson) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("lesson_id", lesson.getLessonId());
            values.put("course_id", lesson.getCourseId());
            values.put("lesson_number", lesson.getLessonNumber());
            values.put("title", lesson.getTitle());
            values.put("section_number", lesson.getSectionNumber());
            values.put("vocab_count", lesson.getVocabCount());
            values.put("grammar_count", lesson.getGrammarCount());
            values.put("credit", lesson.getCredit());
            values.put("is_supplement", lesson.isSupplement());
            values.put("cover_photo", lesson.getCoverPhoto());
            values.put("is_bought", lesson.isBought());
            values.put("is_completed", lesson.isCompleted());
            values.put("level", lesson.getLevel());
            values.put("is_free", lesson.getIsFree());
            values.put("duration", lesson.getDuration());
            values.put("available", lesson.getIsAvalilable());
            values.put("url", lesson.getUrl());

            db.insertWithOnConflict("lesson", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            Log.e(TAG, "Insert Failed.");
        } finally {
            db.close();
        }
    }

    public void insertWhatsOns(List<WhatsOn> whatsOnList) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();

        db.delete("whatson", null, null);

        try {
            for (WhatsOn whatsOn : whatsOnList) {
                ContentValues values = new ContentValues();
                values.put("whatson_id", whatsOn.getWhatsOnId());
                values.put("whatson_title", whatsOn.getTitle());
                values.put("whatson_title_image", whatsOn.getCoverPhoto());
                values.put("whatson_article", whatsOn.getArticle());
                values.put("create_date", whatsOn.getmWhatsOnPostedDate());
                values.put("whatson_topic_name", whatsOn.getTopicName());
                values.put("viewed_count", whatsOn.getmWhatsOnViewCount());
                values.put("favourited_count", whatsOn.getmWhatsOnFavouriteCount());
                values.put("grammar", whatsOn.getmWhatsOnGrammar());

                db.insert("whatson", null, values);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "Insert Failed.");
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void insertWhatsOn(WhatsOn whatsOn) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put("whatson_id", whatsOn.getWhatsOnId());
            values.put("whatson_title", whatsOn.getTitle());
            values.put("whatson_title_image", whatsOn.getCoverPhoto());
            values.put("whatson_article", whatsOn.getArticle());
            values.put("create_date", whatsOn.getmWhatsOnPostedDate());
            values.put("whatson_topic_name", whatsOn.getTopicName());
            values.put("viewed_count", whatsOn.getmWhatsOnViewCount());
            values.put("favourited_count", whatsOn.getmWhatsOnFavouriteCount());
            values.put("grammar", whatsOn.getmWhatsOnGrammar());

            db.insert("whatson", null, values);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "Insert Failed.");
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<WhatsOn> getAllWhatsOn() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<WhatsOn> whatsOnList = new ArrayList<>();

        String sql = "SELECT * FROM whatson ORDER BY create_date DESC";
        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                do {
                    WhatsOn whatsOn = new WhatsOn();

                    whatsOn.setmWhatsOnId(mCur.getString(mCur.getColumnIndex("whatson_id")));
                    whatsOn.setmWhatsOnTitle(mCur.getString(mCur.getColumnIndex("whatson_title")));
                    whatsOn.setmWhatsOnTitleImage(mCur.getString(mCur.getColumnIndex("whatson_title_image")));
                    whatsOn.setmWhatsOnArticle(mCur.getString(mCur.getColumnIndex("whatson_article")));
                    whatsOn.setmWhatsOnPostedDate(mCur.getString(mCur.getColumnIndex("create_date")));
                    whatsOn.setmWhatsOnViewCount(mCur.getInt(mCur.getColumnIndex("viewed_count")));
                    whatsOn.setmWhatsOnFavouriteCount(mCur.getInt(mCur.getColumnIndex("favourited_count")));
                    whatsOn.setmWhatsOnArticle(mCur.getString(mCur.getColumnIndex("whatson_article")));
                    whatsOn.setmWhatsOnTopicName(mCur.getString(mCur.getColumnIndex("whatson_topic_name")));
                    whatsOn.setmWhatsOnGrammar(mCur.getString(mCur.getColumnIndex("grammar")));

                    whatsOnList.add(whatsOn);
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return whatsOnList;
    }

    public List<String> getWhatsOnTopics() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<String> whatsOnTopicList = new ArrayList<>();
        whatsOnTopicList.add(0, "All");

        String sql = "SELECT DISTINCT whatson_topic_name FROM whatson";
        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                do {
                    whatsOnTopicList.add(mCur.getString(mCur.getColumnIndex("whatson_topic_name")));
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return whatsOnTopicList;
    }

    public WhatsOn getWhatsOnById(String whatsOnId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] selectArgs = {whatsOnId};
        String sql = "SELECT * FROM whatson WHERE whatson_id = ?";
        Cursor mCur = db.rawQuery(sql, selectArgs);
        try {
            if (mCur.moveToFirst()) {
                WhatsOn whatsOn = new WhatsOn();
                whatsOn.setmWhatsOnId(mCur.getString(mCur.getColumnIndex("whatson_id")));
                whatsOn.setmWhatsOnTitle(mCur.getString(mCur.getColumnIndex("whatson_title")));
                whatsOn.setmWhatsOnTitleImage(mCur.getString(mCur.getColumnIndex("whatson_title_image")));
                whatsOn.setmWhatsOnArticle(mCur.getString(mCur.getColumnIndex("whatson_article")));
                whatsOn.setmWhatsOnPostedDate(whatsOn.getmWhatsOnPostedDate());
                whatsOn.setmWhatsOnViewCount(mCur.getInt(mCur.getColumnIndex("viewed_count")));
                whatsOn.setmWhatsOnFavouriteCount(mCur.getInt(mCur.getColumnIndex("favourited_count")));
                whatsOn.setmWhatsOnArticle(mCur.getString(mCur.getColumnIndex("whatson_article")));
                whatsOn.setmWhatsOnTopicName(mCur.getString(mCur.getColumnIndex("whatson_topic_name")));
                whatsOn.setmWhatsOnGrammar(mCur.getString(mCur.getColumnIndex("grammar")));

                return whatsOn;
            }
        } finally {
            mCur.close();
            db.close();
        }

        return null;
    }

    public List<WhatsOn> getWhatsOnByTopic(String whatsOnTopic) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<WhatsOn> whatsOnList = new ArrayList<>();

        String[] selectArgs = {whatsOnTopic};
        String sql = "SELECT * FROM whatson WHERE whatson_topic_name = ? ORDER BY create_date DESC";
        Cursor mCur = db.rawQuery(sql, selectArgs);
        try {
            if (mCur.moveToFirst()) {
                do {
                    WhatsOn whatsOn = new WhatsOn();
                    whatsOn.setmWhatsOnId(mCur.getString(mCur.getColumnIndex("whatson_id")));
                    whatsOn.setmWhatsOnTitle(mCur.getString(mCur.getColumnIndex("whatson_title")));
                    whatsOn.setmWhatsOnTitleImage(mCur.getString(mCur.getColumnIndex("whatson_title_image")));
                    whatsOn.setmWhatsOnArticle(mCur.getString(mCur.getColumnIndex("whatson_article")));
                    whatsOn.setmWhatsOnPostedDate(whatsOn.getmWhatsOnPostedDate());
                    whatsOn.setmWhatsOnViewCount(mCur.getInt(mCur.getColumnIndex("viewed_count")));
                    whatsOn.setmWhatsOnFavouriteCount(mCur.getInt(mCur.getColumnIndex("favourited_count")));
                    whatsOn.setmWhatsOnArticle(mCur.getString(mCur.getColumnIndex("whatson_article")));
                    whatsOn.setmWhatsOnTopicName(mCur.getString(mCur.getColumnIndex("whatson_topic_name")));
                    whatsOn.setmWhatsOnGrammar(mCur.getString(mCur.getColumnIndex("grammar")));
                    whatsOnList.add(whatsOn);
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return whatsOnList;
    }

    public List<WhatsOn> searchWhatsOnByTitle(String title) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<WhatsOn> whatsOnList = new ArrayList<>();

        String sql = "SELECT * FROM whatson WHERE whatson_title LIKE '%" + title + "%' ORDER BY create_date";
        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                do {
                    WhatsOn whatsOn = new WhatsOn();
                    whatsOn.setmWhatsOnId(mCur.getString(mCur.getColumnIndex("whatson_id")));
                    whatsOn.setmWhatsOnTitle(mCur.getString(mCur.getColumnIndex("whatson_title")));
                    whatsOn.setmWhatsOnTitleImage(mCur.getString(mCur.getColumnIndex("whatson_title_image")));
                    whatsOn.setmWhatsOnArticle(mCur.getString(mCur.getColumnIndex("whatson_article")));
                    whatsOn.setmWhatsOnPostedDate(whatsOn.getmWhatsOnPostedDate());
                    whatsOn.setmWhatsOnViewCount(mCur.getInt(mCur.getColumnIndex("viewed_count")));
                    whatsOn.setmWhatsOnFavouriteCount(mCur.getInt(mCur.getColumnIndex("favourited_count")));
                    whatsOn.setmWhatsOnArticle(mCur.getString(mCur.getColumnIndex("whatson_article")));
                    whatsOn.setmWhatsOnTopicName(mCur.getString(mCur.getColumnIndex("whatson_topic_name")));
                    whatsOn.setmWhatsOnGrammar(mCur.getString(mCur.getColumnIndex("grammar")));

                    whatsOnList.add(whatsOn);
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return whatsOnList;
    }

    public void insertVideos(List<Video> videoList) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();

        db.delete("video", null, null);

        try {
            for (Video video : videoList) {
                ContentValues values = new ContentValues();
                values.put("video_id", video.getVideoId());
                values.put("video_title", video.getTitle());
                values.put("video_topic_name", video.getTopicName());
                values.put("length", video.getLength());
                values.put("youtube_id", video.getYoutubeId());
                values.put("author", video.getAuthor());
                values.put("description", video.getDescription());

                db.insert("video", null, values);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "Insert Failed.");
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<Video> getVideoList() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<Video> videoList = new ArrayList<>();

        String sql = "SELECT * FROM video";
        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                do {
                    Video video = new Video();

                    video.setVideoId(mCur.getString(mCur.getColumnIndex("video_id")));
                    video.setTitle(mCur.getString(mCur.getColumnIndex("video_title")));
                    video.setVideoTopicName(mCur.getString(mCur.getColumnIndex("video_topic_name")));
                    video.setLength(mCur.getString(mCur.getColumnIndex("length")));
                    video.setViewCount(mCur.getInt(mCur.getColumnIndex("view_count")));
                    video.setYoutubeId(mCur.getString(mCur.getColumnIndex("youtube_id")));
                    video.setAuthor(mCur.getString(mCur.getColumnIndex("author")));
                    video.setDescription(mCur.getString(mCur.getColumnIndex("description")));

                    videoList.add(video);
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return videoList;
    }

    public List<String> getVideoTopics() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<String> videoTopicList = new ArrayList<>();
        videoTopicList.add(0, "All");

        String sql = "SELECT DISTINCT video_topic_name FROM video";
        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                do {
                    videoTopicList.add(mCur.getString(mCur.getColumnIndex("video_topic_name")));
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return videoTopicList;
    }

    public Video getVideoById(String videosId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] selectArgs = {videosId};
        String sql = "SELECT * FROM video WHERE video_id = ?";
        Cursor mCur = db.rawQuery(sql, selectArgs);
        try {
            if (mCur.moveToFirst()) {
                Video video = new Video();

                video.setVideoId(mCur.getString(mCur.getColumnIndex("video_id")));
                video.setTitle(mCur.getString(mCur.getColumnIndex("video_title")));
                video.setVideoTopicName(mCur.getString(mCur.getColumnIndex("video_topic_name")));
                video.setLength(mCur.getString(mCur.getColumnIndex("length")));
                video.setViewCount(mCur.getInt(mCur.getColumnIndex("view_count")));
                video.setYoutubeId(mCur.getString(mCur.getColumnIndex("youtube_id")));
                video.setAuthor(mCur.getString(mCur.getColumnIndex("author")));
                video.setDescription(mCur.getString(mCur.getColumnIndex("description")));

                return video;
            }
        } finally {
            mCur.close();
            db.close();
        }

        return null;
    }

    public void updateYoutubeVideoInformation(String youtubeId, int viewCount) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("view_count", viewCount);

            String whereClause = "youtube_id = ?";

            String[] whereArgs = {String.valueOf(youtubeId)};

            db.update("video", values, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    public List<Video> getVideosByTopic(String videosTopic) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<Video> videoList = new ArrayList<>();

        String[] selectArgs = {videosTopic};
        String sql = "SELECT * FROM video WHERE video_topic_name = ?";
        Cursor mCur = db.rawQuery(sql, selectArgs);
        try {
            if (mCur.moveToFirst()) {
                do {
                    Video video = new Video();

                    video.setVideoId(mCur.getString(mCur.getColumnIndex("video_id")));
                    video.setTitle(mCur.getString(mCur.getColumnIndex("video_title")));
                    video.setVideoTopicName(mCur.getString(mCur.getColumnIndex("video_topic_name")));
                    video.setLength(mCur.getString(mCur.getColumnIndex("length")));
                    video.setViewCount(mCur.getInt(mCur.getColumnIndex("view_count")));
                    video.setYoutubeId(mCur.getString(mCur.getColumnIndex("youtube_id")));
                    video.setAuthor(mCur.getString(mCur.getColumnIndex("author")));

                    videoList.add(video);
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return videoList;
    }

    public List<Video> searchVideosByTitle(String title) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<Video> videoList = new ArrayList<>();

        String sql = "SELECT * FROM video WHERE video_title LIKE '%" + title + "%'";
        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                do {
                    Video video = new Video();

                    video.setVideoId(mCur.getString(mCur.getColumnIndex("video_id")));
                    video.setTitle(mCur.getString(mCur.getColumnIndex("video_title")));
                    video.setVideoTopicName(mCur.getString(mCur.getColumnIndex("video_topic_name")));
                    video.setLength(mCur.getString(mCur.getColumnIndex("length")));
                    video.setViewCount(mCur.getInt(mCur.getColumnIndex("view_count")));
                    video.setYoutubeId(mCur.getString(mCur.getColumnIndex("youtube_id")));
                    video.setAuthor(mCur.getString(mCur.getColumnIndex("author")));

                    videoList.add(video);
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return videoList;
    }

    public void insertFCDecks(List<FlashCardDeck> flashCardDecks) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.beginTransaction();

        String whereClause = "type <> 3";


        db.delete("flashcard_deck", whereClause, null);

        try {
            for (FlashCardDeck deck : flashCardDecks) {
                ContentValues values = new ContentValues();
                values.put("course_id", deck.getCourseId());
                values.put("title", deck.getCardTitle());
                values.put("count", deck.getCardCount());
                values.put("type", deck.getType());
                values.put("level", deck.getCardLevel());

                db.insert("flashcard_deck", null, values);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "Insert Failed.");
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public boolean insertCustomFCDeck(String deckName) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        try {
            String selection = "title = ? AND type = 3";
            String[] selectionArgs = {deckName};
            Cursor cursor = db.query("flashcard_deck", null, selection, selectionArgs, null, null, null);

            if (cursor.getCount() == 0) {

                ContentValues values = new ContentValues();
                values.put("title", deckName);
                values.put("type", 3);

                db.insert("flashcard_deck", null, values);

                return true;
            }
        } catch (SQLException e) {
            Log.e(TAG, "Insert Failed.");
        } finally {
            db.close();
        }
        return false;
    }

    public void deleteCustomFCDeck(int deckId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            String[] args = {String.valueOf(deckId)};
            db.delete("custom_flashcard", "fcdeck_id = ?", args);

            db.delete("flashcard_deck", "deck_id = ?", args);

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "Delete Failed.", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void updateCustomFCDeck(int deckId, String flashcardTitle) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("title", flashcardTitle);

            String whereClause = "deck_id = ?";

            String[] whereArgs = {String.valueOf(deckId)};

            db.update("flashcard_deck", values, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    public List<FlashCardDeck> getCommonFCDecks() {
        List<FlashCardDeck> flashCardDecks = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String sql = "SELECT * FROM flashcard_deck WHERE type <> 3";
        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                do {
                    FlashCardDeck flashCardDeck = new FlashCardDeck();

                    flashCardDeck.setCourseId(mCur.getString(mCur.getColumnIndex("course_id")));
                    flashCardDeck.setCardCount(mCur.getInt(mCur.getColumnIndex("count")));
                    flashCardDeck.setCardTitle(mCur.getString(mCur.getColumnIndex("title")));
                    flashCardDeck.setCardLevel(mCur.getString(mCur.getColumnIndex("level")));
                    flashCardDeck.setType(mCur.getInt(mCur.getColumnIndex("type")));

                    flashCardDecks.add(flashCardDeck);
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return flashCardDecks;
    }

    public List<FlashCardDeck> getCustomFCDecks() {
        List<FlashCardDeck> flashCardDecks = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String sql = "SELECT flashcard_deck.*, COUNT(custom_flashcard.flashcard_id) AS 'count' FROM flashcard_deck LEFT JOIN custom_flashcard ON flashcard_deck.deck_id = custom_flashcard.fcdeck_id WHERE flashcard_deck.type = 3 GROUP BY flashcard_deck.title";
        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                do {
                    FlashCardDeck flashCardDeck = new FlashCardDeck();

                    flashCardDeck.setDeckId(mCur.getInt(mCur.getColumnIndex("deck_id")));
                    flashCardDeck.setCardCount(mCur.getInt(mCur.getColumnIndex("count")));
                    flashCardDeck.setCardTitle(mCur.getString(mCur.getColumnIndex("title")));
                    flashCardDeck.setType(mCur.getInt(mCur.getColumnIndex("type")));

                    flashCardDecks.add(flashCardDeck);
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return flashCardDecks;
    }

    public void insertFlashCards(List<FlashCard> flashCards, String courseId, int type) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.beginTransaction();

        String whereClause = "course_id = ? AND type = ?";

        String whereArgs[] = {courseId, String.valueOf(type)};

        db.delete("flashcard", whereClause, whereArgs);

        try {
            for (FlashCard card : flashCards) {
                ContentValues values = new ContentValues();

                values.put("flashcard_id", card.getFlashCardId());
                values.put("course_id", card.getCourseId());
                values.put("chinese", card.getChinese());
                values.put("example", card.getExample());
                values.put("part_of_speech", card.getPartOfSpeech());
                values.put("type", card.getType());

                if (type == 1) {
                    Vocab vocab = (Vocab) card;

                    values.put("photo", vocab.getPhoto());
                    values.put("voice", vocab.getVoice());
                    values.put("pinyin", vocab.getPinyin());
                    values.put("definition", vocab.getDefinition());
                } else {
                    Grammar grammar = (Grammar) card;

                    values.put("english", grammar.getEnglish());
                    values.put("description", grammar.getDescription());
                }

                db.insertWithOnConflict("flashcard", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "Insert Failed.");
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<FlashCard> getCommonFlashCardsByDeck(String courseId, int type) {
        List<FlashCard> flashCards = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String sql = "SELECT * FROM flashcard WHERE course_id = ? AND type = ? ORDER BY pinyin";
        String[] selectionArgs = {courseId, String.valueOf(type)};

        Cursor mCur = db.rawQuery(sql, selectionArgs);
        try {
            if (mCur.moveToFirst()) {
                do {
                    if (type == 1) {
                        Vocab vocab = new Vocab();

                        vocab.setFlashCardId(mCur.getString(mCur.getColumnIndex("flashcard_id")));
                        vocab.setCourseId(mCur.getString(mCur.getColumnIndex("course_id")));
                        vocab.setChinese(mCur.getString(mCur.getColumnIndex("chinese")));
                        vocab.setExample(mCur.getString(mCur.getColumnIndex("example")));
                        vocab.setPartOfSpeech(mCur.getString(mCur.getColumnIndex("part_of_speech")));
                        vocab.setType(mCur.getInt(mCur.getColumnIndex("type")));

                        vocab.setPhoto(mCur.getString(mCur.getColumnIndex("photo")));
                        vocab.setVoice(mCur.getString(mCur.getColumnIndex("voice")));
                        vocab.setPinyin(mCur.getString(mCur.getColumnIndex("pinyin")));
                        vocab.setDefinition(mCur.getString(mCur.getColumnIndex("definition")));

                        flashCards.add(vocab);
                    } else {
                        Grammar grammar = new Grammar();

                        grammar.setFlashCardId(mCur.getString(mCur.getColumnIndex("flashcard_id")));
                        grammar.setCourseId(mCur.getString(mCur.getColumnIndex("course_id")));
                        grammar.setChinese(mCur.getString(mCur.getColumnIndex("chinese")));
                        grammar.setExample(mCur.getString(mCur.getColumnIndex("example")));
                        grammar.setPartOfSpeech(mCur.getString(mCur.getColumnIndex("part_of_speech")));
                        grammar.setType(mCur.getInt(mCur.getColumnIndex("type")));

                        grammar.setEnglish(mCur.getString(mCur.getColumnIndex("english")));
                        grammar.setDescription(mCur.getString(mCur.getColumnIndex("description")));

                        flashCards.add(grammar);
                    }
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return flashCards;
    }

    public List<FlashCard> getCustomFlashCardsByDeck(String deckId) {
        List<FlashCard> flashCards = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String sql = "SELECT * FROM flashcard WHERE flashcard_id IN (SELECT flashcard_id FROM custom_flashcard WHERE fcdeck_id = ?)";
        String[] selectionArgs = {deckId};

        Cursor mCur = db.rawQuery(sql, selectionArgs);
        try {
            if (mCur.moveToFirst()) {
                do {
                    int type = mCur.getInt(mCur.getColumnIndex("type"));
                    if (type == 1) {
                        Vocab vocab = new Vocab();

                        vocab.setFlashCardId(mCur.getString(mCur.getColumnIndex("flashcard_id")));
                        vocab.setCourseId(mCur.getString(mCur.getColumnIndex("course_id")));
                        vocab.setChinese(mCur.getString(mCur.getColumnIndex("chinese")));
                        vocab.setExample(mCur.getString(mCur.getColumnIndex("example")));
                        vocab.setPartOfSpeech(mCur.getString(mCur.getColumnIndex("part_of_speech")));
                        vocab.setType(mCur.getInt(mCur.getColumnIndex("type")));

                        vocab.setPhoto(mCur.getString(mCur.getColumnIndex("photo")));
                        vocab.setVoice(mCur.getString(mCur.getColumnIndex("voice")));
                        vocab.setPinyin(mCur.getString(mCur.getColumnIndex("pinyin")));
                        vocab.setDefinition(mCur.getString(mCur.getColumnIndex("definition")));

                        flashCards.add(vocab);
                    } else {
                        Grammar grammar = new Grammar();

                        grammar.setFlashCardId(mCur.getString(mCur.getColumnIndex("flashcard_id")));
                        grammar.setCourseId(mCur.getString(mCur.getColumnIndex("course_id")));
                        grammar.setChinese(mCur.getString(mCur.getColumnIndex("chinese")));
                        grammar.setExample(mCur.getString(mCur.getColumnIndex("example")));
                        grammar.setPartOfSpeech(mCur.getString(mCur.getColumnIndex("part_of_speech")));
                        grammar.setType(mCur.getInt(mCur.getColumnIndex("type")));

                        grammar.setEnglish(mCur.getString(mCur.getColumnIndex("english")));
                        grammar.setDescription(mCur.getString(mCur.getColumnIndex("description")));

                        flashCards.add(grammar);
                    }
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return flashCards;
    }

    public void addToCustomFCDeck(String flashCardId, List<String> mCustomDeckIds) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        try {
            for (String deckId : mCustomDeckIds) {
                // Check card is included in deck or not
                String selection = "flashcard_id = ? AND fcdeck_id = ?";
                String[] selectionArgs = {flashCardId, deckId};
                Cursor cursor = db.query("custom_flashcard", null, selection, selectionArgs, null, null, null);

                if (cursor.getCount() == 0) {
                    ContentValues values = new ContentValues();
                    values.put("flashcard_id", flashCardId);
                    values.put("fcdeck_id", deckId);

                    db.insert("custom_flashcard", null, values);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public List<Integer> getBookingSectionNumber(String courseId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<Integer> sections = new ArrayList<>();
        String[] args = {courseId};
        String sql = "SELECT section_number FROM lesson WHERE course_id = ? GROUP BY section_number";

        Cursor cur = db.rawQuery(sql, args);
        try {
            if (cur.moveToFirst()) {
                do {
                    sections.add(cur.getInt(cur.getColumnIndex("section_number")));
                } while (cur.moveToNext());
            }
        } finally {
            cur.close();
            db.close();
        }
        return sections;
    }

    public List<Lesson> getLessonBySectionNumber(String courseId, int sectionNumber) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<Lesson> lessons = new ArrayList<>();
        String sql = "SELECT * FROM lesson WHERE course_id = ? and section_number = ?";

        String[] args = {courseId, String.valueOf(sectionNumber)};
        Cursor mCur = db.rawQuery(sql, args);
        try {
            if (mCur.moveToFirst()) {
                do {
                    Lesson lesson = new Lesson();
                    lesson.setLessonId(mCur.getString(mCur.getColumnIndex("lesson_id")));
                    lesson.setLessonNumber(mCur.getInt(mCur.getColumnIndex("lesson_number")));
                    lesson.setTitle(mCur.getString(mCur.getColumnIndex("title")));
                    lesson.setSectionNumber(mCur.getInt(mCur.getColumnIndex("section_number")));
                    lesson.setCourseId(mCur.getString(mCur.getColumnIndex("course_id")));
                    lesson.setVocabCount(mCur.getInt(mCur.getColumnIndex("vocab_count")));
                    lesson.setGrammarCount(mCur.getInt(mCur.getColumnIndex("grammar_count")));
                    lesson.setCredit(mCur.getInt(mCur.getColumnIndex("credit")));
                    lesson.setIsSupplement(mCur.getInt(mCur.getColumnIndex("is_supplement")));
                    lesson.setIsBought(mCur.getInt(mCur.getColumnIndex("is_bought")));
                    lesson.setIsFree(mCur.getInt(mCur.getColumnIndex("is_free")));
                    lesson.setIsCompleted(mCur.getInt(mCur.getColumnIndex("is_completed")));
                    lesson.setLevel(mCur.getString(mCur.getColumnIndex("level")
                    ));
                    lesson.setCoverPhoto(mCur.getString(mCur.getColumnIndex("cover_photo")));
                    lesson.setDuration(mCur.getInt(mCur.getColumnIndex("duration")));

                    lessons.add(lesson);
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }
        return lessons;
    }

    public boolean insertVideoVocab(String videoId, List<VideoVocab> videoVocabs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        try {
            db.beginTransaction();

            String whereClause = "video_id = ?";
            String whereArgs[] = {videoId};
            db.delete("video_vocab", whereClause, whereArgs);

            for (VideoVocab videoVocab : videoVocabs) {
                ContentValues values = new ContentValues();
                values.put("video_id", videoId);
                values.put("video_vocab_id", videoVocab.getVideoVocabId());
                values.put("character", videoVocab.getChatacter());
                values.put("definition", videoVocab.getDefinition());
                values.put("part_of_speech", videoVocab.getPartOfSpeech());
                values.put("pinyin", videoVocab.getPinyin());

                values.put("photo", videoVocab.getPhoto());
                values.put("voice", videoVocab.getVoice());
                values.put("example", videoVocab.getExample());

                db.insert("video_vocab", null, values);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "Insert Failed.");
        } finally {
            db.endTransaction();
            db.close();
        }
        return false;
    }

    public List<VideoVocab> retrieveVideoVocab(String video_id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<VideoVocab> videoVocabs = new ArrayList<>();

        String[] args = {video_id};
        String sql = "SELECT * FROM video_vocab WHERE video_id = ? ORDER BY video_vocab_id";

        Cursor cur = db.rawQuery(sql, args);
        try {
            if (cur.moveToFirst()) {
                do {
                    VideoVocab videoVocab = new VideoVocab();
                    videoVocab.setVideoVocabId(cur.getString(cur.getColumnIndex("video_vocab_id")));
                    videoVocab.setVideoId(cur.getString(cur.getColumnIndex("video_id")));
                    videoVocab.setChatacter(cur.getString(cur.getColumnIndex("character")));
                    videoVocab.setDefinition(cur.getString(cur.getColumnIndex("definition")));
                    videoVocab.setPartOfSpeech(cur.getString(cur.getColumnIndex("part_of_speech")));
                    videoVocab.setPinyin(cur.getString(cur.getColumnIndex("pinyin")));

                    videoVocab.setPhoto(cur.getString(cur.getColumnIndex("photo")));
                    videoVocab.setVoice(cur.getString(cur.getColumnIndex("voice")));
                    videoVocab.setExample(cur.getString(cur.getColumnIndex("example")));

                    videoVocabs.add(videoVocab);
                } while (cur.moveToNext());
            }
        } finally {
            cur.close();
            db.close();
        }
        return videoVocabs;
    }

    public List<FlashCard> getVideoFlashCards(String video_id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<FlashCard> videoVocabs = new ArrayList<>();

        String[] args = {video_id};
        String sql = "SELECT * FROM video_vocab WHERE video_id = ? ORDER BY video_vocab_id";

        Cursor cur = db.rawQuery(sql, args);
        try {
            if (cur.moveToFirst()) {
                do {
                    Vocab vocab = new Vocab();

                    vocab.setFlashCardId(cur.getString(cur.getColumnIndex("video_vocab_id")));
                    vocab.setCourseId(cur.getString(cur.getColumnIndex("video_id")));
                    vocab.setChinese(cur.getString(cur.getColumnIndex("character")));
                    vocab.setExample(cur.getString(cur.getColumnIndex("example")));
                    vocab.setPartOfSpeech(cur.getString(cur.getColumnIndex("part_of_speech")));
                    vocab.setType(FlashCard.Type.VIDEO_VOCAB);

                    vocab.setPhoto(cur.getString(cur.getColumnIndex("photo")));
                    vocab.setVoice(cur.getString(cur.getColumnIndex("voice")));
                    vocab.setPinyin(cur.getString(cur.getColumnIndex("pinyin")));
                    vocab.setDefinition(cur.getString(cur.getColumnIndex("definition")));

                    videoVocabs.add(vocab);
                } while (cur.moveToNext());
            }
        } finally {
            cur.close();
            db.close();
        }
        return videoVocabs;
    }

    public List<FlashCard> getWhatsOnFlashCards(String whats_on_id) {
        List<FlashCard> vocabList = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] args = {whats_on_id};
        String sql = "SELECT * FROM whats_on_vocab WHERE whats_on_id = ? ORDER BY whats_on_vocab_id";

        Cursor cur = db.rawQuery(sql, args);
        try {
            if (cur.moveToFirst()) {
                do {
                    Vocab vocab = new Vocab();

                    vocab.setFlashCardId(cur.getString(cur.getColumnIndex("whats_on_vocab_id")));
                    vocab.setCourseId(cur.getString(cur.getColumnIndex("whats_on_id")));
                    vocab.setChinese(cur.getString(cur.getColumnIndex("character")));
                    vocab.setExample(cur.getString(cur.getColumnIndex("example")));
                    vocab.setPartOfSpeech(cur.getString(cur.getColumnIndex("part_of_speech")));
                    vocab.setType(FlashCard.Type.WHATS_ON_VOCAB);

                    vocab.setPhoto(cur.getString(cur.getColumnIndex("photo")));
                    vocab.setVoice(cur.getString(cur.getColumnIndex("voice")));
                    vocab.setPinyin(cur.getString(cur.getColumnIndex("pinyin")));
                    vocab.setDefinition(cur.getString(cur.getColumnIndex("definition")));
                    vocabList.add(vocab);
                } while (cur.moveToNext());
            }
        } finally {
            cur.close();
            db.close();
        }
        return vocabList;
    }

    public boolean insertWhatsOnVocab(String whatsOnId, List<WhatsOnVocab> whatsOnVocabs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        try {
            db.beginTransaction();

            String whereClause = "whats_on_id = ?";
            String[] whereArgs = {whatsOnId};

            db.delete("whats_on_vocab", whereClause, whereArgs);

            for (WhatsOnVocab whatsOnVocab : whatsOnVocabs) {
                ContentValues values = new ContentValues();

                values.put("whats_on_id", whatsOnId);
                values.put("whats_on_vocab_id", whatsOnVocab.getWhatsOnVocabId());
                values.put("character", whatsOnVocab.getChatacter());
                values.put("definition", whatsOnVocab.getDefinition());
                values.put("part_of_speech", whatsOnVocab.getPartOfSpeech());
                values.put("pinyin", whatsOnVocab.getPinyin());

                values.put("photo", whatsOnVocab.getPhoto());
                values.put("voice", whatsOnVocab.getVoice());
                values.put("example", whatsOnVocab.getExample());

                db.insert("whats_on_vocab", null, values);
            }

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "Insert Failed.", e);
        } finally {
            db.endTransaction();
            db.close();
        }
        return false;
    }

    public List<WhatsOnVocab> retrieveWhatsOnVocab(String whats_on_id) {
        List<WhatsOnVocab> vocabList = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] args = {whats_on_id};
        String sql = "SELECT * FROM whats_on_vocab WHERE whats_on_id = ? ORDER BY whats_on_vocab_id";

        Cursor cur = db.rawQuery(sql, args);
        try {
            if (cur.moveToFirst()) {
                do {
                    WhatsOnVocab whatsOnVocab = new WhatsOnVocab();

                    whatsOnVocab.setWhatsOnVocabId(cur.getString(cur.getColumnIndex("whats_on_vocab_id")));
                    whatsOnVocab.setWhatsOnId(cur.getString(cur.getColumnIndex("whats_on_id")));
                    whatsOnVocab.setChatacter(cur.getString(cur.getColumnIndex("character")));
                    whatsOnVocab.setDefinition(cur.getString(cur.getColumnIndex("definition")));
                    whatsOnVocab.setPartOfSpeech(cur.getString(cur.getColumnIndex("part_of_speech")));
                    whatsOnVocab.setPinyin(cur.getString(cur.getColumnIndex("pinyin")));

                    whatsOnVocab.setPhoto(cur.getString(cur.getColumnIndex("photo")));
                    whatsOnVocab.setVoice(cur.getString(cur.getColumnIndex("voice")));
                    whatsOnVocab.setExample(cur.getString(cur.getColumnIndex("example")));

                    vocabList.add(whatsOnVocab);
                } while (cur.moveToNext());
            }
        } finally {
            cur.close();
            db.close();
        }
        return vocabList;
    }

    public List<String> retrieveWhatsOnGrammar(String whats_on_id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<String> whatsOnGrammarList = new ArrayList<>();

        String[] args = {whats_on_id};
        String sql = "SELECT grammar FROM whatson WHERE whatson_id = ?";

        Cursor cur = db.rawQuery(sql, args);
        try {
            if (cur.moveToFirst()) {
                do {
                    whatsOnGrammarList.add(cur.getString(cur.getColumnIndex("grammar")));
                } while (cur.moveToNext());
            }
        } finally {
            cur.close();
            db.close();
        }
        return whatsOnGrammarList;
    }

    public boolean checkFavourite(String lessonTutorId, String accountId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] args = {lessonTutorId, accountId};
        String sql = "SELECT * FROM favourite WHERE lesson_tutor_id=? AND account_id=?";

        Cursor cur = db.rawQuery(sql, args);
        try {
            if (cur.moveToFirst()) {
                return true;
            }
        } finally {
            cur.close();
            db.close();
        }
        return false;
    }

    public void insertFavourite(Map<String, String> favouriteLesson) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        try {
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put("lesson_tutor_id", favouriteLesson.get("lesson_tutor_id"));
            values.put("account_id", favouriteLesson.get("fav_lesson_user"));
            values.put("type", Integer.parseInt(favouriteLesson.get("type")));
            db.insertWithOnConflict("favourite", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "Insert Failed.");
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void insertFavourites(List<Map<String, String>> favourites) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.beginTransaction();
        db.delete("favourite", null, null);

        try {
            for (Map<String, String> favourite : favourites) {
                ContentValues values = new ContentValues();
                values.put("lesson_tutor_id", favourite.get("lesson_tutor_id"));
                values.put("account_id", favourite.get("fav_lesson_user"));
                values.put("type", Integer.parseInt(favourite.get("type")));

                db.insertWithOnConflict("favourite", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "Insert Failed.", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<String> getFavouriteLessonId(String accountId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<String> lessonIds = new ArrayList<>();

        String[] args = {accountId};
        String sql = "SELECT * FROM favourite where account_id = ? and type = 2";
        Cursor cur = db.rawQuery(sql, args);
        try {
            if (cur.moveToFirst()) {
                do {
                    lessonIds.add(cur.getString(cur.getColumnIndex("lesson_tutor_id")));
                } while (cur.moveToNext());
            }
        } finally {
            cur.close();
            db.close();
        }
        return lessonIds;
    }

    public void removeFavouriteById(String accountId, String lessonId, String type) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            String[] args = {accountId, lessonId, type};

            db.delete("favourite", "account_id = ? and lesson_tutor_id =? and type = ?", args);

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "Delete Failed.", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<String> getFavouriteTutorId(String accountId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<String> lessonIds = new ArrayList<>();

        String[] args = {accountId};
        String sql = "SELECT * FROM favourite where account_id = ? and type = 5";
        Cursor cur = db.rawQuery(sql, args);
        try {
            if (cur.moveToFirst()) {
                do {
                    lessonIds.add(cur.getString(cur.getColumnIndex("lesson_tutor_id")));
                } while (cur.moveToNext());
            }
        } finally {
            cur.close();
            db.close();
        }
        return lessonIds;
    }

    public List<String> getFavouriteWhatsOnId(String accountId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<String> whatsOnIds = new ArrayList<>();

        String[] args = {accountId};
        String sql = "SELECT * FROM favourite where account_id = ? and type = 1";
        Cursor cur = db.rawQuery(sql, args);
        try {
            if (cur.moveToFirst()) {
                do {
                    whatsOnIds.add(cur.getString(cur.getColumnIndex("lesson_tutor_id")));
                } while (cur.moveToNext());
            }
        } finally {
            cur.close();
            db.close();
        }
        return whatsOnIds;
    }


    public List<String> getFavouriteClassId(String accountId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<String> classIds = new ArrayList<>();

        String[] args = {accountId};
        String sql = "SELECT * FROM favourite where account_id = ? and type = 3";
        Cursor cur = db.rawQuery(sql, args);
        try {
            if (cur.moveToFirst()) {
                do {
                    classIds.add(cur.getString(cur.getColumnIndex("lesson_tutor_id")));
                } while (cur.moveToNext());
            }
        } finally {
            cur.close();
            db.close();
        }
        return classIds;
    }

    public List<Topic> getTopicList() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<Topic> topicList = new ArrayList<>();

        String sql = "SELECT * FROM topic";
        Cursor mCur = db.rawQuery(sql, null);
        try {
            if (mCur.moveToFirst()) {
                do {
                    Topic topic = new Topic();

                    topic.setTopicId(mCur.getString(mCur.getColumnIndex("topic_id")));
                    topic.setTitle(mCur.getString(mCur.getColumnIndex("topic_title")));
                    topic.setLevel(mCur.getString(mCur.getColumnIndex("level")));
                    topic.setPhotoUrl(mCur.getString(mCur.getColumnIndex("photo_url")));
                    topic.setDescription(mCur.getString(mCur.getColumnIndex("description")));
                    topic.setmTopicIcon(mCur.getString(mCur.getColumnIndex("topic_icon")));
                    topic.setmClassCount(mCur.getString(mCur.getColumnIndex("class_count")));

                    topicList.add(topic);
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return topicList;
    }

    public Topic getTopicById(String topicId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] selectionArgs = {topicId};

        String sql = "SELECT * FROM topic WHERE topic_id = ?";
        Cursor mCur = db.rawQuery(sql, selectionArgs);
        try {
            if (mCur.moveToFirst()) {
                Topic topic = new Topic();

                topic.setTopicId(mCur.getString(mCur.getColumnIndex("topic_id")));
                topic.setTitle(mCur.getString(mCur.getColumnIndex("topic_title")));
                topic.setLevel(mCur.getString(mCur.getColumnIndex("level")));
                topic.setPhotoUrl(mCur.getString(mCur.getColumnIndex("photo_url")));
                topic.setDescription(mCur.getString(mCur.getColumnIndex("description")));
                topic.setmTopicIcon(mCur.getString(mCur.getColumnIndex("topic_icon")));
                topic.setmClassCount(mCur.getString(mCur.getColumnIndex("class_count")));

                return topic;
            }
        } finally {
            mCur.close();
            db.close();
        }

        return null;
    }


    public List<String> getTopicLevels() {
        try {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            List<String> topicLevelList = new ArrayList<>();
            topicLevelList.add(0, "All");

            String sql = "SELECT DISTINCT level FROM topic";
            Cursor mCur = db.rawQuery(sql, null);
            try {
                if (mCur.moveToFirst()) {
                    do {
                        topicLevelList.add(mCur.getString(mCur.getColumnIndex("level")));
                    } while (mCur.moveToNext());
                }
            } finally {
                mCur.close();
                db.close();
            }

            return topicLevelList;
        } catch (Exception e) {
            Log.e(TAG, "No such table error");
        }
        return null;
    }

    public List<Topic> getTopicByLevel(String level) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<Topic> topicList = new ArrayList<>();

        String[] selectArgs = {level};
        String sql;
        Cursor mCur;
        if (level.equals("All")) {
            sql = "SELECT * FROM topic";
            mCur = db.rawQuery(sql, null);
        } else {
            sql = "SELECT * FROM topic WHERE level = ?";
            mCur = db.rawQuery(sql, selectArgs);
        }

        try {
            if (mCur.moveToFirst()) {
                do {
                    Topic topic = new Topic();

                    topic.setTopicId(mCur.getString(mCur.getColumnIndex("topic_id")));
                    topic.setTitle(mCur.getString(mCur.getColumnIndex("topic_title")));
                    topic.setLevel(mCur.getString(mCur.getColumnIndex("level")));
                    topic.setPhotoUrl(mCur.getString(mCur.getColumnIndex("photo_url")));
                    topic.setDescription(mCur.getString(mCur.getColumnIndex("description")));
                    topic.setmTopicIcon(mCur.getString(mCur.getColumnIndex("topic_icon")));
                    topic.setmClassCount(mCur.getString(mCur.getColumnIndex("class_count")));

                    topicList.add(topic);
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return topicList;
    }

    public void insertTopics(List<Topic> topics) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.beginTransaction();
        db.delete("topic", null, null);

        try {
            for (Topic topic : topics) {
                ContentValues values = new ContentValues();
                values.put("topic_id", topic.getTopicId());
                values.put("topic_title", topic.getTitle());
                values.put("level", topic.getLevel());
                values.put("photo_url", topic.getPhotoUrl());
                values.put("description", topic.getDescription());
                values.put("topic_icon", topic.getmTopicIcon());
                values.put("class_count", topic.getmClassCount());

                db.insertWithOnConflict("topic", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "Insert Failed.", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void insertTopic(Topic topic) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("topic_id", topic.getTopicId());
            values.put("topic_title", topic.getTitle());
            values.put("level", topic.getLevel());
            values.put("photo_url", topic.getPhotoUrl());
            values.put("description", topic.getDescription());
            values.put("topic_icon", topic.getmTopicIcon());
            values.put("class_count", topic.getmClassCount());

            db.insertWithOnConflict("topic", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "Insert Failed.", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void insertTopicClass(TopicClass topicClass) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("class_id", topicClass.getClassId());
            values.put("topic_id", topicClass.getTopicId());
            values.put("title", topicClass.getTitle());
            values.put("vocab_count", topicClass.getVocabCount());
            values.put("grammar_count", topicClass.getGrammarCount());
            values.put("credit", topicClass.getCredit());
            values.put("photo_url", topicClass.getCoverPhoto());
            values.put("is_completed", topicClass.getIsCompleted());
            values.put("level", topicClass.getLevel());
            values.put("duration", topicClass.getDuration());
            values.put("available", topicClass.getIsAvalilable());
            values.put("class_url", topicClass.getUrl());
            values.put("level", topicClass.getLevel());
            values.put("is_bought", topicClass.getIsBought());

            db.insertWithOnConflict("topic_class", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        } catch (SQLException e) {
            Log.e(TAG, "Insert Failed.");
        } finally {
            db.close();
        }
    }

    public void insertTopicClasses(List<TopicClass> topicClasses, String topicId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.beginTransaction();

        String whereClause = "topic_id = ?";
        String[] whereArgs = {topicId};
        db.delete("topic_class", whereClause, whereArgs);

        try {
            for (TopicClass topicClass : topicClasses) {
                ContentValues values = new ContentValues();
                values.put("class_id", topicClass.getClassId());
                values.put("topic_id", topicClass.getTopicId());
                values.put("title", topicClass.getTitle());
                values.put("vocab_count", topicClass.getVocabCount());
                values.put("grammar_count", topicClass.getGrammarCount());
                values.put("credit", topicClass.getCredit());
                values.put("photo_url", topicClass.getCoverPhoto());
                values.put("is_completed", topicClass.getIsCompleted());
                values.put("level", topicClass.getLevel());
                values.put("duration", topicClass.getDuration());
                values.put("available", topicClass.getIsAvalilable());
                values.put("class_url", topicClass.getUrl());
                values.put("level", topicClass.getLevel());
                values.put("is_bought", topicClass.getIsBought());

                db.insertWithOnConflict("topic_class", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "Insert Failed.");
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<TopicClass> getTopicClasses(String topicId) {
        List<TopicClass> topicClasses = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] selectionArgs = {topicId};

        String sql = "SELECT * FROM topic_class WHERE topic_id = ?";
        Cursor mCur = db.rawQuery(sql, selectionArgs);
        try {
            if (mCur.moveToFirst()) {
                do {
                    TopicClass topicClass = new TopicClass();

                    topicClass.setClassId(mCur.getString(mCur.getColumnIndex("class_id")));
                    topicClass.setTitle(mCur.getString(mCur.getColumnIndex("title")));
                    topicClass.setIsAvailable(mCur.getInt(mCur.getColumnIndex("available")));
                    topicClass.setUrl(mCur.getString(mCur.getColumnIndex("content_url")));
                    topicClass.setCredit(mCur.getDouble(mCur.getColumnIndex("credit")));
                    topicClass.setDuration(mCur.getInt(mCur.getColumnIndex("duration")));
                    topicClass.setGrammarCount(mCur.getInt(mCur.getColumnIndex("grammar_count")));
                    topicClass.setIsCompleted(mCur.getInt(mCur.getColumnIndex("is_completed")));
                    topicClass.setCoverPhoto(mCur.getString(mCur.getColumnIndex("photo_url")));
                    topicClass.setTopicId(mCur.getString(mCur.getColumnIndex("topic_id")));
                    topicClass.setVocabCount(mCur.getInt(mCur.getColumnIndex("vocab_count")));
                    topicClass.setLevel(mCur.getString(mCur.getColumnIndex("level")));
                    topicClass.setUrl(mCur.getString(mCur.getColumnIndex("class_url")));
                    topicClass.setIsBought(mCur.getInt(mCur.getColumnIndex("is_bought")));

                    topicClasses.add(topicClass);
                } while (mCur.moveToNext());
            }
        } finally {
            mCur.close();
            db.close();
        }

        return topicClasses;
    }

    public TopicClass getTopicClassById(String classId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] selectionArgs = {classId};

        String sql = "SELECT * FROM topic_class WHERE class_id = ?";
        Cursor mCur = db.rawQuery(sql, selectionArgs);
        try {
            if (mCur.moveToFirst()) {
                TopicClass topicClass = new TopicClass();

                topicClass.setClassId(mCur.getString(mCur.getColumnIndex("class_id")));
                topicClass.setTitle(mCur.getString(mCur.getColumnIndex("title")));
                topicClass.setIsAvailable(mCur.getInt(mCur.getColumnIndex("available")));
                topicClass.setUrl(mCur.getString(mCur.getColumnIndex("content_url")));
                topicClass.setCredit(mCur.getDouble(mCur.getColumnIndex("credit")));
                topicClass.setDuration(mCur.getInt(mCur.getColumnIndex("duration")));
                topicClass.setGrammarCount(mCur.getInt(mCur.getColumnIndex("grammar_count")));
                topicClass.setIsCompleted(mCur.getInt(mCur.getColumnIndex("is_completed")));
                topicClass.setCoverPhoto(mCur.getString(mCur.getColumnIndex("photo_url")));
                topicClass.setTopicId(mCur.getString(mCur.getColumnIndex("topic_id")));
                topicClass.setVocabCount(mCur.getInt(mCur.getColumnIndex("vocab_count")));
                topicClass.setLevel(mCur.getString(mCur.getColumnIndex("level")));
                topicClass.setUrl(mCur.getString(mCur.getColumnIndex("class_url")));
                topicClass.setIsBought(mCur.getInt(mCur.getColumnIndex("is_bought")));

                return topicClass;
            }
        } finally {
            mCur.close();
            db.close();
        }

        return null;
    }

    public void insertTrialClass(TrialClass trialClass) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("class_id", trialClass.getClassId());
            values.put("title", trialClass.getTitle());
            values.put("article", trialClass.getArticle());
            values.put("image", trialClass.getImageUrl());

            db.insertWithOnConflict("trial_class", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        } catch (SQLException e) {
            Log.e(TAG, "Insert Failed.", e);
        } finally {
            db.close();
        }
    }

    public TrialClass getTrialClassById(String classId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] selectionArgs = {classId};

        String sql = "SELECT * FROM trial_class WHERE class_id = ?";
        Cursor mCur = db.rawQuery(sql, selectionArgs);
        try {
            if (mCur.moveToFirst()) {
                TrialClass trialClass = new TrialClass();

                trialClass.setClassId(mCur.getString(mCur.getColumnIndex("class_id")));
                trialClass.setTitle(mCur.getString(mCur.getColumnIndex("title")));
                trialClass.setImageUrl(mCur.getString(mCur.getColumnIndex("image")));
                trialClass.setArticle(mCur.getString(mCur.getColumnIndex("article")));

                return trialClass;
            }
        } finally {
            mCur.close();
            db.close();
        }

        return null;
    }

}