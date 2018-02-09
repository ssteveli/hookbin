package hookbin.test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.jayway.restassured.http.ContentType;

import hookbin.spring.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
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
