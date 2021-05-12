# Ourea-基于Zookeeper的Thrift服务发现框架实现

## 目录

1. [框架背景](#Introduction)
2. [Thrift服务原生使用](#Thrift_Native_Demo)
3. [Ourea服务发现实现原理](#Ourea_Implement)
4. [Ourea服务发现框架使用示例](#Ourea_Demo)
5. [总结](#Summary)

## <a id="Introduction">1 框架背景</a>

 Apache Thrift 是Facebook 实现的一种高效的、支持多种编程语言的远程服务调用的框架。在多语言并行于业务之中的公司，其是一个很好的RPC框架选择，但是由于缺少服务发现管理功能，在使用的时候，需要告知业务方现有业务部署的地址，并且调用方需要自己实现服务状态的感知和重试机制。此外，对于互联网公司而言，业务快速变化必然导致机器的增减，这些变化，需要通知到所有调用方来更改调用机器的配置，是非常麻烦的。

 显然，对于Thrift来说，一个服务发现管理框架是多么的重要。

 那么，服务发现管理框架其实可以做的很重，也可以做的很轻；对于我们，需要满足什么需求：

 * 服务调用方自动获取服务提供方地址；
 * 服务提供方服务分组；
 * 服务调用方负载均衡策略；
 * 服务非兼容升级；

具体的需求分析和实现，将在 [Ourea服务发现实现原理](#Ourea_Implement)介绍。

<!--more-->

## <a id="Thrift_Native_Demo">2 Thrift服务原生使用</a>

Thrift 接口使用还是比较简单地，对外提供的server和client接口封装了所有的内部实现细节，所以，一般我们只需要告诉`Thrift`地址端口信息，然后就可以完成简单地RPC调用。

下面，给出一个简单地示例：

```java

// 服务端示例
public class SimpleThriftServer {
    private static final Logger logger = LoggerFactory.getLogger(SimpleThriftServer.class);
    private static final int port = 9999;

    public  void simple(int port){

        try {
            TServerSocket tServerSocket = new TServerSocket(port);
            Hello.Processor processor = new Hello.Processor(new HelloService());

            TServer server = new TSimpleServer(new TServer.Args(tServerSocket).processor(processor) );
            server.serve();

        }catch (Exception e){
            logger.error("server start error........",e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SimpleThriftServer server = new SimpleThriftServer();
        server.simple(port);
    }
}

// client 端示例
public class SimpleThriftClient {

    private static final Logger logger = LoggerFactory.getLogger(SimpleThriftClient.class);
    private static final int port = 9999;
    private static final String ip = "127.0.0.1";

    public static void main() {
        TTransport transport = null;
        try {
            transport = new TSocket(ip, port);
            TProtocol protocol = new TBinaryProtocol(transport);
            Hello.Client client = new Hello.Client(protocol);
            transport.open();
            HelloResult result = client.sayHello("hello world");
        } catch (Exception e) {
            logger.error("client invoke fail. ", e);
        } finally {
            if (transport != null) {
                transport.close();
            }
        }
    }
}
```

>> Note:
>> * TProtocol 协议和编解码组件
>> * TTransport 传输通信组件
>> * TProcessor 服务处理相关组件
>> * TServer 服务提供组件
>> * Client 服务调用客户端

Thrift 原生的对外接口已经很简单了，但是为什么还需要去封装呢？上文的代码虽然简单，但是有几个点需要去注意：

1. 对于生产环境的服务，在发布新功能，出现故障down机，都会导致服务出现不可用的情况；此外，对外的服务一般都是集群部署，集群机器的增减也是很可能会出现的事情，因此，就会出现最初对外提供的服务IP地址会出现新增(新建服务)，减少(缩减服务)，暂时停服(机器故障)，这些所有变更通知所有业务服务调用方去更改是很难处理的事情。此外，由于服务可能存在大量的机器列表，这些配置在业务代码中，本身也是不可取的。
2. 服务调用的时候，可能存在某些服务当时负载过高，或者服务网络问题等导致服务调用策略需要调整。也就是在选择调用集群中某台机器的时候，每个业务都要自己去实现策略，这是不可取的。此外，对于服务的负载情况无法感知，即使是静态的服务提供权重都无法获取，导致了即使客户端自己实现均衡策略，由于缺少必要的数据支持，导致只能采用轮询和随机。
3. 业务上，服务调用之间隔离，服务接口的灰度升级等，是比较常见的技术需求。Thrift 对外发布的服务的所有IP，对于调用方来说都是平等的，也就是，如果我需要将集群中某些机器进行接口的非兼容的灰度升级，或者某些机器独立出来给一些非常重要的业务使用。目前，这种场景，只能新加机器来解决了。
4. 对于调用方Client的调用，每次都需要去创建连接，然后和server端交互，对于大请求场景下的应用，对性能的影响是很大的。创建connection对象，是很重的，需要进行池化。
5. ......

基于以上的一些原因，开发了基于`Zookeeper`的Thrift服务发现机制框架。

## <a id="Ourea_Implement">3 Ourea服务发现实现原理</a>

服务发现机制，在很多RPC套件中都会提供。`Zookeeper`一直作为经典实现服务发现机制的底层服务，所以这里显然也是这样的。下面给出，实现的模块交互图：

<img src="http://ketao1989.github.io/images/2016/04/ourea.png" />

### 3.1 交互说明

在设计交互图中，`Server`服务提供方会在启动的时候，读取服务相关配置属性，比如版本号、组名等信息，以及注册`Zookeeper`地址及相关zk操作配置信息；然后根据配置去指定`Zookeeper`集群注册服务，其路径为``` /ourea/service_name_xxx/group_yyy/version_zzz/provider/ip=mmm&port=nnn&weight=lll ```,注册完成之后，就可以启动Thrift服务了；如果发现服务启动失败，则去`Zookeeper`上注销该路径节点。

服务调用方启动的时候，同样也会读取相关配置，然后去`Zookeeper`上注册服务调用信息，注册完了之后，服务调用方会去查询``` /ourea/service_name_xxx/group_yyy/version_zzz/provider/```下地子节点列表，并且注册监听逻辑，等待子节点变更则触发重新查询操作。服务调用方拿到ip列表之后，就可以按照选择的选择策略来拿到指定服务器的连接去调用相关服务接口。

需要说明的是，由于服务调用方拿到服务提供方的地址列表后，其只会监听`Zookeeper`的变更通知，调用方是直接和服务方交互的，因此，当`Zookeeper`出现不可服务时，并不会影响Thrift的服务提供和调用交互。因此，也不会影响Thrift的性能。

### 3.2 针对Thrift原生接口的解决方案

1. 服务扩张自动感知
当服务提供方的服务状态发生变更，比如新加机器扩展，或者服务发布上下线等，会自动在指定路径创建一个临时节点，节点包含机器相关信息，利用`Zookeeper`的临时节点特性来实现服务状态自动感知功能。

2. 服务调用负载策略
目前，ourea 也只支持方法级轮询策略和权重随机分布策略。服务会记录某个方法上次调用情况，然后从服务list中找出这次选择的机器进行服务调用。权重随机分布，是根据服务提供方在服务启动的时候，配置的机器权重来计算，权重大的服务机器，被选择的几率大于权重小的服务提供者。

3. 服务提供粒度细化 
服务隔离保证重要业务调用方不被其他业务干扰；此外，由于前期设计的问题，需要对部分接口进行非兼容升级(虽然我们非常不提倡这种行为)，这些问题都需要对服务提供进行细化隔离。因此，在ourea中，增加了`group`和`version`的控制。在server端，初始都是为默认的group提供服务，当存在某个重要的业务需要抗干扰服务级别时，下面一些机器，配置为特定group给改业务调用，这时，其他业务是无法自动获取到这些服务提供者地址的，从而到达服务隔离的目的。

4. Thrift连接池化
java client端，会对获取到得服务提供者地址列表分别创建对应的socket连接池，保证经过策略选择到指定机器后，可以直接和服务端交互。

## <a id="Ourea_Demo">4 Ourea服务发现框架使用示例</a>

使用ourea框架非常简单，生成jar包，引入到项目，然后如下调用方法即可完成Thrift交互。

```java

public class ZkThriftServerSample {

    public static void main(String[] args) {

        System.out.println(Ourea.Processor.class.getCanonicalName());

        Properties properties = PropertiesUtils.load("provider.properties");

        ThriftServerConfig config = new ThriftServerConfig(Integer.valueOf(properties.getProperty("port")));
        config.setGroup(properties.getProperty("group"));
        ServiceProviderFactory.exposeService(new OureaImpl(), new ZkConfig(properties.getProperty("zkAddress")),
                config);
    }
}

public class ZkThriftClientSample {

  public static void main(String[] args) throws Exception {

    Properties properties = PropertiesUtils.load("consumer.properties");

    Ourea.Iface client = ConsumerProxyFactory.getProxyClient(Ourea.Iface.class,
        new ZkConfig(properties.getProperty("zkAddress")));
    int count =0;
    long start = System.currentTimeMillis();
    while (count++ <1000){
      System.out.println(count + "-----" + client.queryEcho("hello"));
    }
    System.out.println(System.currentTimeMillis() - start);
  }

}
```

服务最少配置如下：

consumer.properties配置:
```java
#zookeeper注册地址
zkAddress=10.10.33.134:2181
#zookeeper超时时间
zkTimeout=3000
```
provider.properties配置:
```java
#对外提供服务端口号
port=9999
#zookeeper注册地址
zkAddress=10.10.33.134:2181
#zookeeper超时时间
zkTimeout=3000
#服务细分的组/topic
group=bbb
```

## <a id="Summary">5 总结</a>

* Thrift 对外提供同步和异步两种接口，但是在实践中基本都是使用同步，所以本框架是基于同步接口开发的；
* 服务提供者信息的采集，目前暂时未实现。所以，负载均衡策略暂不支持根据服务响应来调整(响应时间负载优化，CPU优化，MM GC 优化等)；
* 目前只支持Java语言的开发，其他语言不是太熟悉，暂无开发计划。
* ~~未来将提供和Spring框架配置集成功能，通过配置直接对外提供服务或者调用服务。~~(已支持)

项目源码开源地址：https://github.com/ketao1989/ourea
