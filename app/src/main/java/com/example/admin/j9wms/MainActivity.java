package com.example.admin.j9wms;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {
    private String login_user,login_password;
    private EditText userName,userPassword;
    private Button submitLogin,cancelLogin;

    private String TAG = "测试----------->>>>>";
    //测试用的用户名密码
    private static final String[] LoginTest = new String[] {
            "admin:admin", "hello:world" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userName = (EditText)findViewById(R.id.userName);
        userPassword = (EditText)findViewById(R.id.userPassword);
        submitLogin = (Button)findViewById(R.id.submit_Login);
        cancelLogin = (Button)findViewById(R.id.cancel_Login);




        submitLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_user = userName.getText().toString().trim();
                login_password = userPassword.getText().toString();
                // 设置密码输入框的格式（不能为空，不能小于4位）如果格式错误重新获得焦点，并提示错误内容
                if (TextUtils.isEmpty(login_user)) {
                    userName.setError("用户名为空，请输入！");
                    userName.requestFocus();

                }else if (TextUtils.isEmpty(login_password))
                {
                    userPassword.setError("密码为空，请输入！");
                }
                else
                {

                    if (checkUser(login_user, login_password)) {
                        //验证成功

                        Toast.makeText(MainActivity.this, "验证成功", Toast.LENGTH_LONG).show();
                       // userName.setText("");
                       // userPassword.setText("");
                       // userName.requestFocus();
                        MainActivity.this.finish();
                        Intent intent = new Intent(MainActivity.this,IndexActivity.class);
                        startActivity(intent);

                    } else {

                        //验证失败
                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

                        dialog.setTitle("验证失败！");
                        dialog.setMessage("重新登录吗？");
                        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                userName.requestFocus();
                                userName.setText("");
                                userPassword.setText("");
                                MainActivity.this.recreate();

                            }
                        });
                        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //退出程序
                                //finish();
                                System.exit(0);
                            }
                        });
                        dialog.create().show();
                    }

                }
            }
        });
        cancelLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });
    }

    //checkuser
    public  boolean checkUser (String userName, String userPassword)
    {
        for (String credential : LoginTest) {//遍历数组验证自定义用户及密码
            String[] pieces = credential.split(":");//分割字符串，将密码和邮箱分离开
            Log.i(TAG,pieces[0]);
            Log.i(TAG,pieces[1]);
            if (pieces[0].equals(userName)) {
                if(pieces[1].equals(userPassword))
                {
                    return true;
                }
            }
        }
        return false;
    }

    //统一点击操作
    /*public void MyClick(View view)
    {

    }*/


}