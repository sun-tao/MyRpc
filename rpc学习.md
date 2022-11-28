# RPC学习

## version2

- request和response的封装(定义通用的消息格式)

- 服务端通过解析`rpcRequest`获取想要调用的服务方法，当然，不能直接调用，因为没有对象，需要在服务端通过反射调用该方法。

  这里没有用到`interfaceName`这个参数，直接指定了从`userservice`接口去调用方法,后续要对此进行优化。
  
  用反射来获取userservice的不同方法，方法层面有了一定的灵活性，但接口层面仍然是固定了的。
  
-  ~~class\<?> 类型是什么，class是类类型，\<?>是通配符，所以class<?>可以指向任何类型的类类型。~~

- ~~Serializable和网络通信的关系~~

- 客户端使用动态代理来增强方法，客户端可以用这种方式来请求不同的接口服务。

  - 动态代理的实现步骤：
  
    动态代理代理的是接口，首先需要有接口的实现类，然后需要实现一个类，该类需要实现`InvocationHandler`接口，并重写它的`invoke`方法，在该方法中对要代理的接口的方法做增强。增强的时候需要有一个被代理的接口对象才可以实现`method.invoke`。
  
    有了该`InvocationHandler`接口的实现类后，即可调用`Proxy.newProxyInstance`来生成被代理接口的代理类了。然后用代理类执行接口的方法，即可走`method.invoke`的增强逻辑。
  
  - 增加了客户端的动态代理之后，客户端就可以像访问本地的方法一样，去访问远程主机上的方法了。
  
    原本未加动态代理时，访问远程的方法需要客户端手动设定`request`对象的各个参数,以便服务端收到了request对象之后在本地进行解析，来调用指定的方法。
  
    ```java
     request.setInterfaceName("userservice");  // 暂时没用
    // 这边固定的访问userservice的单一方法，进行测试
    request.setMethodName("insertUser");
    request.setParamsType(new Class[]{int.class});
    request.setParams(new Object[]{6});
    ```
  
    加入动态代理之后，客户端的调用就可以像调用本地的方法一样，来实现对远程方法的调用了。
  
    ```java
    Userservice proxy = (Userservice) Proxy.newProxyInstance(Userservice.class.getClassLoader(), new Class[]{Userservice.class}, new UserInvocationHandler(userservice));
    int userById = proxy.insertUser(5);
    ```
  
    当然本地最好有同名方法，不然会报错，不过该客户端本地该方法的逻辑完全不重要，因为在动态代理的`invoke`增强方法中并不会去真的执行本地的同名方法，而是在此进行网络通信，向服务端发送`rpcRequest`并接受`rpcResponse`，由于`rpcResponse`中封装了服务端返回的数据，因此可以将这部分数据进行返回，这就是动态代理对象调用本地同名方法的最终返回值，该返回值来自远程服务端。于是，对客户端而言，好像就在本地调用了一个方法一样，得到了返回值，其实底层通过动态代理技术和网络通信调用了服务端的方法，实现了远程调用。
  
## version3

-  改进：

  - 服务端能够提供多种服务，不仅局限于单一的`userService`

  - 服务端代码重构

    **使用线程池来优化多线程的写法**，同时将线程的通信任务和server的监听任务解耦。

    **Java线程池：**

    **线程池中的线程处理完了任务会怎么样？**
  
    内部使用worker来具体的处理任务，worker中封装了Thread线程，worker在执行完其run方法后，会被调用workers.remove方法从workers中移出，也就是说，线程池中的线程处理完任务后，就会被移出workers这个Hashset，如果线程池中的worker过少，则会对通过addworker其进行补偿，再加新的worker进入workers。
  
    从根本上来看，无论核心还是非核心线程都表现为worker，进一步说，是Thread类，无本质区别，区别核心和非核心的，只是对他们的创建和销毁时机方式，通过特定的方式，让线程池在平稳条件下的线程数维持在核心线程数附件，当并发量高时，就多创建所谓的非核心线程，增加线程池中的线程数，当并发量下去之后，自然地删去那些增加的非核心线程数，保持线程池中的线程数量几乎维持在核心线程数。所以，在我看来，核心线程数更像是一种分配策略，让线程池中的线程数维持在该数量，可能对系统的资源占用，线程的上下文开销都有好处。
  
    ```java
    // 1.工作线程小于核心线程数时  创建worker并执行任务
    // 2.工作线程大于核心线程数时，尝试将当前任务加到阻塞队列中
    //   1. 加入成功，ok，等待worker run的时候从阻塞队列中取就行了
    //   2. 加入失败，大概率是因为阻塞队列满了
    //      此时尝试创建非核心线程的worker
    //      3.1 创建成功，直接执行就可以了
    //      3.2 创建失败，说明总的线程数已经超过maximumPoolSize了，则执行拒绝策略
    ```
  
    
  
    ```java
    // 来了一个任务，首先看核心线程，如果线程池中运行的线程数少于核心线程数，则直接新起线程来执行任务，无论是否有空闲线程
    // 如果线程池的核心线程数满了，则将任务添加到阻塞队列，等待核心线程空闲出来了，再从中取出来执行
    // 如果阻塞队列也满了，但当前线程池中的线程数还没有超过 maximumPoolSize，那就直接新起非核心线程来执行
    // 如果连maximumPoolSize都超过了，那就执行拒绝策略，拒绝策略共有4种：
    // 1.抛异常
    // 2.不新起线程，用当前的线程执行任务
    // 3.抛弃阻塞队列最老的任务，用线程池的线程执行当前任务
    // 4.直接丢弃
    ```
  
    java提供的`ThreadPoolExecutor`线程池使用，参数如何选择等。 线程池的原理。
  
    七大参数、四种初始化方法：	
  
    ```java
    // 四种初始化方法是对七大参数给予特定的值，来构成的特殊的线程池
    // 1.Executors.newCachedThreadPool
    // coreSize = 0,maximumPoolSize = MAX,阻塞队列为SynchronousQueue，不存任何元素
    // 因此所有任务提交了都会创建非核心线程来执行，可能会导致OOM
    // 2.Executors.newSingleThreadExecutor
    // coreSize = 1,maximumPoolSize = 1,阻塞队列为LinkedBlockingQueue，长度为无限长
    // 因此所有任务都会存入阻塞队列，容易OOM
    // 3.Executors.newFixedThreadPool
    // 同理，阻塞队列的长度为无限长
    // 4.Executors.newScheduledThreadPool
    // maximumPoolSize = MAX,可以创建无限多的非核心线程，容易OOM
    // 特别的，它的阻塞队列使用DelayedWorkQueue，可以实现定时和周期任务。
    // 综上，因为这四个现成的类都有无限长的参数设置，可能导致OOM，因此不推荐使用他们
    // 推荐自己创建ThreadPoolExecutor并设定参数
    ```
  
  - 通过反射去将服务对象初始化到`serviceProvider`中

## version4

**NIO是什么？Netty是如何实现NIO的。**

- 升级客户端部分，可以通过制定不同的RpcClient的实现类，来选择不同的通信方式，SimpleRpcClient为BIO的通信方式，NettyRpcClient为NIO的通信方式。

- **使用Netty网络框架来实现NIO的通信**

  - 通过官方文档熟悉了Netty框架的服务端和客户端的启动，基础的基于netty的C/S架构通信于本机实现。
  - 后续使用Netty来优化RPC的网络通信部分，借用其**NIO**来提高网络通信效率。

- **值得深挖、思考的点：**

  **NIO是什么？Netty是如何实现NIO的。**

  ~~TCP粘包、拆包问题，最好能动手实验观察一下。~~

  **handler的执行顺序问题**

  对于输入的数据，handler的执行顺序和注册顺序一致，即先in1后in2，对于输出的数据，handler的执行顺序和注册顺序相反，即先out1后out2。

  一般来说`注册outhandler的时候，必须放到最后一个inhandler前面`。

- ```java
  // 这里将 FixedLengthFrameDecoder 添加到 pipeline 中，指定长度为20
  ch.pipeline().addLast(new FixedLengthFrameDecoder(20));    // in1
  // 将前一步解码得到的数据转码为字符串  
  ch.pipeline().addLast(new StringDecoder());     // in2
  // 这里FixedLengthFrameEncoder是我们自定义的，用于将长度不足20的消息补全空格
  //  假设这里需要对输出做进一步处理  ...  // out2
  ch.pipeline().addLast(new FixedLengthFrameEncoder(20));  // out1
  // 最终的数据处理
  ch.pipeline().addLast(new EchoServerHandler());  4   // in3
  
  ```
  
- Netty编解码器：

  netty的编码器，在执行`channelActive`后会自动将客户端输出的数据通过编码器进行编码的，而我的测试过程中，一开始客户端的在`channelActive`中写到通道里的数据总是不经过编码器直接发给服务器，经过仔细查看，发现这是因为对定义的编码器的`encode`函数的参数不熟悉，该函数有多种重载，对应了不同的输入数据。传递给该编码器的数据类型，必须要符合自己去重写的那个`encode`函数所需要的输入类型。否则将不会走该编码器进行编码。

  同时，一开始在客户端编码成功后，但服务端就收不到该编码后的数据，再仔细查看发现，服务端使用的`FixedLengthFrameDecoder`解码器，它所需要的是netty的`ByteBuf`类型数据。

  ```java
   protected void encode(ChannelHandlerContext channelHandlerContext, String msg, ByteBuf byteBuf) throws Exception    // version1
   protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception // version2
  ```

- Netty客户端调用了sendRequest之后需要等待服务端返回response,这边应该要阻塞在此，等待返回，但Netty是非阻塞的，所以没办法同步，这里需要特殊处理。

- Java的序列化冲突问题,在服务端的解码过程中，即将bytebuf解码成RpcResquest对象的时候，出现问题。这与Java对象的序列化方式有关。客户端发来的的Java对象默认采用jdk的序列化方式，服务端也应该直接使用jdk默认反序列化方式即可实现解码，将字节流转为Java对象。**(这块涉及到Netty的发送与接收的序列化与反序列化的知识，还需要进一步学习)**

  Jdk默认的序列化方式，序列化出来的字节流包含了整个类的信息！所以非常复杂，性能比较差。
  
## Version5

**既然是编解码，为什么Netty要考虑编解码的问题，而普通的socket通信却不需要考虑？**

- 自定义编解码格式和方法,在编解码的同时，也可以扩展多种序列化的方法，如json等格式的序列化方法，相比较于上一版本的直接使用Java原生的序列化和反序列化方法，极大的提高了效率和性能。

- Netty本身的`ObjectDecoder `和`ObjectEncoder `使用的就是Java本身的序列化技术(ObjectOutputStream、ObjectInputStream），性能低，体积大，无法跨语言。

  因此，本版本使用其他的高效的序列化技术，如`Protobuf`和`Json`等，来实现网络传输前后的序列化和反序列化过程。

  - Protobuf：

    成功使用Google的Protobuf序列化工具，制作了基于Protobuf的RpcRequest类，其中2个参数与common的RpcRequest一致，有不足的就是，common的RpcRequest里面的参数类型和参数值的类型是Class数组以及Object数组，**目前了解的Protobuf生成的java类没有与这两个类型对应的类**，所以只能使用Protobuf中的`bytestring`类型来表示这两个类型。当然，这样就导致这两个类型在Protobuf的RpcRequest中是以字节的形式存储的，与common的RpcRequest不一致，因此还是需要使用Java原生的序列化方式来对其做 对象->字节流以及字节流->对象的转换。(对象是Class数组以及Object数组)。
    
    这一版本的Protobuf做序列化的编解码仍然存在着很大的不足，就是除了简单的基本类型可以直接使用Protobuf对象的类型来做对应外，对于其他RpcReqeust和RpcResponse中拥有的复杂的类型，没有找到可以与之对应的对象，统统采用了`bytestring`来对应，这样在实际编解码的时候，仍然无法避免使用java原生的序列化方式对其做编解码的预(后)处理。
    
    **后续如果要进一步研究Protobuf，可以对这一问题做优化。**

## Version6

- 注册中心：微服务中，如果需要多台主机一同提供RPC服务，那客户端每次调用的时候还需要手动的指定不同的主机的IP和端口号，非常不便。因此，本版本使用zookeeper来提供服务的注册功能和发现功能。

  RPC服务端启动的时候，使用curator(zookeeper客户端)来连接zookeeper服务器(此时为本机的zookeeper server进程),并在zookeeper上注册当前主机提供的所有服务(通过zookeeper znode的形式 "/接口名/本机ip+端口号")。

  RPC客户端启动的时候，同样先使用curator连接zookeeper服务器，在发送RPCRequest请求的时候，先通过rpcRequest请求中的接口名来从zookeeper中获取服务提供者的IP地址和端口号，再进行后续的网络通信操作。

  原先rpc客户端原本需要手动指定服务提供者，使用zookeeper可以通过统一的注册中心来获取服务提供者，客户端不必预先知道各个服务提供者的ip和端口号，而且服务提供者如果端口或ip发生了变动，也会直接在zookeeper中修改，客户端是不会感知到的，也不需要做相应的修改。
  
- 客户端选择服务端主机的时候加入**随机负载均衡算法**的。

## RPC优化：

1. 集成Spring框架，通过注解注册服务，通过注解消费服务

   使用说明：在服务端，对想要注册的服务实现类加`@RpcService`注解，在客户端，对Controller的Service成员变量加`@RpcReference`注解
   
   - [√] 通过注解来注册服务，核心思想就是用Spring IOC来管理服务端提供的接口(服务)实现类对象，通过在Spring的Bean的生命周期中穿插执行自定义的逻辑，比如:给定某个接口的实现类`@RPCService`注解，在Spring实例化/初始化该实现类对象的时候，通过`BeanPostProcessor`在其创建Bean的过程中，通过注解@RPCService找到需要执行额外逻辑的类，让其执行本机注册和Zookeeper上注册的逻辑。也就是说，某个服务如要注册，只需要再其前面加上`@RPCService`注解即可。省去了手动调用注册方法来注册服务。  
   - [√] 通过注解来消费服务，若不采用注解的方法，客户端每次执行，需要**手动调用函数**，获取HelloService的代理类对象，使用该代理类对象才可以进行RPC通信。而采用注解来进行的话，则可以由Spring自动注入HelloController的HelloService对象，注入的内容即为它的**代理类对象**。其核心思想仍然是，在Spring为HelloController创建Bean的生命周期中，扫描出其带有自定义`@RPCReference`注解的HelloService对象，将其用规定好的代理类来注入，这样，就可以避免客户端手动调用代理类相关的API，直接完成动态代理。
   
   客户使用本框架将十分方便。只需通过`@RPCService`和`@RPCReference`即可。
   
   ServiceProvider的注册服务，需要知道具体的服务，才能够进行注册，因此不能够直接用Spring来自动注入ServiceProvider，需要使用`BeanPostProcessor`，在初始化实现类对象的时候，会拿到这个`Bean`，在此时即可实现注册服务。
   
   



## Guide的RPC项目值得思考的点：

1. 设计模式：单例模式，工厂模式。  需要会写获取单例对象的工厂类，在serviceProvider处使用到了。

2. Netty的心跳机制

3. Java对象的`serialVersionUID`值的含义

4. ConcurrentHashMap

5. Spring bean的生命周期

   <img src="https://img2018.cnblogs.com/blog/1066538/201909/1066538-20190902000437259-1068766043.png" alt="img" style="zoom:67%;" />



