package com.alpha.museum.museum.tools;

import android.util.Log;

import com.alpha.museum.museum.models.Location;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BeaconScan {

    private static final String TAG = "BeaconScan";

    public Beacon getCloserBeacon(Collection<Beacon> beacons) {
        Beacon beacon = null;
        List<Beacon> list = new ArrayList<>(beacons);
        for (int i = 0; i < list.size(); i++) {
            if (beacon != null) {
                if (beacon.getDistance() > list.get(i).getDistance()) {
                    beacon = list.get(i);
                }
            }
            else {
                beacon = list.get(i);
            }
        }
        return (beacon);
    }

    public Location getLocation(Collection<Beacon> beacons) {
        Location location = new Location();
        Beacon beacon = getCloserBeacon(beacons);
        Log.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + beacon.toString() + " <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        if (beacon != null) {
            location.setDist_a(beacon.getDistance());
            location.getBeacon_a().setX(Integer.parseInt(beacon.toString().substring(5, 8)));
            Log.i(TAG, "getLocation: " + beacon.toString().substring(5, 8));
            location.getBeacon_a().setY(Integer.parseInt(beacon.toString().substring(8, 11)));
            beacons.remove(beacon);
        }
        beacon = getCloserBeacon(beacons);
        if (beacon != null) {
            location.setDist_b(beacon.getDistance());
            location.getBeacon_b().setX(Integer.parseInt(beacon.toString().substring(5, 8)));
            location.getBeacon_b().setY(Integer.parseInt(beacon.toString().substring(8, 11)));
            beacons.remove(beacon);
        }
        beacon = getCloserBeacon(beacons);
        if (beacon != null) {
            location.setDist_b(beacon.getDistance());
            location.getBeacon_c().setX(Integer.parseInt(beacon.toString().substring(5, 8)));
            location.getBeacon_c().setY(Integer.parseInt(beacon.toString().substring(8, 11)));
            beacons.remove(beacon);
        }
        return location;
    }
}
