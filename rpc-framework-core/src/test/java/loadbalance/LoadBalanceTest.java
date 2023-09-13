package loadbalance;

import github.rpc.common.RpcRequest;
import github.rpc.loadbalance.LoadBalance;
import github.rpc.loadbalance.loadbalancer.ConsistentHashLoadBalance;

import java.util.ArrayList;
import java.util.List;

public class LoadBalanceTest {
    public static void main(String[] args) {
        List<String> addresses = new ArrayList<>();
        addresses.add("sever1");
        addresses.add("sever2");
        addresses.add("sever3");
        addresses.add("sever4");
        addresses.add("sever5");
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setInterfaceName("userService");
        rpcRequest.setMethodName("getUserById");
        RpcRequest rpcRequest1 = new RpcRequest();
        rpcRequest1.setMethodName("blogService123");
        rpcRequest1.setMethodName("getBlogBy12335123Id");
        LoadBalance loadBalance = new ConsistentHashLoadBalance();
//        loadBalance.loadBalance(addresses,rpcRequest);
//        loadBalance.loadBalance(addresses,rpcRequest);
//        loadBalance.loadBalance(addresses,rpcRequest1);
    }
}
