package hookbin.test;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static hookbin.test.matcher.UrlMatcher.*;

import hookbin.model.Bucket;
import hookbin.spring.Application;

import java.io.IOException;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class DeleteBucketRequestsFT extends AbstractBucketTest {
    
    Resource<Bucket> bucket;
    
    @Autowired
    private ObjectMapper om;
    
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
