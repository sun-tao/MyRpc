package github.rpc.remoting.codec;


import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.URL;
import github.rpc.serializer.CommunicationProtocol;
import github.rpc.serializer.MessageType;
import github.rpc.serializer.Serializer;
import github.rpc.util.GenerateMessageID;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

// 可扩展通信协议格式
// ## 协议头：魔数(4B) ## 整体长度 (4B) ## 头长度(2B) ## 协议版本 (1B) ## 消息类型(1B) ## 序列化方式(1B) ## 消息ID(2B) ## 头部扩展字段()
// ## 协议体: 数据
@Slf4j
public class MyRpcEncoder extends MessageToByteEncoder implements MessageType, CommunicationProtocol{
    //首先要对java对象进行序列化
    private Serializer serializer;

    public MyRpcEncoder(URL url){
        String serializerType = url.getSerializerType();
        serializer = Serializer.getSerializerByType(Integer.parseInt(serializerType));
    }

    //接着要对序列化得到的字节流来进行定长编码，解决粘包问题
    @Override
    public void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        byte[] bytes = serializer.serialize(msg);
        log.info("序列化后消息长度: " + bytes.length);
        // 整体长度 = 2 + 1 + 1 + 1 + 2 + 扩展字段长度 + 数据长度  = 头部长度 + 数据长度
        int extendedLength = 0; // 扩展字段长度
        short headerLength = (short)(5 + extendedLength);  // 0 扩展字段
        int dataLength = bytes.length;  // 数据长度
        int totalLength = headerLength + dataLength + 2;
        // 魔数
        out.writeInt(MAGIC_NUMBER);
        //整体长度
        out.writeInt(totalLength);
        // 头长度
        out.writeShort(headerLength);
        // 版本
        out.writeByte(VERSION);
        // 消息类型
        if (msg instanceof RpcRequest){
            out.writeByte(RPC_REQUEST);
        }
        else if (msg instanceof RpcResponse){
            out.writeByte(RPC_RESPONSE);
        }
        else{
            System.out.println("不支持解析该对象");
            return;
        }
        // 序列化方式
        out.writeByte(serializer.getSerializerType());  // 0 : java原生 1 : Hessian2
        // 消息ID
        out.writeBytes(GenerateMessageID.getMessageId());
        // 扩展字段,暂时没有
        // 数据
        out.writeBytes(bytes);
    }
}
