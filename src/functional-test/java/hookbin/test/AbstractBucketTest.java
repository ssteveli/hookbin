package hookbin.test;

import static com.jayway.restassured.RestAssured.*;
import static hookbin.test.matcher.UrlMatcher.*;
import static org.hamcrest.Matchers.*;

import hookbin.model.Bucket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.http.ContentType;

public abstract class AbstractBucketTest extends AbstractTest {

    @Autowired
    private ObjectMapper om;
    
    protected URI bucketUri;
    
    @Before
    public void setupBucketUri() throws URISyntaxException {
        String url = 
                given()
                    .accept(ContentType.JSON)
                .when()
                    .get()
                .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body("_links.buckets.href", isValid())
                    .extract().path("_links.buckets.href");
        bucketUri = new URI(url);
    }
    
    protected Resource<Bucket> createBucket() {
        String s = 
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
                    .body("ttl", notNullValue())
                    .extract().asString();
            
        try {
            return om.readValue(s, new TypeReference<Resource<Bucket>>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
