package com.byw.stock.house.track.api.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.CharsetUtil;

import java.util.Iterator;

import com.byw.stock.house.track.api.HttpClientReferent;


/**
 * 
 * Your class summary,end with '.'.
 * 
 * Your class Detail description,end with '.'.
 * 
 * @title HttpSnoopClient
 * @package com.topsec.bdc.platform.api.http.snoop.client
 * @author baiyanwei
 * @version
 * @date Jul 17, 2015
 * 
 */
public class HttpClient {

    final private static PlatformLogger _logger = PlatformLogger.getLogger(HttpClient.class);

    //
    final private HttpClientReferent _hcr;
    //
    private SslContext sslCtx = null;
    private EventLoopGroup _group = null;
    private Bootstrap _clientBootstrap = null;
    private Channel _channel = null;

    //

    public HttpClient(HttpClientReferent hcr) {

        this._hcr = hcr;
    }

    public void start() throws Exception {

        if (_hcr._enableSSL == true) {
            sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }
        // Configure the client.
        try {
            //build HTTP request
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, _hcr._path, Unpooled.copiedBuffer(_hcr._requestContent, CharsetUtil.UTF_8));
            request.headers().set(HttpHeaders.Names.HOST, _hcr._host);
            request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
            request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes());

            for (Iterator<String> keyIter = _hcr._requestHeaderMap.keySet().iterator(); keyIter.hasNext();) {
                String keyName = keyIter.next();
                request.headers().set(keyName, _hcr._requestHeaderMap.get(keyName));
            }
            //
            if (_hcr._requestCookies.isEmpty() == false) {
                // Set cookies.
                request.headers().set(HttpHeaders.Names.COOKIE, ClientCookieEncoder.LAX.encode(_hcr._requestCookies));
            }

            //
            _group = new NioEventLoopGroup(_hcr._nioEventLoopGroupSize);
            _clientBootstrap = new Bootstrap();
            //
            //set timeout
            _clientBootstrap.option(ChannelOption.TCP_NODELAY, true);
            _clientBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, _hcr._connect_timeout_millis);
            //
            _clientBootstrap.group(_group);
            _clientBootstrap.channel(NioSocketChannel.class);
            _clientBootstrap.handler(new ClientInitializer(sslCtx, _hcr));

            // Make the connection attempt.
            _channel = _clientBootstrap.connect(_hcr._host, _hcr._port).sync().channel();
            // Send the HTTP request.
            _channel.writeAndFlush(request);

            // Wait for the server to close the connection.
            _channel.closeFuture().sync();
        } catch (Throwable t) {
            // Shut down executor threads to exit.
            _logger.exception(t);
            final Throwable ft = t;
            new Thread() {

                public void run() {

                    _hcr._responseListener.fireException(_hcr, ft);
                }
            }.start();
        } finally {
            if (_group != null) {
                _group.shutdownGracefully();
            }
        }
    }

    public void stop() throws Exception {

        if (_group != null) {
            _group.shutdownGracefully();
        }

    }
}
