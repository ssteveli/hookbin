package hookbin.test;

import static com.jayway.restassured.RestAssured.*;
import static hookbin.test.matcher.UrlMatcher.*;
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
