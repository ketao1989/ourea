/*
* Copyright (c) 2015 taocoder.com. All Rights Reserved.
*/
package com.taocoder.ourea.server;

import com.taocoder.ourea.model.ServiceInfo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author tao.ke Date: 16/4/25 Time: 下午4:09
 */
public class ServiceProviderProxy<T> implements InvocationHandler {

  private ServiceProvider serviceProvider;


  public ServiceProviderProxy(ServiceProvider serviceProvider, ServiceInfo serviceInfo) {
    this.serviceProvider = serviceProvider;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    return method.invoke(proxy,args);
  }
}
