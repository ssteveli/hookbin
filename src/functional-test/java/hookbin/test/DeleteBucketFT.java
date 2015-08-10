package hookbin.test;

import static com.jayway.restassured.RestAssured.*;

import hookbin.model.Bucket;
import hookbin.spring.Application;

import java.io.IOException;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.hateoas.Resource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class DeleteBucketFT extends AbstractBucketTest {
    
    Resource<Bucket> bucket;
    
    @Autowired
    private ObjectMapper om;
    
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
