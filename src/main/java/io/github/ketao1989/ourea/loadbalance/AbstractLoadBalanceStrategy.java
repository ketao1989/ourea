/*
* Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
*/
package io.github.ketao1989.ourea.loadbalance;

import io.github.ketao1989.ourea.model.Invocation;
import io.github.ketao1989.ourea.model.InvokeConn;
import org.apache.commons.collections4.CollectionUtils;

import java.net.URL;
import java.util.List;

/**
 * @author tao.ke Date: 16/3/3 Time: 下午3:17
 */
public abstract class AbstractLoadBalanceStrategy implements ILoadBalanceStrategy {

    @Override
    public InvokeConn select(List<InvokeConn> invokeConns, URL url,Invocation invocation) {
        if (CollectionUtils.isEmpty(invokeConns)){
            return null;
        }

        if (invokeConns.size() == 1){
            return invokeConns.get(0);
        }

        return doSelect(invokeConns, url,invocation);
    }

    protected abstract InvokeConn doSelect(List<InvokeConn> invokeConns, URL url,Invocation invocation);
}
