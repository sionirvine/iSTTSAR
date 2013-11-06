package app.istts.ar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

public class MatchDialogFragment extends DialogFragment {

    private RadioGroup radioGroup1;
    private Button btnDetect;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("Detect Location");
        LinearLayout mLayout = (LinearLayout) inflater.inflate(R.layout.match_dialogfragment,
                container);
        radioGroup1 = (RadioGroup) mLayout.findViewById(R.id.radioGroup1);
        btnDetect = (Button) mLayout.findViewById(R.id.btnDetect);
        btnDetect.setOnClickListener(new btnDetectListener());

        return mLayout;
    }

    private class btnDetectListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (radioGroup1.getCheckedRadioButtonId()) {
                case R.id.radOpenCV:
                    Intent data = new Intent();
                    data.putExtra("ITEM", "OPENCV");
                    getTargetFragment().onActivityResult(777, Activity.RESULT_OK, data);

                    dismiss();
                    break;
                case R.id.radTesseract:
                    Intent send = new Intent();
                    send.putExtra("ITEM", "TESSERACT");
                    getTargetFragment().onActivityResult(777, Activity.RESULT_OK, send);

                    dismiss();
                    break;
            }
        }

    }
}
