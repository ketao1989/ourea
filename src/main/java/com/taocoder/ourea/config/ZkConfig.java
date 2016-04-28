/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea.config;

/**
 * zk相关配置
 * 
 * @author tao.ke Date: 16/4/28 Time: 上午10:53
 */
public class ZkConfig {

  /**
   * zk ip:port,
   */
  private String zkAddress;

  /**
   * zk 超时时间
   */
  private int zkTimeout = 3000;

  /**
   * 重试之间初始等待时间
   */
  private int baseSleepTimeMs = 10;

  /**
   * 重试之间最长等待时间
   */
  private int maxSleepTimeMs = 1000;

  /**
   * 重试次数
   */
  private int maxRetries = 3;

  public ZkConfig(String zkAddress) {
    this.zkAddress = zkAddress;
  }

  public String getZkAddress() {
    return zkAddress;
  }

  public int getZkTimeout() {
    return zkTimeout;
  }

  public void setZkTimeout(int zkTimeout) {
    this.zkTimeout = zkTimeout;
  }

  public int getBaseSleepTimeMs() {
    return baseSleepTimeMs;
  }

  public void setBaseSleepTimeMs(int baseSleepTimeMs) {
    this.baseSleepTimeMs = baseSleepTimeMs;
  }

  public int getMaxSleepTimeMs() {
    return maxSleepTimeMs;
  }

  public void setMaxSleepTimeMs(int maxSleepTimeMs) {
    this.maxSleepTimeMs = maxSleepTimeMs;
  }

  public int getMaxRetries() {
    return maxRetries;
  }

  public void setMaxRetries(int maxRetries) {
    this.maxRetries = maxRetries;
  }

}
