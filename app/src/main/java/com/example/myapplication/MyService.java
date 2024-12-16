package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.telephony.SmsManager;

public class MyService extends Service {
    
    private Map<String, Integer> carriages = new HashMap<>();
    
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化服務時可執行的初始化工作
    }

    public void onCreate() {
        super.onCreate();
        // 初始化每個車廂的人數
        carriages.put("Carriage 1", 0);
        carriages.put("Carriage 2", 0);
        carriages.put("Carriage 3", 0);
        carriages.put("Carriage 4", 0);
        carriages.put("Carriage 5", 0);
        carriages.put("Carriage 6", 0);
    }
    
    // 用於發送短信的方法
    public void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Toast.makeText(this, "SMS sent: " + message, Toast.LENGTH_SHORT).show();
    }

    // 這個方法可以用來處理接收短信的邏輯
    // 您可以在適當的地方觸發此方法來處理從模擬器收到的短信
    public void receiveSMS(Intent intent) {
        // 從 intent 中獲取短信內容
        String message = intent.getStringExtra("sms_body");
        Toast.makeText(this, "Received SMS: " + message, Toast.LENGTH_SHORT).show();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            // 根據 intent 的 action 來決定呼叫哪個方法
            if ("Arrive".equals(intent.getAction())) {
                // 實作部分（處理新乘客）
                private void handleCustomerEnteringCarriage(String sender, String carriage) {
                    Integer currentCount = carriages.get(carriage);
                    if (currentCount != null) {
                        carriages.put(carriage, currentCount + 1);
                    }
                // tell the arriving passenger which carriage has the least number of passengers
                String leastFullCarriage = getLeastFullCarriage();
                sendSMS(sender, "you have entered " + carriage + "，the carriage with minimum passengers is " + leastFullCarriage);
                }


                String number = intent.getStringExtra("number");
                String message = "test."; //暫時
                sendSMS(number,message);
            }
            if ("Departure".equals(intent.getAction())) {
                // 實作部分（處理離開的乘客）
                private void handleCustomerExit(String sender, String carriage) {
                    // passenger leaves a carriage
                    Integer currentCount = carriages.get(carriage);
                    if (currentCount != null && currentCount > 0) {
                        carriages.put(carriage, currentCount - 1);
                    }

                    // send SMS when passenger leaves
                    sendSMS(sender, "you leave the carriage.");
                }

                String number = intent.getStringExtra("number");
                String message = "test2."; //暫時
                sendSMS(number,message);
            }
            if ("Response".equals(intent.getAction())) {
                // 乘客回應已到達指定車廂

                String number = intent.getStringExtra("number");
                String message = "test3."; //暫時
                sendSMS(number,message);
            }
        }
        return START_STICKY; // 返回該服務的啟動方式
    }

    // 修改 func() 方法來接收參數(search for the carriage with the least passengers)
    private String getLeastFullCarriage() {
        String leastFullCarriage = null;
        int minCount = Integer.MAX_VALUE;

        for (Map.Entry<String, Integer> entry : carriages.entrySet()) {
            if (entry.getValue() < minCount) {
                minCount = entry.getValue();
                leastFullCarriage = entry.getKey();
            }
        }

        return leastFullCarriage;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // 如果不需要綁定服務，返回 null
    }


}
