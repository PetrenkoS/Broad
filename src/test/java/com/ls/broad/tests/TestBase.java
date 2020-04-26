package com.ls.broad.tests;
import com.ls.broad.tests.appmanager.ApplicationManager;
import org.testng.annotations.BeforeClass;

import static io.restassured.RestAssured.baseURI;

public class TestBase {

    protected final ApplicationManager app = new ApplicationManager();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        baseURI = "https://api-v3.mbta.com/";

    }


}
