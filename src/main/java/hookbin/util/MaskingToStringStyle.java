package hookbin.util;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringStyle;

public class MaskingToStringStyle extends ToStringStyle {
    private static final long serialVersionUID = -6237524369785634313L;

    private final List<String> fields;
    
    public MaskingToStringStyle(String... fields) {
        this.fields = Arrays.asList(fields);
    }
    
    @Override
    public void appendDetail(
            StringBuffer buffer, 
            String fieldName, 
            Object value) {
        if (fields.contains(fieldName)) {
            if (value instanceof String) {
                buffer.append(StringUtils.repeat("#", ((String)value).length()));
            } else {
                buffer.append("MASKED");
            }
        } else {
            super.appendDetail(buffer, fieldName, value);
        }
    }
}
