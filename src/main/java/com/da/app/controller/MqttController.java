package com.da.app.controller;

import com.da.app.po.MyMessage;
import com.da.app.service.MqttGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author Da
 * @Description: 三十年生死两茫茫，写程序，到天亮。
 * 千行代码，Bug何处藏。
 * 纵使上线又怎样，朝令改，夕断肠。
 * 领导每天新想法，天天改，日日忙。
 * 相顾无言，惟有泪千行。
 * 每晚灯火阑珊处，夜难寐，又加班。
 * @Date 2022/7/13 上午 10:50
 */
@RestController
@Slf4j
public class MqttController {
    @Resource
    private MqttGateway mqttGateway;

    @Autowired
    private MessageProducer producer;

    @Autowired
    private List<String> dynamicTopic;


    @PostMapping("/send")
    public String send(@RequestBody MyMessage myMessage) {
        // 发送消息到指定主题
        mqttGateway.sendToMqtt(myMessage.getTopic(), 1, myMessage.getContent());
        return "send topic: " + myMessage.getTopic() + ", message : " + myMessage.getContent();
    }

    @GetMapping("/add/{topic}")
    public String add(@PathVariable String topic) {
//        动态添加topic
        ((MqttPahoMessageDrivenChannelAdapter) producer).addTopic(topic);
//        最好是把主题消息动态的存到数据库中
        dynamicTopic.add(topic);
        log.info("订阅了" + topic + "主题");
        return topic;
    }
}
