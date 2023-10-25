import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import github.rpc.BlogService;
import github.rpc.Userservice;
import github.rpc.common.URL;
import github.rpc.config.CustomShutdownHook;
import github.rpc.config.SpringConfig;
import github.rpc.extension.ExtensionLoader;

import github.rpc.remoting.server.RpcServerProxy;
import github.rpc.serviceImpl.BlogServiceImpl;
import github.rpc.serviceImpl.UserserviceImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class DemoProvider {
    public static void main(String[] args) {
        URL url1 = new URL();
        url1.setIp("127.0.0.1");
        url1.setPort("20880");
        url1.setSide("provider");
        url1.setProtocol("myrpc");
        URL url2 = new URL();
        url2.setIp("127.0.0.1");
        url2.setPort("20880");
        url2.setSide("provider");
        url2.setProtocol("myrpc");
//        url2.setConsumer_async("true");
        String serviceName1 = BlogService.class.getName();
        url1.setServiceName(serviceName1);
        String serviceName2 = Userservice.class.getName();
        url2.setServiceName(serviceName2);
        RpcServerProxy rpcServerProxy = new RpcServerProxy();
        rpcServerProxy.setRef(serviceName1,new BlogServiceImpl());
        rpcServerProxy.setUrl(serviceName1,url1);
        rpcServerProxy.setRef(serviceName2,new UserserviceImpl());
        rpcServerProxy.setUrl(serviceName2,url2);
        initFlowRules(serviceName1);
        rpcServerProxy.exportAndRegister();
        //rpcServerProxy.await(); // 没必要的，netty只要启动了server，在不关闭的情况下，默认是会保证主线程不退出的
    }

    private static void initFlowRules(String interfaceName){
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource(interfaceName);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // Set limit QPS to 20.
        rule.setCount(5);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }
}
