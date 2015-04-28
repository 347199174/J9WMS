package com.example.admin.j9wms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.google.zxing.client.android.CaptureActivity;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by admin on 2015/4/27.
 */
public class IndexActivity extends Activity {

    private Button btn_stroe_scan,btn_remove_scan,btn_query,btn_baseinfo,btn_current_status;

    //监听双击退出
    private static Boolean isExit = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        btn_stroe_scan = (Button)findViewById(R.id.btn_store_scan);
        btn_remove_scan = (Button)findViewById(R.id.btn_remove_scan);
        btn_query = (Button)findViewById(R.id.btn_query);
        btn_baseinfo = (Button)findViewById(R.id.btn_baseinfo);
        btn_current_status = (Button)findViewById(R.id.btn_current_status);

        btn_stroe_scan.setOnClickListener(new MyListener());
        btn_current_status.setOnClickListener(new MyListener());
        btn_baseinfo.setOnClickListener(new MyListener());
        btn_query.setOnClickListener(new MyListener());
        btn_remove_scan.setOnClickListener(new MyListener());
    }

    //统一点击操作
    private class MyListener implements View.OnClickListener{

        Intent intent1 = new Intent(IndexActivity.this, CaptureActivity.class);
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {

                case R.id.btn_store_scan://入库扫描

                    //boolean isStore = true;
                    intent1.putExtra("isStore",true);
                    intent1.putExtra("isCargo",true);
                    finish();
                    startActivity(intent1);
                    break;
                case R.id.btn_remove_scan://出库扫描
                    intent1.putExtra("isStore",false);
                    intent1.putExtra("isCargo",true);
                    finish();
                    startActivity(intent1);
                    break;
                case R.id.btn_query://查询操作

                    finish();
                    break;
                case R.id.btn_baseinfo://基本信息

                    finish();
                    break;
                case R.id.btn_current_status://实时状态
                    Intent intent5 = new Intent(IndexActivity.this,CurrentStatusActivity.class);
                    finish();
                    startActivity(intent5);

                    break;
                default:
                    break;
            }
        }

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(IndexActivity.this);
        dialog.setTitle("退出");
        dialog.setMessage("确定退出吗？");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                IndexActivity.this.finish();
                System.exit(0);

            }
        });
        dialog.setNegativeButton("取消", null);
        dialog.create().show();
        /*//双击退出
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            finish();
            System.exit(0);
        }*/
    }
}
