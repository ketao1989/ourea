/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package com.taocoder.ourea.registry;

import com.taocoder.ourea.model.ProviderInfo;
import com.taocoder.ourea.model.ServiceInfo;

/**
 * @author tao.ke Date: 16/3/3 Time: 下午11:37
 */
public interface IRegistry {

    /**
     * 注册，比如：提供者地址，消费者地址，路由规则，覆盖规则，等数据。
     *
     */
    void register(ServiceInfo serviceInfo, ProviderInfo providerInfo, String role);

    /**
     * 注销服务,也就是将服务的provider从父path中移除
     * 
     * @param serviceInfo
     * @param providerInfo
     * @param role
     */
    void unregister(ServiceInfo serviceInfo, ProviderInfo providerInfo, String role);

    /**
     * consumer订阅对应的zk路径下provider,每次都会触发callback操作回调consumer pool
     * 
     * @param serviceInfo
     * @return
     */
    void subscribe(ServiceInfo serviceInfo, INotifyListener listener);

}
