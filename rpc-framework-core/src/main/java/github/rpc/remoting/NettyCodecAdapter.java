package github.rpc.remoting;

import github.rpc.remoting.codec.MyRpcDecoder;
import github.rpc.remoting.codec.MyRpcEncoder;
import github.rpc.serializer.CommunicationProtocol;
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
            Encoder eCoder = codec.getEncoder();
            // fixed:扩展性不足,目前只支持myrpc协议，要接入其他自定义二进制协议需要改动此处
            eCoder.encode(o,byteBuf);
        }
    }

    public class InternalDecoder extends ByteToMessageDecoder{
        @Override
        protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
            Decoder dc = codec.getDecoder();
            if (dc instanceof MyRpcDecoder){
                do{
                    int saveReaderIndex = byteBuf.readerIndex();
                    Object obj = ((MyRpcDecoder) dc).decode(channelHandlerContext, byteBuf, list);
                    if (obj == null){
                        return;
                    }else if (obj == CommunicationProtocol.DecodeResult.NEED_MORE_INPUT){
                        byteBuf.readerIndex(saveReaderIndex);
                        return;
                    }else{
                        list.add(obj);
                    }
                } while (byteBuf.isReadable());
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
