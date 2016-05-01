/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea.core.config;

import com.taocoder.ourea.core.common.Constants;
import com.taocoder.ourea.core.loadbalance.ILoadBalanceStrategy;
import com.taocoder.ourea.core.loadbalance.RoundRobinLoadBalanceStrategy;

/**
 * thrift client 相关配置
 *
 * @author tao.ke Date: 16/4/28 Time: 上午10:54
 */
public class ThriftClientConfig {

    /**
     * 服务组,不同组之间不能服务交互
     */
    private String group = Constants.DEFAULT_GROUP_NAME;

    /**
     * 服务版本,不同版本之间不能交互
     */
    private String version = Constants.DEFAULT_VERSION_VALUE;

    /**
     * 超时时间
     */
    private int timeout = Constants.DEFAULT_TIMEOUT_VALUE;

    /**
     * 服务重试次数,默认重试一次
     */
    private int retryTimes = 1;

    /**
     * 服务client的负载策略
     */
    private ILoadBalanceStrategy loadBalanceStrategy = new RoundRobinLoadBalanceStrategy();

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public ILoadBalanceStrategy getLoadBalanceStrategy() {
        return loadBalanceStrategy;
    }

    public void setLoadBalanceStrategy(ILoadBalanceStrategy loadBalanceStrategy) {
        this.loadBalanceStrategy = loadBalanceStrategy;
    }
}
