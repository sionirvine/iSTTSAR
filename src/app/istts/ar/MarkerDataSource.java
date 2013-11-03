package app.istts.ar;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.jwetherell.augmented_reality.data.DataSource;
import com.jwetherell.augmented_reality.ui.IconMarker;
import com.jwetherell.augmented_reality.ui.Marker;

import java.util.ArrayList;
import java.util.List;

public class MarkerDataSource extends DataSource {

    private List<Marker> cachedMarkers = new ArrayList<Marker>();
    private static Bitmap N_icon;
    private static Bitmap B_icon;
    private static Bitmap L_icon;
    private static Bitmap U_icon;
    private static Bitmap E_icon;

    public MarkerDataSource(Resources res) {
        if (res == null)
            throw new NullPointerException();

        createIcon(res);
    }

    protected void createIcon(Resources res) {
        if (res == null)
            throw new NullPointerException();
        
        N_icon = BitmapFactory.decodeResource(res, R.drawable.nicon);
        B_icon = BitmapFactory.decodeResource(res, R.drawable.bicon);
        L_icon = BitmapFactory.decodeResource(res, R.drawable.licon);
        U_icon = BitmapFactory.decodeResource(res, R.drawable.uicon);
        E_icon = BitmapFactory.decodeResource(res, R.drawable.eicon);
    }

    public List<Marker> getMarkers() {
        // Marker atl = new IconMarker("ATL ICON", 39.931228, -75.051262, 0,
        // Color.DKGRAY, icon);
        // cachedMarkers.add(atl);

        Marker N = new IconMarker("Gedung N", -7.291322, 112.758876, 15, Color.DKGRAY, N_icon);
        cachedMarkers.add(N);

        Marker B = new IconMarker("Gedung B", -7.291028, 112.758879, 15, Color.DKGRAY, B_icon);
        cachedMarkers.add(B);

        Marker L = new IconMarker("Gedung L", -7.291489, 112.759185, 15, Color.DKGRAY, L_icon);
        cachedMarkers.add(L);

        Marker U = new IconMarker("Gedung U", -7.291247, 112.758561, 15, Color.DKGRAY, U_icon);
        cachedMarkers.add(U);

        Marker E = new IconMarker("Gedung E", -7.291294, 112.758404, 15, Color.DKGRAY, E_icon);
        cachedMarkers.add(E);

        // Marker home = new Marker("ATL CIRCLE", 39.931269, -75.051231, 0,
        // Color.YELLOW);
        // cachedMarkers.add(home);

        /*
         * Marker lon = new IconMarker(
         * "I am a really really long string which should wrap a number of times on the screen."
         * , 39.95335, -74.9223445, 0, Color.MAGENTA, icon);
         * cachedMarkers.add(lon); Marker lon2 = new IconMarker(
         * "2: I am a really really long string which should wrap a number of times on the screen."
         * , 39.95334, -74.9223446, 0, Color.MAGENTA, icon);
         * cachedMarkers.add(lon2);
         */

        /*
         * float max = 10; for (float i=0; i<max; i++) { Marker marker = null;
         * float decimal = i/max; if (i%2==0) marker = new Marker("Test-"+i,
         * 39.99, -75.33+decimal, 0, Color.LTGRAY); marker = new
         * IconMarker("Test-"+i, 39.99+decimal, -75.33, 0, Color.LTGRAY, icon);
         * cachedMarkers.add(marker); }
         */

        return cachedMarkers;
    }

}
