/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package com.taocoder.ourea.core.registry;

import com.google.common.base.Joiner;
import com.taocoder.ourea.core.common.Constants;
import com.taocoder.ourea.core.common.OureaException;
import com.taocoder.ourea.core.common.ProviderInfoUtils;
import com.taocoder.ourea.core.model.ServiceInfo;
import com.taocoder.ourea.core.config.ZkConfig;
import com.taocoder.ourea.core.model.ProviderInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 注册信息格式 xx=yy&mm=nn
 *
 * @author tao.ke Date: 16/3/3 Time: 下午11:42
 */
public class ZkRegistry implements IRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkRegistry.class);

    /**
     * 节点的名字模板
     */
    private static final String END_PATH_PREFIX_TEMPLATE = "ip=%s&port=%s";
    private static final String END_PATH_TEMPLATE = END_PATH_PREFIX_TEMPLATE + "&weight=%s&status=%s";

    private CuratorFramework zkClient;

    public ZkRegistry(ZkConfig zkConfig) {

        zkClient = CuratorFrameworkFactory.builder().connectString(zkConfig.getZkAddress())
                .sessionTimeoutMs(zkConfig.getZkTimeout())
                .retryPolicy(new BoundedExponentialBackoffRetry(zkConfig.getBaseSleepTimeMs(),
                        zkConfig.getMaxSleepTimeMs(), zkConfig.getMaxRetries()))
                .build();

        zkClient.start();
    }

    @Override
    public void register(ServiceInfo info, ProviderInfo providerInfo, String role) {

        if (info == null) {
            throw new IllegalArgumentException("register info param invalid.");
        }

        if (info.getInterfaceClazz() == null || StringUtils.isEmpty(role)) {
            throw new IllegalArgumentException("register info.clazz or role param invalid.");
        }

        String parentPath = Joiner.on(Constants.PATH_SEPARATOR).join(Constants.ZK_PATH_PREFIX, adjustClazzName(info),
                info.getGroup(), info.getVersion(), role);

        String endPath = String.format(END_PATH_TEMPLATE, providerInfo.getIp(), providerInfo.getPort(),
                providerInfo.getWeight(), providerInfo.isStatus());

        String path = parentPath + Constants.PATH_SEPARATOR + endPath;

        try {

            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
            LOGGER.info("register thrift service in path :{}", path);

        } catch (Exception e) {
            // handle exception
            LOGGER.error("register thrift service error.path:{},e:", path, e);
            throw new OureaException("register thrift service error." + e.getMessage());
        }

    }

    @Override
    public void unregister(ServiceInfo info, ProviderInfo providerInfo, String role) {

        String parentPath = Joiner.on(Constants.PATH_SEPARATOR).join(Constants.ZK_PATH_PREFIX, adjustClazzName(info),
                info.getGroup(), info.getVersion(), role);
        String endPrefixPath = String.format(END_PATH_PREFIX_TEMPLATE, providerInfo.getIp(), providerInfo.getPort());

        try {

            // 由于状态可能会变更导致节点变化,所以这里先获取list,然后找到对应服务的前缀节点处理
            List<String> nodes = zkClient.getChildren().forPath(parentPath);

            for (String endPath : nodes) {
                if (endPath.startsWith(endPrefixPath)) {
                    zkClient.delete().forPath(parentPath + Constants.PATH_SEPARATOR + endPath);
                }
            }

        } catch (Exception e) {
            LOGGER.error("unregister thrift service error.parentPath:{},endPrefixPath:{},e:", parentPath, endPrefixPath,
                    e);
            throw new OureaException("unregister thrift service error." + e.getMessage());
        }
    }

    @Override
    public void subscribe(ServiceInfo info, final INotifyListener listener) {

        final String parentPath = Joiner.on(Constants.PATH_SEPARATOR).join(Constants.ZK_PATH_PREFIX,
                adjustClazzName(info), info.getGroup(), info.getVersion(), Constants.DEFAULT_INVOKER_PROVIDER);
        try {

            List<String> nodes = zkClient.getChildren().usingWatcher(new CuratorWatcher() {
                @Override
                public void process(WatchedEvent event) throws Exception {
                    List<String> childrenNodes = zkClient.getChildren().usingWatcher(this).forPath(parentPath);
                    listener.notify(ProviderInfoUtils.convertZkChildren(childrenNodes));
                }
            }).forPath(parentPath);

            listener.notify(ProviderInfoUtils.convertZkChildren(nodes));

        } catch (Exception e) {
            LOGGER.error("subscribe thrift service error.path:{},e:", parentPath, e);
            throw new OureaException("subscribe thrift service error." + e.getMessage());
        }

        // 主动拉取数据,防止zk监听失效
        new ScheduledThreadPoolExecutor(1).schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> nodes = zkClient.getChildren().forPath(parentPath);
                    listener.notify(ProviderInfoUtils.convertZkChildren(nodes));
                } catch (Exception e) {
                    LOGGER.warn("pull zookeeper path:{} children fail.e:", parentPath, e);
                }
            }
        }, 10L, TimeUnit.SECONDS);
    }

    /**
     * thrift内部类的类名不一致,这里统一一种,保证发布订阅是一样的
     * 
     * @param info
     * @return
     */
    private String adjustClazzName(ServiceInfo info) {

        String clazzName = info.getInterfaceClazz().getCanonicalName();

        if (StringUtils.endsWith(clazzName, ".Iface")) {
            return StringUtils.substringBefore(clazzName, ".Iface") + "$Iface";
        }
        return clazzName;
    }

}
