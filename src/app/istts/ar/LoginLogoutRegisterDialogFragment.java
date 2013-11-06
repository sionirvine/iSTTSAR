package app.istts.ar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class LoginLogoutRegisterDialogFragment extends DialogFragment {

    private EditText txtUser;
    private EditText txtPass;
    private Button btnLogin;
    private Button btnRegister;
    private ProgressBar prgLogin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        LinearLayout mLayout = (LinearLayout) inflater.inflate(
                R.layout.loginlogoutregister_dialogfragment, container);

        txtUser = (EditText) mLayout.findViewById(R.id.txtUser);
        txtPass = (EditText) mLayout.findViewById(R.id.txtPass);
        btnLogin = (Button) mLayout.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(btnLoginListener);
        btnRegister = (Button) mLayout.findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(btnRegisterListener);
        prgLogin = (ProgressBar) mLayout.findViewById(R.id.prgLogin);

        return mLayout;
    }

    View.OnClickListener btnLoginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String user = txtUser.getText().toString();
            String pass = "";
            try {
                pass = SHA1(txtPass.getText().toString());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            PostToWS postURL = new PostToWS() {

                @Override
                public Void preExecute() {
                    prgLogin.setVisibility(View.VISIBLE);
                    return null;
                }

                @Override
                public String postResult(String result) {
                    prgLogin.setVisibility(View.GONE);

                    if (result.trim().equals("false")) {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Login Failed, Username already exists",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "Login Successful",
                                Toast.LENGTH_SHORT).show();

                        SharedPreferences settings = getActivity().getSharedPreferences(
                                "app.istts.ar", 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("loggeduser", user);
                        editor.putLong("lastlogin", new Date().getTime());
                        
                        // Commit the edits!
                        editor.commit();
                    }
                    dismiss();

                    return null;
                }

            };

            postURL.addData("username", user);
            postURL.addData("password", pass);

            postURL.execute(new String[] {
                    "http://lach.hopto.org:8080/isttsar.ws/login"
            });
        }
    };

    View.OnClickListener btnRegisterListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            String user = txtUser.getText().toString();
            String pass = "";
            try {
                pass = SHA1(txtPass.getText().toString());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            PostToWS postURL = new PostToWS() {

                @Override
                public Void preExecute() {
                    prgLogin.setVisibility(View.VISIBLE);
                    return null;
                }

                @Override
                public String postResult(String result) {
                    prgLogin.setVisibility(View.GONE);

                    if (result.trim().equals("false")) {
                        Toast.makeText(getActivity().getApplicationContext(), "Register Failed",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Register Successful",
                                Toast.LENGTH_SHORT).show();
                    }
                    dismiss();

                    return null;
                }

            };

            postURL.addData("username", user);
            postURL.addData("password", pass);

            postURL.execute(new String[] {
                    "http://lach.hopto.org:8080/isttsar.ws/register"
            });
        }
    };
    
    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }
}
