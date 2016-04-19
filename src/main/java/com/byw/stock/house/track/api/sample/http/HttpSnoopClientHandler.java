package com.byw.stock.house.track.api.sample.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;


public class HttpSnoopClientHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final StringBuilder _responseBodyBuf = new StringBuilder();

    private Charset _bodyCharset = CharsetUtil.UTF_8;

    public HttpSnoopClientHandler() {

    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {

        if (msg instanceof HttpResponse) {
            //
            _responseBodyBuf.setLength(0);
            //
            HttpResponse response = (HttpResponse) msg;
            HttpResponseStatus status = response.getStatus();
            //
            if (status.code() < 200 || status.code() >= 300) {
                fireError(new Exception("HTTP Response Code Exception, The Response code is " + status.code()));
                ctx.close();
                return;
            }
            String contentType = response.headers().get(HttpHeaders.Names.CONTENT_TYPE);
            if (contentType != null && contentType.isEmpty() == false && contentType.indexOf(HttpHeaders.Values.CHARSET) != -1) {
                //Content-Type:application/x-javascript; charset=GBK
                String[] contentTypeArray = contentType.split(";");
                for (int i = 0; i < contentTypeArray.length; i++) {
                    String contentTypeElement = contentTypeArray[i].toLowerCase();
                    if (contentTypeElement.indexOf("charset") != -1 && contentTypeElement.indexOf("=") != -1) {
                        String[] values = contentTypeElement.split("=");
                        if (values.length == 2) {
                            _bodyCharset = Charset.forName(values[1]);
                        }
                    }
                }
            }
        }
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            //if (content.content().isReadable() == true) {
            _responseBodyBuf.append(content.content().toString(_bodyCharset));
            //}
            if (content instanceof LastHttpContent) {
                fireSucceed(_responseBodyBuf.toString());
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        cause.printStackTrace();
        fireError(cause);
        ctx.close();
    }

    /**
     * @param exception
     */
    private void fireError(Throwable exception) {

        exception.printStackTrace();
    }

    /**
     * @param content
     */
    private void fireSucceed(String content) {

        System.out.println(content);
    }
}
