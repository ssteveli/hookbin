package hookbin.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

public class JsonISO8601ZDateSerializer extends JsonDateSerializer {
    private static final String format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    @Override
    public void serialize(Date d, JsonGenerator g, SerializerProvider s) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        String formattedDate = dateFormat.format(d);

        g.writeString(formattedDate);
    }

}
