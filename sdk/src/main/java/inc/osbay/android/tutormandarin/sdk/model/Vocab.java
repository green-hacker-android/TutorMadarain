package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Vocab extends FlashCard {
    private String mPhoto;
    private String mVoice;
    private String mPinyin;
    private String mDefinition;

    public Vocab(){
        super();
    }

    public Vocab(JSONObject json) throws JSONException {
        super(json, 1);

        mPhoto = json.getString("cover_photo");
        mVoice = json.getString("voice_url");
        mPinyin = json.getString("title");
        mDefinition = json.getString("definition");
    }

    public String getPhoto() {
        return mPhoto;
    }

    public void setPhoto(String photo) {
        mPhoto = photo;
    }

    public String getVoice() {
        return mVoice;
    }

    public void setVoice(String voice) {
        mVoice = voice;
    }

    public String getPinyin() {
        return mPinyin;
    }

    public void setPinyin(String pinyin) {
        mPinyin = pinyin;
    }

    public String getDefinition() {
        return mDefinition;
    }

    public void setDefinition(String definition) {
        mDefinition = definition;
    }
}
