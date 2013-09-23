
package app.istts.ar;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class CameraFragment extends Fragment {

    private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        RelativeLayout mLayout = (RelativeLayout) inflater.inflate(
                R.layout.activity_camera_fragment, container, false);

        if (checkCameraHardware(getActivity().getApplicationContext())) {
            // Create an instance of Camera
            mCamera = getCameraInstance();

            // Create our Preview view and set it as the content of our
            // activity.
            mPreview = new CameraPreview(getActivity().getApplicationContext(), mCamera);
            FrameLayout preview = (FrameLayout) mLayout.findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }

        return mLayout;
    }

    @Override
    public void onPause() {
        super.onPause();

        releaseCamera(); // release the camera immediately on pause event
    }

    /** CAMERA **/

    /** Check if device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            // Camera is not available
        }
        return c;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release(); // release the camera for other applications
            mCamera = null;
        }
    }

}
