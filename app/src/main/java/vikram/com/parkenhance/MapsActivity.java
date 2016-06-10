package vikram.com.parkenhance;

import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;
import android.location.LocationListener;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener {


    // volley stuff
    private RequestQueue queue;
    public static final String TAG = "MyTag";

    private Location location;

    private GoogleMap mMap;
    private Marker currLoc;
    private ArrayList<ParkingLot> lots;
    private HashMap<Marker, ParkingLot> markerListenerMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    protected void getNearbyLots() {
        queue = Volley.newRequestQueue(this);
        String url = Common.BASEURL + Common.NEAR + "?lat=" + location.getLatitude() + "&long=" + location.getLongitude();
        lots = new ArrayList<ParkingLot>();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject lot = new JSONObject(response);
                            ParkingLot pLot = new ParkingLot(lot.getString("status"), lot.getDouble("latitude"), lot.getDouble("longitude"));
                            lots.add(pLot);
                            addNearLots();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MapsActivity.this, "json error", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this, "Volley Request Error" + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        queue.add(stringRequest);
    }

    public void addNearLots(){
        mMap.clear();
        // add curr location marker first
        currLoc = mMap.addMarker(new MarkerOptions().position(currLoc.getPosition()));
        currLoc.setIcon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        markerListenerMap = new HashMap<Marker, ParkingLot>();
        for (ParkingLot pLot: lots){
            Marker pLotMarker = mMap.addMarker(new MarkerOptions().position(pLot.getLoc()));
            switch (pLot.getStatus()){
                case EMPTY:
                    currLoc.setIcon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    break;
                case FULL:
                    currLoc.setIcon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    break;
                case RELATIVELY_EMPTY:
                    currLoc.setIcon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    break;
                case RELATIVELY_FULL:
                    currLoc.setIcon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    break;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        if (currLoc != null) {
            currLoc.remove();
        }
        // mMap.clear()
        currLoc = mMap.addMarker(new MarkerOptions().position(latLng));
        currLoc.setIcon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        getNearbyLots();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(this, "cannot access GPS", Toast.LENGTH_LONG).show();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        try {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastKnownLocation != null) {
                onLocationChanged(lastKnownLocation);
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, this);
        } catch (SecurityException e) {
            Toast.makeText(this, "rejected security permission", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, "hey", Toast.LENGTH_LONG).show();
        return false; // to show options to navigate to place return false
    }
}
