package hookbin.test;

import org.junit.After;
import org.junit.Before;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.hateoas.hal.Jackson2HalModule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.ObjectMapperConfig;
import com.jayway.restassured.config.RestAssuredConfig;

public abstract class AbstractTest {
    @LocalServerPort
    private int serverPort;

    @Before
    public void setUp() throws Exception {
        RestAssured.basePath = "/api";
        RestAssured.port = serverPort;

        RestAssured.config = RestAssuredConfig.config()
            .objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (@SuppressWarnings("rawtypes") Class clazz, String s) -> {
                    ObjectMapper om = new ObjectMapper();
                    return om.registerModule(new Jackson2HalModule());
                }
            ));
    }

    @After
    public void cleanup() {
        RestAssured.reset();
    }
}
