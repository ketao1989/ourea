/*
* Copyright (c) 2015 taocoder.com. All Rights Reserved.
*/
package com.taocoder.ourea;

import com.taocoder.ourea.server.ZkServiceProvider;

/**
 * @author tao.ke Date: 16/4/25 Time: 下午2:27
 */
public class ZkThriftServerSample {

  public static void main(String[] args) {

    System.out.println(Ourea.Processor.class.getCanonicalName());

    ZkServiceProvider provider = new ZkServiceProvider(new OureaImpl(),false,false);
    provider.start();
  }

}
