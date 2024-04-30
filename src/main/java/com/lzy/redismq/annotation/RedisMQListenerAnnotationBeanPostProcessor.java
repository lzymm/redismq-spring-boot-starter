package com.lzy.redismq.annotation;

import com.lzy.redismq.config.RedisMQConfigUtils;
import com.lzy.redismq.config.RedisMQListenerEndpoint;
import com.lzy.redismq.config.RedisMQListenerEndpointRegistry;
import com.lzy.redismq.config.RedisMQStreamHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.lzy.redismq.config.RedisMQListenerEndpoint.buildListerEndpoint;

/**
 * 一个用于解析注册被{@link RedisMQListener}标记的方法的 bean 后置处理器
 */
@Slf4j(topic = "redis-mq:RedisMQListenerAnnotationBeanPostProcessor")
public class RedisMQListenerAnnotationBeanPostProcessor<K, V> implements BeanPostProcessor, Ordered, BeanFactoryAware, SmartInitializingSingleton {
    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    private BeanFactory beanFactory;
    private RedisMQListenerEndpointRegistry endpointRegistry;
    private Set<RedisMQListenerEndpoint> endpoints = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    static {
        log.info("======= @RedisMQListener Processor start  =======");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    /**
     * 在这里进行注解读取解析
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!this.nonAnnotatedClasses.contains(bean.getClass())) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            //获取方法上的@RedisMQListener注解
            Map<Method, Set<RedisMQListener>> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                    (MethodIntrospector.MetadataLookup<Set<RedisMQListener>>) method -> {
                        Set<RedisMQListener> listenerMethods = findListenerAnnotations(method);
                        return (!listenerMethods.isEmpty() ? listenerMethods : null);
                    });

            if (annotatedMethods.isEmpty()) {
                this.nonAnnotatedClasses.add(bean.getClass());
                this.log.trace("No @RedisMQListener annotations found on bean type: " + bean.getClass());
            } else {
                // Non-empty set of methods
                for (Map.Entry<Method, Set<RedisMQListener>> entry : annotatedMethods.entrySet()) {
                    Method method = entry.getKey();
                    for (RedisMQListener listener : entry.getValue()) {
                        processRedisMQListener(listener, method, bean, beanName);
                    }
                }
                this.log.debug(annotatedMethods.size() + " @RedisMQListener methods processed on bean '" + beanName + "': " + annotatedMethods);
            }

        }
        return bean;
    }

    /**
     * 最低优先权
     */
    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (endpointRegistry != null && endpoints.size() >0) {
            endpoints.forEach(endpoint->{
                endpointRegistry.registryListener(endpoint);
                log.info("@RedisMQListener annotations found:bean={},method={}",endpoint.getBeanName(),endpoint.getMethod().getName());
            });
        }else {
            this.log.info("No @RedisMQListener annotations found on the project");
        }
    }

    /*
     * AnnotationUtils.getRepeatableAnnotations does not look at interfaces
     */
    private Set<RedisMQListener> findListenerAnnotations(Method method) {
        Set<RedisMQListener> listeners = new HashSet<>();
        RedisMQListener ann = AnnotatedElementUtils.findMergedAnnotation(method, RedisMQListener.class);
        if (ann != null) {
            listeners.add(ann);
        }
        return listeners;
    }

    protected void processRedisMQListener(RedisMQListener redisMQListener, Method method, Object bean, String beanName) {
        if (this.endpointRegistry == null) {
            this.endpointRegistry = this.beanFactory.getBean(RedisMQConfigUtils.REDIS_MQ_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME,
                    RedisMQListenerEndpointRegistry.class);
        }

        RedisMQStreamHelper redisMQStreamHelper = this.beanFactory.getBean(RedisMQConfigUtils.REDIS_MQ_STREAM_HELPER_BEAN_NAME,
                RedisMQStreamHelper.class);

        Method methodToUse = checkProxy(method, bean);
        RedisMQListenerEndpoint endpoint = buildListerEndpoint(redisMQStreamHelper, redisMQListener, methodToUse, bean, beanName);
        this.endpoints.add(endpoint);
       /**
        *  endpoints registry
        *  @see #afterSingletonsInstantiated
        */
    }



    /**
     * 校验当前bean的所代理的接口中是否包含当前方法
     */
    private Method checkProxy(Method methodArg, Object bean) {
        Method method = methodArg;
        if (AopUtils.isJdkDynamicProxy(bean)) {
            try {
                // Found a @RedisMQListener method on the target class for this JDK proxy ->
                // is it also present on the proxy itself?
                method = bean.getClass().getMethod(method.getName(), method.getParameterTypes());
                Class<?>[] proxiedInterfaces = ((Advised) bean).getProxiedInterfaces();
                for (Class<?> iface : proxiedInterfaces) {
                    try {
                        method = iface.getMethod(method.getName(), method.getParameterTypes());
                        break;
                    } catch (@SuppressWarnings("unused") NoSuchMethodException noMethod) {
                        // NOSONAR
                    }
                }
            } catch (SecurityException ex) {
                ReflectionUtils.handleReflectionException(ex);
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException(String.format(
                        "@RedisMQListener method '%s' found on bean target class '%s', " +
                                "but not found in any interface(s) for bean JDK proxy. Either " +
                                "pull the method up to an interface or switch to subclass (CGLIB) " +
                                "proxies by setting proxy-target-class/proxyTargetClass " +
                                "attribute to 'true'", method.getName(),
                        method.getDeclaringClass().getSimpleName()), ex);
            }
        }
        return method;
    }



}
