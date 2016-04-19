package com.byw.stock.house.track.api.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

import java.util.Iterator;
import java.util.Set;

import com.byw.stock.house.track.api.HttpClientReferent;


/**
 * 
 * Your class summary,end with '.'.
 * 
 * Your class Detail description,end with '.'.
 * 
 * @title HttpClientHandler
 * @package com.byw.stock.house.track.api.common
 * @author baiyanwei
 * @version
 * @date Jan 28, 2016
 * 
 */
public class HttpClientHandler extends SimpleChannelInboundHandler<HttpObject> {

    final private static PlatformLogger _logger = PlatformLogger.getLogger(HttpClientHandler.class);
    //
    private final HttpClientReferent _hcr;

    private final ByteBuf _responseBodyBuf;

    public HttpClientHandler(HttpClientReferent hcr) {

        this._hcr = hcr;
        _responseBodyBuf = PooledByteBufAllocator.DEFAULT.directBuffer();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {

        //if message is HTTP Response header
        if (msg instanceof HttpResponse) {
            //clear the buffer.
            _responseBodyBuf.clear();
            //
            HttpResponse response = (HttpResponse) msg;
            //get HTTP Response code.
            this._hcr._responseStatus = response.getStatus();
            //
            //print header.
            Set<String> headers = response.headers().names();
            for (Iterator<String> keyIter = headers.iterator(); keyIter.hasNext();) {
                String keyName = keyIter.next();
                //System.out.println(keyName + ":" + response.headers().get(keyName));
                this._hcr._responseHeaderMap.put(keyName, response.headers().get(keyName));
            }
        }
        //content or lastContent
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            _responseBodyBuf.writeBytes(content.content());
            //if is last content fire done.
            if (content instanceof LastHttpContent) {
                try {
                    fireDone(_responseBodyBuf.toString(this._hcr._responseBodyCharset));
                } finally {
                    ctx.close();
                    releaseResource();
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        _logger.exception(cause);
        ctx.close();
        releaseResource();
        final Throwable error = cause;
        new Thread() {

            public void run() {

                _hcr._responseListener.fireException(_hcr, error);
            }
        }.start();
    }

    private void releaseResource() {

        //release all
        _responseBodyBuf.release();
    }

    /**
     * @param content
     */
    private void fireDone(final Object content) {

        new Thread() {

            public void run() {

                _hcr._responseListener.fireDone(_hcr, content);
            }
        }.start();
    }
}
