package app.istts.ar;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CommentsRatingDialogFragment extends DialogFragment {

    private TextView lblLocationName;
    private Button btnAddComments;

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
        btnAddComments = (Button) mLayout.findViewById(R.id.btnAddComments);
        btnAddComments.setOnClickListener(btnAddCommentsListener);

        return mLayout;
    }

    View.OnClickListener btnAddCommentsListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            PostToWS postURL = new PostToWS() {

                @Override
                public String postResult(String result) {
                    Log.d("test", result);
                    return null;
                }

                @Override
                public Void preExecute() {
                    return null;
                }
            };

            postURL.addData("username", "a");
            postURL.addData("password", "b");

            postURL.execute(new String[] {
                    "http://lach.hopto.org:8080/isttsar.ws/login"
            });
        }
    };

}
