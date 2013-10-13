package app.istts.ar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

/* MAINACTIVITY
 * handle multiple fragments; show them on the screen
 */

public class MainActivity extends ActionBarActivity implements TrainFragment.CameraTakePicture,
        LocationFragment.CamTakePicture {
    
    private static final String TAG = "iSTTSAR::MainActivity";
    
    private Fragment cameraFragment;
    private Fragment trainFragment;
    private Fragment locationFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_main);
        
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

    public void takePicture() {
        CameraFragment cameraFragment = (CameraFragment) this.cameraFragment;

        cameraFragment.takePicture();
        cameraFragment.restartCameraPreview();
    }

    public void takePicture2() {

    }

}
