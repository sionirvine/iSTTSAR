package app.istts.ar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

/* UPLOAD DIALOGFRAGMENT
 * handle naming image and uploading process to server.
 * show OK if upload complete
 */

public class UploadDialogFragment extends DialogFragment {

    private final static String TAG = "iSTTSAR::UploadDialogFragment";

    private ImageView imgPreview;
    private ProgressBar prgLoading;
    private EditText txtName;
    private Button btnUpload;
    private TextView lblStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        LinearLayout mLayout = (LinearLayout) inflater.inflate(R.layout.upload_dialogfragment,
                container);

        imgPreview = (ImageView) mLayout.findViewById(R.id.imgPreview);
        prgLoading = (ProgressBar) mLayout.findViewById(R.id.prgLoading);
        txtName = (EditText) mLayout.findViewById(R.id.txtName);
        btnUpload = (Button) mLayout.findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(btnUploadListener);
        lblStatus = (TextView) mLayout.findViewById(R.id.lblLocationStatus);

        return mLayout;
    }

    /** button listener **/
    View.OnClickListener btnUploadListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (btnUpload.getText().toString().equals("Upload")) {
                if (isNetworkAvailable()) {
                    new postURL().execute(new String[] {
                            "http://lach.hopto.org:8888/cgi/uploadimage"
                    });
                } else {
                    Toast.makeText(getActivity(), "Network not available. Network Problem?",
                            Toast.LENGTH_LONG).show();
                }
            } else {
                dismiss();
            }

        }
    };

    /** set image preview from path **/
    public void setImage(String path) {
        new SetImageTask().execute(new String[] {
            path
        });
    }

    /** asynctask untuk mengatasi imgPreview yang belum siap **/
    class SetImageTask extends AsyncTask<String, Void, String> {

        private File imgFile;

        @Override
        protected String doInBackground(String... path) {
            Boolean flag = false;
            imgFile = new File(path[0]);

            while (!flag) {
                if (imgPreview != null && imgFile.exists())
                    flag = true;

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return path[0];
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            imgPreview.setImageBitmap(myBitmap);
            imgPreview.setVisibility(View.VISIBLE);
            prgLoading.setVisibility(View.GONE);
        }
    }

    /** asynctask untuk mengatasi upload file ke server **/
    private class postURL extends AsyncTask<String, String, String> {

        String attachmentName;
        String crlf = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            this.attachmentName = txtName.getText().toString();

            lblStatus.setText("uploading " + this.attachmentName);
            imgPreview.setVisibility(View.GONE);
            txtName.setVisibility(View.GONE);
            btnUpload.setVisibility(View.GONE);

            prgLoading.setVisibility(View.VISIBLE);
            lblStatus.setVisibility(View.VISIBLE);
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
                request.writeBytes("Content-Type: text/plain" + this.crlf);
                request.writeBytes("Content-Disposition: form-data; "
                        + "name=\"desc\"" + this.crlf);
                request.writeBytes(this.crlf + this.attachmentName + this.crlf);

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

                // dapatkan output dari hasil upload (response)
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    output = readStream(con.getInputStream());
                } else {
                    output = con.getResponseMessage();
                }

                // disconnect setelah selesai melakukan proses baca response
                con.disconnect();


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

            prgLoading.setVisibility(View.GONE);
            lblStatus.setText(result);
            btnUpload.setVisibility(View.VISIBLE);
            btnUpload.setText("OK");
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
