package app.istts.ar;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.FileOutputStream;

/* CAMERAFRAGMENT
 * show camera preview on a fragment
 */

public class CameraFragment extends Fragment {
    
    private final static String TAG = "iSTTSAR::CameraFragment";
    
    private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkCameraHardware(this.getActivity())) {
            mPreview = new CameraPreview(this.getActivity());
        } else {
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
            mPreview.setCamera(null);
            releaseCamera(); // release the camera immediately on pause event
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        mCamera = getCameraInstance();
        mPreview.setCamera(mCamera);
    }

    public void takePicture() {
        mCamera.takePicture(null, null, new PhotoHandler(getActivity().getApplicationContext()));
    }

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
            Log.d(TAG, "Camera is not available!");
        }
        return c;
    }

    /** release the camera for other applications **/
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /** restart camera preview for resuming after taking picture **/
    public void restartCameraPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }

    private class PhotoHandler implements PictureCallback {

        private final Context context;

        public PhotoHandler(Context context) {
            this.context = context;
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            String savepath = getActivity().getExternalCacheDir().getAbsolutePath() + "/temp.jpg";

            try {
                Bitmap savedPicture = BitmapFactory.decodeByteArray(data, 0, data.length);
                // resize picture
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(savedPicture, 640, 480, true);

                FileOutputStream out = new FileOutputStream(savepath);
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();

                Log.d(TAG, "image saved to " + savepath);

            } catch (Exception error) {
                Log.d(TAG, "image could not be saved : " + error.toString());
                Toast.makeText(context, "Image could not be saved.", Toast.LENGTH_LONG).show();
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
