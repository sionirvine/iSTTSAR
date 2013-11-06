
package app.istts.ar;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jwetherell.augmented_reality.data.ARData;
import com.jwetherell.augmented_reality.ui.Marker;

public class MapsFragment extends Fragment {

    private static MapsFragment instance;

    public static MapsFragment getInstance() {
        if (instance == null)
            instance = new MapsFragment();
        return instance;
    }

    private GoogleMap maps;
    private LinearLayout mLayout;
    private static View view;

    private Boolean stopTake = false;
    private com.google.android.gms.maps.model.Marker userLocation;
    private getIndoorLocation mCallback;

    public interface getIndoorLocation {
        public String getIndoorLabel();

        public String getLocationStatus();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.maps_fragment, container, false);
        } catch (InflateException e) {
            /* map is already there, just return view as it is */
        }

        maps = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        maps.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-7.291322, 112.758876), 19f));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // tampilkan marker
        maps.clear();
        for (Marker m : ARData.getMarkers()) {
            Double latitude = m.getPhysicalLocation().getLatitude();
            Double longitude = m.getPhysicalLocation().getLongitude();
            String title = m.getName();
            String namagedung = title.substring(0, 1).toLowerCase();

            MarkerOptions marker = new MarkerOptions();
            marker.position(new LatLng(latitude, longitude));
            marker.title(title);

            if (namagedung.equals("n")) {
                // cokelat
                marker.icon(BitmapDescriptorFactory.defaultMarker(30));
            } else if (namagedung.equals("b")) {
                // merah
                marker.icon(BitmapDescriptorFactory.defaultMarker(0));
            } else if (namagedung.equals("l")) {
                // biru
                marker.icon(BitmapDescriptorFactory.defaultMarker(210));
            } else if (namagedung.equals("u")) {
                // hijau muda
                marker.icon(BitmapDescriptorFactory.defaultMarker(70));
            } else if (namagedung.equals("e")) {
                // kuning
                marker.icon(BitmapDescriptorFactory.defaultMarker(60));
            }

            maps.addMarker(marker);
        }

        // tambah marker posisi user
        MarkerOptions mo = new MarkerOptions();
        mo.position(new LatLng(-7.30619, 112.76189));
        mo.title("my location");
        mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.current_loc));
        userLocation = maps.addMarker(mo);


        stopTake = false;
        // tampilkan posisi user dalam map
        final Handler handler = new Handler();
        Runnable showUserPos = new Runnable() {
            @Override
            public void run() {
                if (stopTake) {
                    handler.removeCallbacks(this);
                } else {
                    // ketika outdoor
                    if (mCallback.getLocationStatus().equals("outdoor")) {
                        Double lat = ARData.getCurrentLocation().getLatitude();
                        Double lng = ARData.getCurrentLocation().getLongitude();

                        userLocation.setPosition(new LatLng(lat, lng));

                        // ketika indoor
                    } else {
                        // tampilkan posisi user dalam peta dengan membaca hasil
                        // indoor positioning
                        if (!mCallback.getIndoorLabel().trim().equals("")) {

                            PostToWS postURL = new PostToWS() {

                                @Override
                                public Void preExecute() {
                                    return null;
                                }

                                @Override
                                public String postResult(String result) {

                                    if (!result.trim().equals("false") || result.length() > 1) {
                                        String[] split = result.split(",");
                                        Double lat = Double.parseDouble(split[0]);
                                        Double lng = Double.parseDouble(split[1]);
                                        userLocation.setPosition(new LatLng(lat, lng));
                                    }
                                    return result;
                                }
                            };

                            postURL.addData("name", mCallback.getIndoorLabel().trim());

                            postURL.execute(new String[] {
                                    "http://lach.hopto.org:8080/isttsar.ws/marker/getlatlng"
                            });

                        }
                    }

                    handler.postDelayed(this, 2000);
                }
            }
        };
        handler.post(showUserPos);
    }

    @Override
    public void onPause() {
        super.onPause();

        stopTake = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mLayout != null) {
            ViewGroup parentViewGroup = (ViewGroup) mLayout.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
                mLayout = null;
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (getIndoorLocation) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement getIndoorLocation");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    };
}
