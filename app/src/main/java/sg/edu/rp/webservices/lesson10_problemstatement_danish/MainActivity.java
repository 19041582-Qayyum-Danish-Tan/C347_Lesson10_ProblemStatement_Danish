package sg.edu.rp.webservices.lesson10_problemstatement_danish;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileWriter;

public class MainActivity extends AppCompatActivity {
    Button btnStartDetector, btnStopDetector, btnRecords;
    TextView tvLatitude, tvLongitude;
    private GoogleMap map;
    private FusedLocationProviderClient client;
    private LocationCallback mLocationCallback;
    private String folderLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartDetector = findViewById(R.id.btnStartDetector);
        btnStopDetector = findViewById(R.id.btnStopDetector);
        btnRecords = findViewById(R.id.btnRecords);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        ToggleButton tbMusic = (ToggleButton) findViewById(R.id.tbMusic);

        checkPermission();

        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);

        client = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        folderLocation = getFilesDir().getAbsolutePath() + "/Folder";
        File targetFile = new File(folderLocation, "data.txt");

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult!= null) {
                    Location data = locationResult.getLastLocation();
                    double lat = data.getLatitude();
                    double lng = data.getLongitude();

                    LatLng poi_currLocation = new LatLng(lat, lng);
                    mapFragment.getMapAsync(new OnMapReadyCallback(){
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            tvLatitude.setText("Latitude: " + lat);
                            tvLongitude.setText("Longitude: " + lng);

                            map = googleMap;
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(poi_currLocation, 15));
                            map.clear();
                            Marker currLocation = map.addMarker(new
                                    MarkerOptions()
                                    .position(poi_currLocation)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                            UiSettings ui = map.getUiSettings();
                            ui.setCompassEnabled(true);
                            ui.setZoomControlsEnabled(true);
                            ui.setMyLocationButtonEnabled(true);

                            File folder = new File(folderLocation);
                            if (folder.exists() == false) {
                                boolean result = folder.mkdir();
                                if (result == true) {
                                    Log.d("File Read/Write", "Folder created");
                                }
                            }
                            try {
                                FileWriter writer = new FileWriter(targetFile, true);
                                writer.write(lat + ", " + lng + "\n");
                                writer.flush();
                                writer.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    Toast.makeText(MainActivity.this,"New Location Detected\n" + "Lat: " + lat + ", " + " Lat: " + lng, Toast.LENGTH_SHORT).show();
                }
            }
        };

        btnStartDetector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();

                LocationRequest mLocationRequest = LocationRequest.create();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setInterval(10000);
                mLocationRequest.setFastestInterval(5000);
                mLocationRequest.setSmallestDisplacement(100);
                client.requestLocationUpdates(mLocationRequest, mLocationCallback, null);

                Intent i = new Intent(MainActivity.this, DetectorService.class);
                startService(i);
                Toast.makeText(MainActivity.this, "Detector is started", Toast.LENGTH_SHORT).show();

            }
        });

        btnStopDetector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                client.removeLocationUpdates(mLocationCallback);
                map.clear();

                Intent i = new Intent(MainActivity.this, DetectorService.class);
                stopService(i);
                Toast.makeText(MainActivity.this, "Detector is stopped", Toast.LENGTH_SHORT).show();

            }
        });

        btnRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, CheckRecords.class);
                startActivity(i);
            }
        });

        tbMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int permissionCheck = PermissionChecker.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if(permissionCheck != PermissionChecker.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                    Log.i("permission", "Permission not granted");
                    return;
                }
                if (isChecked) {
                    tbMusic.setTextOn("MUSIC OFF");
                    startService(new Intent(MainActivity.this, MyService.class));
                } else {
                    tbMusic.setTextOff("MUSIC ON");
                    stopService(new Intent(MainActivity.this, MyService.class));
                }
            }
        });
    }

    private boolean checkPermission() {
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    btnStartDetector.performClick();
                    Toast.makeText(MainActivity.this, "Location update started", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission not granted", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}