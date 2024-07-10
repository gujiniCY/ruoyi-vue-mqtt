package com.ruoyi.mqtt.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("mqtt")
@Data
public class MqttProperties {
    /**
     * mqtt地址
     */
    private String[] host;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，
     * 这里设置为true表示每次连接到服务器都以新的身份连接
     */
    private Boolean cleanSession;
    /**
     * 是否自动重连
     */
    private Boolean autoReconnect;
    /**
     * 消息质量
     * 0只会发送一次，不管成不成功
     * 1未成功会继续发送，直到成功，可能会收到多次
     * 2未成功会继续发送，但会保证只收到一次
     */
    private int qos;
    /**
     * 心跳间隔
     */
    private int keepAliveInterval;
    /**
     * 连接超时
     */
    private int connectionTimeout;
    /**
     * 断开连接超时时间
     */
    private long completionTimeout;
    /**
     * 连接服务器默认客户端ID
     */
    private String producerClientId;
    /**
     * 默认的推送主题，实际可在调用接口时指定
     */
    private String producerDefaultTopic;

    /**
     * MQTT 消费者连接服务器默认客户端ID
     */
    private String consumerClientId;
    /**
     * MQTT 消费者默认的推送主题，实际可在调用接口时指定
     */
    private String[] consumerDefaultTopic;

    /*
      遗嘱消息配置
      复现：使用MQTTX工具订阅遗嘱主题，然后在断网时关闭该SpringBoot应用，当关闭完成时，连接网络，查看MQTTX订阅的主题，会出现遗嘱消息
     */
    /**
     * 遗愿主题
     */
    private String willTopic;
    /**
     * 遗愿消息内容
     */
    private String willContent;

    private int willQos;

    private boolean willRetained;

}

