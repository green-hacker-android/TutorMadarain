package inc.osbay.android.tutormandarin.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import inc.osbay.android.tutormandarin.TMApplication;
import inc.osbay.android.tutormandarin.ui.activity.MainActivity;

public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {

        Activity activity;

        public DefaultExceptionHandler(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
                Log.e("ExceptionHandler", "App crashed", ex);

//            try {

                Intent intent = new Intent(activity, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("class_type", "normal");
                intent.putExtras(bundle);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK);

                PendingIntent pendingIntent = PendingIntent.getActivity(
                        TMApplication.getInstance().getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                //Following code will restart your application after 2 seconds
                AlarmManager mgr = (AlarmManager) TMApplication.getInstance().getBaseContext()
                        .getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                        pendingIntent);

                //This will finish your activity manually
                activity.finish();

                //This will stop your application and take out from it.
                System.exit(2);

//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }