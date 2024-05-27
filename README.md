# redismq
基于redis-stream的消息队列封装

## 使用
### 1.引入坐标
```java
    <dependency>
        <groupId>com.lzy</groupId>
        <artifactId>redismq-spring-boot-starter</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
```
配置
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

### 2. 启动类添加注解 @EnableRedisMQ
### 3. 任意Spring Component 中添加带有注解 @RedisMQListener 的方法
```java
    // （@Component、@Service、@Controller、@@Configuration）标记的class中

    //自动ack（默认）
    @RedisMQListener(stream = Constants.STREAM_KEY_001, group = "group001", names = {"consumer001", "consumer002"})
    public void onMessage(MapRecord<String, String, Object> msg, Consumer consumer) {
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
    public void onMessage(MapRecord<String, String, Object> msg, Consumer consumer, StreamAcknowledge acknowledge) {
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
```



