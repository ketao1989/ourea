/*
* Copyright (c) 2015 taocoder.com. All Rights Reserved.
*/
package com.taocoder.ourea.config;

import com.taocoder.ourea.common.Constants;

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

  public ThriftServerConfig(int port) {
    this.port = port;
  }

  public void setMinWorkerThreads(int minWorkerThreads) {
    MinWorkerThreads = minWorkerThreads;
  }

  public void setMaxWorkerThreads(int maxWorkerThreads) {
    MaxWorkerThreads = maxWorkerThreads;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public int getPort() {
    return port;
  }

  public int getMinWorkerThreads() {
    return MinWorkerThreads;
  }

  public int getMaxWorkerThreads() {
    return MaxWorkerThreads;
  }

  public String getGroup() {
    return group;
  }

  public String getVersion() {
    return version;
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }
}
