package inc.osbay.android.tutormandarin.util;

import android.content.Context;

import com.twilio.video.ConnectOptions;
import com.twilio.video.Room;

public class TwilioVideoUtil {
    private static final String TAG = TwilioVideoUtil.class.getSimpleName();

    private static TwilioVideoUtil mInstance;

    private static String mAccessToken;

    private Room.Listener mRoomListener;

//    private VideoClient mVideoClient;
    private Room mRoom;

    public static TwilioVideoUtil getInstance() {
        if (mInstance == null) {
            mInstance = new TwilioVideoUtil();
        }
        return mInstance;
    }

    public void initialize(Context context, String accessToken) {
        mAccessToken = accessToken;

//        mVideoClient = new VideoClient(context, accessToken);
    }

//    public void joinToRoom(String roomName, LocalMedia localMedia) {
//        ConnectOptions connectOptions = new ConnectOptions.Builder()
//                .roomName(roomName)
//                .localMedia(localMedia)
//                .build();
//
//        // Connect to the room
//        mRoom = mVideoClient.connect(connectOptions, mRoomListener);
//    }

    public void setRoomListener(Room.Listener roomListener) {
        mRoomListener = roomListener;
    }

//    public VideoClient getVideoClient(){
//        return mVideoClient;
//    }

    public Room getRoom(){
        return mRoom;
    }
}
