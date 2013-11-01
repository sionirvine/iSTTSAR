package app.istts.ar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MapsFragment extends Fragment {

    private GoogleMap maps;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout mLayout = (LinearLayout) inflater.inflate(R.layout.maps_fragment,
                container, false);

        maps = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        maps.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-7.291322, 112.758876), 15));

        return mLayout;
    }
}
