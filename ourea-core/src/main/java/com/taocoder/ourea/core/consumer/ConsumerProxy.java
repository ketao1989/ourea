/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea.core.consumer;

import com.taocoder.ourea.core.common.Constants;
import com.taocoder.ourea.core.common.LocalIpUtils;
import com.taocoder.ourea.core.common.OureaConnCreateException;
import com.taocoder.ourea.core.common.OureaException;
import com.taocoder.ourea.core.config.ThriftClientConfig;
import com.taocoder.ourea.core.config.ZkConfig;
import com.taocoder.ourea.core.model.Invocation;
import com.taocoder.ourea.core.model.InvokeConn;
import com.taocoder.ourea.core.model.ProviderInfo;
import com.taocoder.ourea.core.model.ServiceInfo;
import com.taocoder.ourea.core.registry.INotifyListener;
import com.taocoder.ourea.core.registry.IRegistry;
import com.taocoder.ourea.core.registry.ZkRegistry;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author tao.ke Date: 16/4/25 Time: 下午3:20
 */
public class ConsumerProxy implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerProxy.class);

    /**
     * 本机信息
     */
    private static final ProviderInfo PROVIDER_INFO = new ProviderInfo(LocalIpUtils.getLocalIp());

    /**
     * service 对外提供服务的provider的连接
     */
    private final ConcurrentHashMap<ProviderInfo, InvokeConn> PROVIDER_CONN_CONCURRENT_MAP = new ConcurrentHashMap<ProviderInfo, InvokeConn>();
    /**
     * 对PROVIDER_CONN_CONCURRENT_MAP操作时,需要获取锁.读不需要
     */
    private final Object PROVIDER_CONN_LOCK = new Object();

    /**
     * 所有服务的list表,冗余PROVIDER_CONN_CONCURRENT_MAP,便于获取连接时,直接获取.
     */
    private CopyOnWriteArrayList<InvokeConn> PROVIDER_CONN_LIST = new CopyOnWriteArrayList<InvokeConn>();

    /**
     * 调用的class
     */
    private ServiceInfo serviceInfo;

    /**
     * 负载策略
     */
    private ThriftClientConfig clientConfig;

    /**
     * zk相关配置
     */
    private ZkConfig zkConfig;

    /**
     * 注册服务
     */
    private IRegistry registry;

    /**
     * TServiceClient 构造器
     */
    private Constructor<TServiceClient> serviceClientConstructor;

    public ConsumerProxy() {
    }

    public ConsumerProxy(ServiceInfo serviceInfo, ZkConfig zkConfig, ThriftClientConfig clientConfig) {
        this.serviceInfo = serviceInfo;
        this.clientConfig = clientConfig;
        this.zkConfig = zkConfig;
        this.serviceClientConstructor = getClientConstructorClazz();
        initZkConsumer();
        initScanFailConn();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        int remainRetryTimes = clientConfig.getRetryTimes();
        String exceptionMsg = null;

        do {

            ObjectPool<TTransport> connPool = null;
            TTransport transport = null;
            InvokeConn conn = null;
            try {
                Invocation invocation = new Invocation(serviceInfo.getInterfaceClazz().getName(), method.getName());
                conn = clientConfig.getLoadBalanceStrategy().select(PROVIDER_CONN_LIST, invocation);
                connPool = conn.getConnPool();
                transport = connPool.borrowObject();
                TProtocol protocol = new TBinaryProtocol(transport);
                TServiceClient client = serviceClientConstructor.newInstance(protocol);

                return method.invoke(client, args);
            } catch (Exception e) {
                // 服务多次重试连接不上,则直接将该服务对应信息移除
                if (e instanceof OureaConnCreateException) {
                    if (PROVIDER_CONN_LIST.remove(conn) && conn != null && conn.getConnPool() != null){
                        conn.getConnPool().close();
                    }
                }
                LOGGER.warn("invoke thrift rpc provider fail.e:", e);
                exceptionMsg = e.getMessage();
            } finally {
                if (connPool != null && transport != null) {
                    connPool.invalidateObject(transport);
                }
            }
        } while (remainRetryTimes-- > 0);

        throw new OureaException("invoke fail.msg:" + exceptionMsg);
    }

    /**
     * 初始化consumer 的zk
     */
    public void initZkConsumer() {

        registry = new ZkRegistry(zkConfig);

        registry.register(serviceInfo, PROVIDER_INFO, Constants.DEFAULT_INVOKER_CONSUMER);

        INotifyListener listener = new INotifyListener() {
            @Override
            public void notify(Set<ProviderInfo> providerInfos) {
                synchronized (PROVIDER_CONN_LOCK) {
                    for (ProviderInfo info : providerInfos) {
                        if (!PROVIDER_CONN_CONCURRENT_MAP.containsKey(info)) {
                            InvokeConn invokeConn = new InvokeConn(info, clientConfig.getTimeout());
                            PROVIDER_CONN_CONCURRENT_MAP.putIfAbsent(info, invokeConn);
                            PROVIDER_CONN_LIST.add(invokeConn);
                        }
                    }

                    for (Map.Entry<ProviderInfo, InvokeConn> entry : PROVIDER_CONN_CONCURRENT_MAP.entrySet()) {
                        if (!providerInfos.contains(entry.getKey())) {
                            PROVIDER_CONN_LIST.remove(entry.getValue());
                            PROVIDER_CONN_CONCURRENT_MAP.remove(entry.getKey());
                        }
                    }
                }
            }
        };
        registry.subscribe(serviceInfo, listener);
    }

    /**
     * 定时10s扫描失败的conn,看是否网络恢复可以提供访问. fixme: 新加了主动pull zk数据现场,这里暂时可以不需要了
     */
    private void initScanFailConn() {

        // new ScheduledThreadPoolExecutor(1).scheduleWithFixedDelay(new Runnable() {
        // @Override
        // public void run() {
        // synchronized (PROVIDER_CONN_LOCK) {
        // // 这里简单处理,把确定活的交给业务处理,一般30s内,zk是会触发listener的.
        // // 如果过了30s还未被zk listener清空,说明只是网络暂时慢,可以让业务再重试看看
        // PROVIDER_CONN_LIST.addAll(PROVIDER_FAIL_CONN_LIST);
        // }
        // }
        // },1L, 30L, TimeUnit.SECONDS);
    }

    private Constructor<TServiceClient> getClientConstructorClazz() {

        String parentClazzName = StringUtils.substringBeforeLast(serviceInfo.getInterfaceClazz().getCanonicalName(),
                ".Iface");
        String clientClazzName = parentClazzName + "$Client";

        try {
            return ((Class<TServiceClient>) Class.forName(clientClazzName)).getConstructor(TProtocol.class);
        } catch (Exception e) {
            //
            LOGGER.error("get thrift client class constructor fail.e:", e);
            throw new IllegalArgumentException("invalid iface implement");
        }

    }

}
