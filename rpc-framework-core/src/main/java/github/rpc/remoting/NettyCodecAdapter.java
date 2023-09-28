package github.rpc.remoting;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;

public class NettyCodecAdapter {
    // 适配器类，用来解决abstractEndpoint的单个codec（handler）无法被多个netty的channel复用的问题
    private Codec codec;

    private ChannelHandler encoder = new InternalEncoder();

    private ChannelHandler decoder = new InternalDecoder();

    public class InternalEncoder extends MessageToByteEncoder {
        @Override
        protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
            MessageToByteEncoder ec = codec.getEncoder();
            // fixme:扩展性不足,目前只支持myrpc协议
            if (ec instanceof MyRpcEncoder){
                ((MyRpcEncoder) ec).encode(channelHandlerContext,o,byteBuf);
            }
        }
    }

    public class InternalDecoder extends ByteToMessageDecoder{
        @Override
        protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
            ByteToMessageDecoder dc = codec.getDecoder();
            if (dc instanceof MyRpcDecoder){
                ((MyRpcDecoder) dc).decode(channelHandlerContext,byteBuf,list);
            }
        }
    }

    public NettyCodecAdapter(Codec codec){
        this.codec = codec;
    }

    public ChannelHandler getEncoder(){
        return encoder;
    }

    public ChannelHandler getDecoder(){
        return decoder;
    }

}
