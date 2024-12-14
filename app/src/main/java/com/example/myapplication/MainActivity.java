package com.example.myapplication;

import static android.app.Service.START_STICKY;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int SEND_SMS_PERMISSION_REQUEST_CODE = 1;
    private EditText phoneEditText, messageEditText;
    private Button sendButton;
    private MyService myService;

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            // 根據 intent 的 action 來決定呼叫哪個方法
            if ("com.example.MY_ACTION".equals(intent.getAction())) {
                func();
            }
        }
        return START_STICKY; // 返回該服務的啟動方式
    }

    // 你的 func() 方法
    public void func() {
        Log.d("MyService", "func() 被呼叫");
    }
    public IBinder onBind(Intent intent) {
        return null; // 如果不需要綁定服務，返回 null
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化元件
        phoneEditText = findViewById(R.id.phn);
        messageEditText = findViewById(R.id.msg);
        sendButton = findViewById(R.id.btn);

        // 檢查並請求發送簡訊權限
        if (!checkPermission(Manifest.permission.SEND_SMS)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
        }



        // 設置按鈕點擊事件
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSms();
            }
        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        }

    }

    private void sendSms() {
        String phoneNumber = phoneEditText.getText().toString().trim();
        String message = messageEditText.getText().toString().trim();

        if (phoneNumber.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "請輸入電話號碼和訊息內容", Toast.LENGTH_SHORT).show();
            return;
        }

        if (checkPermission(Manifest.permission.SEND_SMS)) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(this, "簡訊已成功發送", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "發送簡訊時發生錯誤: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "無法發送簡訊，請檢查權限", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void Report(View view) {
        String phoneNumber = phoneEditText.getText().toString().trim();
        String message = messageEditText.getText().toString().trim();

        if (phoneNumber.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "請輸入回報內容", Toast.LENGTH_SHORT).show();
            return;
        }
        // 在 Activity 中發送 Intent
// 在 Activity 中發送 Intent 並傳遞資料
        Intent serviceIntent = new Intent(this, MyService.class);
        serviceIntent.setAction("com.example.MY_ACTION");

// 傳遞資料（變數）
        serviceIntent.putExtra("message", message);
        serviceIntent.putExtra("number", phoneNumber);

// 啟動 Service
        startService(serviceIntent);

    }
}


