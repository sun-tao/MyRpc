package loadbalance;

import github.rpc.loadbalance.LoadBalance;

import java.util.ArrayList;
import java.util.List;

public class RandomBalanceTest {
    public static void main(String[] args) {
//        LoadBalance loadBalance = new RandomLoadBalance();
//        String invoker1 = "127.0.0.1:8080?weight=10";
//        String invoker2 = "127.0.0.1:8081?weight=0";
//        String invoker3 = "127.0.0.1:8082?weight=0";
//        List<String> invokers = new ArrayList<>();
//        invokers.add(invoker1);
//        invokers.add(invoker2);
//        invokers.add(invoker3);
//        for (int i = 0 ; i < 10 ; i++){
//            String s = loadBalance.loadBalance(invokers, null);
//            System.out.println(s);
//        }

        String s = "123";
        System.out.println(s.indexOf("?"));
    }


}
