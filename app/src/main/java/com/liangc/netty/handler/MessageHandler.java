package com.liangc.netty.handler;

import android.os.Handler;
import android.os.Message;

public class MessageHandler {
    private Handler handler;

    private static MessageHandler instance = new MessageHandler();
    public static MessageHandler getInstance() {
        return instance;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void sendMessage(int code, Object data) {
        if (handler != null) {
            Message msg = new Message();
            msg.what = code;
            msg.obj = data;
            handler.sendMessage(msg);
        }
    }
}
