package com.hansion.netty;

/**
 * 230
 * Description：
 * Author: Hansion
 * Time: 2017/7/13 15:08
 */
public interface INettyTcpClient {

    //建立连接
    void connect(String ip, int port);

    void connect(String ip, int port, boolean needReconnect, long delay);

    //发送消息
    void sendMessage(byte[] msg);

    //断开连接
    void disconnect();

    //设置接收数据的监听器
    void addDataReceiveListener(OnDataReceiveListener listener);

    void removeAllDataReceiveListener();

    //设置断开连接的监听器
    void addOnDisconnected(OnConnectStatusListener listener);

    boolean isContainsConnectStatusListener(INettyTcpClient.OnConnectStatusListener listener);

    //是否已经连接
    boolean isConnected();


    interface OnDataReceiveListener {
        //接收到数据时触发
        void onDataReceive(byte[] recive);
    }

    interface OnConnectStatusListener {
        //断开连接时触发
        void onDisconnected(boolean isBreakByMyself);

        //Tcp连接成功时触发
        void onTcpConnect();
    }

}
