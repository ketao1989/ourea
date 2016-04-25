/*
 * Copyright (c) 2015 ketao1989.github.com. All Rights Reserved.
 */
package com.taocoder.ourea.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * 调用方法相关属性,主要用来做负载均衡时,针对方法级别的RR
 *
 * @author tao.ke Date: 16/3/3 Time: 下午3:40
 */
public class Invocation implements Serializable {

    private static final long serialVersionUID = 3518301260123528398L;

    private String interfaceName;

    private String methodName;

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Invocation that = (Invocation) o;

        return new EqualsBuilder().append(interfaceName, that.interfaceName).append(methodName, that.methodName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(interfaceName).append(methodName).toHashCode();
    }
}
