package com.lzy.redismq.annotation;

import com.lzy.redismq.config.RedisMQConfigUtils;
import com.lzy.redismq.config.RedisMQHelper;
import com.lzy.redismq.consumer.AsyncStreamConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.*;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * listener 端点
 */
@Slf4j(topic = "redis-mq:RedisMQListenerEndpointRegistry")
public class RedisMQListenerEndpointRegistry implements DisposableBean, SmartLifecycle, ApplicationContextAware,
        ApplicationListener<ContextRefreshedEvent> {
    private final Map<String, StreamMessageListenerContainer> listenerContainers = new ConcurrentHashMap<>();
    private ConfigurableApplicationContext applicationContext;
    private boolean contextRefreshed = true;
    private volatile boolean running = true;

    /**
     * Return the managed {@link StreamMessageListenerContainer} instance(s).
     *
     * @return the managed {@link StreamMessageListenerContainer} instance(s).
     */
    public Collection<StreamMessageListenerContainer> getListenerContainers() {
        return Collections.unmodifiableCollection(this.listenerContainers.values());
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            this.applicationContext = (ConfigurableApplicationContext) applicationContext;
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().equals(this.applicationContext)) {
            this.contextRefreshed = true;
        }
    }

    @Override
    public void start() {
        for (StreamMessageListenerContainer listenerContainer : getListenerContainers()) {
            startIfNecessary(listenerContainer);
        }
        this.running = true;
    }

    @Override
    public void stop() {
        this.running = false;
        for (StreamMessageListenerContainer listenerContainer : getListenerContainers()) {
            listenerContainer.stop();
        }
    }

    @Override
    public void destroy() throws Exception {
        for (StreamMessageListenerContainer listenerContainer : getListenerContainers()) {
            if (listenerContainer instanceof DisposableBean) {
                try {
                    ((DisposableBean) listenerContainer).destroy();
                } catch (Exception ex) {
                    this.log.warn("Failed to destroy message listener container", ex);
                }
            }
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    /**
     * Start the specified {@link StreamMessageListenerContainer} if it should be started
     * on startup.
     *
     * @param listenerContainer the listener container to start.
     * @see StreamMessageListenerContainer#isAutoStartup()
     */
    private void startIfNecessary(StreamMessageListenerContainer listenerContainer) {
        if (this.contextRefreshed || listenerContainer.isAutoStartup()) {
            listenerContainer.start();
        }
    }

    private StreamMessageListenerContainer createListenerContainer(RedisMQListenerEndpoint endpoint) {
        RedisMQContainerFactory redisMQContainerFactory = this.applicationContext.getBean(RedisMQConfigUtils.REDIS_MQ_CONTAINER_FACTORY_BEAN_NAME, RedisMQContainerFactory.class);
        return redisMQContainerFactory.createContainer(endpoint);
    }

    public void registryListener(RedisMQListenerEndpoint endpoint) {
        String id = endpoint.getId();
        Assert.hasText(id, "Endpoint id must not be empty");
        synchronized (this.listenerContainers) {
            Assert.state(!this.listenerContainers.containsKey(id),
                    "Another endpoint is already registered with id '" + id + "'");

            if (StringUtils.hasText(endpoint.getGroup()) && this.applicationContext != null) {

                StreamMessageListenerContainer container = createListenerContainer(endpoint);
                listenerContainers.put(endpoint.getId(), container);

                List<StreamMessageListenerContainer> containerGroup;
                if (this.applicationContext.containsBean(endpoint.getGroup())) {
                    containerGroup = this.applicationContext.getBean(endpoint.getGroup(), List.class);
                } else {
                    containerGroup = new ArrayList<>();
                    this.applicationContext.getBeanFactory().registerSingleton(endpoint.getGroup(), containerGroup);
                }
                containerGroup.add(container);

                //初始化stream & group
                initStreamAndGroups(endpoint);

                //注册consumer
                for (String name : endpoint.getConsumers()) {
                    Consumer consumer = Consumer.from(endpoint.getGroup(), name);
                    StreamMessageListenerContainer.ConsumerStreamReadRequest<String> streamReadRequest = StreamMessageListenerContainer.StreamReadRequest
                            .builder(StreamOffset.create(endpoint.getStream(), ReadOffset.lastConsumed()))
                            .consumer(consumer)
                            .autoAcknowledge(endpoint.isAutoAck())
                            // 如果消费者发生了异常，判断是否取消消费者消费
                            .errorHandler(endpoint.getErrorHandler())
                            .cancelOnError(throwable -> true)
                            .build();
                    container.register(streamReadRequest, new AsyncStreamConsumer(endpoint, consumer));
                }
                //启动container
                startIfNecessary(container);
            }
        }
    }

    private void initStreamAndGroups(RedisMQListenerEndpoint endpoint) {
        synchronized (EnableRedisMQListener.class) {
            String streamKey = endpoint.getStream();
            String group = endpoint.getGroup();
            RedisMQHelper redisMQHelper = endpoint.getRedisMQHelper();
            if (!redisMQHelper.hasStream(streamKey)) {
                redisMQHelper.createStream(streamKey);
            }
            if (!redisMQHelper.hasGroup(streamKey, group)) {
                redisMQHelper.createGroup(streamKey, group);
            }
        }
    }
}
