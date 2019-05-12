package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class FlashCardDeck implements Serializable {

    private int mDeckId;

    private String mCourseId;

    private String mCardTitle;

    private String mCardLevel;

    private int mType; // 1. Vocab, 2. Grammar, 3. Custom(Vocab/ Grammar)

    private int mCardCount;

    public FlashCardDeck() {

    }

    public int getDeckId() {
        return mDeckId;
    }

    public void setDeckId(int mDeckId) {
        this.mDeckId = mDeckId;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public FlashCardDeck(JSONObject json) throws JSONException {
        mCourseId = json.getString("course_id");
        mCardLevel = json.getString("language_level");

        mType = json.getInt("type");
        mCardCount = json.getInt("flashcard_count");
        if (mType == Type.VOCAB) {
            mCardTitle = mCardLevel + " Vocab";
        } else if (mType == Type.GRAMMAR) {
            mCardTitle = mCardLevel + " Grammar";
        }
    }

    public String getCourseId() {
        return mCourseId;
    }

    public void setCourseId(String courseId) {
        mCourseId = courseId;
    }

    public String getCardTitle() {
        return mCardTitle;
    }

    public void setCardTitle(String cardTitle) {
        mCardTitle = cardTitle;
    }

    public String getCardLevel() {
        return mCardLevel;
    }

    public void setCardLevel(String cardLevel) {
        mCardLevel = cardLevel;
    }

    public int getCardCount() {
        return mCardCount;
    }

    public void setCardCount(int cardCount) {
        mCardCount = cardCount;
    }

    public static class Type {
        public static final int VOCAB = 1;

        public static final int GRAMMAR = 2;

        public static final int CUSTOM = 3;

        public static final int VIDEO_VOCAB = 4;

        public static final int WHATS_ON_VOCAB = 5;
    }
}
