package github.rpc.remoting.codec;

import github.rpc.remoting.Codec;
import github.rpc.remoting.Decoder;
import github.rpc.serializer.CommunicationProtocol;
import github.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import javafx.beans.binding.ObjectExpression;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// 可扩展通信协议格式
// ## 协议头：魔数(4B) ## 整体长度 (4B) ## 头长度(2B) ## 协议版本 (1B) ## 消息类型(1B) ## 序列化方式(1B) ## 消息ID(2B) ## 头部扩展字段()
// ## 协议体: 数据
@Slf4j
public class MyRpcDecoder implements Decoder,CommunicationProtocol {
    public Object decode(ChannelHandlerContext ctx,ByteBuf in, Object obj) throws Exception {
        // 魔数校验
        int magicNumber = in.readInt();
        if (magicNumber != MAGIC_NUMBER) ctx.close();
        // 整体长度
        int totalLength = in.readInt();
        // 头部长度
        int headerLength = in.readShort();
        int len = in.readableBytes();
        if (len < totalLength - headerLength){
            return DecodeResult.NEED_MORE_INPUT;
        }
        // 版本
        int version = in.readByte();
        // 消息类型
        int messageType = in.readByte();
        // 序列化方式
        int serializerType = in.readByte();
        // 消息ID
        byte[] idbytes = new byte[2];
        in.readBytes(idbytes,0,2);
        // 如果头部长度大于5，代表有扩展字段,要对扩展字段做处理
        if (headerLength == 5){
            // 解析对象
            byte[] bytes = new byte[totalLength - headerLength - 2];
            log.info("接收到的消息长度:{}",totalLength - headerLength - 2);
            in.readBytes(bytes);
            Serializer serializer= Serializer.getSerializerByType(serializerType);
            obj = serializer.deserialize(bytes,messageType);
            return obj;
        }
        return null;
    }
}
