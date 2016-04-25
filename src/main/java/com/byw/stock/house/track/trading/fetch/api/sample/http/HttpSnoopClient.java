package com.byw.stock.house.track.trading.fetch.api.sample.http;

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
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


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
public class HttpSnoopClient {

    private SslContext sslCtx = null;
    private EventLoopGroup _group = null;
    private Bootstrap _clientBootstrap = null;
    private Channel _channel = null;
    //
    public HashMap<String, String> _headerMap = new HashMap<String, String>();
    private Set<Cookie> _cookies = new HashSet<Cookie>();
    public String _content = "";
    public boolean _enableSSL = false;
    public String _host = null;
    public String _path = null;
    public int _port = 80;

    public HttpSnoopClient(String host, int port, String path, boolean enableSSL) {

        this._port = port;
        this._enableSSL = enableSSL;
        this._host = host;
        this._path = path;
    }

    /**
     * add header value .
     * 
     * @param key
     * @param value
     */
    public void addHeader(String key, String value) {

        _headerMap.put(key, value);
    }

    /**
     * set cookies.
     * 
     * @param key
     * @param value
     */
    public void addCookies(String key, String value) {

        _cookies.add(new DefaultCookie(key, value));
    }

    public void setContent(String content) {

        this._content = content;
    }

    public void start() throws Exception {

        if (_enableSSL == true) {
            sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }
        // Configure the client.
        try {
            //build HTTP request
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, this._path, Unpooled.copiedBuffer(_content, CharsetUtil.UTF_8));
            request.headers().set(HttpHeaders.Names.HOST, this._host);
            request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
            request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes());

            for (Iterator<String> keyIter = this._headerMap.keySet().iterator(); keyIter.hasNext();) {
                String keyName = keyIter.next();
                request.headers().set(keyName, this._headerMap.get(keyName));
            }
            //
            if (_cookies.isEmpty() == false) {
                // Set cookies.
                request.headers().set(HttpHeaders.Names.COOKIE, ClientCookieEncoder.LAX.encode(_cookies));
            }

            //
            _group = new NioEventLoopGroup(5);
            _clientBootstrap = new Bootstrap();
            //
            //set timeout
            _clientBootstrap.option(ChannelOption.TCP_NODELAY, true);
            _clientBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 100000);
            //
            _clientBootstrap.group(_group);
            _clientBootstrap.channel(NioSocketChannel.class);
            _clientBootstrap.handler(new HttpSnoopClientInitializer(sslCtx));

            // Make the connection attempt.
            _channel = _clientBootstrap.connect(this._host, this._port).sync().channel();
            // Send the HTTP request.
            _channel.writeAndFlush(request);

            // Wait for the server to close the connection.
            _channel.closeFuture().sync();
        } catch (Throwable t) {
            // Shut down executor threads to exit.
            t.printStackTrace();

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

    public static void main(String[] args) {

        ///cjmx/2016/20160105/0601766.xls
        //http://quotes.money.163.com/cjmx/2016/20160105/0601766.xls
        //#1
        //获取代码为sh600900，在2011-07-08的成交明细，数据为xls格式。
        //http://market.finance.sina.com.cn/downxls.php?date=2011-07-08&symbol=sh600900
        String host = "market.finance.sina.com.cn";
        int port = 80;
        String path = "/downxls.php?date=2011-07-08&symbol=sh600900";
        boolean enableSSL = false;
        HttpSnoopClient hclient = new HttpSnoopClient(host, port, path, enableSSL);
        hclient.addHeader(HttpHeaders.Names.USER_AGENT, "BYW");
        hclient.addHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-US,en;q=0.8");
        hclient.addHeader(HttpHeaders.Names.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        try {
            hclient.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
