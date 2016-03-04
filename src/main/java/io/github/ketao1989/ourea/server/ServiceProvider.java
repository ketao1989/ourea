/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package io.github.ketao1989.ourea.server;

import com.facebook.fb303.FacebookService;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import io.github.ketao1989.ourea.common.ClassUtils;
import io.github.ketao1989.ourea.registry.ZkRegistry;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServerEventHandler;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;

import java.util.Map;

/**
 * @author tao.ke Date: 16/3/3 Time: 下午5:28
 */
public class ServiceProvider {

    /**
     * zk集群地址,如果为空,则不注册zk集群
     */
    private String zkAddress;

    /**
     * zk连接超时时间
     */
    private int zkTimeout;

    /**
     * Thrift最小线程数
     */
    private int MinWorkerThreads;

    /**
     * Thrift最大线程数
     */
    private int MaxWorkerThreads;

    /**
     * 服务唯一标记,一般推荐使用项目组.如果不设置,默认使用interfaceClazz名称
     */
    private String id;

    /**
     * 对外暴露的接口类
     */
    private Class interfaceClazz;

    /**
     * 接口实例
     */
    private Object refImpl;

    /**
     * 对外暴露服务的端口号
     */
    private int port;

    /**
     * 暴露服务的分组
     */
    private String group;

    private TServerEventHandler serverEventHandler;

    private TProcessor tProcessor;

    private CuratorFramework zkClient;

    private String info;

    public String getZkAddress() {
        return zkAddress;
    }

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    public int getZkTimeout() {
        return zkTimeout;
    }

    public void setZkTimeout(int zkTimeout) {
        this.zkTimeout = zkTimeout;
    }

    public int getMinWorkerThreads() {
        return MinWorkerThreads;
    }

    public void setMinWorkerThreads(int minWorkerThreads) {
        MinWorkerThreads = minWorkerThreads;
    }

    public int getMaxWorkerThreads() {
        return MaxWorkerThreads;
    }

    public void setMaxWorkerThreads(int maxWorkerThreads) {
        MaxWorkerThreads = maxWorkerThreads;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Class getInterfaceClazz() {
        return interfaceClazz;
    }

    public void setInterfaceClazz(Class interfaceClazz) {
        this.interfaceClazz = interfaceClazz;
    }

    public Object getRefImpl() {
        return refImpl;
    }

    public void setRefImpl(Object refImpl) {
        this.refImpl = refImpl;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public TServerEventHandler getServerEventHandler() {
        return serverEventHandler;
    }

    public void setServerEventHandler(TServerEventHandler serverEventHandler) {
        this.serverEventHandler = serverEventHandler;
    }

    public TProcessor gettProcessor() {
        return tProcessor;
    }

    public void settProcessor(TProcessor tProcessor) {
        this.tProcessor = tProcessor;
    }

    // 启动server
    public void start() {

        try {

            init();

            TServerSocket serverTransport = new TServerSocket(port);
            TThreadPoolServer.Args args = new TThreadPoolServer.Args(serverTransport);
            args.maxWorkerThreads = getMaxWorkerThreads();
            args.minWorkerThreads = getMinWorkerThreads();
            args.protocolFactory(new TBinaryProtocol.Factory());

            TProcessor tProcessor = ClassUtils.getProcessorConstructorIface(interfaceClazz).newInstance(refImpl);
            args.processor(tProcessor);
            final TServer server = new TThreadPoolServer(args);
            server.setServerEventHandler(serverEventHandler);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    server.serve();
                }
            });
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            destroy();
        }

    }

    private void destroy() {

        if (zkClient != null){
            if (StringUtils.isNoneBlank(info)){
                ZkRegistry zkRegistry = new ZkRegistry(zkClient);
                zkRegistry.unRegister(info);
            }
        }

    }

    private void init() throws Exception {

        zkClient = CuratorFrameworkFactory.builder().connectString(zkAddress)
                .sessionTimeoutMs(zkTimeout).retryPolicy(new BoundedExponentialBackoffRetry(10, 1000, 3)).build();
        ZkRegistry zkRegistry = new ZkRegistry(zkClient);

        Map<String,String> map = Maps.newHashMap();
        map.put("interface",interfaceClazz.getCanonicalName());
        map.put("port",String.valueOf(port));
        map.put("server",getLocalIp());
        map.put("group",group);

        FacebookService.Iface facebookService = (FacebookService.Iface) refImpl;

        map.put("version",facebookService.getVersion());
        map.put("status",String.valueOf(facebookService.getStatus().getValue()));
        info = Joiner.on('&').withKeyValueSeparator("=").join(map);

        zkRegistry.register(info);
    }

    private String getLocalIp() {
        return null;
    }

}
