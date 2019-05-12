package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

public class FlashCard {

    private String mFlashCardId;
    private String mCourseId;
    private String mChinese;
    private String mPartOfSpeech; // temp
    private String mExample;
    private int mType; // 1. Vocab, 2. Grammar

    public FlashCard(){}

    public FlashCard(JSONObject json, int type) throws JSONException {
        mFlashCardId = json.getString("id");
        mCourseId = json.getString("course_id");
        mChinese = json.getString("characters");
        mPartOfSpeech = json.getString("part_of_speech");
        mExample = json.getString("example");
        mType = type;
    }

    public String getChinese() {
        return mChinese;
    }

    public void setChinese(String chinese) {
        mChinese = chinese;
    }

    public String getFlashCardId() {
        return mFlashCardId;
    }

    public void setFlashCardId(String flashCardId) {
        mFlashCardId = flashCardId;
    }

    public String getCourseId() {
        return mCourseId;
    }

    public void setCourseId(String courseId) {
        mCourseId = courseId;
    }

    public String getPartOfSpeech() {
        return mPartOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        mPartOfSpeech = partOfSpeech;
    }

    public String getExample() {
        return mExample;
    }

    public void setExample(String example) {
        mExample = example;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public static class Type {
        public static final int VOCAB = 1;

        public static final int GRAMMAR = 2;

        public static final int VIDEO_VOCAB = 3;

        public static final int WHATS_ON_VOCAB = 4;
    }
}
