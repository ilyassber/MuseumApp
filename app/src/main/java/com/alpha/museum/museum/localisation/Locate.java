package com.alpha.museum.museum.localisation;

import com.alpha.museum.museum.models.Location;

public class Locate {

    void getLocation (Location location) {

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

        dist_x = location.getBeacon_a().getX() - location.getBeacon_b().getX();
        dist_y = location.getBeacon_a().getY() - location.getBeacon_b().getY();
        dist = Math.sqrt(Math.pow(dist_x, 2) + Math.pow(dist_y, 2));
        alpha = location.getDist_a() / dist;
        med[0] = location.getBeacon_a().getX() - (dist_x * alpha);
        med[1] = location.getBeacon_a().getY() - (dist_y * alpha);
        dist_a = dist * alpha;
        h = Math.sqrt(Math.pow(dist_a, 2) + Math.pow(location.getDist_a(), 2));
        pt1[0] = med[0] + (h * (location.getBeacon_b().getY() - location.getBeacon_a().getY()) / dist);
        pt1[1] = med[1] - (h * (location.getBeacon_b().getX() - location.getBeacon_a().getX()) / dist);
        pt2[0] = med[0] - (h * (location.getBeacon_b().getY() - location.getBeacon_a().getY()) / dist);
        pt2[1] = med[1] + (h * (location.getBeacon_b().getX() - location.getBeacon_a().getX()) / dist);
        dist_1 = Math.sqrt(Math.pow((pt1[0] - location.getBeacon_c().getX()), 2) + Math.pow((pt1[1] - location.getBeacon_c().getY()), 2));
        dist_2 = Math.sqrt(Math.pow((pt2[0] - location.getBeacon_c().getX()), 2) + Math.pow((pt2[1] - location.getBeacon_c().getY()), 2));
        if (Math.abs(dist_1 - location.getDist_c()) < Math.abs(dist_2 - location.getDist_c())) {
            location.setX((int) (pt1[0] + 0.5));
            location.setY((int) (pt1[1] + 0.5));
        } else {
            location.setX((int) (pt2[0] + 0.5));
            location.setY((int) (pt2[1] + 0.5));
        }
    }
}
