package app.istts.ar;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/* CAMERAFRAGMENT
 * show camera preview on a fragment
 */

public class CameraFragment extends Fragment {
    
    private final static String TAG = "iSTTSAR::CameraFragment";
    
    private Camera mCamera;
    private CameraPreview mPreview;

    private Boolean stopTake;
    private Boolean ocrMode;

    public void setOCRMode(Boolean value) {
        this.ocrMode = value;
    }

    public Boolean getOCRMode() {
        return this.ocrMode.booleanValue();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkCameraHardware(getActivity())) {
            mPreview = new CameraPreview(getActivity());
        } else {
            Log.e(TAG, "Camera is not found!");
            Toast.makeText(getActivity(), "Camera is not found!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPreview.setOnClickListener(autoFocusListener);
        return mPreview;
    }

    @Override
    public void onPause() {
        super.onPause();
        
        if (mCamera != null) {
            stopTake = true;
            mPreview.setCamera(null);
            releaseCamera(); // release the camera immediately on pause event
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        mCamera = getCameraInstance();
        mPreview.setCamera(mCamera);

        stopTake = false;
        ocrMode = false;
        final Handler handler = new Handler();
        Runnable takeImage = new Runnable() {
            @Override
            public void run() {
                if (stopTake) {
                    handler.removeCallbacks(this);
                } else {
                    if (ocrMode)
                        mCamera.setOneShotPreviewCallback(asyncTakePicture);
                    
                    handler.postDelayed(this, 500);
                }
            }
        };
        handler.post(takeImage);
        // mCamera.setPreviewCallback(this);
        // mCamera.addCallbackBuffer(new byte[2048]);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mPreview != null) {
            ViewGroup parentViewGroup = (ViewGroup) mPreview.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
                mPreview = null;
            }

        }
    }

    public void takePicture(String path) {
        final PhotoHandler photoHandler = new PhotoHandler(getActivity().getApplicationContext());
        photoHandler.setPath(path);
        mCamera.takePicture(null, null, photoHandler);
    }

    private Camera.PreviewCallback asyncTakePicture = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(final byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            final Size size = parameters.getPreviewSize();

            if (parameters.getPreviewFormat() == ImageFormat.NV21) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                YuvImage yuvImage = new YuvImage(data,
                        ImageFormat.NV21,
                        size.width,
                        size.height,
                        null);
                yuvImage.compressToJpeg(new Rect(0, 0, size.width, size.height),
                        50, out);
                byte[] imageBytes = out.toByteArray();
                Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0,
                        imageBytes.length);

                TessBaseAPI baseApi = new TessBaseAPI();
                baseApi.setDebug(true);
                baseApi.init(getActivity().getExternalCacheDir().getAbsolutePath()
                        + File.separator, "ind");
                baseApi.setImage(image);

                String recognizedText = baseApi.getUTF8Text();
                Log.d(TAG, "TEXT: " + recognizedText);
            }
        }
    };

    /** BACKGROUND **/

    View.OnClickListener autoFocusListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCamera.autoFocus(myAutoFocusCallback);
        }
    };

    /** CAMERA **/

    /** Check if device has a camera **/
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. **/
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            // Camera is not available
            Log.e(TAG, "Camera is not available!");
        }
        return c;
    }

    /** release the camera for other applications **/
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private class PhotoHandler implements PictureCallback {

        private final Context context;
        private String path;

        public PhotoHandler(Context context) {
            this.context = context;
            this.path = "";
        }

        public void setPath(String path) {
            this.path = path;
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (!path.equals("")) {
                try {
                    // TODO: use inSampleSize to resize picture to 640 by 480?
                    Bitmap savedPicture = BitmapFactory.decodeByteArray(data, 0, data.length);
                    // resize picture
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(savedPicture, 640, 480, true);

                    FileOutputStream out = new FileOutputStream(path);
                    resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();

                    /** restart camera preview for resuming after taking picture **/
                    camera.startPreview();
                    Log.d(TAG, "image saved to " + path);
                } catch (Exception error) {
                    Log.e(TAG, "image could not be saved : " + error.toString());
                    Toast.makeText(context, "Image could not be saved.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            // null procedure. to handle autofocus.
        }
    };

}
