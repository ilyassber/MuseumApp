package com.alpha.museum.museum.tools;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BeaconScan {

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
}
