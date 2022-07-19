package com.da.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * @Author Da
 * @Description: <br/>
 * 三十年生死两茫茫，写程序，到天亮。
 * 千行代码，Bug何处藏。
 * 纵使上线又怎样，朝令改，夕断肠。
 * 领导每天新想法，天天改，日日忙。
 * 相顾无言，惟有泪千行。
 * 每晚灯火阑珊处，夜难寐，又加班。
 * @Date: 2022-07-19
 * @Time: 13:58
 */
@Slf4j
@Service
public class ActiveMqService {

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    /**
     * 监听one主题的消息
     *
     * @param msg 接收到的消息
     */
    @JmsListener(destination = "#{'${mqtt.topic}'.split(',')[0]}", containerFactory = "jmsListenerContainerTopic")
    public void oneMsg(String msg) {
        log.info("这里处理来自one主题的消息 => {}", msg);
    }

    /**
     * 监听two主题的消息
     *
     * @param msg 接收到的消息
     */
    @JmsListener(destination = "#{'${mqtt.topic}'.split(',')[1]}", containerFactory = "jmsListenerContainerTopic")
    public void twoMsg(String msg) {
        log.info("这里处理来自two主题的消息 => {}", msg);
    }

    /**
     * 发送消息
     *
     * @param destination 消息的destination
     * @param msg         消息内容
     */
    public void send(String destination, String msg) {
        jmsMessagingTemplate.convertAndSend(destination, msg);
    }

}
