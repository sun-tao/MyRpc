package github.rpc.loadbalance.loadbalancer;

import github.rpc.common.RpcRequest;
import github.rpc.loadbalance.LoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

@Slf4j
public class RandomLoadBalance implements LoadBalance {

    @Override
    public String loadBalance(List<String> addresses, RpcRequest rpcRequest) {
        Random random = new Random();
        int i = random.nextInt(addresses.size());
        log.info("负载均衡算法选择了{}服务器" , addresses.get(i));
        return addresses.get(i);
    }
}
