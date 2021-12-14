package com.liangc.netty.handler;

public class MessageType {
    /**
     * 接收到数据
     * */
    public static final int RECEIVE_DATA = 1;

    /**
     * 服务端异常
     * */
    public static final int SERVER_EXCEPTION = 100;
    /**
     * 服务启动成功
     * */
    public static final int SERVER_START_SUCCESS = 101;
    /**
     * 服务启动失败
     * */
    public static final int SERVER_START_FAILED = 102;
    /**
     * 服务端被客户端连接成功
     * */
    public static final int SERVER_CONNECT_SUCCESS = 103;
    /**
     * 服务端断开连接成功
     * */
    public static final int SERVER_DISCONNECT_SUCCESS = 105;
    /**
     * 服务端关闭成功
     * */
    public static final int SERVER_CLOSE_SUCCESS = 106;

    /**
     * 客户端异常
     * */
    public static final int CLIENT_EXCEPTION = 200;
    /**
     * 客户端连接服务成功
     * */
    public static final int CLIENT_CONNECT_SUCCESS = 203;
    /**
     * 客户端连接断服务失败
     * */
    public static final int CLIENT_CONNECT_FAILED = 204;
    /**
     * 客户端断开连接成功
     * */
    public static final int CLIENT_DISCONNECT_SUCCESS = 205;
    /**
     * 客户端关闭成功
     * */
    public static final int CLIENT_CLOSE_SUCCESS = 206;
}
