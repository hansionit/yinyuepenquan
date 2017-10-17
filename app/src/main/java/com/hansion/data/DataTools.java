package com.hansion.data;


import com.hansion.App;
import com.hansion.netty.INettyTcpClient;
import com.hansion.netty.NettyTcpClient;
import com.hansion.utils.LogUtil;
import com.hansion.utils.NetworkUtils;

import static com.hansion.Constants.IP;
import static com.hansion.Constants.PORT;

/**
 * Description：
 * Author: Hansion
 * Time: 2017/4/27 11:20
 */
public class DataTools {

    private static volatile DataTools instance;
    private NettyTcpClient nettyTcpClient;
    private NetworkUtils networkUtils;

    public static DataTools getInstance() {
//        if (instance == null) {
//            synchronized (DataTools.class) {
//                if (instance == null) {
//                    instance = new DataTools();
//                }
//            }
//        }
        if (instance == null) {
            instance = new DataTools();
        }
        return instance;
    }

    private DataTools() {
        initClient();
    }


    private void initClient() {
        initNettyTcpClient();
        networkUtils = new NetworkUtils(App.getAppContext());
    }


    public void connectServer(boolean needReconnect) {
        connectNettyTcpServer(needReconnect);
    }


    public void sendData(byte[] message) {
        if (networkUtils.isConnectHaisi()) {
            sendDataFromTcp(message);
        } else {
            LogUtil.e("WIFI连接不正确 不发送");
        }
    }

    //---------------------------------- Netty TCP --------------------------------------------

    public void initNettyTcpClient() {
        if (nettyTcpClient != null) {
            return;
        }
        nettyTcpClient = NettyTcpClient.getInstance();
    }

    private void connectNettyTcpServer(boolean needReconnect) {
        nettyTcpClient.connect(IP, PORT, needReconnect, 800);
    }

    public void setOnDataReceiveListener(INettyTcpClient.OnDataReceiveListener onDataReceiveListener) {
        if (nettyTcpClient == null) {
            return;
        }
        nettyTcpClient.addDataReceiveListener(onDataReceiveListener);
    }

    public void setOnConnectStatusListener(INettyTcpClient.OnConnectStatusListener onConnectStatusListener) {
        if (nettyTcpClient == null) {
            return;
        }
        nettyTcpClient.addOnDisconnected(onConnectStatusListener);
    }
    public boolean isContainsConnectStatusListener(INettyTcpClient.OnConnectStatusListener onConnectStatusListener) {
        if (nettyTcpClient == null) {
            return false;
        }
        return nettyTcpClient.isContainsConnectStatusListener(onConnectStatusListener);
    }

    public void removeAllDataReceiveListener() {
        if (nettyTcpClient == null) {
            return;
        }
        nettyTcpClient.removeAllDataReceiveListener();
    }


    //---------------------------------- TCP --------------------------------------------

    /**
     * TCP是否断开连接
     *
     * @return
     */
    public boolean isTcpDisconnected() {
        if (nettyTcpClient != null) {
            return !nettyTcpClient.isConnected();
        }
        return false;
    }

    /**
     * TCP是否已经稳定连接
     *
     * @return
     */
    public void isTcpConnectedPerfect(NettyTcpClient.ConnectedPerfectListener connectedPerfectListener) {
        if (nettyTcpClient != null) {
            nettyTcpClient.checkIsConnectedPerfect(connectedPerfectListener);
        }
    }



    public void disconnectTcp() {
        if (nettyTcpClient != null) {
            nettyTcpClient.disconnect();
        }
    }

    private void sendDataFromTcp(byte[] message) {
        if (nettyTcpClient != null) {
            nettyTcpClient.sendMessage(message);
        }

    }

}
