package app.istts.ar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jwetherell.augmented_reality.data.ARData;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout mLayout = (LinearLayout) inflater.inflate(
                R.layout.location_fragment, container, false);

        Button btnMaps = (Button) mLayout.findViewById(R.id.btnMaps);
        btnMaps.setOnClickListener(btnMapsListener);

        btnLocation = (ImageButton) mLayout.findViewById(R.id.btnLocation);
        btnLocation.setOnClickListener(btnLocationListener);

        lblLocationStatus = (TextView) mLayout.findViewById(R.id.lblLocationStatus);
        lblIndoor = (TextView) mLayout.findViewById(R.id.lblIndoor);

        prgOCR = (ProgressBar) mLayout.findViewById(R.id.prgOCR);

        final Handler handler = new Handler();
        final Runnable getLocation = new Runnable() {
            @Override
            public void run() {
                Location currentLocation = ARData.getCurrentLocation();
                if (currentLocation != null) {
                    if (currentLocation.getAccuracy() < 7.1f) {
                        lblLocationStatus.setText("Outdoor");
                    } else {
                        lblLocationStatus.setText("Indoor");
                    }
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(getLocation);

        return mLayout;
    }

    /** BUTTON LISTENER **/
    View.OnClickListener btnLocationListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // toggle OCR on / off
            if (lblLocationStatus.getText().toString().equals("Indoor")) {
                // if (mOCRCallback.getOCRMode() == true) {
                // mOCRCallback.setOCRMode(false);
                // } else {
                // mOCRCallback.setOCRMode(true);
                // }
                setLocationButtonVisible(false);
                mOCRCallback.takePictureWithOCR();
            }

        }
    };

    View.OnClickListener btnMapsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mOCRCallback.swapFragment();
        }
    };

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
