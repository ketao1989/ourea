/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea.core.consumer;

import com.google.common.base.Joiner;
import com.taocoder.ourea.core.config.ThriftClientConfig;
import com.taocoder.ourea.core.config.ZkConfig;
import com.taocoder.ourea.core.model.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tao.ke Date: 16/4/27 Time: 上午11:41
 */
public class ConsumerProxyFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerProxyFactory.class);

    /**
     * 正常的业务需求,是保证全局的thrift client 单例的,因为构造client成本是很重的. 这里的,key为class_group_version
     */
    private static final ConcurrentHashMap<String, Object> SERVICE_CLIENT_CONCURRENT_HASH_MAP = new ConcurrentHashMap<String, Object>();

    private static final Joiner CLIENT_KEY_JOINER = Joiner.on("_");

    private static final Object SERVICE_CLIENT_LOCK = new Object();

    public <T> T getProxyClient(Class<T> clientClazz, ZkConfig zkConfig) {
        return getProxyClient(clientClazz, new ThriftClientConfig(), zkConfig);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxyClient(Class<T> clientClazz, ThriftClientConfig config, ZkConfig zkConfig) {

        String clientKey = CLIENT_KEY_JOINER.join(clientClazz.getCanonicalName(), config.getGroup(),
                config.getVersion());
        T client = (T) SERVICE_CLIENT_CONCURRENT_HASH_MAP.get(clientKey);

        if (client == null) {
            synchronized (SERVICE_CLIENT_LOCK) {
                client = (T) SERVICE_CLIENT_CONCURRENT_HASH_MAP.get(clientKey);
                if (client == null) {

                    long start = System.currentTimeMillis();
                    final ServiceInfo serviceInfo = new ServiceInfo(clientClazz, config.getVersion(),
                            config.getGroup());

                    client = (T) Proxy.newProxyInstance(ConsumerProxyFactory.class.getClassLoader(),
                            new Class[] { clientClazz }, new ConsumerProxy(serviceInfo, zkConfig, config));
                    SERVICE_CLIENT_CONCURRENT_HASH_MAP.putIfAbsent(clientKey, client);
                    LOGGER.info("build thrift client succ.cost:{},clientConfig:{},zkConfig:{}",
                            System.currentTimeMillis() - start, config, zkConfig);
                }
            }
        }
        return client;
    }

}
