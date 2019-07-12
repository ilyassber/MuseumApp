package com.alpha.museum.museum;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static androidx.core.app.ActivityCompat.requestPermissions;
import static com.alpha.museum.museum.MainActivity.CHANNEL_ID;
import static com.alpha.museum.museum.MainActivity.TAG;

public class ListeningService extends Service implements BeaconConsumer {

    private BeaconManager beaconManager;
    private final String BEACON_UUID = "11687109-915f-4136-a1f8-e60ff514f96d";
    private final int BEACON_MAJOR = 3;

    @Override
    public void onCreate() {
        super.onCreate();
        L.p("In TestBestzBeaconService onCreate()");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        L.p("in TestBestzBeaconService onStartCommand()");

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.bind(this);

        //iBeacons ?
        BeaconParser bp0 = new BeaconParser();
        bp0.setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
        beaconManager.getBeaconParsers().add(bp0);

        //Bluecats?
        BeaconParser bp1 = new BeaconParser();
        bp1.setBeaconLayout("m:2-3=0201,i:28-29,p:24-24");
        beaconManager.getBeaconParsers().add(bp1);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        L.p("In TestBestzBeaconService onDestroy()");
        beaconManager.unbind(this);
    }


    @Override
    public void onBeaconServiceConnect() {

        L.p("In TestBestzBeaconService onBeaconServiceConnect()");

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> arg0, Region arg1) {
                L.p("In TestBestzBeaconService - anonymous didRangeBeaconsInRegion()");
            }
        });

        Region region = new Region("myregion", Identifier.parse(BEACON_UUID), Identifier.fromInt(BEACON_MAJOR), null); //

        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            L.p("In TestBestzBeaconService onBeaconServiceConnect(), REMOTEEXCEPTION!");
        }

    }

    private static class L
    {
        public static void p(String s) {
            Log.i("beacon", s);
        }
    }

    /*

    String messageBody = null;
    String address = null;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private final String BEACON_UUID = "00001111-0000-1111-0000-111100001111";
    private final int BEACON_MAJOR = 0;

    private BeaconManager beaconManager;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
                SmsMessage[] smsMessage = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                messageBody = smsMessage[0].getMessageBody();
                address = smsMessage[0].getOriginatingAddress();
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (BeaconManager.getInstanceForApplication(this).checkAvailability()) {
            Log.i(TAG, "onStartCommand: BLE Available");
        } else {
            Log.i(TAG, "onStartCommand: BLE not Available");
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.bind(this);


        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        //beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        //beaconManager.bind(this);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("onListening")
                .setContentText("...")
                .setSmallIcon(R.drawable.head_image)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.setPriority(999);

        registerReceiver(receiver, intentFilter);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onBeaconServiceConnect() {

        final Region mRegion = new Region("myRangingUniqueId", Identifier.parse(BEACON_UUID), Identifier.fromInt(BEACON_MAJOR), null);

        beaconManager.setMonitorNotifier(new MonitorNotifier() {
             @Override
             public void didEnterRegion(Region region) {
                 Log.d("TEST", "ENTERED beacon region");
                 //Start Raning as soon as you detect a beacon
                 try {
                     beaconManager.startRangingBeaconsInRegion(mRegion);
                 } catch (RemoteException e) {
                     e.printStackTrace();
                 }
             }

            @Override
            public void didExitRegion(Region region) {

            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

        RangeNotifier rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  " + beacons.size());
                    Beacon firstBeacon = beacons.iterator().next();
                    Log.i(TAG, "The first beacon " + firstBeacon.toString() + " is about " + firstBeacon.getDistance() + " meters away.");
                }
            }

        };
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.addRangeNotifier(rangeNotifier);
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.addRangeNotifier(rangeNotifier);
        } catch (RemoteException e) {
        }


        /*
        beaconManager.removeAllMonitorNotifiers();
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {    }
        */

}
