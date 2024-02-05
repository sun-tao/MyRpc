package github.rpc.loadbalance.loadbalancer;

import github.rpc.Invoker;
import github.rpc.common.RpcRequest;
import github.rpc.loadbalance.LoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

@Slf4j
public class RandomLoadBalance implements LoadBalance {
    // 随机负载均衡算法
    @Override
    public String loadBalance(List<Invoker> invokers, RpcRequest rpcRequest) {
        int size = invokers.size();
        int idx = new Random().nextInt(size);
        Invoker invoker = invokers.get(idx);
        String instance =  invoker.getURL().parseInstance();
        log.info("负载均衡算法选择了{}服务器",instance);
        return instance;
    }
}
