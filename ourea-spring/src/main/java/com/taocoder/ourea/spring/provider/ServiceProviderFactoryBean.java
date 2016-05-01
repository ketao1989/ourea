/*
* Copyright (c) 2015 taocoder.com. All Rights Reserved.
*/
package com.taocoder.ourea.spring.provider;

import com.taocoder.ourea.core.common.PropertiesUtils;
import com.taocoder.ourea.core.config.ThriftServerConfig;
import com.taocoder.ourea.core.config.ZkConfig;
import com.taocoder.ourea.core.provider.ServiceProviderFactory;
import com.taocoder.ourea.spring.ConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.server.TServerEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;

import java.util.Properties;

/**
 * @author tao.ke Date: 16/5/1 Time: 下午2:17
 */
public class ServiceProviderFactoryBean
        implements FactoryBean<ServiceProviderFactory>, InitializingBean, ApplicationListener<ApplicationEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceProviderFactoryBean.class);

    /**
     * zk集群配置
     */
    private ZkConfig zkConfig;

    /**
     * thrift server 配置
     */
    private ThriftServerConfig serverConfig;

    /**
     * provider 配置
     */
    private Resource configLocation;

    /**
     * 对外暴露接口的实现对象
     */
    private Object ref;

    private ServiceProviderFactory serviceProviderFactory;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            serviceProviderFactory.exposeService(ref, zkConfig, serverConfig);
            LOGGER.info("expose thrift service class:{},port:{}.", ref.getClass(), serverConfig.getPort());
        }
    }

    @Override
    public ServiceProviderFactory getObject() throws Exception {
        if (this.serviceProviderFactory == null) {
            afterPropertiesSet();
        }
        return this.serviceProviderFactory;
    }

    @Override
    public Class<?> getObjectType() {
        return this.serviceProviderFactory == null ? ServiceProviderFactory.class
                : this.serviceProviderFactory.getClass();

    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (ref == null) {
            throw new IllegalArgumentException("build consumer bean fail. clazz is null.");
        }

        if (configLocation != null && (zkConfig == null || serverConfig == null)) {
            Properties properties = PropertiesUtils.load(configLocation.getInputStream());
            if (zkConfig == null) {
                buildZkConfig(properties);
            }
            if (serverConfig == null) {
                buildServerConfig(properties);
            }
        }

        serviceProviderFactory = new ServiceProviderFactory();
    }

    private void buildServerConfig(Properties properties) {

        ConfigUtils.assertEmpty("port", properties.getProperty("port"));
        serverConfig = new ThriftServerConfig(Integer.parseInt(properties.getProperty("port")));

        if (StringUtils.isNoneEmpty(properties.getProperty("group"))) {
            serverConfig.setGroup(properties.getProperty("group"));
        }
        if (StringUtils.isNoneEmpty(properties.getProperty("version"))) {
            serverConfig.setVersion(properties.getProperty("version"));
        }
        if (StringUtils.isNoneEmpty(properties.getProperty("MinWorkerThreads"))) {
            serverConfig.setMinWorkerThreads(Integer.parseInt(properties.getProperty("MinWorkerThreads")));
        }
        if (StringUtils.isNoneEmpty(properties.getProperty("MaxWorkerThreads"))) {
            serverConfig.setMaxWorkerThreads(Integer.parseInt(properties.getProperty("MaxWorkerThreads")));
        }
        if (StringUtils.isNoneEmpty(properties.getProperty("weight"))) {
            serverConfig.setWeight(Integer.parseInt(properties.getProperty("weight")));
        }
        if (StringUtils.isNoneEmpty(properties.getProperty("status"))) {
            serverConfig.setStatus(Boolean.parseBoolean(properties.getProperty("status")));
        }
        if (StringUtils.isNoneEmpty(properties.getProperty("directInvoke"))) {
            serverConfig.setDirectInvoke(Boolean.parseBoolean(properties.getProperty("directInvoke")));
        }
        if (StringUtils.isNoneEmpty(properties.getProperty("daemonRun"))) {
            serverConfig.setDaemonRun(Boolean.parseBoolean(properties.getProperty("daemonRun")));
        }

        if (StringUtils.isNoneEmpty(properties.getProperty("serverEventHandler"))) {

            try {
                Class<TServerEventHandler> handlerClass = (Class<TServerEventHandler>) Class
                        .forName(properties.getProperty("serverEventHandler"));
                serverConfig.setServerEventHandler(handlerClass.newInstance());
            } catch (Exception e) {
                LOGGER.warn(
                        "config full loadBalanceStrategy class not find.use default RoundRobinLoadBalanceStrategy.");
            }
        }
    }

    private void buildZkConfig(Properties properties) {
        zkConfig = ConfigUtils.buildConfig(properties);
    }

    public void setZkConfig(ZkConfig zkConfig) {
        this.zkConfig = zkConfig;
    }

    public void setServerConfig(ThriftServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public void setConfigLocation(Resource configLocation) {
        this.configLocation = configLocation;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }
}
