
package app.istts.ar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public abstract class PostToWS extends AsyncTask<String, String, String> {

    private final String TAG = "iSTTSAR::PostToWS";

    private Map<String, String> formData;
    private String crlf = "\r\n";
    private String twoHyphens = "--";
    private String boundary = "*****";

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        preExecute();
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

            DataOutputStream request = new
                    DataOutputStream(con.getOutputStream());

            // ADD PARAMETERS
            for (Map.Entry<String, String> entry : formData.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (key.equals("file")) {
                    request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
                    request.writeBytes("Content-Disposition: form-data; ");
                    request.writeBytes("name=\"file\"; ");
                    request.writeBytes("filename=\"img.jpg\"; " + this.crlf);
                    request.writeBytes("Content-Type: application/octet-stream" + this.crlf);
                    request.writeBytes("Content-Transfer-Encoding: binary" + this.crlf
                            + this.crlf);

                    Bitmap bmp = BitmapFactory.decodeFile(value);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    try {
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] pixels = stream.toByteArray();
                        request.write(pixels);
                    } catch (RuntimeException err) {
                        // do nothing
                    }



                    /*****************/
                    // I want to send only 8 bit black & white bitmaps
                    // byte[] pixels = new byte[bitmap.getWidth() *
                    // bitmap.getHeight()];
                    // for (int i = 0; i < bitmap.getWidth(); ++i) {
                    // for (int j = 0; j < bitmap.getHeight(); ++j) {
                    // // we're interested only in the MSB of the first
                    // byte,
                    // // since the other 3 bytes are identical for B&W
                    // images
                    // pixels[i + j] = (byte) ((bitmap.getPixel(i, j) &
                    // 0x80) >> 7);
                    // }
                    // }
                    /*****************/

                    request.writeBytes(this.crlf);
                } else {
                    request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
                    request.writeBytes("Content-Disposition: form-data; ");
                    request.writeBytes(String.format("name=\"%s\"" + this.crlf, key));
                    request.writeBytes(this.crlf + value + this.crlf);
                }
            }

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

    public abstract String postResult(String result);

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        postResult(result);
    }

    public abstract Void preExecute();

    public void addData(final String key, final String value) {
        if (formData == null)
            formData = new HashMap<String, String>();

        formData.put(key, value);
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
