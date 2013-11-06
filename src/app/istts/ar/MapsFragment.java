
package app.istts.ar;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jwetherell.augmented_reality.data.ARData;
import com.jwetherell.augmented_reality.ui.Marker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

    private Boolean addMarker = false;
    private Boolean finishMarker = false;
    private com.google.android.gms.maps.model.Marker newMarker;

    private Boolean stopTake = false;
    private com.google.android.gms.maps.model.Marker userLocation;
    private getIndoorLocation mCallback;

    public interface getIndoorLocation {
        public String getIndoorLabel();

        public String getLocationStatus();
    }

    private Spinner spinMapLantai;
    private Button btnAddMarker;

    /** indoor map image overlay **/
    Map<String, LatLngBounds> indoorMapBounds;
    Map<String, Integer> indoorMapDrawable;

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
        maps.setOnMapClickListener(new MapClickListener());

        spinMapLantai = (Spinner) view.findViewById(R.id.spinMapLantai);
        ArrayAdapter<CharSequence> adapLantai = ArrayAdapter.createFromResource(getActivity(),
                R.array.lantai_array, android.R.layout.simple_spinner_item);
        adapLantai.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinMapLantai.setAdapter(adapLantai);

        spinMapLantai.setOnItemSelectedListener(new SpinnerMapLantaiClickListener());

        btnAddMarker = (Button) view.findViewById(R.id.btnAddMarker);

        SharedPreferences settings = getActivity().getSharedPreferences("app.istts.ar", 0);
        String user = settings.getString("loggeduser", "");
        if (user.toLowerCase().equals("admin")) {
            btnAddMarker.setVisibility(View.VISIBLE);

        } else {
            btnAddMarker.setVisibility(View.GONE);
        }

        btnAddMarker.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (addMarker == false && finishMarker == false) {
                    Toast.makeText(getActivity(), "Click on map to add new marker",
                            Toast.LENGTH_LONG).show();
                    addMarker = true;
                }

                if (addMarker == false && finishMarker == true) {
                    Double lat = newMarker.getPosition().latitude;
                    Double lng = newMarker.getPosition().longitude;

                    newMarker.remove();

                    FragmentManager fm = getFragmentManager();

                    DialogFragment nmDialog = NewMarkerDialogFragment.setDialog(lat, lng);
                    nmDialog.setRetainInstance(true);
                    nmDialog.show(fm, "New Marker");

                    btnAddMarker.setText("Add Marker");
                    finishMarker = false;
                }

            }
        });

        return view;
    }

    private void initFloorPlan() {
        if (indoorMapBounds == null) {
            indoorMapBounds = new HashMap<String, LatLngBounds>();

            /** SW, NE **/
            indoorMapBounds.put("n", new LatLngBounds(
                    new LatLng(-7.291584, 112.758652),
                    new LatLng(-7.291227, 112.758997)));
            indoorMapBounds.put("b", new LatLngBounds(
                    new LatLng(-7.291222, 112.758699),
                    new LatLng(-7.290937, 112.759007)));
            indoorMapBounds.put("l", new LatLngBounds(
                    new LatLng(-7.291679, 112.759061),
                    new LatLng(-7.291343, 112.759283)));
            indoorMapBounds.put("u", new LatLngBounds(
                    new LatLng(-7.291390, 112.758451),
                    new LatLng(-7.291179, 112.758647)));
            indoorMapBounds.put("e", new LatLngBounds(
                    new LatLng(-7.291388, 112.758212),
                    new LatLng(-7.291261, 112.758543)));
        }

        if (indoorMapDrawable == null) {
            indoorMapDrawable = new HashMap<String, Integer>();
            indoorMapDrawable.put("n1", R.drawable.n1);
            indoorMapDrawable.put("n2", R.drawable.n2);

            indoorMapDrawable.put("b1", R.drawable.b1);
            indoorMapDrawable.put("b2", R.drawable.b2);
            indoorMapDrawable.put("b3", R.drawable.b3);
            indoorMapDrawable.put("b4", R.drawable.b4);
            indoorMapDrawable.put("b5", R.drawable.b5);

            indoorMapDrawable.put("l1", R.drawable.l1);
            indoorMapDrawable.put("l2", R.drawable.l2);
            indoorMapDrawable.put("l3", R.drawable.l3);
            indoorMapDrawable.put("l4", R.drawable.l4);
            indoorMapDrawable.put("l5", R.drawable.l5);

            indoorMapDrawable.put("u1", R.drawable.u1);
            indoorMapDrawable.put("u2", R.drawable.u2);
            indoorMapDrawable.put("u3", R.drawable.u3);
            indoorMapDrawable.put("u4", R.drawable.u4);

            indoorMapDrawable.put("e1", R.drawable.e1);
            indoorMapDrawable.put("e2", R.drawable.e2);
            indoorMapDrawable.put("e3", R.drawable.e3);
            indoorMapDrawable.put("e4", R.drawable.e4);
            indoorMapDrawable.put("e5", R.drawable.e5);
            indoorMapDrawable.put("e6", R.drawable.e6);
            indoorMapDrawable.put("e7", R.drawable.e7);
        }

    }

    private void setFloorPlan(int floor) {
        Iterator<Map.Entry<String, LatLngBounds>> itr = indoorMapBounds.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, LatLngBounds> pairs = itr.next();

            BitmapDescriptor bd;
            GroundOverlayOptions goo;

            if (indoorMapDrawable.containsKey(pairs.getKey() + String.valueOf(floor))) {
                bd = BitmapDescriptorFactory.fromResource(indoorMapDrawable.get(pairs.getKey()
                        + String.valueOf(floor)));
                goo = new GroundOverlayOptions();
                goo.positionFromBounds(pairs.getValue());

                if (pairs.getKey().equals("b") || pairs.getKey().equals("l")) {
                    goo.bearing((float) 90);
                } else if (pairs.getKey().equals("e")) {
                    goo.bearing((float) -90);
                }

                goo.image(bd);
                maps.addGroundOverlay(goo);
            }

        }

    }

    private void refreshMarkers() {
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

    }

    private class MapClickListener implements GoogleMap.OnMapClickListener {

        @Override
        public void onMapClick(LatLng point) {
            if (addMarker) {
                MarkerOptions mo = new MarkerOptions();
                mo.position(new LatLng(point.latitude, point.longitude));
                mo.title("new marker");
                mo.draggable(true);
                mo.icon(BitmapDescriptorFactory.defaultMarker());
                newMarker = maps.addMarker(mo);
                addMarker = false;
                finishMarker = true;

                Toast.makeText(getActivity(),
                        "drag marker to reposition.\nfinish using finish button", Toast.LENGTH_LONG)
                        .show();
                btnAddMarker.setText("Finish Marker");
            }
        }

    }

    private class SpinnerMapLantaiClickListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Resources res = getResources();
            String[] lantai = res.getStringArray(R.array.lantai_array);

            refreshMarkers();
            if (lantai[arg2].equals("none")) {
                setFloorPlan(1);
            } else {
                setFloorPlan(Integer.parseInt(lantai[arg2]));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // do nothing
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshMarkers();
        // tampilkan ground overlay (floor plan)
        initFloorPlan();
        setFloorPlan(1);

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

                                    if (!result.trim().equals("false") && result.length() > 1) {
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
