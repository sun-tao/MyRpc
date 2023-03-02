package github.rpc.loadbalance.loadbalancer;

import github.rpc.common.RpcRequest;
import github.rpc.loadbalance.LoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

@Slf4j
public class RandomLoadBalance implements LoadBalance {
    // 权重随机负载均衡算法
    @Override
    public String loadBalance(List<String> invokers, RpcRequest rpcRequest) {
        Random random = new Random();
        if (invokers == null || invokers.size() == 0){
            return null;
        }
        int totalWeigth = 0;
        int[] weights = new int[invokers.size()];   // 维护前缀和数组
        boolean isSame = true; // 判断是否结点间权重全都一样，如全一样，则直接随机即可
        int lastWeight = 0;
        for (int i = 0 ; i < invokers.size() ; i++){
            int weight = getWeight(invokers.get(i));
            totalWeigth += weight;
            weights[i] = totalWeigth;
            if (i >= 1 && weight != lastWeight) isSame = false;
            lastWeight = weight;
        }
        if (isSame){
            // 直接随机
            log.info("负载均衡算法选择了{}服务器",invokers.get(random.nextInt(invokers.size())));
            return invokers.get(random.nextInt(invokers.size()));
        }
        int offset = random.nextInt(totalWeigth);
        for (int i = 0 ; i < weights.length; i++){
            if (weights[i] > offset) {
                log.info("负载均衡算法选择了{}服务器",invokers.get(random.nextInt(invokers.size())));
                return invokers.get(i);
            }
        }
        log.warn("no invoker find");
        return null;
    }

    private int getWeight(String invoker){
        int index = invoker.indexOf("?");
        String parameters = invoker.substring(index+1);
        String[] parameterSplited = parameters.split("&");
        for (int i = 0 ; i < parameterSplited.length; i++){
            String[] split = parameterSplited[i].split("=");
            if ("weight".equals(split[0])){
                return Integer.parseInt(split[1]);
            }
        }
        // 默认权重为0
        return 0;
    }
}
