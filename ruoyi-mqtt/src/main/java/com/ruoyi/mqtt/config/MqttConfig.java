package com.ruoyi.mqtt.config;

import cn.hutool.core.date.DateUtil;
import com.ruoyi.mqtt.handle.MqttCallbackHandle;
import com.ruoyi.mqtt.properties.MqttProperties;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.event.MqttConnectionFailedEvent;
import org.springframework.integration.mqtt.event.MqttSubscribedEvent;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * MQTT配置，生产者
 */
@Configuration
@Slf4j
public class MqttConfig {

    @Autowired
    private MqttProperties prop;

    /**
     * 订阅的bean名称
     */
    public static final String CHANNEL_NAME_IN = "mqttInboundChannel";
    /**
     * 发布的bean名称
     */
    public static final String CHANNEL_NAME_OUT = "mqttOutboundChannel";

    @Autowired
    private MqttCallbackHandle mqttCallbackHandle;

    /**
     * MQTT连接器选项
     *
     * @return {@link org.eclipse.paho.client.mqttv3.MqttConnectOptions}
     */
    @Bean
    public MqttConnectOptions getMqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，
        // 这里设置为true表示每次连接到服务器都以新的身份连接
        options.setCleanSession(prop.getCleanSession());
        // 设置连接的用户名
        options.setUserName(prop.getUsername());
        // 设置连接的密码
        options.setPassword(prop.getPassword().toCharArray());
        options.setServerURIs(prop.getHost());
        // 设置超时时间 单位为秒
        options.setConnectionTimeout(prop.getConnectionTimeout());
        // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送心跳判断客户端是否在线，但这个方法并没有重连的机制
        options.setKeepAliveInterval(prop.getKeepAliveInterval());
        // 断开后重连，但这个方法并没有重新订阅的机制
        // 在尝试重新连接之前，它将首先等待1秒，对于每次失败的重新连接尝试，延迟将加倍，直到达到2分钟，此时延迟将保持在2分钟。
        options.setAutomaticReconnect(prop.getAutoReconnect());
        // 设置“遗嘱”消息的话题，若客户端与服务器之间的连接意外中断，服务器将发布客户端的“遗嘱”消息。
        options.setWill(prop.getWillTopic(), prop.getWillContent().getBytes(StandardCharsets.UTF_8),
                prop.getWillQos(), prop.isWillRetained());
        return options;
    }


    /**
     * MQTT客户端
     *
     * @return {@link MqttPahoClientFactory}
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(getMqttConnectOptions());
        return factory;
    }

    /**
     * MQTT信息通道（生产者）
     *
     * @return {@link org.springframework.messaging.MessageChannel}
     */
    @Bean(name = CHANNEL_NAME_OUT)
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    /**
     * MQTT消息处理器（生产者）
     *
     * @return {@link org.springframework.messaging.MessageHandler}
     */
    @Bean
    @ServiceActivator(inputChannel = CHANNEL_NAME_OUT)
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(
                prop.getProducerClientId(),
                mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic(prop.getProducerDefaultTopic());
        messageHandler.setDefaultRetained(false);
        return messageHandler;
    }

    /**
     * MQTT消息订阅绑定（消费者）
     *
     * @return {@link org.springframework.integration.core.MessageProducer}
     */
    @Bean
    public MessageProducer inbound() {
        // 可以同时消费（订阅）多个Topic
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        prop.getConsumerClientId(), mqttClientFactory(), prop.getConsumerDefaultTopic());
        adapter.setCompletionTimeout(prop.getCompletionTimeout());
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(prop.getQos());
        // 设置订阅通道
        adapter.setOutputChannel(mqttInboundChannel());
        adapter.setErrorChannel(errorChannel());
        return adapter;
    }

    /**
     * MQTT信息通道（消费者）
     *
     * @return {@link org.springframework.messaging.MessageChannel}
     */
    @Bean(name = CHANNEL_NAME_IN)
    public MessageChannel mqttInboundChannel() {
        return new DirectChannel();
    }

    /**
     * MQTT消息处理器（消费者）
     *
     * @return {@link org.springframework.messaging.MessageHandler}
     */
    @Bean
    @ServiceActivator(inputChannel = CHANNEL_NAME_IN)
    public MessageHandler handler() {
        return message -> {
            String topic = Objects.requireNonNull(message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC)).toString();
            String payload = message.getPayload().toString();
            mqttCallbackHandle.handle(topic, payload);
        };
    }

    /**
     * @param event
     * @return void
     * @desc mqtt连接失败或者订阅失败时, 触发MqttConnectionFailedEvent事件
     */
    @EventListener(MqttConnectionFailedEvent.class)
    public void mqttConnectionFailedEvent(MqttConnectionFailedEvent event) {
        log.error("-----mqtt----连接mqtt失败: date={}, hostUrl={}, username={}, error={}",
                DateUtil.now(), prop.getHost(), prop.getUsername(), event.getCause().getMessage());
    }

    /**
     * @param event
     * @return void
     * @desc 成功订阅到主题，MqttSubscribedEvent事件就会被触发(多个主题,多次触发)
     */
    @EventListener(MqttSubscribedEvent.class)
    public void mqttSubscribedEvent(MqttSubscribedEvent event) {
        log.info("-----mqtt----订阅成功信息: date={}, info={}", DateUtil.now(), event.toString());
    }

    /**
     * 错误消息
     *
     * @return
     */
    @Bean
    public MessageChannel errorChannel() {
        return new DirectChannel();
    }
}

