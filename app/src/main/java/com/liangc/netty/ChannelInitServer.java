package com.liangc.netty;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 服务端数据收发线程
 * @author liangc
 * */
public class ChannelInitServer extends ChannelInitializer<SocketChannel> {

    private DataHandlerAdapter adapter;

    ChannelInitServer(DataHandlerAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        try {
            ChannelPipeline channelPipeline = ch.pipeline();
            //添加心跳机制，例：每3000ms发送一次心跳
            channelPipeline.addLast(new IdleStateHandler(3000, 3000, 3000, TimeUnit.MILLISECONDS));
            //添加数据处理（接收、发送、心跳）
            channelPipeline.addLast(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
