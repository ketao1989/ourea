/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea.provider;

import com.taocoder.ourea.config.ThriftServerConfig;
import com.taocoder.ourea.config.ZkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tao.ke Date: 16/4/28 Time: 下午1:06
 */
public class ServiceProviderFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceProviderFactory.class);

    /**
     * 根据实例对象的类名来缓存
     */
    private static final ConcurrentHashMap<Class, ServiceProvider> SERVICE_PROVIDER_CONCURRENT_MAP = new ConcurrentHashMap<Class, ServiceProvider>();

    /**
     * 创建SERVICE_PROVIDER_CONCURRENT_MAP时锁
     */
    private static final Object SERVICE_LOCK = new Object();

    public static void exposeService(Object ref, ZkConfig zkConfig, ThriftServerConfig serverConfig) {

        Class clazz = ref.getClass();

        ServiceProvider provider = SERVICE_PROVIDER_CONCURRENT_MAP.get(clazz);

        if (provider == null) {
            synchronized (SERVICE_LOCK) {
                provider = SERVICE_PROVIDER_CONCURRENT_MAP.get(clazz);
                if (provider == null) {

                    provider = new ServiceProvider(ref, zkConfig, serverConfig);
                    provider.start();
                    SERVICE_PROVIDER_CONCURRENT_MAP.putIfAbsent(clazz, provider);
                }
            }
        }
    }

}
