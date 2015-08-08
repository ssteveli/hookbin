package hookbin.test;

import static com.jayway.restassured.RestAssured.*;
import static hookbin.test.matcher.UrlMatcher.*;
import static org.hamcrest.Matchers.*;
import hookbin.model.Bucket;
import hookbin.spring.Application;

import java.io.IOException;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.junit.After;
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
public class BucketReceiveLastRequestFT extends AbstractTest {
    
    Resource<Bucket> bucket;
    String lastRequestUrl;
    String body;
    
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
                .body("_links.requests", nullValue())
                .body("bucketId", notNullValue())
                .body("createdTsEpoch", notNullValue())
                .body("createdTs", notNullValue())
                .body("requestCount", equalTo(0))
                .body("ttl", notNullValue())
                .extract().asString();
        
        bucket = om.readValue(s, new TypeReference<Resource<Bucket>>() {});
        
        body = UUID.randomUUID().toString();
        given()
            .body(body)
        .when()
            .post(bucket.getLink("receive").getHref())
        .then()
            .statusCode(HttpStatus.SC_OK);
        
        lastRequestUrl = given()
                .when()
                    .get(bucket.getLink("self").getHref())
                .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body("requestCount", equalTo(1))
                    .body("_links.lastRequest.href", allOf(isValid(), canFollow()))
                    .extract().path("_links.lastRequest.href");
    }
    
    @After
    public void cleanupBucket() {
        given()
        .when()
            .delete(bucket.getLink("self").getHref())
        .then()
            .statusCode(HttpStatus.SC_NO_CONTENT);
    }
    
    @Test
    public void lastRequestCanBeRetrieved() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get(lastRequestUrl)
        .then()
            .statusCode(HttpStatus.SC_OK)
            .body("_links.self.href", allOf(isValid(), canFollow()))
            .body("_links.bucket.href", allOf(isValid(), canFollow()))
            .body("requestId", notNullValue())
            .body("receivedTsEpoch", notNullValue())
            .body("receivedTs", notNullValue())
            .body("body", equalTo(body));
    }
}
