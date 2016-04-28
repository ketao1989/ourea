/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea.client;

import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.taocoder.ourea.config.ThriftClientConfig;
import com.taocoder.ourea.config.ZkConfig;
import com.taocoder.ourea.model.ServiceInfo;

/**
 * @author tao.ke Date: 16/4/27 Time: 上午11:41
 */
public class ConsumerProxyFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerProxyFactory.class);

    private static final ConcurrentHashMap<String, Object> SERVICE_CLIENT_CONCURRENT_HASH_MAP = new ConcurrentHashMap<String, Object>();

    private static final Joiner CLIENT_KEY_JOINER = Joiner.on("_");

    private static final Object SERVICE_CLIENT_LOCK = new Object();

    public static <T> T getProxyClient(Class<T> clientClazz, ZkConfig zkConfig) {
        return getProxyClient(clientClazz, new ThriftClientConfig(), zkConfig);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getProxyClient(Class<T> clientClazz, ThriftClientConfig config, ZkConfig zkConfig) {

        String clientKey = CLIENT_KEY_JOINER.join(clientClazz.getCanonicalName(), config.getGroup(),
                config.getVersion());
        T client = (T) SERVICE_CLIENT_CONCURRENT_HASH_MAP.get(clientKey);

        if (client == null) {
            synchronized (SERVICE_CLIENT_LOCK) {
                client = (T) SERVICE_CLIENT_CONCURRENT_HASH_MAP.get(clientKey);
                if (client == null) {

                    final ServiceInfo serviceInfo = new ServiceInfo(clientClazz, config.getVersion(),
                            config.getGroup());

                    client = (T) Proxy.newProxyInstance(ConsumerProxyFactory.class.getClassLoader(),
                            new Class[] { clientClazz }, new ConsumerProxy(serviceInfo, zkConfig, config));
                    SERVICE_CLIENT_CONCURRENT_HASH_MAP.putIfAbsent(clientKey, client);
                }
            }
        }
        return client;
    }

}
