package com.example.sb4jackson.serialization.v3;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class V3DateSerializer extends ValueSerializer<Date> {

    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    @Override
    public void serialize(Date value, JsonGenerator generator, SerializationContext context) throws tools.jackson.core.JacksonException {
        SimpleDateFormat formatter = new SimpleDateFormat(PATTERN, Locale.US);
        formatter.setTimeZone(UTC);
        generator.writeString(formatter.format(value));
    }
}
