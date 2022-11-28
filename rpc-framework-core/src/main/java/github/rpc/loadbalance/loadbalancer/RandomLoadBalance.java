package github.rpc.loadbalance.loadbalancer;

import github.rpc.loadbalance.LoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

@Slf4j
public class RandomLoadBalance implements LoadBalance {

    @Override
    public String loadBalance(List<String> addresses) {
        Random random = new Random();
        int i = random.nextInt(addresses.size());
        log.info("负载均衡算法选择了第{}台服务器" , i );
        return addresses.get(i);
    }
}
