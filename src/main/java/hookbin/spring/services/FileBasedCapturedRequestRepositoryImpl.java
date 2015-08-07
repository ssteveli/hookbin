package hookbin.spring.services;

import hookbin.model.Bucket;
import hookbin.model.CapturedRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileBasedCapturedRequestRepositoryImpl implements CapturedRequestRepository {

    private final File baseDir;
    
    @Value("${bucket.ttl:360000}")
    private long ttl;
    
    public FileBasedCapturedRequestRepositoryImpl() {
        baseDir = new File("./datastore");
        if (!baseDir.exists()) {
            baseDir.mkdir();
        }
    }
    
    @Override
    public void save(String bucketId, CapturedRequest request) {
        File bucket = new File(baseDir, bucketId);
        if (!bucket.exists()) {
            bucket.mkdir();
        }
        
        try {
            File crf = new File(bucket, request.getRequestId());
            FileOutputStream of = new FileOutputStream(crf);
            ObjectOutputStream oos = new ObjectOutputStream(of);
            oos.writeObject(request);
            oos.close();
            of.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CapturedRequest> getRequests(String bucketId) {
        File bucket = new File(baseDir, bucketId);
        if (!bucket.exists()) {
            return Collections.emptyList();
        }        
        
        List<CapturedRequest> results = new ArrayList<>();
        for (File crf : bucket.listFiles()) {
            try {
                FileInputStream fis = new FileInputStream(crf);
                ObjectInputStream ois = new ObjectInputStream(fis);
                CapturedRequest r = (CapturedRequest)ois.readObject();
                ois.close();
                fis.close();
                
                results.add(r);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }            
        }
        
        Collections.sort(results, new Comparator<CapturedRequest>() {
            @Override
            public int compare(CapturedRequest o1, CapturedRequest o2) {
                return o2.getReceivedTs().compareTo(o1.getReceivedTs());
            }
        });
        
        return Collections.unmodifiableList(results);
    }
    
    @Override
    public CapturedRequest findById(String bucketId, String requestId) {
        File bucket = new File(baseDir, bucketId);
        if (!bucket.exists()) {
            return null;
        }
        
        File crf = new File(bucket, requestId);
        if (!crf.exists() || !crf.canRead()) {
            return null;
        }
        
        try {
            FileInputStream fis = new FileInputStream(crf);
            ObjectInputStream ois = new ObjectInputStream(fis);
            CapturedRequest r = (CapturedRequest)ois.readObject();
            ois.close();
            fis.close();
            
            return r;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Bucket createBucket(String bucketId) {
        File bucket = new File(baseDir, bucketId);
        if (!bucket.exists()) {
            if (bucket.mkdir()) {
                return buildBucket(bucket);
            }
        }
        
        return null;
    }

    @Override
    public Bucket getBucket(String bucketId) {
        File bucket = new File(baseDir, bucketId);
        if (bucket.exists()) {
            return buildBucket(bucket);
        }
        
        return null;
    }
    
    @Override
    public void deleteBucket(String bucketId) {
        File bucket = new File(baseDir, bucketId);
        if (bucket.exists()) {
            try {
                FileUtils.deleteDirectory(bucket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected Date getCreatedTime(File f) {
        try {
            Path p = Paths.get(f.getAbsolutePath());
            BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
            long ms = attr.creationTime().to(TimeUnit.MILLISECONDS);
            return new Date(ms);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Bucket> getBuckets() {
        List<Bucket> results = new ArrayList<Bucket>();
        for (File bucket : baseDir.listFiles()) {
            results.add(buildBucket(bucket));
        }
        
        Collections.sort(results, new Comparator<Bucket>() {
            @Override
            public int compare(Bucket o1, Bucket o2) {
                return o2.getCreatedTs().compareTo(o1.getCreatedTs());
            }
        });
        
        return Collections.unmodifiableList(results);
    }
    
    private Bucket buildBucket(File bucket) {
        Date d = null;
        for (File rf : bucket.listFiles()) {
            if (d == null) {
                d = getCreatedTime(rf);
            } else {
                Date ld = getCreatedTime(rf);
                if (ld.after(d)) {
                    d = ld;
                }
            }
        }
        
        Date created = getCreatedTime(bucket);
        return Bucket.builder()
                .bucketId(bucket.getName())
                .createdTsEpoch(created)
                .lastUpdatedTsEpoch(d)
                .requestCount(bucket.listFiles().length)
                .ttl((created.getTime() + ttl) - System.currentTimeMillis())
                .build();
    }
}
