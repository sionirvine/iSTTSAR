
package app.istts.ar;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import com.jwetherell.augmented_reality.data.ARData;
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

        // Marker N = new IconMarker("Gedung N", -7.291322, 112.758876, 10,
        // Color.rgb(139, 69, 19),
        // N_icon);
        // cachedMarkers.add(N);
        //
        // Marker B = new IconMarker("Gedung B", -7.291028, 112.758879, 10,
        // Color.RED, B_icon);
        // cachedMarkers.add(B);
        //
        // Marker L = new IconMarker("Gedung L", -7.291489, 112.759185, 10,
        // Color.BLUE, L_icon);
        // cachedMarkers.add(L);
        //
        // Marker U = new IconMarker("Gedung U", -7.291247, 112.758561, 10,
        // Color.WHITE, U_icon);
        // cachedMarkers.add(U);
        //
        // Marker E = new IconMarker("Gedung E", -7.291294, 112.758404, 10,
        // Color.YELLOW, E_icon);
        // cachedMarkers.add(E);

        PostToWS postURL = new PostToWS() {

            @Override
            public Void preExecute() {

                return null;
            }

            @Override
            public String postResult(String result) {
                Log.d("TAG", "result" + result);

                try {
                    if (result.length() > 1 && !result.trim().equals("false")) {

                        for (String split : result.split(String.valueOf((char) 020))) {
                            String[] nameLatlngLantaiGedung = split.split(String
                                    .valueOf((char) 007));
                            if (nameLatlngLantaiGedung.length > 1) {
                                String name = nameLatlngLantaiGedung[0];
                                String[] latLng = nameLatlngLantaiGedung[1].split(",");
                                double latitude = Double.parseDouble(latLng[0]);
                                double longitude = Double.parseDouble(latLng[1]);
                                // int lantai =
                                // Integer.parseInt(nameLatlngLantaiGedung[2]);
                                String gedung = nameLatlngLantaiGedung[3];
                                int color = 0;
                                Bitmap icon = null;

                                if (gedung.toLowerCase().equals("n")) {
                                    color = Color.rgb(139, 69, 19);
                                    icon = N_icon;
                                } else if (gedung.toLowerCase().equals("b")) {
                                    color = Color.RED;
                                    icon = B_icon;
                                } else if (gedung.toLowerCase().equals("l")) {
                                    color = Color.BLUE;
                                    icon = L_icon;
                                } else if (gedung.toLowerCase().equals("u")) {
                                    color = Color.WHITE;
                                    icon = U_icon;
                                } else if (gedung.toLowerCase().equals("e")) {
                                    color = Color.YELLOW;
                                    icon = E_icon;
                                }

                                Marker marker = new IconMarker(name, latitude, longitude,
                                        0,
                                        color,
                                        icon);

                                cachedMarkers.add(marker);
                                Log.d("TAG", "added marker: " + name);
                            }

                        }

                    }

                } catch (Exception err) {
                    err.printStackTrace();
                }

                ARData.addMarkers(cachedMarkers);
                return result;
            }
        };

        postURL.addData("name", "false");
        postURL.addData("lantai", "-1");
        postURL.addData("gedung", "false");

        postURL.execute(new String[] {
                "http://lach.hopto.org:8080/isttsar.ws/marker/get"
        });

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

    public List<Marker> getMarkersFilter(String name, String lantai, String gedung) {

        PostToWS postURL = new PostToWS() {

            @Override
            public Void preExecute() {

                return null;
            }

            @Override
            public String postResult(String result) {
                Log.d("TAG", "result" + result);

                try {
                    if (result.length() > 1 && !result.trim().equals("false")) {

                        for (String split : result.split(String.valueOf((char) 020))) {
                            String[] nameLatlngLantaiGedung = split.split(String
                                    .valueOf((char) 007));
                            if (nameLatlngLantaiGedung.length > 1) {
                                String name = nameLatlngLantaiGedung[0];
                                String[] latLng = nameLatlngLantaiGedung[1].split(",");
                                double latitude = Double.parseDouble(latLng[0]);
                                double longitude = Double.parseDouble(latLng[1]);
                                // int lantai =
                                // Integer.parseInt(nameLatlngLantaiGedung[2]);
                                String gedung = nameLatlngLantaiGedung[3];
                                int color = 0;
                                Bitmap icon = null;

                                if (gedung.toLowerCase().equals("n")) {
                                    color = Color.rgb(139, 69, 19);
                                    icon = N_icon;
                                } else if (gedung.toLowerCase().equals("b")) {
                                    color = Color.RED;
                                    icon = B_icon;
                                } else if (gedung.toLowerCase().equals("l")) {
                                    color = Color.BLUE;
                                    icon = L_icon;
                                } else if (gedung.toLowerCase().equals("u")) {
                                    color = Color.WHITE;
                                    icon = U_icon;
                                } else if (gedung.toLowerCase().equals("e")) {
                                    color = Color.YELLOW;
                                    icon = E_icon;
                                }

                                Marker marker = new IconMarker(name, latitude, longitude,
                                        0,
                                        color,
                                        icon);

                                cachedMarkers.add(marker);
                                Log.d("TAG", "added marker: " + name);
                            }

                        }

                    }

                } catch (Exception err) {
                    err.printStackTrace();
                }

                ARData.addMarkers(cachedMarkers);
                return result;
            }
        };

        String mName = name;
        String mLantai = lantai;
        String mGedung = gedung;
        if (name.trim().equals("none") || name.trim().equals("")) {
            mName = "false";
        }

        if (lantai.trim().equals("none") || lantai.trim().equals("")) {
            mLantai = "-1";
        }

        if (gedung.trim().equals("none") || gedung.trim().equals("")) {
            mGedung = "false";
        }

        postURL.addData("name", mName);
        postURL.addData("lantai", mLantai);
        postURL.addData("gedung", mGedung);

        postURL.execute(new String[] {
                "http://lach.hopto.org:8080/isttsar.ws/marker/get"
        });

        return cachedMarkers;
    }

}
