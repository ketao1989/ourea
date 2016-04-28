/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package com.taocoder.ourea.registry;

import com.google.common.base.Joiner;

import com.taocoder.ourea.common.Constants;
import com.taocoder.ourea.common.OureaException;
import com.taocoder.ourea.common.ProviderInfoUtils;
import com.taocoder.ourea.config.ZkConfig;
import com.taocoder.ourea.model.ProviderInfo;
import com.taocoder.ourea.model.ServiceInfo;

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

/**
 * 注册信息格式 xx=yy&mm=nn
 *
 * @author tao.ke Date: 16/3/3 Time: 下午11:42
 */
public class ZkRegistry implements IRegistry {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZkRegistry.class);
  /**
   * 包含ip,port,weight
   */
  private static final String END_PATH_TEMPLATE = "ip=%s&port=%s&weight=%s";

  private CuratorFramework zkClient;

  public ZkRegistry(ZkConfig zkConfig) {

    zkClient = CuratorFrameworkFactory.builder().connectString(zkConfig.getZkAddress())
        .sessionTimeoutMs(zkConfig.getZkTimeout())
        .retryPolicy(new BoundedExponentialBackoffRetry(zkConfig.getBaseSleepTimeMs(), zkConfig.getMaxSleepTimeMs(),
            zkConfig.getMaxRetries()))
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
        providerInfo.getWeight());

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
    String endPath = String.format(END_PATH_TEMPLATE, providerInfo.getIp(), providerInfo.getPort(),
        providerInfo.getWeight());
    String path = parentPath + Constants.PATH_SEPARATOR + endPath;

    try {
      zkClient.delete().forPath(path);
    } catch (Exception e) {
      LOGGER.error("unregister thrift service error.path:{},e:", path, e);
      throw new OureaException("unregister thrift service error." + e.getMessage());
    }
  }

  @Override
  public void subscribe(ServiceInfo info, final INotifyListener listener) {

    final String parentPath = Joiner.on(Constants.PATH_SEPARATOR).join(Constants.ZK_PATH_PREFIX, adjustClazzName(info),
        info.getGroup(), info.getVersion(), Constants.DEFAULT_INVOKER_PROVIDER);
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
