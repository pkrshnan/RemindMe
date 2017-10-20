package tech.pranavkrishnan.remindme;

import android.content.Intent;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set Gradient
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        setContentView(R.layout.activity_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TextView title = (TextView) findViewById(R.id.edit_activity_title);
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/Quicksand-Bold.otf");
        title.setTypeface(custom_font);

        // Obtain MapFragment and notify when map ready
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title.setText(getIntent().getStringExtra("Title"));

        // Get Textviews
        final TextView addressText = findViewById(R.id.edit_address);
        final TextView tagText = findViewById(R.id.edit_tag);
        final TextView priorityText = findViewById(R.id.edit_priority);
        TextView repeatText = findViewById(R.id.edit_repeat);

        // Set Values
        addressText.setText(getIntent().getStringExtra("Address"));
        tagText.setText(getIntent().getStringExtra("Tag"));
        priorityText.setText(getIntent().getStringExtra("Priority"));

        // Set Repeat Value
        boolean[] repeat = getIntent().getBooleanArrayExtra("Repeat");
        String concatenated = "";
        for (int i= 0; i < repeat.length; i++) {
            if (repeat[i]) {
                concatenated = concatenated + checkDay(i) + ", ";
            }
        }

        if (concatenated.equals("")) {
            concatenated = "None Set";
        } else {
            concatenated = concatenated.substring(0, concatenated.length() - 2);
        }

        repeatText.setText(concatenated);

        // Handle button click
        ImageView editButton = findViewById(R.id.button_edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreationActivity.class);
                intent.putExtra("Activity", "EditActivity");
                intent.putExtra("Reminder", new Reminder(title.getText().toString(), addressText.getText().toString(), tagText.getText().toString(), priorityText.getText().toString()));
                startActivity(intent);
            }
        });

    }

    private String checkDay(int i) {
        switch (i) {
            case 0:
                return "Sunday";
            case 1:
                return "Monday";
            case 2:
                return "Tuesday";
            case 3:
                return "Wednesday";
            case 4:
                return "Thursday";
            case 5:
                return "Friday";
            case 6:
                return "Saturday";
        }

        return "None";
    }

    private Address getLocation(final String strAddress) {
        final Geocoder coder = new Geocoder(this);
        final List<Address> address = new ArrayList<Address>();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // TODO: implement logic to choose a marker in Creation, then pass that LatLng here
                    List<Address> addresses = coder.getFromLocationName(strAddress, 1);
                    if (addresses != null) {
                        address.addAll(addresses);
                    } else {
                        Log.d("GetLocation", "Addresses are null");
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
            Log.d("Join", i.toString());
        }
        if (address.isEmpty()) {
            return null;
        }
        return address.get(0);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set Map
        TextView addressText = findViewById(R.id.edit_address);
        Address selectedAddress = getLocation(addressText.getText().toString());
        if (selectedAddress != null) {
            LatLng latlng = new LatLng(selectedAddress.getLatitude(), selectedAddress.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15f));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            mMap.addMarker(new MarkerOptions().position(latlng).alpha(0.8f).title("Selected Address"));
        } else {
            Log.d("Error", "No address set.");
        }
    }


}