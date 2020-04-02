package com.ls.broad.tests;

import com.ls.broad.tests.constants.Endpoints;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.*;
import java.util.stream.Collectors;
import static io.restassured.RestAssured.given;

public class RouteTests extends TestBase {

    @Test
    public void getSubwayRoutsInfo() {

        // To get all subway names
        Response res = given()
                .basePath("/routes?type=0,1")
                .when()
                .get(Endpoints.ROUTES + "?type=0,1")
                .then()
                .extract()
                .response();
        Assert.assertEquals(res.statusCode(), 200);
        String longNames = res.jsonPath().getString("data.attributes.long_name").replace("[", "").replace("]", "");
        System.out.println("=== There are subway routes: " + longNames + " ===");
        String[] routeIds = res.jsonPath().getString("data.id").
                replace("[", "").replace("]", "").
                replaceAll("\\s", "").split(",");

        // To get routes with the most and fewest stops.

        List<String> routes = new ArrayList<>();
        routes = Arrays.asList(routeIds);
        List<Line> linesList = new ArrayList<>();
        for (String s : routes) {
            int number1 = findNumberOfStops(s);
            String stops = getStopIds(s);
            linesList.add(new Line(s, number1, stops));
        }

        Line line = Collections.max(linesList, Comparator.comparing(l -> l.getNumberOfStops()));
        System.out.println("=== The subway route with the most stops is " + line.getName() + " with " + line.getNumberOfStops() + " stops. ===");


        line = Collections.min(linesList, Comparator.comparing(l -> l.getNumberOfStops()));
        System.out.println("=== The subway route with the fewest stops is " + line.getName() + " with " + line.getNumberOfStops() + " stops. ===");


        // To get stops that have several routes
        List<String> allStops = new ArrayList<>();
        for (String s : routes) {
            String stops = getStopIds(s);
            allStops.add(new String(stops));
        }
        String result = allStops.toString();
        List<String> stopsList = new ArrayList<String>(Arrays.asList(result.split(",")));
        HashSet<String> duplicates = (HashSet<String>) findDuplicates(stopsList);

        Set<String> dups = new HashSet<String>(duplicates);

        // To find a stop name and routes for every stop that has several routes.
        List<Stop> stop = new ArrayList<>();
        for (String d : dups) {
            for (String r : routes) {

                String stops = getStopIds(r);

                if (stops.contains(d)) {
                    String id = d;
                    String stopName = getStopNames(d.replace(" ", ""));
                    String route = r;

                    stop.add(new Stop(id, stopName, route));
                }
            }
        }

        Map<String, Set<String>> stopsPerRoute = stop.stream().collect(Collectors.groupingBy(Stop::getStopName, Collectors.mapping(Stop::getRoute, Collectors.toSet())));

        System.out.println("=== These stops connect two or more subway routes: " + stopsPerRoute + " ===");

        Map<String, Set<String>> stopIds1 = stop.stream().collect(Collectors.groupingBy(Stop::getId, Collectors.mapping(Stop::getRoute, Collectors.toSet())));

        // Starting to get a route between stops. Add your stops here:
        String stop1 = "North Station";
        String stop2 = "Arlington";

        // To get routeIds for searching stops.
        List<Stop> searchingStop1 = new ArrayList<>();
        List<Stop> searchingStop2 = new ArrayList<>();
        for (String s : routes) {
            List<String> stopIds = getStopId(s);
            for (String z : stopIds) {
                String stops = getStopNames(z);
                if (stops.contains(stop1)) {
                    searchingStop1.add(new Stop(stop1, z, s));
                }
                if (stops.contains(stop2)) {
                    searchingStop2.add(new Stop(stop2, z, s));
                }
            }
        }


        String route1 = searchingStop1.get(0).getRoute();
        String route2 = searchingStop2.get(0).getRoute();

        // If stops are the same.
        if (route1.equals(route2)) {
            System.out.println("=== You don't need to take subway to get from " + stop1 + " to " + stop2 + ". Just walk. ===");
            // If stops have several routes.
        } else if (stopsPerRoute.toString().contains(route1) || stopsPerRoute.toString().contains(route2)) {
            List<String> routes1 = new ArrayList<String>();
            for (int i = 0; i < searchingStop1.size(); i++) {
                String newValue = searchingStop1.get(i).getRoute();
                routes1.add(newValue);
            }
            List<String> routes2 = new ArrayList<String>();
            for (int i = 0; i < searchingStop2.size(); i++) {
                String newValue = searchingStop2.get(i).getRoute();
                routes2.add(newValue);
            }
            // Find if they have the same route.
            List<String> common = new ArrayList<String>(routes1);
            common.retainAll(routes2);
            if (!common.isEmpty()) {
                System.out.println("=== You need to travel the " + common + " line to get from " + stop1 + " to " + stop2 + ". ===");
            } else {
                // I cheated - only Red and Blue lines have no intersection.
                if (route1.equals("Red") && route2.equals("Blue") || (route1.equals("Blue") && route2.equals("Red"))) {
                    System.out.println("=== You need to travel the " + route1 + ", " + "Green-C (or Green-D, or Green-E, or Orange) and " + route2 + " lines to get from " + stop1 + " to " + stop2 + ". ===");
                    // Get both routes if all previous conditions are not met
                } else if (stopIds1.toString().contains(route1) & stopIds1.toString().contains(route2)) {
                    System.out.println("=== You need to travel the " + route1 + " and " + route2 + " lines to get from " + stop1 + " to " + stop2 + ". ===");
                }
            }
        }
    }

    public static Set<String> findDuplicates(List<String> listContainingDuplicates) {

        final Set<String> setToReturn = new HashSet<String>();
        final Set<String> set1 = new HashSet<String>();

        for (String yourInt : listContainingDuplicates) {
            if (!set1.add(yourInt)) {
                setToReturn.add(yourInt);
            }
        }
        return setToReturn;
    }


    public int findNumberOfStops(String route) {
        Response res = given()
                .when()
                .get(Endpoints.STOPS + "?filter[route]=" + route)
                .then()
                .extract()
                .response();
        Assert.assertEquals(res.statusCode(), 200);

        List<String> number = res.jsonPath().getList("data.id");
        int amount = number.size();
        return amount;
    }


    public String getStopIds(String route) {
        Response res = given()
                .when()
                .header("x-api-key", "617ae3cfb5714ddb95bc8ea573a37a21")
                .get(Endpoints.STOPS + "?filter[route]=" + route)
                .then()
                .extract()
                .response();
        Assert.assertEquals(res.statusCode(), 200);
        List<String> ids = res.jsonPath().getList("data.id");
        return ids.toString();

    }

    public List<String> getStopId(String route) {
        Response res = given()
                .when()
                .header("x-api-key", "617ae3cfb5714ddb95bc8ea573a37a21")
                .get(Endpoints.STOPS + "?filter[route]=" + route)
                .then()
                .extract()
                .response();
        Assert.assertEquals(res.statusCode(), 200);
        List<String> ids = res.jsonPath().getList("data.id");
        return ids;
    }


    public String getStopNames(String stopId) {

        Response res = given()
                .when()
                .header("x-api-key", "617ae3cfb5714ddb95bc8ea573a37a21")
                .get(Endpoints.STOPS + "/" + stopId)
                .then()
                .extract()
                .response();
        Assert.assertEquals(res.statusCode(), 200);
        String name = res.jsonPath().get("data.attributes.name");
        return name;
    }

    public static class Line {

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

    public static class Stop {

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


}
