package app.istts.ar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;

public class MainActivity extends ActionBarActivity {
    
    private static final String TAG = "iSTTSAR::MainActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Fragment cameraFragment = new CameraFragment();
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
        .replace(R.id.content_frame, cameraFragment)
        .commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
