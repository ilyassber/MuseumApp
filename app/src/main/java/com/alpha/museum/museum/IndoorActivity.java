package com.alpha.museum.museum;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alpha.museum.museum.localisation.Locate;
import com.alpha.museum.museum.models.Location;
import com.alpha.museum.museum.tools.BeaconScan;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RangedBeacon;
import org.altbeacon.beacon.service.ScanJob;

import java.util.ArrayList;
import java.util.Collection;

public class IndoorActivity extends AppCompatActivity implements BeaconConsumer {

    private static final String TAG = "BeaconScan";
    private BeaconManager beaconManager;
    private BeaconScan beaconScan;
    private Locate locate;
    private Location location;
    private com.alpha.museum.museum.models.Beacon beacon_a = null;
    private com.alpha.museum.museum.models.Beacon beacon_b = null;
    private com.alpha.museum.museum.models.Beacon beacon_c = null;
    private int width;

    ImageView pointer;
    TextView statistics;

    double x = 0;
    double y = 0;

    ArrayList<com.alpha.museum.museum.models.Beacon> beacons;

    Thread draw = new Thread(new Runnable() {
        @Override
        public void run() {

            findLocationStimulator(beacons);
            //moveImageView(pointer,(int) (x * (double) width / 100) - 5, (int) (y * (double) width / 100) - 5);
        }
    });

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            double i = 0;
            while (true) {
                try {
                    Log.i(TAG, "run: >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> i = " + (i % 360) + " -- cos(i) = " + Math.cos(i / 180 * Math.PI) + " -- sin(i) = " + Math.sin(i / 180 * Math.PI));
                     thread.sleep(75);
                    double v = 50;
                    x = v + (25 * Math.cos((i % 360) / 180 * Math.PI));
                    y = v + (25 * Math.sin((i % 360) / 180 * Math.PI));
                    beacons.get(0).setDistance(Math.sqrt(Math.pow((x - beacons.get(0).getX()), 2) + Math.pow((y - beacons.get(0).getY()), 2)));
                    beacons.get(1).setDistance(Math.sqrt(Math.pow((x - beacons.get(1).getX()), 2) + Math.pow((y - beacons.get(1).getY()), 2)));
                    beacons.get(2).setDistance(Math.sqrt(Math.pow((x - beacons.get(2).getX()), 2) + Math.pow((y - beacons.get(2).getY()), 2)));
//                  beacons.get(0).setDistance(70.71);
//                  beacons.get(1).setDistance(70.71);
//                  beacons.get(2).setDistance(70.71);
                    runOnUiThread(draw);
                    i++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoor);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconScan = new BeaconScan();
        locate = new Locate();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;

        ScanJob.setOverridePeriodicScanJobId(1000);
        ScanJob.setOverrideImmediateScanJobId(1001);
        RangedBeacon.setSampleExpirationMilliseconds(2000);

        pointer = (ImageView) findViewById(R.id.pointer);
        statistics = (TextView) findViewById(R.id.statistics);

        beacons = new ArrayList<>();

        beacons.add(new com.alpha.museum.museum.models.Beacon(0, 0));
        beacons.add(new com.alpha.museum.museum.models.Beacon(100, 100));
        beacons.add(new com.alpha.museum.museum.models.Beacon(100, 0));

        thread.start();


        //beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
        beaconManager.bind(this);

        //moveImageView(pointer, width / 2, width / 2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        beaconManager.unbind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.bind(this);
    }

    @Override
    public void onBeaconServiceConnect() {

        RangeNotifier rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Log.i(TAG, "didRangeBeaconsInRegion: beacons size = " + beacons.size());
                if (beacons.size() > 0) {
                    Log.d(TAG, "didRangeBeaconsInRegion called with beacon count: " + beacons.size());
                    //findLocation(beacons);
                    /*
                    Beacon firstBeacon = beacons.iterator().next();
                    Log.i(TAG, "The first beacon " + firstBeacon.toString() + " is about " + firstBeacon.getDistance() + " meters away.");
                    beacons.remove(firstBeacon);
                    firstBeacon = beacons.iterator().next();
                    if (firstBeacon != null) {
                        Log.i(TAG, "The second beacon " + firstBeacon.toString() + " is about " + firstBeacon.getDistance() + " meters away.");
                        beacons.remove(firstBeacon);
                    }
                    firstBeacon = beacons.iterator().next();
                    if (firstBeacon != null) {
                        Log.i(TAG, "The third beacon " + firstBeacon.toString() + " is about " + firstBeacon.getDistance() + " meters away.");
                    }
                    */
                }
            }

        };
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.addRangeNotifier(rangeNotifier);
            //beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            //beaconManager.addRangeNotifier(rangeNotifier);
        } catch (RemoteException e) {
        }
    }

    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {

            }
        });
    }

    void findLocation(Collection<Beacon> beacons) {
        Location location = beaconScan.getLocation(beacons);
        locate.getLocation(location);
        moveImageView(pointer, (int) (location.getX() * ((double) width) / 100) - 5, (int) (location.getY() * ((double) width) / 100) - 5);
    }

    void findLocationStimulator(ArrayList<com.alpha.museum.museum.models.Beacon> beacons) {

        String data;

        Location location = beaconScan.getLocationStimulator(beacons);
        locate.getLocation(location);
        moveImageView(pointer, (int) (location.getX() * ((double) width) / 100) - 5, (int) (location.getY() * ((double) width) / 100) - 5);
        data =  "Beacon_A : x = " + location.getBeacon_a().getX() + " - y = " + location.getBeacon_a().getY() + "\nDistance_A = " + location.getDist_a() + "\n" +
                "Beacon_B : x = " + location.getBeacon_b().getX() + " - y = " + location.getBeacon_b().getY() + "\nDistance_B = " + location.getDist_b() + "\n" +
                "Beacon_C : x = " + location.getBeacon_c().getX() + " - y = " + location.getBeacon_c().getY() + "\nDistance_C = " + location.getDist_c();
        statistics.setText(data);
    }

    public void moveImageView(View view, int x, int y) {

        Log.i(TAG, "moveImageView: set location => x : " + x + " -- y : " + y);

        ObjectAnimator objectX;
        ObjectAnimator objectY;
        AnimatorSet animatorXY = new AnimatorSet();

        objectX = ObjectAnimator.ofFloat(view, "translationX", x);
        objectY = ObjectAnimator.ofFloat(view, "translationY", y);
        animatorXY.playTogether(objectX, objectY);
        animatorXY.setDuration(50);
        animatorXY.start();
    }

}
