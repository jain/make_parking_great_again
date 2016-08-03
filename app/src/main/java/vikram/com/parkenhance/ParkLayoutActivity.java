package vikram.com.parkenhance;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.provider.Settings.Secure;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ParkLayoutActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // volley stuff
    private RequestQueue queue;
    public static final String TAG = "MyTag";

    // layout
    private GridLayout grid;
    private int floor = 0;
    private LotData[][][] map3d;
    private Spinner spinner;
    private String android_id;
    private TextView name;
    private TextView[][] views;
    public AsyncHttpClient client = new AsyncHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.park_layout_main);
        android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
        grid = (GridLayout) findViewById(R.id.grid);
        spinner = (Spinner) findViewById(R.id.floor);
        name = (TextView) findViewById(R.id.plot_name);
    }

    public void help(View v) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.contact_owner, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(promptView);
        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        final TextView tView = (TextView) promptView.findViewById(R.id.textView);
        tView.setText("Help Message");
        builder.setPositiveButton("Request Help", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // FIRE ZE MISSILES!
                String msg = editText.getText().toString();
                final String add = "?id=" + Common.id + "&lot=" + Common.parkingLot.getName() + "&msg=" + msg;
                client.get(Common.BASEURL + Common.HELP_MESSAGE + add, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Toast.makeText(ParkLayoutActivity.this, "failed to contact server", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        Toast.makeText(ParkLayoutActivity.this, "Your request has been sent to us please be patient", Toast.LENGTH_LONG).show();
                    }
                });
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancellcancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        //return builder.create();
        builder.create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        name.setText(Common.parkingLot.getName());
        getParkingData();
    }

    private void resume() {
        String[] spinnerArray = new String[map3d.length];
        for (int i = 0; i < spinnerArray.length; i++) {
            spinnerArray[i] = "level " + i;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        if (floor != 0) {
            spinner.setSelection(floor);
        } else {
            genGrid();
        }
    }

    private void genGrid() {
        grid.removeAllViews();
        grid.invalidate();
        grid.setUseDefaultMargins(false);
        grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        grid.setRowOrderPreserved(false);
        grid.setBackgroundColor(Color.BLACK);
        int h = 500/map3d[floor].length;
        int w = 500/map3d[floor][0].length;

        //LotData[][] arr = map3d[floor];
        LotData[][] arr = new LotData[map3d[floor].length * 2 + 1][map3d[floor][0].length * 2 + 1];
        for (int i = 0; i < map3d[floor].length; i++) {
            for (int j = 0; j < map3d[floor][0].length; j++) {
                arr[1 + (2 * i)][1 + (2 * j)] = map3d[floor][i][j];
            }
        }
        views = new TextView[arr.length][arr[0].length];
        grid.setRowCount(arr.length);

        grid.setColumnCount(arr[0].length);
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                if (arr[i][j] != null) {
                    TextView gridView = new TextView(this);
                    gridView.setGravity(Gravity.CENTER);
                    //gridView.setTextSize(20);
                    gridView.setHeight(h);
                    gridView.setWidth(w);
                    //gridView.setPadding(10, 10, 10, 10);
                    if (arr[i][j].reservation.equals(Common.id)){
                        gridView.setBackgroundColor(Color.CYAN);
                        gridView.setText("R");
                    } else {
                        switch (arr[i][j].type) {
                            case 0: //free green
                                gridView.setBackgroundColor(Color.GREEN);
                                gridView.setText("E");
                                gridView.setClickable(true);
                                gridView.setOnClickListener(new ParkingSpaceClickListener(queue,
                                        new int[]{floor, (i - 1) / 2, (j - 1) / 2}, this, android_id, arr[i][j].group));
                                break;
                            case 1: //  reserved (android this is yellow)
                                gridView.setBackgroundColor(Color.YELLOW);
                                gridView.setText("R");
                                break;
                            case 2: // full
                                gridView.setBackgroundColor(Color.RED);
                                gridView.setText("F");
                                break;
                            case 10: // road
                                gridView.setBackgroundColor(Color.GRAY);
                                gridView.setText("- -");
                                //gridView.setBackgroundResource(R.drawable.right);
                                break;
                            case 20: // left
                                gridView.setBackgroundColor(Color.BLACK);
                                //gridView.setText("");
                                //gridView.setBackgroundResource(R.drawable.left);
                                break;
                        }
                    }

                    //gridView.setText(""+arr[i][j]);
                    views[i][j] = gridView;
                    grid.addView(gridView);
                } else {
                    TextView gridView = new TextView(this);
                    gridView.setGravity(Gravity.CENTER);
                    int width = 4;
                    int height = 4;
                    gridView.setWidth(width);
                    gridView.setHeight(height);
                    views[i][j] = gridView;
                    //gridView.setBackgroundColor(Color.BLACK);
                    grid.addView(gridView);
                }
            }
        }
        mergeGroups(arr, h, w);
        grid.postInvalidate();
    }

    private void mergeGroups(LotData[][] arr, int h, int w) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                if (arr[i][j] != null) {
                    LotData curr = arr[i][j];
                    try {
                        LotData up = arr[i - 2][j];
                        if (curr.type > 2) {
                            if (curr.type == up.type) {
                                TextView txt = views[i - 1][j];
                                txt.setWidth(w);
                                txt.setBackgroundColor(((ColorDrawable) views[i][j].getBackground()).getColor());
                            }
                        } else {
                            if (curr.group == up.group && curr.type == up.type) {
                                TextView txt = views[i - 1][j];
                                txt.setWidth(w);
                                txt.setBackgroundColor(((ColorDrawable) views[i][j].getBackground()).getColor());
                            }
                        }
                    } catch (Exception e) {

                    }
                    try {
                        LotData right = arr[i][j + 2];
                        if (curr.type > 2) {
                            if (curr.type == right.type) {
                                TextView txt = views[i][j + 1];
                                txt.setHeight(h);
                                txt.setBackgroundColor(((ColorDrawable) views[i][j].getBackground()).getColor());
                            }
                        } else {
                            if (curr.group == right.group && curr.type == right.type) {
                                TextView txt = views[i][j + 1];
                                txt.setHeight(h);
                                txt.setBackgroundColor(((ColorDrawable) views[i][j].getBackground()).getColor());
                            }
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                try{
                    int tl = (((ColorDrawable)views[i-1][j-1].getBackground()).getColor());
                    int tr = (((ColorDrawable)views[i-1][j+1].getBackground()).getColor());
                    int br = (((ColorDrawable)views[i+1][j+1].getBackground()).getColor());
                    int bl = (((ColorDrawable)views[i+1][j-1].getBackground()).getColor());
                    if (tl==tr&&tr==br&&bl==br){
                        views[i][j].setBackgroundColor(tl);
                    }
                } catch (Exception e){

                }
            }
        }
    }

    public void getParkingData() {
        queue = Volley.newRequestQueue(this);
        String url = Common.BASEURL + Common.PARKING_LOT_INFO + "?name=" + Common.parkingLot.getName();

        JsonObjectRequest lotRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject parkingLot) {
                        try {
                            JSONArray map = parkingLot.getJSONArray("map");
                            map3d = new LotData[map.length()][][];
                            for (int i = 0; i < map.length(); i++) {
                                JSONArray floor = map.getJSONArray(i);
                                map3d[i] = new LotData[floor.length()][];
                                for (int j = 0; j < floor.length(); j++) {
                                    JSONArray col = floor.getJSONArray(j);
                                    map3d[i][j] = new LotData[col.length()];
                                    for (int k = 0; k < col.length(); k++) {
                                        JSONObject spaceData = col.getJSONObject(k);
                                        LotData ld = new LotData(spaceData.getInt("group"), spaceData.getInt("type"), spaceData.getString("reservation"));
                                        map3d[i][j][k] = ld;
                                    }
                                }
                            }
                            resume();
                            //Toast.makeText(ParkLayoutActivity.this, "success", Toast.LENGTH_LONG).show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ParkLayoutActivity.this, "json error", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ParkLayoutActivity.this, "Volley Request Error" + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        queue.add(lotRequest);
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        floor = position;
        genGrid();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        floor = 0;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }
}
