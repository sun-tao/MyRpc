import com.sun.org.apache.xerces.internal.dom.PSVIDOMImplementationImpl;
import github.rpc.common.SingletonFactory;
import github.rpc.config.CustomShutdownHook;
import github.rpc.config.SpringConfig;
import github.rpc.extension.ExtensionLoader;
import github.rpc.provider.ServiceProvider;
import github.rpc.registry.zk.ZkServiceRegister;
import github.rpc.server.NettyRpcServer;
import github.rpc.server.RpcServer;
import github.rpc.server.SimpleRpcServer;
import github.rpc.serviceImpl.BlogServiceImpl;
import github.rpc.serviceImpl.UserserviceImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Random;


public class TestServer {
    public static void main(String[] args) {
        // 基于netty的rpc
        ApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(SpringConfig.class);
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        RpcServer rpcServer = ExtensionLoader.getExtensionLoader(RpcServer.class).getExtension("nettyServer");
//        rpcServer.start(8100);
        rpcServer.start(8400);

        //  不采用Spring的服务启动方式
////        ServiceProvider serviceProvider =  new ServiceProvider();
////        serviceProvider.fillServiceProvider(new UserserviceImpl(),"3","3");
////        SimpleRpcServer rpcServer1 = (SimpleRpcServer) ExtensionLoader.getExtensionLoader(RpcServer.class).getExtension("socketServer");
////        rpcServer1.setServiceProvider(serviceProvider.getServiceProvider());
////        rpcServer1.start();
    }
}
