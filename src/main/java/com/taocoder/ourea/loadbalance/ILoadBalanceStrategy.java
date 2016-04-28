/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package com.taocoder.ourea.loadbalance;

import java.util.List;

import com.taocoder.ourea.model.Invocation;
import com.taocoder.ourea.model.InvokeConn;

/**
 * @author tao.ke Date: 16/3/3 Time: 下午2:33
 */
public interface ILoadBalanceStrategy {

    /**
     * 从众多连接池子中选择其中一个池子.
     * 
     * @param invokeConns
     * @return
     */
    public InvokeConn select(List<InvokeConn> invokeConns, Invocation invocation);
}
