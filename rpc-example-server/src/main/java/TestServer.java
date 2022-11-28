import github.rpc.provider.ServiceProvider;
import github.rpc.server.BlogServiceImpl;
import github.rpc.server.NettyRpcServer;
import github.rpc.server.RpcServer;
import github.rpc.server.UserserviceImpl;

import java.util.Random;


public class TestServer {
    public static void main(String[] args) {
//        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfig.class);
//        UserserviceImpl userservice = (UserserviceImpl) applicationContext.getBean("userserviceImpl");
//        Map<String,Object> serviceProvider = new HashMap<String, Object>();
        // 服务端启动前设置好能够监听的服务，将服务接口的对象设置到map里去，方便后续客户端访问了之后可以取得对应的对象
//        serviceProvider.put("com.rpc.rpcVersion2.service.BlogService",new BlogServiceImpl());
//        serviceProvider.put("com.rpc.rpcVersion2.service.Userservice",new UserserviceImpl());
        // 服务端设置接口服务，不应该手动去设置key值，最好直接通过反射设置好
        // 服务端直接暴露自己的IP+端口号，在zookeeper上进行注册， 提供的服务名 ： 自己的IP+端口号
        Random random = new Random();
        int port = random.nextInt(10) + 8100;
        ServiceProvider serviceProvider = new ServiceProvider("127.0.0.1",port);
        serviceProvider.fillServiceProvider(new UserserviceImpl());
        serviceProvider.fillServiceProvider(new BlogServiceImpl());  // 通过反射设定serviceProvider这个map
        RpcServer rpcServer = new NettyRpcServer(serviceProvider.getServiceProvider(),port);  // netty版本改动2
        rpcServer.start();

//        RpcServer rpcServer = new SimpleRpcServer(serviceProvider);
//        rpcServer.start();
    }
}
