package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

public class WhatsOnVocab {
    private String mWhatsOnVocabId;
    private String mWhatsOnId;
    private String mCharacter;
    private String mDefinition;
    private String mPartOfSpeech;
    private String mPinyin;

    private String mPhoto;
    private String mVoice;
    private String mExample;

    public WhatsOnVocab() {
    }

    public WhatsOnVocab(JSONObject jsonObject) throws JSONException {
        mWhatsOnVocabId = jsonObject.getString("vocab_id");
        mCharacter = jsonObject.getString("characters");
        mDefinition = jsonObject.getString("definition");
        mPartOfSpeech = jsonObject.getString("partOfSpeech");
        mPinyin = jsonObject.getString("title");

        try {
            mPhoto = jsonObject.getString("cover_photo");
            mVoice = jsonObject.getString("voice_url");
            mExample = jsonObject.getString("example");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getChatacter() {
        return mCharacter;
    }

    public void setChatacter(String chatacter) {
        this.mCharacter = chatacter;
    }

    public String getWhatsOnVocabId() {
        return mWhatsOnVocabId;
    }

    public void setWhatsOnVocabId(String mVideoVocabId) {
        this.mWhatsOnVocabId = mVideoVocabId;
    }

    public String getWhatsOnId() {
        return mWhatsOnId;
    }

    public void setWhatsOnId(String whatsOnId) {
        this.mWhatsOnId = whatsOnId;
    }

    public String getDefinition() {
        return mDefinition;
    }

    public void setDefinition(String mDefinition) {
        this.mDefinition = mDefinition;
    }

    public String getPartOfSpeech() {
        return mPartOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.mPartOfSpeech = partOfSpeech;
    }

    public String getPinyin() {
        return mPinyin;
    }

    public void setPinyin(String pinyin) {
        this.mPinyin = pinyin;
    }

    public String getExample() {
        return mExample;
    }

    public void setExample(String mExample) {
        this.mExample = mExample;
    }

    public String getPhoto() {
        return mPhoto;
    }

    public void setPhoto(String photo) {
        this.mPhoto = photo;
    }

    public String getVoice() {
        return mVoice;
    }

    public void setVoice(String voice) {
        this.mVoice = voice;
    }
}
