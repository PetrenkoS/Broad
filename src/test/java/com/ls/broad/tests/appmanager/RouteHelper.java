package com.ls.broad.tests.appmanager;

import com.ls.broad.tests.constants.Endpoints;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;

public class RouteHelper {
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
}
