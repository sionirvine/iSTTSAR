package app.istts.ar;

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
import com.google.android.gms.maps.model.LatLng;

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
}
