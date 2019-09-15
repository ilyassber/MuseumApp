package com.alpha.museum.museum.localisation;

import android.util.Log;

import com.alpha.museum.museum.models.Location;

public class Locate {

    private static String TAG = "BeaconCalc";

    public void getLocation (Location location) {

        double[] med = new double[2];
        double[] pt1 = new double[2];
        double[] pt2 = new double[2];
        double dist;
        double dist_a;
        double dist_x;
        double dist_y;
        double h;
        double alpha;
        double dist_1;
        double dist_2;

        Log.i(TAG, "getLocation: Dist A : " + location.getDist_a() + " Dist B : " + location.getDist_b() + " Dist C : " + location.getDist_c());

        if (location.getDist_a() != -1 && location.getDist_b() != -1 && location.getDist_c() != -1) {
            dist_x = location.getBeacon_a().getX() - location.getBeacon_b().getX();
            Log.i(TAG, "getLocation: DIST_X = " + dist_x);
            dist_y = location.getBeacon_a().getY() - location.getBeacon_b().getY();
            Log.i(TAG, "getLocation: DIST_Y = " + dist_y);
            dist = Math.sqrt(Math.pow(dist_x, 2) + Math.pow(dist_y, 2));
            Log.i(TAG, "getLocation: Dist = " + dist);
            alpha = location.getDist_a() / (location.getDist_a() + location.getDist_b());
            Log.i(TAG, "getLocation: Alpha = " + alpha);
            med[0] = location.getBeacon_a().getX() - (dist_x * alpha);
            med[1] = location.getBeacon_a().getY() - (dist_y * alpha);
            Log.i(TAG, "getLocation: MED_X = " + med[0] + " -- MED_Y = " + med[1]);
            dist_a = dist * alpha;
            Log.i(TAG, "getLocation: DIST_A = " + dist_a);
            h = Math.sqrt(Math.abs(Math.pow(location.getDist_a(), 2) - Math.pow(dist_a, 2)));
            Log.i(TAG, "getLocation: H = " + h);
            pt1[0] = med[0] + (h * dist_y / dist);
            pt1[1] = med[1] - (h * dist_x / dist);
            Log.i(TAG, "getLocation: POINT1_X = " + pt1[0] + " -- POINT1_Y = " + pt1[1]);
            pt2[0] = med[0] - (h * dist_y / dist);
            pt2[1] = med[1] + (h * dist_x / dist);
            Log.i(TAG, "getLocation: POINT2_X = " + pt2[0] + " -- POINT2_Y = " + pt2[1]);
            dist_1 = Math.sqrt(Math.pow((pt1[0] - location.getBeacon_c().getX()), 2) + Math.pow((pt1[1] - location.getBeacon_c().getY()), 2));
            Log.i(TAG, "getLocation: DIST1 = " + dist_1);
            dist_2 = Math.sqrt(Math.pow((pt2[0] - location.getBeacon_c().getX()), 2) + Math.pow((pt2[1] - location.getBeacon_c().getY()), 2));
            Log.i(TAG, "getLocation: DIST2 = " + dist_2);
            if (Math.abs(dist_1 - location.getDist_c()) < Math.abs(dist_2 - location.getDist_c())) {
                location.setX(pt1[0] + 0.5);
                location.setY(pt1[1] + 0.5);
            } else {
                location.setX(pt2[0] + 0.5);
                location.setY(pt2[1] + 0.5);
            }
        } else {
            location.setX(0);
            location.setY(0);
        }
    }
}
