package com.example.sb4jackson.serialization.v2;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class V2DateSerializer extends JsonSerializer<Date> {

    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    @Override
    public void serialize(Date value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat(PATTERN, Locale.US);
        formatter.setTimeZone(UTC);
        generator.writeString(formatter.format(value));
    }
}
