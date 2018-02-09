package hookbin.test;

import static com.jayway.restassured.RestAssured.given;
import static hookbin.test.matcher.UrlMatcher.canFollow;
import static hookbin.test.matcher.UrlMatcher.isValid;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.jayway.restassured.http.ContentType;

import hookbin.model.Bucket;
import hookbin.spring.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class DeleteBucketRequestsFT extends AbstractBucketTest {
    
    Resource<Bucket> bucket;
    
    @Before
    public void setupBucket() throws JsonParseException, JsonMappingException, IOException {        
        bucket = createBucket();
    }
    
    @Test
    public void bucketWithRequestsCanBeDeleted() {
        int total = 10;
        
        // post something to the bucket
        for (int i=0; i<total; i++) {
            given()
                .body(UUID.randomUUID().toString())
            .when()
                .post(bucket.getLink("receive").getHref())
            .then()
                .statusCode(HttpStatus.SC_OK);
        }
              
        // refresh our bucket
        String requestUrl = given()
            .accept(ContentType.JSON)
        .when()
            .get(bucket.getLink("self").getHref())
        .then()
            .statusCode(HttpStatus.SC_OK)
            .body("requestCount", equalTo(total))
            .body("_links.requests.href", allOf(isValid(), canFollow()))
            .extract().path("_links.requests.href");
        
        // delete all requests
        given()
        .when()
            .delete(requestUrl)
        .then()
            .statusCode(HttpStatus.SC_NO_CONTENT);
        
        given()
            .accept(ContentType.JSON)
        .when()
            .get(bucket.getLink("self").getHref())
        .then()
            .statusCode(HttpStatus.SC_OK)
            .body("requestCount", equalTo(0))
            .body("_links.requests", nullValue());
    }
}
