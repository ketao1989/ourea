/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea.core.config;

import com.taocoder.ourea.core.common.Constants;
import org.apache.thrift.server.TServerEventHandler;

/**
 * thrift 服务端相关配置
 *
 * @author tao.ke Date: 16/4/28 Time: 上午10:53
 */
public class ThriftServerConfig {

    /**
     * 端口号
     */
    private int port;

    /**
     * server最小工作线程数
     */
    private int MinWorkerThreads = 10;

    /**
     * server最大工作线程数
     */
    private int MaxWorkerThreads = 64;

    /**
     * server service 组
     */
    private String group = Constants.DEFAULT_GROUP_NAME;

    /**
     * 版本号
     */
    private String version = Constants.DEFAULT_VERSION_VALUE;

    /**
     * 服务器权重设置
     */
    private int weight = Constants.DEFAULT_WEIGHT_VALUE;

    /**
     * 服务状态,表示是否对外服务
     */
    private boolean status = true;

    /**
     * 是否注册zk
     */
    private boolean directInvoke = false;

    /**
     * 是否以daemon的形式运行
     */
    private boolean daemonRun = false;

    /**
     * thrift 服务handler
     */
    private TServerEventHandler serverEventHandler;

    public ThriftServerConfig() {
        this.port = Constants.DEFAULT_THRIFT_PORT;
    }

    public ThriftServerConfig(int port) {
        this.port = port;
    }

    public ThriftServerConfig(int port, boolean directInvoke, boolean daemonRun) {
        this.port = port;
        this.directInvoke = directInvoke;
        this.daemonRun = daemonRun;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setDirectInvoke(boolean directInvoke) {
        this.directInvoke = directInvoke;
    }

    public void setDaemonRun(boolean daemonRun) {
        this.daemonRun = daemonRun;
    }

    public boolean isDirectInvoke() {
        return directInvoke;
    }

    public boolean isDaemonRun() {
        return daemonRun;
    }

    public TServerEventHandler getServerEventHandler() {
        return serverEventHandler;
    }

    public void setServerEventHandler(TServerEventHandler serverEventHandler) {
        this.serverEventHandler = serverEventHandler;
    }

    public int getPort() {
        return port;
    }

    public int getMinWorkerThreads() {
        return MinWorkerThreads;
    }

    public void setMinWorkerThreads(int minWorkerThreads) {
        MinWorkerThreads = minWorkerThreads;
    }

    public int getMaxWorkerThreads() {
        return MaxWorkerThreads;
    }

    public void setMaxWorkerThreads(int maxWorkerThreads) {
        MaxWorkerThreads = maxWorkerThreads;
    }

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

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
