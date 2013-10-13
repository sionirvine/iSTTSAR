package app.istts.ar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/* LOCATIONFRAGMENT
 * handle user location; show single button to take picture and match the image.
 * read user location; if not outdoor = show indoor.
 */

public class LocationFragment extends Fragment {

    private final String TAG = "iSTTSAR::LocationFragment";
    private TextView lblLocationStatus;

    private CamTakePicture mCallback;

    public interface CamTakePicture {
        public void takePicture();
    }

    // TODO: implement fusedlocationprovider. much smarter than manual GPS.
    // TODO: implement multi - state location button
    // TODO: set matching state label

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout mLayout = (LinearLayout) inflater.inflate(
                R.layout.location_fragment, container, false);

        ImageButton btnLocation = (ImageButton) mLayout.findViewById(R.id.btnLocation);
        btnLocation.setOnClickListener(btnLocationListener);

        lblLocationStatus = (TextView) mLayout.findViewById(R.id.lblLocationStatus);

        return mLayout;
    }
    
    /** BUTTON LISTENER **/

    View.OnClickListener btnLocationListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String savepath = getActivity().getExternalCacheDir().getAbsolutePath() + "/temp.jpg";
            File image = new File(savepath);
            if (image.exists())
                image.delete();

            if (mCallback == null) {
                Log.d(TAG, "mCallback is null");
            } else {
                mCallback.takePicture();
            }

            if (isNetworkAvailable()) {
                new postURL().execute(new String[] {
                        "http://lach.hopto.org:8888/cgi/match_training"
                });
            } else {
                Toast.makeText(getActivity(), "Network not available. Network Problem?",
                        Toast.LENGTH_LONG).show();
            }


        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (CamTakePicture) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement CamTakePicture");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    };

    /** asynctask untuk mengatasi upload file ke server **/
    private class postURL extends AsyncTask<String, String, String> {

        String crlf = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            String output = "";

            try {
                URL myUrl = new URL(urls[0]);

                final HttpURLConnection con = (HttpURLConnection) myUrl.openConnection();
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setUseCaches(false);
                con.setRequestMethod("POST");
                con.setRequestProperty("Connection", "Keep-Alive");
                con.setRequestProperty("Cache-Control", "no-cache");
                con.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + this.boundary);
                con.connect();

                // ADD MULTIPART (IMAGE DATA) UPLOAD
                DataOutputStream request = new
                        DataOutputStream(con.getOutputStream());

                // ADD PARAMETERS
                request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
                request.writeBytes("Content-Type: application/octet-stream" + this.crlf);
                request.writeBytes("Content-Transfer-Encoding: binary" + this.crlf);
                request.writeBytes("Content-Disposition: form-data; "
                        + "name=\"file\";"
                        + "filename=\"temp.jpg\""
                        + this.crlf);
                request.writeBytes(this.crlf);

                /** ADD BITMAP **/
                String savepath =
                        getActivity().getExternalCacheDir().getAbsolutePath()
                                + "/temp.jpg";

                Bitmap bmp = BitmapFactory.decodeFile(savepath);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] pixels = stream.toByteArray();
                request.write(pixels);

                // // I want to send only 8 bit black & white bitmaps
                // byte[] pixels = new byte[bitmap.getWidth() *
                // bitmap.getHeight()];
                // for (int i = 0; i < bitmap.getWidth(); ++i) {
                // for (int j = 0; j < bitmap.getHeight(); ++j) {
                // // we're interested only in the MSB of the first byte,
                // // since the other 3 bytes are identical for B&W images
                // pixels[i + j] = (byte) ((bitmap.getPixel(i, j) & 0x80) >> 7);
                // }
                // }
                /*****************/

                request.writeBytes(this.crlf);
                request.writeBytes(this.twoHyphens + this.boundary +
                        this.twoHyphens + this.crlf);

                request.flush();
                request.close();

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    output = readStream(con.getInputStream());
                    con.disconnect();
                } else {
                    Toast.makeText(getActivity(), con.getResponseMessage(), Toast.LENGTH_LONG)
                            .show();

                    con.disconnect();
                }

            } catch (MalformedURLException err) {
                Log.d(TAG, err.getMessage().toString());
            } catch (IOException err) {
                Log.d(TAG, err.getMessage().toString());
            }

            return output;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            lblLocationStatus.setText(result);
        }

        private String readStream(InputStream in) {
            BufferedReader reader = null;
            String hasil = "";

            try {
                reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null)
                    hasil += line + "\n";

            } catch (IOException e) {
                Log.d(TAG, e.getMessage().toString());
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.d(TAG, e.getMessage().toString());
                    }
                }
            }

            try {
                in.close();
            } catch (IOException e) {
                Log.d(TAG, e.getMessage().toString());
            }

            return hasil;
            //
        }

    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected())
            return true;

        return false;
    }

}
