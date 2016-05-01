/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea.core.consumer;

import com.taocoder.ourea.core.common.OureaConnCreateException;
import com.taocoder.ourea.core.common.OureaException;
import com.taocoder.ourea.core.model.ProviderInfo;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tao.ke Date: 16/4/25 Time: 下午4:46
 */
public class ConsumerPoolFactory implements PooledObjectFactory<TTransport> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerPoolFactory.class);

    /**
     * 服务提供者的信息
     */
    private ProviderInfo providerInfo;

    /**
     * 调用服务的超时时间
     */
    private int timeout;

    public ConsumerPoolFactory(ProviderInfo providerInfo, int timeout) {
        this.providerInfo = providerInfo;
        this.timeout = timeout;
    }

    /**
     * 这个地方创建连接的时候,如果创建失败,重试一次,还不行则抛出异常,把该节点暂时移除
     * 
     * @return
     * @throws Exception
     */
    @Override
    public PooledObject<TTransport> makeObject() throws Exception {
        TTransport transport = null;
        Exception ex = null;
        int retryConn = 1;
        do {
            try {
                transport = new TSocket(providerInfo.getIp(), providerInfo.getPort(), timeout);
                transport.open();
                ((TSocket) transport).setTimeout(timeout);
                return new DefaultPooledObject<TTransport>(transport);
            } catch (Exception e) {
                ex = e;
            }
        } while (retryConn-- > 0);
        LOGGER.error("make transport object fail.e:", ex);
        throw new OureaConnCreateException("make transport object fail." + ex.getMessage());
    }

    @Override
    public void destroyObject(PooledObject<TTransport> p) throws Exception {

        try {
            TTransport transport = p.getObject();
            if (transport.isOpen()) {
                transport.close();
            }
        } catch (Exception e) {
            LOGGER.error("destroy transport object fail.maybe exist memory leek.e:", e);
            throw new OureaException("destroy transport object fail" + e.getMessage());
        }

    }

    @Override
    public boolean validateObject(PooledObject<TTransport> p) {
        if (p.getObject().isOpen()) {
            return true;
        }
        return false;
    }

    @Override
    public void activateObject(PooledObject<TTransport> p) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<TTransport> p) throws Exception {

    }
}
