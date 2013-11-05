package app.istts.ar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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

import java.io.File;

/* UPLOAD DIALOGFRAGMENT
 * handle naming image and uploading process to server.
 * show OK if upload complete
 */

public class UploadDialogFragment extends DialogFragment {

    private static UploadDialogFragment instance;

    public static UploadDialogFragment getInstance() {
        if (instance == null)
            instance = new UploadDialogFragment();
        return instance;
    }

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
                    final String fileName = txtName.getText().toString();
                    
                    PostToWS postURL = new PostToWS() {

                        @Override
                        public Void preExecute() {

                            lblStatus.setText("uploading " + fileName);
                            imgPreview.setVisibility(View.GONE);
                            txtName.setVisibility(View.GONE);
                            btnUpload.setVisibility(View.GONE);

                            prgLoading.setVisibility(View.VISIBLE);
                            lblStatus.setVisibility(View.VISIBLE);

                            return null;
                        }

                        @Override
                        public String postResult(String result) {

                            prgLoading.setVisibility(View.GONE);
                            lblStatus.setText(result);
                            btnUpload.setVisibility(View.VISIBLE);
                            btnUpload.setText("OK");

                            return result;
                        }
                    };
                    
                    postURL.addData("desc", fileName);
                    postURL.addData("file", getActivity().getExternalCacheDir().getAbsolutePath()
                            + "/temp.jpg");
                    
                    postURL.execute(new String[] {
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
