package com.ruoyi.mqtt.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * DataRespStrategy 类是用于处理 "/api/v1/.*mqtt/dataResp" 主题消息的消息处理策略。
 * 实现了 MessageHandlerStrategy 接口，提供了处理消息和获取主题模式的方法。
 */
@Component
@Slf4j
public class DataRespStrategy implements MessageHandlerStrategy {
    /**
     * 处理消息的方法
     *
     * @param payload 消息主体
     */
    @Override
    public void handle(String payload) {
        log.info("收到消息:" + payload);
    }

    /**
     * 获取该策略处理的消息主题的模式。
     *
     * @return 主题模式字符串
     */
    @Override
    public String getTopicPattern() {
        return "/api/v1/.*mqtt/dataResp";
    }
}
