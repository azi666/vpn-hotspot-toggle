package com.vpnhotspot;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class StopActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Runtime.getRuntime().exec(new String[]{"su", "-c",
                "sh /data/adb/modules/vpn_hotspot_share/proxy_ctrl.sh stop"
            });
            Toast.makeText(this, "正在停止VPN热点共享...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            try {
                Runtime.getRuntime().exec(new String[]{"sh", "-c",
                    "sh /data/adb/modules/vpn_hotspot_share/proxy_ctrl.sh stop"
                });
                Toast.makeText(this, "正在停止VPN热点共享...", Toast.LENGTH_SHORT).show();
            } catch (Exception e2) {
                Toast.makeText(this, "停止失败", Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }
}
