package com.alpha.museum.museum;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.alpha.museum.museum.models.Image;
import com.alpha.museum.museum.models.Monument;
import com.alpha.museum.museum.models.Museum;
import com.alpha.museum.museum.preference.ManagePreference;
import com.alpha.museum.museum.tools.BeaconScan;
import com.alpha.museum.museum.tools.MuseumScan;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RangedBeacon;
import org.altbeacon.beacon.service.ScanJob;

import java.util.Arrays;
import java.util.Collection;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.alpha.museum.museum.MainActivity.CHANNEL_ID;
import static com.alpha.museum.museum.MainActivity.TAG;
import static com.alpha.museum.museum.MainActivity.museumsList;

public class ListeningService extends Service implements BeaconConsumer, LocationListener {

    // The minimum distance to change updates in metters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;

    // The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 0;

    // flag for GPS Status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private final String BEACON_UUID = "00001111-0000-1111-0000-111100001111";
    private final int BEACON_MAJOR = 0;
    private String beaconId = null;
    private int beaconSerial = -1;
    private Monument monument;
    private RequestQueue requestQueue;

    private BeaconManager beaconManager;
    private BeaconScan beaconScan;
    private MuseumScan museumScan;
    private ManagePreference managePreference;
    private NotificationCompat.Builder mNotification;
    private int isNear = 0;
    private int listen = 0;
    Museum nearMuseum = null;

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    //Log.i(TAG, "run: in Thread");
                    Museum museum = gpsMuseumScan();
                    if (museum != null && museum != nearMuseum) {
                        listen = 0;
                        nearMuseum = museum;
                        managePreference.shareIntData("museum_id_n", nearMuseum.getMuseumId());
                        managePreference.shareIntData("museum_id", -1);
                        Log.i(TAG, "museum id = " + Integer.toString(nearMuseum.getMuseumId()));
                        Intent intent = new Intent(getApplicationContext(), MuseumProfile.class);
                        intent.putExtra(String.format("museum_%d", nearMuseum.getMuseumId()), nearMuseum);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                        mNotification.setSmallIcon(R.drawable.etil) // notification icon
                                .setContentTitle(nearMuseum.getCardTitle()) // title for notification
                                .setContentText("You are near to me .. get a visit !") // message for notification
                                .setAutoCancel(true) // clear notification after click
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setContentIntent(pendingIntent);

                        startForeground(1, mNotification.build());
                    } else if (museum == null) {
                        if (listen == 0) {
                            listen = 1;
                            nearMuseum = null;
                            Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
                            mNotification.setContentTitle("onListening")
                                    .setContentText("...")
                                    .setSmallIcon(R.drawable.etil)
                                    .setContentIntent(pendingIntent)
                                    .build();

                            startForeground(1, mNotification.build());
                        }
                    }
                    thread.sleep(3000);
                } catch (Exception e) {
                    Log.i(TAG, "run: Exception : " + e.toString());
                    e.printStackTrace();
                }
            }
        }
    });

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ScanJob.setOverridePeriodicScanJobId(1000);
        ScanJob.setOverrideImmediateScanJobId(1001);
        RangedBeacon.setSampleExpirationMilliseconds(2000);

        mNotification = new NotificationCompat.Builder(this, CHANNEL_ID);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        beaconScan = new BeaconScan();
        museumScan = new MuseumScan();
        managePreference = new ManagePreference(getApplicationContext());
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        beaconManager = BeaconManager.getInstanceForApplication(this);
        //beaconManager.setForegroundScanPeriod(1);
        //beaconManager.setBackgroundScanPeriod(1);
        beaconManager.bind(this);

        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        //beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        //beaconManager.bind(this);

        mNotification.setContentTitle("onListening")
                .setContentText("...")
                .setSmallIcon(R.drawable.etil)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, mNotification.build());
        listen = 1;

        if (thread.getState() == Thread.State.NEW)
        {
            thread.start();
        }

//        beaconManager.enableForegroundServiceScanning(notification.build(), 456);
//        beaconManager.setEnableScheduledScanJobs(false);
//        beaconManager.setBackgroundBetweenScanPeriod(0);
//        beaconManager.setBackgroundScanPeriod(1100);
//        beaconManager.bind(this);

//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("android.intent.action.SCREEN_ON");
//        intentFilter.addAction("android.intent.action.SCREEN_OFF");
//        intentFilter.setPriority(999);

//        registerReceiver(receiver, intentFilter);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onBeaconServiceConnect() {

        RangeNotifier rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

                Beacon beacon = null;

                Log.i(TAG, "didRangeBeaconsInRegion: beacons size = " + beacons.size());
                if (beacons.size() > 0) {
                    //Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  "+beacons.size());
                    //Beacon firstBeacon = beacons.iterator().next();
                    beacon = beaconScan.getCloserBeacon(beacons);
                    Log.i(TAG, "The first beacon " + beacon.toString() + " is about " + beacon.getDistance() + " meters away.");
                    if (beacon.getDistance() < 1.5) {
                        Log.i(TAG, "beacon to string = " + beacon.toString().substring(40, 41));//beacon.toString());
                        if (!(beacon.toString().equals(beaconId))) {
                            Log.i(TAG, "didRangeBeaconsInRegion: <<<<<<<<<<<<<<<<<<< New Notification >>>>>>>>>>>>>>");
                            beaconId = beacon.toString();
                            String uid = beaconId.substring(40, 41);
                            beaconSerial = Integer.parseInt(uid);
                            getMonument();
                        }
                    }
                }
            }

        };
        try {
            //beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.addRangeNotifier(rangeNotifier);
            //beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            //beaconManager.addRangeNotifier(rangeNotifier);
        } catch (RemoteException e) {
        }
    }

    void getMonument() {
        Log.i(TAG, "onResponse: Get Monument ??");
        try {
            String url = "https://www.kdefaoui-camagru.tk/api/monument/" + beaconSerial;
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Gson gson = new GsonBuilder().create();
                                monument = gson.fromJson(response, Monument.class);
                                Log.i(TAG, "onResponse: Monument Loaded !!");
                                if (monument != null) {
                                    buildNotification();
                                }
                                Log.i(TAG, "onResponse: Notification Build !!");
                            } catch (Exception e) {
                                Log.i(TAG, "Exception : " + e);
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i(TAG, "Error : " + error);
                }
            });
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }
    }

    void buildNotification() {
        managePreference.shareIntData("monument_id", monument.getMonumentId());
        Intent intent = new Intent(this, MonumentProfile.class);
        intent.putExtra(String.format("monument_%d", monument.getMonumentId()), monument);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        /*
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(monument.getMonumentTitle())
                .setContentText(monument.getMonumentDescription())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
                */

        mNotification.setSmallIcon(R.drawable.etil) // notification icon
                .setContentTitle(monument.getMonumentTitle()) // title for notification
                .setContentText(monument.getMonumentDescription()) // message for notification
                .setAutoCancel(true) // clear notification after click
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        startForeground(1, mNotification.build());
    }

    Museum gpsMuseumScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                // getting GPS status
                isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                // getting network status
                isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (isGPSEnabled && isNetworkEnabled) {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            double longitude = location.getLongitude();
                            double altitude = location.getLatitude();
                            //Log.i(TAG, "isInRange: My Location : " + altitude + "," + longitude);
                            Museum museum = museumScan.isInRange(museumsList, altitude, longitude);
                            if (museum != null) {
                                isNear = 1;
                                return museum;
                            }
                        } else {
                            Log.i(TAG, "gpsMuseumScan: Location NULL");
                        }
                    } else {
                        Log.i(TAG, "gpsMuseumScan: PROVIDER NOT ENABLED !!");
                    }
                } else {
                    Log.i(TAG, "gpsMuseumScan: GPS OR NETWORK NOT ENABLED");
                }
            }
        }
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        
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
}
