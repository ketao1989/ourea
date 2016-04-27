/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package com.taocoder.ourea.model;

import com.taocoder.ourea.client.ConsumerPoolFactory;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.thrift.transport.TTransport;

import java.io.Serializable;

/**
 * 对于一个provider server的连接池对象.这个对象很重,所以需要缓存之后在使用
 * 
 * @author tao.ke Date: 16/3/3 Time: 下午2:35
 */
public class InvokeConn implements Serializable {

    private static final long serialVersionUID = -805739143582019252L;

    private ProviderInfo providerInfo;

    private ObjectPool<TTransport> connPool;

    private GenericObjectPoolConfig poolConfig;

  public InvokeConn(ProviderInfo providerInfo) {
    this(providerInfo, new GenericObjectPoolConfig());
  }

  public InvokeConn(ProviderInfo providerInfo, GenericObjectPoolConfig poolConfig) {
    this.providerInfo = providerInfo;
    this.poolConfig = poolConfig;
    connPool = new GenericObjectPool<TTransport>(new ConsumerPoolFactory(providerInfo),poolConfig);
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

    public void setPoolConfig(GenericObjectPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }
}
