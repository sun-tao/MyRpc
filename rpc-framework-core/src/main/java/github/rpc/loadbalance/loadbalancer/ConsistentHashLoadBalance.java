package github.rpc.loadbalance.loadbalancer;

import github.rpc.common.RpcRequest;
import github.rpc.loadbalance.LoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class ConsistentHashLoadBalance implements LoadBalance {
    // dubble源码中 对不同的服务采用不同的哈希环来 存储不同服务器的哈希值
    // 这里对不同的服务共享同一个哈希环
    // 除非address有变化，才会变动哈希环
    private final HashMap<Integer,ConsistentHashSelector> map = new HashMap<>();
    @Override
    public String loadBalance(List<String> addresses, RpcRequest rpcRequest) {
        int identityHashCode = System.identityHashCode(addresses);
        // 尝试从缓存中获取
        ConsistentHashSelector consistentHashSelector = map.get(identityHashCode);
        if (consistentHashSelector == null){
            // map中还没有保存ConsistentHashSelector
            map.clear(); // 先清除原有的map元素
            // 加入缓存
            map.put(identityHashCode,new ConsistentHashSelector(addresses,160));
            consistentHashSelector = map.get(identityHashCode);
        }
        // 希望请求同相同服务，相同方法的Rpc请求尽可能分给同一个结点
        String selected = consistentHashSelector.select(rpcRequest.getRpcServiceName());
        log.info("负载均衡算法选择了{}服务器" , selected);
        return selected;
    }

    static class ConsistentHashSelector{
        // 用TreeMap实现哈希环，treemap中的索引范围在0-2^31-1之间
        private TreeMap<Long,String> virtualInvokers;
        ConsistentHashSelector(List<String> address,int replicaNumber){
            // 哈希环
            this.virtualInvokers = new TreeMap<>();
            for (int i = 0 ; i < address.size() ; i++){
                // 每个address分配replicaNumber个虚拟结点
                for (int j = 0 ; j < replicaNumber / 4 ; j++){
                    // 先做md5
                    byte[] bytes = md5(address.get(i) + j);
                    for (int k = 0 ; k < 4 ; k++){
                        long hash = hash(bytes, k);
                        virtualInvokers.put(hash,address.get(i));
                    }
                }
            }
        }
        public String select(String rpcServiceKey){
            byte[] bytes = md5(rpcServiceKey);
            long hash = hash(bytes, 0);
            String value = selectForKey(hash);
            return value;
        }
        private String selectForKey(Long hash){
            Map.Entry<Long, String> longStringEntry = virtualInvokers.tailMap(hash, true).firstEntry();
            if (longStringEntry == null){
                longStringEntry = virtualInvokers.firstEntry();
            }
            return longStringEntry.getValue();
        }


        static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

            return md.digest();
        }

        static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }
    }
}
