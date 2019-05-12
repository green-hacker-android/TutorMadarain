package inc.osbay.android.tutormandarin.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import com.artifex.mupdf.fitz.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import inc.osbay.android.tutormandarin.R;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;
import inc.osbay.android.tutormandarin.ui.activity.LessonPdfActivity;

public class DownloadFile extends AsyncTask<String, Void, Void> {
    private String mLessonId;
    private ProgressDialog mDialog;
    private Context mContext;
    private String fileName;
    private String fileUrl;

    public DownloadFile(Context context) {
        mContext = context;
        mDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog.setMessage("Downloading....");
        mDialog.setCancelable(false);
        mDialog.show();
    }

    @Override
    protected Void doInBackground(String... strings) {
        fileUrl = strings[0];
        fileName = strings[1];
        mLessonId = strings[2];
        File folder = new File(CommonConstant.PDF_PATH);
        folder.mkdir();

        File pdfFile = new File(folder, fileName);

        try {
            pdfFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileDownloader fileDownloader = new FileDownloader();
        fileDownloader.downloadFile(fileUrl, pdfFile);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mDialog.dismiss();
        String url = CommonConstant.PDF_PATH + File.separator + fileName;
        File file = new File(url);
        try {
            if (!TextUtils.isEmpty(fileName)) {
                Document mDocument = Document.openDocument(url);

                if (mDocument.countPages() > 0) {
                    Intent intent = new Intent(mContext, LessonPdfActivity.class);
                    intent.putExtra(LessonPdfActivity.FILE_NAME, fileName);
                    intent.putExtra(LessonPdfActivity.EXTRA_PDF_LESSON_ID, mLessonId);
                    mContext.startActivity(intent);
                }
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.ls_no_file), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            if (file.exists()) {
                boolean isDeleted = file.delete();
                if (isDeleted) {
                    Toast.makeText(mContext, mContext.getString(R.string.ls_file_incorrect), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    private class FileDownloader {
        private static final int MEGABYTE = 1024 * 1024;

        void downloadFile(String fileUrl, File directory) {
            try {

                URL url = new URL(fileUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                //urlConnection.setRequestMethod("GET");
                //urlConnection.setDoOutput(true);
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(directory);
                int totalSize = urlConnection.getContentLength();

                byte[] buffer = new byte[MEGABYTE];
                int bufferLength = 0;
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, bufferLength);
                }
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}