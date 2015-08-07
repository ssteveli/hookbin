package hookbin.test;

import static com.jayway.restassured.RestAssured.*;
import static hookbin.test.matcher.UrlMatcher.*;
import static org.hamcrest.Matchers.*;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.http.ContentType;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class DeleteBucketFT extends AbstractTest {
    
    Resource<Bucket> bucket;
    
    @Autowired
    private ObjectMapper om;
    
    @Before
    public void createBucket() throws JsonParseException, JsonMappingException, IOException {
        String s = 
            given()
                .accept(ContentType.JSON)
            .when()
                .post("/buckets")
            .then()
                .statusCode(HttpStatus.SC_CREATED)
                .header("Location", allOf(isValid(), canFollow()))
                .body("_links.self.href", allOf(isValid(), canFollow()))
                .body("_links.receive.href", isValid())
                .body("bucketId", notNullValue())
                .body("createdTsEpoch", notNullValue())
                .body("createdTs", notNullValue())
                .body("requestCount", equalTo(0))
                .body("ttl", notNullValue())
                .extract().asString();
        
        bucket = om.readValue(s, new TypeReference<Resource<Bucket>>() {});
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
