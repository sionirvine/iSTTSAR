package app.istts.ar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
        LocationFragment.CamTakePicture, LocationFragment.OCRTakePicture,
        CameraFragment.setLocation, MapsFragment.getIndoorLocation {
    
    private static final String TAG = "iSTTSAR::MainActivity";
    
    // inisialisasi variabel
    private Fragment cameraFragment;
    private Fragment trainFragment;
    private Fragment locationFragment;
    private Fragment augmentedRealityFragment;
    private Fragment mapsFragment;

    private PowerManager.WakeLock wakeLock;

    private String[] mDrawerMenu;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

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
        
        // hapus actionBar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        
        // cek Google play Services telah terinstall belum
        if (!servicesConnected())
            Toast.makeText(
                    getApplicationContext(),
                    "Google Play Services is needed to work properly",
                    Toast.LENGTH_LONG).show();

        // wakelock sehingga layar tidak mati setelah beberapa waktu
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK, "iSTTSAR wakelock");
        wakeLock.acquire();

        // tampilkan fragment
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
        
        // lakukan proses untuk menaruh data tesseract
        copyTrainedData();

        // application drawer
        mDrawerMenu = getResources().getStringArray(R.array.drawermenu_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.drawer_header, mDrawerList, false);
        mDrawerList.addHeaderView(header, null, false);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerMenu));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

    }

    @Override
    protected void onResume() {
        super.onResume();
        wakeLock.acquire();
    }

    @Override
    protected void onPause() {
        super.onPause();
        wakeLock.release();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /** NAVIGATION DRAWER **/
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        // Create a new fragment and specify the planet to show based on
        // position
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (position) {
            case 1: // iSTTS AR
                break;
            case 2: // Maps
                fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, mapsFragment)
                        .addToBackStack(null)
                        .commit();
                break;
            case 3: // Filter
                FragmentManager fm = getSupportFragmentManager();

                DialogFragment fDialog = new FilterDialogFragment();
                fDialog.setRetainInstance(true);
                fDialog.show(fm, "Filter");
                break;
            case 4: // Settings
                break;
        }

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    /** TESSERACT **/
    // copy data from assets to external storage (language data)
    private void copyTrainedData() {
        try {
            File tessDataFolder = new File(getExternalCacheDir().getAbsolutePath()
                    + File.separator
                    + "tessdata");

            AssetManager assetManager = getAssets();
            for (String s : assetManager.list("ind")) {
                File trainedData = new File(getExternalCacheDir().getAbsolutePath()
                        + File.separator
                        + "tessdata"
                        + File.separator
                        + s);

                if (!trainedData.exists()) {
                    tessDataFolder.mkdir();

                    InputStream in = assetManager.open("ind/" + s);
                    OutputStream out = new FileOutputStream(trainedData.getAbsolutePath());
                    byte[] buf = new byte[8024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "Was unable to copy tesseract data " + e.toString());
        }
    }

    /** Fragment Interface **/
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

    @Override
    public void setLocationStatus(String location) {
        LocationFragment locFragment = (LocationFragment) this.locationFragment;
        locFragment.setLocationStatus(location);
    }

    @Override
    public String getLocationStatus() {
        LocationFragment locFragment = (LocationFragment) this.locationFragment;
        return locFragment.getLocationStatus();
    }

    @Override
    public void takePictureWithOCR() {
        CameraFragment cameraFragment = (CameraFragment) this.cameraFragment;
        cameraFragment.takePictureOCR();
    }

    @Override
    public void setLocationButtonVisible(Boolean state) {
        LocationFragment locFragment = (LocationFragment) this.locationFragment;
        locFragment.setLocationButtonVisible(state);
    }

    @Override
    public void setIndoorLabel(String location) {
        LocationFragment locFragment = (LocationFragment) this.locationFragment;
        locFragment.setIndoorLabel(location);
    }

    @Override
    public String getIndoorLabel() {
        LocationFragment locFragment = (LocationFragment) this.locationFragment;

        return locFragment.getIndoorLabel();
    }

    @Override
    public void takePictureWithOpenCV() {
        CameraFragment cameraFragment = (CameraFragment) this.cameraFragment;
        cameraFragment.takePictureOpenCV();
    }

}
