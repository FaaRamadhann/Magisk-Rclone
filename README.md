# Faa Magisk Rclone

Rclone v1.75.0 (linux/arm64-v8a) + fusermount sebagai binary sistem (`/system/bin`). Khusus untuk perangkat **ARM64 (arm64-v8a)**. Termasuk daemon opsional rclone rcd (API/Web GUI di `127.0.0.1:5572`) yang bisa dinyalakan lewat tombol Action di Magisk.

> Note: Modul ini cocok untuk kamu yang ingin menggunakan Rclone langsung dari shell Android tanpa perlu membuka atau bergantung pada Termux, terutama jika kamu lebih memilih Rclone tersedia sebagai utility sistem.

## Fitur

- Binary `rclone` v1.75.0 (linux/arm64-v8a)
- Binary `fusermount` (tidak perlu fuser dari Termux)
- Config persisten di `/data/adb/rclone/rclone.conf`
- Autostart daemon rclone rcd via tombol Action di Magisk

## Cara Install

### 1. Clone repository

```bash
git clone https://github.com/FaaRamadhann/Magisk-Rclone.git
cd Magisk-Rclone
```

### 2. Buat zip dari source

**Cara paling mudah (universal, semua OS):**
```bash
python zip.py
```

Script ini otomatis:
- Membuat file zip `rclone-v1.75.0-fusermount-arm64.zip`
- Melewati `.git`, `.gitignore`, `README.md`, `LICENSE`, `zip.py`

Mau nama file beda? Tinggal ketik pas diminta.

**Cara manual (Linux/macOS):**
```bash
zip -r rclone-v1.75.0-fusermount-arm64.zip . -x ".git/*" ".gitignore" "README.md" "LICENSE" "zip.py"
```

> **PENTING:** Jangan pakai `Compress-Archive` (PowerShell) karena path-nya pake backslash, Magisk tidak bisa extract dengan benar.

### 3. Install ke device

Copy file zip ke device lalu install lewat Magisk App:
- Buka Magisk App → **Install** → pilih file zip

Atau via ADB:
```bash
adb push rclone-v1.75.0-fusermount-arm64.zip /sdcard/
```
Lalu install dari Magisk App.

### 4. Verifikasi

```bash
su -c 'rclone version'
su -c 'fusermount --version'
```

## Struktur Module

```
├── META-INF/          # Magisk installer script
├── system/
│   └── bin/
│       ├── rclone     # Binary rclone
│       └── fusermount # Binary fusermount
├── customize.sh       # Install script
├── service.sh         # Autostart daemon saat boot
├── action.sh          # Toggle daemon via tombol Action
├── module.prop        # Module info
└── uninstall.sh       # Cleanup saat uninstall
```

## Config

- **Config file:** `/data/adb/rclone/rclone.conf`
- **Log file:** `/data/adb/rclone/rcd.log`
- **Autostart toggle:** tombol ACTION di Magisk App
- **Web GUI:** `/data/adb/rclone/.webgui` (buat file ini untuk aktifkan)

## Author

**faa_ramadhan**

## License

[MIT](LICENSE)
