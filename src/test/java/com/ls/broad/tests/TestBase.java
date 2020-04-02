package com.ls.broad.tests;
import org.testng.annotations.BeforeClass;
import static io.restassured.RestAssured.baseURI;

public class TestBase {

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        baseURI = "https://api-v3.mbta.com/";

    }


}
