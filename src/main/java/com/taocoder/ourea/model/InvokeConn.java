/*
* Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
*/
package com.taocoder.ourea.model;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.Serializable;

/**
 * @author tao.ke Date: 16/3/3 Time: 下午2:35
 */
public class InvokeConn implements Serializable {

    private static final long serialVersionUID = -805739143582019252L;

    private ProviderInfo providerInfo;

    private ObjectPool connPool;

    private GenericObjectPoolConfig poolConfig;

    public ProviderInfo getProviderInfo() {
        return providerInfo;
    }

    public void setProviderInfo(ProviderInfo providerInfo) {
        this.providerInfo = providerInfo;
    }

    public ObjectPool getConnPool() {
        return connPool;
    }

    public void setConnPool(ObjectPool connPool) {
        this.connPool = connPool;
    }

    public GenericObjectPoolConfig getPoolConfig() {
        return poolConfig;
    }

    public void setPoolConfig(GenericObjectPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }
}
