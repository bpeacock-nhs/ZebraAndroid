package nhstayside.pss_ss_android;

import static android.view.KeyEvent.KEYCODE_ENTER;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class SubmainActivity extends AppCompatActivity {

    EditText editText_Scan;
    TextView textView_initials;
    ImageButton keypad;
    Boolean toggled = false;
    String batch = "", initials = "", binno = "", qty = "";
    SysproConnection sysproConnection;
    ConnectionClass connectionClass;
    ProgressBar progressBar;
    final List<List<String>> rowList = new LinkedList<List<String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submain);

        editText_Scan = findViewById(R.id.editText);
        progressBar = findViewById(R.id.progressBar);

        keypad = findViewById(R.id.imageButton_keypad);

        sysproConnection = new SysproConnection();
        connectionClass = new ConnectionClass();

        editText_Scan.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        keypad.setOnClickListener(toggleKeypad);

        textView_initials = findViewById(R.id.textView_initials1);
        Bundle b = getIntent().getExtras();
        initials = b.getString("initials");
        textView_initials.setText(initials);

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getAction() == KeyEvent.ACTION_MULTIPLE){
            if (e.getCharacters().length() <= 6) {

                if (editText_Scan.length() == 0) {
                    editText_Scan.setText(e.getCharacters());
                }

                    View current = getCurrentFocus();
                    if (current != null) current.clearFocus();

                    if (toggled) {
                        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        toggled = false;
                        editText_Scan.setVisibility(View.INVISIBLE);
                    }

                    if (editText_Scan.getText().length() == 6) {
                        batch = editText_Scan.getText().toString();
                        Intent i = new Intent(SubmainActivity.this, ConfirmActivity.class);
                        i.putExtra("batch", batch);
                        i.putExtra("initials", initials);
                        startActivity(i);

                        editText_Scan.setText("");
                        batch = "";

                        return true;
                    }

                    if (batch.length() == 4 || editText_Scan.getText().length() == 4) { //Get bin info
                        if (batch.length() == 4) {
                            binno = batch;
                        }
                        if (editText_Scan.getText().length() == 4) {
                            binno = editText_Scan.getText().toString();
                            editText_Scan.setText("");
                        }
                        if (!binno.equals("0000")) {
                            GetBin getBin = new GetBin();
                            getBin.execute("");
                        }
                    }
            } else if (e.getCharacters().length() == 9) { //grn
                GetBN getBN = new GetBN();
                getBN.execute(e.getCharacters());
            }
        } else if (e.getKeyCode() == KEYCODE_ENTER && e.getAction() == KeyEvent.ACTION_UP) {

            if (toggled) {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                toggled = false;
                editText_Scan.setVisibility(View.INVISIBLE);
            }

            if (editText_Scan.getText().length() == 6) {

                batch = editText_Scan.getText().toString();
                Intent i = new Intent(SubmainActivity.this, ConfirmActivity.class);
                i.putExtra("batch", batch);
                i.putExtra("initials", initials);
                startActivity(i);

                editText_Scan.setText("");
                batch = "";

                return true;
            }
        }
        return super.dispatchKeyEvent(e);
    }

    public class GetBin extends AsyncTask<String, String, String> {
        String z = "";
        ResultSet rs;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        protected void onPostExecute(String r) {
            progressBar.setVisibility(View.INVISIBLE);
            if (r != "") {
                Toast.makeText(SubmainActivity.this, r, Toast.LENGTH_LONG).show();
            } else {

                batch = rowList.get(0).get(0);
                qty = rowList.get(0).get(1);

                Intent i = new Intent(SubmainActivity.this, ConfirmActivity.class);
                i.putExtra("batch", batch);
                i.putExtra("qty", qty);
                i.putExtra("initials", initials);
                i.putExtra("binno", binno);
                startActivity(i);
            }
        }
        @Override
        protected String doInBackground(String... params) {
            Connection con = sysproConnection.CONN();
            if (con == null) {
                z = "Error in connection with SYSPRO.";
            } else {
                String query = "SELECT * from [SysproCompanyA].[dbo].[InvBinTrack] WHERE [Bin] = '" + binno + "'";
                try {
                    PreparedStatement stmt = con.prepareStatement(query);
                    rs = stmt.executeQuery();
                    final ResultSetMetaData meta = rs.getMetaData();
                    final int columnCount = meta.getColumnCount();
                    if (rs.next()) {
                        rowList.clear();
                        do {
                            final List<String> columnList = new LinkedList<String>();
                            rowList.add(columnList);
                            for (int column = 1; column <= columnCount; ++column) {
                                final Object value = rs.getObject(column);
                                columnList.add(String.valueOf(value));
                            }
                        } while (rs.next());
                    } else {
                        z = "Bin Not Found";
                    }
                } catch (SQLException se) {
                    Log.e("ERROR", se.getMessage());
                    z = se.getMessage();
                }
            }
            return z;
        }
    }

    public class GetBN extends AsyncTask<String, String, String> {
        String z = "";
        ResultSet rs;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        protected void onPostExecute(String r) {
            progressBar.setVisibility(View.INVISIBLE);
            if (r != "") {
                Toast.makeText(SubmainActivity.this, r, Toast.LENGTH_LONG).show();
            } else {
                batch = rowList.get(0).get(1).substring(9);
                Intent i = new Intent(SubmainActivity.this, ConfirmActivity.class);
                i.putExtra("batch", batch);
                i.putExtra("initials", initials);
                startActivity(i);
                editText_Scan.setText("");
                batch = "";
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String grn = params[0];
            Connection con = sysproConnection.CONN();
            if (con == null) {
                z = "Error in connection with SYSPRO.";
            } else {
                String query = "SELECT * from [SysproCompanyA].[dbo].[InvInspect] WHERE [Grn] = '000000" + grn + "'";
                try {
                    PreparedStatement stmt = con.prepareStatement(query);
                    rs = stmt.executeQuery();
                    final ResultSetMetaData meta = rs.getMetaData();
                    final int columnCount = meta.getColumnCount();
                    if (rs.next()) {
                        rowList.clear();
                        do {
                            final List<String> columnList = new LinkedList<String>();
                            rowList.add(columnList);
                            for (int column = 1; column <= columnCount; ++column) {
                                final Object value = rs.getObject(column);
                                columnList.add(String.valueOf(value));
                            }
                        } while (rs.next());
                    } else {
                        z = "GRN Not Found.";
                    }
                } catch (SQLException se) {
                    Log.e("ERROR", se.getMessage());
                    z = se.getMessage();
                }
            }
            return z;
        }
    }

    private View.OnClickListener toggleKeypad = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            toggled = true;
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            editText_Scan.setVisibility(View.VISIBLE);
            editText_Scan.setText("");
            editText_Scan.requestFocus();
        }
    };

    @Override
    public void onBackPressed() {
        toggled = false;
        Intent i = new Intent(SubmainActivity.this, MainActivity.class);
        startActivity(i);
    }
}

