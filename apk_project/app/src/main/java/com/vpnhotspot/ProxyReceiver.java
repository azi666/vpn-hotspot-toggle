package com.vpnhotspot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ProxyReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "vpn_hotspot_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        if ("com.vpnhotspot.SHOW".equals(action)) {
            showNotification(context);
        } else if ("com.vpnhotspot.HIDE".equals(action)) {
            cancelNotification(context);
        } else if ("com.vpnhotspot.TOGGLE".equals(action)) {
            cancelNotification(context);
            try {
                Runtime.getRuntime().exec(new String[]{"sh", "-c",
                    "sh /data/adb/modules/vpn_hotspot_share/action.sh"});
            } catch (Exception e) {}
        }
    }

    private void showNotification(Context context) {
        NotificationManager nm = (NotificationManager)
            context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "VPN热点共享",
                NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }

        Intent stopIntent = new Intent(context, ProxyReceiver.class);
        stopIntent.setAction("com.vpnhotspot.TOGGLE");
        PendingIntent stopPi = PendingIntent.getBroadcast(
            context, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(context);
        }

        Notification notif = builder
            .setContentTitle("VPN热点共享运行中")
            .setContentText("代理: 172.25.1.1:7890  点击停止")
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_MAX)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setStyle(new Notification.BigTextStyle()
                .bigText("VPN热点共享运行中\n代理地址: 172.25.1.1:7890\n点击下方按钮停止共享"))
            .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                "停止共享", stopPi)
            .build();

        nm.notify(NOTIFICATION_ID, notif);
    }

    private void cancelNotification(Context context) {
        NotificationManager nm = (NotificationManager)
            context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
    }
}
