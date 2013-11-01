package app.istts.ar;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CommentsRatingDialogFragment extends DialogFragment {

    private TextView lblLocationName;

    static CommentsRatingDialogFragment setDialogTitle(String name) {
        CommentsRatingDialogFragment f = new CommentsRatingDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("title", name);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        LinearLayout mLayout = (LinearLayout) inflater.inflate(R.layout.comments_dialogfragment,
                container);

        lblLocationName = (TextView) mLayout.findViewById(R.id.lblLocationName);
        lblLocationName.setText(getArguments().getString("title"));

        return mLayout;
    }

}
