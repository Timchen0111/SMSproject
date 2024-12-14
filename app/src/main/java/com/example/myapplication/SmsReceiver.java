package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.MyService;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String smsBody = intent.getStringExtra("sms_body");
        Log.d("SMSReceiver", "接收到簡訊: " + smsBody);

        // 可以在這裡處理簡訊內容
        Toast.makeText(context, "有人找你", Toast.LENGTH_SHORT).show();
    }
}

