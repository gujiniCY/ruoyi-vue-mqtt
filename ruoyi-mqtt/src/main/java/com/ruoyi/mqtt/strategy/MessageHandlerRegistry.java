package com.ruoyi.mqtt.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * MessageHandlerRegistry 类是消息处理策略注册表，负责注册和获取消息处理策略。
 * 通过扫描 Spring ApplicationContext 中的所有实现 MessageHandlerStrategy 接口的 Bean，
 * 将它们注册到注册表中，并提供方法根据主题获取对应的消息处理策略。
 */
@Component
public class MessageHandlerRegistry {
    /**
     * Spring 应用上下文，用于获取 MessageHandlerStrategy 类型的 Bean。
     */
    private final ApplicationContext applicationContext;
    /**
     * 存储主题模式和消息处理策略的映射关系。
     */
    private final Map<String, MessageHandlerStrategy> handlers = new HashMap<>();

    /**
     * 构造函数，注入 Spring 应用上下文，并在初始化时注册所有消息处理策略。
     *
     * @param applicationContext Spring 应用上下文
     */
    @Autowired
    public MessageHandlerRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        registerAllHandlers();
    }

    /**
     * 扫描 Spring ApplicationContext 中的所有 MessageHandlerStrategy 类型的 Bean，
     * 并将它们注册到注册表中。
     */
    private void registerAllHandlers() {
        Map<String, MessageHandlerStrategy> handlerBeans = applicationContext.getBeansOfType(MessageHandlerStrategy.class);
        for (Map.Entry<String, MessageHandlerStrategy> entry : handlerBeans.entrySet()) {
            MessageHandlerStrategy handler = entry.getValue();
            registerHandler(handler.getTopicPattern(), handler);
        }
    }

    /**
     * 注册消息处理策略到注册表中。
     *
     * @param topicPattern 主题模式
     * @param handler      消息处理策略
     */
    public void registerHandler(String topicPattern, MessageHandlerStrategy handler) {
        handlers.put(topicPattern, handler);
    }

    /**
     * 根据主题获取对应的消息处理策略。
     *
     * @param topic 消息主题
     * @return 对应的消息处理策略，如果未找到则返回 null
     */
    public MessageHandlerStrategy getHandler(String topic) {
        for (Map.Entry<String, MessageHandlerStrategy> entry : handlers.entrySet()) {
            String topicPattern = entry.getKey();
            if (topic.matches(topicPattern)) {
                return entry.getValue();
            }
        }
        return null;
    }

}
