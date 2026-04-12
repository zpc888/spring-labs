package com.example.demo.config;

import com.example.demo.resolver.CardTypeAndNumberArgumentResolver;
import com.example.demo.resolver.ChannelArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CardTypeAndNumberArgumentResolver cardTypeAndNumberArgumentResolver;
    private final ChannelArgumentResolver channelArgumentResolver;

    public WebConfig(CardTypeAndNumberArgumentResolver cardTypeAndNumberArgumentResolver,
                     ChannelArgumentResolver channelArgumentResolver) {
        this.cardTypeAndNumberArgumentResolver = cardTypeAndNumberArgumentResolver;
        this.channelArgumentResolver = channelArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<org.springframework.web.method.support.HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(cardTypeAndNumberArgumentResolver);
        resolvers.add(channelArgumentResolver);
    }
}