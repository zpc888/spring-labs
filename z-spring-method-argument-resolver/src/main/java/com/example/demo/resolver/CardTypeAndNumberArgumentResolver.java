package com.example.demo.resolver;

import com.example.demo.exception.BusinessSecurityException;
import com.example.demo.model.CardTypeAndNumber;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CardTypeAndNumberArgumentResolver implements HandlerMethodArgumentResolver {

    private final RedisTemplate<String, String> redisTemplate;

    public CardTypeAndNumberArgumentResolver(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentCard.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String accessToken = request.getHeader("x-access-token");
        
        if (accessToken == null || accessToken.isEmpty()) {
            throw new BusinessSecurityException("Missing access token");
        }
        
        String cardType = redisTemplate.opsForValue().get("cardType:" + accessToken);
        String cardNumber = redisTemplate.opsForValue().get("cardNumber:" + accessToken);
        
        if (cardType == null || cardNumber == null) {
            throw new BusinessSecurityException("Invalid access token - card not found");
        }
        
        return new CardTypeAndNumber(
                com.example.demo.model.CardType.valueOf(cardType),
                cardNumber
        );
    }
}