package hookbin.spring.web.api;

import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;
import hookbin.model.Bucket;
import hookbin.model.CapturedRequest;
import hookbin.spring.BucketIdGenerator;
import hookbin.spring.services.CapturedRequestRepository;
import hookbin.spring.web.BuckerReceiverController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/buckets")
public class BucketController {
    
    @Autowired
    private BucketIdGenerator idGenerator;
    
    @Autowired
    private CapturedRequestRepository repo;
    
    @RequestMapping(value = "/{bucketId}/requests", method = {RequestMethod.GET})
    public ResponseEntity<List<CapturedRequest>> getRequests(
            @PathVariable("bucketId") String bucketId) {
        List<CapturedRequest> requests = repo.getRequests(bucketId);
        
        if (requests == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        for (CapturedRequest r : requests) {
            resolveLinks(bucketId, r);
        }
        
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/{bucketId}/requests/{requestId}", method = {RequestMethod.GET})
    public ResponseEntity<CapturedRequest> getRequest(
            @PathVariable("bucketId") String bucketId,
            @PathVariable("requestId") String requestId) {
        
        CapturedRequest request = repo.findById(bucketId, requestId);
        
        if (request == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        return new ResponseEntity<>(resolveLinks(bucketId, request), HttpStatus.OK);
    }
    
    @RequestMapping(value = "/{bucketId}", method = {RequestMethod.GET})
    public ResponseEntity<Bucket> getBucket(@PathVariable("bucketId") String bucketId) {
        Bucket b = repo.getBucket(bucketId);
        
        if (b == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(resolveLinks(b), HttpStatus.OK);
        }
    }
    
    @RequestMapping(value = "/{bucketId}", method = {RequestMethod.DELETE})
    public ResponseEntity<?> deleteBucket(@PathVariable("bucketId") String bucketId) {
        repo.deleteBucket(bucketId);
        
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @RequestMapping(method = {RequestMethod.POST})
    public ResponseEntity<?> createBucket() {
        String id = idGenerator.generateId();
        Bucket b = repo.createBucket(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(linkTo(methodOn(BucketController.class).getBucket(id)).toUri());
        
        return new ResponseEntity<Bucket>(resolveLinks(b), headers, HttpStatus.CREATED);
    }
    
    private Bucket resolveLinks(Bucket b) {
        b.add(linkTo(methodOn(BucketController.class).getBucket(b.getBucketId())).withSelfRel());
        b.add(linkTo(BuckerReceiverController.class).slash(b.getBucketId()).withRel("receive"));
        
        if (b.getRequestCount() > 0) {
            b.add(linkTo(methodOn(BucketController.class).getRequests(b.getBucketId())).withRel("requests")); 
        }
        
        return b;
    }
    
    private CapturedRequest resolveLinks(String bucketId, CapturedRequest r) {
        r.add(linkTo(methodOn(BucketController.class).getRequest(bucketId, r.getRequestId())).withSelfRel());
        r.add(linkTo(methodOn(BucketController.class).getBucket(bucketId)).withRel("bucket"));
        return r;
    }
}
