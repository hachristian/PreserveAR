package edu.uark.kacounts.preservear.PhotoActivity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import edu.uark.kacounts.preservear.Data.PhotoDataSource;
import edu.uark.kacounts.preservear.Data.PhotoRepository;
import edu.uark.kacounts.preservear.PhotoActivity.TakePhotoActivity;
import edu.uark.kacounts.preservear.R;
import edu.uark.kacounts.preservear.databinding.ActivityMapsBinding;
import edu.uark.kacounts.preservear.experienceActivity;
import util.AppExecutors;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean requestingLocationUpdates;
    private LocationCallback locationCallback;
    static Double Longitude;
    static Double Latitude;
    int count;
    private PhotoDataSource model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = PhotoRepository.getInstance(new AppExecutors(),getApplicationContext());
        Longitude = 0.0;
        Latitude = 0.0;
        count = 1;

        requestingLocationUpdates = true;

        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION, false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                            } else {
                                // No location access granted.
                            }
                        }
                );

        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    Log.d("MapsActivity", "" + location.getLatitude() + ":" + location.getLongitude());
                    Longitude = location.getLongitude();
                    Latitude = location.getLatitude();
                }
            }
        };

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            if (mMap != null) {
                                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                                mMap.setMyLocationEnabled(true);
                            }
                            Log.d("MapsActivity", "" + location.getLatitude() + ":" + location.getLongitude());
                            // Logic to handle location object
                            try {
                                Longitude = location.getLongitude();
                                Latitude = location.getLatitude();
                            } catch (Exception e) {

                            }
                        }
                    }
                });
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }

    }

    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }


    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(createLocationRequest(),
                locationCallback,
                Looper.getMainLooper());
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

        //Add a marker in Sydney and move the camera
        mMap.setOnMarkerClickListener(this);
        LatLng BATTLEFIELDPARK = new LatLng(35.98326, -94.30998);
        mMap.addMarker(new MarkerOptions().position(BATTLEFIELDPARK).title("Battlefield park"));

        LatLng battle2 = new LatLng(35.98336, -94.31032);
        mMap.addMarker(new MarkerOptions().position(battle2).title("Battle of prairie grove"));

        LatLng battle3 = new LatLng(35.98353, -94.31040);
        mMap.addMarker(new MarkerOptions().position(battle3).title("March of the armies"));
    }

    public void takePic(View v) {
        Intent photoIntent = new Intent();
        photoIntent.setClass(this, TakePhotoActivity.class);
        startActivity(photoIntent);
        setMarker();
    }

    //sets marker on the map and makes it clickable
    public void setMarker() {
        mMap.setOnMarkerClickListener(this);
        Marker temp = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(Latitude, Longitude))
                .title(Integer.toString(count)));
        temp.setTag(count);
        count++;
    }

    /**
     * Called when the user clicks a marker.
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        // Retrieve the data from the marker.
        Intent markerIntent = new Intent();
        markerIntent.setClass(this, experienceActivity.class);
        markerIntent.putExtra("id", count);
        markerIntent.putExtra("name", marker.getTitle());
        startActivity(markerIntent);

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }
}