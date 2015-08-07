package hookbin.util;

public class JsonISO8601ZDateDeserializer extends JsonDateDeserializer {
    @Override
    protected String getDatePattern() {

        return "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    }
}
