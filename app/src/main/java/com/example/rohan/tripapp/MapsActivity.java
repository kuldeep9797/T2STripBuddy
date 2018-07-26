package com.example.rohan.tripapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
        , GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener,
        LocationListener , GetNearByPlacesData.AsyncResponse ,wikiFetch.WikiResponse
        ,TextToSpeech.OnInitListener

{
    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;

    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLocationMarker;
    public static final int REQUEST_LOCATION_CODE = 99;
    private int PROXIMITY_RADIUS = 10000;
    ToggleButton toggleButton = null;
    double latitude,longitute;
    String[] retPlace;
    private TextToSpeech tts;
    public String placeDes = null;
    public int position = 0;
    public int lenOfretPlace;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);




        tts = new TextToSpeech(this, this);
        tts.stop();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        tv = findViewById(R.id.tvPlaceName);

    }



    protected void onRequestPermissionResult(int requestCode, @NonNull String[] permissions
            , @NonNull int[] grantResults)
    {
        switch(requestCode)
        {
            case REQUEST_LOCATION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED)
                    {
                        if(client == null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else
                {
                    Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
                }
                return;
        }
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
        tts.stop();
        mMap = googleMap;
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED )
        {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        Toast.makeText(this,"map is ready",Toast.LENGTH_SHORT).show();

    }

    public void onClick(View v)
    {
        if(v.getId() == R.id.btnLoc)
        {
          nearbyloc();
        }
        if(v.getId() == R.id.btnNext)
        {
            nextLoc();
        }

        if(v.getId() == R.id.btnPrev)
        {
            prevLoc();

        }
    }
    public void prevLoc()
    {
        tts.stop();
        position--;
        if(position < 0)
        {
            Toast.makeText(this,"this is the first location",Toast.LENGTH_LONG).show();
            position = 0;

        }
        Object wiki[] = new Object[2];
        wiki[0] = retPlace[position];
        wikiFetch w = new wikiFetch();
        w.wik = (wikiFetch.WikiResponse) this;
        w.execute(wiki);

    }
    public void nextLoc()
    {
        tts.stop();
        position++;
        if(position > lenOfretPlace)
        {
            Toast.makeText(this,"this is the last location",Toast.LENGTH_LONG).show();
            position = lenOfretPlace;
        }
        Object wiki[] = new Object[2];
        wiki[0] = retPlace[position];
        wikiFetch w = new wikiFetch();
        w.wik = (wikiFetch.WikiResponse) this;
        w.execute(wiki);

    }
    public void nearbyloc()
    {


                String url;
                Object dataTransfer[]= new Object[2];

                String nearby = "";
                url = getUrl(latitude,longitute,nearby);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                GetNearByPlacesData getNearByPlacesData = new GetNearByPlacesData();
                getNearByPlacesData.res = (GetNearByPlacesData.AsyncResponse) this;
                getNearByPlacesData.execute(dataTransfer);

    }


        @Override
        public void retWiki(String arr)
        {
            tts.stop();
            placeDes = arr;
           if(arr.equals("wikinofound"))
           {
               Toast.makeText(this,"Place description not found , please select new location"
                       ,Toast.LENGTH_LONG).show();
               placeDes = "Place description not found , please select new location";
                tv.setText(placeDes);
               t2s();
           }
           else
           {
               Toast.makeText(this,placeDes,Toast.LENGTH_LONG).show();
               tv.setText(placeDes);
               t2s();
           }
        }


    private void t2s() {
        tts.speak(placeDes, TextToSpeech.QUEUE_FLUSH, null);

    }


    @Override
    public void retPlaces(String[] arr){
        retPlace = arr;
        lenOfretPlace = retPlace.length;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < retPlace.length; i++) {
            str = str.append( retPlace[i] + " , ");
        }
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        Object wiki[] = new Object[2];
        wiki[0] = retPlace[position];
        wikiFetch w = new wikiFetch();
        w.wik = (wikiFetch.WikiResponse) this;
        w.execute(wiki);

    }


    private String getUrl(double latitude,double longitude,String nearbyPlace)
    {
        StringBuilder googlePlaceUrl
                = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type="+nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key="+"AIzaSyCJH8t2V98-3ULwhB91InwB_Y43FslLehg");

        return googlePlaceUrl.toString();
    }

    protected synchronized void buildGoogleApiClient()
    {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitute = location.getLongitude();
        lastLocation = location;
            if(currentLocationMarker != null)
            {
                currentLocationMarker.remove();
            }
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("current location");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            currentLocationMarker = mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));

            if(client != null)
            {
                LocationServices.FusedLocationApi.removeLocationUpdates(client,this);
            }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);



        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(client, locationRequest, this);
        }
    }


    public boolean checkLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale
                    (this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this,new String[]
                        {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[]
                        {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_CODE);
            }
            return false;
        }
        else
            return true;
    }


    @Override
    public void onInit(int status) {


        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                t2s();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}
