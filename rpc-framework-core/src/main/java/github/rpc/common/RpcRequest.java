package github.rpc.common;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class RpcRequest implements Serializable {
    // 版本号机制
    private static final long serialVersionUID = -3911255650485738676L;
    // 提供的服务接口名
    private String interfaceName;
    // 接口中的方法名字
    private String methodName;
    // Id为随机字符串
    private static AtomicInteger INVOKE_ID = new AtomicInteger(0);
    private int requestId;
    // MessageType 分为两种：0为普通的消息，1为心跳包
    private int messageType = 0;
    // 参数类型
    private Class<?>[] paramsType;
    // 参数列表
    private Object[] params;

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

    public int getRequestId() {
        return requestId;
    }

    public RpcRequest() {
        requestId = INVOKE_ID.getAndIncrement();
    }

    public RpcRequest(String interfaceName, String methodName, Object[] params, Class<?>[] paramsType) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.params = params;
        this.paramsType = paramsType;
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
