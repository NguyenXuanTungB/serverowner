package com.example.htc.mapmodule;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.htc.mapmodule.DatabaseTool.datasource;
import com.example.htc.mapmodule.R;
import com.example.htc.mapmodule.pathtomove.HttpConnection;
import com.example.htc.mapmodule.pathtomove.PathJSONParser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity {
    public datasource data=null;

    private static final LatLng BROOKLYN_BRIDGE = new LatLng(21.006213, 105.842824);
    private static final LatLng WALL_STREET = new LatLng(40.7064, -74.0094);
    ProgressDialog progressDialog;
    String des=null;
    GoogleMap googleMap;
    final String TAG = "PathGoogleMapActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        progressDialog= new ProgressDialog(this);
        progressDialog.setTitle("dang tai map");
        progressDialog.setMessage("xin vui long doi 1 chut");
        progressDialog.setCancelable(true);

        data= new datasource(this);
        try {
            data.opendatabase();
        } catch (SQLException e) {
            Toast.makeText(this, "sai roi", Toast.LENGTH_LONG).show();
        }
        //lay id vi tri tu co so du lieu, lam 1 truong trong bang nha hang(hien tai chua co)
        des= data.getdata("place1");
        data.closedatabase();
        googleMap = fm.getMap();
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                progressDialog.dismiss();
            }
        });
        googleMap.setMyLocationEnabled(true);
        String url = getMapsApiDirectionsUrl();
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);
        LocationManager locationManager= (LocationManager)getSystemService(LOCATION_SERVICE);
        Criteria criteria= new Criteria();
        Location location= locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        //LatLng mylaLatLng = new LatLng(21.006213,  105.842824);
        if(location!= null) {
            LatLng mylaLatLng = new LatLng(location.getLatitude(),location.getLongitude());

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylaLatLng, 13));
            CameraPosition cameraPosition= new CameraPosition.Builder()
                    .target(mylaLatLng).tilt(40).bearing(90).zoom(15).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            //    addMarkers();
            MarkerOptions options1= new MarkerOptions();
            options1.title(des);
            options1.position(mylaLatLng);
            options1.snippet("this's cool");
            googleMap.addMarker(new MarkerOptions().position(mylaLatLng).title(des));
        }
    }

    private String getMapsApiDirectionsUrl() {


        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=hanoi&destination=" +des+
                "&sensor=true";

        return url;
    }



    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(2);
                polyLineOptions.color(Color.BLUE);
            }

            googleMap.addPolyline(polyLineOptions);
        }
    }
}