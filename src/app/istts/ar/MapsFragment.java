package app.istts.ar;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
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
    LinearLayout mLayout;
    private static View view;

    private getIndoorLocation mCallback;

    public interface getIndoorLocation {
        public String getIndoorLabel();
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

        for (Marker m : ARData.getMarkers()) {
            Double latitude = m.getPhysicalLocation().getLatitude();
            Double longitude = m.getPhysicalLocation().getLongitude();
            String title = m.getName();
            String namagedung = title.substring(title.length() - 1).toLowerCase();

            MarkerOptions marker = new MarkerOptions();
            marker.position(new LatLng(latitude, longitude));
            marker.title(title);

            if (namagedung.equals("n")) {
                marker.icon(BitmapDescriptorFactory.defaultMarker(30));
            } else if (namagedung.equals("b")) {
                marker.icon(BitmapDescriptorFactory.defaultMarker(0));
            } else if (namagedung.equals("l")) {
                marker.icon(BitmapDescriptorFactory.defaultMarker(210));
            } else if (namagedung.equals("u")) {
                marker.icon(BitmapDescriptorFactory.defaultMarker(70));
            } else if (namagedung.equals("e")) {
                marker.icon(BitmapDescriptorFactory.defaultMarker(60));
            }

            maps.addMarker(marker);
        }

        if (!mCallback.getIndoorLabel().equals("")) {
            if (mCallback.getIndoorLabel().equals("B301")) {
                MarkerOptions marker = new MarkerOptions();
                marker.position(new LatLng(-7.291153, 112.758765));
                marker.title("my location");
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.current_loc));

                maps.addMarker(marker);
                
                Location location = new Location("B301");
                location.setLatitude(-7.291153);
                location.setLongitude(112.758765);
                ARData.setCurrentLocation(location);
            }
        }


        return view;
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
