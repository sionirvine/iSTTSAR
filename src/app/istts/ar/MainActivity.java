package app.istts.ar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends ActionBarActivity {
    
    private static final String TAG = "iSTTSAR::MainActivity";
    
    private Fragment cameraFragment;
    private Fragment trainFragment;
    
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
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
        .replace(R.id.content_frame, cameraFragment)
        .commit();
        
        fragmentManager.beginTransaction()
        .replace(R.id.control_frame, trainFragment)
        .commit();
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
