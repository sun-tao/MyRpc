import github.rpc.*;
import github.rpc.remoting.MyRpcDecoder;
import github.rpc.remoting.MyRpcEncoder;
import github.rpc.remoting.client.RpcClientProxy;
import github.rpc.common.URL;
import github.rpc.config.SpringConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
@Slf4j
public class DemoConsumer {
    public static void main(String[] args) {
        RpcClientProxy proxy = new RpcClientProxy();
        URL url1 = new URL();
        url1.setServiceName(BlogService.class.getName());
        url1.setLoadBalacne("consistentHash");
        proxy.setUrl(BlogService.class.getName(),url1);
        MyRpcDecoder myRpcDecoder = new MyRpcDecoder();
        URL url2 = new URL();
        url2.setServiceName(Userservice.class.getName());
        url2.setLoadBalacne("consistentHash");
        proxy.setUrl(Userservice.class.getName(),url2);
        proxy.refer();

        BlogService blogService = (BlogService) proxy.getProxy(BlogService.class);
        Userservice userservice = (Userservice) proxy.getProxy(Userservice.class);
        System.out.println(blogService.getBlogByid(5));
        log.info("===");
        System.out.println(userservice.getUserById(1));
    }
}
