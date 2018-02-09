package hookbin.spring.web.api;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import hookbin.model.Bucket;
import hookbin.model.CapturedRequest;
import hookbin.spring.BucketIdGenerator;
import hookbin.spring.services.CapturedRequestRepository;
import hookbin.spring.web.BuckerReceiverController;
import hookbin.spring.web.api.BucketController.StreamContext.Type;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@RestController
@RequestMapping("/api/buckets")
@Slf4j
public class BucketController {
    private final BucketIdGenerator idGenerator;
    private final CapturedRequestRepository repo;
    private final ObjectMapper om;
    
    private List<StreamContext> streams = new ArrayList<>();
    
    @RequestMapping(value = "/{bucketId}/requests", method = {RequestMethod.DELETE})
    public ResponseEntity<List<CapturedRequest>> deleteRequests(
            @PathVariable("bucketId") String bucketId) {
        Bucket bucket = repo.getBucket(bucketId);
        
        if (bucket == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        repo.deleteRequests(bucketId);
        
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
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
    
    @RequestMapping(value = "/{bucketId}/lastRequest", method = {RequestMethod.GET})
    public ResponseEntity<CapturedRequest> getLastRequest(
            @PathVariable("bucketId") String bucketId) {
        
        List<CapturedRequest> requests = repo.getRequests(bucketId);
        
        if (requests == null || requests.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        return new ResponseEntity<>(resolveLinks(bucketId, requests.get(0)), HttpStatus.OK);
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
        streams.stream()
            .filter(context -> StringUtils.equals(bucketId, context.bucketId))
            .forEach(context -> context.emitter.complete());
        
        repo.deleteBucket(bucketId);
        
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @RequestMapping(method = {RequestMethod.POST})
    public ResponseEntity<?> createBucket() {
        Bucket b = null;
        for (int i=0; i<100; i++) {
            String id = idGenerator.generateId();
            b = repo.createBucket(id);
            if (b != null) {
                break;
            }
        }
        
        if (b == null) {
            log.error("error creating bucket, duplicate id problem?");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(linkTo(methodOn(BucketController.class).getBucket(b.getBucketId())).toUri());
        
        return new ResponseEntity<Bucket>(resolveLinks(b), headers, HttpStatus.CREATED);
    }
    
    @RequestMapping(value = "/{bucketId}/stream")
    public ResponseBodyEmitter streamingRequests(@PathVariable("bucketId") String bucketId, @RequestParam("jsonOnly") StreamContext.Type type) {
        final SseEmitter emitter = new SseEmitter();
        streams.add(StreamContext.builder()
                .bucketId(bucketId)
                .emitter(emitter)
                .type(type)
                .build());
        
        return emitter;
    }
    
    public void broadcastCapturedRequest(String bucketId, CapturedRequest capturedRequest) {
        streams.removeIf(context -> !context.connected);
        
        streams.stream()
            .filter(context -> StringUtils.equals(bucketId, context.bucketId))
            .forEach(context -> {
                try {
                    switch (context.type) {
                    case BODY:
                        context.emitter.send(capturedRequest.getBody());
                        break;
                    case REQUEST:
                        context.emitter.send(capturedRequest);
                        break;
                    }
                } catch (IOException e) {
                    log.error("error sending captured request to sse stream for bucketId {}", bucketId, e);
                    context.emitter.completeWithError(e);
                    context.connected = false;
                }
            });
    }
    
    private Bucket resolveLinks(Bucket b) {
        b.add(linkTo(methodOn(BucketController.class).getBucket(b.getBucketId())).withSelfRel());
        b.add(linkTo(BuckerReceiverController.class).slash(b.getBucketId()).withRel("receive"));
        b.add(linkTo(methodOn(BucketController.class).streamingRequests(b.getBucketId(), Type.REQUEST)).withRel("streamRequest"));
        b.add(linkTo(methodOn(BucketController.class).streamingRequests(b.getBucketId(), Type.BODY)).withRel("streamBody"));
        
        if (b.getRequestCount() > 0) {
            b.add(linkTo(methodOn(BucketController.class).getRequests(b.getBucketId())).withRel("requests")); 
            b.add(linkTo(methodOn(BucketController.class).getLastRequest(b.getBucketId())).withRel("lastRequest"));
        }
        
        return b;
    }
    
    private CapturedRequest resolveLinks(String bucketId, CapturedRequest r) {
        r.add(linkTo(methodOn(BucketController.class).getRequest(bucketId, r.getRequestId())).withSelfRel());
        r.add(linkTo(methodOn(BucketController.class).getBucket(bucketId)).withRel("bucket"));
        return r;
    }
    
    @Data
    @Builder
    static class StreamContext {
        public enum Type {
            REQUEST,
            BODY
        }
        
        public final String bucketId;
        public final SseEmitter emitter;
        public final Type type;
        @Builder.Default
        public boolean connected = true;
    }
}
