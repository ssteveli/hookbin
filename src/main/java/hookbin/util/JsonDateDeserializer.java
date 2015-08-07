package hookbin.util;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public abstract class JsonDateDeserializer extends JsonDeserializer<Date> {
    private static Logger log = LoggerFactory.getLogger(JsonDateDeserializer.class);
    
    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        DateFormat myformat = getFormat();
        String dateString = jp.getText();
        
        if (dateString.matches("^\\d+$")) {
            log.debug("parsing date string [{}] as epoch", dateString);
            return new Date(Long.parseLong(dateString));
        } else {
            log.debug("parsing date string [{}] for json field {} using pattern [{}]", 
                    dateString, jp.getCurrentName(), getDatePattern());
            try {
                log.debug("date string {}",dateString);
                return myformat.parse(dateString);
            } catch (ParseException e) {
                log.error("parse error " , e);
                throw new JsonParseException(String.format(
                        "error parsing date string [%s] from json field [%s] using ISO8601 format [%s]",
                        dateString,
                        jp.getCurrentName(),
                        getDatePattern()), jp.getCurrentLocation());
            }
        }
    }

    protected DateFormat getFormat() {
        return new SimpleDateFormat(getDatePattern(), Locale.US);
    }
    
    abstract protected String getDatePattern();
}
