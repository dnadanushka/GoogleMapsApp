package com.example.mapsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mapsapp.directionhelpers.FetchURL;
import com.example.mapsapp.directionhelpers.TaskLoadedCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener , GoogleApiClient.ConnectionCallbacks, TaskLoadedCallback {

    private static final String TAG = "MapActivy";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_COODE = 1234;
    private static final int START_PLACE = 0;
    private static final int END_PLACE = 1;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS= new LatLngBounds(
            new LatLng(-40,-168),new LatLng(71 , 136)
    );


    private boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mfusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private Location mcurrentLocation;


    private MarkerOptions place1, place2;
    private Polyline currentPolyline;


    //widgets
    private AutoCompleteTextView mStartText;
    //widgets
    private AutoCompleteTextView mEndText;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mStartText = (AutoCompleteTextView) findViewById(R.id.input_search);
        mEndText = (AutoCompleteTextView) findViewById(R.id.inupt_search_1);


        getLocationPermition();
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this,this)
                .build();
        init();
    }

    private void init(){
        Log.d(TAG, "init: ");







        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(MapActivity.this,mGoogleApiClient,LAT_LNG_BOUNDS,null);
        mStartText.setAdapter(mPlaceAutocompleteAdapter);
        mStartText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_SEARCH
                || i == EditorInfo.IME_ACTION_DONE
                || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){
                    Log.d(TAG, "onEditorAction: asdfa");
                    //execute the method for seaching
                    geolocate(START_PLACE);
                    return true;

                }
                return false;
            }
        });

        mEndText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_SEARCH
                        || i == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){
                    Log.d(TAG, "onEditorAction: asdfa");
                    //execute the method for seaching
                    geolocate(END_PLACE);
                    return true;

                }
                return false;
            }
        });
    }


    private void geolocate(int startOrEnd) {

        Address address;

        Log.d(TAG, "geolocate: ");
        String searchString;
        if(startOrEnd == START_PLACE){
            searchString = mStartText.getText().toString();
        }else{
            searchString = mEndText.getText().toString();
        }

        String startString = mStartText.getText().toString();
        String endString = mEndText.getText().toString();


        Geocoder geocoder = new Geocoder(MapActivity.this);
        //start address
        List<Address> list = new ArrayList<Address>();
        try{
            list =geocoder.getFromLocationName(startString,10);
            
        }catch(IOException e){
            Log.e(TAG, "geolocate: errror" + e.getMessage());
        }

        if(list.size() > 0) {
            address = list.get(0);
            for (int i = 0; i < list.size(); i++) {
                Log.d(TAG, "geolocate: locka" + list.get(i));
            }

            place1 = new MarkerOptions().position(new LatLng(address.getLatitude(), address.getLongitude())).title("Location 1");
            if(place2 != null){
                Log.d(TAG, "geolocate: onePhase");
                mMap.addMarker(place1);
                mMap.addMarker(place2);
                new FetchURL(MapActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(), "driving"), "driving");
                moveCamera(new LatLng(place2.getPosition().latitude , place2.getPosition().longitude),DEFAULT_ZOOM, address.getAddressLine(0));

            }
        }

        //end address
        list = new ArrayList<Address>();
        try{
            list =geocoder.getFromLocationName(endString,10);

        }catch(IOException e){
            Log.e(TAG, "geolocate: errror" + e.getMessage());
        }

        if(list.size() > 0) {
            address = list.get(0);
            for (int i = 0; i < list.size(); i++) {
                Log.d(TAG, "geolocate: locka" + list.get(i));
            }

            place2 = new MarkerOptions().position(new LatLng(address.getLatitude(), address.getLongitude())).title("Location 2");
            if(place1 != null){
                Log.d(TAG, "geolocate: twoPhse");
                mMap.addMarker(place1);
                mMap.addMarker(place2);
                new FetchURL(MapActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(), "driving"), "driving");
                moveCamera(new LatLng(place2.getPosition().latitude , place2.getPosition().longitude),DEFAULT_ZOOM, address.getAddressLine(0));

            }
        }


            //Toast.makeText(this,address.toString(),Toast.LENGTH_SHORT).show();
           // moveCamera(new LatLng(place2.getPosition().latitude , place2.getPosition().longitude),DEFAULT_ZOOM, address.getAddressLine(0));
    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            if(mLocationPermissionGranted){
                Task location = mfusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: Found locaton");
                            Location currentLocation = (Location) task.getResult();
                            mcurrentLocation = currentLocation;

                            moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),DEFAULT_ZOOM,"Current location");

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this,"Unable to find the current location",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: "+ e.getMessage());
        }
    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getLocationPermition(){
        String[] permisions = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};


        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,permisions,LOCATION_PERMISSION_REQUEST_COODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,permisions,LOCATION_PERMISSION_REQUEST_COODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_COODE:{
                if(grantResults.length > 0){
                    for(int i = 0 ; i< grantResults.length ; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted = false;
                            return;
                        }

                    }
                    mLocationPermissionGranted = true;
                    //innitialize the map
                    initMap();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is redy");
        Toast.makeText(MapActivity.this,"Map is ready",Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        // Add polylines and polygons to the map. This section shows just
        // a single polyline. Read the rest of the tutorial to learn more.


        if(mLocationPermissionGranted){
            getDeviceLocation();

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            init();
        }
    }

    private void moveCamera(LatLng latLng,float zoom,String title){
        Log.d(TAG, "moveCamera: Moving the camera to " + latLng.latitude + " " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + "AIzaSyBncwDnow_xVKigauV16yUAFu99kGo5-hQ";
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        Log.d(TAG, "onTaskDone: gotPath");
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }
}
