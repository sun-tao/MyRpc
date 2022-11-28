package github.rpc.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class Decode extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 有一个问题，这个方法会自动调用多少次？ 如果一次发了多个Response对象过来，能一次解码得到多个对象吗？待测试
        // 经过测试，这是可以的，一次发多个response对象过来，缓冲区中会有多个对象的字节，本方法会一直读取缓冲区
        // 直至不满足解码的条件

        // 获取消息类型
        short messageType = in.readShort();

        // 获取序列化方式
        short serializerType = in.readShort();

        // 消息长度
        int length = in.readInt();

        byte[] bytes = new byte[length];
        in.readBytes(bytes);

        Serializer serializer = Serializer.getSerializerByType(serializerType);
        // 解码同理编码，对于protobuf，获得的二进制字节流，在内部先是解析成protobuf的对应对象，再将其转为普通的java对象返回
        // 对外来说，解码出来的就是普通的RpcRequest或者RpcResponse对象
        Object obj = serializer.deserialize(bytes,messageType);
        out.add(obj);
    }
}
