package com.liangc.netty;

import android.os.Handler;
import android.util.Log;

import com.liangc.netty.handler.MessageHandler;
import com.liangc.netty.handler.MessageType;

import java.util.concurrent.Executors;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * Netty服务端
 * @author liangc
 * */
public class NettyServer {
    private static final String TAG = "NettyServer";
    /**
     * 网络连接
     * */
    private Channel channel;
    /**
     * 监听端口
     * */
    private int port;

    private DataHandlerAdapter dataHandlerAdapter;

    public NettyServer(int port) {
        this.port = port;
        dataHandlerAdapter = new DataHandlerAdapter(DataHandlerAdapter.ConnectType.SERVER);
    }

    /**
     * 启动客户端
     * */
    public void start() {
        Executors.newSingleThreadScheduledExecutor().submit(new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "服务启动");
                EventLoopGroup bossGroup = new NioEventLoopGroup();
                EventLoopGroup workerGroup = new NioEventLoopGroup();
                try {
                    ChannelInitServer channelInit = new ChannelInitServer(dataHandlerAdapter);
                    ServerBootstrap serverBootstrap = new ServerBootstrap();
                    serverBootstrap.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(channelInit)
                            .option(ChannelOption.SO_BACKLOG, 128)
                            .option(ChannelOption.TCP_NODELAY, true)
                            .option(ChannelOption.SO_KEEPALIVE, true);
                    ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
                    channel = channelFuture.channel();
                    channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                        @Override
                        public void operationComplete(Future<? super Void> future) {
                            if (future.isSuccess()) {
                                //服务启动成功
                                Log.w(TAG, "服务启动成功");
                                MessageHandler.getInstance().sendMessage(MessageType.SERVER_START_SUCCESS, "服务启动成功");
                            } else {
                                //服务启动失败
                                Log.w(TAG, "服务启动失败");
                                MessageHandler.getInstance().sendMessage(MessageType.SERVER_START_FAILED, "服务启动失败");
                            }
                        }
                    });
                    channel.closeFuture().sync();
                    Log.w(TAG, "服务关闭成功");
                    MessageHandler.getInstance().sendMessage(MessageType.SERVER_CLOSE_SUCCESS, "服务关闭成功");
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageHandler.getInstance().sendMessage(MessageType.SERVER_EXCEPTION, "服务异常：" + e.getMessage());
                } finally {
                    try {
                        workerGroup.shutdownGracefully().sync();
                        bossGroup.shutdownGracefully().sync();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        MessageHandler.getInstance().sendMessage(MessageType.SERVER_EXCEPTION, "服务异常2：" + e.getMessage());
                    }
                }
            }
        });
    }

    public void addHeartBeat(HeartBeatListener listener) {
        if (dataHandlerAdapter != null) {
            dataHandlerAdapter.addHeartBeatListener(listener);
        }
    }

    public void setHandler(Handler handler) {
        MessageHandler.getInstance().setHandler(handler);
    }

    public boolean sentData(byte[] data) {
        return dataHandlerAdapter.sendData(data);
    }

    public void stop() {
        Executors.newSingleThreadScheduledExecutor().submit(new Runnable() {
            @Override
            public void run() {
                if (channel != null) {
                    channel.close();
                    channel = null;
                }
            }
        });
    }
}
