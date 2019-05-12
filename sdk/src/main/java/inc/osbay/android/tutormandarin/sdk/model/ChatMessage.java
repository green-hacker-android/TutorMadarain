package inc.osbay.android.tutormandarin.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatMessage {
    private String mMessageId;
    private String mSender;
    private String mSenderName;
    private String mTo;
    private String mBody;
    private String signalType;
    private String messageType;

    public ChatMessage() {

    }

    public ChatMessage(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            if (json.has("message_id"))
                mMessageId = json.getString("message_id");
            mSender = json.getString("account_id");
            if (json.has("account_name"))
                mSenderName = json.getString("account_name");
            mBody = json.getString("message_content");
            if (json.has("send_to"))
                mTo = json.getString("send_to");
            if (json.has("signal_type"))
                signalType = json.getString("signal_type");
            if (json.has("message_type"))
                messageType = json.getString("message_type");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public String getMessageId() {
        return mMessageId;
    }

    public void setMessageId(String messageId) {
        mMessageId = messageId;
    }

    public String getSender() {
        return mSender;
    }

    public void setSender(String sender) {
        mSender = sender;
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(String body) {
        mBody = body;
    }

    public String getSignalType() {
        return signalType;
    }

    public void setSignalType(String signalType) {
        this.signalType = signalType;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getTo() {
        return mTo;
    }

    public void setTo(String to) {
        mTo = to;
    }

    public String getSenderName() {
        return mSenderName;
    }

    public void setSenderName(String senderName) {
        mSenderName = senderName;
    }
}
