package inc.osbay.android.tutormandarin.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class ImageDownloader {
    private static final String TAG = ImageDownloader.class.getSimpleName();

    private static OnDownloadFinishedListener downloadFinishedListener;

    public static void downloadImage(String remoteUrl, String localUrl, OnDownloadFinishedListener listener) {
        downloadFinishedListener = listener;

        new DownloadFileTask().execute(remoteUrl, localUrl);
    }

    public interface OnDownloadFinishedListener {
        void onSuccess();

        void onError();
    }

    private static class DownloadFileTask extends AsyncTask<String, Integer, Bitmap> {
        protected Bitmap doInBackground(String... urls) {
            try {
                if (urls.length == 2) {
                    URL remoteUrl = new URL(urls[0]);
                    String localUrl = urls[1];
                    Log.d(TAG, "Downloading image." + remoteUrl + localUrl);

                    Bitmap bitmap = BitmapFactory.decodeStream(remoteUrl.openConnection().getInputStream());
                    saveImageFile(bitmap, localUrl);

                    return bitmap;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onPostExecute(Bitmap result) {
            if (result != null) {
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
    /**
     * Saved Image file at given file path.
     *
     * @param bmp      Image's Bit Map
     * @param localUrl File path with file name
     */
    public static void saveImageFile(Bitmap bmp, String localUrl) {
        FileOutputStream out = null;

        // create output directory if it doesn't exist
        if (!TextUtils.isEmpty(localUrl)) {
            File dir = new File(localUrl.substring(0,
                    localUrl.lastIndexOf('/')));
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }

        try {
            out = new FileOutputStream(localUrl);
            bmp.compress(Bitmap.CompressFormat.JPEG,
                    100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}