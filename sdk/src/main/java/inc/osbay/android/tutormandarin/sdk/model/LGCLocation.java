package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Erik on 7/28/2016.
 */
public class LGCLocation {
    private int mCountryId;
    private String mCityName;
    private String mCountryName;
    private String mTimeZone;

    public LGCLocation() {
    }

    public LGCLocation(JSONObject json) throws JSONException {
        mCountryId = json.getInt("country_id");
        mCityName = json.getString("city_name");
        mCountryName = json.getString("country_name");
        mTimeZone = json.getString("time_zone");
    }

    public int getCountryId() {
        return mCountryId;
    }

    public void setCountryId(int countryId) {
        mCountryId = countryId;
    }

    public String getCityName() {
        return mCityName;
    }

    public void setCityName(String cityName) {
        mCityName = cityName;
    }

    public String getCountryName() {
        return mCountryName;
    }

    public void setCountryName(String countryName) {
        mCountryName = countryName;
    }

    public String getTimeZone() {
        return mTimeZone;
    }

    public void setTimeZone(String timeZone) {
        mTimeZone = timeZone;
    }
}
