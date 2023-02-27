package github.rpc.route;

import org.springframework.util.StringUtils;

import java.util.*;

public class ConditionRoute implements Route {
    // 根据给定的文本串，构造匹配规则
    // 192.168.1.1=>192.168.1.22   !192.168.1.1,192.168.2.23=>!192.168.1.22
    private  MatchPair whenCondition;  // consumer
    private  MatchPair thenCondition; // provider
    public ConditionRoute(String textUrl){
        if (StringUtils.isEmpty(textUrl)) throw new RuntimeException("Illegal route rule!");
        int index = textUrl.indexOf("=>");
        if (index < 0) throw new RuntimeException("Illegal route rule!");
        textUrl = textUrl.trim();
        String whenRule = textUrl.substring(0,index);
        String thenRule = textUrl.substring(index+2);
        MatchPair when = StringUtils.isEmpty(whenRule) ? null : parseRule(whenRule);
        MatchPair then = StringUtils.isEmpty(thenRule) ? null : parseRule(thenRule);
        this.whenCondition = when;
        this.thenCondition = then;
    }

    private MatchPair parseRule(String rule){
        MatchPair mp = new MatchPair();
        if (rule.charAt(0) == '!'){
            // mismatch append
            String localRule = rule.substring(1);
            int i = localRule.indexOf(',');
            if (i < 0){
                mp.mismatch.add(localRule);
            }else{
                String[] splited = localRule.split(",");
                for (int j = 0 ; j < splited.length; j++){
                    mp.mismatch.add(splited[j]);
                }
            }
        }else {
            // match append
            int i = rule.indexOf(',');
            if (i < 0) mp.match.add(rule);
            else{
                String[] split = rule.split(",");
                for (int j = 0 ; j < split.length; j++){
                    mp.match.add(split[j]);
                }
            }
        }
        return mp;
    }

    private final class MatchPair{
        final Set<String> match = new HashSet<>();
        final Set<String> mismatch = new HashSet<>();

        public boolean isMatch(String url){
            if (match.size() > 0 && mismatch.size() == 0){
                // 匹配match
                return match.contains(url);
            }else if (match.size() == 0 && mismatch.size() > 0){
                return !mismatch.contains(url);
            }
            return false;
        }

        public boolean isEmpty(){
            return match.isEmpty() && mismatch.isEmpty();
        }

    }

    private boolean matchWhen(String url){
        if (whenCondition == null || whenCondition.isEmpty()) return true;
        return whenCondition.isMatch(url);
    }

    private boolean matchThen(String url){
        if (thenCondition == null || thenCondition.isEmpty()) return false;
        return thenCondition.isMatch(url);
    }

    /*
        input:  invokers: ip:port
                url: ip
     */
    @Override
    public List<String> route(List<String> invokers, String url) {
        // url为消费者的url
        if (invokers == null || invokers.size() == 0) return invokers;

        if (!matchWhen(url)){
            // 不match则不使用本条路由策略
            return invokers;
        }

        List<String> result = new ArrayList<>();
        for (int i = 0 ; i < invokers.size() ; i++){
            // 针对项目的输入做特殊处理, 分割 : , 将分割后的IP用来过滤
            String IpPort = invokers.get(i);
            String IP = IpPort.split(":")[0];
            if (matchThen(IP)){
                result.add(invokers.get(i));
            }
        }
        return result;
    }
}
