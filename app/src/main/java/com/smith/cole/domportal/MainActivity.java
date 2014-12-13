package com.smith.cole.domportal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import Service.CheckForUpdate;
import Service.GetLocalIP;
import Service.Login;
import Service.LoginStatusRegex;
import Service.MyHttpClient;
import Service.StoreOrGetData;


public class MainActivity extends ActionBarActivity {

    private final String VERSION = CheckForUpdate.VERSION;
    private Handler checkForUpdateHandler;

    private Switch loginStatus;
    private boolean alreadyLogin;
    private boolean loginStatusChange;
    private Handler loginStatusHandler;
    private Handler logoutStatusHandler;
    private Switch save;

    private EditText username;
    private EditText password;
    private EditText captureCode;

    private ImageView captureImage;

    private TextView error;
    private TextView ip;

    private TextView checkForUpdate;

    // detect handler
    private Handler actDetectNetHandler;
    private int actDetectNetCount;

    // login handler
    private MyHttpClient loginHttp;
    private Handler captureImageHandler;

    // Store Or Get Data
    private StoreOrGetData dataImplement;

    /**
     * Initalize widgets
     * */
    private void init() {

        loginStatus = (Switch)findViewById(R.id.loginStatus);
        alreadyLogin = false;
        loginStatus.setChecked(false);
        loginStatusChange = false;
        save = (Switch)findViewById(R.id.save);

        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        captureCode = (EditText)findViewById(R.id.captureCode);
        captureCode.setHint(R.string.capturetips);

        captureImage = (ImageView)findViewById(R.id.captureImage);

        error = (TextView)findViewById(R.id.error);
        ip = (TextView)findViewById(R.id.ip);

        ip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, GetLocalIP.getLocalIpAddress(MainActivity.this), Toast.LENGTH_SHORT).show();
            }
        });

        checkForUpdate = (TextView)findViewById(R.id.checkForUpdate);
        checkForUpdate.setText("Version " + VERSION + "(检查更新)");
        checkForUpdateHandler = new CheckForUpdateHandler();
        checkForUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("QDU_EDU_CN Client")
                        .setMessage("检查更新?")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(MainActivity.this, "检查更新中...", Toast.LENGTH_SHORT).show();
                                new CheckForUpdateThread(1).start();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(MainActivity.this, "取消检查更新", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        });

        loginHttp = new MyHttpClient();
        actDetectNetHandler = new actDetectNetHandlerClass();
        actDetectNetCount = -1;
        captureImageHandler = new FetchCaptureImageHandler();

        // 默认断开
        loginStatus.setText(R.string.off);

        // dataImplement
        dataImplement = new StoreOrGetData(this);
        setWidgetsInfo();

        loginStatusHandler = new LoginStatusHandler();
        logoutStatusHandler = new LogoutStatusHandler();

        // take time
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.obj = Login.detectNetwork();
                msg.what = 3;
                actDetectNetHandler.sendMessage(msg);
            }
        };
        Timer timer = new Timer(true);
        timer.schedule(task, 1000, 3000);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize widgets
        init();

        // CheckNet
        actDetectNet();

        loginStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                loginStatusChange = true;
                // 保存数据
                if (save.isChecked()) {
                    dataImplement.store_username_password(getWidgetsInfo());
                }

                // 登出
                if (alreadyLogin && ! isChecked) {

                    new Thread() {
                        @Override
                        public void run() {
                            Message msg = new Message();
                            msg.obj = LoginStatusRegex.logout(Login.logout(loginHttp));
                            logoutStatusHandler.sendMessage(msg);

                        }
                    }.start();
                    return ;
                }

                // 登录
                // ...
                // HashMap<String, String> data = getWidgetsInfo();
                new Thread() {
                    @Override
                    public void run() {
                        Message msg = new Message();

                        HashMap<String, String> data = getWidgetsInfo();
                        String u = data.get("username");
                        String p = data.get("password");
                        String c = data.get("captureCode");


                        if (u.isEmpty() || p.isEmpty() || c.isEmpty()) {
                            msg.what = 1;
                            loginStatusHandler.sendMessage(msg);
                            return ;
                        }

                        String errorLog = Login.loginPortal(loginHttp, u, p, c);
                        msg.what = LoginStatusRegex.isSuccessLogin(errorLog);
                        msg.obj = LoginStatusRegex.loginInfo(errorLog);
                        loginStatusHandler.sendMessage(msg);
                    }
                }.start();
            }
        });

        save.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (! isChecked) {
                    dataImplement.store_username_password("false");
                    return ;
                }
                // 保存数据
                dataImplement.store_username_password(getWidgetsInfo());
            }
        });

        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchCaptureImage();
            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("QDU_EDU_CN Client")
                .setMessage("确认退出?")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.super.onBackPressed();
                        // kill this process
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).show();
    }


    public class CheckForUpdateThread extends Thread {
        private int choice = 1;

        public CheckForUpdateThread() {}

        public CheckForUpdateThread(int value) {
            choice = value;
        }

        @Override
        public void run() {
            Message msg = new Message();
            msg.what = choice;
            if (choice == 1) {// 检测更新
                msg.obj = CheckForUpdate.check();
            }
            else if (choice == 2) {
                msg.obj = CheckForUpdate.update();
            }
            checkForUpdateHandler.sendMessage(msg);
        }
    }

    public class CheckForUpdateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 1) {
                String message = (String)msg.obj;
                if (message == null) {
                    Toast.makeText(MainActivity.this, "检查失败,软件出错", Toast.LENGTH_SHORT).show();
                }


                if (message.contains("检测到新版本")) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("QDU_EDU_CN Client")
                            .setMessage(message + ", 是否更新?")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(MainActivity.this, "更新中...", Toast.LENGTH_SHORT).show();
                                    new CheckForUpdateThread(2).start();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(MainActivity.this, "取消更新", Toast.LENGTH_SHORT).show();
                                }
                            }).show();
                } else {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            } else if (msg.what == 2) {
                installAPK((File)msg.obj);
            }
        }
    }

    public void installAPK(File apk) {
        // TODO Auto-generated method stub
        Log.e("OpenFile", apk.getName());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apk),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }

    class LoginStatusHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                Toast.makeText(getApplicationContext(), "您已经登陆成功!", Toast.LENGTH_SHORT).show();
                alreadyLogin = true;
                loginStatus.setChecked(true);
                loginStatus.setText(R.string.on);

                ip.setText(GetLocalIP.getLocalIpAddress(MainActivity.this));
                return ;
            }

            if (!alreadyLogin && msg.what == 1) {
                Toast.makeText(getApplicationContext(), "用户名 密码 验证码均不能为空", Toast.LENGTH_SHORT).show();
                loginStatus.setChecked(false);
                return ;
            }

            if (!alreadyLogin && msg.what < 0) {
                String message = String.valueOf(msg.obj);

                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                loginStatus.setChecked(false);
                loginStatus.setText(R.string.off);
                return ;
            }
        }
    }

    class LogoutStatusHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj == null) {
                Toast.makeText(getApplicationContext(), "value null", Toast.LENGTH_SHORT).show();
                return ;
            }
            alreadyLogin = false;
            Toast.makeText(getApplicationContext(), String.valueOf(msg.obj) + "\n5秒后方可重新登陆!", Toast.LENGTH_LONG).show();
            captureCode.setHint(R.string.capturetips);
            loginStatus.setText(R.string.off);
            /*
            loginStatus.setEnabled(false);
            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                ;
            } finally {
                loginStatus.setEnabled(true);
            }*/
        }
    }

    //  activity detect network main func
    public void actDetectNet () {
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();
                boolean status =  Login.detectNetwork();
                if (! status) {
                    msg.what = -1;
                    actDetectNetHandler.sendMessage(msg);
                    return ;
                }

                int statusInt = Login.loginSuccess(loginHttp);
                if (statusInt != 0) {
                    msg.what = 1;
                    Login.get(loginHttp);
                    msg.obj = Login.getImg(loginHttp);
                    actDetectNetHandler.sendMessage(msg);
                    return ;
                }

                msg.what = 0;
                actDetectNetHandler.sendMessage(msg);
            }
        }.start();
    }

    /**
     * activity detect network handler class
     */
    class actDetectNetHandlerClass extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == -1) {
                Toast.makeText(getApplicationContext(), "网络连接失败,请检测是否已连接到校园网!", Toast.LENGTH_SHORT).show();
                loginStatus.setEnabled(false);
                return ;
            }

            if (msg.what == 1) {
                Toast.makeText(getApplicationContext(), "已检测到校园网!", Toast.LENGTH_SHORT).show();
                captureImage.setImageBitmap((Bitmap) msg.obj);
                return ;
            }


            if (msg.what == 3) {
                if (! (boolean)msg.obj) {
                    // Toast.makeText(getApplicationContext(), "网络连接失败,请检测是否已连接到校园网!", Toast.LENGTH_SHORT).show();
                    loginStatus.setEnabled(false);
                    return ;
                }
                // Toast.makeText(getApplicationContext(), "已检测到校园网!", Toast.LENGTH_SHORT).show();
                loginStatus.setEnabled(true);
                return ;
            }

            if (msg.what == 0) {
                Toast.makeText(getApplicationContext(), "已登陆校园网,请勿重复使用账号!", Toast.LENGTH_SHORT).show();
                alreadyLogin = true;
                loginStatus.setChecked(true);
                loginStatus.setText(R.string.on);
                captureCode.setHint(R.string.on);
                ip.setText(GetLocalIP.getLocalIpAddress(MainActivity.this));
                return ;
            }
        }
    }

    public void fetchCaptureImage() {
        new Thread() {
            @Override
            public void run() {
                Login.get(loginHttp);
                Message msg = new Message();
                msg.obj = Login.getImg(loginHttp);
                captureImageHandler.sendMessage(msg);
            }
        }.start();
    }

    class FetchCaptureImageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            captureImage.setImageBitmap((Bitmap) msg.obj);
        }
    }

    public HashMap<String, String> getWidgetsInfo() {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("username", username.getText().toString().trim());
        data.put("password", password.getText().toString().trim());
        data.put("save", String.valueOf(save.isChecked()));
        data.put("captureCode", captureCode.getText().toString().trim());

        return data;
    }

    public void setWidgetsInfo () {
        HashMap<String, String> data = dataImplement.get_username_password();
        // System.out.println("~~~~~~~~~~~set: " + data.get("save").equals("true"));

        if (data.get("save").equals("false")) {;
            return ;
        }

        username.setText(data.get("username"));
        password.setText(data.get("password"));
        save.setChecked(data.get("save").equals("true"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
