package com.vpnhotspot;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String CHANNEL_ID = "vpn_hotspot_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final int PERM_REQUEST_CODE = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    new String[]{"android.permission.POST_NOTIFICATIONS"},
                    PERM_REQUEST_CODE);
                return;
            }
        }
        doAction();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doAction();
            } else {
                Toast.makeText(this, "需要通知权限才能显示开关",
                    Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }

    private void doAction() {
        // /data/adb is root-only, app can't read it directly
        // Always send broadcast to trigger proxy start via root shell
        try {
            Intent i = new Intent("com.vpnhotspot.TOGGGLE_START");
            i.setPackage(getPackageName());
            sendBroadcast(i);
            Toast.makeText(this, "正在切换VPN热点共享...",
                Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "操作失败",
                Toast.LENGTH_LONG).show();
        }
        finish();
    }

    private void showNotification() {
        NotificationManager nm = (NotificationManager)
            getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "VPN热点共享",
                NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }

        Intent stopIntent = new Intent(this, ProxyReceiver.class);
        stopIntent.setAction("com.vpnhotspot.TOGGGLE_STOP");
        PendingIntent stopPi = PendingIntent.getBroadcast(
            this, 100, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        Notification notif = builder
            .setContentTitle("VPN热点共享运行中")
            .setContentText("点击此通知停止共享  代理:172.25.1.1:7890")
            .setContentIntent(stopPi)
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_MAX)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setStyle(new Notification.BigTextStyle()
                .bigText("VPN热点共享运行中\n代理地址: 172.25.1.1:7890\n\n点击此通知即可停止共享"))
            .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                "停止共享", togglePi)
            .build();

        nm.notify(NOTIFICATION_ID, notif);
    }
}
