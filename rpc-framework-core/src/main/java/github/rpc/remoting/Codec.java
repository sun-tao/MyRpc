package github.rpc.remoting;

import github.rpc.annotation.Spi;
import github.rpc.remoting.codec.MyRpcDecoder;
import github.rpc.remoting.codec.MyRpcEncoder;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

@Spi
public interface Codec {
    // fixed: 扩展性不足
    Encoder getEncoder();  // 为了适配http的编码和myrpc的编码
    Decoder getDecoder();
}
