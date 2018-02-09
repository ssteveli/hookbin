package hookbin.test;

import static com.jayway.restassured.RestAssured.given;

import java.io.IOException;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.hateoas.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import hookbin.model.Bucket;
import hookbin.spring.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class DeleteBucketFT extends AbstractBucketTest {
    
    Resource<Bucket> bucket;
    
    @Before
    public void setupBucket() throws JsonParseException, JsonMappingException, IOException {        
        bucket = createBucket();
    }
    
    @Test
    public void canDeleteBucket() {
        given()
        .when()
            .delete(bucket.getLink("self").getHref())
        .then()
            .statusCode(HttpStatus.SC_NO_CONTENT);
        
        given()
        .when()
            .get(bucket.getLink("self").getHref())
        .then()
            .statusCode(HttpStatus.SC_NOT_FOUND);
        
        // another delete request should be successful also
        given()
        .when()
            .delete(bucket.getLink("self").getHref())
        .then()
            .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void bucketWithRequestsCanBeDeleted() {
        // post something to the bucket
        given()
            .body(UUID.randomUUID().toString())
        .when()
            .post(bucket.getLink("receive").getHref())
        .then()
            .statusCode(HttpStatus.SC_OK);
        
        given()
        .when()
            .delete(bucket.getLink("self").getHref())
        .then()
            .statusCode(HttpStatus.SC_NO_CONTENT);
        
        given()
        .when()
            .get(bucket.getLink("self").getHref())
        .then()
            .statusCode(HttpStatus.SC_NOT_FOUND);
    }
}
