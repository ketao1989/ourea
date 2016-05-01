/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package com.taocoder.ourea.core.model;

import com.taocoder.ourea.core.consumer.ConsumerPoolFactory;
import com.taocoder.ourea.core.config.OureaObjectPoolConfig;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        InvokeConn conn = (InvokeConn) o;

        return new EqualsBuilder()
                .append(providerInfo, conn.providerInfo)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(providerInfo)
                .toHashCode();
    }
}
