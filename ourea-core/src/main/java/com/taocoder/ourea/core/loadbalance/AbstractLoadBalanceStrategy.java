/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package com.taocoder.ourea.core.loadbalance;

import java.util.List;

import com.taocoder.ourea.core.model.Invocation;
import org.apache.commons.collections4.CollectionUtils;

import com.taocoder.ourea.core.model.InvokeConn;

/**
 * @author tao.ke Date: 16/3/3 Time: 下午3:17
 */
public abstract class AbstractLoadBalanceStrategy implements ILoadBalanceStrategy {

    @Override
    public InvokeConn select(List<InvokeConn> invokeConns, Invocation invocation) {

        if (CollectionUtils.isEmpty(invokeConns)) {
            throw new IllegalStateException("no valid provider exist online.");
        }

        if (invokeConns.size() == 1) {
            return invokeConns.get(0);
        }

        return doSelect(invokeConns, invocation);
    }

    protected abstract InvokeConn doSelect(List<InvokeConn> invokeConns, Invocation invocation);
}
