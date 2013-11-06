
package app.istts.ar;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
 * handle taking picture, OCR using tesseract,
 * take picture and use WS if OCR failed.
 */

public class CameraFragment extends Fragment {

    private static CameraFragment instance;

    public static CameraFragment getInstance() {
        if (instance == null)
            instance = new CameraFragment();
        return instance;
    }

    private final static String TAG = "iSTTSAR::CameraFragment";

    private Camera mCamera;
    private CameraPreview mPreview;

    private TessBaseAPI baseApi;
    private Boolean stopTake;
    private Boolean ocrMode;
    private String[] namaruangan;

    private Boolean OCRProcessing;

    public void setOCRMode(Boolean value) {
        this.ocrMode = value;
    }

    public Boolean getOCRMode() {
        return this.ocrMode.booleanValue();
    }

    private setLocation mCallback;

    public interface setLocation {
        public void setLocationStatus(String location);

        public void setLocationButtonVisible(Boolean state);

        public void setIndoorLabel(String location);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** AMBIL LIST NAMA RUANGAN DARI STRING ARRAY (VALUES) **/
        Resources res = getResources();
        namaruangan = res.getStringArray(R.array.namaruangan_array);

        /** INISIALISASI KAMERA **/
        if (checkCameraHardware(getActivity())) {
            mPreview = new CameraPreview(getActivity());
        } else {
            Log.e(TAG, "Camera is not found!");
            Toast.makeText(getActivity(), "Camera is not found!", Toast.LENGTH_LONG).show();
        }

        /** INISIALISASI OCR TESSERACT **/
        baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(getActivity().getExternalCacheDir().getAbsolutePath()
                + File.separator, "ind");

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
        OCRProcessing = false;
        final Handler handler = new Handler();
        Runnable takeImage = new Runnable() {
            @Override
            public void run() {
                if (stopTake) {
                    handler.removeCallbacks(this);
                } else {
                    if (ocrMode) {
                        mCamera.setOneShotPreviewCallback(asyncTakePicture);
                    }

                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(takeImage);
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (setLocation) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement setLocation");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    };

    public void takePicture(String path) {
        final PhotoHandler photoHandler = new PhotoHandler(getActivity().getApplicationContext());
        photoHandler.setPath(path);
        mCamera.takePicture(null, null, photoHandler);
    }

    public void takePictureOCR() {
        OCRHandler ocrHandler = new OCRHandler(getActivity().getApplicationContext());
        mCamera.takePicture(null, null, ocrHandler);
    }

    public void takePictureOpenCV() {
        final OpenCVHandler openCVHandler = new OpenCVHandler(getActivity().getApplicationContext());
        mCamera.takePicture(null, null, openCVHandler);
    }

    // ambil gambar dengan async. (untuk OCR)
    // tidak mengganggu UI.
    private Camera.PreviewCallback asyncTakePicture = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(final byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            final Size size = parameters.getPreviewSize();

            if (OCRProcessing == false) {
                // mengubah format gambar dari kamera (RAW) dengan YUV ke (JPEG)
                // RGB.

                if (parameters.getPreviewFormat() == ImageFormat.NV21) {
                    OCRProcessing = true;
                    Log.d(TAG, "PICTURE TAKEN");
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    YuvImage yuvImage = new YuvImage(data,
                            ImageFormat.NV21,
                            size.width,
                            size.height,
                            null);
                    yuvImage.compressToJpeg(new Rect(0, 0, size.width, size.height),
                            80, out);
                    byte[] imageBytes = out.toByteArray();
                    Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0,
                            imageBytes.length);

                    baseApi.setDebug(true);
                    baseApi.init(getActivity().getExternalCacheDir().getAbsolutePath()
                            + File.separator, "ind");
                    // cek gambar dengan tesseract
                    baseApi.setImage(Bitmap.createScaledBitmap(image, 320, 240, false));
                    // ambil hasil dengan getUTF8Text,
                    // hapus semua whitespace dan simbol dalam text
                    String text = baseApi.getUTF8Text().replaceAll("\\s|\\W|_", "");

                    baseApi.end();
                    for (int i = 0; i < namaruangan.length; i++) {
                        if (text.matches(".*" + namaruangan[i] + ".*")) {
                            mCallback.setLocationStatus(namaruangan[i]);
                            Log.d(TAG, "MATCH: " + text);
                        }
                    }
                    Log.d(TAG, "TEXT: " + text);
                    OCRProcessing = false;
                    Log.d(TAG, "PICTURE PROCESSED");
                }
            }

        }
    };

    /** BACKGROUND **/
    View.OnClickListener autoFocusListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                mCamera.autoFocus(myAutoFocusCallback);
            } catch (NullPointerException err) {
                Log.d(TAG, "Camera autofocus null");
            }

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

    private class OCRHandler implements PictureCallback {

        private final Context context;

        public OCRHandler(Context context) {
            this.context = context;
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                Bitmap savedPicture = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(savedPicture, 640, 480, false);

                baseApi.setDebug(true);
                baseApi.init(getActivity().getExternalCacheDir().getAbsolutePath()
                        + File.separator, "ind");

                // cek gambar dengan tesseract
                baseApi.setImage(resizedBitmap);
                // ambil hasil dengan getUTF8Text,
                // hapus semua whitespace dan simbol dalam text
                String text = baseApi.getUTF8Text().replaceAll("\\s|\\W|_", "");

                baseApi.end();

                for (int i = 0; i < namaruangan.length; i++) {
                    if (text.matches(".*" + namaruangan[i] + ".*")) {
                        // mCallback.setLocationStatus(namaruangan[i]);
                        mCallback.setIndoorLabel(namaruangan[i]);
                        Log.d(TAG, "MATCH: " + text);
                    }
                }
                Log.d(TAG, "TEXT: " + text);
                mCallback.setLocationButtonVisible(true);

                /** restart camera preview for resuming after taking picture **/
                camera.startPreview();

            } catch (Exception error) {
                Log.e(TAG, "OCR Failed : " + error.toString());
                Toast.makeText(context, "OCR failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class OpenCVHandler implements PictureCallback {

        private final Context context;

        public OpenCVHandler(Context context) {
            this.context = context;
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                String path = getActivity().getExternalCacheDir().getAbsolutePath() + "/match.jpg";
                Bitmap savedPicture = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(savedPicture, 640, 480, true);

                FileOutputStream out = new FileOutputStream(path);
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.close();

                /** restart camera preview for resuming after taking picture **/
                camera.startPreview();
            } catch (Exception error) {
                Log.e(TAG, "image could not be detected : " + error.toString());
                Toast.makeText(context, "Image could not be detected.", Toast.LENGTH_LONG).show();
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
