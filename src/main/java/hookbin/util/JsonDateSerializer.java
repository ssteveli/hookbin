package hookbin.util;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.util.StdDateFormat;

public class JsonDateSerializer extends JsonSerializer<Date> {
	private static final DateFormat iso8601Format = 
			StdDateFormat.getISO8601Format(TimeZone.getTimeZone("UTC"), Locale.US);

	@Override
	public void serialize(Date d, JsonGenerator g, SerializerProvider s)
			throws IOException, JsonProcessingException {
		DateFormat myformat = (DateFormat) iso8601Format.clone();
		String formattedDate = myformat.format(d);
		g.writeString(formattedDate);
	}

}
