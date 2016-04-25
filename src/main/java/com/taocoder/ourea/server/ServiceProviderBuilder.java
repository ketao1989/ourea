/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package com.taocoder.ourea.server;

import com.facebook.fb303.FacebookService;
import com.taocoder.ourea.common.ClassUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.server.TServerEventHandler;

/**
 * @author tao.ke Date: 16/3/3 Time: 下午5:46
 */
public class ServiceProviderBuilder {

    private static final String DEFAULT_GROUP = "ourea";
    private static final int DEFAULT_MAX_THREADS = 64;
    private static final int DEFAULT_MIN_THREADS = 10;
    private static final int DEFAULT_ZK_TIMEOUT = 3000;

    private ServiceProvider serviceProvider;

    ServiceProviderBuilder(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public static ServiceProviderBuilder newBuilder(Class clazz, Object ref, int port,String zkAddr) {

        if (!FacebookService.Iface.class.isAssignableFrom(ClassUtils.getIface(clazz))) {
            throw new IllegalArgumentException("illegal interface not implement com.facebook.fb303.FacebookService");
        }

        ServiceProvider serviceProvider = new ServiceProvider();

        serviceProvider.setInterfaceClazz(clazz);
        serviceProvider.setRefImpl(ref);
        serviceProvider.setPort(port);
        serviceProvider.setZkAddress(zkAddr);

        return new ServiceProviderBuilder(serviceProvider);
    }

    public ServiceProviderBuilder setServiceId(String id) {
        serviceProvider.setId(id);
        return this;
    }

    public ServiceProviderBuilder setMaxThread(int maxThread) {
        serviceProvider.setMaxWorkerThreads(maxThread);
        return this;
    }

    public ServiceProviderBuilder setMinThread(int minThread) {
        serviceProvider.setMinWorkerThreads(minThread);
        return this;
    }

    public ServiceProviderBuilder setGroup(String group) {
        serviceProvider.setGroup(group);
        return this;
    }

    public ServiceProviderBuilder withServerHandler(TServerEventHandler handler){
        serviceProvider.setServerEventHandler(handler);
        return this;
    }

    public ServiceProvider build() {

        if (StringUtils.isBlank(serviceProvider.getId())) {
            serviceProvider.setId(serviceProvider.getInterfaceClazz().getCanonicalName());
        }

        if (StringUtils.isBlank(serviceProvider.getGroup())) {
            serviceProvider.setGroup(DEFAULT_GROUP);
        }

        if (serviceProvider.getMaxWorkerThreads() <= 0) {
            serviceProvider.setMaxWorkerThreads(DEFAULT_MAX_THREADS);
        }

        if (serviceProvider.getMinWorkerThreads() <= 0) {
            serviceProvider.setMinWorkerThreads(DEFAULT_MIN_THREADS);
        }

        if (serviceProvider.getZkTimeout() <= 0) {
            serviceProvider.setZkTimeout(DEFAULT_ZK_TIMEOUT);
        }

        return serviceProvider;
    }
}
