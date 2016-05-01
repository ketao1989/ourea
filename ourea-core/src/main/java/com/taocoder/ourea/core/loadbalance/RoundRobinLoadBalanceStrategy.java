/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package com.taocoder.ourea.core.loadbalance;

import com.taocoder.ourea.core.model.Invocation;
import com.taocoder.ourea.core.model.InvokeConn;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据方法级别进行轮询调用.不考虑权重
 * 
 * @author tao.ke Date: 16/3/3 Time: 下午3:39
 */
public class RoundRobinLoadBalanceStrategy extends AbstractLoadBalanceStrategy {

    private static final ConcurrentHashMap<String, Integer> current = new ConcurrentHashMap<String, Integer>();

    @Override
    protected InvokeConn doSelect(List<InvokeConn> invokeConns, Invocation invocation) {

        String key = invocation.getInterfaceName() + invocation.getMethodName();

        Integer cur = current.get(key);

        if (cur == null || cur >= Integer.MAX_VALUE - 1) {
            cur = 0;
        }

        current.putIfAbsent(key, cur + 1);

        return invokeConns.get(cur % invokeConns.size());

    }
}
