import github.rpc.BlogController;
import github.rpc.UserController;
import github.rpc.client.NettyRpcClient;
import github.rpc.client.RpcClientProxy;
import github.rpc.common.Blog;
import github.rpc.common.User;
import github.rpc.config.SpringConfig;
import github.rpc.registry.zk.ZkServiceRegister;
import github.rpc.service.BlogService;
import github.rpc.service.Userservice;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

public class TestClient {

    public static void main(String[] args) {
        ApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(SpringConfig.class);
        UserController userController = (UserController) annotationConfigApplicationContext.getBean("userController");
        userController.start();
        BlogController blogController = (BlogController) annotationConfigApplicationContext.getBean("blogController");
        blogController.start();
    }
}
