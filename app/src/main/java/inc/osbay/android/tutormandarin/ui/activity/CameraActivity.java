package inc.osbay.android.tutormandarin.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import inc.osbay.android.tutormandarin.R;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "CameraActivity";

    private Camera mCamera;

    private CameraPreview mPreview;

    private PictureCallback mPicture;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        final String mediaUrl = getIntent().getStringExtra(
                android.provider.MediaStore.EXTRA_OUTPUT);
        Log.d(TAG, "Image url - " + mediaUrl);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        if (mCamera == null) {
            this.finish();
            return;
        }

        mPicture = new PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                File dir = new File(mediaUrl.substring(0,
                        mediaUrl.lastIndexOf('/')));
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File pictureFile = new File(mediaUrl);
                // if (pictureFile == null){
                // mLog.d(TAG,
                // "Error creating media file, check storage permissions: ");
                // return;
                // }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }

                mCamera.release();
                mCamera = null;

                Intent intent = new Intent();
                intent.setData(Uri.fromFile(pictureFile));

                CameraActivity.this.setResult(RESULT_OK, intent);
                CameraActivity.this.finish();
            }
        };
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.fl_preview);
        preview.addView(mPreview);

        Button captureButton = (Button) findViewById(R.id.btn_shot);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );
    }

    @Override
    protected final void onDestroy() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        super.onDestroy();
    }

    private Camera getCameraInstance() {
        int cameraCount = 0;
        Camera cam = null;

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras(); // get cameras number

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }

        if (cam == null) {
            try {
                cam = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }

        return cam;
    }

    class CameraPreview extends SurfaceView implements
            SurfaceHolder.Callback {
        private static final String TAG = "CameraPreview";

        private SurfaceHolder mHolder;

        private Camera mCamera;

        private Camera.Size mPreviewSize;

        private List<Camera.Size> mSupportedPreviewSizes;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            mSupportedPreviewSizes = mCamera.getParameters()
                    .getSupportedPreviewSizes();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            final int width = resolveSize(getSuggestedMinimumWidth(),
                    widthMeasureSpec);
            final int height = resolveSize(getSuggestedMinimumHeight(),
                    heightMeasureSpec);
            setMeasuredDimension(width, height);

            if (mSupportedPreviewSizes != null) {
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes,
                        width,
                        height);
            }
        }

        private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes,
                                                  int w,
                                                  int h) {
            final double ASPECT_TOLERANCE = 0.1;
            double targetRatio = (double) h / w;

            if (sizes == null) {
                return null;
            }

            Camera.Size optimalSize = null;
            double minDiff = Double.MAX_VALUE;

            int targetHeight = h;

            for (Camera.Size size : sizes) {
                double ratio = (double) size.width / size.height;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                    continue;
                }

                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }

            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (Camera.Size size : sizes) {
                    if (Math.abs(size.height - targetHeight) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - targetHeight);
                    }
                }
            }
            return optimalSize;
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw
            // the
            // preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.setDisplayOrientation(90);

                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPreviewSize(mPreviewSize.width,
                        mPreviewSize.height);
                mCamera.setParameters(parameters);
                mCamera.startPreview();

            } catch (IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your
            // activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w,
                                   int h) {
            // If your preview can change or rotate, take care of those events
            // here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }

            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e) {
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }
    }
}
