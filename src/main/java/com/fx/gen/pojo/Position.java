package com.fx.gen.pojo;

/**
 *
 * @author pscha
 */
public class Position {

    private double lon;
    private double lat;
    private int idx=0;
    
    public Position(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public Position(double lon, double lat, int idx) {
        this.lon = lon;
        this.lat = lat;
        this.idx = idx;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }
}
