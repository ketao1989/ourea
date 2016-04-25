/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package com.taocoder.ourea.registry;

import com.google.common.base.Joiner;
import com.taocoder.ourea.common.Constants;
import com.taocoder.ourea.model.ProviderInfo;
import com.taocoder.ourea.model.ServiceInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * 注册信息格式 xx=yy&mm=nn
 *
 * @author tao.ke Date: 16/3/3 Time: 下午11:42
 */
public class ZkRegistry implements IRegistry {

    /**
     * 包含ip,port,weight
     */
    private static final String END_PATH_TEMPLATE = "ip=%s&port=%s&weight=%s";

    private CuratorFramework zkClient;

    public ZkRegistry(String zkAddress,int zkTimeout) {

        zkClient = CuratorFrameworkFactory.builder().connectString(zkAddress).sessionTimeoutMs(zkTimeout)
                .retryPolicy(new BoundedExponentialBackoffRetry(10, 1000, 3)).build();

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

        String parentPath = Joiner.on(Constants.PATH_SEPARATOR).join(Constants.ZK_PATH_PREFIX,
                info.getInterfaceClazz().getCanonicalName(), info.getGroup(), info.getVersion(), role);

        String endPath = String.format(END_PATH_TEMPLATE, providerInfo.getIp(), providerInfo.getPort(),
                providerInfo.getWeight());

        String path = parentPath + Constants.PATH_SEPARATOR + endPath;

        try {

            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);

        } catch (Exception e) {
            // handle exception
            e.printStackTrace();
        }

    }

    @Override
    public void unregister(ServiceInfo info, ProviderInfo providerInfo, String role) {

        String parentPath = Joiner.on(Constants.PATH_SEPARATOR).join(Constants.ZK_PATH_PREFIX,
                info.getInterfaceClazz().getCanonicalName(), info.getGroup(), info.getVersion(), role);
        String endPath = String.format(END_PATH_TEMPLATE, providerInfo.getIp(), providerInfo.getPort(),
                providerInfo.getWeight());
        String path = parentPath + Constants.PATH_SEPARATOR + endPath;

        try {
            zkClient.delete().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
