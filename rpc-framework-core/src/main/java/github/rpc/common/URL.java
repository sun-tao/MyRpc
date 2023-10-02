package github.rpc.common;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;

@Data
@Setter
@Getter
public class URL {
    private String protocol = "MyRpc";
    private String ip;
    private String port;
    private String serviceName;
    private String applicationName;
    private String retryTimes = "3";
    private String loadbalacne = "random";
    private String clusterType = "FailoverCluster";
    private String registryIp = "localhost";
    private String registryPort = "2181";
    private String registryType = "zookeeper";
    private String codecName = "myrpc";
    private String consumerAsync = "false";  //默认客户端同步调用
    private String providerAsync = "fasle"; // 默认服务端同步调用
    private String serializerType = "0"; //0-java 1-hession
    public String parseUrl(){ // 服务端使用该接口
        return protocol + "|" + "{" + ip + ":" + port  + "}" + "?" + serviceName;
    }
    // 解析服务实例 ： ip + port
    public String parseInstance(){ //todo 服务实例 ip+port分割
        return ip + port;
    }

    public String parseRegistryInstance(){
        return registryIp + ":" +registryPort;
    }

    public InetSocketAddress toInetSocketAddress(){
        return new InetSocketAddress(ip,Integer.parseInt(port));
    }

    public static URL parseString2Url(String s){
        String protocol = s.split("|")[0];
        String instance = parseInstanceFromString(s);
        String ip = instance.split(":")[0];
        String port = instance.split(":")[1];
        String service = s.split("/?")[1];
        URL url = new URL();
        url.setIp(ip);
        url.setPort(port);
        return url;
    }

    private static String parseInstanceFromString(String s){
        // 根据 {} 分割
        int first = 0 , last = 0;
        for (int i = 0 ; i < s.length() ; i++){
            if (s.charAt(i) == '{'){
                first = i;
            }
            if (s.charAt(i) == '}'){
                last = i;
            }
        }
        return s.substring(first+1,last);
    }

}
