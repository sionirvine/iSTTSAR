package app.istts.ar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class CommentsRatingDialogFragment extends DialogFragment {

    private TextView lblLocationName;
    private TextView lblLocationDesc;
    private TextView lblComment_Content;
    private EditText txtComment;
    private Button btnAddComments;
    private Boolean loggedIn;
    private String loggedUser;
    private RatingBar ratingBar1;

    ;;
    static CommentsRatingDialogFragment setDialog(String name, String desc) {
        CommentsRatingDialogFragment f = new CommentsRatingDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("title", name);
        args.putString("desc", desc);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loggedIn = false;
        loggedUser = "";
        SharedPreferences settings = getActivity().getSharedPreferences("app.istts.ar", 0);
        long last = settings.getLong("lastlogin", 0);
        if (last != 0) {
            long difference = new Date().getTime() - last;

            // user still logged on within 5 minutes
            if (difference < 300000) {
                loggedIn = true;
                loggedUser = settings.getString("loggeduser", "");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        LinearLayout mLayout = (LinearLayout) inflater.inflate(R.layout.comments_dialogfragment,
                container);

        lblLocationName = (TextView) mLayout.findViewById(R.id.lblLocationName);
        lblLocationName.setText(getArguments().getString("title"));
        lblLocationDesc = (TextView) mLayout.findViewById(R.id.lblLocationDesc);
        lblLocationDesc.setText(getArguments().getString("desc"));

        ratingBar1 = (RatingBar) mLayout.findViewById(R.id.ratingBar1);
        lblComment_Content = (TextView) mLayout.findViewById(R.id.lblComments_content);
        PostToWS getComments = new PostToWS() {

            @Override
            public Void preExecute() {
                lblComment_Content.setText("");
                return null;
            }

            @Override
            public String postResult(String result) {
                StringBuilder sb = new StringBuilder("");

                if (result.trim().equals("false")) {
                    sb.append("No one has commented yet. be the first commenter!");
                } else {
                    Log.d("HASIL", result);
                    for (String s : result.split(String.valueOf((char) 020))) {
                        String[] usercommentrating = s.split(String.valueOf((char) 007));
                        sb.append(usercommentrating[0] + ": ");
                        sb.append(usercommentrating[1] + "\n");

                        // TODO: MANAGE RATINGS
                    }
                    
                }

                lblComment_Content.setText(sb);
                return null;
            }

        };

        getComments.addData("lokasi", lblLocationName.getText().toString());

        getComments.execute(new String[] {
                "http://lach.hopto.org:8080/isttsar.ws/comments/get"
        });

        txtComment = (EditText) mLayout.findViewById(R.id.txtComment);
        btnAddComments = (Button) mLayout.findViewById(R.id.btnAddComments);
        btnAddComments.setOnClickListener(btnAddCommentsListener);
        if (loggedIn) {
            txtComment.setVisibility(View.VISIBLE);
            btnAddComments.setText("Add comment");
        } else {
            txtComment.setVisibility(View.GONE);
            btnAddComments.setText("Login to comment");
        }

        return mLayout;
    }

    View.OnClickListener btnAddCommentsListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (loggedIn) {
                PostToWS postURL = new PostToWS() {

                    @Override
                    public Void preExecute() {
                        return null;
                    }

                    @Override
                    public String postResult(String result) {
                        if (result.trim().equals("false")) {
                            Toast.makeText(getActivity(),
                                    "Adding comment failed. please try again", Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            Toast.makeText(getActivity(), "Thanks! Comment added.",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }

                        return null;
                    }


                };

                postURL.addData("user", loggedUser);
                postURL.addData("lokasi", lblLocationName.getText().toString());
                postURL.addData("waktu", String.valueOf(new Date().getTime()));
                postURL.addData("comment", txtComment.getText().toString());
                postURL.addData("rating", String.valueOf(ratingBar1.getRating()));

                postURL.execute(new String[] {
                        "http://lach.hopto.org:8080/isttsar.ws/comments"
                });
            } else {
                FragmentManager fm = getFragmentManager();

                DialogFragment llrDialog = new LoginLogoutRegisterDialogFragment();
                llrDialog.setRetainInstance(true);
                llrDialog.show(fm, "Comments");

            }

        }
    };

}
