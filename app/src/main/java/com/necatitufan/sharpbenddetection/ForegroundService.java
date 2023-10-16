package com.necatitufan.sharpbenddetection;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
//import android.support.annotation.Nullable;
//import android.support.v4.app.NotificationCompat;

public class ForegroundService extends Service
{
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private ArrayList<double[]> coords = new ArrayList<>();
    private boolean isDestroying = false;

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        //do heavy work on a background thread
        //stopSelf();

        try
        {
            readCoords();
            startTime();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return START_NOT_STICKY;
    }

    private void readCoords() throws IOException
    {
        InputStream inputStream = getResources().openRawResource(R.raw.coord);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String eachline = bufferedReader.readLine();
        while (eachline != null)
        {
            // `the words in the file are separated by space`, so to get each words
            String[] words = eachline.split(":");
            coords.add(new double[]{Double.parseDouble(words[0]), Double.parseDouble(words[1])});
            eachline = bufferedReader.readLine();
        }
    }

    private Handler mHandler = new Handler();

    private void startTime()
    {
        mHandler.removeCallbacks(mUpdateTimeTask);
        mHandler.postDelayed(mUpdateTimeTask, 1000);
    }

    private Runnable mUpdateTimeTask = new Runnable()
    {
        public void run()
        {
            setMockLocation();
            // buraya ne yapmak istiyorsan o kodu yaz.. Kodun sonlandıktan sonra 1 saniye sonra tekrar çalışacak şekilde handler tekrar çalışacak.
            if (!isDestroying)
                mHandler.postDelayed(this, 1000);
        }
    };

    int i = 0;

    private void setMockLocation()
    {
        /*LocationManager locMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locMgr.addTestProvider(LocationManager.GPS_PROVIDER,
                "requiresNetwork" == "",
                "requiresSatellite" == "",
                "requiresCell" == "",
                "hasMonetaryCost" == "",
                "supportsAltitude" == "",
                "supportsSpeed" == "",
                "supportsBearing" == "",
                android.location.Criteria.POWER_LOW,
                android.location.Criteria.ACCURACY_FINE);

        Location newLocation = new Location(LocationManager.GPS_PROVIDER);

        newLocation.setLatitude(coords.get(i)[0]);
        newLocation.setLongitude(coords.get(i)[1]);
        newLocation.setAccuracy(1.0f);
//        newLocation.setAltitude(0);
//        newLocation.setAccuracy(500);
        newLocation.setElapsedRealtimeNanos(System.currentTimeMillis());
        newLocation.setTime(System.currentTimeMillis());
        locMgr.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

        locMgr.setTestProviderStatus(LocationManager.GPS_PROVIDER,
                LocationProvider.AVAILABLE,
                null, System.currentTimeMillis());

        locMgr.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocation);

        i++;*/

        // translate to actual GPS location
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(coords.get(i)[0]);
        location.setLongitude(coords.get(i)[1]);
//        location.setAltitude(500);
        location.setAccuracy(0f);
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        location.setTime(System.currentTimeMillis());


        // show debug message in log
        Log.d("_mock", location.toString());

        // provide the new location
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);
        i++;
    }

    @Override
    public void onDestroy()
    {
        isDestroying = true;
        mHandler.removeCallbacks(mUpdateTimeTask);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}