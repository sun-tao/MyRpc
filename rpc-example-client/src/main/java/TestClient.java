import github.rpc.BlogController;
import github.rpc.UserController;
import github.rpc.client.NettyRpcClient;
import github.rpc.client.RpcClient;
import github.rpc.client.RpcClientProxy;
import github.rpc.client.SimpleRpcClient;
import github.rpc.common.Blog;
import github.rpc.common.User;
import github.rpc.config.SpringConfig;
import github.rpc.extension.ExtensionLoader;
import github.rpc.registry.zk.ZkServiceRegister;
import github.rpc.service.BlogService;
import github.rpc.service.Userservice;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

public class TestClient {

    public static void main(String[] args) {
        // 基于netty的rpc
        ApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(SpringConfig.class);
//        UserController userController = (UserController) annotationConfigApplicationContext.getBean("userController");
//        userController.start();
        BlogController blogController = (BlogController) annotationConfigApplicationContext.getBean("blogController");
        blogController.start();


        // 基于Socket的rpc
//        SimpleRpcClient simpleRpcClient = new SimpleRpcClient(new ZkServiceRegister());
//        SimpleRpcClient socketClient = (SimpleRpcClient) ExtensionLoader.getExtensionLoader(RpcClient.class).getExtension("socket");
//        socketClient.setZkServiceRegister(new ZkServiceRegister());
//        RpcClientProxy rpcClientProxy = new RpcClientProxy(socketClient, "3", "3");
//        Userservice proxy = (Userservice) rpcClientProxy.getProxy(Userservice.class);
//        User userById = proxy.getUserById(6);
//        System.out.println(userById);
    }
}
