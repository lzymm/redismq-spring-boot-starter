// package com.lzy.redismq.config;
//
// import com.lzy.redismq.consumer.AsyncStreamConsumer;
// import com.lzy.redismq.error.DefaultErrorHandler;
// import com.lzy.redismq.properties.RedisStreamGroup;
// import com.lzy.redismq.properties.RedisStreamProperties;
// import com.lzy.redismq.properties.RedisStreamQueue;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.boot.context.properties.EnableConfigurationProperties;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.core.task.AsyncTaskExecutor;
// import org.springframework.data.redis.connection.RedisConnectionFactory;
// import org.springframework.data.redis.connection.stream.Consumer;
// import org.springframework.data.redis.connection.stream.MapRecord;
// import org.springframework.data.redis.connection.stream.ReadOffset;
// import org.springframework.data.redis.connection.stream.StreamOffset;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.data.redis.serializer.RedisSerializer;
// import org.springframework.data.redis.stream.StreamMessageListenerContainer;
// import org.springframework.data.redis.stream.Subscription;
//
// import java.time.Duration;
// import java.util.List;
// import java.util.stream.Collectors;
// import java.util.stream.Stream;
//
// /**
//  *
//  */
// @SuppressWarnings("all")
// @Slf4j
// @Configuration
// @ConditionalOnClass(RedisTemplate.class)
// @EnableConfigurationProperties(RedisStreamProperties.class)
// @ConditionalOnProperty(name = "spring.redis.stream.enable", havingValue = "true", matchIfMissing = false)
// public class RedisStreamListenerConfig {
//
//     @Autowired
//     private RedisStreamProperties redisStreamProperties;
//
//     @Autowired
//     private RedisConnectionFactory redisConnectionFactory;
//
//     @Autowired
//     private AsyncTaskExecutor taskExecutor;
//
//
//     //单容器
//     // @DependsOn(" StreamListener")
//     // @Bean(initMethod = "start", destroyMethod = "stop")
//     // public StreamMessageListenerContainer streamMessageListenerContainer(){
//     //     //1.创建container options
//     //     //2.创建container request
//     //     //3.创建container
//     //     //4.注册consumer listener
//     //     StreamMessageListenerContainer.StreamMessageListenerContainerOptions
//     //
//     // }
//
//     /**
//      * 可以同时支持 独立消费 和 消费者组 消费
//      * <p>
//      * 可以支持动态的 增加和删除 消费者
//      * <p>
//      * <p>
//      * 支持消费者发生异常后，还可以继续消费，消费者不会被剔除，通过StreamReadRequest的cancelOnError来实现
//      * </p>
//      * 消费组需要预先创建出来
//      *
//      * @return StreamMessageListenerContainer
//      */
//     @Bean
//     public List<Subscription> intiSubscription() {
//         List<RedisStreamQueue> queues = redisStreamProperties.getQueues();
//         queues.forEach(redisStreamQueue ->   {
//             //stream key
//             String streamKey = redisStreamQueue.getName();
//             // stream groups
//             List<RedisStreamGroup> groups = redisStreamQueue.getGroups();
//
//             for (RedisStreamGroup redisStreamGroup : groups) {
//                 String groupName = redisStreamGroup.getName();
//                 Integer pollTimeoutSeconds = redisStreamGroup.getPollTimeoutSeconds();
//                 Integer perPollSize = redisStreamGroup.getPerPollSize();
//                 Class<?> messageTargetClass = redisStreamGroup.getMessageTargetClass();
//                 Boolean autoAck = redisStreamGroup.getAutoAck();
//                 String[] consumers = redisStreamGroup.getConsumers();
//                 //0.初始化redis中的stream数据结构和消费者组
//                 // initStream(streamKey, groupName);
//                 //1.创建消息监听器
//                 // 初始化options
//                 // StreamMessageListenerContainer<String, ? extends ObjectRecord<String, ?>> listenerContainer = StreamMessageListenerContainer.create(redisConnectionFactory, StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
//                 //         // 一次最多获取多少条消息
//                 //         .batchSize(perPollSize)
//                 //         // Stream 中没有消息时，阻塞多长时间，需要比 `spring.redis.timeout` 的时间小
//                 //         .pollTimeout(Duration.ofSeconds(pollTimeoutSeconds))
//                 //         // 运行 Stream 的 poll task
//                 //         .executor(taskExecutor)
//                 //         // 可以理解为 Stream Key 的序列化方式
//                 //         .keySerializer(RedisSerializer.string())
//                 //         // 可以理解为 Stream 后方的字段的 key 的序列化方式
//                 //         .hashKeySerializer(RedisSerializer.string())
//                 //         // 可以理解为 Stream 后方的字段的 value 的序列化方式
//                 //         .hashValueSerializer(RedisSerializer.string())
//                 //
//                 //         // ObjectRecord 时，将 对象的 filed 和 value 转换成一个 Map 比如：将Book对象转换成map
//                 //         .objectMapper(new ObjectHashMapper())
//                 //         // 将发送到Stream中的Record转换成ObjectRecord，转换成具体的类型是这个地方指定的类型
//                 //         .targetType(messageTargetClass)
//                 //         // 获取消息的过程或获取到消息给具体的消息者处理的过程中，发生了异常的处理
//                 //         .errorHandler(new DefaultErrorHandler())
//                 //         .build());
//                 // 如果需要对某个消费者进行个性化配置在调用register方法的时候传递`StreamReadRequest`对象
//
//
//                 StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, Object>> options = StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
//                         // 一次最多获取多少条消息
//                         .batchSize(perPollSize)
//                         // Stream 中没有消息时，阻塞多长时间，需要比 `spring.redis.timeout` 的时间小
//                         .pollTimeout(Duration.ofSeconds(pollTimeoutSeconds))
//                         // 运行 Stream 的 poll task
//                         .executor(taskExecutor)
//                         // 可以理解为 Stream Key 的序列化方式
//                         .keySerializer(RedisSerializer.string())
//                         // 可以理解为 Stream 后方的字段的 key 的序列化方式
//                         .hashKeySerializer(RedisSerializer.string())
//                         .build();
//                 StreamMessageListenerContainer<String, MapRecord<String, String, Object>> listenerContainer = StreamMessageListenerContainer.create(redisConnectionFactory, options);
//
//                 Stream.of(consumers).map(consumerName -> {
//                     Consumer consumer = Consumer.from(groupName, consumerName);
//                     StreamMessageListenerContainer.ConsumerStreamReadRequest<String> streamReadRequest = StreamMessageListenerContainer.StreamReadRequest
//                             .builder(StreamOffset.create(streamKey, ReadOffset.lastConsumed()))
//                             .consumer(consumer)
//                             .autoAcknowledge(autoAck)
//                             .errorHandler(new DefaultErrorHandler())
//                             // 如果消费者发生了异常，判断是否取消消费者消费
//                             .cancelOnError(throwable -> false).build();
//                     Subscription subscription = listenerContainer.register(streamReadRequest, new AsyncStreamConsumer(consumer));
//                     return subscription;
//                 }).collect(Collectors.toList());
//             }
//
//
//         });
//
//
//         // //
//         //
//         // // 独立消费
//         // // String streamKey = Constants.STREAM_KEY_001;
//         // // streamMessageListenerContainer.receive(StreamOffset.fromStart(streamKey),
//         // //         new AsyncConsumeStreamListener("独立消费", Consumer.from(null,"独立消费1")));
//         //
//         // // 消费组A,不自动ack
//         // // 从消费组中没有分配给消费者的消息开始消费
//         // // Consumer consumerA = Consumer.from("group-a", "consumer-a");
//         // // streamMessageListenerContainer.receive(consumerA,
//         // //         StreamOffset.create(streamKey, ReadOffset.lastConsumed()), new AsyncConsumeStreamListener("消费组A", consumerA));
//         //
//         // // group-c中的消费者consumer-c，在消费发生异常时，还可以继续消费
//         // Consumer consumerC = Consumer.from("group-a", "consumer-c");
//         // queues.forEach(stream -> {
//         //     String streamName = stream.getName();
//         //     List<RedisStreamConsumer> consumers = stream.getConsumers();
//         //     // 如果需要对某个消费者进行个性化配置在调用register方法的时候传递`StreamReadRequest`对象
//         //     StreamMessageListenerContainer.ConsumerStreamReadRequest<String> streamReadRequest = StreamMessageListenerContainer.StreamReadRequest.builder(StreamOffset.create(streamName, ReadOffset.lastConsumed())).consumer(consumerC).autoAcknowledge(true).errorHandler(new DefaultErrorHandler())
//         //             // 如果消费者发生了异常，判断是否取消消费者消费
//         //             .cancelOnError(throwable -> false).build();
//         //     streamMessageListenerContainer.register(streamReadRequest, new AsyncConsumeStreamListener("消费组A", consumerC));
//         //
//         // });
//         //
//         //
//         // // // 从消费组中没有分配给消费者的消息开始消费
//         // Consumer consumerA_B = Consumer.from("group-a", "consumer-b");
//         // streamMessageListenerContainer.receive(consumerA_B, StreamOffset.create("streamKey", ReadOffset.lastConsumed()), new AsyncConsumeStreamListener("消费组A", consumerA_B));
//         //
//         // // // 消费组B,自动ack
//         // // Consumer consumerB_A = Consumer.from("group-b", "consumer-a");
//         // // streamMessageListenerContainer.receiveAutoAck(consumerB_A,
//         // //         StreamOffset.create(streamKey, ReadOffset.lastConsumed()), new AsyncConsumeStreamListener("消费组B", consumerB_A));
//         //
//         //
//         // return streamMessageListenerContainer;
//         return null;
//     }
//
//     // private void initStream(String key, String group) {
//     //     boolean hasKey = RedisStreamHelper.hasKey(key);
//     //     if (!hasKey) {
//     //         Map<String, Object> map = new HashMap<>(1);
//     //         map.put("field", "value");
//     //         //创建主题
//     //         String result = RedisStreamHelper.sendMap(key, map);
//     //         //创建消费组
//     //         RedisStreamHelper.createGroup(key, group);
//     //         //将初始化的值删除掉
//     //         RedisStreamHelper.del(key, result);
//     //         log.info("stream:{}-group:{} initialize success", key, group);
//     //     }
//     // }
//
//
// }
