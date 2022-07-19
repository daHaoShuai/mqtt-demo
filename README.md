# springboot使用mqtt的和ActiveMQ一个小例子

> 安装mqtt服务器(我用的是docker安装的)

```shell
docker pull registry.cn-hangzhou.aliyuncs.com/synbop/emqttd:2.3.6
```

> 运行镜像
>
> --name 名字
>
> -p 18083 服务器启动端口
>
> -p 1882 TCP端口
>
> -p 8083 WS端口
>
> -p 8084 WSS端口
>
> -p 8883 SSL端口
>
> -d 指定容器

```shell
docker run --name emq -p 18083:18083 -p 1883:1883 -p 8084:8084 -p 8883:8883 -p 8083:8083 -d registry.cn-hangzhou.aliyuncs.com/synbop/emqttd:2.3.6
```

> 在浏览器输入机器IP:18083 就可以进入emqtt页面,初始的账户 admin, 密码 public
>
> 这里可以下载MQTTX客户端工具测试服务器 https://mqttx.app/zh

> 安装ActiveMQ服务器(我用的是docker安装的)

```shell
 # 查询镜像
  docker search activemq
 # 取start最多的镜像
  docker pull docker.io/webcenter/activemq
 # 启动镜像容器
  docker run -d --name activemq -p 61616:61616 -p 8161:8161 --restart=always docker.io/webcenter/activemq:latest
```

> 访问web页面 http://宿主机IP:8161 #默认账号:admin 默认密码:admin

> 需要的mqtt和activeMQ依赖

```xml

<dependencies>
    <!--        mqtt依赖包-->
    <dependency>
        <groupId>org.eclipse.paho</groupId>
        <artifactId>org.eclipse.paho.client.mqttv3</artifactId>
        <version>1.2.5</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.integration</groupId>
        <artifactId>spring-integration-mqtt</artifactId>
    </dependency>
    <!--         ActiveMQ依赖包 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-activemq</artifactId>
    </dependency>
</dependencies>
```

> 测试接口

向one主题发生消息

```shell
curl -H "Content-Type: application/json" -X POST -d '{"topic": "one","content":"{\"msg\":\"hello\"}" }' "http://127.0.0.1:8080/send"
```
