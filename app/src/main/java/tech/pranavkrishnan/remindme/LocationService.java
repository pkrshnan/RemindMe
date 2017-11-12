package tech.pranavkrishnan.remindme;

import android.*;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocationService extends Service {

    NotificationManager mNotificationManager;
    LocationManager locationManager;
    LocationListener locationListener;
    Notification notification;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Reminder reminder = checkLocation(location);

                if (reminder != null) {
                    createNotification(reminder);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 900, 0, locationListener);
        }
    }

    @Override
    @TargetApi(26)
    public int onStartCommand(Intent intent, int flags, int startid) {

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "main_channel";

        // Name, Description and Importance of Channel
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_MIN;

        NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        mNotificationManager.createNotificationChannel(mChannel);

        Intent notificationIntent = new Intent(this, EditActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification mNotification =  new Notification.Builder(this, channelId)

                .setSmallIcon(R.drawable.ic_nav_general)
                .setTicker("RemindMe Is Running")
                .setContentTitle("Currently Active")
                .setContentIntent(pendingIntent)
                .setContentText("The reminders service is actively running - please leave location on! We only request a location every 15 minutes.")
                .build();
        startForeground(1, mNotification);
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Reminder checkLocation(Location location) {
        for (Reminder reminder : MainActivity.reminderList) {
            Address address = getLocation(reminder.getAddress());
           Location reminderLoc = new Location("");
           reminderLoc.setLatitude(address.getLatitude());
           reminderLoc.setLongitude(address.getLongitude());

            if (location.distanceTo(reminderLoc) < 2000) {
                return reminder;
            }
        }

        return null;
    }


    private void createNotification(Reminder reminder) {
        NotificationCompat.Builder mBuilder;

        if (Build.VERSION.SDK_INT >= 26) {
            String channelId = "main_channel";

            // Name, Description and Importance of Channel
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            mNotificationManager.createNotificationChannel(mChannel);

            mBuilder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_nav_general)
                    .setContentTitle(reminder.getTitle())
                    .setOngoing(true)
                    .setContentText("You're near " + reminder.getAddress() + " , make sure to complete your tasks!");
        } else {
            mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_nav_general)
                    .setContentTitle(reminder.getTitle())
                    .setOngoing(true)
                    .setContentText("You're near " + reminder.getAddress() + " , make sure to complete your tasks!");
        }


        Intent editIntent =  new Intent(this, EditActivity.class);

        // Create Artificial BackStack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(EditActivity.class);
        stackBuilder.addNextIntent(editIntent);

        PendingIntent editPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(editPendingIntent);

        notification = mBuilder.build();
        mNotificationManager.notify(1, notification);
    }
    private Address getLocation(final String strAddress) {
        final Geocoder coder = new Geocoder(this);
        final List<Address> address = new ArrayList<Address>();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Address> addresses = coder.getFromLocationName(strAddress, 1);
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

        return address.get(0);
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }
}
