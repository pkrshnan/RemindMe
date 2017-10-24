package tech.pranavkrishnan.remindme;

import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    public static List<Reminder> reminderList;
    private TextView title;
    private Reminder newReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, GeneralFragment.newInstance("General")).commit();

        // Populating General Fragment
        if (deserializeData() == null) {
            MainActivity.reminderList = new ArrayList<Reminder>();
        } else {
            MainActivity.reminderList = deserializeData();
        }
        //Set Gradient
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        // Inflating Layout
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Typeface
        setTitle("");
        title = (TextView) findViewById(R.id.main_activity_title);
        NavigationView navigationView = findViewById(R.id.nav_view);
        TextView navHeaderName = navigationView.getHeaderView(0).findViewById(R.id.nav_header_name);
        TextView navHeaderEmail = navigationView.getHeaderView(0).findViewById(R.id.nav_header_email);

        Typeface boldFont = Typeface.createFromAsset(getAssets(),  "fonts/Quicksand-Bold.otf");
        Typeface normalFont = Typeface.createFromAsset(getAssets(),  "fonts/Quicksand-Regular.otf");


        title.setTypeface(boldFont);
        navHeaderName.setTypeface(boldFont);
        navHeaderEmail.setTypeface(normalFont);

        if (getIntent() != null) {
            if (getIntent().getStringExtra("Activity") != null) {
                if (getIntent().getStringExtra("Activity").equals("CreationActivity")) {
                    newReminder = getIntent().getParcelableExtra("Reminder");
                } else if(getIntent().getStringExtra("Activity").equals("EditActivity")) {
                    String removeName = getIntent().getStringExtra("Remove");
                    for (Reminder i: MainActivity.reminderList) {
                        if (i.getTitle().equals(removeName)) {
                            MainActivity.reminderList.remove(i);
                        }
                    }
                }
            }
        }
                // Create button for creating reminder
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CreationActivity.class);
                intent.putExtra("Activity", "MainActivity");
                startActivity(intent);
            }
        });

        // Opening and Closing Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Selecting items in drawer
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        String add = "General";
        boolean priority = false;
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_general:
                add = "General";
                title.setText("General");
                break;
            case R.id.nav_shopping:
                add = "Shopping";
                priority = true;
                title.setText("Shopping");
                break;
            case R.id.nav_grocery:
                add = "Grocery";
                priority = true;
                title.setText("Grocery");
                break;
            case R.id.nav_tasks:
                add = "Tasks";
                priority = true;
                title.setText("Tasks");
                break;
            case R.id.nav_settings:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_help:
                break;
        }

        //TODO: find a way to implement it so that tag is replaced with priority in categorized groups

        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, GeneralFragment.newInstance(add)).commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void serializeData(List<Reminder> reminderList) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(getFilesDir() + "data.ser");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(reminderList);
            objectOutputStream.close();
            fileOutputStream.close();

        } catch (Exception e) {
            Log.e("Serialize", e.toString());
        }
    }

    public List<Reminder> deserializeData() {
        try {
            FileInputStream fileInputStream = new FileInputStream(getFilesDir() + "data.ser");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            List<Reminder> reminderList = (List<Reminder>) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();

            return reminderList;
        } catch (Exception e) {
            Log.e("Deserialize", e.toString());
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!MainActivity.reminderList.isEmpty() && !MainActivity.reminderList.contains(newReminder) && newReminder != null) {
            if (!newReminder.getTitle().equals("") && !newReminder.getAddress().equals("")) {
                boolean exists = false;
                for (Reminder i : MainActivity.reminderList) {
                    if (i.getTitle().equals(newReminder.getTitle()) && i.getAddress().equals(newReminder.getAddress())) {
                        exists = true;
                    }
                }
                if (!exists) {
                    MainActivity.reminderList.add(newReminder);
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_container, GeneralFragment.newInstance("General")).commit();
                }

            }
        } else if (MainActivity.reminderList.isEmpty() && newReminder != null) {
            MainActivity.reminderList.add(newReminder);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_container, GeneralFragment.newInstance("General")).commit();
        }

    }
    @Override
    public void onStop() {
        super.onStop();
        serializeData(MainActivity.reminderList);
    }
}
