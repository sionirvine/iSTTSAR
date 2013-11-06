package app.istts.ar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.jwetherell.augmented_reality.common.LowPassFilter;
import com.jwetherell.augmented_reality.common.Matrix;
import com.jwetherell.augmented_reality.data.ARData;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class extends Fragment and processes sensor data and location data. this
 * includes: GPS, Accelerometer, Magnetic Field. extended by
 * AugmentedRealityFragment
 */

public class SensorsFragment extends Fragment implements
        SensorEventListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {

    private static SensorsFragment instance;

    public static SensorsFragment getInstance() {
        if (instance == null)
            instance = new SensorsFragment();
        return instance;
    }

    private getLocation mCallback;

    public interface getLocation {
        public String getLocationStatus();

        public void setLocationStatus(String location);
        public String getIndoorLabel();
    }

    protected static final String TAG = "iSTTSAR::SensorsFragment";
    private static final AtomicBoolean computing = new AtomicBoolean(false);

    // matrix in Android format
    private static final float temp[] = new float[9]; // Temporary rotation
    private static final float rotation[] = new float[9]; // Final rotation

    private static final float grav[] = new float[3]; // Gravity (a.k.a
    // accelerometer data)
    private static final float mag[] = new float[3]; // Magnetic
    /*
     * Using Matrix operations instead. This was way too inaccurate, private
     * static final float apr[] = new float[3]; //Azimuth, pitch, roll
     */
    private static final Matrix worldCoord = new Matrix();
    private static final Matrix magneticCompensatedCoord = new Matrix();
    private static final Matrix xAxisRotation = new Matrix();
    private static final Matrix yAxisRotation = new Matrix();
    private static final Matrix mageticNorthCompensation = new Matrix();

    private static GeomagneticField gmf = null;
    private static float smooth[] = new float[3];
    private static SensorManager sensorMgr = null;
    private static List<Sensor> sensors = null;
    private static Sensor sensorGrav = null;
    private static Sensor sensorMag = null;

    // Define an object that holds accuracy and frequency parameters
    /** fused location provider via play service **/
    private LocationRequest mLocationRequest; // fusedLocPro request
    private LocationClient mLocationClient; // fusedLocPro client
    private SharedPreferences mPrefs; // shared preferences android
    private Editor mEditor; // shared preferences editor
    boolean mUpdatesRequested = true; // hold update request status for play
                                      // service

    boolean firstTimeFix = true;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** set fusedLocPro parameters **/
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // 5 seconds for normal interval
        // 1 seconds for fast interval
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);

        /** save settings on sharedpreferences **/
        mPrefs = getActivity().getSharedPreferences("SharedPreferences",
                Context.MODE_PRIVATE);
        // Get a SharedPreferences editor
        mEditor = mPrefs.edit();
        // context, connectionCallback, connectionFailedListener
        mLocationClient = new LocationClient(getActivity(), this, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        super.onStart();
        mLocationClient.connect(); // fusedLocPro

        float neg90rads = (float) Math.toRadians(-90);

        // Counter-clockwise rotation at -90 degrees around the x-axis
        // [ 1, 0, 0 ]
        // [ 0, cos, -sin ]
        // [ 0, sin, cos ]
        xAxisRotation.set(
                1f, 0f, 0f,
                0f, (float) Math.cos(neg90rads), (float) -Math.sin(neg90rads),
                0f, (float) Math.sin(neg90rads), (float) Math.cos(neg90rads));

        // Counter-clockwise rotation at -90 degrees around the y-axis
        // [ cos, 0, sin ]
        // [ 0, 1, 0 ]
        // [ -sin, 0, cos ]
        yAxisRotation.set(
                (float) Math.cos(neg90rads), 0f, (float) Math.sin(neg90rads),
                0f, 1f, 0f,
                (float) -Math.sin(neg90rads), 0f, (float) Math.cos(neg90rads));

        try {
            sensorMgr = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

            sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
            if (sensors.size() > 0)
                sensorGrav = sensors.get(0);

            sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
            if (sensors.size() > 0)
                sensorMag = sensors.get(0);

            sensorMgr.registerListener(this, sensorGrav, SensorManager.SENSOR_DELAY_NORMAL);
            sensorMgr.registerListener(this, sensorMag, SensorManager.SENSOR_DELAY_NORMAL);

            try {

                gmf = new GeomagneticField((float) ARData.getCurrentLocation().getLatitude(),
                        (float) ARData.getCurrentLocation().getLongitude(),
                        (float) ARData.getCurrentLocation().getAltitude(),
                        System.currentTimeMillis());

                float dec = (float) Math.toRadians(-gmf.getDeclination());

                synchronized (mageticNorthCompensation) {
                    // Identity matrix
                    // [ 1, 0, 0 ]
                    // [ 0, 1, 0 ]
                    // [ 0, 0, 1 ]
                    mageticNorthCompensation.toIdentity();

                    // Counter-clockwise rotation at negative declination around
                    // the y-axis
                    // note: declination of the horizontal component of the
                    // magnetic field
                    // from true north, in degrees (i.e. positive means the
                    // magnetic
                    // field is rotated east that much from true north).
                    // note2: declination is the difference between true north
                    // and magnetic north
                    // [ cos, 0, sin ]
                    // [ 0 , 1, 0 ]
                    // [ -sin, 0, cos ]
                    mageticNorthCompensation.set((float) Math.cos(dec), 0f, (float) Math.sin(dec),
                            0f, 1f, 0f,
                            (float) -Math.sin(dec), 0f, (float) Math.cos(dec));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex1) {
            try {
                if (sensorMgr != null) {
                    sensorMgr.unregisterListener(this, sensorGrav);
                    sensorMgr.unregisterListener(this, sensorMag);
                    sensorMgr = null;
                }
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        super.onStop();

        if (mLocationClient.isConnected()) {
            // removeLocationUpdates(this);
            mLocationClient.removeLocationUpdates(this);
        }

        mLocationClient.disconnect();

        try {
            try {
                sensorMgr.unregisterListener(this, sensorGrav);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                sensorMgr.unregisterListener(this, sensorMag);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            sensorMgr = null;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // fusedLocPro
        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
        mEditor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();

        // fusedLocPro
        if (mPrefs.contains("KEY_UPDATES_ON")) {
            mUpdatesRequested =
                    mPrefs.getBoolean("KEY_UPDATES_ON", false);
        } else {
            mEditor.putBoolean("KEY_UPDATES_ON", false);
            mEditor.commit();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (getLocation) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement getLocation");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSensorChanged(SensorEvent evt) {
        if (!computing.compareAndSet(false, true))
            return;

        if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            smooth = LowPassFilter.filter(0.5f, 1.0f, evt.values, grav);
            grav[0] = smooth[0];
            grav[1] = smooth[1];
            grav[2] = smooth[2];
        } else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            smooth = LowPassFilter.filter(2.0f, 4.0f, evt.values, mag);
            mag[0] = smooth[0];
            mag[1] = smooth[1];
            mag[2] = smooth[2];
        }

        // // Find real world position relative to phone location ////
        // Get rotation matrix given the gravity and geomagnetic matrices
        SensorManager.getRotationMatrix(temp, null, grav, mag);

        // Translate the rotation matrices from Y and -Z (landscape)
        // SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_Y,
        // SensorManager.AXIS_MINUS_X, rotation);
        // SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_X,
        // SensorManager.AXIS_MINUS_Z, rotation);
        SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_Z,
                rotation);

        /*
         * Using Matrix operations instead. This was way too inaccurate, //Get
         * the azimuth, pitch, roll SensorManager.getOrientation(rotation,apr);
         * float floatAzimuth = (float)Math.toDegrees(apr[0]); if
         * (floatAzimuth<0) floatAzimuth+=360; ARData.setAzimuth(floatAzimuth);
         * ARData.setPitch((float)Math.toDegrees(apr[1]));
         * ARData.setRoll((float)Math.toDegrees(apr[2]));
         */

        // Convert from float[9] to Matrix
        worldCoord.set(rotation[0], rotation[1], rotation[2], rotation[3], rotation[4],
                rotation[5], rotation[6], rotation[7], rotation[8]);

        // // Find position relative to magnetic north ////
        // Identity matrix
        // [ 1, 0, 0 ]
        // [ 0, 1, 0 ]
        // [ 0, 0, 1 ]
        magneticCompensatedCoord.toIdentity();

        synchronized (mageticNorthCompensation) {
            // Cross product the matrix with the magnetic north compensation
            magneticCompensatedCoord.prod(mageticNorthCompensation);
        }

        // The compass assumes the screen is parallel to the ground with the
        // screen pointing
        // to the sky, rotate to compensate.
        magneticCompensatedCoord.prod(xAxisRotation);

        // Cross product with the world coordinates to get a mag north
        // compensated coords
        magneticCompensatedCoord.prod(worldCoord);

        // Y axis
        magneticCompensatedCoord.prod(yAxisRotation);

        // Invert the matrix since up-down and left-right are reversed in
        // landscape mode
        magneticCompensatedCoord.invert();

        // Set the rotation matrix (used to translate all object from lat/lon to
        // x/y/z)
        ARData.setRotationMatrix(magneticCompensatedCoord);

        computing.set(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLocationChanged(Location location) {
        if (firstTimeFix) {
            ARData.setCurrentLocation(location);
            firstTimeFix = false;
        }

        Boolean indoor = true;
        if (location != null) {
            if (location.getAccuracy() < 7.1f) {
                mCallback.setLocationStatus("Outdoor");
                indoor = false;
            } else {
                mCallback.setLocationStatus("Indoor");
            }
        }

        // if outdoor
        if (indoor == false) {
            ARData.setCurrentLocation(location);

            // if indoor
        } else {
            PostToWS postURL = new PostToWS() {

                @Override
                public Void preExecute() {
                    return null;
                }

                @Override
                public String postResult(String result) {

                    if (!result.trim().equals("false") && result.length() > 1) {
                        String[] split = result.split(",");
                        Double lat = Double.parseDouble(split[0]);
                        Double lng = Double.parseDouble(split[1]);

                        Location location = new Location("myloc");
                        location.setLatitude(lat);
                        location.setLongitude(lng);
                        ARData.setCurrentLocation(location);
                    }
                    return result;
                }
            };

            postURL.addData("name", mCallback.getIndoorLabel().trim());

            postURL.execute(new String[] {
                    "http://lach.hopto.org:8080/isttsar.ws/marker/getlatlng"
            });
        }

        gmf = new GeomagneticField((float) ARData.getCurrentLocation().getLatitude(),
                (float) ARData.getCurrentLocation().getLongitude(),
                (float) ARData.getCurrentLocation().getAltitude(), System.currentTimeMillis());

        float dec = (float) Math.toRadians(-gmf.getDeclination());

        synchronized (mageticNorthCompensation) {
            mageticNorthCompensation.toIdentity();

            mageticNorthCompensation.set((float) Math.cos(dec), 0f, (float) Math.sin(dec),
                    0f, 1f, 0f,
                    (float) -Math.sin(dec), 0f, (float) Math.cos(dec));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor == null)
            throw new NullPointerException();

        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
                && accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.e(TAG, "Compass data unreliable");
        }
    }

    /** Google Play Services **/
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected");
        if (mUpdatesRequested) {
            mLocationClient.requestLocationUpdates(mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "onConnectionFailed");
        Toast.makeText(getActivity(), "GPS Service failed",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "onDisconnected");
        Toast.makeText(getActivity(), "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }
}