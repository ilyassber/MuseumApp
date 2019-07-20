package com.alpha.museum.museum.models;

import java.io.Serializable;

public class Location implements Serializable {

    private Beacon beacon_a;
    private Beacon beacon_b;
    private Beacon beacon_c;
    private int x;
    private int y;

    public void Location(Beacon beacon_a, Beacon beacon_b, Beacon beacon_c) {
        this.beacon_a = beacon_a;
        this.beacon_b = beacon_b;
        this.beacon_c = beacon_c;
    }

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

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
