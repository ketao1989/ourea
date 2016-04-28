/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package com.taocoder.ourea.loadbalance;

import java.util.List;

import org.apache.commons.lang3.RandomUtils;

import com.taocoder.ourea.model.Invocation;
import com.taocoder.ourea.model.InvokeConn;

/**
 * @author tao.ke Date: 16/3/3 Time: 下午3:16
 */
public class RandomLoadBalanceStrategy extends AbstractLoadBalanceStrategy {

    @Override
    protected InvokeConn doSelect(List<InvokeConn> invokeConns, Invocation invocation) {

        boolean sameWeight = true;
        int totalWeight = 0;

        // 找到权重
        int preWeight = invokeConns.get(0).getProviderInfo().getWeight();
        for (InvokeConn invokeConn : invokeConns) {
            totalWeight += invokeConn.getProviderInfo().getWeight();
            if (preWeight != invokeConn.getProviderInfo().getWeight()) {
                sameWeight = false;
            }
        }

        if (totalWeight > 0 && !sameWeight) {
            // 如果权重不相同且权重大于0则按总权重数随机
            int offset = RandomUtils.nextInt(0, totalWeight);
            // 并确定随机值落在哪个片断上
            for (InvokeConn invokeConn : invokeConns) {
                offset -= invokeConn.getProviderInfo().getWeight();
                if (offset < 0) {
                    return invokeConn;
                }
            }
        }

        return invokeConns.get(RandomUtils.nextInt(0, invokeConns.size()));
    }
}
