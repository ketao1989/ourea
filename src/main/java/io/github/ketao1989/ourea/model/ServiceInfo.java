/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package io.github.ketao1989.ourea.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * 服务信息.id+interfaceName+version+group 唯一确定一个调用服务
 * 
 * @author tao.ke Date: 16/3/3 Time: 下午2:26
 */
public class ServiceInfo implements Serializable {

    private static final long serialVersionUID = -7572772909108866010L;

    /**
     * 服务接口名,全路径.
     */
    private String interfaceName;

    /**
     * 服务版本号
     */
    private String version;

    /**
     * 服务集群组名,默认为ourea,如果服务部署两套环境,推荐设置该参数
     */
    private String group;

    /**
     * 服务状态,0表示不可用,1表示正常
     */
    private int status;

    /**
     * 表示服务是否只是直接调用.true表示直接调用,false表示会额外注册到zk上.
     */
    private boolean directInvoke;

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isDirectInvoke() {
        return directInvoke;
    }

    public void setDirectInvoke(boolean directInvoke) {
        this.directInvoke = directInvoke;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
