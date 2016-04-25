/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea.server;

import com.taocoder.ourea.common.ClassUtils;
import com.taocoder.ourea.model.ServiceInfo;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServerEventHandler;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;

/**
 * @author tao.ke Date: 16/4/25 Time: 下午4:20
 */
public class ServiceProviderImpl implements IServiceProvider {

  /**
   * 服务配置相关信息
   */
  private ServiceInfo serviceInfo;

  /**
   * Thrift最小线程数
   */
  private int MinWorkerThreads;

  /**
   * Thrift最大线程数
   */
  private int MaxWorkerThreads;

  /**
   * 接口实例
   */
  private Object refImpl;

  /**
   * 服务端事务处理handler
   */
  private TServerEventHandler serverEventHandler;

  /**
   * 对外暴露服务的端口号
   */
  private int port;

  public ServiceProviderImpl(ServiceInfo serviceInfo, Object refImpl, int port) {
    this.serviceInfo = serviceInfo;
    this.refImpl = refImpl;
    this.port = port;
  }

  public void setMinWorkerThreads(int minWorkerThreads) {
    MinWorkerThreads = minWorkerThreads;
  }

  public void setMaxWorkerThreads(int maxWorkerThreads) {
    MaxWorkerThreads = maxWorkerThreads;
  }

  public void setServerEventHandler(TServerEventHandler serverEventHandler) {
    this.serverEventHandler = serverEventHandler;
  }


  @Override
  public void start() {

    try {
      TServerSocket serverTransport = new TServerSocket(port);
      TThreadPoolServer.Args args = new TThreadPoolServer.Args(serverTransport);
      args.maxWorkerThreads = this.MaxWorkerThreads;
      args.minWorkerThreads = this.MinWorkerThreads;
      args.protocolFactory(new TBinaryProtocol.Factory());

      TProcessor tProcessor = ClassUtils.getProcessorConstructorIface(serviceInfo.getInterfaceClazz())
          .newInstance(refImpl);
      args.processor(tProcessor);
      final TServer server = new TThreadPoolServer(args);
      server.setServerEventHandler(serverEventHandler);
      Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
          server.serve();
        }
      });
      thread.setDaemon(false);
      thread.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
