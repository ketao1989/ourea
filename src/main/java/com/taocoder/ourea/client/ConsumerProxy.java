/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea.client;

import com.google.common.collect.Lists;

import com.taocoder.ourea.common.Constants;
import com.taocoder.ourea.common.LocalIpUtils;
import com.taocoder.ourea.common.PropertiesUtils;
import com.taocoder.ourea.loadbalance.ILoadBalanceStrategy;
import com.taocoder.ourea.loadbalance.RoundRobinLoadBalanceStrategy;
import com.taocoder.ourea.model.Invocation;
import com.taocoder.ourea.model.InvokeConn;
import com.taocoder.ourea.model.ProviderInfo;
import com.taocoder.ourea.model.ServiceInfo;
import com.taocoder.ourea.registry.INotifyListener;
import com.taocoder.ourea.registry.IRegistry;
import com.taocoder.ourea.registry.ZkRegistry;

import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author tao.ke Date: 16/4/25 Time: 下午3:20
 */
public class ConsumerProxy implements InvocationHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerProxy.class);

  private static final Properties properties = PropertiesUtils.load("consumer.properties");

  /**
   * 本机信息
   */
  private static final ProviderInfo PROVIDER_INFO = new ProviderInfo(LocalIpUtils.getLocalIp());

  /**
   * service 对外提供服务的provider的连接
   */
  private static final ConcurrentHashMap<ProviderInfo, InvokeConn> PROVIDER_CONN_CONCURRENT_MAP = new ConcurrentHashMap<ProviderInfo, InvokeConn>();

  /**
   * 所有服务的list表,冗余PROVIDER_CONN_CONCURRENT_MAP,便于获取连接时,直接获取.
   */
  private static List<InvokeConn> PROVIDER_CONN_LIST = new CopyOnWriteArrayList<InvokeConn>();

  /**
   * 对PROVIDER_CONN_CONCURRENT_MAP操作时,需要获取锁.读不需要
   */
  private static final Object PROVIDER_CONN_LOCK = new Object();

  /**
   * 重试次数
   */
  private static final int RETRY_TIMES = 1;

  /**
   * 调用的class
   */
  private ServiceInfo serviceInfo;

  /**
   * 负载策略
   */
  private ILoadBalanceStrategy loadBalanceStrategy;

  /**
   * 注册服务
   */
  private IRegistry registry;

  private Constructor<TServiceClient> serviceClientConstructor;

  public ConsumerProxy(ServiceInfo serviceInfo) {
    this(serviceInfo, new RoundRobinLoadBalanceStrategy());
  }

  public ConsumerProxy(ServiceInfo serviceInfo, ILoadBalanceStrategy loadBalanceStrategy) {
    this.serviceInfo = serviceInfo;
    this.loadBalanceStrategy = loadBalanceStrategy;
    this.serviceClientConstructor = getClientConstructorClazz();
    initZkConsumer();
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    int remainRetryTimes = RETRY_TIMES;
    String exceptionMsg = null;

    do {

      Invocation invocation = new Invocation(serviceInfo.getInterfaceClazz().getName(), method.getName());
      InvokeConn invokeConn = loadBalanceStrategy.select(PROVIDER_CONN_LIST, invocation);
      TProtocol protocol = new TBinaryProtocol(invokeConn.getConnPool().borrowObject());
      TServiceClient client = serviceClientConstructor.newInstance(protocol);

      try {
        return method.invoke(client, args);
      } catch (Exception e) {
        LOGGER.warn("invoke thrift rpc provider fail.e:", e);
        exceptionMsg = e.getMessage();
      }
    } while (remainRetryTimes-- > 0);

    throw new IllegalStateException("invoke fail.msg:" + exceptionMsg);
  }

  /**
   * 初始化consumer 的zk
   */
  public void initZkConsumer() {

    registry = new ZkRegistry(properties.getProperty("zkAddress"),
        Integer.parseInt(properties.getProperty("zkTimeout")));

    registry.register(serviceInfo, PROVIDER_INFO, Constants.DEFAULT_INVOKER_CONSUMER);

    INotifyListener listener = new INotifyListener() {
      @Override
      public void notify(Set<ProviderInfo> providerInfos) {
        synchronized (PROVIDER_CONN_LOCK) {
          for (ProviderInfo info : providerInfos) {
            if (!PROVIDER_CONN_CONCURRENT_MAP.containsKey(info)) {
              InvokeConn invokeConn = new InvokeConn(info);
              PROVIDER_CONN_CONCURRENT_MAP.putIfAbsent(info, invokeConn);
            }
          }
        }
        for (Map.Entry<ProviderInfo, InvokeConn> entry : PROVIDER_CONN_CONCURRENT_MAP.entrySet()) {
          if (!providerInfos.contains(entry.getKey())) {
            PROVIDER_CONN_CONCURRENT_MAP.remove(entry.getKey());
          }
        }
        PROVIDER_CONN_LIST = Lists.newArrayList( PROVIDER_CONN_CONCURRENT_MAP.values());
      }
    };
    registry.subscribe(serviceInfo, listener);
  }

  private Constructor<TServiceClient> getClientConstructorClazz() {

    String parentClazzName = StringUtils.substringBeforeLast(serviceInfo.getInterfaceClazz().getCanonicalName(),
        ".Iface");
    String clientClazzName = parentClazzName + "$Client";

    try {
      return ((Class<TServiceClient>) Class.forName(clientClazzName)).getConstructor(TProtocol.class);
    } catch (Exception e) {
      //
      e.printStackTrace();
    }
    throw new IllegalArgumentException("invalid iface implement");
  }

}
