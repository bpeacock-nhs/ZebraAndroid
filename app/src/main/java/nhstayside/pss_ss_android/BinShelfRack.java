package nhstayside.pss_ss_android;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class BinShelfRack extends AppCompatActivity {

    static String batch = "", parser = "", bin = "", shelf = "", qty = "", initials, binno;
    TextView textView_batch, textView_bin, textView_qty, textView_shelf, textView_initials;
    ImageButton yesBtn;
    ProgressBar progressBar;
    static final List<List<String>> WipList = new LinkedList<List<String>>();

    static SysproConnection syspro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binshelfrack);

        Bundle b = getIntent().getExtras();
        batch = b.getString("batch");
        qty = b.getString("qty");
        initials = b.getString("initials");
        binno = b.getString("binno");

        yesBtn = findViewById(R.id.imageButton_tick);
        textView_batch = findViewById(R.id.textView_batchno);
        textView_shelf = findViewById(R.id.textView_shelf);
        textView_qty = findViewById(R.id.textView_qty);
        textView_bin = findViewById(R.id.textView_bin);
        progressBar = findViewById(R.id.progressBar);
        textView_initials = findViewById(R.id.textView_initials);

        textView_shelf.setVisibility(View.INVISIBLE);
        textView_batch.setVisibility(View.INVISIBLE);
        textView_qty.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        yesBtn.setVisibility(View.INVISIBLE);

        if (binno != null) {

            textView_bin.setText("BIN: FP" + binno);
            textView_bin.setVisibility(View.VISIBLE);

        } else {

            binno = "0000";
            textView_bin.setVisibility(View.INVISIBLE);

        }

        yesBtn.requestFocus();

        textView_initials.setVisibility(View.VISIBLE);

        syspro = new SysproConnection();

        textView_batch.setText(batch);
        textView_qty.setText("QTY:  " + qty);
        textView_initials.setText(initials);
        yesBtn.setOnClickListener(yesClick);
    }

    public class Commit extends AsyncTask<String, String, String> {
        String z = "";
        ResultSet rs;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String r) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(BinShelfRack.this, r, Toast.LENGTH_LONG).show();
                finish();
        }

        @Override
        protected String doInBackground(String... params) {
            Connection conn = syspro.CONN();

            if (conn == null) {
                z = z + "Could not connect to database. ";
            } else {

                String query = "SELECT TOP (1) [Lot]\n" +
                        "      ,[Quantity]\n" +
                        "      ,[Bin]\n" +
                        "      ,[Location]\n" +
                        "      ,[Packer_initials]\n" +
                        "      ,[Storemen_initials]\n" +
                        "      ,[Packed_date]\n" +
                        "      ,[Stored_date]\n" +
                        "  FROM [InvBinTrack] WHERE [Lot] = '" + batch + "'";

                try {
                    PreparedStatement stmt = conn.prepareStatement(query);
                    rs = stmt.executeQuery();

                    if (qty.equals(null)) {
                        qty.equals("0");
                    }

                    if (rs.next()) {
                        if (binno == "0000") {

                            String update_query = "UPDATE [InvBinTrack] SET [Quantity] = '" + qty + "', [Storemen_initials] = '" + initials + "', [Location] = '" + shelf + "', [rack] = '" + shelf.substring(2, 5) + "', [shelf] = '" + shelf.substring(5) + "', [Stored_date] = CURRENT_TIMESTAMP WHERE [Lot] = '" + batch + "'";
                            PreparedStatement update_stmt = conn.prepareStatement(update_query);
                            update_stmt.executeUpdate();

                            String update_query2 = "UPDATE [InvMovements] SET [Bin] = '" + shelf + "' WHERE [Job] = '000000000" + batch + "' AND [Warehouse] = 'MS' AND [TrnType] = 'R'";
                            PreparedStatement update_stmt2 = conn.prepareStatement(update_query2);
                            update_stmt2.executeUpdate();

                            String update_query3 = "INSERT INTO [InvBinTrackLog] ([Lot], [Quantity], [Bin], [Location], [Packer_initials], [Storemen_initials], [Packed_date], [Stored_date], [rack], [shelf]) VALUES ('" + batch + "', '" + qty + "', '0000' ,'" + shelf + "', '" + initials + "', '" + initials + "', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '" + shelf.substring(2, 5) + "', '" + shelf.substring(5) + "')";
                            PreparedStatement update_stmt3 = conn.prepareStatement(update_query3);
                            update_stmt3.executeUpdate();

                            z = z + "Updated record. Logged movement.";

                        } else {

                            String update_query1 = "UPDATE [InvBinTrack] SET [Storemen_initials] = '" + initials + "', [Location] = '" + shelf + "', [rack] = '" + shelf.substring(2, 5) + "', [shelf] = '" + shelf.substring(5) + "', [Stored_date] = CURRENT_TIMESTAMP WHERE [Bin] = '" + binno + "'";
                            PreparedStatement update_stmt1 = conn.prepareStatement(update_query1);
                            update_stmt1.executeUpdate();

                            String update_query2 = "UPDATE [InvMovements] SET [Bin] = '" + shelf + "' WHERE [Job] = '000000000" + batch + "' AND [Warehouse] = 'MS' AND [TrnType] = 'R'";
                            PreparedStatement update_stmt2 = conn.prepareStatement(update_query2);
                            update_stmt2.executeUpdate();

                            String update_query3 = "INSERT INTO [InvBinTrackLog] ([Lot], [Quantity], [Bin], [Location], [Packer_initials], [Storemen_initials], [Packed_date], [Stored_date], [rack], [shelf]) VALUES ('" + batch + "', '" + qty + "', '0000' ,'" + shelf + "', '" + initials + "', '" + initials + "', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '" + shelf.substring(2, 5) + "', '" + shelf.substring(5) + "')";
                            PreparedStatement update_stmt3 = conn.prepareStatement(update_query3);
                            update_stmt3.executeUpdate();

                            z = z + "Updated record. Logged movement.";

                        }
                    } else {

                        String insert_query = "INSERT INTO [InvBinTrack] ([Lot], [Quantity], [Bin], [Location], [Packer_initials], [Storemen_initials], [Packed_date], [Stored_date], [rack], [shelf]) VALUES ('" + batch + "', '" + qty + "', '0000' ,'" + shelf + "', '" + initials + "', '" + initials + "', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '" + shelf.substring(2, 5) + "', '" + shelf.substring(5) + "')";
                        PreparedStatement insert_stmt = conn.prepareStatement(insert_query);
                        insert_stmt.executeUpdate();

                        String update_query2 = "UPDATE [InvMovements] SET [Bin] = '" + shelf + "' WHERE [Job] = '" + batch + "' AND [Warehouse] = 'MS' AND [TrnType] = 'R'";
                        PreparedStatement update_stmt2 = conn.prepareStatement(update_query2);
                        update_stmt2.executeUpdate();

                        String update_query3 = "INSERT INTO [InvBinTrackLog] ([Lot], [Quantity], [Bin], [Location], [Packer_initials], [Storemen_initials], [Packed_date], [Stored_date], [rack], [shelf]) VALUES ('" + batch + "', '" + qty + "', '0000' ,'" + shelf + "', '" + initials + "', '" + initials + "', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '" + shelf.substring(2, 5) + "', '" + shelf.substring(5) + "')";
                        PreparedStatement update_stmt3 = conn.prepareStatement(update_query3);
                        update_stmt3.executeUpdate();

                        z = z + "Inserted new record. Updated receipt bin. Logged movement.";

                    }

                } catch (SQLException se) {
                    Log.e("ERROR", se.getMessage());
                    z = z + se.getMessage();
                }
            }
            return z;
        }
    }

    private View.OnClickListener yesClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Commit commit = new Commit();
            commit.execute();
            finish();
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getAction() == KeyEvent.ACTION_MULTIPLE){
            parser = e.getCharacters();
        }
        if (e.getAction() == KeyEvent.ACTION_UP) {
            if (e.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (parser.length() == 7) {

                    textView_shelf.setText("RACK/SHELF: " + parser);

                    shelf = parser;

                    textView_shelf.setVisibility(View.VISIBLE);
                    textView_batch.setVisibility(View.VISIBLE);
                    textView_qty.setVisibility(View.VISIBLE);

                    yesBtn.setVisibility(View.VISIBLE);

                }
                if (parser.length() == 4) {

                    textView_shelf.setVisibility(View.VISIBLE);
                    textView_batch.setVisibility(View.VISIBLE);
                    textView_qty.setVisibility(View.VISIBLE);
                    yesBtn.setVisibility(View.VISIBLE);

                    bin = parser;
                }
                parser = "";
                return true;
            } else if (e.getKeyCode() > 6 &&
                    e.getKeyCode() < 17 ||
                    e.getKeyCode() == KeyEvent.KEYCODE_F ||
                    e.getKeyCode() == KeyEvent.KEYCODE_P ||
                    e.getKeyCode() == KeyEvent.KEYCODE_R ||
                    e.getKeyCode() == KeyEvent.KEYCODE_M ||
                    e.getKeyCode() == KeyEvent.KEYCODE_C ||
                    e.getKeyCode() == KeyEvent.KEYCODE_D ||
                    e.getKeyCode() == KeyEvent.KEYCODE_Z ||
                    e.getKeyCode() == KeyEvent.KEYCODE_S ||
                    e.getKeyCode() == KeyEvent.KEYCODE_Q ||
                    e.getKeyCode() == KeyEvent.KEYCODE_L) {
                if (parser == "") {
                    if (e.getKeyCode() == KeyEvent.KEYCODE_F ||
                            e.getKeyCode() == KeyEvent.KEYCODE_P ||
                            e.getKeyCode() == KeyEvent.KEYCODE_R ||
                            e.getKeyCode() == KeyEvent.KEYCODE_M ||
                            e.getKeyCode() == KeyEvent.KEYCODE_C ||
                            e.getKeyCode() == KeyEvent.KEYCODE_D ||
                            e.getKeyCode() == KeyEvent.KEYCODE_Z ||
                            e.getKeyCode() == KeyEvent.KEYCODE_S ||
                            e.getKeyCode() == KeyEvent.KEYCODE_Q ||
                            e.getKeyCode() == KeyEvent.KEYCODE_L) {
                        char c = (char) (e.getKeyCode() + 36);
                        parser += c;
                    } else {
                        parser = String.valueOf(e.getKeyCode() - 7);
                    }
                } else if (e.getKeyCode() == KeyEvent.KEYCODE_F ||
                        e.getKeyCode() == KeyEvent.KEYCODE_P ||
                        e.getKeyCode() == KeyEvent.KEYCODE_R ||
                        e.getKeyCode() == KeyEvent.KEYCODE_M ||
                        e.getKeyCode() == KeyEvent.KEYCODE_C ||
                        e.getKeyCode() == KeyEvent.KEYCODE_D ||
                        e.getKeyCode() == KeyEvent.KEYCODE_Z ||
                        e.getKeyCode() == KeyEvent.KEYCODE_S ||
                        e.getKeyCode() == KeyEvent.KEYCODE_Q ||
                        e.getKeyCode() == KeyEvent.KEYCODE_L) {
                    char c = (char) (e.getKeyCode() + 36);
                    parser += c;
                } else {
                    parser = parser + (e.getKeyCode() - 7);
                }
            }
        }
        return super.dispatchKeyEvent(e);
    }
}
