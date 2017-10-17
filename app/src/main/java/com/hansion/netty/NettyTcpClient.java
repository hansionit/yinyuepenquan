package com.hansion.netty;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;

import com.hansion.App;
import com.hansion.utils.LogUtil;
import com.hansion.utils.NetworkUtils;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Description：
 * Author: Hansion
 * Time: 2017/7/13 15:10
 */
public class NettyTcpClient implements INettyTcpClient {

    private final int MESSAGE_INIT = 0x1;
    private final int MESSAGE_CONNECT = 0x2;
    private final int MESSAGE_SEND = 0x3;
    private final String ACTION_SEND_MSG = "action_send_msg";


    private String serverIP;
    private int serverPort;
    private boolean needReconnect;
    private long delay;

    private static NettyTcpClient mInstance;
    private HandlerThread workThread = null;
    private Handler mWorkHandler = null;
    private NettyClientHandler nettyClientHandler;
    private Bootstrap bootstrap;
    private NioEventLoopGroup group;
    private Channel channel;


    public synchronized static NettyTcpClient getInstance() {
        if (mInstance == null) {
            mInstance = new NettyTcpClient();
        }
        return mInstance;
    }

    public NettyTcpClient() {
        init();
    }

    private void init() {
        workThread = new HandlerThread(NettyTcpClient.class.getName());
        workThread.start();
        mWorkHandler = new Handler(workThread.getLooper(), mWorkHandlerCallback);
        nettyClientHandler = new NettyClientHandler();
        nettyClientHandler.addConnectStatusListener(new OnConnectStatusListener() {
            @Override
            public void onDisconnected(boolean isBreakByMyself) {
                if (!isBreakByMyself && needReconnect) {
                    sendReconnectMessage();
                }
            }

            @Override
            public void onTcpConnect() {

            }
        });
        mWorkHandler.sendEmptyMessage(MESSAGE_INIT);
    }


    @Override
    public void connect(String ip, int port) {
        connect(ip, port, false, -1);
    }

    @Override
    public void connect(String ip, int port, boolean needReconnect, long delay) {
        serverIP = ip;
        serverPort = port;
        this.needReconnect = needReconnect;
        this.delay = delay;
        mWorkHandler.sendEmptyMessage(MESSAGE_CONNECT);
    }

    @Override
    public void sendMessage(byte[] msg) {
        if (msg == null || msg.length <= 0) {
            return;
        }
        Message message = new Message();
        Bundle bundle = new Bundle();
        message.what = MESSAGE_SEND;
        bundle.putByteArray(ACTION_SEND_MSG, msg);
        message.setData(bundle);
        mWorkHandler.sendMessage(message);
    }

    @Override
    public void disconnect() {
        if (channel != null) {
            if (nettyClientHandler != null) {
                nettyClientHandler.setBreakByMyself(true);
            }
            channel.disconnect();
        }
    }

    @Override
    public void addDataReceiveListener(OnDataReceiveListener listener) {
        if (nettyClientHandler != null) {
            nettyClientHandler.addDataReceiveListener(listener);
        }
    }

    @Override
    public void removeAllDataReceiveListener() {
        if (nettyClientHandler != null) {
            nettyClientHandler.removeAllDataReceiveListener();
        }
    }

    @Override
    public void addOnDisconnected(OnConnectStatusListener listener) {
        if (nettyClientHandler != null) {
            nettyClientHandler.addConnectStatusListener(listener);
        }
    }

    @Override
    public boolean isContainsConnectStatusListener(OnConnectStatusListener listener) {
        if (nettyClientHandler != null) {
           return nettyClientHandler.isContainsConnectStatusListener(listener);
        }
        return false;
    }

    @Override
    public boolean isConnected() {
        if (nettyClientHandler != null) {
            return nettyClientHandler.isConnected();
        }
        return false;
    }

    public void checkIsConnectedPerfect(final ConnectedPerfectListener connectedPerfectListener) {
        LogUtil.e("检测TCP");
        new Thread(new Runnable() {
            @Override
            public void run() {
                int status = 0;
                int time = 3000;
                while (time > 0) {
                    if (!isConnected()) {
                        status++;
                        LogUtil.e(status + "次断开");
                    }
                    SystemClock.sleep(300);
                    time -= 300;
                }
                connectedPerfectListener.isConnectedPerfect(status < 6);
            }
        }).start();
    }

    public interface ConnectedPerfectListener {
        void isConnectedPerfect(boolean isPerfect);
    }


    public void sendReconnectMessage() {
        if (mWorkHandler != null && needReconnect && delay > 0) {
            mWorkHandler.sendEmptyMessageDelayed(MESSAGE_CONNECT, delay);
        }
    }

    private Handler.Callback mWorkHandlerCallback = new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_INIT: {
                    group = new NioEventLoopGroup();
                    bootstrap = new Bootstrap();
                    bootstrap.channel(NioSocketChannel.class);
                    bootstrap.group(group);
                    bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(nettyClientHandler);
                        }
                    });
                    break;
                }
                case MESSAGE_CONNECT: {
                    if (!NetworkUtils.isWifi(App.getAppContext())) {
                        if (mWorkHandler != null && needReconnect && delay > 0) {
                            LogUtil.i("WIFI未连接 跳过本次重连");
                            mWorkHandler.sendEmptyMessageDelayed(MESSAGE_CONNECT, delay * 2);
                        }
                        break;
                    }

                    try {
                        if (TextUtils.isEmpty(serverIP) || serverPort == 0) {
                            Exception exception = new Exception("Netty host | port is invalid");
                            throw exception;
                        }
                        if (nettyClientHandler != null) {
                            nettyClientHandler.setBreakByMyself(false);
                        }
                        if (channel != null && channel.isActive()) {
                            LogUtil.i("当前已经连接");
                            break;
                        }

                        LogUtil.i("正在连接");

                        ChannelFuture future = bootstrap.connect(new InetSocketAddress(serverIP, serverPort));
                        future.addListener(new ChannelFutureListener() {
                            public void operationComplete(ChannelFuture futureListener) throws Exception {
                                if (futureListener.isSuccess()) {
                                    channel = futureListener.channel();
                                } else if (needReconnect) {
                                    sendReconnectMessage();
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendReconnectMessage();
                    }
                    break;
                }
                case MESSAGE_SEND: {
                    byte[] sendMsg = msg.getData().getByteArray(ACTION_SEND_MSG);
                    if (channel != null && channel.isActive()) {
                        ByteBuf buf = channel.alloc().buffer(sendMsg.length);
                        buf.writeBytes(sendMsg);
                        channel.writeAndFlush(buf);
//                        LogUtil.e("发送："+ DataTransUtils.bytesToHexString(sendMsg));
                    }
                    break;
                }
            }
            return true;
        }
    };
}
