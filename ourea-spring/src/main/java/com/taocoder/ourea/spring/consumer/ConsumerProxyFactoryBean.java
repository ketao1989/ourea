/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea.spring.consumer;

import com.taocoder.ourea.core.common.Constants;
import com.taocoder.ourea.core.common.PropertiesUtils;
import com.taocoder.ourea.core.config.ThriftClientConfig;
import com.taocoder.ourea.core.config.ZkConfig;
import com.taocoder.ourea.core.consumer.ConsumerProxyFactory;
import com.taocoder.ourea.core.loadbalance.ILoadBalanceStrategy;
import com.taocoder.ourea.core.loadbalance.RoundRobinLoadBalanceStrategy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;

import java.util.Properties;

/**
 * @author tao.ke Date: 16/5/1 Time: 下午2:18
 */
public class ConsumerProxyFactoryBean
        implements FactoryBean<ConsumerProxyFactory>, InitializingBean, ApplicationListener<ApplicationEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerProxyFactoryBean.class);

    /**
     * zk配置
     */
    private ZkConfig zkConfig;

    /**
     * consumer配置
     */
    private ThriftClientConfig clientConfig;

    /**
     * 通过读取resource获取配置Properties 获取上述配置
     */
    private Resource configLocation;

    /**
     * 需要调用的接口类
     */
    private Class clazz;

    /**
     * 引用的接口在spring中的唯一id,在其他resource或者autowire中,最好注明是byname
     */
    private String refId;

    /**
     * target 对象
     */
    private ConsumerProxyFactory consumerProxyFactory;

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {

        if (applicationEvent instanceof ContextRefreshedEvent){
            Object ref = consumerProxyFactory.getProxyClient(clazz,clientConfig,zkConfig);
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) ((ContextRefreshedEvent) applicationEvent).getApplicationContext().getAutowireCapableBeanFactory();
            beanFactory.registerSingleton(refId,ref);
            LOGGER.info("register consumer client interface class into spring applicationContext.");
        }

    }

    @Override
    public ConsumerProxyFactory getObject() throws Exception {
        if (this.consumerProxyFactory == null) {
            afterPropertiesSet();
        }
        return this.consumerProxyFactory;
    }

    @Override
    public Class<?> getObjectType() {
        return this.consumerProxyFactory == null ? ConsumerProxyFactory.class : this.consumerProxyFactory.getClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if (clazz == null) {
            throw new IllegalArgumentException("build consumer bean fail. clazz is null.");
        }
        assertEmpty("refId", refId);

        if (configLocation != null && (zkConfig == null || clientConfig == null)) {
            Properties properties = PropertiesUtils.load(configLocation.getInputStream());
            if (zkConfig == null) {
                buildZkConfig(properties);
            }
            if (clientConfig == null) {
                buildClientConfig(properties);
            }
        }

        consumerProxyFactory = new ConsumerProxyFactory();

    }

    /**
     * 构建client config对象
     *
     * @param properties
     */
    private void buildClientConfig(Properties properties) {

        clientConfig = new ThriftClientConfig();

        if (StringUtils.isNoneEmpty(properties.getProperty("group"))){
            clientConfig.setGroup(properties.getProperty("group"));
        }
        if (StringUtils.isNoneEmpty(properties.getProperty("version"))){
            clientConfig.setVersion(properties.getProperty("version"));
        }
        if (StringUtils.isNoneEmpty(properties.getProperty("timeout"))){
            clientConfig.setTimeout(Integer.parseInt(properties.getProperty("timeout")));
        }
        if (StringUtils.isNoneEmpty(properties.getProperty("retryTimes"))){
            clientConfig.setRetryTimes(Integer.parseInt(properties.getProperty("retryTimes")));
        }
        if (StringUtils.isNoneEmpty(properties.getProperty("loadBalanceStrategy"))){

            try {
                Class<ILoadBalanceStrategy> strategyClass = (Class<ILoadBalanceStrategy>) Class.forName(properties.getProperty("loadBalanceStrategy"));
                clientConfig.setLoadBalanceStrategy(strategyClass.newInstance());
            }catch (Exception e){
                LOGGER.warn("config full loadBalanceStrategy class not find.use default RoundRobinLoadBalanceStrategy.");
            }
        }

    }

    /**
     * 服务组,不同组之间不能服务交互
     */
    private String group = Constants.DEFAULT_GROUP_NAME;

    /**
     * 服务版本,不同版本之间不能交互
     */
    private String version = Constants.DEFAULT_VERSION_VALUE;

    /**
     * 超时时间
     */
    private int timeout = Constants.DEFAULT_TIMEOUT_VALUE;

    /**
     * 服务重试次数,默认重试一次
     */
    private int retryTimes = 1;

    /**
     * 服务client的负载策略
     */
    private ILoadBalanceStrategy loadBalanceStrategy = new RoundRobinLoadBalanceStrategy();


    /**
     * 构建zkConfig对象
     *
     * @param properties
     */
    private void buildZkConfig(Properties properties) {

        assertEmpty("zkAddress", properties.getProperty("zkAddress"));
        zkConfig = new ZkConfig(properties.getProperty("zkAddress"));

        if (StringUtils.isNoneEmpty(properties.getProperty("baseSleepTimeMs"))){
            zkConfig.setBaseSleepTimeMs(Integer.parseInt(properties.getProperty("baseSleepTimeMs")));
        }

        if (StringUtils.isNoneEmpty(properties.getProperty("maxRetries"))){
            zkConfig.setMaxRetries(Integer.parseInt(properties.getProperty("maxRetries")));
        }

        if (StringUtils.isNoneEmpty(properties.getProperty("maxSleepTimeMs"))){
            zkConfig.setMaxSleepTimeMs(Integer.parseInt(properties.getProperty("maxSleepTimeMs")));
        }

        if (StringUtils.isNoneEmpty(properties.getProperty("zkTimeout"))){
            zkConfig.setZkTimeout(Integer.parseInt(properties.getProperty("zkTimeout")));
        }

    }

    public void setZkConfig(ZkConfig zkConfig) {
        this.zkConfig = zkConfig;
    }

    public void setClientConfig(ThriftClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public void setConfigLocation(Resource configLocation) {
        this.configLocation = configLocation;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    private void assertEmpty(String key, String val) {
        if (StringUtils.isBlank(val)) {
            throw new IllegalArgumentException(key + "value do not can be empty.");
        }
    }
}
