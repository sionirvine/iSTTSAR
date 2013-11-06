package app.istts.ar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/* LOCATIONFRAGMENT
 * show UI for manipulating location
 */

public class LocationFragment extends Fragment {

    private static LocationFragment instance;

    public static LocationFragment getInstance() {
        if (instance == null)
            instance = new LocationFragment();
        return instance;
    }

    private final String TAG = "iSTTSAR::LocationFragment";
    private TextView lblLocationStatus;
    private ImageButton btnLocation;
    private ProgressBar prgOCR;
    private TextView lblIndoor;

    private CamTakePicture mCallback;
    private Bitmap cameraPicture;
    
    public interface CamTakePicture {
        public void takePicture(String path);
    }

    private OCRTakePicture mOCRCallback;

    public interface OCRTakePicture {
        public void setOCRMode(Boolean value);
        public Boolean getOCRMode();
        public void swapFragment();

        public void takePictureWithOCR();

        public void takePictureWithOpenCV();

        public void setIndoorLabel(String location);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout mLayout = (LinearLayout) inflater.inflate(
                R.layout.location_fragment, container, false);

        btnLocation = (ImageButton) mLayout.findViewById(R.id.btnLocation);
        btnLocation.setOnClickListener(new btnLocationListener());

        lblLocationStatus = (TextView) mLayout.findViewById(R.id.lblLocationStatus);
        lblIndoor = (TextView) mLayout.findViewById(R.id.lblIndoor);

        prgOCR = (ProgressBar) mLayout.findViewById(R.id.prgOCR);

        // final Handler handler = new Handler();
        // final Runnable getLocation = new Runnable() {
        // @Override
        // public void run() {
        // Location currentLocation = ARData.getCurrentLocation();
        // if (currentLocation != null) {
        // if (currentLocation.getAccuracy() < 5.1f) {
        // lblLocationStatus.setText("Outdoor");
        // } else {
        // lblLocationStatus.setText("Indoor");
        // }
        // }
        // handler.postDelayed(this, 1000);
        // }
        // };
        // handler.post(getLocation);

        return mLayout;
    }

    /** BUTTON LISTENER **/
    private class btnLocationListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // take picture with OCR
            FragmentManager fm = getFragmentManager();

            DialogFragment mDialog = new MatchDialogFragment();
            mDialog.setTargetFragment(instance, 777);
            mDialog.setRetainInstance(true);
            mDialog.show(fm, "Match");

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String item = data.getStringExtra("ITEM");

        if (requestCode == 777) {
            if (resultCode == Activity.RESULT_OK) {
                if (item.equals("OPENCV")) {
                    mOCRCallback.takePictureWithOpenCV();

                    PostToWS postURL = new PostToWS() {

                        @Override
                        public Void preExecute() {
                            setLocationButtonVisible(false);
                            return null;
                        }

                        @Override
                        public String postResult(String result) {
                            setLocationButtonVisible(true);
                            
                            // result from openCV
                            // example: "result: B-403"
                            //           0123456789012
                            if (!result.trim().equals("")) {
                                String trimResult = result.substring(8, result.length());
                                mOCRCallback.setIndoorLabel(trimResult);
                            }
                            
                            return result;
                        }
                    };

                    postURL.addData("file", getActivity().getExternalCacheDir().getAbsolutePath()
                            + "/match.jpg");

                    postURL.execute(new String[] {
                            "http://lach.hopto.org:8888/cgi/match_training"
                    });

                } else if (item.equals("TESSERACT")) {
                    setLocationButtonVisible(false);
                    mOCRCallback.takePictureWithOCR();

                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // nothing
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (CamTakePicture) activity;
            mOCRCallback = (OCRTakePicture) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement CamTakePicture");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
        mOCRCallback = null;
    };

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
    
    public String getLocationStatus() {
        return lblLocationStatus.getText().toString().toLowerCase();
    }

    public void setLocationStatus(String location) {
        lblLocationStatus.setText(location);
    }

    public void setIndoorLabel(String location) {
        lblIndoor.setText(location);
    }

    public String getIndoorLabel() {
        return lblIndoor.getText().toString();
    }

    public void setLocationButtonVisible(Boolean state) {
        if (state == false) {
            prgOCR.setVisibility(View.VISIBLE);
            btnLocation.setVisibility(View.GONE);
        } else {
            prgOCR.setVisibility(View.GONE);
            btnLocation.setVisibility(View.VISIBLE);
        }
    }

}
