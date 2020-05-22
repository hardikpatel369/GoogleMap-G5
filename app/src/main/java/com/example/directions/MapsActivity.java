package com.example.directions;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, PlacesAutoCompleteAdapter.ClickListener {
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private static final int LOCATION_REQUEST = 500;
    Location currentLocation;
    LatLng currentLatLng;
    private static final int LOCATION_REQUEST_CODE = 101;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private int ProximityRadius = 5000;
    EditText place_search;
    private PlacesAutoCompleteAdapter mAutoCompleteAdapter;
    private RecyclerView recyclerView;
    String lat, lang;
    TextView tv_save, tv_show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        PlacesClient mPlacesClient = Places.createClient(this);

        tv_save = findViewById(R.id.tv_save);
        tv_show = findViewById(R.id.tv_show);
        recyclerView = findViewById(R.id.places_recycler_view);
        place_search = findViewById(R.id.place_search);
        place_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            addCourseFromTextBox();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        tv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnSaveClick();
            }
        });

        tv_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnShowClick();
            }
        });

        mAutoCompleteAdapter = new PlacesAutoCompleteAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAutoCompleteAdapter.setClickListener(this);
        recyclerView.setAdapter(mAutoCompleteAdapter);
        mAutoCompleteAdapter.notifyDataSetChanged();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fetchLastLocation(null);
    }

    private void addCourseFromTextBox() {
        String place = place_search.getText().toString();

        if (!place.equals("")) {
            mAutoCompleteAdapter.getFilter().filter(place);
            if (recyclerView.getVisibility() == View.GONE) {
                recyclerView.setVisibility(View.VISIBLE);
            }
        } else {
            if (recyclerView.getVisibility() == View.VISIBLE) {
                recyclerView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return;
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.clear();
                Log.d("onMapClick", latLng.latitude + "-" + latLng.longitude);
                LatLng latLng1 = new LatLng(latLng.latitude, latLng.longitude);
                mMap.addMarker(new MarkerOptions().position(latLng1).title("My Click Position"));

                lat = String.valueOf(latLng.latitude);
                lang = String.valueOf(latLng.longitude);
            }
        });
    }

    public void nearByPlaces(View v) {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View yourCustomView = inflater.inflate(R.layout.choose_place, null);
        final RadioGroup radioGroup = yourCustomView.findViewById(R.id.radiogroup);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Choose place")
                .setView(yourCustomView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mMap.clear();
                        String place = null;
                        int id = radioGroup.getCheckedRadioButtonId();
                        if (id == R.id.Restaurants)
                            place = "restaurant";
                        else if (id == R.id.Coffee)
                            place = "cafe";
                        else if (id == R.id.Bars)
                            place = "bar";
                        else if (id == R.id.Gyms)
                            place = "gym";
                        else if (id == R.id.Art)
                            place = "art_gallery";
                        else if (id == R.id.Nightlife)
                            place = "night_club";
                        else if (id == R.id.Libraries)
                            place = "library";
                        else if (id == R.id.Movies)
                            place = "movie_theater";
                        else if (id == R.id.Museum)
                            place = "museum";
                        else if (id == R.id.Groceries)
                            place = "supermarket";
                        else if (id == R.id.shopping_centes)
                            place = "shopping_mall";
                        else if (id == R.id.School)
                            place = "school";
                        else if (id == R.id.Hospital)
                            place = "hospital";
                        else if (id == R.id.Gas)
                            place = "gas_station";
                        else if (id == R.id.ATMs)
                            place = "atm";
                        new GetNearbyPlaces().execute(mMap, getUrl(currentLatLng.latitude, currentLatLng.longitude, place), place);
                    }
                })
                .setNegativeButton("Cancel", null).create();
        dialog.show();
    }

    private String getUrl(double latitide, double longitude, String nearbyPlace) {
        StringBuilder googleURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleURL.append("location=" + latitide + "," + longitude);
        googleURL.append("&radius=" + ProximityRadius);
        googleURL.append("&type=" + nearbyPlace);
        googleURL.append("&sensor=true");
        googleURL.append("&key=" + "AIzaSyBB3AJPtJxMGvVT5J-BmVcvc41u2O6hSN4");
        Log.d("GoogleMapsActivity", "url = " + googleURL.toString());
        return googleURL.toString();
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public void fetchLastLocation(final View v) {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();
        } else {
            showGPSDisabledAlertToUser();
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    mMap.clear();
                    currentLocation = location;
                    LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    currentLatLng = latLng;
                    if (v != null) {
                        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                            mMap.setMyLocationEnabled(true);
                        }
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                        Toast.makeText(MapsActivity.this, currentLocation.getLatitude() + " " + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MapsActivity.this, "No Location recorded", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
                break;
        }
    }

    @Override
    public void click(Place place) {
        Toast.makeText(this, place.getAddress() + ", " + place.getLatLng().latitude + place.getLatLng().longitude, Toast.LENGTH_SHORT).show();
        Log.d("--abc--", "click: " + place.getAddress() + ", " + place.getLatLng().latitude + place.getLatLng().longitude);

        String lat = "Latitude : " + place.getLatLng().latitude;
        String lang = "Longitude : " + place.getLatLng().longitude;
        String address1 = "Address : " + place.getAddress();

        if (recyclerView.getVisibility() == View.VISIBLE) {
            recyclerView.setVisibility(View.GONE);
        }

        LatLng latLng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latLng.latitude, latLng.longitude)).zoom(16).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.addMarker(new MarkerOptions().position(latLng).title(place.getAddress()));

        mapFragment.getMapAsync(this);
    }

    public void OnShowClick() {
        Toast.makeText(this, "SHOW", Toast.LENGTH_SHORT).show();
        SharedPreferences sp = getSharedPreferences("ClickPosition", MODE_PRIVATE);
        double lat = Double.parseDouble(sp.getString("Latitude", "Not found"));
        double lang = Double.parseDouble(sp.getString("Longitude", "Not found"));

        LatLng latLng1 = new LatLng(lat, lang);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng1, 12));
        mMap.addMarker(new MarkerOptions().position(latLng1).title("My Click Position"));
    }

    public void OnSaveClick() {
        Toast.makeText(this, "SAVE", Toast.LENGTH_SHORT).show();
        SharedPreferences sharedPreferences = getSharedPreferences("ClickPosition", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Latitude", lat);
        editor.putString("Longitude", lang);
        editor.apply();
    }
}