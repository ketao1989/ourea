/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea.server;

import com.taocoder.ourea.common.PropertiesUtils;
import com.taocoder.ourea.model.ProviderInfo;
import com.taocoder.ourea.model.ServiceInfo;
import com.taocoder.ourea.registry.IRegistry;
import com.taocoder.ourea.registry.ZkRegistry;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServerEventHandler;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Properties;

/**
 * 目前只支持Iface接口
 *
 * @author tao.ke Date: 16/4/25 Time: 下午10:00
 */
public class ZkServiceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkServiceProvider.class);

    private static final Properties properties = PropertiesUtils.load("server.properties");

    private static final String SERVER_ROLE = "provider";

    /**
     * 接口实例
     */
    private Object refImpl;

    /**
     * 是否注册zk
     */
    private boolean directInvoke;

    /**
     * 是否以daemon的形式运行
     */
    private boolean daemonRun;

    /**
     * thrift 服务handler
     */
    private TServerEventHandler serverEventHandler;

    /**
     * 注册服务
     */
    private IRegistry registry;

    public ZkServiceProvider(Object refImpl, boolean directInvoke, boolean daemonRun) {

        if (refImpl == null) {
            throw new IllegalArgumentException("invalid refImpl instance.");
        }
        this.refImpl = refImpl;
        this.directInvoke = directInvoke;
        this.daemonRun = daemonRun;
    }

    public void setServerEventHandler(TServerEventHandler serverEventHandler) {
        this.serverEventHandler = serverEventHandler;
    }

    /**
     * 启动服务,如果设置为zk注册,则先注册服务; 如果启动thrift server失败,则注销该服务
     */
    public void start() {

        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.setIp(getLocalIp());
        providerInfo.setPort(Integer.parseInt(properties.getProperty("port")));
        providerInfo.setWeight(1);

        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setDirectInvoke(directInvoke);
        serviceInfo.setGroup(properties.getProperty("group"));
        serviceInfo.setInterfaceClazz(refImpl.getClass());
        serviceInfo.setVersion(properties.getProperty("version"));

        if (!directInvoke) {
            zkRegister(providerInfo, serviceInfo);
        }

        try {
            startServer();
        } catch (Exception e) {
            LOGGER.error("start thrift server fail.e:", e);

            if (!directInvoke) {
                unZkRegister(providerInfo, serviceInfo);
            }
        }
    }

    /**
     * 注册zk服务到指定zk集群上
     */
    private void zkRegister(ProviderInfo providerInfo, ServiceInfo serviceInfo) {

        // 创建一个新的register对象
        registry = new ZkRegistry(properties.getProperty("zkAddress"),
                Integer.parseInt(properties.getProperty("zkTimeout")));
        registry.register(serviceInfo, providerInfo, SERVER_ROLE);

        System.out.println("--------------start zk register--------------------");

    }

    /**
     * 启动thrift server
     */
    private void startServer() throws Exception {

        TServerSocket serverTransport = new TServerSocket(Integer.parseInt(properties.getProperty("port")));
        TThreadPoolServer.Args args = new TThreadPoolServer.Args(serverTransport);
        args.maxWorkerThreads = Integer.parseInt(properties.getProperty("MaxWorkerThreads", "64"));
        args.minWorkerThreads = Integer.parseInt(properties.getProperty("MinWorkerThreads", "10"));
        args.protocolFactory(new TBinaryProtocol.Factory());

        TProcessor tProcessor = getProcessorIface();
        args.processor(tProcessor);
        final TServer server = new TThreadPoolServer(args);
        server.setServerEventHandler(serverEventHandler);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                server.serve();
            }
        });
        thread.setDaemon(daemonRun);
        thread.start();
    }

    /**
     * 启动失败需要将注册的服务注销掉
     */
    private void unZkRegister(ProviderInfo providerInfo, ServiceInfo serviceInfo) {

        if (registry == null) {
            LOGGER.error("registry is null.can not unregister zk service.");
            return;
        }

        registry.unregister(serviceInfo, providerInfo, SERVER_ROLE);
    }

    /**
     * 根据refImpl来获取相应的TProcessor,然后构造一个对象
     * 
     * @return
     */
    private TProcessor getProcessorIface() {

        Class[] interfaces = refImpl.getClass().getInterfaces();
        Class iface = null;

        for (Class clazz : interfaces) {
            if (StringUtils.equals(clazz.getSimpleName(), "Iface")) {
                iface = clazz;
                break;
            }
        }

        if (iface == null) {
            LOGGER.error("refImpl is not thrift implement class instance.");
            throw new IllegalArgumentException("invalid refImpl params");
        }

        String parentClazzName = StringUtils.substringBeforeLast(iface.getCanonicalName(), ".Iface");
        String processorClazzName = parentClazzName + "$Processor";
        System.out.println(processorClazzName);

        try {
            Class<TProcessor> processorClazz = (Class<TProcessor>) Class.forName(processorClazzName);
            return processorClazz.getConstructor(iface).newInstance(refImpl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("invalid refImpl params");
        }

    }

    /**
     * 获取本地ip地址,如果多个,选择第一个
     * 
     * @return
     */
    private String getLocalIp() {

        try {
            for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {

                NetworkInterface item = e.nextElement();

                for (InterfaceAddress address : item.getInterfaceAddresses()) {
                    if (address.getAddress() instanceof Inet4Address) {
                        Inet4Address inet4Address = (Inet4Address) address.getAddress();
                        if (inet4Address.isLoopbackAddress()) {
                            continue;
                        }
                        System.out.println(inet4Address.getHostAddress());
                        return inet4Address.getHostAddress();
                    }
                }

            }
        } catch (Exception e) {
            throw new IllegalStateException("no ip");
        }
        throw new IllegalStateException("no ip");
    }

}
