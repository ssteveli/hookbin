package hookbin.test;

import static com.jayway.restassured.RestAssured.given;
import static hookbin.test.matcher.UrlMatcher.canFollow;
import static hookbin.test.matcher.UrlMatcher.isValid;
import static org.hamcrest.Matchers.allOf;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import com.jayway.restassured.http.ContentType;

import hookbin.spring.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class RootFT extends AbstractTest {

    @Test
    public void rootShouldReturnProperResponseWithHALLinks() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get()
        .then()
            .statusCode(HttpStatus.SC_OK)
            .contentType(ContentType.JSON)
            .body("_links.self.href", allOf(isValid(), canFollow()))
            .body("_links.buckets.href", isValid());
    }
}
