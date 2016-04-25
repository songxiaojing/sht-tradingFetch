package com.byw.stock.house.track.trading.fetch.api.common;

import com.byw.stock.house.track.trading.fetch.api.HttpClientReferent;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;


/**
 * 
 * Your class summary,end with '.'.
 * 
 * Your class Detail description,end with '.'.
 * 
 * @title HttpSnoopClientInitializer
 * @package com.topsec.bdc.platform.api.http.snoop.client
 * @author baiyanwei
 * @version
 * @date Jul 17, 2015
 * 
 */
public class ClientInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext _sslCtx;
    private final HttpClientReferent _hcr;

    public ClientInitializer(SslContext sslCtx, HttpClientReferent hcr) {

        this._sslCtx = sslCtx;
        this._hcr = hcr;
    }

    @Override
    public void initChannel(SocketChannel ch) {

        ChannelPipeline p = ch.pipeline();

        // Enable HTTPS if necessary.
        if (_sslCtx != null) {
            p.addLast(_sslCtx.newHandler(ch.alloc()));
        }
        //timeout
        p.addLast("readTimeoutHandler", new ReadTimeoutHandler(30));
        p.addLast("writeTimeoutHandler", new WriteTimeoutHandler(30));
        //
        p.addLast(new HttpClientCodec());

        // Remove the following line if you don't want automatic content decompression.
        p.addLast(new HttpContentDecompressor());

        // Uncomment the following line if you don't want to handle HttpContents.
        //p.addLast(new HttpObjectAggregator(1048576));

        p.addLast(new HttpClientHandler(_hcr));
    }
}
