package hookbin.test;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import hookbin.spring.Application;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.jayway.restassured.http.ContentType;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class CORSValidationFT extends AbstractBucketTest {

    @Test
    public void shouldReturnCORSHeaderFields() {
        given()
            .accept(ContentType.JSON)
        .when()
            .post(bucketUri)
        .then()
            .statusCode(HttpStatus.SC_CREATED)
            .header("Access-Control-Allow-Origin", equalTo("*"))
            .header("Access-Control-Allow-Methods", allOf(
                    containsString("POST"),
                    containsString("GET"),
                    containsString("PUT"),
                    containsString("OPTIONS"),
                    containsString("DELETE")))
            .header("Access-Control-Max-Age", equalTo("3600"))
            .header("Access-Control-Allow-Headers", containsString("x-requested-with"));
    }
    
    @Test
    public void shouldSupportOptionsHttpMethodWithCORSHeadersReturned() {
        given()
            .accept(ContentType.JSON)
        .when()
            .options(bucketUri)
        .then()
            .statusCode(HttpStatus.SC_OK)
            .header("Access-Control-Allow-Origin", equalTo("*"))
            .header("Access-Control-Allow-Methods", allOf(
                    containsString("POST"),
                    containsString("GET"),
                    containsString("PUT"),
                    containsString("OPTIONS"),
                    containsString("DELETE")))
            .header("Access-Control-Max-Age", equalTo("3600"))
            .header("Access-Control-Allow-Headers", containsString("x-requested-with"));
    }

}
