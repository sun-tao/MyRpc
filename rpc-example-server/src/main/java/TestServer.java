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
        NettyRpcServer rpcServer = SingletonFactory.getInstance(NettyRpcServer.class);
        rpcServer.start();

        // SPI机制，基于socket的rpc
//        ServiceProvider serviceProvider =  new ServiceProvider();
//        serviceProvider.fillServiceProvider(new UserserviceImpl(),"3","3");
//        SimpleRpcServer rpcServer1 = (SimpleRpcServer) ExtensionLoader.getExtensionLoader(RpcServer.class).getExtension("socketServer");
//        rpcServer1.setServiceProvider(serviceProvider.getServiceProvider());
//        rpcServer1.start();
    }
}
