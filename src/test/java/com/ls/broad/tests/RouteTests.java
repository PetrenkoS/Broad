package com.ls.broad.tests;

import com.ls.broad.tests.appmanager.Line;
import com.ls.broad.tests.appmanager.Stop;
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
            int number1 = app.getRouteHelper().findNumberOfStops(s);
            String stops = app.getRouteHelper().getStopIds(s);
            linesList.add(new Line(s, number1, stops));
        }

        Line line = Collections.max(linesList, Comparator.comparing(l -> l.getNumberOfStops()));
        System.out.println("=== The subway route with the most stops is " + line.getName() + " with " + line.getNumberOfStops() + " stops. ===");


        line = Collections.min(linesList, Comparator.comparing(l -> l.getNumberOfStops()));
        System.out.println("=== The subway route with the fewest stops is " + line.getName() + " with " + line.getNumberOfStops() + " stops. ===");


        // To get stops that have several routes
        List<String> allStops = new ArrayList<>();
        for (String s : routes) {
            String stops = app.getRouteHelper().getStopIds(s);
            allStops.add(new String(stops));
        }
        String result = allStops.toString();
        List<String> stopsList = new ArrayList<String>(Arrays.asList(result.split(",")));
        HashSet<String> duplicates = (HashSet<String>) app.getRouteHelper().findDuplicates(stopsList);

        Set<String> dups = new HashSet<String>(duplicates);

        // To find a stop name and routes for every stop that has several routes.
        List<Stop> stop = new ArrayList<>();
        for (String d : dups) {
            for (String r : routes) {

                String stops = app.getRouteHelper().getStopIds(r);

                if (stops.contains(d)) {
                    String id = d;
                    String stopName = app.getRouteHelper().getStopNames(d.replace(" ", ""));
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
            List<String> stopIds = app.getRouteHelper().getStopId(s);
            for (String z : stopIds) {
                String stops = app.getRouteHelper().getStopNames(z);
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
                // Only Red and Blue lines have no intersection.
                if (route1.equals("Red") && route2.equals("Blue") || (route1.equals("Blue") && route2.equals("Red"))) {
                    System.out.println("=== You need to travel the " + route1 + ", " + "Green-C (or Green-D, or Green-E, or Orange) and " + route2 + " lines to get from " + stop1 + " to " + stop2 + ". ===");
                    // Get both routes if all previous conditions are not met
                } else if (stopIds1.toString().contains(route1) & stopIds1.toString().contains(route2)) {
                    System.out.println("=== You need to travel the " + route1 + " and " + route2 + " lines to get from " + stop1 + " to " + stop2 + ". ===");
                }
            }
        }
    }


}
