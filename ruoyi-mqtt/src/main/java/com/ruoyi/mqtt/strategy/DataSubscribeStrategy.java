package com.ruoyi.mqtt.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataSubscribeStrategy implements MessageHandlerStrategy {
    @Override
    public void handle(String payload) {
        log.info("DataSubscribeStrategy收到消息:" + payload);
    }

    @Override
    public String getTopicPattern() {
        return "/api/v1/.*mqtt/dataSubscribe";
    }
}
