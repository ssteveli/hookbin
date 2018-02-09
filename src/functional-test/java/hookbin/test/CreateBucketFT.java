package hookbin.test;

import static com.jayway.restassured.RestAssured.given;
import static hookbin.test.matcher.UrlMatcher.canFollow;
import static hookbin.test.matcher.UrlMatcher.isValid;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.jayway.restassured.http.ContentType;

import hookbin.spring.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
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
