package app.istts.ar;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

public class NewMarkerDialogFragment extends DialogFragment {

    static NewMarkerDialogFragment setDialog(Double latitude, Double longitude) {
        NewMarkerDialogFragment f = new NewMarkerDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putDouble("lat", latitude);
        args.putDouble("lng", longitude);

        f.setArguments(args);

        return f;
    }

    private EditText txtMarkName;
    private EditText txtMarkDesc;
    private Spinner spinMarkLantai;
    private Spinner spinMarkGedung;
    private Button btnMarkOK;
    private Button btnMarkCancel;
    private ProgressBar prgMark;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("Finish marker");
        LinearLayout mLayout = (LinearLayout) inflater.inflate(R.layout.newmarker_dialogfragment,
                container);

        txtMarkName = (EditText) mLayout.findViewById(R.id.txtMarkName);
        txtMarkDesc = (EditText) mLayout.findViewById(R.id.txtMarkDesc);
        spinMarkLantai = (Spinner) mLayout.findViewById(R.id.spinMarkLantai);
        spinMarkGedung = (Spinner) mLayout.findViewById(R.id.spinMarkGedung);
        btnMarkOK = (Button) mLayout.findViewById(R.id.btnMarkOK);
        btnMarkCancel = (Button) mLayout.findViewById(R.id.btnMarkCancel);
        prgMark = (ProgressBar) mLayout.findViewById(R.id.prgMark);
        
        ArrayAdapter<CharSequence> adapLantai = ArrayAdapter.createFromResource(getActivity(),
                R.array.lantai_array, android.R.layout.simple_spinner_item);
        adapLantai.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinMarkLantai.setAdapter(adapLantai);

        ArrayAdapter<CharSequence> adapGedung = ArrayAdapter.createFromResource(getActivity(),
                R.array.gedung_array, android.R.layout.simple_spinner_item);
        adapGedung.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinMarkGedung.setAdapter(adapGedung);
        
        btnMarkOK.setOnClickListener(new btnOKListener());
        btnMarkCancel.setOnClickListener(new btnCancelListener());

        return mLayout;
    }

    private void closeDialog() {
        dismiss();
    }

    private class btnOKListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (!txtMarkName.getText().toString().trim().equals("")) {
                if (!txtMarkDesc.getText().toString().trim().equals("")) {
                    if (!spinMarkLantai.getSelectedItem().toString().equals("none")) {
                        if (!spinMarkGedung.getSelectedItem().toString().equals("none")) {
                            // everything OK
                            Double lat = getArguments().getDouble("lat");
                            Double lng = getArguments().getDouble("lng");

                            PostToWS postURL = new PostToWS() {

                                @Override
                                public Void preExecute() {
                                    prgMark.setVisibility(View.VISIBLE);
                                    return null;
                                }

                                @Override
                                public String postResult(String result) {
                                    prgMark.setVisibility(View.GONE);
                                    
                                    if (!result.trim().equals("false")) {
                                        Toast.makeText(getActivity(), "Marker Added!",
                                                Toast.LENGTH_SHORT).show();

                                    } else {
                                        Toast.makeText(getActivity(), "Failed to add Marker!",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    
                                    closeDialog();
                                    return null;
                                }
                            };

                            postURL.addData("name", txtMarkName.getText().toString());
                            postURL.addData("latitude", String.valueOf(lat));
                            postURL.addData("longitude", String.valueOf(lng));
                            postURL.addData("lantai", spinMarkLantai.getSelectedItem().toString());
                            postURL.addData("gedung", spinMarkGedung.getSelectedItem().toString());
                            postURL.addData("description", txtMarkDesc.getText().toString());

                            postURL.execute(new String[] {
                                    "http://lach.hopto.org:8080/isttsar.ws/marker/add"
                            });
                        }
                    }
                }
            }
        }
    }

    private class btnCancelListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            dismiss();
        }
    }
}
