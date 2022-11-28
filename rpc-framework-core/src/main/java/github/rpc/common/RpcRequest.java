package github.rpc.common;

import java.io.Serializable;
import java.util.Arrays;


public class RpcRequest implements Serializable {
    // 提供的服务接口名
    private String interfaceName;
    // 接口中的方法名字
    private String methodName;

    @Override
    public String toString() {
        return "RpcRequest{" +
                "interfaceName='" + interfaceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", paramsType=" + Arrays.toString(paramsType) +
                ", params=" + Arrays.toString(params) +
                '}';
    }

    // 参数类型
    private Class<?>[] paramsType;
    // 参数列表
    private Object[] params;


    public RpcRequest() {

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
}
