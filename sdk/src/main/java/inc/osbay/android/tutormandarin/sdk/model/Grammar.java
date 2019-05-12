package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Grammar extends FlashCard {

    private String mEnglish;
    private String mDescription;

    public Grammar(){}

    public Grammar(JSONObject json) throws JSONException {
        super(json, 2);

        mEnglish = json.getString("spelling");
        mDescription = json.getString("english_description");
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getEnglish() {
        return mEnglish;
    }

    public void setEnglish(String english) {
        mEnglish = english;
    }
}
