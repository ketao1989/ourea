/*
* Copyright (c) 2015 taocoder.com. All Rights Reserved.
*/
package com.taocoder.ourea;

import com.taocoder.ourea.server.ServiceProviderBuilder;

/**
 * @author tao.ke Date: 16/4/25 Time: 下午2:27
 */
public class ZkThriftServerSample {

  public static void main(String[] args) {
    ServiceProviderBuilder builder = ServiceProviderBuilder.newBuilder(Ourea.class, new OureaImpl(), 9999,"10.10.33.134:2181");
    builder.build().start();
  }

}
