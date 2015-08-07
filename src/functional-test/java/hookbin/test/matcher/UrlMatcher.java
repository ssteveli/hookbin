package hookbin.test.matcher;

import static com.jayway.restassured.RestAssured.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpStatus;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import com.jayway.restassured.response.Response;

public class UrlMatcher
    extends CustomTypeSafeMatcher<String> {

    private final boolean canFollow;
    
    @Factory
    public static Matcher<String> isValid() {
        return new UrlMatcher();
    }

    public static Matcher<String> canFollow() {
        return new UrlMatcher(true);
    }
    
    public UrlMatcher() {
        super("Determines if URL is valid");
        this.canFollow = false;
    }

    public UrlMatcher(boolean canFollow) {
        super("Determines if URL is valid");
        this.canFollow = canFollow;
    }
    
    public boolean matchesSafely(String subject) {
        boolean returnValue;

        try {
            URL url = new URL(subject);
            if (canFollow) {
                Response r = given()
                .when()
                    .get(url)
                .thenReturn();
                
                if (r.getStatusCode() != HttpStatus.SC_OK) {
                    returnValue = false;
                } else {
                    returnValue = true;
                }
            } else {
                returnValue = true;
            }
        } catch (MalformedURLException e) {
            returnValue = false;
        }

        return returnValue;
    }
}
