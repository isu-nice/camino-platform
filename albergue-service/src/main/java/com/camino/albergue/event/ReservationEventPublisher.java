package com.camino.albergue.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ReservationEventPublisher.class);
    private static final String CHANNEL = "reservation-events";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public ReservationEventPublisher(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public void publish(ReservationEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            log.info("이벤트 발행 시도: channel={}, payload={}", CHANNEL, json);
            redisTemplate.convertAndSend(CHANNEL, json);
            log.info("이벤트 발행 완료");
        } catch (Exception e) {
            log.error("이벤트 발행 실패", e);
        }
    }
}