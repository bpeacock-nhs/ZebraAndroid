package nhstayside.pss_ss_android;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private GridView loginGrid;
    ConnectionClass connectionClass;
    final List<List<String>> rowList = new LinkedList<List<String>>();
    String initials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginGrid = findViewById(R.id.loginGrid);
        loginGrid.setNumColumns(3);

        connectionClass = new ConnectionClass();

        mSwipeRefreshLayout = findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        GetUsers getUsers = new GetUsers();
        getUsers.execute("");
    }

    public void onRefresh() {
        GetUsers getUsers = new GetUsers();
        getUsers.execute("");
    }

    public class GetUsers extends AsyncTask<String, String, String> {
        String z = "";
        ResultSet rs;

        @Override
        protected void onPreExecute() {
            mSwipeRefreshLayout.setRefreshing(true);
        }

        protected void onPostExecute(String r) {
            mSwipeRefreshLayout.setRefreshing(false);
            if (r != "")
            Toast.makeText(MainActivity.this, r, Toast.LENGTH_LONG).show();
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_item) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = view.findViewById(android.R.id.text1);
                    text.setTextColor(Color.WHITE);
                    text.setTextSize(30);
                    text.setTextAlignment(view.TEXT_ALIGNMENT_CENTER);
                    text.setShadowLayer(5,1,1, Color.BLACK);
                    view.setBackgroundColor(0xFF3F51B5);
                    return view;
                }
            };

            for (int i = 0; i < rowList.size(); i++) {
                adapter.add(rowList.get(i).get(1));
            }
            loginGrid.setAdapter(adapter);
            loginGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    initials = ((TextView) view).getText().toString();
                    String status = "";

                    GetUsers getUsers = new GetUsers();
                    getUsers.execute("");

                    for (int i = 0; i < rowList.size(); i++) {
                        if (rowList.get(i).get(1) == initials) {
                            status = rowList.get(i).get(2);
                        }
                    }
                    if (!status.equals("NEW")) {
                        Intent i = new Intent(MainActivity.this, DelnoteActivity.class);
                        i.putExtra("initials", initials);
                        i.putExtra("status", status);
                        startActivity(i);
                    } else {
                        Intent i = new Intent(MainActivity.this, SubmainActivity.class);
                        i.putExtra("initials", initials);
                        startActivity(i);
                    }
                }
            });
        }

        @Override
        protected String doInBackground(String... params) {
            Connection con = connectionClass.CONN();
            if (con == null) {
                z = "Error in connection with SQL server.";
            } else {
                String query = "select * from [DN]";
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
                        z = "No users available.";
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

