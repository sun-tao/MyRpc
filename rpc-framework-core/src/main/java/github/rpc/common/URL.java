package github.rpc.common;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;

@Data
@Setter
@Getter
public class URL {
    private String protocol = "myrpc";
    private String ip;
    private String port;
    private String side;
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
    private String timeout = "0";  //默认客户端超时时间为0
    private String mock = ""; //支持null值mock，int类型mock，String类型mock和自定义类型mock
    public String parseMockMode(){ // 是fail 还是 force
        int i = mock.indexOf(":");
        return mock.substring(0,i);
    }
    public String parseMockResult(){
        int i = mock.indexOf(":");
        String v = mock.substring(i+1,mock.length());
        return v;
    }
    public String parseUrl(){ // 服务端使用该接口
        return protocol + "|" + "{" + ip + ":" + port  + "}" + "?" + serviceName;
    }
    // 解析服务实例 ： ip + port
    public String parseInstance(){
        return ip + ":" + port;
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

    @Override
    public boolean equals(Object obj){
        if (this == obj){
            return true;
        }
        if (obj == null || getClass() != obj.getClass()){
            return false;
        }
        URL url = (URL) obj;
        return this.ip.equals(url.ip) && this.port.equals(url.port) && this.protocol.equals(url.protocol) && this.serviceName.equals(url.serviceName);
    }

    @Override
    public int hashCode(){
        int result = 31;
        result = 31 * result +  ip.hashCode();
        result = 31 * result +  port.hashCode();
        result = 31 * result +  protocol.hashCode();
        result = 31 * result +  serviceName.hashCode();
        return result;
    }

}
