#!/system/bin/sh
# Monitor for APK command flags and execute proxy start/stop
MODDIR=/data/adb/modules/vpn_hotspot_share
FLAG_FILE=/data/local/tmp/vpn_hotspot_cmd

# Find APK data dir (package may have different uid on reinstall)
find_apk_flag() {
    # Try /data/local/tmp first
    if [ -f "$FLAG_FILE" ]; then
        echo "$FLAG_FILE"
        return
    fi
    # Try app private dir
    for d in /data/data/com.vpnhotspot/files/vpn_hotspot_cmd; do
        if [ -f "$d" ]; then
            echo "$d"
            return
        fi
    done
}

while true; do
    APK_FLAG=$(find_apk_flag)

    if [ -n "$APK_FLAG" ]; then
        CMD=$(cat "$APK_FLAG" 2>/dev/null)
        rm -f "$APK_FLAG" 2>/dev/null

        if [ -n "$CMD" ]; then
            case "$CMD" in
                start)
                    sh "$MODDIR/proxy_ctrl.sh" start >/dev/null 2>&1
                    if [ -f /data/adb/vpn_hotspot_share_active ]; then
                        am broadcast -a com.vpnhotspot.STARTED -p com.vpnhotspot >/dev/null 2>&1
                    else
                        am broadcast -a com.vpnhotspot.FAILED -p com.vpnhotspot --es msg "开启失败" >/dev/null 2>&1
                    fi
                    ;;
                stop)
                    sh "$MODDIR/proxy_ctrl.sh" stop >/dev/null 2>&1
                    if [ ! -f /data/adb/vpn_hotspot_share_active ]; then
                        am broadcast -a com.vpnhotspot.STOPPED -p com.vpnhotspot >/dev/null 2>&1
                    else
                        am broadcast -a com.vpnhotspot.FAILED -p com.vpnhotspot --es msg "关闭失败" >/dev/null 2>&1
                    fi
                    ;;
                toggle)
                    if [ -f /data/adb/vpn_hotspot_share_active ]; then
                        sh "$MODDIR/proxy_ctrl.sh" stop >/dev/null 2>&1
                        if [ ! -f /data/adb/vpn_hotspot_share_active ]; then
                            am broadcast -a com.vpnhotspot.STOPPED -p com.vpnhotspot >/dev/null 2>&1
                        fi
                    else
                        sh "$MODDIR/proxy_ctrl.sh" start >/dev/null 2>&1
                        if [ -f /data/adb/vpn_hotspot_share_active ]; then
                            am broadcast -a com.vpnhotspot.STARTED -p com.vpnhotspot >/dev/null 2>&1
                        fi
                    fi
                    ;;
            esac
        fi
    fi

    sleep 2
done
