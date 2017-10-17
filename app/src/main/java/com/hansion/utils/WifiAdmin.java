package com.hansion.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.hansion.App;

import java.util.List;

/**
 * Description：Wifi处理工具类
 * Author: Hansion
 * Time: 2016/10/9 10:27
 */
public class WifiAdmin {

    private static WifiAdmin wifiAdmin;
    private Context context;
    // 定义WifiManager对象
    private WifiManager mWifiManager;
    // 定义WifiInfo对象
    private WifiInfo mWifiInfo;
    // 扫描出的网络连接列表
    private List<ScanResult> mWifiList;
    // 网络连接列表
    private List<WifiConfiguration> mWifiConfiguration;

    // 构造
    public WifiAdmin(Context context) {
        this.context = context;
        // 取得WifiManager对象
        mWifiManager = (WifiManager) App.getAppContext().getSystemService(Context.WIFI_SERVICE);
        // 取得WifiInfo对象
        mWifiInfo = mWifiManager.getConnectionInfo();
    }

    /**
     * 获取BleController实例对象（☆）
     *
     * @param context
     * @return
     */
    public synchronized static WifiAdmin getInstance(Context context) {
        if (null == wifiAdmin) {
            wifiAdmin = new WifiAdmin(context);
        }
        return wifiAdmin;
    }

    //wifi是否开启
    public boolean isWifiOpen() {
        return mWifiManager.isWifiEnabled();
    }

    // 打开WIFI
    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    // 关闭WIFI
    public void closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    //开始扫描
    private void startScan() {
        mWifiManager.startScan();
        // 得到扫描结果
        mWifiList = mWifiManager.getScanResults();
        // 得到配置好的网络连接
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();
    }

    // 得到网络列表
    public List<ScanResult> getWifiList() {
        startScan();
        return mWifiList;
    }

    // 得到曾经连接过的网络集合
    public List<WifiConfiguration> getConfiguration() {
        if (mWifiConfiguration != null) {
            mWifiConfiguration.clear();
        }
        startScan();
        return mWifiConfiguration;
    }

    public void connectConfigurationOfSsid(String ssid) {
        if (mWifiConfiguration == null) {
            mWifiConfiguration = getConfiguration();
        }
        ssid = "\"" + ssid + "\"";
        for (int i = 0; i < mWifiConfiguration.size(); i++) {
            if (ssid.equals(mWifiConfiguration.get(i).SSID)) {
                connectConfiguration(i);
                LogUtil.e("正在连接：" + mWifiConfiguration.get(i).SSID);
            }
        }

    }


    // 指定配置好的网络进行连接
    public void connectConfiguration(final int index) {
        // 索引大于配置好的网络索引返回
        if (index > mWifiConfiguration.size()) {
            LogUtil.e("索引太大");
            return;
        }

        // 连接配置好的指定ID的网络
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId, true);
//        Method connectMethod = connectWifiByReflectMethod(mWifiConfiguration.get(index).networkId);
//        if (connectMethod == null) {
//            LogUtil.e(  "connect wifi by enableNetwork method, Add by jiangping.li");
//            // 通用API
//            mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId, true);
//        }
    }

//    /**
//     * 通过反射出不同版本的connect方法来连接Wifi
//     *
//     * @param netId
//     * @return
//     * @since MT 1.0
//     *
//     */
//    private Method connectWifiByReflectMethod(int netId) {
//        Method connectMethod = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            LogUtil.e( "connectWifiByReflectMethod road 1");
//            // 反射方法： connect(int, listener) , 4.2 <= phone‘s android version
//            for (Method methodSub : mWifiManager.getClass() .getDeclaredMethods()) {
//                if ("connect".equalsIgnoreCase(methodSub.getName())) {
//                    Class<?>[] types = methodSub.getParameterTypes();
//                    if (types != null && types.length > 0) {
//                        if ("int".equalsIgnoreCase(types[0].getName())) {
//                            connectMethod = methodSub;
//                        }
//                    }
//                }
//            }
//            if (connectMethod != null) {
//                try {
//                    connectMethod.invoke(mWifiManager, netId, null);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    LogUtil.e( "connectWifiByReflectMethod Android "
//                            + Build.VERSION.SDK_INT + " error!");
//                    return null;
//                }
//            }
//        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
//            // 反射方法: connect(Channel c, int networkId, ActionListener listener)
//            // 暂时不处理4.1的情况 , 4.1 == phone‘s android version
//            LogUtil.e(  "connectWifiByReflectMethod road 2");
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
//                && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
//            LogUtil.e( "connectWifiByReflectMethod road 3");
//            // 反射方法：connectNetwork(int networkId) ,
//            // 4.0 <= phone‘s android version < 4.1
//            for (Method methodSub : mWifiManager.getClass() .getDeclaredMethods()) {
//                if ("connectNetwork".equalsIgnoreCase(methodSub.getName())) {
//                    Class<?>[] types = methodSub.getParameterTypes();
//                    if (types != null && types.length > 0) {
//                        if ("int".equalsIgnoreCase(types[0].getName())) {
//                            connectMethod = methodSub;
//                        }
//                    }
//                }
//            }
//            if (connectMethod != null) {
//                try {
//                    connectMethod.invoke(mWifiManager, netId);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    LogUtil.e( "connectWifiByReflectMethod Android " + Build.VERSION.SDK_INT + " error!");
//                    return null;
//                }
//            }
//        } else {
//            // < android 4.0
//            return null;
//        }
//        return connectMethod;
//    }

    // 得到当前连接的SSID(每次去getSSID的时候需要获得最新的wifiInfo对象)
    public String getSSID() {
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        if (mWifiInfo == null && !isConnected()) {
            return "NULL";
        } else {
            if (mWifiInfo.getSSID().length() > 2 && mWifiInfo.getSSID().contains("\"")) {
                return mWifiInfo.getSSID().split("\"")[1];
            }
        }
        return "NULL";
    }

    // 得到IP地址
    public String getIPAddress() {
        String s = null;
        if (mWifiInfo != null) {
            int ipAddress = mWifiInfo.getIpAddress();
            s = intToIp(ipAddress);
        }
        return s;
    }

    // 得到MAC地址
    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    // 得到接入点的BSSID
    public String getBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    // 得到连接的ID
    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    // 断开网络
    public void disconnectWifi() {
        mWifiManager.disableNetwork(mWifiManager.getConnectionInfo().getNetworkId());
        mWifiManager.disconnect();
    }

    //转换成正常的IP
    private String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    /**
     * 判断是否连接Wifi
     *
     * @return
     */
    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED)
            return true;
        return false;
    }
}
