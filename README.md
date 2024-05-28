# redismq
基于redis-stream的消息队列封装

## 使用
### 引入坐标
```java
    <dependency>
        <groupId>com.lzy</groupId>
        <artifactId>redismq-spring-boot-starter</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
```
配置(消费者端可以不做配置)
```java
spring:
  redis:
    stream:
      enable: true #启用
      def-max-len: 10
      streams:
        - name: stream1
          max-len: 2
        - name: stream2
          max-len: 4

```

### 发送消息
```java
@Service
public class MessageService {
    @Autowired
    private RedisMQProducer producer;
    public void send(Map<String, Object> msg) {
        producer.sendMessage("streamKey", msg);
    }
}
```



### 消费消息 
#### 1.启动类或任意spring组件类添加注解 @EnableRedisMQListener
#### 2.任意Spring Component 中添加带有注解 @RedisMQListener 的方法（@Component、@Service、@Controller、@@Configuration）标记的class中
```java

@Service
@EnableRedisMQListener
public class ConsumerService {
    //自动ack（默认）
    @RedisMQListener(stream = Constants.STREAM_KEY_001, group = "group001", names = {"consumer001", "consumer002"})
    public void receiveMessageAutoAck(MapRecord<String, String, Object> msg, Consumer consumer) {
        System.out.println("====================自动ack=============================\r\n"
                + MessageFormat.format("Thread:{0}|msgId:{1}|stream:{2}|consumer:{3}|content:{4}"
                , Thread.currentThread().getName()
                , msg.getId()
                , msg.getStream()
                , consumer
                , msg.getValue()
        ));
    }

    @RedisMQListener(stream = Constants.STREAM_KEY_001, group = "group002", names = {"consumer001"}, autoAck = false)
    public void receiveMessage(MapRecord<String, String, Object> msg, Consumer consumer, StreamAcknowledge acknowledge) {
        System.out.println("====================手动ack=============================\r\n"
                + MessageFormat.format("Thread:{0}|msgId:{1}|stream:{2}|consumer:{3}|content:{4}"
                , Thread.currentThread().getName()
                , msg.getId()
                , msg.getStream()
                , consumer
                , msg.getValue()

        ));
        acknowledge.ack(msg);
    }
}
   
```



