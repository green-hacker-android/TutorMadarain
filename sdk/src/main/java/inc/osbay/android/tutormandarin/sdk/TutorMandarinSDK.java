package inc.osbay.android.tutormandarin.sdk;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import inc.osbay.android.tutormandarin.sdk.constant.ErrorCode;
import inc.osbay.android.tutormandarin.sdk.database.DataBaseHelper;
import inc.osbay.android.tutormandarin.sdk.listener.StatusListener;

public class TutorMandarinSDK {
    private static final String TAG = TutorMandarinSDK.class.getSimpleName();

    public static void initialize(Context mContext, StatusListener listener) {
        DataBaseHelper mDbHelper = new DataBaseHelper(mContext);
        try {
            mDbHelper.createDataBase();

            mDbHelper.close();

            listener.onSuccess();
        } catch (IOException e) {
            Log.e(TAG, "Database create fail.", e);
            listener.onError(ErrorCode.DB_CREATE_FAIL);
        }
    }
}
