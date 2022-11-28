package github.rpc.serializer;


import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class Encode extends MessageToByteEncoder implements MessageType {
    //首先要对java对象进行序列化
    private Serializer serializer;

    public Encode(Serializer serializer){
        this.serializer = serializer;
    }

    //接着要对序列化得到的字节流来进行定长编码，解决粘包问题
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (msg instanceof RpcRequest){
            out.writeShort(RPC_REQUEST);
        }
        else if (msg instanceof RpcResponse){
            out.writeShort(RPC_RESPONSE);
        }
        else{
            System.out.println("不支持解析该对象");
            return;
        }

        out.writeShort(serializer.getSerializerType());  // 0 : java原生  1 : Google protobuf

        // 使用protobuf来进行序列化编码，输入都为普通的RpcRequest和RpcResponse对象，但通过在内部将其转为protobuf制定的对象
        // 再对其进行编码，可以获得编码的高效性，性能比直接java编码对象要高很多，且在接收端，可以实现跨语言的解析
        byte[] bytes = serializer.serialize(msg);
        int length = bytes.length;
        out.writeInt(length);

        out.writeBytes(bytes);
        // 至此，一个数据包的编码完成
    }
}
