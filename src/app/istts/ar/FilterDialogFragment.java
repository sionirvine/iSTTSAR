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
import android.widget.Spinner;

import com.jwetherell.augmented_reality.data.ARData;

public class FilterDialogFragment extends DialogFragment {
    private EditText txtFilterName;
    private Spinner spinLantai;
    private Spinner spinGedung;
    private Button btnFilter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("Filter");
        LinearLayout mLayout = (LinearLayout) inflater.inflate(R.layout.filter_dialogfragment,
                container);

        txtFilterName = (EditText) mLayout.findViewById(R.id.txtFilterName);
        spinLantai = (Spinner) mLayout.findViewById(R.id.spinLantai);
        spinGedung = (Spinner) mLayout.findViewById(R.id.spinGedung);
        btnFilter = (Button) mLayout.findViewById(R.id.btnFilter);

        ArrayAdapter<CharSequence> adapLantai = ArrayAdapter.createFromResource(getActivity(),
                R.array.lantai_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapLantai.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinLantai.setAdapter(adapLantai);

        ArrayAdapter<CharSequence> adapGedung = ArrayAdapter.createFromResource(getActivity(),
                R.array.gedung_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapGedung.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinGedung.setAdapter(adapGedung);

        btnFilter.setOnClickListener(new btnFilterListener());

        return mLayout;
    }

    private class btnFilterListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ARData.clearMarkers();

            MarkerDataSource localData = new MarkerDataSource(getActivity().getResources());

            String name = txtFilterName.getText().toString().trim();
            String lantai = spinLantai.getSelectedItem().toString();
            String gedung = spinGedung.getSelectedItem().toString();

            localData.getMarkersFilter(name, lantai, gedung);
            dismiss();
        }

    }

}
