package com.da.app.config;

import com.da.app.po.MyMessage;
import com.da.app.service.ActiveMqService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author Da
 * @Description: 三十年生死两茫茫, 写程序, 到天亮。
 * 千行代码,Bug何处藏。
 * 纵使上线又怎样,朝令改,夕断肠。
 * 领导每天新想法,天天改,日日忙。
 * 相顾无言,惟有泪千行。
 * 每晚灯火阑珊处,夜难寐,又加班。
 * @Date 2022/7/13 上午 10:28
 */
@Slf4j
@Configuration
public class MqttConfig {

    @Value("${mqtt.urls}")
    private String urls;

    @Value("${mqtt.client.id}")
    private String clientId;

    @Value("${mqtt.topic}")
    private String topic;
    //    保存动态的主题列表
    private final List<String> dynamicTopic = new ArrayList<>();

    @Autowired
    private ActiveMqService activeMqService;

    //    用来解析json为实体类
//    @Autowired
//    private ObjectMapper objectMapper;

    //    这里保存着动态订阅的主题
    @Bean(value = "dynamicTopic")
    public List<String> getDynamicTopic() {
        return dynamicTopic;
    }

    /**
     * 先创建连接
     * 创建MqttPahoClientFactory,设置MQTT Broker连接属性,如果使用SSL验证,也在这里设置。
     *
     * @return factory
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        // 设置代理端的URL地址,可以是多个,如果配置中用,分开说明是多个地址
        if (urls.contains(",")) {
            options.setServerURIs(urls.split(","));
        } else {
            options.setServerURIs(new String[]{urls});
        }
        options.setUserName("springboot");
        factory.setConnectionOptions(options);
        return factory;
    }

    /**
     * 入站通道
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    /**
     * 入站
     */
    @Bean
    public MessageProducer inbound() {
//        Paho客户端消息驱动通道适配器,主要用来订阅主题
        final MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(clientId, mqttClientFactory());
//        如果配置中用,分开说明是多个主题
        if (topic.contains(",")) {
            adapter.addTopic(topic.split(","));
        } else {
            adapter.addTopic(topic);
        }
        adapter.setCompletionTimeout(5000);
        // Paho消息转换器
        DefaultPahoMessageConverter defaultPahoMessageConverter = new DefaultPahoMessageConverter();
        // 按字节接收消息
//        defaultPahoMessageConverter.setPayloadAsBytes(true);
        adapter.setConverter(defaultPahoMessageConverter);
        adapter.setQos(1); // 设置QoS
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    /**
     * 消息转化,中间站,在这里处理订阅频道的消息
     */

    @Bean
    // ServiceActivator注解表明：当前方法用于处理MQTT消息,inputChannel参数指定了用于消费消息的channel。
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return message ->
        {
//            发来的消息
            String payload = message.getPayload().toString();
//            try {
//                如果是MyMessage格式的可以转成实体类
//                final MyMessage value = objectMapper.readValue(payload, MyMessage.class);
//                log.info("msg => {}", value);
//            } catch (JsonProcessingException e) {
//                e.printStackTrace();
//            }
            // byte[] bytes = (byte[]) message.getPayload(); // 收到的消息是字节格式
            String topic = Objects.requireNonNull(message.getHeaders().get("mqtt_receivedTopic")).toString();
//            交给activeMq处理
            activeMqService.send(topic, payload);
            log.info("收到消息来自主题:{} 负载: {}", topic, payload);
//            根据主题分别进行消息处理。
//            if (topic.equals("one")) {
//                log.info("这里只处理来自one主题的消息 => {}", payload);
//            }
            if (dynamicTopic.size() > 0) {
                log.info("这里处理动态订阅的主题 => {}", dynamicTopic);
                log.info("收到消息来自主题:{} 负载: {}", topic, payload);
            }
        };
    }

    /**
     * 消息出去
     * 出站通道
     */
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    /**
     * 出站
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler outbound() {
        // 发送消息和消费消息Channel可以使用相同MqttPahoClientFactory
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler("publishClient", mqttClientFactory());
        messageHandler.setAsync(true); // 如果设置成true,即异步,发送消息时将不会阻塞。
        messageHandler.setDefaultTopic("command");
        messageHandler.setDefaultQos(1); // 设置默认QoS
        // Paho消息转换器
        DefaultPahoMessageConverter defaultPahoMessageConverter = new DefaultPahoMessageConverter();
        // defaultPahoMessageConverter.setPayloadAsBytes(true); // 发送默认按字节类型发送消息
        messageHandler.setConverter(defaultPahoMessageConverter);
        return messageHandler;
    }

}
