package mersys;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import mersys.model.Country;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class CountryTest {

    private Cookies cookies;
    private Country country = new Country();
    private String randomCountryName = randomString(7);
    private String randomCountryCode = randomString(3);
    private String countryId;

    @BeforeClass
    public void loginMersys() {

        baseURI = "https://demo.mersys.io";

        Map<String, String> loginDatas = new HashMap<>();
        loginDatas.put("username", "richfield.edu");
        loginDatas.put("password", "Richfield2020!");

        cookies = given()
                .body(loginDatas)
                .contentType(ContentType.JSON)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().response().getDetailedCookies()
        ;
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    public void createCountry() {

        country.setName(randomCountryName);
        country.setCode(randomCountryCode);

        countryId = given()
                .cookies(cookies)
                .contentType(ContentType.JSON)
                .body(country)
                .when()
                .post("/school-service/api/countries")
                .then()
                .statusCode(201)
                .body("name", equalTo(randomCountryName))
                .body("code", equalTo(randomCountryCode))
                .log().body()
                .extract().jsonPath().getString("id")
        ;
    }

    @Test(dependsOnMethods = {"createCountry"})
    @Severity(SeverityLevel.NORMAL)
    public void createCountryNegative() {

        country.setName(randomCountryName);
        country.setCode(randomCountryCode);

        given()
                .cookies(cookies)
                .contentType(ContentType.JSON)
                .body(country)
                .when()
                .post("/school-service/api/countries")
                .then()
                .statusCode(400)
                .body("message", equalTo("The Country with Name \"" + randomCountryName + "\" already exists."))
                .log().body()
        ;
    }

    @Test(dependsOnMethods = {"createCountryNegative"})
    @Severity(SeverityLevel.MINOR)
    public void updateCountry() {

        String countryName = randomString(9);
        String countryCode = randomString(3);
        country.setName(countryName);
        country.setCode(countryCode);
        country.setId(countryId);

        given()
                .cookies(cookies)
                .contentType(ContentType.JSON)
                .body(country)
                .when()
                .put("/school-service/api/countries")
                .then()
                .statusCode(200)
                .log().body()
                .body("name", equalTo(countryName))
                .body("code", equalTo(countryCode))
                .body("id", equalTo(countryId))
        ;
    }

    @Test(dependsOnMethods = {"updateCountry"})
    @Severity(SeverityLevel.TRIVIAL)
    public void deleteCountry() {

        given()
                .cookies(cookies)
                .pathParam("countryId", countryId)
                .when()
                .delete("/school-service/api/countries/{countryId}")
                .then()
                .statusCode(200)
        ;
    }

    @Test(dependsOnMethods = {"deleteCountry"})
    @Severity(SeverityLevel.BLOCKER)
    public void deleteCountryNegative() {

        given()
                .cookies(cookies)
                .pathParam("countryId", countryId)
                .when()
                .delete("/school-service/api/countries/{countryId}")
                .then()
                .statusCode(404)
        ;
    }


    public String randomString(int count) {
        return RandomStringUtils.randomAlphabetic(count).toUpperCase();
    }
}
