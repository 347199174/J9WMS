package com.example.admin.j9wms;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.admin.wifi.WifiRss;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2015/4/27.
 */
public class BuildDB extends Activity {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    //SharedPreferences中储存数据的路径
    public final static String DATA_URL = "/data/data/";
    public final static String SHARED_MAIN_XML = "WifiInfo.xml";
    //private ScanResult scanResult;
    private List<ScanResult> list;
    private Button mem, refresh, scan, clear;
    private EditText editText, refl, last;
    private com.example.admin.wifi.WifiAdmin wifiAdmin;
    private StringBuffer stringBuffer = new StringBuffer();
    //定义静态变量观测点号
    private static int point_num = 1;
    //刷新操作次数
    private static int r_num = 0;
    //定义对象动态数组 存储BSSID、RSSI
    private ArrayList<WifiRss> wifiRss = new ArrayList<>();

    private boolean scanClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_builddb);

        //初始化
        wifiAdmin = new com.example.admin.wifi.WifiAdmin(BuildDB.this);
        sharedPreferences = this.getSharedPreferences("WifiInfo", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        refl = (EditText) findViewById(R.id.refl);
        last = (EditText) findViewById(R.id.last);
        mem = (Button) findViewById(R.id.mem);
        scan = (Button) findViewById(R.id.scan);
        refresh = (Button) findViewById(R.id.refresh);
        clear = (Button) findViewById(R.id.clear);
        editText = (EditText) findViewById(R.id.show);
        mem.setOnClickListener(new MyListener());
        scan.setOnClickListener(new MyListener());
        refresh.setOnClickListener(new MyListener());
        clear.setOnClickListener(new MyListener());

    }

    //统一点击操作
    private class MyListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {

                case R.id.scan://扫描网络
                    getAllNetWorkList();
                    break;
                case R.id.mem://存储数据
                    storeWifi();
                    Toast.makeText(BuildDB.this, "存储完成", Toast.LENGTH_LONG).show();
                    showStore();
                    break;
                case R.id.refresh://刷新数据
                    refWifi();
                    break;
                case R.id.clear://重置数据
                    clearAll();
                    break;
                default:
                    break;
            }
        }

    }
    //重置数据
    public void clearAll() {
        point_num = 1;//重新从第一观测点开始
        scanClicked = false;
        File file = new File(DATA_URL + getPackageName().toString()
                + "/shared_prefs", SHARED_MAIN_XML);
        if (file.exists()) {
            file.delete();
            Toast.makeText(BuildDB.this, "删除SharedPreferences文件成功", Toast.LENGTH_LONG).show();
            editText.setText("");
            refl.setText("");
            last.setText("");
            sharedPreferences.getAll().clear();
            wifiRss.clear();

        }
        Test();

    }

    public void Test() {
        StringBuffer stringBuffer1 = new StringBuffer();
        if (stringBuffer1 != null) {
            stringBuffer1 = new StringBuffer();
            for (int k = 0; k < wifiRss.size(); k++) {
                stringBuffer1.append("\nBSSID---->>>>:" + wifiRss.get(k).getW_BSSID());
                stringBuffer1.append("\nRSSI---->>>>:" + wifiRss.get(k).getW_RSSI());
                stringBuffer1.append("\nLIST_SIZE---->>>>" + list.size() + "\nWIFI_SIZE---->>>>" + wifiRss.size());
                stringBuffer1.append("\nk========" + k);
                editText.setText(stringBuffer1.toString());
            }
        }

    }

    public void showStore()//显示存储的信息
    {
        String content = "";
        Map<String, ?> map = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            content += (entry.getKey() + "为：" + entry.getValue() + "\n");
        }

        last.setText(content);
    }

    public void refWifi()//刷新
    {
        if (scanClicked) {
            if (r_num < 9) {

                if (stringBuffer != null) {
                    stringBuffer = new StringBuffer();
                }
                wifiAdmin.startScan();
                list = wifiAdmin.getWifiList();
                stringBuffer = wifiAdmin.lookUpScan();
                r_num++;
                editText.setText("扫描到的wifi网络" + r_num + "：\n" + stringBuffer.toString());
                for (int i = 0; i < wifiRss.size(); i++) {
                    for (int j = 0; j < list.size(); j++) {
                        if (wifiRss.get(i).getW_BSSID().equals(list.get(j).BSSID)) {
                            int t = wifiRss.get(i).getW_RSSI();
                            t += list.get(j).level;
                            wifiRss.get(i).setW_RSSI(t);
                        }
                    }
                }
            } else {
                //显示超过10次
                Toast.makeText(BuildDB.this, "超过10次", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(BuildDB.this, "请先扫描", Toast.LENGTH_LONG).show();
        }
    }

    public int getRSSI(int k) {
        int sum = 0;
        int lastRssi = 0;

        sum = wifiRss.get(k).getW_RSSI();
        lastRssi = sum / 10;
        return lastRssi;
    }

    //存储WiFi信息
    public void storeWifi() {
        if (r_num == 9) //测试10组才能存储
        {
            int router_num = 1;//记录每个观测点的路由器号
            for (int i = 0; i < wifiRss.size(); i++) {
                editor.putString("第" + point_num + "观测点、" + "路由器" + router_num + ":\n" + "BSSID", wifiRss.get(i).getW_BSSID());
                editor.putInt("第" + point_num + "观测点、" + "路由器" + router_num + ":\n" + "RSSI", getRSSI(i));
                editor.commit();
                router_num++;
            }
            point_num++;
            r_num = 0;//置0，防止再按一次继续存储相同数据
            scanClicked = false;
        } else {
            //Toast.makeText(MainActivity.this, "测试数据太少", Toast.LENGTH_LONG).show();
        }

    }
     //获取所有无线信息
    public void getAllNetWorkList() {
        // 每次点击扫描之前清空上一次的扫描结果
        if (stringBuffer != null) {
            stringBuffer = new StringBuffer();
        }
        //清除缓存数据
        wifiRss.clear();
        r_num = 0;
        //记录scan是否点击
        scanClicked = true;
        //开始扫描网络
        wifiAdmin.startScan();
        list = wifiAdmin.getWifiList();
        stringBuffer = wifiAdmin.lookUpScan();
        editText.setText("扫描到的wifi网络：\n" + stringBuffer.toString());
        StringBuffer stringBuffer1 = new StringBuffer();
        if (stringBuffer1 != null) {
            stringBuffer1 = new StringBuffer();

            //往对象数组中添加信号强度大于-80的
            for (int i = 0; i < list.size(); i++) {

                if (Math.abs(list.get(i).level) <= 80) {
                    //每次添加都要初始化，不然会覆盖
                    WifiRss wifiRss1 = new WifiRss("null", 0);
                    String BSSID = list.get(i).BSSID;
                    int RSSI = list.get(i).level;
                    wifiRss1.setW_RSSI(RSSI);
                    wifiRss1.setW_BSSID(BSSID);
                    wifiRss.add(wifiRss1);
                }
            }
            for (int k = 0; k < wifiRss.size(); k++) {
                stringBuffer1.append("\nBSSID---->>>>:" + wifiRss.get(k).getW_BSSID());
                stringBuffer1.append("\nRSSI---->>>>:" + wifiRss.get(k).getW_RSSI());
                stringBuffer1.append("\nLIST_SIZE---->>>>" + list.size() + "\nWIFI_SIZE---->>>>" + wifiRss.size());
                stringBuffer1.append("\nk========" + k);
                refl.setText(stringBuffer1.toString());
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(BuildDB.this,CurrentStatusActivity.class);
        finish();
        startActivity(intent);
    }
}
