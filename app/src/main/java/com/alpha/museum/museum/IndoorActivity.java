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

import java.util.Collection;

public class IndoorActivity extends AppCompatActivity implements BeaconConsumer {

    private static final String TAG = "IndoorActivity";
    private BeaconManager beaconManager;
    private BeaconScan beaconScan;
    private Locate locate;
    private Location location;
    private com.alpha.museum.museum.models.Beacon beacon_a = null;
    private com.alpha.museum.museum.models.Beacon beacon_b = null;
    private com.alpha.museum.museum.models.Beacon beacon_c = null;
    private int width;

    ImageView pointer;

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

        //beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
        beaconManager.bind(this);

        moveImageView(pointer, width / 2, width / 2);
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
                    Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  "+beacons.size());
                    findLocation(beacons);
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
        } catch (RemoteException e) {   }
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
        moveImageView(pointer, (int)(location.getX() * (double) width / 100) - 5, (int)(location.getY() * (double) width / 100) - 5);
    }

    public void moveImageView(View view, int x, int y){
        ObjectAnimator objectX;
        ObjectAnimator objectY;
        AnimatorSet animatorXY = new AnimatorSet();

        objectX = ObjectAnimator.ofFloat(view, "translationX", x);
        objectY = ObjectAnimator.ofFloat(view, "translationY", y);
        animatorXY.playTogether(objectX, objectY);
        animatorXY.setDuration(500);
        animatorXY.start();
    }

}
