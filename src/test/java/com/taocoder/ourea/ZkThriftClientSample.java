/*
* Copyright (c) 2015 taocoder.com. All Rights Reserved.
*/
package com.taocoder.ourea;

import com.taocoder.ourea.client.ConsumerProxyFactory;

/**
 * @author tao.ke Date: 16/4/27 Time: 下午5:03
 */
public class ZkThriftClientSample {

  public static void main(String[] args) throws Exception {

    Ourea.Iface client = ConsumerProxyFactory.getProxyClient(Ourea.Iface.class);
    System.out.println(client.queryEcho("hello"));
  }

}
