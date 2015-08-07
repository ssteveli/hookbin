package hookbin.model;

import hookbin.util.JsonDateSerializer;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({
    "_links",
    "requestId",
    "receivedTsEpoch",
    "receivedTs",
    "headers",
    "body"
})
public class CapturedRequest extends ResourceSupport implements Serializable {
    private static final long serialVersionUID = 6823665251155870690L;
    private final String requestId = UUID.randomUUID().toString();
    private final Date receivedTsEpoch = new Date();
    private final Map<String, String> headers;
    private final String body;
    
    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getReceivedTs() {
        return receivedTsEpoch;
    }
}
