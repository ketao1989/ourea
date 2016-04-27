/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea.registry;

import com.taocoder.ourea.model.ProviderInfo;

import java.util.Set;

/**
 * @author tao.ke Date: 16/4/27 Time: 上午10:24
 */
public interface INotifyListener {

  /**
   * 根据监听到得变更获取最新的服务提供者的列表
   * 
   * @param providerInfos
   */
  void notify(Set<ProviderInfo> providerInfos);

}
