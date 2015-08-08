package hookbin.spring.web;

import hookbin.model.CapturedRequest;
import hookbin.spring.services.CapturedRequestRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class BuckerReceiverController {

    @Autowired
    private CapturedRequestRepository repo;
    
    @RequestMapping(
            value = "/{bucketId}", 
            method = {RequestMethod.POST})
    public ResponseEntity<?> receiveWebhook(
            @PathVariable("bucketId") @NotNull @NotBlank String bucketId,
            HttpServletRequest request) throws IOException {
        
        if (repo.getBucket(bucketId) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        // read in the body
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        StringBuffer buf = new StringBuffer();
        String line = null;
        while ((line = reader.readLine()) != null) {
            buf.append(line);
        }
        reader.close();
        
        // grab the headers
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> e = request.getHeaderNames();
        while (e.hasMoreElements()) {
            String k = e.nextElement();
            headers.put(k, request.getHeader(k));
        }
        
        CapturedRequest r = CapturedRequest.builder()
                .body(buf.toString())
                .headers(headers)
                .build();
        repo.save(bucketId, r);
        log.debug("saved payload length {} for bucketId {}", r.getBody().length(), bucketId);
        
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
