/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * @author tao.ke Date: 16/4/28 Time: 上午10:59
 */
public class OureaObjectPoolConfig extends GenericObjectPoolConfig {

    /**
     * 最大连接数
     */
    private int maxTotal = 100;

    /**
     * 最大空闲连接数
     */
    private int maxIdle = 32;

    /**
     * 最小空闲连接数
     */
    private int minIdle = 10;

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    @Override
    public OureaObjectPoolConfig clone() {

        return (OureaObjectPoolConfig) super.clone();

    }
}
