package app.istts.ar;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.io.File;

/* LOCATIONFRAGMENT
 * handle train button; upload user image from camera to web server
 */

public class TrainFragment extends Fragment {
    
    private static TrainFragment instance;

    public static TrainFragment getInstance() {
        if (instance == null)
            instance = new TrainFragment();
        return instance;
    }

    private final String TAG = "iSTTSAR::MatchImageFragment";
    
    private CameraTakePicture mCallback;
    public interface CameraTakePicture {
        public void takePicture(String path);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout mLayout = (LinearLayout) inflater.inflate(
                R.layout.train_fragment, container, false);

        ImageButton btnSnap = (ImageButton) mLayout.findViewById(R.id.btnSnap);
        btnSnap.setOnClickListener(btnSnapListener);

        return mLayout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (CameraTakePicture) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement CameraTakePicture");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    };

    /** Button Listeners **/
    View.OnClickListener btnSnapListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            
            String savepath = getActivity().getExternalCacheDir().getAbsolutePath() + "/temp.jpg";
            File image = new File(savepath);
            if (image.exists())
                image.delete();

            if (mCallback == null) {
                Log.d(TAG, "mCallback is null");
            } else {
                mCallback.takePicture(savepath);
            }

            FragmentManager fm = getFragmentManager();

            UploadDialogFragment uploadDialog = new UploadDialogFragment();
            uploadDialog.setRetainInstance(true);
            uploadDialog.show(fm, "Upload");

            uploadDialog.setImage(savepath);
        }

    };

}
