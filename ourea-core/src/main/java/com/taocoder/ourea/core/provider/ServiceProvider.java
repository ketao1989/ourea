/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea.core.provider;

import com.taocoder.ourea.core.common.Constants;
import com.taocoder.ourea.core.common.OureaException;
import com.taocoder.ourea.core.registry.IRegistry;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taocoder.ourea.core.common.LocalIpUtils;
import com.taocoder.ourea.core.config.ThriftServerConfig;
import com.taocoder.ourea.core.config.ZkConfig;
import com.taocoder.ourea.core.model.ProviderInfo;
import com.taocoder.ourea.core.model.ServiceInfo;
import com.taocoder.ourea.core.registry.ZkRegistry;

/**
 * 目前只支持Iface接口
 *
 * @author tao.ke Date: 16/4/25 Time: 下午10:00
 */
public class ServiceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceProvider.class);

    /**
     * 接口实例
     */
    private Object refImpl;

    /**
     * 注册服务
     */
    private IRegistry registry;

    /**
     * server zk 配置
     */
    private ZkConfig zkConfig;

    /**
     * thrift server 配置
     */
    private ThriftServerConfig serverConfig;

    public ServiceProvider(Object refImpl, ZkConfig zkConfig, ThriftServerConfig serverConfig) {

        if (refImpl == null) {
            throw new IllegalArgumentException("invalid refImpl instance.");
        }
        this.refImpl = refImpl;
        this.zkConfig = zkConfig;
        this.serverConfig = serverConfig;
    }

    /**
     * 启动服务,如果设置为zk注册,则先注册服务; 如果启动thrift server失败,则注销该服务
     */
    public void start() {

        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.setIp(LocalIpUtils.getLocalIp());
        providerInfo.setPort(serverConfig.getPort());
        providerInfo.setWeight(serverConfig.getWeight());

        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setDirectInvoke(serverConfig.isDirectInvoke());
        serviceInfo.setGroup(serverConfig.getGroup());
        serviceInfo.setInterfaceClazz(getIfaceClass());
        serviceInfo.setVersion(serverConfig.getVersion());

        if (!serverConfig.isDirectInvoke()) {
            zkRegister(providerInfo, serviceInfo);
        }

        try {
            startServer();
        } catch (Exception e) {
            LOGGER.error("start thrift server fail.e:", e);
            if (!serverConfig.isDirectInvoke()) {
                unZkRegister(providerInfo, serviceInfo);
            }
        }
    }

    /**
     * 注册zk服务到指定zk集群上
     */
    private void zkRegister(ProviderInfo providerInfo, ServiceInfo serviceInfo) {

        // 创建一个新的register对象
        registry = new ZkRegistry(zkConfig);
        registry.register(serviceInfo, providerInfo, Constants.DEFAULT_INVOKER_PROVIDER);

        LOGGER.info("--------------start zk register--------------------");

    }

    /**
     * 启动thrift server
     */
    private void startServer() throws Exception {

        TServerSocket serverTransport = new TServerSocket(serverConfig.getPort());
        TThreadPoolServer.Args args = new TThreadPoolServer.Args(serverTransport);
        args.maxWorkerThreads = serverConfig.getMaxWorkerThreads();
        args.minWorkerThreads = serverConfig.getMinWorkerThreads();
        args.protocolFactory(new TBinaryProtocol.Factory());

        TProcessor tProcessor = getProcessorIface(getIfaceClass());
        args.processor(tProcessor);
        final TServer server = new TThreadPoolServer(args);
        server.setServerEventHandler(serverConfig.getServerEventHandler());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                server.serve();
            }
        });
        thread.setDaemon(serverConfig.isDaemonRun());
        thread.start();
        LOGGER.info("----------------start thrift server--------------");
    }

    /**
     * 启动失败需要将注册的服务注销掉
     */
    private void unZkRegister(ProviderInfo providerInfo, ServiceInfo serviceInfo) {

        if (registry == null) {
            LOGGER.error("registry is null.can not unregister zk service.");
            return;
        }

        registry.unregister(serviceInfo, providerInfo, Constants.DEFAULT_INVOKER_PROVIDER);
    }

    /**
     * 查询对象的iface类型
     * 
     * @return
     */
    private Class getIfaceClass() {

        Class[] interfaces = refImpl.getClass().getInterfaces();

        for (Class clazz : interfaces) {
            if (StringUtils.equals(clazz.getSimpleName(), "Iface")) {
                return clazz;
            }
        }
        throw new OureaException("refImpl is not thrift iface implement.");
    }

    /**
     * 根据refImpl来获取相应的TProcessor,然后构造一个对象
     * 
     * @return
     */
    private TProcessor getProcessorIface(Class iface) {

        if (iface == null) {
            LOGGER.error("refImpl is not thrift implement class instance.");
            throw new OureaException("invalid null refImpl params");
        }

        String parentClazzName = StringUtils.substringBeforeLast(iface.getCanonicalName(), ".Iface");
        String processorClazzName = parentClazzName + "$Processor";

        try {

            Class clazz = Class.forName(processorClazzName);
            if (clazz.isMemberClass() && !clazz.isInterface()) {
                @SuppressWarnings("unchecked")
                Class<TProcessor> processorClazz = (Class<TProcessor>) clazz;
                return processorClazz.getConstructor(iface).newInstance(refImpl);
            }
            return null;
        } catch (Exception e) {
            LOGGER.error("get thrift Porcessor class from Iface class fail.e:", e);
            throw new OureaException("invalid iface class params maybe not thrift class.");
        }
    }

}
