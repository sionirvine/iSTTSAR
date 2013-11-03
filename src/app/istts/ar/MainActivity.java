package app.istts.ar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/* MAINACTIVITY
 * handle multiple fragments; show them on the screen
 */

public class MainActivity extends ActionBarActivity implements TrainFragment.CameraTakePicture,
        LocationFragment.CamTakePicture, LocationFragment.OCRTakePicture {
    
    private static final String TAG = "iSTTSAR::MainActivity";
    
    private Fragment cameraFragment;
    private Fragment trainFragment;
    private Fragment locationFragment;
    private Fragment augmentedRealityFragment;
    private Fragment mapsFragment;

    /** CHECK FOR GOOGLE PLAY SERVICES **/
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    @Override
    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        break;
                }
        }
    }

    private boolean servicesConnected() {
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d("Location Updates",
                    "Google Play services is available.");
            return true;
        } else {
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            if (errorDialog != null) {
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(
                        getSupportFragmentManager(),
                        "Location Updates");
            }
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // remove actionbar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        
        // check for google play services
        if (!servicesConnected())
            Toast.makeText(
                    getApplicationContext(),
                    "Google Play Services is needed to work properly",
                    Toast.LENGTH_LONG).show();

        cameraFragment = CameraFragment.getInstance();
        augmentedRealityFragment = AugmentedRealityFragment.getInstance();
        trainFragment = TrainFragment.getInstance();
        locationFragment = LocationFragment.getInstance();
        mapsFragment = MapsFragment.getInstance();

        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .add(R.id.camera_frame, cameraFragment)
                .commit();

        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, augmentedRealityFragment)
                .commit();

        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, trainFragment)
                .commit();

        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, locationFragment)
                .commit();
        
        copyTrainedData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void copyTrainedData() {
        try {
            File tessDataFolder = new File(getExternalCacheDir().getAbsolutePath()
                    + File.separator
                    + "tessdata");

            File trainedData = new File(getExternalCacheDir().getAbsolutePath()
                    + File.separator
                    + "tessdata"
                    + File.separator
                    + "ind.traineddata");

            if (!trainedData.exists()) {
                tessDataFolder.mkdir();

                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("ind.traineddata");
                OutputStream out = new FileOutputStream(trainedData.getAbsolutePath());
                byte[] buf = new byte[8024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Was unable to copy tesseract data " + e.toString());
        }
    }

    public void takePicture(String path) {
        CameraFragment cameraFragment = (CameraFragment) this.cameraFragment;
        cameraFragment.takePicture(path);
    }

    @Override
    public void setOCRMode(Boolean value) {
        CameraFragment cameraFragment = (CameraFragment) this.cameraFragment;
        cameraFragment.setOCRMode(value);
    }

    @Override
    public Boolean getOCRMode() {
        CameraFragment cameraFragment = (CameraFragment) this.cameraFragment;
        return cameraFragment.getOCRMode();
    }

    @Override
    public void swapFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, mapsFragment)
                .addToBackStack(null)
                .commit();
    }

}
