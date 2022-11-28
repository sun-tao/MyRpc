package github.rpc.loadbalance.loadbalancer;

import github.rpc.loadbalance.LoadBalance;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance implements LoadBalance {

    @Override
    public String loadBalance(List<String> addresses) {
        Random random = new Random();
        int i = random.nextInt(addresses.size());
        System.out.println("负载均衡算法选择了第" + i + "台服务器");
        return addresses.get(i);
    }
}
