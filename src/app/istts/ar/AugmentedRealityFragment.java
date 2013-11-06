package app.istts.ar;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import com.jwetherell.augmented_reality.data.ARData;
import com.jwetherell.augmented_reality.ui.Marker;

import java.text.DecimalFormat;

/* AUGMENTEDREALITYFRAGMENT
 * buat augmented reality fragment untuk mengatur objek-objek POI yang ada.
 */
public class AugmentedRealityFragment extends SensorsFragment implements OnTouchListener {

    private static AugmentedRealityFragment instance;

    public static AugmentedRealityFragment getInstance() {
        if (instance == null)
            instance = new AugmentedRealityFragment();
        return instance;
    }

    private AugmentedRealityView augmentedRealityView;

    // private static final String locale = Locale.getDefault().getLanguage();
    // private static final BlockingQueue<Runnable> queue = new
    // ArrayBlockingQueue<Runnable>(1);
    // private static final ThreadPoolExecutor exeService = new
    // ThreadPoolExecutor(1, 1, 20,
    // TimeUnit.SECONDS, queue);
    // private static final Map<String, NetworkDataSource> sources = new
    // ConcurrentHashMap<String, NetworkDataSource>();
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        augmentedRealityView = new AugmentedRealityView(getActivity());

        ARData.setRadius(5f);
        ARData.setZoomLevel(FORMAT.format(5f));
        // Local
        MarkerDataSource localData = new MarkerDataSource(this.getResources());
        // ARData.addMarkers(localData.getMarkers());
        localData.getMarkers();

        // Network
        // NetworkDataSource twitter = new
        // TwitterDataSource(this.getResources());
        // sources.put("twitter", twitter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        augmentedRealityView.setOnTouchListener(this);
        return augmentedRealityView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (augmentedRealityView != null) {
            ViewGroup parentViewGroup = (ViewGroup) augmentedRealityView.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent evt) {
        super.onSensorChanged(evt);

        if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER
                || evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            augmentedRealityView.postInvalidate();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);

        // updateData(location.getLatitude(), location.getLongitude(),
        // location.getAltitude());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        for (Marker marker : ARData.getMarkers()) {
            if (marker.handleClick(event.getX(), event.getY())) {
                if (event.getAction() == MotionEvent.ACTION_UP)
                    markerTouched(marker);
                return true;
            }
        }

        return getActivity().onTouchEvent(event);
    }

    protected void markerTouched(Marker marker) {
        String name = marker.getName();

        FragmentManager fm = getFragmentManager();
        DialogFragment crDialog = CommentsRatingDialogFragment.setDialog(name);
        crDialog.setRetainInstance(true);
        crDialog.show(fm, "Comments");
    }

    @Override
    public void onStart() {
        super.onStart();

        // Location last = ARData.getCurrentLocation();
        // updateData(last.getLatitude(), last.getLongitude(),
        // last.getAltitude());
    }

    // private void updateData(final double lat, final double lon, final double
    // alt) {
    // try {
    // exeService.execute(new Runnable() {
    //
    // @Override
    // public void run() {
    // for (NetworkDataSource source : sources.values())
    // download(source, lat, lon, alt);
    // }
    // });
    // } catch (RejectedExecutionException rej) {
    // Log.w(TAG, "Not running new download Runnable, queue is full.");
    // } catch (Exception e) {
    // Log.e(TAG, "Exception running download Runnable.", e);
    // }
    // }

    // private static boolean download(NetworkDataSource source, double lat,
    // double lon, double alt) {
    // if (source == null)
    // return false;
    //
    // String url = null;
    // try {
    // url = source.createRequestURL(lat, lon, alt, ARData.getRadius(), locale);
    // } catch (NullPointerException e) {
    // return false;
    // }
    //
    // List<Marker> markers = null;
    // try {
    // markers = source.parse(url);
    // } catch (NullPointerException e) {
    // return false;
    // }
    //
    // ARData.addMarkers(markers);
    // return true;
    // }

}

