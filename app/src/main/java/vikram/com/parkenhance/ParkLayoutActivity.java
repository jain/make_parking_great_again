package vikram.com.parkenhance;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ParkLayoutActivity extends AppCompatActivity {

    // volley stuff
    private RequestQueue queue;
    public static final String TAG = "MyTag";

    // layout
    private GridLayout grid;
    private int floor;
    private int[][][] map3d;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.park_layout_main);
        grid = (GridLayout) findViewById(R.id.grid);
    }

    @Override
    protected void onResume(){
        super.onResume();
        getParkingData();
        floor = 1; // for now
    }

    private void genGrid() {
        grid.invalidate();
        int[][] arr = map3d[floor];
        grid.setRowCount(arr.length);
        grid.setColumnCount(arr[0].length);
        for (int i = 0; i<arr.length; i++){
            for (int j = 0; j<arr[i].length; j++){
                TextView gridView = new TextView(this);
                gridView.setGravity(Gravity.CENTER);
                gridView.setTextSize(20);
                gridView.setMinHeight(100);
                gridView.setMinWidth(100);
                switch (arr[i][j]){
                    case 0: //free green
                        gridView.setBackgroundColor(Color.GREEN);
                        break;
                    case 1: //  reserved (android this is yellow)
                        gridView.setBackgroundColor(Color.YELLOW);
                        break;
                    case 2: // full
                        gridView.setBackgroundColor(Color.RED);
                        break;
                    case 10: // right
                        gridView.setText("→");
                        //gridView.setBackgroundResource(R.drawable.right);
                        break;
                    case 11: // left
                        gridView.setText("←");
                        //gridView.setBackgroundResource(R.drawable.left);
                        break;
                    case 12: // left + right
                        gridView.setText("↔");
                        break;
                    case 13: // up
                        gridView.setText("↑");
                        //gridView.setBackgroundResource(R.drawable.up);
                        break;
                    case 14: // down
                        gridView.setText("↓");
                        //gridView.setBackgroundResource(R.drawable.down);
                        break;
                    case 15:
                        gridView.setText("↕");
                        break;
                }

                //gridView.setText(""+arr[i][j]);
                grid.addView(gridView);
            }
        }
        grid.postInvalidate();
    }

    public void getParkingData() {
        queue = Volley.newRequestQueue(this);
        String url = "https://parkenhance.firebaseio.com//.json?print=pretty";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject js = new JSONObject(response);
                            JSONObject lot1 = js.getJSONObject("lot1");
                            JSONArray map = lot1.getJSONArray("map");
                            map3d = new int[map.length()][][];
                            for (int i=0;i<map.length();i++){
                                JSONArray floor = map.getJSONArray(i);
                                map3d[i] = new int[floor.length()][];
                                for (int j = 0; j<floor.length(); j++){
                                    JSONArray col = floor.getJSONArray(j);
                                    map3d[i][j] = new int[col.length()];
                                    for (int k = 0; k<col.length(); k++){
                                        map3d[i][j][k] = col.getInt(k);
                                    }
                                }
                            }
                            genGrid();
                            Toast.makeText(ParkLayoutActivity.this, "success", Toast.LENGTH_LONG).show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ParkLayoutActivity.this, "json error", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ParkLayoutActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        queue.add(stringRequest);
    }

    @Override
    public void onPause(){
        super.onPause();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }
}
