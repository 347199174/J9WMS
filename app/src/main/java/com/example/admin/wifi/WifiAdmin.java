package com.example.admin.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.List;

/**
 * Created by admin on 2015/4/18.
 */
public class WifiAdmin {
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    //扫描出的网络连接列表
    private List<ScanResult> wifiList;
    //网络连接列表
    private List<WifiConfiguration> wifiConfigurations;
    WifiManager.WifiLock wifiLock;
    public WifiAdmin(Context context)
    {
        wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
    }
    //Open Wifi
    public void openWifi()
    {
        if(!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);
    }
    //Close Wifi
    public  void closeWifi()
    {
        if (wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(false);
    }
    //Check State
    public int checkState()
    {
        return wifiManager.getWifiState();
    }
    //Lock Wifi
    public void acquireWifiLock()
    {
        wifiLock.acquire();
    }
    //Unlock Wifi
    public void releaseWifiLock()
    {
        //判断是否锁定
        if (wifiLock.isHeld())
            wifiLock.acquire();
    }
    //Create a WifiLock
    public  void createWifiLock()
    {
        wifiLock = wifiManager.createWifiLock("test");
    }
    //得到配置好的网络
    public List<WifiConfiguration> getWifiConfigurations()
    {
        return wifiConfigurations;
    }
    //指定配置好的网络进行连接
    public void connetionConfiguration(int index){
        if(index>wifiConfigurations.size()){
            return ;
        }
        //连接配置好指定ID的网络
        wifiManager.enableNetwork(wifiConfigurations.get(index).networkId, true);
    }

    public void startScan()
    {
        wifiManager.startScan();
        wifiList = wifiManager.getScanResults();
        wifiConfigurations = wifiManager.getConfiguredNetworks();
    }
    //得到网络列表
    public List<ScanResult> getWifiList()
    {
        return wifiList;
    }
    //查看扫描结果
    public StringBuffer lookUpScan()
    {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0 ; i < wifiList.size() ; i++)
        {
            stringBuffer.append("第" + new Integer(i + 1).toString() + "个：\n");
            // 将ScanResult信息转换成一个字符串包
            // 其中包括：BSSID、SSID、capabilities、frequency、level
            //stringBuffer.append(wifiList.get(i).toString()).append("\n");
            stringBuffer.append("RSSI的值为：（" +wifiList.get(i).level+ "）\n");
            stringBuffer.append("SSID的值为：（" +wifiList.get(i).SSID+ "）\n");
            stringBuffer.append("BSSID的值为：（" +wifiList.get(i).BSSID + "）\n");

        }
        return stringBuffer;
    }
    public String getMacAddress(){
        return (wifiInfo==null)?"NULL":wifiInfo.getMacAddress();
    }
    public String getBSSID(){
        return (wifiInfo==null)?"NULL":wifiInfo.getBSSID();
    }

    public String getSSID(){
        return ((wifiInfo==null)?"NULL":wifiInfo.getSSID()).toString();
    }
    public String getRSSI(){
        return ((wifiInfo == null) ? "NULL":wifiInfo.getRssi()).toString();
    }
    public int getIpAddress(){
        return (wifiInfo==null)?0:wifiInfo.getIpAddress();
    }
    //得到连接的ID
    public int getNetWordId(){
        return (wifiInfo==null)?0:wifiInfo.getNetworkId();
    }
    //得到wifiInfo的所有信息
    public String getWifiInfo(){
        return (wifiInfo==null)?"NULL":wifiInfo.toString();
    }
    //添加一个网络并连接
    public void addNetWork(WifiConfiguration configuration){
        int wcgId=wifiManager.addNetwork(configuration);
        wifiManager.enableNetwork(wcgId, true);
    }
    //断开指定ID的网络
    public void disConnectionWifi(int netId) {
        wifiManager.disableNetwork(netId);
        wifiManager.disconnect();
    }
}
