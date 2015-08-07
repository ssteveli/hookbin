package hookbin.model;

import hookbin.util.JsonDateSerializer;

import java.util.Date;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({
    "_links",
    "bucketId",
    "createdTsEpoch",
    "createdTs",
    "lastUpdatedTsEpoch",
    "lastUpdatedTs",
    "requestCount",
    "ttl"
})
@JsonDeserialize(builder = Bucket.BucketBuilder.class)
public class Bucket extends ResourceSupport {
    private final String bucketId;
    private final Date createdTsEpoch;
    private final Date lastUpdatedTsEpoch;
    private final int requestCount;
    private final long ttl;
    
    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getCreatedTs() {
        return createdTsEpoch;
    }
    
    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getLastUpdatedTs() {
        return lastUpdatedTsEpoch;
    }

}
