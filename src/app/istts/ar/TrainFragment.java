
package app.istts.ar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class TrainFragment extends Fragment {
    private final String TAG = "iSTTSAR::MatchImageFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        LinearLayout mLayout = (LinearLayout) inflater.inflate(
                R.layout.train_fragment, container, false);

        ImageButton btnSnap = (ImageButton) mLayout
                .findViewById(R.id.btnSnap);
        btnSnap.setOnClickListener(btnSnapListener);

        return mLayout;
    }

    /** Button Listeners **/
    View.OnClickListener btnSnapListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            
            // take picture
            
            
            
            FragmentManager fm = getFragmentManager();

            UploadDialogFragment uploadDialog = new UploadDialogFragment();
            uploadDialog.setRetainInstance(true);
            uploadDialog.show(fm, "Upload");
        }

    };

    private class postURL extends AsyncTask<String, String, String> {

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
                output = readStream(con.getInputStream());

            } catch (MalformedURLException err) {
                Log.d(TAG, err.getMessage().toString());
            } catch (IOException err) {
                Log.d(TAG, err.getMessage().toString());
            }

            return output;
        }

        private String readStream(InputStream in) {
            BufferedReader reader = null;
            String hasil = "";

            try {
                reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null)
                    hasil += line;

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return hasil;
            //
        }

        /*
         * private String getOutputFromUrl(URL url) { String hasil = ""; try {
         * final HttpURLConnection con = (HttpURLConnection)
         * url.openConnection(); } catch(IOException err) {
         * Toast.makeText(getActivity().getApplicationContext(),
         * err.getMessage().toString(), Toast.LENGTH_SHORT).show(); } return
         * hasil; }
         */

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
}
