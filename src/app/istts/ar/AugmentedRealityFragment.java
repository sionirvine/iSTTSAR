package app.istts.ar;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import com.jwetherell.augmented_reality.data.ARData;
import com.jwetherell.augmented_reality.data.NetworkDataSource;
import com.jwetherell.augmented_reality.ui.Marker;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AugmentedRealityFragment extends SensorsFragment implements OnTouchListener {

    private static AugmentedRealityFragment instance;

    public static AugmentedRealityFragment getInstance() {
        if (instance == null)
            instance = new AugmentedRealityFragment();
        return instance;
    }

    private AugmentedRealityView augmentedRealityView;

    private static final String locale = Locale.getDefault().getLanguage();
    private static final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(1);
    private static final ThreadPoolExecutor exeService = new ThreadPoolExecutor(1, 1, 20,
            TimeUnit.SECONDS, queue);
    private static final Map<String, NetworkDataSource> sources = new ConcurrentHashMap<String, NetworkDataSource>();
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        augmentedRealityView = new AugmentedRealityView(getActivity());

        ARData.setRadius(5f);
        ARData.setZoomLevel(FORMAT.format(5f));
        // Local
        MarkerDataSource localData = new MarkerDataSource(this.getResources());
        ARData.addMarkers(localData.getMarkers());

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
        Log.d(TAG, "touched");
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
        String namagedung = name.substring(name.length() - 1).toLowerCase();
        String desc = "";

        if (namagedung.equals("n")) {
            desc = "Gedung N mempunyai ruang kuliah, ruang BAAK, ruang BAU, ruang PMB, dan Auditorium iSTTS yang berfungsi untuk acara-acara besar di iSTTS.";
        } else if (namagedung.equals("b")) {
            desc = "Gedung yang terletak di sisi belakang STTS ini memiliki beberapa ruang kelas yang juga dipergunakan untuk proses belajar mengajar. Tak hanya itu, gedung ini juga dilengkapi dengan perpustakaan, lapangan basket, studio DKV, kantin, dan beberapa ruang lainnya. Gedung ini kerap kali dimanfaatkan oleh mahasiswa untuk beristirahat, melepas penat sembari bermain ping pong.";
        } else if (namagedung.equals("l")) {
            desc = "Gedung Laboratorium yang telah berdiri sejak 80an ini telah mengalami banyak pembenahan dan renovasi disana-sininya. Hingga kini, gedung yang terdiri atas 5 lantai ini merupakan tempat praktikum dan training dari berbagai program studi yang ada di STTS. Dilengkapi dengan jaringan WiFi, gedung ini sering dijadikan sebagai salah satu pilihan favorit mahasiswa untuk browsing dan mengerjakan tugas bersama.";
        } else if (namagedung.equals("u")) {
            desc = "Gedung ini merupakan pusat kegiatan administrasi di STTS. Biro Administrasi Akademik, Biro Administrasi Keuangan dan ruang dosen mendominasi ruang-ruang dalam gedung ini. Namun terkadang, beberapa kuliah diselenggarakan di gedung ini, termasuk U-401, ruangan terbesar dan tertinggi di gedung ini.";
        } else if (namagedung.equals("e")) {
            desc = "Gedung paling barat di STTS ini merupakan gedung baru yang telah diresmikan 8 November 2012 lalu. Dinamai Tower of The Eagles, gedung 7 lantai ini dilengkapi dengan berbagai fasilitas seperti e-Library, ruang kelas, ruang pusat studi, ruang Himpunan Mahasiswa (HIMA) setiap program studi, dan ruang Badan Eksekutif Mahasiswa (BEM).";
        }

        FragmentManager fm = getFragmentManager();

        DialogFragment crDialog = CommentsRatingDialogFragment.setDialog(marker.getName(), desc);
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

