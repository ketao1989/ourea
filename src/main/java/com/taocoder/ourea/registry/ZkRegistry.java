/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package com.taocoder.ourea.registry;

import com.taocoder.ourea.common.PathUtils;
import com.taocoder.ourea.common.Constants;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;

/**
 * 注册信息格式 xx=yy&mm=nn
 *
 * @author tao.ke Date: 16/3/3 Time: 下午11:42
 */
public class ZkRegistry implements IRegistry {

    private CuratorFramework zkClient;

    public ZkRegistry(CuratorFramework zkClient) {
        this.zkClient = zkClient;
    }

    @Override
    public void register(String info) {

        if (StringUtils.isBlank(info)) {
            throw new IllegalArgumentException("register info param invalid.");
        }

        String parentPath = PathUtils.buildPath(info);

        zkClient.start();

        try {

            zkClient.create().creatingParentsIfNeeded().forPath(parentPath);

            String path = parentPath + Constants.PATH_SEPARATOR + info;

            zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(path);

        } catch (Exception e) {
            // handle exception
            e.printStackTrace();
        }

    }

    @Override
    public void unRegister(String info) {

        zkClient.start();

        String path = PathUtils.buildPath(info) + Constants.PATH_SEPARATOR + info;
        try {
            zkClient.delete().forPath(path);
        } catch (Exception e) {

            // handle exception
        }

    }

    @Override
    public void subscribe(String info) {

    }

    @Override
    public void unSubscribe(String info) {

    }

}
