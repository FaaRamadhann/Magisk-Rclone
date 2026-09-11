SKIPUNZIP=0

ABI=$(getprop ro.product.cpu.abi)
if [ "$ABI" != "arm64-v8a" ]; then
  abort "! ABI tidak didukung: $ABI"
  abort "! Modul ini hanya untuk perangkat arm64-v8a"
fi

ui_print "- Rclone v1.75.1 (linux/arm64) + fusermount"
ui_print "- Memasang binary ke /system/bin/rclone dan /system/bin/fusermount"

set_perm_recursive "$MODPATH/system/bin" 0 2000 0755 0755

# Folder konfigurasi persisten (tidak hilang walau module diuninstall)
mkdir -p /data/adb/rclone
if [ ! -f /data/adb/rclone/rclone.conf ]; then
  touch /data/adb/rclone/rclone.conf
fi
set_perm /data/adb/rclone 0 0 0700
set_perm /data/adb/rclone/rclone.conf 0 0 0600

ui_print "- Config   : /data/adb/rclone/rclone.conf"
ui_print "- Autostart: tombol ACTION di Magisk app"
ui_print "- Setelah reboot, coba: su -c 'rclone version'"
ui_print "- fusermount tersedia di /system/bin/fusermount"
