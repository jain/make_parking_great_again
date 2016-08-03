package vikram.com.parkenhance;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by vikram on 6/28/16.
 */
public class ParkingSpaceClickListener implements View.OnClickListener {
    private RequestQueue queue;
    private int[] position;
    private ParkLayoutActivity act;
    private String android_id;
    private int group;
    public ParkingSpaceClickListener(RequestQueue queue, int[] pos, ParkLayoutActivity act, String android_id, int group) {
        super();
        this.queue = queue;
        position = pos;
        this.act = act;
        this.android_id = android_id;
        this.group = group;
    }

    @Override
    public void onClick(View view) {
        final TextView txtView = (TextView) view;
        String url = Common.BASEURL + Common.RESERVE_SPOT;
        HashMap <String, String> spot = new HashMap<>();
        spot.put("android_id", Common.id);
        spot.put("group", ""+group);
        //spot.put("position", position);
        spot.put("name", Common.parkingLot.getName());
        JSONObject js = new JSONObject(spot);
        JSONArray pos = new JSONArray();
        pos.put(position[0]);
        pos.put(position[1]);
        pos.put(position[2]);
        try {
            js.put("position", pos);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest nearbyRequest = new JsonObjectRequest(Request.Method.POST, url, js,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //txtView.setText("R");
                        //txtView.setBackgroundColor(Color.YELLOW);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        act.getParkingData();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(act, "Volley Request Error" + error.getMessage(), Toast.LENGTH_LONG).show();
                Toast.makeText(act, "Clear Your Existing Reservations", Toast.LENGTH_LONG).show();
            }
        });
        queue.add(nearbyRequest);
    }
}
