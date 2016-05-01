/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package com.taocoder.ourea.core.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * 服务的地址和端口,权重暂时不使用.未来通过权重还设置负载均衡的比重
 * 
 * @author tao.ke Date: 16/3/3 Time: 下午2:12
 */
public class ProviderInfo implements Serializable {

    private static final long serialVersionUID = 733316025823163238L;

    private String ip;
    private int port;
    private int weight;

    /**
     * 是否对外提供服务,后台可以配置手动下线
     */
    private boolean status = true;

    public ProviderInfo() {
    }

    public ProviderInfo(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProviderInfo that = (ProviderInfo) o;

        return new EqualsBuilder().append(port, that.port).append(ip, that.ip).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(ip).append(port).toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
