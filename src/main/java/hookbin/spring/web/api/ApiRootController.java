package hookbin.spring.web.api;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiRootController {

    @RequestMapping(method = {RequestMethod.GET})
    public ResponseEntity<?> getRoot() {
        ResourceSupport response = new ResourceSupport();
        response.add(linkTo(methodOn(ApiRootController.class).getRoot()).withSelfRel());
        response.add(linkTo(methodOn(BucketController.class).createBucket()).withRel("buckets"));
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
