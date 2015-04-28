package com.example.admin.wifi;

/**
 * Created by admin on 2015/4/18.
 */
public class WifiRss {
    private String w_BSSID;
    private int w_RSSI;
    public WifiRss(String w_BSSID, int w_RSSI)
    {
        this.w_BSSID = w_BSSID;
        this.w_RSSI = w_RSSI;
    }


    public void setW_BSSID(String w_BSSID) {
        this.w_BSSID = w_BSSID;
    }

    public void setW_RSSI(int w_RSSI) {
        this.w_RSSI = w_RSSI;
    }

    public int getW_RSSI() {
        return w_RSSI;
    }

    public String getW_BSSID() {
        return w_BSSID;
    }
}
