package com.example.sb4jackson.serialization.v2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class V2DateDeserializer extends JsonDeserializer<Date> {

    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    @Override
    public Date deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat(PATTERN, Locale.US);
        formatter.setTimeZone(UTC);
        try {
            return formatter.parse(parser.getValueAsString());
        } catch (ParseException exception) {
            throw com.fasterxml.jackson.databind.exc.InvalidFormatException.from(
                    parser,
                    "Failed to parse dateFiled",
                    parser.getValueAsString(),
                    Date.class
            );
        }
    }
}
