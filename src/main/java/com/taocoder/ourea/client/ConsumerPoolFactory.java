/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea.client;

import com.taocoder.ourea.model.ProviderInfo;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 * @author tao.ke Date: 16/4/25 Time: 下午4:46
 */
public class ConsumerPoolFactory implements PooledObjectFactory<TTransport> {

  /**
   * 服务提供者的信息
   */
  private ProviderInfo providerInfo;

  /**
   * 调用服务的超时时间
   */
  private int timeout;

  public ConsumerPoolFactory(ProviderInfo providerInfo, int timeout) {
    this.providerInfo = providerInfo;
    this.timeout = timeout;
  }

  @Override
  public PooledObject<TTransport> makeObject() throws Exception {
    TTransport transport = null;
    transport = new TSocket(providerInfo.getIp(),providerInfo.getPort(),timeout);
    transport.open();
    ((TSocket)transport).setTimeout(timeout);
    return new DefaultPooledObject<TTransport>(transport);
  }

  @Override
  public void destroyObject(PooledObject<TTransport> p) throws Exception {

    TTransport transport = p.getObject();
    if (transport.isOpen()){
      transport.close();
    }
  }

  @Override
  public boolean validateObject(PooledObject<TTransport> p) {
    if (p.getObject().isOpen()){
      return true;
    }
    return false;
  }

  @Override
  public void activateObject(PooledObject<TTransport> p) throws Exception {

  }

  @Override
  public void passivateObject(PooledObject<TTransport> p) throws Exception {

  }
}
