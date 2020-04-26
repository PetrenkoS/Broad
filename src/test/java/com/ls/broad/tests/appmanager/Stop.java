package com.ls.broad.tests.appmanager;

public class Stop {

    private String id;
    private String stopName;
    private String route;

    public Stop(String id, String stopName, String route) {
        this.id = id;
        this.stopName = stopName;
        this.route = route;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }
}


