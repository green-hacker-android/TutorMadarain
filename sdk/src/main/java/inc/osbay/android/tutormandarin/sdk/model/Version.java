package inc.osbay.android.tutormandarin.sdk.model;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

import org.json.JSONException;
import org.json.JSONObject;

public class Version
{

    private String id;
    private String versionNumber;
    private String versionCode;
    private String description;
    private String isForce;
    private String publishLink;
    private String serverLink;
    private String ownLink;
    private String isPublish;
    private String publishDate;
    private String deviceType;
    private String updateDate;

    public Version(JSONObject json) throws JSONException {
        this.id = json.getString("id");
        this.versionNumber = json.getString("version_number");
        this.versionCode = json.getString("version_code");
        this.description = json.getString("description");
        this.isForce = json.getString("is_force");
        this.publishLink = json.getString("publish_link");
        serverLink= json.getString("own_link");
        this.ownLink = json.getString("own_link");
        this.isPublish = json.getString("is_publish");
        this.publishDate = json.getString("publish_date");
        this.deviceType = json.getString("device_type");
        this.updateDate = json.getString("update_date");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIsForce() {
        return isForce;
    }

    public void setIsForce(String isForce) {
        this.isForce = isForce;
    }

    public String getPublishLink() {
        return publishLink;
    }

    public void setPublishLink(String publishLink) {
        this.publishLink = publishLink;
    }

    public String getOwnLink() {
        return ownLink;
    }

    public void setOwnLink(String ownLink) {
        this.ownLink = ownLink;
    }

    public String getIsPublish() {
        return isPublish;
    }

    public void setIsPublish(String isPublish) {
        this.isPublish = isPublish;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getServerLink() {
        return serverLink;
    }

    public void setServerLink(String serverLink) {
        this.serverLink = serverLink;
    }

    public void setDeviceType(String deviceType) {

        this.deviceType = deviceType;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }
}
