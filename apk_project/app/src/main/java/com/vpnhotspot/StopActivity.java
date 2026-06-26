package com.vpnhotspot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class StopActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Intent i = new Intent("com.vpnhotspot.TOGGGLE_STOP");
            i.setPackage(getPackageName());
            sendBroadcast(i);
            Toast.makeText(this, "正在停止VPN热点共享...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "停止失败", Toast.LENGTH_LONG).show();
        }
        finish();
    }
}
