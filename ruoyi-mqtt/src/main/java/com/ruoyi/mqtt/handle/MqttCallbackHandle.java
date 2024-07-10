package com.ruoyi.mqtt.handle;

import com.ruoyi.mqtt.sender.IMqttSender;
import com.ruoyi.mqtt.strategy.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 处理topic回调
 * 进行消息处理
 */
@Service
@Slf4j
public class MqttCallbackHandle {
    @Resource
    private MessageService messageService;

    public void handle(String topic, String payload) {
        log.info("收到消息:主题:{}:消息内容:{},交给策略分发处理...", topic, payload);
        messageService.processMessage(topic, payload);
    }
}

