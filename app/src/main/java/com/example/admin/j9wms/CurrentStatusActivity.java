package com.example.admin.j9wms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.zxing.client.android.CaptureActivity;

/**
 * Created by admin on 2015/4/27.
 */
public class CurrentStatusActivity extends Activity {

    private Button btn_findMe,btn_foundDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currentstatus);
        btn_findMe = (Button)findViewById(R.id.btn_findMe);
        btn_foundDB= (Button)findViewById(R.id.btn_foundDB);

        btn_findMe.setOnClickListener(new MyListener());
        btn_foundDB.setOnClickListener(new MyListener());

    }
    //统一点击操作
    private class MyListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {

                case R.id.btn_findMe://定位

                    break;
                case R.id.btn_foundDB://建立指纹数据库
                    Intent intent2 = new Intent(CurrentStatusActivity.this,BuildDB.class);
                    finish();
                    startActivity(intent2);

                    break;

                default:
                    break;
            }
        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CurrentStatusActivity.this,IndexActivity.class);
        finish();
        startActivity(intent);
    }
}
