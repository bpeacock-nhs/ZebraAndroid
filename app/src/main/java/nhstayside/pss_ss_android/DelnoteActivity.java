package nhstayside.pss_ss_android;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
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

public class DelnoteActivity extends AppCompatActivity {

    static ConnectionClass connectionClass;
    static SysproConnection sysproConnection;
    static final List<List<String>> SorList = new LinkedList<List<String>>();
    static final List<List<String>> DescList = new LinkedList<List<String>>();
    static String userInitials, batch = "", status;
    GridView Lines;
    ProgressBar progressBar;
    ImageButton yesBtn;
    boolean complete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delnote);
        TextView initials = findViewById(R.id.textView_initials1);

        progressBar = this.findViewById(R.id.progressBar);
        Lines = findViewById(R.id.sorGrid);
        yesBtn = findViewById(R.id.imageButton_tick);

        yesBtn.setOnClickListener(yesClick);

        Lines.setNumColumns(3);

        yesBtn.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        Bundle b = getIntent().getExtras();
        userInitials = b.getString("initials");
        status = b.getString("status");

        initials.setText(userInitials);

        connectionClass = new ConnectionClass();
        sysproConnection = new SysproConnection();

        GetDelDetails GetLines = new GetDelDetails();
        GetLines.execute();
    }

    private View.OnClickListener yesClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String z;
            Connection con = connectionClass.CONN();
            if (con == null) {
                z = "Error in connection with SQL server.";
            } else {
                String query = "UPDATE [DN] SET [status] = 'Y' where login = '" + userInitials + "'";
                try {
                    PreparedStatement stmt = con.prepareStatement(query);
                    stmt.executeUpdate();
                    z = "Complete.";
                } catch (SQLException se) {
                    Log.e("ERROR", se.getMessage());
                    z = se.getMessage();
                }
            }
            Toast.makeText(DelnoteActivity.this, z, Toast.LENGTH_SHORT).show();
            finish();
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {

        if (e.getAction() == KeyEvent.ACTION_MULTIPLE){
            batch = e.getCharacters();
        }

        if (e.getAction() == KeyEvent.ACTION_UP) {
            if (e.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                Toast.makeText(DelnoteActivity.this, "You scanned batch " + batch, Toast.LENGTH_SHORT).show();
                for (int i = 0; i < SorList.size(); i++) {
                    if (SorList.get(i).get(2).equals(batch)) {
                        String z;
                        Connection con = connectionClass.CONN();
                        if (con == null) {
                            z = "Error in connection with SQL server.";
                        } else {
                            String query;
                            if (status == "DN") {
                                query = "UPDATE [deldetails] SET [status] = 'green' where batch = '" + batch + "'";
                            } else {
                                query = "UPDATE [issdetails] SET [status] = 'green' where batch = '" + batch + "'";
                            }
                            try {
                                PreparedStatement stmt = con.prepareStatement(query);
                                stmt.executeUpdate();
                                z = "Complete.";
                            } catch (SQLException se) {
                                Log.e("ERROR", se.getMessage());
                                z = se.getMessage();
                            }
                        }
                       // Toast.makeText(DelnoteActivity.this, z, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, PickqtyActivity.class);
                        intent.putExtra("qty", Integer.parseInt(SorList.get(i).get(1)));
                        intent.putExtra("batch", SorList.get(i).get(2));
                        intent.putExtra("desc", SorList.get(i).get(3));
                        intent.putExtra("initials", SorList.get(i).get(6));
                        startActivity(intent);
                    }
                }
                batch = "";
                return true;
            } else if (e.getKeyCode() > 6 && e.getKeyCode() < 17) {
                if (batch == "") {
                    batch = String.valueOf(e.getKeyCode() - 7);
                } else
                    batch = batch + (e.getKeyCode() - 7);
            }
        }
        return super.dispatchKeyEvent(e);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        GetDelDetails GetLines = new GetDelDetails();
        GetLines.execute();
    }

    public class GetDelDetails extends AsyncTask<String, String, String> {
        String z = "";
        ResultSet rs;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String r) {
            progressBar.setVisibility(View.INVISIBLE);
            if (r != "No details available.") {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(DelnoteActivity.this, android.R.layout.simple_list_item_1) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        ((TextView) view).setGravity(View.TEXT_ALIGNMENT_CENTER);
                        if (position > 2) {

                            ((TextView) view).setTextColor(Color.WHITE);
                            ((TextView) view).setShadowLayer(5, 1, 1, Color.BLACK);
                            ((TextView) view).setTextSize(19);
                            ((TextView) view).setTypeface(null, Typeface.BOLD);

                            if (SorList.get((position / 3) - 1).get(4).contains("yellow")) {
                                view.setBackgroundColor(0xCBFFE500);
                            }
                            if (SorList.get((position / 3) - 1).get(4).contains("green")) {
                                view.setBackgroundColor(0xCC00FF0A);
                            }
                            if (SorList.get((position / 3) - 1).get(4).contains("orange")) {
                                view.setBackgroundColor(Color.RED);
                            }
                        } else {
                            ((TextView) view).setTextColor(Color.WHITE);
                            view.setBackgroundColor(Color.BLACK);
                            ((TextView) view).setShadowLayer(5, 1, 1, Color.BLACK);
                            ((TextView) view).setTextSize(25);
                        }
                        return view;
                    }
                };

                adapter.add("BIN");
                adapter.add("QTY");
                adapter.add("BATCH");

                for (int i = 0; i < SorList.size(); i++) {
                    adapter.add(SorList.get(i).get(0));
                    adapter.add(SorList.get(i).get(1));
                    adapter.add(SorList.get(i).get(2));
                    if (SorList.get(i).get(4).equals("green")) {
                        complete = true;
                    }
                    else {
                        complete = false;
                    }
                }
                Lines.setAdapter(adapter);
                Lines.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position > 2) {
                            int row = (position / 3) - 1;
                            if (row >= 0 && SorList.get(row).get(4).equals("orange")) {
                                Intent intent = new Intent(DelnoteActivity.this, PickqtyActivity.class);
                                intent.putExtra("qty", Integer.parseInt(SorList.get(row).get(1)));
                                intent.putExtra("batch", SorList.get(row).get(2));
                                intent.putExtra("desc", SorList.get(row).get(3));
                                intent.putExtra("initials", SorList.get(row).get(6));
                                startActivity(intent);
                            }
                            if (row >= 0 && SorList.get(row).get(4).equals("yellow")) {
                                GetDesc GetDescription = new GetDesc();
                                GetDescription.execute(SorList.get(row).get(2));
                            }
                        }
                    }
                });
            } else {
                //Toast.makeText(DelnoteActivity.this, r, Toast.LENGTH_SHORT).show();
                finish();
            }
            if (complete) {
                yesBtn.setVisibility(View.VISIBLE);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                       //finish();
                    }
                }, 5000);   //5 seconds
            }
        }

        @Override
        protected String doInBackground(String... params) {
            Connection con = connectionClass.CONN();
            if (con == null) {
                z = "Error in connection with SQL server.";
            } else {
                String query;
                if (status.equals("DN")) {
                    query = "select * from [deldetails] where login = '" + userInitials + "' order by bin";
                } else {
                    query = "select * from [issdetails] where login = '" + userInitials + "' order by bin";
                }
                try {
                    PreparedStatement stmt = con.prepareStatement(query);
                    rs = stmt.executeQuery();
                    final ResultSetMetaData meta = rs.getMetaData();
                    final int columnCount = meta.getColumnCount();
                    if (rs.next()) {
                        SorList.clear();
                        z = "Connected successfully.";
                        do {
                            final List<String> columnList = new LinkedList<String>();
                            SorList.add(columnList);
                            for (int column = 1; column <= columnCount; ++column) {
                                final Object value = rs.getObject(column);
                                columnList.add(String.valueOf(value));
                            }
                        } while (rs.next());
                    } else {
                        z = "No details available.";
                    }
                } catch (SQLException se) {
                    Log.e("ERROR", se.getMessage());
                    z = se.getMessage();
                }
            }
            return z;
        }
    }
    public class GetDesc extends AsyncTask<String, String, String> {
        String z = "";
        ResultSet rs;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String r) {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(DelnoteActivity.this, DescList.get(0).get(1) + " " + DescList.get(0).get(2), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... params) {
            Connection con = sysproConnection.CONN();
            if (con == null) {
                z = "Error in connection with SQL server.";
            } else {
                String query = "SELECT * FROM [SysproCompanyA].[dbo].[LOT_DESCRIPTION] WHERE Lot = '000000000" + params[0] + "'";
                try {
                    PreparedStatement stmt = con.prepareStatement(query);
                    rs = stmt.executeQuery();
                    final ResultSetMetaData meta = rs.getMetaData();
                    final int columnCount = meta.getColumnCount();
                    if (rs.next()) {
                        DescList.clear();
                        do {
                            final List<String> columnList = new LinkedList<String>();
                            DescList.add(columnList);
                            for (int column = 1; column <= columnCount; ++column) {
                                final Object value = rs.getObject(column);
                                columnList.add(String.valueOf(value));
                            }
                        } while (rs.next());
                    } else {
                        z = "No details available.";
                    }
                } catch (SQLException se) {
                    Log.e("ERROR", se.getMessage());
                    z = se.getMessage();
                }
            }
            return z;
        }
    }
}
