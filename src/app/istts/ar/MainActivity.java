package app.istts.ar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/* MAINACTIVITY
 * handle multiple fragments; show them on the screen
 */

public class MainActivity extends ActionBarActivity implements TrainFragment.CameraTakePicture,
        LocationFragment.CamTakePicture {
    
    private static final String TAG = "iSTTSAR::MainActivity";
    
    private Fragment cameraFragment;
    private Fragment trainFragment;
    private Fragment locationFragment;

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
        
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_main);
        
        // check for google play services
        if (!servicesConnected())
            Toast.makeText(
                    getApplicationContext(),
                    "Google Play Services is needed to work properly",
                    Toast.LENGTH_LONG).show();

        cameraFragment = new CameraFragment();
        trainFragment = new TrainFragment();
        locationFragment = new LocationFragment();
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.camera_frame, cameraFragment)
                .commit();
        
        fragmentManager.beginTransaction()
                .replace(R.id.train_frame, trainFragment)
                .commit();
        
        fragmentManager.beginTransaction()
                .replace(R.id.location_frame, locationFragment)
                .commit();
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void takePicture(String path) {
        CameraFragment cameraFragment = (CameraFragment) this.cameraFragment;

        cameraFragment.takePicture(path);
    }

}
