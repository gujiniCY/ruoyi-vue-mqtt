package com.ruoyi.mqtt.strategy;

import javax.validation.constraints.NotNull;

/**
 * MessageHandlerStrategy 接口定义了处理 MQTT 消息的策略。
 * 实现类需要提供处理消息的具体逻辑，并定义用于匹配主题的模式。
 */
public interface MessageHandlerStrategy {

    /**
     * 处理 MQTT 消息的方法。
     *
     * @param payload 消息主体
     */
    void handle(@NotNull String payload);

    /**
     * 获取用于匹配主题的模式。
     *
     * @return 主题模式字符串
     */
    String getTopicPattern();
}
