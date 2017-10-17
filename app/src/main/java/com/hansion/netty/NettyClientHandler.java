package com.hansion.netty;

import android.os.Handler;
import android.os.Looper;

import com.hansion.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Description：
 * Author: Hansion
 * Time: 2017/7/13 15:15
 */
@ChannelHandler.Sharable
public class NettyClientHandler extends ChannelInboundHandlerAdapter {


    private boolean isConnected;
    private boolean isBreakByMyself;
    private List<INettyTcpClient.OnDataReceiveListener> reciveListeners = new ArrayList<>();
    private List<INettyTcpClient.OnConnectStatusListener> connectStatusListeners = new ArrayList<>();


    public void setBreakByMyself(boolean isBreakByMyself) {
        this.isBreakByMyself = isBreakByMyself;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //建立连接成功
        super.channelActive(ctx);
        isConnected = true;
        LogUtil.i("建立TCP连接成功");
        notifiConnectedListeners();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //收到数据
//        super.channelRead(ctx, msg);
        ByteBuf buf = (ByteBuf) msg;
        byte[] recive = new byte[buf.readableBytes()];
        buf.readBytes(recive);
        notifiReciveListeners(recive);

    }

    /**
     * 将byte数组转为16进制字符串
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv.toUpperCase()).append(" ");
        }
        return stringBuilder.toString();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //Netty抛出异常
//        super.exceptionCaught(ctx, cause);
        ctx.close();
        if(isConnected) {
            isConnected = false;
            notifiBreakConnListeners(false);
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //服务端主动触发的断开连接
//        super.channelInactive(ctx);
        if(isConnected) {
            isConnected = false;
            notifiBreakConnListeners(isBreakByMyself);
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    public boolean isConnected() {
        return isConnected;
    }


    //接收到数据,通知所有监听
    private void notifiReciveListeners(final byte[] recive) {
        for (final INettyTcpClient.OnDataReceiveListener listener : reciveListeners)
            if (listener != null)
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {//主线程中进行
                        listener.onDataReceive(recive);
                    }
                });
    }

    //连接成功,通知所有监听
    private void notifiConnectedListeners() {
        for (final INettyTcpClient.OnConnectStatusListener listener : connectStatusListeners)
            if (listener != null)
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {//主线程中进行
                        listener.onTcpConnect();
                    }
                });
    }


    //断开连接,通知所有监听
    private void notifiBreakConnListeners(final boolean isBreakByMyself) {
        if(isBreakByMyself) {
            LogUtil.i("手动断开TCP连接成功");
        } else {
            LogUtil.i("TCP 断开连接");
        }

        for (final INettyTcpClient.OnConnectStatusListener listener : connectStatusListeners)
            if (listener != null)
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {//主线程中进行
                        listener.onDisconnected(isBreakByMyself);
                    }
                });
    }

    public void removeAllDataReceiveListener() {
        reciveListeners.clear();
    }

    public boolean isContainsConnectStatusListener(INettyTcpClient.OnConnectStatusListener listener) {
       return connectStatusListeners.contains(listener);
    }


    //绑定OnDataReceiveListener
    public void addDataReceiveListener(INettyTcpClient.OnDataReceiveListener listener) {
        if (!reciveListeners.contains(listener)) {
            reciveListeners.add(listener);
        }
    }

    //绑定OnConnectStatusListener
    public void addConnectStatusListener(INettyTcpClient.OnConnectStatusListener listener) {
        if (!connectStatusListeners.contains(listener)) {
            connectStatusListeners.add(listener);
        }
    }
}
