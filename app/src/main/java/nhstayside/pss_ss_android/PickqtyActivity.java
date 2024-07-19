package nhstayside.pss_ss_android;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PickqtyActivity extends AppCompatActivity {

    static ConnectionClass connectionClass;
    static String batch, desc;
    static int count, qty;
    static String initials;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickqty);
        progressBar = this.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        Bundle b = getIntent().getExtras();
        qty = b.getInt("qty");
        batch = b.getString("batch");
        desc = b.getString("desc");
        initials = b.getString("initials");

        TextView qtyText = findViewById(R.id.qty);
        TextView batchText = findViewById(R.id.batch);
        TextView descText = findViewById(R.id.desc);
        EditText pickQty = findViewById(R.id.pickQtyEditText);
        pickQty.requestFocus();

        qtyText.setText(String.valueOf(qty));
        batchText.setText(batch);
        descText.setText(desc);

        connectionClass = new ConnectionClass();
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public class line {

       public String bn;
       public int qty;
       public String logon;

       line(String batch, int quantity) {
           this.bn = batch;
           this.qty = quantity;
           this.logon = initials;
       }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        EditText pickQty = findViewById(R.id.pickQtyEditText);
        if (e.getAction() == KeyEvent.ACTION_UP && pickQty.length() > 0) {
            count = Integer.parseInt(pickQty.getText().toString());
            if (e.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (count == qty) {
                    line linedetails = new line(batch, qty);
                    UpdateDelDetails UpdateLines = new UpdateDelDetails();
                    UpdateLines.execute(linedetails);
                    finish();
                } else {
                    Toast.makeText(PickqtyActivity.this, "You have entered the wrong quantity.", Toast.LENGTH_SHORT).show();

                    finish();
                }
            }
            if (e.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                pickQty.setText("");
            }
        }
        return super.dispatchKeyEvent(e);
    }

    public class UpdateDelDetails extends AsyncTask<line, String, String> {
        String z = "";

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String r) {
            progressBar.setVisibility(View.INVISIBLE);
            //Toast.makeText(PickqtyActivity.this, r, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(line... params) {
            Connection con = connectionClass.CONN();
            if (con == null) {
                z = "Error in connection with SQL server.";
            } else {
                String query = "UPDATE TOP (1) [deldetails] SET status = 'green' WHERE batch = '" + params[0].bn + "' AND qty = '" + params[0].qty + "' AND login = '" + params[0].logon + "'";
                try {
                    PreparedStatement stmt = con.prepareStatement(query);
                    stmt.executeUpdate();
                    z = "Updated line status successfully.";
                } catch (SQLException se) {
                    Log.e("ERROR", se.getMessage());
                    z = "Could not update line status.";
                }
            }
            return z;
        }
    }
}
