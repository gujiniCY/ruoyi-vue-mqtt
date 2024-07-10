package com.ruoyi.mqtt.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * MessageService 类是消息处理服务的实现。
 * 通过 MessageHandlerRegistry 注册表获取对应主题的消息处理策略，并调用相应的处理方法来处理消息。
 */
@Service
public class MessageService {
    /**
     * 消息处理策略的注册表。
     */
    private final MessageHandlerRegistry handlerRegistry;

    /**
     * 构造函数，注入 MessageHandlerRegistry 实例。
     *
     * @param handlerRegistry 消息处理策略的注册表
     */
    @Autowired
    public MessageService(MessageHandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
    }

    /**
     * 处理 MQTT 消息的方法。根据主题获取对应的消息处理策略，然后调用处理方法处理消息。
     *
     * @param topic   消息主题
     * @param payload 消息主体
     */
    public void processMessage(String topic, String payload) {
        MessageHandlerStrategy handler = handlerRegistry.getHandler(topic);
        if (handler != null) {
            handler.handle(payload);
        } else {
            System.out.println("No handler found for topic: " + topic);
        }
    }
}
