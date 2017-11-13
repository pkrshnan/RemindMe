package tech.pranavkrishnan.remindme;

import android.*;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

// TODO: Apply FusedLocationProvider instead of LocationManager.
public class LocationService extends Service {

    NotificationManager mNotificationManager;
    LocationManager locationManager;
    LocationListener locationListener;
    Notification notification;
    String remindersGroup = "Reminders";
    int notificationCount = 2;

    @Override
    public void onCreate() {
        super.onCreate();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                List<Reminder> checkedList = checkLocation(location);

                if (!checkedList.isEmpty()) {
                    for (Reminder reminder: checkedList) {
                        createNotification(reminder);
                    }
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 900000, 100, locationListener);
        }
    }

    @Override
    @TargetApi(26)
    public int onStartCommand(Intent intent, int flags, int startid) {

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "running_channel";

        // Name, Description and Importance of Channel
        CharSequence name = "Service Running";
        String description = "Channel that contains persistent notification of status of background service.";
        int importance = NotificationManager.IMPORTANCE_MIN;

        NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        mNotificationManager.createNotificationChannel(mChannel);

        Intent notificationIntent = new Intent(this, EditActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification mNotification =  new Notification.Builder(this, channelId)

                .setSmallIcon(R.drawable.ic_location_outline)
                .setTicker("RemindMe Is Running")
                .setContentTitle("Currently Active")
                .setContentIntent(pendingIntent)
                .setGroup("Active")
                .setContentText("The reminders service is actively running - please leave location on!")
                .build();
        startForeground(1, mNotification);
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private List<Reminder> checkLocation(Location location) {
        List<Reminder> reminderList = deserializeData();
        List<Reminder> checkedList = new ArrayList<Reminder>();

        for (Reminder reminder : reminderList) {
            Address address = getLocation(reminder.getAddress());
           Location reminderLoc = new Location("");
           reminderLoc.setLatitude(address.getLatitude());
           reminderLoc.setLongitude(address.getLongitude());

            if (location.distanceTo(reminderLoc) <= 500) {
                checkedList.add(reminder);
            }
        }

        return checkedList;
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

            if (notificationCount >= 3) {
                NotificationCompat.Builder summaryNotification = new NotificationCompat.Builder(this)
                        .setGroupSummary(true)
                        .setContentTitle("Reminders")
                        .setSmallIcon(R.drawable.ic_nav_general)
                        .setChannelId(channelId)
                        .setGroup(remindersGroup);

                mNotificationManager.notify(0, summaryNotification.build());
            }

            mBuilder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_nav_general)
                    .setContentTitle(reminder.getTitle())
                    .setAutoCancel(true)
                    .setGroup(remindersGroup)
                    .setContentText("You're near " + reminder.getAddress() + ", make sure to complete your task!");
        } else {
            mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_nav_general)
                    .setContentTitle(reminder.getTitle())
                    .setGroup(remindersGroup)
                    .setAutoCancel(true)
                    .setContentText("You're near " + reminder.getAddress() + ", make sure to complete your task!");
        }


        Intent editIntent =  new Intent(this, EditActivity.class);
        editIntent.setAction("Hello");
        editIntent.putExtra("Reminder",  (Parcelable) new Reminder(reminder.getTitle(), reminder.getAddress(), reminder.getCategory(), reminder.getPriority(), reminder.getRepeat()));

        PendingIntent editPendingIntent = PendingIntent.getActivity(this, notificationCount, editIntent, PendingIntent.FLAG_ONE_SHOT);
        mBuilder.setContentIntent(editPendingIntent);

        notification = mBuilder.build();
        mNotificationManager.notify(notificationCount, notification);
        notificationCount += 1;
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
    public void onDestroy () {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }
}
