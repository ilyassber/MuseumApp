package com.alpha.museum.museum.models;

import java.io.Serializable;

public class Location implements Serializable {

    private Beacon beacon_a = new Beacon();
    private Beacon beacon_b = new Beacon();
    private Beacon beacon_c = new Beacon();
    private double dist_a = -1;
    private double dist_b = -1;
    private double dist_c = -1;
    private double x = 0;
    private double y = 0;

    public Beacon getBeacon_a() {
        return beacon_a;
    }

    public void setBeacon_a(Beacon beacon_a) {
        this.beacon_a = beacon_a;
    }

    public Beacon getBeacon_b() {
        return beacon_b;
    }

    public void setBeacon_b(Beacon beacon_b) {
        this.beacon_b = beacon_b;
    }

    public Beacon getBeacon_c() {
        return beacon_c;
    }

    public void setBeacon_c(Beacon beacon_c) {
        this.beacon_c = beacon_c;
    }

    public double getDist_a() {
        return dist_a;
    }

    public void setDist_a(double dist_a) {
        this.dist_a = dist_a;
    }

    public double getDist_b() {
        return dist_b;
    }

    public void setDist_b(double dist_b) {
        this.dist_b = dist_b;
    }

    public double getDist_c() {
        return dist_c;
    }

    public void setDist_c(double dist_c) {
        this.dist_c = dist_c;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
