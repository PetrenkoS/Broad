package com.ls.broad.tests.appmanager;

public class Line {

    private String name;
    private int numberOfStops;
    private String stops;

    public Line(String name, int numberOfStops, String stops) {
        this.name = name;
        this.numberOfStops = numberOfStops;
        this.stops = stops;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfStops() {
        return numberOfStops;
    }

    public void setNumberOfStops(int numberOfStops) {
        this.numberOfStops = numberOfStops;
    }

    public String getStops() {
        return stops;
    }

    public void setStops(String stops) {
        this.stops = stops;
    }
}
