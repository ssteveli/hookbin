package hookbin.spring.services;

import java.util.List;

import hookbin.model.Bucket;
import hookbin.model.CapturedRequest;

public interface CapturedRequestRepository {
    List<Bucket> getBuckets();
    Bucket createBucket(String bucketId);
    void deleteBucket(String bucketId);
    Bucket getBucket(String bucketId);
    void save(String bucketId, CapturedRequest request);
    CapturedRequest findById(String bucketId, String requestId);
    List<CapturedRequest> getRequests(String bucketId);
}
