package com.liangc.test;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.liangc.netty.HeartBeatListener;
import com.liangc.netty.NettyClient;
import com.liangc.netty.NettyServer;
import com.liangc.netty.R;
import com.liangc.netty.handler.MessageType;

import java.io.UnsupportedEncodingException;
/**
 * Netty测试
 * @author liangc
 * */
public class MainActivity extends AppCompatActivity {

    private boolean isTestServer = false;
    private boolean isTestClient = false;
    private NettyClient client;
    private NettyServer server;

    private TextView tvResult;

    private String Stringjson= "";
    private String result = "";
    private EditText ipEt;
    private EditText portEt;
    private EditText contentEt;
    private boolean errorState=false;//true是异常关闭，需要重启；false是正常关闭(退出页面时)不需要重启

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipEt = findViewById(R.id.ipEt);
        portEt = findViewById(R.id.portEt);

        contentEt = findViewById(R.id.contentEt);

        tvResult = findViewById(R.id.tvTestResult);




        //测试服务端
        findViewById(R.id.btnTestServer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isTestServer) {
                    result = "";
                    testNettyServer();
                } else {
                    stopNettyServer();
                }
                isTestServer = !isTestServer;
            }
        });
        //测试客户端
        findViewById(R.id.btnTestClient).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isTestClient) {
                    result = "";
                    testNettyClient();
                } else {
                    stopNettyClient();
                }
                isTestClient = !isTestClient;
            }
        });

        //发送
        findViewById(R.id.sendBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (client!=null){
                    try {
                        client.sentData(contentEt.getText().toString().getBytes("GBK"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }else {
                    Toast.makeText(MainActivity.this,"连接断开",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    //测试客户端
    private void testNettyClient() {

        client = new NettyClient(ipEt.getText().toString(), Integer.parseInt(portEt.getText().toString()));
        client.addHeartBeat(new HeartBeatListener() {
            @Override
            public byte[] getHeartBeat() {
                String data = "心跳";
                try {
                    client.sentData("测试数据".getBytes("GBK"));
                    return data.getBytes("GBK");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        client.setHandler(handler);
        client.start();
    }

    private void stopNettyClient() {
        if (client != null) {
            client.stop();
            client=null;
        }
    }

    private void testNettyServer() {
        server = new NettyServer(8080);
        server.addHeartBeat(new HeartBeatListener() {
            @Override
            public byte[] getHeartBeat() {
                String data = "心跳";
                try {
                    server.sentData("测试数据".getBytes("GBK"));
                    return data.getBytes("GBK");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        server.setHandler(handler);
        server.start();
    }

    private void stopNettyServer() {
        if (server != null) {
            server.stop();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
//            result += "\r\n";
//            result += msg.obj;
//            tvResult.setText(result);
            result = (String) msg.obj;

            Stringjson += "\r\n";
            Stringjson += result;
            tvResult.setText(Stringjson);
            switch (msg.what){
                case MessageType.RECEIVE_DATA://接收到数据
                    //{"desc":"client"}
                    //测试客户端，测试服务端需要注释下面代码
//                    String stringJson="{\"code\":200,\"message\":\"哎妈呀，收到数据了！\",\"data\":{}}";
//                    if (client!=null){
//                        try {
//                            client.sentData(stringJson.getBytes("GBK"));
//                        } catch (UnsupportedEncodingException e) {
//                            e.printStackTrace();
//                        }
//                    }else {
//                        Toast.makeText(MainActivity.this,"连接断开",Toast.LENGTH_SHORT).show();
//                    }
                    Log.d("+++++++++++++1",result);
                    break;

                case MessageType.CLIENT_EXCEPTION://客户端异常
                    testNettyClient();//重新启动客户端
                    Log.d("+++++++++++++2",result);
                    break;

                case MessageType.CLIENT_CONNECT_SUCCESS://客户端连接服务成功
                    //true是异常关闭，需要重启；false是正常关闭(退出页面时)不需要重启
                    errorState=true;
                    Log.d("+++++++++++++3",result);
                    break;

                case MessageType.CLIENT_CONNECT_FAILED://客户端连接断服务失败
                    Log.d("+++++++++++++4",result);
                    break;

                case MessageType.CLIENT_DISCONNECT_SUCCESS://客户端断开连接成功
                    Log.d("+++++++++++++5",result);
                    break;

                case MessageType.CLIENT_CLOSE_SUCCESS://客户端关闭成功
                    if (errorState){//true是异常关闭，需要重启；false是正常关闭(退出页面时)不需要重启
                        testNettyClient();//重新启动客户端
                    }
                    Log.d("+++++++++++++6",result);
                    break;

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        errorState=false;
        stopNettyClient();
        if (handler!=null){
            handler.removeCallbacksAndMessages(null);
            handler=null;
        }
    }
}
