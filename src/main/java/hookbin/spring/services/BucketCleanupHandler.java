package hookbin.spring.services;

import lombok.extern.slf4j.Slf4j;
import hookbin.model.Bucket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BucketCleanupHandler {

    @Autowired
    private CapturedRequestRepository repo;
    
    @Value("${bucket.ttl:360000}")
    private long ttl;
    
    @Scheduled(fixedRate = 30000)
    public void cleanup() {
        for (Bucket b : repo.getBuckets()) {
            if ((b.getCreatedTs().getTime() + ttl) < System.currentTimeMillis()) {
                log.debug("expiring bucket: {}", b);
                repo.deleteBucket(b.getBucketId());
            }
        }
    }
}
