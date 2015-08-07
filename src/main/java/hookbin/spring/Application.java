package hookbin.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAutoConfiguration
@EnableScheduling
@EnableHypermediaSupport(type = {HypermediaType.HAL})
@ComponentScan(basePackages = "hookbin.spring")
public class Application {

    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(Application.class);
    
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
