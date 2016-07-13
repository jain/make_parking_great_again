package vikram.com.parkenhance;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.location.LocationListener;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.braintreepayments.api.BraintreePaymentActivity;
import com.braintreepayments.api.PaymentRequest;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.design.widget.NavigationView;
import android.content.res.Configuration;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener, NavigationView.OnNavigationItemSelectedListener{


    // volley stuff
    private RequestQueue queue;
    public static final String TAG = "MyTag";

    private Location location;

    private GoogleMap mMap;
    private Marker currLoc;
    private ArrayList<ParkingLot> lots;
    private HashMap<String, ParkingLot> markerListenerMap;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;

    private AsyncHttpClient client = new AsyncHttpClient();
    private String clientToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);

        mActivityTitle = getTitle().toString();
        setupDrawer();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getClientToken();
    }

    private void getClientToken() {
        client.get(Common.BASEURL + Common.CLIENT_TOKEN, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(MapsActivity.this, "failed to access payment info", Toast.LENGTH_LONG).show();
                getClientToken();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                clientToken = responseString;

            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Navigation!");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (item.getTitle().toString().toLowerCase().contains("payment")){
            if(clientToken.isEmpty()){
                PaymentRequest paymentRequest = new PaymentRequest()
                        .clientToken(clientToken)
                        .amount("$10.00")
                        .primaryDescription("Awesome payment")
                        .secondaryDescription("Using the Client SDK")
                        .submitButtonText("Pay");

                startActivityForResult(paymentRequest.getIntent(this), 1);
            } else {
                Toast.makeText(MapsActivity.this, "Payment Err" , Toast.LENGTH_LONG).show();
            }

        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == BraintreePaymentActivity.RESULT_OK) {
            /*PaymentMethodNonce paymentMethodNonce = data.getParcelableExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE);

            RequestParams requestParams = new RequestParams();
            requestParams.put("payment_method_nonce", paymentMethodNonce.getNonce());
            requestParams.put("amount", "10.00");

            client.post(SERVER_BASE + "/payment", requestParams, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Toast.makeText(SDKActivity.this, responseString, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    Toast.makeText(SDKActivity.this, responseString, Toast.LENGTH_LONG).show();
                }
            });*/
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        queue = Volley.newRequestQueue(this);

    }
    // testPost();
    /*protected void testPost() {
        String url = Common.BASEURL + "hi";
        HashMap<String, ArrayList<String>> ohno = new HashMap<String, ArrayList<String>>();
        ArrayList<String> l = new ArrayList<String>();
        l.add("hi");
        l.add("test");
        ohno.put("testCase", l);
        JSONObject js = new JSONObject(ohno);
        JsonObjectRequest nearbyRequest = new JsonObjectRequest(Request.Method.POST, url, js,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject pLots) {
                        Toast.makeText(MapsActivity.this, "swag", Toast.LENGTH_LONG).show();
                        //JSONObject pLots = new JSONObject(resp);
                        Iterator<String> it = pLots.keys();
                        while (it.hasNext()) {
                            String key = it.next();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this, "Volley Request Error" + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        queue.add(nearbyRequest);
    }*/

    // http://arnab.ch/blog/2013/08/asynchronous-http-requests-in-android-using-volley/
    protected void getNearbyLots() {

        String url = Common.BASEURL + Common.NEAR + "?lat=" + location.getLatitude() + "&long=" + location.getLongitude();
        lots = new ArrayList<ParkingLot>();
        JsonObjectRequest nearbyRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject pLots) {
                        try {

                            Iterator<String> it = pLots.keys();

                            while (it.hasNext()) {
                                String key = it.next();
                                JSONObject lot = pLots.getJSONObject(key);
                                ParkingLot pLot = new ParkingLot(lot.getString("status"),
                                        lot.getDouble("latitude"), lot.getDouble("longitude"), key);
                                lots.add(pLot);
                            }

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
        queue.add(nearbyRequest);
    }

    public void addNearLots() {
        markerListenerMap = new HashMap<String, ParkingLot>();
        for (ParkingLot pLot : lots) {
            Marker pLotMarker = mMap.addMarker(new MarkerOptions().position(pLot.getLoc()));
            switch (pLot.getStatus()) {
                case EMPTY:
                    pLotMarker.setIcon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    break;
                case FULL:
                    pLotMarker.setIcon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    break;
                case RELATIVELY_EMPTY:
                    pLotMarker.setIcon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    break;
                case RELATIVELY_FULL:
                    pLotMarker.setIcon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    break;
            }
            pLotMarker.setTitle(pLot.getName());
            markerListenerMap.put(pLotMarker.getTitle(), pLot);
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
        mMap.clear();
        currLoc = mMap.addMarker(new MarkerOptions().position(latLng));
        currLoc.setTitle("My Location");
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

        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        try {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastKnownLocation != null) {
                onLocationChanged(lastKnownLocation);
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100000, 0, this);
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
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (markerListenerMap.containsKey(marker.getTitle())){
            ParkingLot pLot = markerListenerMap.get(marker.getTitle());
            if (pLot.getStatus()== ParkingLot.STATUS.FULL){
                Toast.makeText(this, "This parking lot is full", Toast.LENGTH_LONG).show();
            } else {
                Common.parkingLot = pLot;
                MarkerDialog dialog = new MarkerDialog(this, pLot);
                dialog.show();
            }
        } else {
            Toast.makeText(this, "This is your location", Toast.LENGTH_LONG).show();
        }
        return false; // to show options to navigate to place return false
    }
}
