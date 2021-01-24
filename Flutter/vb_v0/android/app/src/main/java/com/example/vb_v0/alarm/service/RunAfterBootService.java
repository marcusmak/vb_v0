package com.example.vb_v0.alarm.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.widget.Toast;
//import android.util.Log;

import com.example.vb_v0.MainActivity;
import com.example.vb_v0.R;
import com.example.vb_v0.alarm.service.util.PackingContextManager;
import com.example.vb_v0.alarm.service.util.RuleChecker;
import com.example.vb_v0.ble.service.GattServiceHandler;

import io.flutter.Log;


//import com.android.volley.Request;
//import com.android.volley.toolbox.JsonObjectRequest;
//import com.example.vb_v0.sqlite.helper.SQLiteHelper;
//import com.google.gson.JsonObject;
//import org.json.JSONObject;
//import java.net.URI;


public class RunAfterBootService extends Service {

    private static final String TAG_BOOT_EXECUTE_SERVICE = "BOOT_BROADCAST_SERVICE";
//    private SQLiteHelper dbHelper;// = new SQLiteHelper();

    private PackingContextManager contextManager;
    private RuleChecker ruleChecker;
    private GattServiceHandler gattServiceHandler;
    public static boolean isRunning;


    public RunAfterBootService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        Context context = getApplicationContext();
        contextManager  = new PackingContextManager(context);
        ruleChecker     = new RuleChecker(context);
        Log.d(TAG_BOOT_EXECUTE_SERVICE, "RunAfterBootService onCreate() method.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String message = "RunAfterBootService onStartCommand() method.";
//        Log.d(TAG_BOOT_EXECUTE_SERVICE, "RunAfterBootService onStartCommand() method.");
        //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

        if(!GattServiceHandler.isConnecting ){ //&& !MainActivity.hasActivity){
            if(connectLastBLE()){
                Log.d(TAG_BOOT_EXECUTE_SERVICE,"connected to ble");
            }else{
                Log.d(TAG_BOOT_EXECUTE_SERVICE,"fail to connect");
            };
        }

        if(GattServiceHandler.isConnecting ){
            //get current ble list
            gattServiceHandler.scanTags();
            //get current context
            PackingContextManager.PackingContext currentPC = contextManager.getCurrentPC();

            //check system rule
            //check custom rule
            ruleChecker.checkRule(currentPC);
            //check internet recommendations
        }





        return super.onStartCommand(intent, flags, startId);
    }

    private boolean connectLastBLE(){
        Log.d(TAG_BOOT_EXECUTE_SERVICE, "connecting ble in background");
        SharedPreferences lastBLEPre = getSharedPreferences(getString(R.string.package_name),Context.MODE_PRIVATE);
        if(!lastBLEPre.contains(getString(R.string.last_ble_connection))){
            Intent cancelIntent = (new Intent()).setAction("ACTION_CANCEL_ALARM");
            sendBroadcast(cancelIntent);
            return false;
        }else{
            String address = lastBLEPre.getString(getString(R.string.last_ble_connection),null);
            BluetoothDevice bluetoothDevice;
            if(address != null) {
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                    Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                    stopSelf();
                    return false;
                }
                BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
                if(bluetoothDevice != null) {
                    gattServiceHandler = GattServiceHandler.getInstance(getApplicationContext(), bluetoothDevice);
                    return gattServiceHandler.connect();
                }
            }else{
                Log.d(TAG_BOOT_EXECUTE_SERVICE,"cant find last ble connection address");
            }

        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        Log.d(TAG_BOOT_EXECUTE_SERVICE, "RunAfterBootService onDestory() method.");
    }
}