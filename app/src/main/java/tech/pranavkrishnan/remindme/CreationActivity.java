package tech.pranavkrishnan.remindme;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location currentLocation;
    private Activity activity;

    private Reminder newReminder;

    private FusedLocationProviderClient mFusedLocationClient;
    private double homeLatitude = 43.610918;
    private double homeLongitude = -79.657034;
    private Marker currentLocationMarker;
    private Circle currentLocationCircle;

    private EditText addressText;
    private EditText nameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        //Set Gradient
        Window window = this.getWindow();
        Drawable background = this.getResources().getDrawable(R.drawable.gradient);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
        window.setNavigationBarColor(getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);


        setContentView(R.layout.activity_creation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Typeface
        TextView tx = (TextView) findViewById(R.id.creation_activity_title);
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/Quicksand-Bold.otf");

        tx.setTypeface(custom_font);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create New Reminder
        newReminder = new Reminder();

        // Name EditText Handling
        nameText = findViewById(R.id.name_edittext);
        // Check when enter is pressed
        // TODO: Figure out why this gives a null pointer exception
        nameText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                        event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    newReminder.setTitle(nameText.getText().toString());
                    return true;
                }
                return false;
            }
        });

        // Check when focus is lost
        nameText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!nameText.getText().toString().equals("")) {
                        newReminder.setTitle(nameText.getText().toString());
                    }
                }
            }
        });

        addressText = findViewById(R.id.address_edittext);
        // Check when enter is pressed
        addressText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                        event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    newReminder.setAddress(addressText.getText().toString());

                    if (!addressText.getText().toString().equals("")) {
                        List<Address> addresses = getLocation(addressText.getText().toString());
                        Log.d("Size", Integer.toString(addresses.size()));
                        if (addresses.size() == 1) {
                            Address currentAd = addresses.get(0);
                            LatLng currentLoc = new LatLng(currentAd.getLatitude(), currentAd.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));
                            mMap.addMarker(new MarkerOptions().position(currentLoc).alpha(0.8f).title("Location"));
                        } else if (addresses.size() > 1) {
                            List<LatLng> latlngs = new ArrayList<LatLng>();
                            double smallestLat = addresses.get(0).getLatitude();
                            double smallestLong = addresses.get(0).getLongitude();
                            double largestLat = addresses.get(0).getLatitude();
                            double largestLong = addresses.get(0).getLongitude();

                            for (int i = 1; i < addresses.size(); i++) {
                                double latitude = addresses.get(i).getLatitude();
                                double longitude = addresses.get(i).getLongitude();
                                if (latitude < smallestLat ) {
                                    smallestLat = latitude;
                                } else if (latitude > largestLat) {
                                    largestLat = latitude;
                                }

                                if (longitude < smallestLong) {
                                    smallestLong = longitude;
                                } else if (longitude > largestLong) {
                                    largestLong = longitude;
                                }
                            }

                            LatLngBounds bounds = new LatLngBounds(new LatLng(smallestLat, smallestLong), new LatLng(largestLat, largestLong));
                                  mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                } else {
                    Toast.makeText(getApplicationContext(), "We're having trouble finding that address. Perhaps be more specific?", Toast.LENGTH_LONG).show();
                }
            }

                    return true;
                }
                return false;
            }
        });

        // Check when focus is lost
        addressText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!addressText.getText().toString().equals("")) {
                        newReminder.setAddress(addressText.getText().toString());

                        if (!addressText.getText().toString().equals("")) {
                            List<Address> addresses = getLocation(addressText.getText().toString());
                            Log.d("Size", Integer.toString(addresses.size()));
                            if (addresses.size() == 1) {
                                Address currentAd = addresses.get(0);
                                LatLng currentLoc = new LatLng(currentAd.getLatitude(), currentAd.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));
                                mMap.addMarker(new MarkerOptions().position(currentLoc).alpha(0.8f).title("Location"));
                            } else if (addresses.size() > 1) {
                                List<LatLng> latlngs = new ArrayList<LatLng>();
                                double smallestLat = addresses.get(0).getLatitude();
                                double smallestLong = addresses.get(0).getLongitude();
                                double largestLat = addresses.get(0).getLatitude();
                                double largestLong = addresses.get(0).getLongitude();

                                for (int i = 1; i < addresses.size(); i++) {
                                    double latitude = addresses.get(i).getLatitude();
                                    double longitude = addresses.get(i).getLongitude();
                                    if (latitude < smallestLat ) {
                                        smallestLat = latitude;
                                    } else if (latitude > largestLat) {
                                        largestLat = latitude;
                                    }

                                    if (longitude < smallestLong) {
                                        smallestLong = longitude;
                                    } else if (longitude > largestLong) {
                                        largestLong = longitude;
                                    }
                                }

                                LatLngBounds bounds = new LatLngBounds(new LatLng(smallestLat, smallestLong), new LatLng(largestLat, largestLong));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                            } else {
                                Toast.makeText(getApplicationContext(), "We're having trouble finding that address. Perhaps be more specific?", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            }
        });


        // Tag Spinner
        final Spinner tagSpinner = (Spinner) findViewById(R.id.tag_spinner);
        ArrayAdapter<CharSequence> tagAdapter =  ArrayAdapter.createFromResource(this, R.array.tag_spinner, android.R.layout.simple_spinner_dropdown_item);

        // Specify layout to use
        tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply Adapter
        tagSpinner.setAdapter(tagAdapter);

        // Priority Spinner
        final Spinner prioritySpinner = (Spinner) findViewById(R.id.priority_spinner);
        ArrayAdapter<CharSequence> priorityAdapter =  ArrayAdapter.createFromResource(this, R.array.priority_spinner, android.R.layout.simple_spinner_dropdown_item);

        // Specify layout to use
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply Adapter
        prioritySpinner.setAdapter(priorityAdapter);

        // If intent is set, set editText values
        if (getIntent() != null) {
            if (getIntent().getStringExtra("Activity").equals("EditActivity")) {
                Reminder reminder = getIntent().getParcelableExtra("Reminder");
                nameText.setText(reminder.getTitle());
                addressText.setText(reminder.getAddress());
                tagSpinner.setSelection(tagAdapter.getPosition(reminder.getCategory()));
                prioritySpinner.setSelection(priorityAdapter.getPosition(reminder.getPriority()));
            }
        }

        // Get checkboxes and switch
        final LinearLayout checkboxes = findViewById(R.id.checkboxes);
        checkboxes.setVisibility(View.GONE);

        final Switch repeat = findViewById(R.id.repeat_switch);
        repeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkboxes.setVisibility(View.VISIBLE);
                } else {
                    checkboxes.setVisibility(View.GONE);
                }
            }
        });
        // Add
        ImageView imageView = findViewById(R.id.create_button_add);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                String nameTextValue = nameText.getText().toString();
                String addressTextValue = addressText.getText().toString();

                // Get all the checkboxes
                CheckBox monday = findViewById(R.id.checkbox_monday);
                CheckBox tuesday = findViewById(R.id.checkbox_tuesday);
                CheckBox wednesday = findViewById(R.id.checkbox_wednesday);
                CheckBox thursday = findViewById(R.id.checkbox_thursday);
                CheckBox friday = findViewById(R.id.checkbox_friday);
                CheckBox saturday = findViewById(R.id.checkbox_saturday);
                CheckBox sunday = findViewById(R.id.checkbox_sunday);

                // Make sure name and address aren't empty, then set all data and start main activity
                if (!addressTextValue.equals("") && !nameText.getText().toString().equals("")) {newReminder.setAddress(addressTextValue);
                    newReminder.setTitle(nameTextValue);
                    newReminder.setCategory(tagSpinner.getSelectedItem().toString());
                    newReminder.setPriority(prioritySpinner.getSelectedItem().toString());
                    newReminder.setRepeat(sunday.isChecked(), monday.isChecked(), tuesday.isChecked(), wednesday.isChecked(), thursday.isChecked(),
                            friday.isChecked(), saturday.isChecked());
                    intent.putExtra("Activity", "CreationActivity");
                    intent.putExtra("Reminder", (Parcelable) newReminder);
                    startActivity(intent);

                } else if (nameTextValue.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter a Name", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Please Enter an Address", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Log.d("Location", "Failure");
            return;
        }

        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20, 100, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                moveCamera(location, true);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
                Toast.makeText(activity, "Remind Me! needs the location permission to function.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                }
                else {
                    Toast.makeText(this, "Remind Me! needs the location permission to function.", Toast.LENGTH_LONG).show();
                    getCurrentLocation();
                }
            }
        }
    }

    private void moveCamera(Location location, Boolean userLocation) {
        currentLocation = location;
        double latitude = currentLocation.getLatitude();
        double longitude = currentLocation.getLongitude();
        LatLng latlng = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15f));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        if (userLocation) {
            currentLocationMarker.setPosition(latlng);
            currentLocationCircle.setCenter(latlng);
            currentLocationCircle.setRadius(location.getAccuracy());
        } else {
            currentLocationMarker.setPosition(latlng);
        }

        if (location.getAccuracy() > 1000) {
            Toast.makeText(getApplicationContext(), "Sorry about the uncertainty, having trouble retrieving your location", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        MarkerOptions currentLocationOptions = new MarkerOptions().position(new LatLng(50000, 50000)).alpha(0.8f).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        CircleOptions circleOptions = new CircleOptions().center(new LatLng(50000, 50000)).radius(10).fillColor(0x9091bbff).strokeWidth(0);
        currentLocationCircle = mMap.addCircle(circleOptions);
        currentLocationMarker = mMap.addMarker(currentLocationOptions);
    }

    private List<Address> getLocation(final String strAddress) {
        final Geocoder coder = new Geocoder(this);
        final List<Address> address = new ArrayList<Address>();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Address> addresses = coder.getFromLocationName(strAddress, 5);
                    if (addresses != null) {
                        address.addAll(addresses);
                    } else {
                        Log.d("Error", "present");
                    }

                } catch (IOException i) {
                    Log.e("getLocation", i.toString());
                }
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException i) {
            Log.e("Thread", i.toString());
        }

        Integer numAddress = address.size();
        List<Address> returnAdd = new ArrayList<Address>();
        for (int i = 0; i < numAddress; i++) {
            double latitude = address.get(i).getLatitude();
            double longitude = address.get(i).getLongitude();

            Log.d("LatDifference", Double.toString(Math.abs(homeLatitude-latitude)));
            Log.d("LongDifference", Double.toString(Math.abs(homeLongitude-longitude)));
            if (Math.abs(homeLatitude-latitude) <= 0.2 && Math.abs(homeLongitude-longitude) <= 0.2) {
                returnAdd.add(address.get(i));
            }
        }
        return returnAdd;
    }

    @Override
    public void onBackPressed() {
        Log.d("nametext", nameText.getText().toString());
        Log.d("addresstext", addressText.getText().toString());
        if (addressText.getText().toString().equals("") || nameText.getText().toString().equals("")) {
            newReminder = null;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (addressText.getText().toString().equals("") || nameText.getText().toString().equals("")) {
                    newReminder = null;
                }
        }
        finish();
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15f));
            getCurrentLocation();
        }
     }
}