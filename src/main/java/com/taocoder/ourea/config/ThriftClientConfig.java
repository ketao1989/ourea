/*
* Copyright (c) 2015 taocoder.com. All Rights Reserved.
*/
package com.taocoder.ourea.config;

import com.taocoder.ourea.common.Constants;
import com.taocoder.ourea.loadbalance.ILoadBalanceStrategy;
import com.taocoder.ourea.loadbalance.RoundRobinLoadBalanceStrategy;

/**
 * thrift client 相关配置
 *
 * @author tao.ke Date: 16/4/28 Time: 上午10:54
 */
public class ThriftClientConfig {

  /**
   * 服务组,不同组之间不能服务交互
   */
  private String group;

  /**
   * 服务版本,不同版本之间不能交互
   */
  private String version;

  /**
   * 超时时间
   */
  private int timeout;

  /**
   * 服务client的负载策略
   */
  private ILoadBalanceStrategy loadBalanceStrategy;

  public ThriftClientConfig() {
    this(Constants.DEFAULT_GROUP_NAME, Constants.DEFAULT_VERSION_VALUE);
  }

  public ThriftClientConfig(String group, String version) {
    this(group, version, Constants.DEFAULT_TIMEOUT_VALUE);
  }

  public ThriftClientConfig(String group, String version, int timeout) {
    this(group, version, timeout, new RoundRobinLoadBalanceStrategy());
  }

  public ThriftClientConfig(String group, String version, int timeout, ILoadBalanceStrategy loadBalanceStrategy) {
    this.group = group;
    this.version = version;
    this.timeout = timeout;
    this.loadBalanceStrategy = loadBalanceStrategy;
  }

  public String getGroup() {
    return group;
  }

  public String getVersion() {
    return version;
  }

  public int getTimeout() {
    return timeout;
  }

  public ILoadBalanceStrategy getLoadBalanceStrategy() {
    return loadBalanceStrategy;
  }
}
