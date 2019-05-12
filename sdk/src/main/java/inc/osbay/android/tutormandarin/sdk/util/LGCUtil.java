package inc.osbay.android.tutormandarin.sdk.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;

public class LGCUtil {
    public static final String TEMP_PHOTO_URL = CommonConstant.IMAGE_PATH + File.separator + "temp.jpg";

    public static String getGenderName(int genderType) {
        if (genderType == 1) {
            return "Male";
        } else if (genderType == 2) {
            return "Female";
        } else {
            return "";
        }
    }

    public static String getCurrentUTCTimeString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(CommonConstant.DATE_TIME_FORMAT,
                Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }

    public static String convertToUTC(String localDateString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date localDate = dateFormat.parse(localDateString);

        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(localDate);
    }

    public static String convertToUTC(String localDateString, String format) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date localDate = dateFormat.parse(localDateString);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(localDate);
    }

    public static String convertToLocale(String utcDateString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date localDate = dateFormat.parse(utcDateString);

        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(localDate);
    }


    public static String convertUTCToLocale(String utcDateString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date localDate = dateFormat.parse(utcDateString);

        SimpleDateFormat dateFormat1 = new SimpleDateFormat("EE, MMM dd, yyyy", Locale.getDefault());
        dateFormat1.setTimeZone(TimeZone.getDefault());
        return dateFormat1.format(localDate);
    }

    public static String getStartTimeFromUTC(String utcDateString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date localDate = dateFormat.parse(utcDateString);

        SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateFormat1.setTimeZone(TimeZone.getDefault());
        return dateFormat1.format(localDate);
    }

    public static String convertToLocaleNoSecond(String utcDateString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date localDate = dateFormat.parse(utcDateString);

        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(localDate);
    }

    /**
     * Copy file to given destination from source.
     *
     * @param sourceUrl      File path to copy
     * @param destinationUrl File path to save
     */
    public static void copyFile(String sourceUrl, String destinationUrl)
            throws Exception {

        InputStream in = null;
        OutputStream out = null;
        try {

            // create output directory if it doesn't exist
            if (!TextUtils.isEmpty(destinationUrl)) {
                File dir = new File(destinationUrl.substring(0,
                        destinationUrl.lastIndexOf('/')));
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            }

            in = new FileInputStream(sourceUrl);
            out = new FileOutputStream(destinationUrl);

            byte[] buffer = new byte[CommonConstant.BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();

        } finally {
            try {
                if (in != null) {
                    in.close();
                }

                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Read image file.
     *
     * @param photoUrl Image file path to read
     * @return Image bitmap
     */
    public static Bitmap readImageFile(String photoUrl) {
        FileInputStream in = null;
        BufferedInputStream buf;

        Bitmap bmp = null;
        try {
            in = new FileInputStream(photoUrl);
            buf = new BufferedInputStream(in, CommonConstant.BUFFER_INPUT_SIZE);
            byte[] buffer = new byte[buf.available()];
            buf.read(buffer);
            bmp = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
        } catch (IOException | OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return bmp;
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
                    CommonConstant.IMAGE_QUALITY, out);
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

    public static boolean isFileExists(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        File f = new File(path);
        return f.exists();
    }

    public static String encodeFileToBase64(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        FileInputStream in = null;
        BufferedInputStream buf;
        FileOutputStream out = null;
        InputStream inputStream = null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            if (!url.endsWith(".amr")) {
                in = new FileInputStream(url);
                buf = new BufferedInputStream(in, CommonConstant.BUFFER_SIZE);
                byte[] buffer = new byte[buf.available()];
                buf.read(buffer);
                int fixValue = 300;
                int widthValue;
                int heightValue;

                Bitmap bmp = BitmapFactory.decodeByteArray(buffer, 0,
                        buffer.length);
                if (bmp.getWidth() > bmp.getHeight()) {
                    float ratio = (float) bmp.getHeight()
                            / (float) bmp.getWidth();
                    heightValue = (int) (fixValue * ratio);
                    widthValue = fixValue;
                } else {
                    float ratio = (float) bmp.getWidth()
                            / (float) bmp.getHeight();
                    widthValue = (int) (fixValue * ratio);
                    heightValue = fixValue;
                }
                Matrix rotateMatrix = rotateImage(url);
                Bitmap scaled = Bitmap.createScaledBitmap(bmp,
                        widthValue,
                        heightValue, true);

                Bitmap rotateBitmap = Bitmap.createBitmap(scaled, 0, 0,
                        scaled.getWidth(),
                        scaled.getHeight(), rotateMatrix, true);
                out = new FileOutputStream(TEMP_PHOTO_URL);
                rotateBitmap.compress(Bitmap.CompressFormat.JPEG,
                        CommonConstant.IMAGE_QUALITY, out);
                bmp.recycle();
            }

            inputStream = new FileInputStream(url);
            byte[] buffer1 = new byte[8192];
            int bytesRead;
            Base64OutputStream output64 = new Base64OutputStream(output,
                    Base64.DEFAULT);
            while ((bytesRead = inputStream.read(buffer1)) != -1) {
                output64.write(buffer1, 0, bytesRead);
            }

            inputStream.close();
            output64.close();
        } catch (IOException | OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

                if (out != null) {
                    out.close();
                }

                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return output.toString();
    }

    public static Matrix rotateImage(String filePath) {
        Matrix matrix = new Matrix();

        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 1);

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }
        } catch (Exception exception) {
            Log.d("Rotate Imgae", "Can't rotate image");
        }
        return matrix;
    }

    public static boolean isScheduledTrialClassExpired(String utcDateString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date localDate = dateFormat.parse(utcDateString);

        Calendar scheduledDate = Calendar.getInstance();
        scheduledDate.setTime(localDate);

        Calendar calendarDate = Calendar.getInstance();
        calendarDate.setTime(new Date());

        boolean isExpired;
        if (calendarDate.compareTo(scheduledDate) > 0) {
            isExpired = true;
        } else {
            isExpired = false;
        }

        return isExpired;
    }

}
