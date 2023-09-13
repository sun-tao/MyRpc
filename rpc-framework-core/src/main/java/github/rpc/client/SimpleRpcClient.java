//package github.rpc.client;
//
//
//import github.rpc.common.RpcRequest;
//import github.rpc.common.RpcResponse;
//import github.rpc.common.SingletonFactory;
//import github.rpc.loadbalance.LoadBalance;
//import github.rpc.loadbalance.loadbalancer.RandomLoadBalance;
//import github.rpc.registry.zk.ZkServiceDiscovery;
//import github.rpc.registry.zk.ZkServiceRegister;
//
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.net.InetSocketAddress;
//import java.net.Socket;
//
//public class SimpleRpcClient implements RpcClient {
//    String targetIp;
//    int targetPort;
//    private ZkServiceRegister zkServiceRegister = SingletonFactory.getInstance(ZkServiceRegister.class);
//    private ZkServiceDiscovery zkServiceDiscovery = SingletonFactory.getInstance(ZkServiceDiscovery.class);
//    public SimpleRpcClient(ZkServiceRegister zkServiceRegister){
//        this.zkServiceRegister = zkServiceRegister;
//    }
//    public SimpleRpcClient(){
//
//    }
//    public void setZkServiceRegister(ZkServiceRegister zkServiceRegister){
//        this.zkServiceRegister = zkServiceRegister;
//    }
//
//    public RpcResponse sendRequest(RpcRequest rpcRequest) {
//        try {
//            LoadBalance loadBalance = new RandomLoadBalance();
//            String inetSocketAddress = zkServiceDiscovery.serviceDiscovery(rpcRequest.getInterfaceName(),loadBalance,rpcRequest,null,null);
//            targetIp = inetSocketAddress;
//            targetPort = Integer.parseInt(inetSocketAddress);
//            Socket socket = new Socket(targetIp, targetPort);
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
//            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
//
//            // 客户端需要动态地设定要访问的服务,采用动态代理的方法去构造request(这里由参数构造好了，直接用即可)
//            objectOutputStream.writeObject(rpcRequest);
//            objectOutputStream.flush();
//
//            RpcResponse rpcResponse = (RpcResponse) objectInputStream.readObject();
//            return rpcResponse;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//}
