package inc.osbay.android.tutormandarin.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import inc.osbay.android.tutormandarin.service.MessengerService;

public class WSMessageClient {

    Context mContext;
    Messenger mService = null;
    boolean mIsBound;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            mService = new Messenger(service);
            // TODO remote service connected.
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;

            // TODO remote service disconnected.
        }
    };

    public WSMessageClient(Context context) {
        mContext = context;
    }

    public void addMessenger(Messenger messenger) {
        if (mService != null) {
            try {
                Message msg = Message.obtain(null,
                        MessengerService.MSG_REGISTER_CLIENT);
                msg.replyTo = messenger;
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void doBindService() {
        mContext.bindService(new Intent(mContext,
                MessengerService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    public void removeMessenger(Messenger messenger) {
        if (mIsBound && mService != null) {
            try {
                Message msg = Message.obtain(null,
                        MessengerService.MSG_UNREGISTER_CLIENT);
                msg.replyTo = messenger;
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            mContext.unbindService(mConnection);
            mIsBound = false;
        }
    }

    public void sendMessage(Message message) throws RemoteException {
        mService.send(message);
    }
}
