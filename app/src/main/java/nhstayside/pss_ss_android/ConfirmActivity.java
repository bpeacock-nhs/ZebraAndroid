package nhstayside.pss_ss_android;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ConfirmActivity extends AppCompatActivity {

    String batch, code, desc1, qty, initials, status, qtyInt, binno, qtyBin;
    TextView textView_batch, textView_desc1, textView_qty, textView_status, textView_code, textView_initials, textView_bin;
    ImageButton yes;
    ProgressBar progressBar;
    SysproConnection syspro;
    ConnectionClass storeswireless;
    ToggleButton toggleButton;

    static final List<List<String>> WipList = new LinkedList<>();
    static final List<List<String>> WIPMaster = new LinkedList<>();
    static final List<List<String>> InvInspect = new LinkedList<>();
    static final List<List<String>> RM_Details = new LinkedList<>();
    static final List<List<String>> LotDetail = new LinkedList<>();
    static final List<List<String>> InvBinTrack = new LinkedList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        textView_batch = findViewById(R.id.textView_batchno);
        textView_desc1 = findViewById(R.id.textView_desc);
        textView_status = findViewById(R.id.textView_status);
        textView_qty = findViewById(R.id.textView_qty);
        textView_code = findViewById(R.id.textView_code);
        textView_bin = findViewById(R.id.textView_bin);
        progressBar = findViewById(R.id.progressBar);
        yes = findViewById(R.id.imageButton_tick);
        textView_initials = findViewById(R.id.textView_initials);
        toggleButton = findViewById(R.id.priorityButton);

        textView_qty.setVisibility(View.INVISIBLE);
        textView_batch.setVisibility(View.INVISIBLE);
        textView_desc1.setVisibility(View.INVISIBLE);
        textView_code.setVisibility(View.INVISIBLE);
        textView_status.setVisibility(View.INVISIBLE);
        textView_bin.setVisibility(View.INVISIBLE);

        progressBar.setVisibility(View.INVISIBLE);
        yes.setVisibility(View.INVISIBLE);
        toggleButton.setVisibility(View.INVISIBLE);

        syspro = new SysproConnection();
        storeswireless = new ConnectionClass();
        yes.setOnClickListener(yesClick);

        toggleButton.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b){
                String z = "";
                compoundButton.setBackgroundColor(Color.MAGENTA);
                Connection conn = syspro.CONN();

                if (conn == null) {
                     z = "Could not connect to database. ";
                } else {

                    String update_query = "UPDATE [WipMaster] SET [Priority] = 99 WHERE [Job] = '000000000" + batch + "'";

                    try {

                        PreparedStatement stmt = conn.prepareStatement(update_query);
                        stmt.executeUpdate();

                    } catch (SQLException se) {
                        Log.e("ERROR", se.getMessage());
                        z = z + se.getMessage();
                    }
                }
            } else {

                Connection conn = syspro.CONN();
                String z = "";
                if (conn == null) {
                    z = z + "Could not connect to database. ";
                } else {
                    compoundButton.setBackgroundColor(Color.GRAY);
                    String update_query = "UPDATE [WipMaster] SET [Priority] = 50 WHERE [Job] = '000000000" + batch + "'";

                    try {

                        PreparedStatement stmt = conn.prepareStatement(update_query);
                        stmt.executeUpdate();

                    } catch (SQLException se) {
                        Log.e("ERROR", se.getMessage());
                        z = z + se.getMessage();
                    }
                }
            }
        });

        Bundle b = getIntent().getExtras();
        batch = b.getString("batch");
        initials = b.getString("initials");
        binno = b.getString("binno");
        qtyBin = b.getString("qty");

        textView_initials.setText(initials);

        GetDetails getDetails = new GetDetails();
        getDetails.execute();
        CheckPriority checkPriority = new CheckPriority();
        checkPriority.execute();

    }

    public class CheckPriority extends AsyncTask<String, String, String> {
        String z = "";
        ResultSet rs;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(String r) {
            if (WipList.size() > 0) {
                if (WipList.get(0).get(1).contains("99")) {
                    toggleButton.setChecked(true);
                    WipList.clear();
                } else {
                    toggleButton.setChecked(false);
                    WipList.clear();
                }
                toggleButton.setVisibility(View.VISIBLE);
            } else {
                toggleButton.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        protected String doInBackground(String... params) {

            Connection conn = syspro.CONN();

            if (conn == null) {
                z = z + "Could not connect to database. ";
            } else {
                String query = "SELECT [Job], [Priority] FROM [WipMaster] WHERE [Job] = '000000000" + batch + "'";
                try {
                    PreparedStatement stmt = conn.prepareStatement(query);
                    rs = stmt.executeQuery();
                    final ResultSetMetaData meta = rs.getMetaData();
                    final int columnCount = meta.getColumnCount();
                    if (rs.next()) {
                        WipList.clear();
                        z = "Connected successfully.";
                        do {
                            final List<String> columnList = new LinkedList<String>();
                            WipList.add(columnList);
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
                    z = z + se.getMessage();
                }
            }
            return z;
        }
    }

    private View.OnClickListener yesClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String z = "";
            Intent i = new Intent(ConfirmActivity.this, BinShelfRack.class);
            i.putExtra("batch", batch);

            if (qtyBin != null) {
                i.putExtra("qty", qtyBin);
            } else {
                i.putExtra("qty", qtyInt);
            }


            i.putExtra("initials", initials);
            i.putExtra("binno", binno);
            startActivity(i);
            finish();
        }
    };

    public class GetDetails extends AsyncTask<String, String, String> {
        String z = "";
        ResultSet rs0, rs1, rs2, rs3, rs4, rs5;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String r) {
            if (r != "") {
                Toast.makeText(ConfirmActivity.this, r, Toast.LENGTH_LONG).show();
                finish();
            } else {

                if (WIPMaster.size() > 0) {
                    String[] subcodes = WIPMaster.get(0).get(6).split("/");
                    code = subcodes[0];
                    desc1 = WIPMaster.get(0).get(7);
                    qty = "QTY TO MAKE: " + WIPMaster.get(0).get(107);
                    qtyInt = WIPMaster.get(0).get(107);
                }

                if (InvInspect.size() > 0) {
                    String[] subcodes = RM_Details.get(0).get(1).split("/");
                    code = subcodes[0];
                    desc1 = RM_Details.get(0).get(2);
                    qty = "ADVISED QTY: " + RM_Details.get(0).get(4);
                    qtyInt = RM_Details.get(0).get(4);
                }

                if (LotDetail.size() > 0) {
                    qty = "QTY ON HAND: " + LotDetail.get(0).get(28);
                    qtyInt = LotDetail.get(0).get(28);
                }

                if (LotDetail.size() > 0 && WIPMaster.size() > 0) {
                    status = "STATUS: RECEIVED";
                } else if (LotDetail.size() > 0 && InvInspect.size() > 0) {
                    status = "STATUS: ACCEPTED";
                } else if (InvInspect.size() > 0) {
                    status = "STATUS: IN BOND";
                } else if (WIPMaster.size() > 0) {
                    status = "STATUS: WIP";
                }

                if (binno != null) {
                    qty = "BIN QTY: " + qtyBin;
                    textView_bin.setText("BIN: " + binno);
                    textView_bin.setVisibility(View.VISIBLE);
                }

                textView_desc1.setText(desc1);
                textView_batch.setText(batch);
                textView_code.setText(code);
                textView_qty.setText(qty);
                textView_status.setText(status);

                textView_status.setVisibility(View.VISIBLE);
                textView_code.setVisibility(View.VISIBLE);
                textView_batch.setVisibility(View.VISIBLE);
                textView_desc1.setVisibility(View.VISIBLE);
                textView_qty.setVisibility(View.VISIBLE);
                yes.setVisibility(View.VISIBLE);

                if (InvBinTrack.size() > 0) {
                    if (!InvBinTrack.get(0).get(2).equals("0000") || binno != null) {
                        status = "STATUS: BINNED";
                        textView_status.setText(status);
                        yes.setVisibility(View.INVISIBLE);
                    }
                }

                progressBar.setVisibility(View.INVISIBLE);

                WIPMaster.clear();
                InvInspect.clear();
                RM_Details.clear();
                LotDetail.clear();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            Connection con = syspro.CONN();

            if (con == null) {
                z = "Could not connect to SYSPRO.";
            } else {

                //check if bn is in invbintrack
                String bin_check = "SELECT * FROM [SysproCompanyA].[dbo].[InvBinTrack] WHERE [Lot] ='" + batch + "'";

                //check if bn is in inspection
                String inspection_check = "SELECT * FROM [SysproCompanyA].[dbo].[InvInspect] WHERE [Lot] ='000000000" + batch + "'";

                //check if bn is in WIP
                String wip_check = "SELECT t.*, CAST(QtyToMake AS int) AS [Qty] FROM [SysproCompanyA].[dbo].[WipMaster] t WHERE Job = '000000000" + batch + "' AND Warehouse = 'MS'";

                //check if bn is TrnType R in LotTransactions
                String trntype_check = "SELECT * FROM [SysproCompanyA].[dbo].[LotTransactions] WHERE [Lot] = '000000000" + batch + "' AND [TrnType] = 'R' AND Warehouse = 'MS'";

                //get current QoH if in LotTransactions as TrnType R
                String QoH = "SELECT t.*, CAST(QtyOnHand AS int) AS [Qty] FROM [SysproCompanyA].[dbo].[LotDetail] t WHERE [Lot] = '000000000" + batch + "' AND Warehouse = 'MS'";

                //get additional RM details
                String rm_details = "SELECT t.*, CAST(QtyAdvised AS int) AS [Qty] FROM [SysproCompanyA].[dbo].[vw_whulme_lotdesc] t WHERE Lot = '000000000" + batch + "'";

                try {
                    PreparedStatement stmt0 = con.prepareStatement(bin_check);
                    rs0 = stmt0.executeQuery();
                    if (rs0.next()) {
                        final ResultSetMetaData meta = rs0.getMetaData();
                        final int columnCount = meta.getColumnCount();
                        InvBinTrack.clear();
                        do {
                            final List<String> columnList = new LinkedList<String>();
                            InvBinTrack.add(columnList);
                            for (int column = 1; column <= columnCount; ++column) {
                                final Object value = rs0.getObject(column);
                                columnList.add(String.valueOf(value));
                            }
                        } while (rs0.next());
                    }

                    PreparedStatement stmt1 = con.prepareStatement(trntype_check);
                    rs1 = stmt1.executeQuery();

                    if (rs1.next()) { // Get QoH, TrnType is R

                        PreparedStatement stmt5 = con.prepareStatement(QoH);
                        rs5 = stmt5.executeQuery();

                        final ResultSetMetaData meta5 = rs5.getMetaData();
                        final int columnCount5 = meta5.getColumnCount();

                        if (rs5.next()) {
                            LotDetail.clear();
                            do {
                                final List<String> columnList = new LinkedList<String>();
                                LotDetail.add(columnList);
                                for (int column = 1; column <= columnCount5; ++column) {
                                    final Object value = rs5.getObject(column);
                                    columnList.add(String.valueOf(value));
                                }
                            } while (rs5.next());
                        }
                    }

                    PreparedStatement stmt2 = con.prepareStatement(inspection_check);
                    rs2 = stmt2.executeQuery();

                    final ResultSetMetaData meta2 = rs2.getMetaData();
                    final int columnCount2 = meta2.getColumnCount();

                    if (rs2.next()) { // Get Inspection Details
                        InvInspect.clear();
                        do {
                            final List<String> columnList = new LinkedList<String>();
                            InvInspect.add(columnList);
                            for (int column = 1; column <= columnCount2; ++column) {
                                final Object value = rs2.getObject(column);
                                columnList.add(String.valueOf(value));
                            }
                        } while (rs2.next());

                        PreparedStatement stmt4 = con.prepareStatement(rm_details);
                        rs4 = stmt4.executeQuery();

                        final ResultSetMetaData meta4 = rs4.getMetaData();
                        final int columnCount4 = meta4.getColumnCount();

                        if (rs4.next()) {
                            RM_Details.clear();
                            do {
                                final List<String> columnList = new LinkedList<String>();
                                RM_Details.add(columnList);
                                for (int column = 1; column <= columnCount4; ++column) {
                                    final Object value = rs4.getObject(column);
                                    columnList.add(String.valueOf(value));
                                }
                            } while (rs4.next());
                        }
                    }

                    PreparedStatement stmt3 = con.prepareStatement(wip_check);
                    rs3 = stmt3.executeQuery();

                    final ResultSetMetaData meta3 = rs3.getMetaData();
                    final int columnCount3 = meta3.getColumnCount();

                    if (rs3.next()) { // Get WIP details
                        WIPMaster.clear();
                        do {
                            final List<String> columnList = new LinkedList<String>();
                            WIPMaster.add(columnList);
                            for (int column = 1; column <= columnCount3; ++column) {
                                final Object value = rs3.getObject(column);
                                columnList.add(String.valueOf(value));
                            }
                        } while (rs3.next());
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
