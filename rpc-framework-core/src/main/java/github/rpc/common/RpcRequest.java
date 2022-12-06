package github.rpc.common;

import java.io.Serializable;
import java.util.Arrays;


public class RpcRequest implements Serializable {
    // 提供的服务接口名
    private String interfaceName;
    // 接口中的方法名字
    private String methodName;
    // Id为随机字符串
    private String requestId;
    // MessageType 分为两种：0为普通的消息，1为心跳包
    private int messageType = 0;

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "interfaceName='" + interfaceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", requestId='" + requestId + '\'' +
                ", messageType=" + messageType +
                ", paramsType=" + Arrays.toString(paramsType) +
                ", params=" + Arrays.toString(params) +
                '}';
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    // 参数类型
    private Class<?>[] paramsType;
    // 参数列表
    private Object[] params;


    public RpcRequest() {

    }

    public RpcRequest(String interfaceName, String methodName, Object[] params, Class<?>[] paramsType,String requestId) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.params = params;
        this.paramsType = paramsType;
        this.requestId = requestId;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public  Class<?>[] getParamsType() {
        return paramsType;
    }

    public void setParamsType(Class<?>[] paramsType) {
        this.paramsType = paramsType;
    }

    public String getRpcServiceName(){
        return interfaceName + methodName;
    }
}
