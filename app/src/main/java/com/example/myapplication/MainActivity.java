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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int SEND_SMS_PERMISSION_REQUEST_CODE = 1;
    private EditText phoneEditText;
    private Button sendButton;
    private MyService myService;
    private String selectedOption;

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

        // 檢查並請求發送簡訊權限
        if (!checkPermission(Manifest.permission.SEND_SMS)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
        }

        Spinner spinner = findViewById(R.id.spinner);

        // 建立選單資料
        String[] options = {"1", "2", "3", "4", "5", "6"};

        // 建立 ArrayAdapter 並綁定到 Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // 設定選擇監聽器
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedOption = options[position];
                Toast.makeText(MainActivity.this, "你選擇了：" + selectedOption, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 當沒有選擇任何項目時
                Toast.makeText(MainActivity.this, "你沒有選擇。" , Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void Arrive(View view) {
        String phoneNumber = phoneEditText.getText().toString().trim();
        //String carriage = messageEditText.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
          Toast.makeText(this, "請先輸入手機號碼！", Toast.LENGTH_SHORT).show();
          return;
        }
        // 在 Activity 中發送 Intent
// 在 Activity 中發送 Intent 並傳遞資料
        Intent serviceIntent = new Intent(this, MyService.class);
        serviceIntent.setAction("Arrive");
// 傳遞資料（變數）
        serviceIntent.putExtra("carriage",selectedOption);
        serviceIntent.putExtra("number", phoneNumber);
// 啟動 Service
        startService(serviceIntent);
    }

    public void Departure(View view) {
        String phoneNumber = phoneEditText.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "請先輸入手機號碼！", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent serviceIntent = new Intent(this, MyService.class);
        serviceIntent.setAction("Departure");
        serviceIntent.putExtra("number", phoneNumber);
        startService(serviceIntent);
    }
    public void Response(View view) {
        String phoneNumber = phoneEditText.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "請先輸入手機號碼！", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent serviceIntent = new Intent(this, MyService.class);
        serviceIntent.setAction("Response");
        serviceIntent.putExtra("number", phoneNumber);
        startService(serviceIntent);
    }
}


