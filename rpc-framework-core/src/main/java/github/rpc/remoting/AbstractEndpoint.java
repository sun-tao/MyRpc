package github.rpc.remoting;

import github.rpc.common.URL;
import github.rpc.extension.ExtensionLoader;

public abstract class AbstractEndpoint extends AbstractPeer{
    private Codec codec;
    private int timeout;

    public AbstractEndpoint(URL url, ChannelHandler handler) {
        super(url, handler);
        this.codec = getCodec2(url);
    }

    private Codec getCodec2(URL url){
        String codec_name = url.getCodecName();
        // 此处根据myrpc协议，会得到myrpcCodec
        Codec codec = ExtensionLoader.getExtensionLoader(Codec.class).getExtension(codec_name,url);
        return codec;
    }

    public Codec getCodec(){
        return codec;
    }
}
