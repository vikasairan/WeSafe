package com.example.womensafety;

import android.annotation.SuppressLint;
import android.hardware.camera2.CameraAccessException;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.womensafety.models.Myplaces;
import com.example.womensafety.models.Remote.GoogleAPIService;
import com.example.womensafety.models.Results;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private double latitude,longitude;
    private Location mLastLocation;
    private Marker mMarker;
    private LocationRequest mLocationRequest;

    GoogleAPIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mService = Common.getGoogleAPIService();

        BottomNavigationView bottomnavigatinview = (BottomNavigationView)findViewById(R.id.bottom_navigation_view);
        bottomnavigatinview.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.action_hospital:
                        NearbyPlace("Hospital");
                        break;
                    case R.id.action_atm:
                        NearbyPlace("atm");
                        break;
                    case R.id.action_police_station:
                        NearbyPlace("Police_station");
                        break;
                    case R.id.action_gas_station:
                        NearbyPlace("Petrol_pump");
                        break;
                    default:
                        break;

                }
            }


        });
    }
    private void NearbyPlace(final String placetype) {
        mMap.clear();
        String url = getUrl(latitude,longitude,placetype);

        mService.getNearbyPlaces(url)
                .enqueue(new Callback<Myplaces>() {
                    @Override
                    public void onResponse(Call<Myplaces> call, Response<Myplaces> response) {

                        if(response.isSuccessful()){

                            for(int i=0;i<response.body().getResults().length;i++){
                                MarkerOptions markerOptions = new MarkerOptions();
                                Results googlePlace = response.body().getResults()[i];
                                double lat = Double.parseDouble(googlePlace.getGeometry().getLocation().getLat());
                                double lng = Double.parseDouble(googlePlace.getGeometry().getLocation().getLng());
                                String placename = googlePlace.getName();
                                String vicinity = googlePlace.getVicinity();
                                LatLng latlng = new LatLng(lat,lng);

                                markerOptions.position(latlng);
                                markerOptions.title(placename);

                                if(placetype.equals("Hospital"))
                                { markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_hospital));}
                                else if(placetype.equals("atm"))
                                { markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_atm));}
                                else if(placetype.equals("Police_station"))
                                { markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_police_station));}
                                else if(placetype.equals("Petrol_pump"))
                                {  markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_gas_station));}
                                else
                                {  markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));}

                                mMap.addMarker(markerOptions);

                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                                mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Myplaces> call, Throwable t) {

                    }
                });

    }

    private String getUrl(double latitude, double longitude, String placetype) {
        StringBuilder googleplacesurl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleplacesurl.append("location="+latitude+","+longitude);
        googleplacesurl.append("&radius="+2000);
        googleplacesurl.append("&type="+placetype);
        googleplacesurl.append("&sensor=true");
        googleplacesurl.append("&key="+getResources().getString(R.string.browser_key));
        Log.d("getURl",googleplacesurl.toString());
        return googleplacesurl.toString();
    }

    @SuppressLint("MissingPermission")


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if(mMarker!=null)
            mMarker.remove();

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latlng = new LatLng(latitude,longitude);
        MarkerOptions markeroptions = new MarkerOptions()
                .position(latlng)
                .title("Your Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mMarker = mMap.addMarker(markeroptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

        if(mGoogleApiClient != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        }
    }
}
