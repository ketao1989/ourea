/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package com.taocoder.ourea.model;

import java.io.Serializable;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.thrift.transport.TTransport;

import com.taocoder.ourea.client.ConsumerPoolFactory;
import com.taocoder.ourea.config.OureaObjectPoolConfig;

/**
 * 对于一个provider server的连接池对象.这个对象很重,所以需要缓存之后在使用
 * 
 * @author tao.ke Date: 16/3/3 Time: 下午2:35
 */
public class InvokeConn implements Serializable {

    private static final long serialVersionUID = -805739143582019252L;

    /**
     * 服务端的信息ip+port
     */
    private ProviderInfo providerInfo;

    /**
     * 和server端的连接池
     */
    private ObjectPool<TTransport> connPool;

    /**
     * 连接池相关配置
     */
    private OureaObjectPoolConfig poolConfig;

    public InvokeConn(ProviderInfo providerInfo, int timeout) {
        this(providerInfo, timeout, new OureaObjectPoolConfig());
    }

    public InvokeConn(ProviderInfo providerInfo, int timeout, OureaObjectPoolConfig poolConfig) {
        this.providerInfo = providerInfo;
        this.poolConfig = poolConfig;
        this.connPool = new GenericObjectPool<TTransport>(new ConsumerPoolFactory(providerInfo, timeout), poolConfig);
    }

    public ProviderInfo getProviderInfo() {
        return providerInfo;
    }

    public void setProviderInfo(ProviderInfo providerInfo) {
        this.providerInfo = providerInfo;
    }

    public ObjectPool<TTransport> getConnPool() {
        return connPool;
    }

    public GenericObjectPoolConfig getPoolConfig() {
        return poolConfig;
    }

    public void setPoolConfig(OureaObjectPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }
}
