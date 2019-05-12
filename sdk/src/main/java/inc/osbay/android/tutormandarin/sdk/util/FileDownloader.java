package inc.osbay.android.tutormandarin.sdk.util;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;

public class FileDownloader {
    private static final String TAG = FileDownloader.class.getSimpleName();

    private static OnDownloadFinishedListener downloadFinishedListener;

    public static void downloadImage(String remoteUrl, OnDownloadFinishedListener listener) {
        downloadFinishedListener = listener;

        new DownloadFileTask().execute(remoteUrl);
    }

    public interface OnDownloadFinishedListener {
        void onSuccess();

        void onError();
    }

    private static class DownloadFileTask extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... urls) {
            String videoUrl = urls[0];

            File mediaPath = new File(CommonConstant.MEDIA_PATH);
            if (!mediaPath.exists())
                mediaPath.mkdirs();

            String fileName = videoUrl.substring(videoUrl.lastIndexOf('/') + 1, videoUrl.length());
            File file = new File(mediaPath, fileName);

            try {
                if (urls.length == 1) {
                    URL remoteUrl = new URL(videoUrl);
                    Log.d(TAG, "Downloading file from - " + remoteUrl);

                    URLConnection urlConnection = remoteUrl.openConnection();

                    urlConnection.connect();

                    FileOutputStream fileOutput = new FileOutputStream(file);
                    InputStream inputStream = new BufferedInputStream(remoteUrl.openStream(), 8192);

                    byte[] buffer = new byte[1024];
                    int bufferLength;

                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        fileOutput.write(buffer, 0, bufferLength);
                    }
                    fileOutput.close();

                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (file.exists()) {
                    file.delete();
                }
            }
            return false;
        }

        public void onPostExecute(Boolean result) {
            if (result) {
                if (downloadFinishedListener != null) {
                    downloadFinishedListener.onSuccess();
                }
            } else {
                if (downloadFinishedListener != null) {
                    downloadFinishedListener.onError();
                }
            }
        }
    }
}