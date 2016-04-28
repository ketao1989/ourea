/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea.common;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.taocoder.ourea.model.ProviderInfo;

/**
 * @author tao.ke Date: 16/4/27 Time: 上午11:01
 */
public class ProviderInfoUtils {

    private static final ConcurrentHashMap<String, ProviderInfo> PROVIDER_INFO_CONCURRENT_HASH_MAP = new ConcurrentHashMap<String, ProviderInfo>();

    /**
     * 将监听到的子节点列表转换为服务信息
     * 
     * @param nodes
     * @return
     */
    public static Set<ProviderInfo> convertZkChildren(List<String> nodes) {

        Set<ProviderInfo> providerInfos = Sets.newHashSetWithExpectedSize(nodes.size());

        for (String node : nodes) {
            providerInfos.add(convertZkChild(node));
        }

        return providerInfos;
    }

    /**
     * 将单个子节点的某个元素转为为服务提供者信息
     * 
     * @param node
     * @return
     */
    public static ProviderInfo convertZkChild(String node) {

        ProviderInfo providerInfo = PROVIDER_INFO_CONCURRENT_HASH_MAP.get(node);

        if (providerInfo == null) {
            Map<String, String> providerMap = Splitter.on("&").withKeyValueSeparator("=").split(node);
            providerInfo = new ProviderInfo();
            providerInfo.setIp(providerMap.get("ip"));
            providerInfo.setPort(Integer.parseInt(providerMap.get("port")));
            providerInfo.setWeight(Integer.parseInt(providerMap.get("weight")));
            PROVIDER_INFO_CONCURRENT_HASH_MAP.putIfAbsent(node, providerInfo);
        }

        return providerInfo;
    }

}
