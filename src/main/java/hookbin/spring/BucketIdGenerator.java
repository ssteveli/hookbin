package hookbin.spring;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

@Service
public class BucketIdGenerator {
    public String generateId() {
        return RandomStringUtils.randomAlphanumeric(10);
    }
}
