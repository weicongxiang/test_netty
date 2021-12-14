package com.liangc.netty;

import android.util.Log;

import com.liangc.netty.handler.MessageHandler;
import com.liangc.netty.handler.MessageType;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 数据收发处理类
 * <p>接收处理</p>
 * <p>发送处理</p>
 * <p>心跳发送</p>
 * @author liangc
 * */
//因为客户端和服务端都有使用，必须添加此注释实现共享
@ChannelHandler.Sharable
public class DataHandlerAdapter extends ChannelHandlerAdapter {
    private static final String TAG = "++++++++++++++++++";

    public enum ConnectType {
        SERVER,
        CLIENT,
    }
    private ConnectType type;

    private ChannelHandlerContext channelHandlerContext;
    private HeartBeatListener listener;

    DataHandlerAdapter(ConnectType type) {
        this.type = type;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.channelHandlerContext = ctx;
        //连接成功
        InetSocketAddress socketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
        String connectAddress =  socketAddress.getAddress().getHostAddress();
        if (type == ConnectType.SERVER) {
            MessageHandler.getInstance().sendMessage(MessageType.SERVER_CONNECT_SUCCESS, connectAddress);
        }
        Log.w(TAG, "连接成功：" + connectAddress);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        this.channelHandlerContext = ctx;
        Log.w(TAG, "连接断开");
        if (type == ConnectType.CLIENT) {
            MessageHandler.getInstance().sendMessage(MessageType.CLIENT_DISCONNECT_SUCCESS, "连接断开");
        } else {
            MessageHandler.getInstance().sendMessage(MessageType.SERVER_DISCONNECT_SUCCESS, "连接断开");
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        this.channelHandlerContext = ctx;
        //接收数据
        Log.w(TAG, "收到数据");
        //取出数据
        ByteBuf byteBuf = (ByteBuf)msg;
        byte[] recvData = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(recvData);
        byteBuf.clear();
        String strChinese_gbk = "";
        try {
            strChinese_gbk = new String(recvData,"GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        MessageHandler.getInstance().sendMessage(MessageType.RECEIVE_DATA, strChinese_gbk);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        this.channelHandlerContext = ctx;
        //发送数据
        ctx.write(msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        this.channelHandlerContext = ctx;
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent)evt).state();
            if (state == IdleState.ALL_IDLE) {
                //发送心跳
                if (listener != null) {
                    sendData(listener.getHeartBeat());
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 心跳数据
     * */
    void addHeartBeatListener(HeartBeatListener listener) {
        this.listener = listener;
    }

    boolean sendData(byte[] data) {
        ByteBuf byteBuf = channelHandlerContext.alloc().buffer();
        byteBuf.writeBytes(data);
        ChannelFuture future = channelHandlerContext.channel().write(byteBuf);
        channelHandlerContext.flush();
        return future.isSuccess();
    }
}
