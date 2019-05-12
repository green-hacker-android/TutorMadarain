package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

public class VideoVocab {
    private String mVideoVocabId;
    private String mVideoId;
    private String mCharacter;
    private String mDefinition;
    private String mPartOfSpeech;
    private String mPinyin;

    private String mPhoto;
    private String mVoice;
    private String mExample;

    public VideoVocab() {
    }

    public VideoVocab(JSONObject jsonObject) throws JSONException {
        mVideoVocabId = jsonObject.getString("vocab_id");
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

    public String getVideoVocabId() {
        return mVideoVocabId;
    }

    public void setVideoVocabId(String mVideoVocabId) {
        this.mVideoVocabId = mVideoVocabId;
    }

    public String getVideoId() {
        return mVideoId;
    }

    public void setVideoId(String mVideoId) {
        this.mVideoId = mVideoId;
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
