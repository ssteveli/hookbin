package hookbin.test;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static hookbin.test.matcher.UrlMatcher.*;

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
public class CreateBucketFT extends AbstractBucketTest {

    @Test
    public void shouldBeAbleToCreateABucket() {
        given()
            .accept(ContentType.JSON)
        .when()
            .post(bucketUri)
        .then()
            .statusCode(HttpStatus.SC_CREATED)
            .header("Location", allOf(isValid(), canFollow()))
            .body("_links.self.href", allOf(isValid(), canFollow()))
            .body("_links.receive.href", isValid())
            .body("bucketId", notNullValue())
            .body("createdTsEpoch", notNullValue())
            .body("createdTs", notNullValue())
            .body("requestCount", equalTo(0))
            .body("ttl", notNullValue());
    }
}
