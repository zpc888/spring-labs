package com.example.sb4jackson.config;

import com.example.sb4jackson.http.TestModelV2HttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        builder.registerDefaults()
                .configureMessageConvertersList(converters -> converters.add(0, new TestModelV2HttpMessageConverter()));
    }
}
