package route;

import github.rpc.route.ConditionRoute;
import github.rpc.route.Route;

import java.util.ArrayList;
import java.util.List;

public class ConditionRouteTest {
    public static void main(String[] args) {
        // 先匹配左端，如果匹配上才会应用该条路由策略
        // 再对invoker中的服务提供者IP列表匹配右端，匹配上才会加入结果集
        Route route0 = new ConditionRoute("192.168.1.3=>192.168.153.3");
        Route route1 = new ConditionRoute("=>!192.168.1.3");  // 排除预发布机
        Route route2 = new ConditionRoute("!192.168.1.3=>"); // 白名单
        Route route3 = new ConditionRoute("192.168.1.3=>"); // 黑名单
        Route route4 = new ConditionRoute("!192.168.1.3=>!10.11.1.3"); // 对刚上线的服务10.11.1.3，指定特定消费者,灰度发布
        List<String> invokers = new ArrayList<>();
        invokers.add("192.168.1.3");
        invokers.add("192.168.1.5");
        invokers.add("192.168.153.3");
        invokers.add("10.11.1.3");
        List<String> r0 = route0.route(invokers, "192.168.1.3");
        System.out.println(r0);
        List<String> r1 = route1.route(invokers, "192.168.1.3");
        System.out.println(r1);
        List<String> r2 = route2.route(invokers, "192.168.1.6");
        System.out.println(r2);
        List<String> r3 = route3.route(invokers, "192.168.1.6");
        System.out.println(r3);
        List<String> r4 = route4.route(invokers, "192.168.1.3");
        System.out.println(r4);


    }
}
