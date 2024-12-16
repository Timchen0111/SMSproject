package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import android.telephony.SmsManager;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MyService extends Service {
    private Map<String, Integer> carriages = new ConcurrentHashMap<>();
    private Map<String,String> passengers =new ConcurrentHashMap<>();//紀錄使用者和其所在車廂
    private Map<String,String> suggest = new ConcurrentHashMap<>(); //記錄使用者和建議前往車廂@Override
    Boolean NULL = false;
    private DatabaseReference mDatabase;
    private DatabaseReference carriagesRef;
    private DatabaseReference passengersRef;
    private DatabaseReference suggestRef;

    @Override
    public void onCreate() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://smsapp-3e781-default-rtdb.firebaseio.com/");
        mDatabase = database.getReference();
        carriagesRef = mDatabase.child("carriages");
        passengersRef = mDatabase.child("passengers");
        suggestRef = mDatabase.child("suggest");

        // 初始化車廂資料僅當節點不存在時
        carriagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                    // 只有在節點不存在時才初始化
                    Map<String, Integer> initialCarriages = new HashMap<>();
                    initialCarriages.put("Carriage 1", 0);
                    initialCarriages.put("Carriage 2", 0);
                    initialCarriages.put("Carriage 3", 0);
                    initialCarriages.put("Carriage 4", 0);
                    initialCarriages.put("Carriage 5", 0);
                    initialCarriages.put("Carriage 6", 0);
                    carriagesRef.setValue(initialCarriages);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error initializing data", databaseError.toException());
            }
        });
        addValueEventListener();
    }


    // 用於發送短信的方法
    public void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        //Toast.makeText(this, "SMS sent: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        addValueEventListener();
        if (intent != null && intent.getAction() != null) {
            // 根據 intent 的 action 來決定呼叫哪個方法
            String sender = intent.getStringExtra("number"); // 接收發送者的號碼
            String carriage = intent.getStringExtra("carriage"); // 接收車廂名稱

            System.out.println(carriage);
            if (carriage != null) {
                carriage = "Carriage " + carriage;
                NULL = false;
            }else{
                NULL = true;
                carriage = "0";
            }
            if ("Arrive".equals(intent.getAction())) {
                // 實作部分（處理新乘客）
                handleCustomerEnteringCarriage(sender,carriage);
            }
            if ("Departure".equals(intent.getAction())) {
                // 實作部分（處理離開的乘客）
                handleCustomerExit(sender);
            }
            if ("Response".equals(intent.getAction())) {
                // 乘客回應已到達指定車廂
                String NewCarriage = suggest.get(sender);
                String OldCarriage = passengers.get(sender);
                passengers.put(sender,NewCarriage);
                // 更改車廂人數紀錄
                Integer CurrentCount = carriages.getOrDefault(NewCarriage, 0);
                carriages.put(NewCarriage,CurrentCount+1);
//                CurrentCount = carriages.getOrDefault(OldCarriage, 0);
                carriages.put(OldCarriage,CurrentCount-1);
                //回應使用者
                String number = intent.getStringExtra("number");
                String message = "系統已收到通知！";
                sendSMS(number,message);
                carriagesRef.setValue(carriages);
                passengersRef.setValue(passengers);
                suggestRef.setValue(suggest);
            }
        }
        return START_STICKY; // 返回該服務的啟動方式
    }

    private void handleCustomerEnteringCarriage(String sender, String carriage) {
        Integer currentCount = carriages.getOrDefault(carriage, 0);
        if (currentCount != null) {
            carriages.put(carriage, currentCount + 1);
        }
        // tell the arriving passenger which carriage has the least number of passengers
        String leastFullCarriage = getLeastFullCarriage(carriage);
        // 印出測試資訊
        System.out.println("Passenger entered carriage: " + carriage);
        System.out.println("Updated carriage count: " + carriages);
        //System.out.println("Carriage with minimum passengers: " + leastFullCarriage);
        passengers.put(sender,carriage);
        suggest.put(sender,leastFullCarriage);
        System.out.println("Passenger INFO: " + passengers);
        System.out.println("Suggest INFO: " + suggest);
        char c = leastFullCarriage.charAt(leastFullCarriage.length() - 1);
        sendSMS(sender, "建議前往第" + c + "車廂。");
        carriagesRef.setValue(carriages);
        passengersRef.setValue(passengers);
        suggestRef.setValue(suggest);
    }

    private void handleCustomerExit(String sender) {
        // passenger leaves a carriage
        String carriage = passengers.get(sender);
        Integer currentCount = carriages.get(carriage);
        if (currentCount != null && currentCount > 0) {
            carriages.put(carriage, currentCount - 1);
        }
        passengers.remove(sender);
        suggest.remove(sender);
        // 印出測試資訊
        //System.out.println("Passenger left carriage: " + carriage);
        //System.out.println("Updated carriage count: " + carriages);
        // send SMS when passenger leaves
        sendSMS(sender, "已離開列車");
        carriagesRef.setValue(carriages);
        passengersRef.setValue(passengers);
        suggestRef.setValue(suggest);
    }

    // 修改 func() 方法來接收參數(search for the carriage with the least passengers)
    private String getLeastFullCarriage(String carriage) {
        String leastFullCarriage = null;
        Integer CurrentNum =  carriages.getOrDefault(carriage, 0);
        int minCount = Integer.MAX_VALUE;
        for (Map.Entry<String, Integer> entry : carriages.entrySet()) {
            if (entry.getValue() < minCount) {
                minCount = entry.getValue();
                leastFullCarriage = entry.getKey();
            }
        }
        //程式邏輯：當人數皆相同時，讓使用者留在原本的車廂即可。

        if (CurrentNum - minCount < 2){
            System.out.println("f");
            if (NULL == false) {
                leastFullCarriage = carriage;
            }
        }

        return leastFullCarriage;
    }
    // 添加監聽器，實時更新本地數據
    // 本地儲存車廂資料的 HashMap

    private void addValueEventListener() {
        // 監聽車廂人數變動
        System.out.println("Listening for carriage updates...");

        carriagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // 清空本地資料，確保資料與 Firebase 同步
                carriages.clear();

                // 確認是否有資料
                if (dataSnapshot.exists()) {
                    // 遍歷所有車廂節點
                    for (DataSnapshot carriageSnapshot : dataSnapshot.getChildren()) {
                        // 取得車廂名稱
                        String carriageName = carriageSnapshot.getKey();

                        // 嘗試取得車廂內乘客人數
                        Integer numberOfPassengers = carriageSnapshot.getValue(Integer.class);

                        // 檢查數據是否為 null，避免空指標例外
                        if (numberOfPassengers != null) {
                            // 將資料存入 HashMap
                            carriages.put(carriageName, numberOfPassengers);
                            Log.d("Firebase", "Updated: " + carriageName + " has " + numberOfPassengers + " passengers.");
                        } else {
                            Log.w("Firebase", "No passenger data for carriage: " + carriageName);
                        }
                    }

                    // Log 本地 HashMap 的最新狀態
                    Log.d("Firebase", "Current carriages data: " + carriages.toString());
                } else {
                    Log.w("Firebase", "No data exists at carriagesRef.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 處理錯誤，例如網路問題或權限不足
                Log.e("Firebase", "Error reading data", databaseError.toException());
            }
        });

        passengersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // 清空本地乘客資料，確保與 Firebase 資料同步
                passengers.clear();

                // 更新乘客資料
                for (DataSnapshot passengerSnapshot : dataSnapshot.getChildren()) {
                    String userId = passengerSnapshot.getKey();
                    String carriage = passengerSnapshot.getValue(String.class);

                    // 將乘客資料更新至本地結構
                    if (userId != null && carriage != null) {
                        passengers.put(userId, carriage);
                        Log.d("Firebase", "Updated: " + userId + " is in " + carriage);
                    } else {
                        Log.w("Firebase", "Invalid passenger data for userId: " + userId);
                    }
                }

                // 確認同步後的乘客資料
                Log.d("Firebase", "Current passengers data: " + passengers.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 處理錯誤
                Log.e("Firebase", "Error reading passenger data", databaseError.toException());
            }
        });

        // 監聽建議車廂變動
        suggestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // 清空本地建議車廂資料
                suggest.clear();

                // 更新建議車廂資料
                for (DataSnapshot suggestSnapshot : dataSnapshot.getChildren()) {
                    String userId = suggestSnapshot.getKey();
                    String suggestedCarriage = suggestSnapshot.getValue(String.class);

                    // 將建議資料更新至本地結構
                    if (userId != null && suggestedCarriage != null) {
                        suggest.put(userId, suggestedCarriage);
                        Log.d("Firebase", "Updated: " + userId + " is suggested to go to " + suggestedCarriage);
                    } else {
                        Log.w("Firebase", "Invalid suggestion data for userId: " + userId);
                    }
                }

                // 確認同步後的建議車廂資料
                Log.d("Firebase", "Current suggestions data: " + suggest.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 處理錯誤
                Log.e("Firebase", "Error reading suggestion data", databaseError.toException());
            }
        });
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null; // 如果不需要綁定服務，返回 null
    }


}