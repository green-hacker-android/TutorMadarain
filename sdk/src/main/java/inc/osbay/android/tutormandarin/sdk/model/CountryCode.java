package inc.osbay.android.tutormandarin.sdk.model;

import java.util.Locale;

/**
 * Created by osbay on 10/13/16.
 */
public class CountryCode {
    private int mCodeId;
    private String mCountry;
    private int mCode;

    public int getCodeId() {
        return mCodeId;
    }

    public void setCodeId(int codeId) {
        this.mCodeId = codeId;
    }

    public String getCountry() {
        return mCountry;
    }

    public void setCountry(String country) {
        this.mCountry = country;
    }

    public int getCode() {
        return mCode;
    }

    public void setCode(int code) {
        this.mCode = code;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "+%d", mCode);
    }
}
