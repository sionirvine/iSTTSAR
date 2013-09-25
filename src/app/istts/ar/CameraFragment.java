
package app.istts.ar;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

}
