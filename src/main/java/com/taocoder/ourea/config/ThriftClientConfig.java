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
   * 服务client的负载策略
   */
  private ILoadBalanceStrategy loadBalanceStrategy;

  public ThriftClientConfig() {
    this(Constants.DEFAULT_GROUP_NAME, Constants.DEFAULT_VERSION_VALUE);
  }

  public ThriftClientConfig(String group, String version) {
    this(group, version, new RoundRobinLoadBalanceStrategy());
  }

  public ThriftClientConfig(String group, String version, ILoadBalanceStrategy loadBalanceStrategy) {
    this.group = group;
    this.version = version;
    this.loadBalanceStrategy = loadBalanceStrategy;
  }

  public String getGroup() {
    return group;
  }

  public String getVersion() {
    return version;
  }

  public ILoadBalanceStrategy getLoadBalanceStrategy() {
    return loadBalanceStrategy;
  }
}
