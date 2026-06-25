package com.example.sb4jackson.serialization.v3;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class V3DateDeserializer extends ValueDeserializer<Date> {

    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    @Override
    public Date deserialize(JsonParser parser, DeserializationContext context) throws tools.jackson.core.JacksonException {
        SimpleDateFormat formatter = new SimpleDateFormat(PATTERN, Locale.US);
        formatter.setTimeZone(UTC);
        try {
            return formatter.parse(parser.getValueAsString());
        } catch (ParseException exception) {
            throw tools.jackson.databind.exc.InvalidFormatException.from(
                    parser,
                    "Failed to parse dateFiled",
                    parser.getValueAsString(),
                    Date.class
            );
        }
    }
}
