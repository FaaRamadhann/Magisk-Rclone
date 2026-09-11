SYARAT PAKAI ex-build.bat / ex-build.py
=====================================
(Build APK Android tanpa Android Studio)

1. JDK 17+  (butuh: javac.exe, keytool.exe)
   - Yang teruji: JDK 21 LTS
   - Cek:  javac -version
   - Download: https://adoptium.net  (atau Oracle JDK)
   - Catatan: JRE saja TIDAK cukup, harus JDK.

2. Android SDK: build-tools + 1 platform  (butuh: aapt, d8, zipalign, apksigner)
   - Yang teruji: build-tools 35.0.0 + platform android-34
   - Cek:  dir "%ANDROID_HOME%\build-tools"  (atau lihat path di KONFIG)
   - Download ringan (tanpa Android Studio):
     a. Ambil "commandlinetools" dari
        https://developer.android.com/studio#command-line-tools-only
     b. Jalankan:  sdkmanager "platform-tools" "platforms;android-34" "build-tools;35.0.0"
   - Sesuaikan path BT dan PLAT di blok KONFIG kedua script.

3. Python 3.8+  (HANYA untuk ex-build.py, .bat tidak butuh)
   - Cek:  python --version
   - Tanpa library tambahan (cuma modul bawaan: os, subprocess, sys).

4. Windows  (kedua script ditulis untuk Windows)
   - ex-build.py memakai javac.exe/d8.bat/aapt.exe versi Windows;
     di Linux/Mac ganti ekstensi dan separator path di KONFIG.

CEK CEPAT (semua harus ada outputnya):
   javac -version
   keytool -help
   C:\AndroidSDK\build-tools\35.0.0\aapt.exe version
   C:\AndroidSDK\build-tools\35.0.0\d8.bat --version
   dir C:\AndroidSDK\platforms\android-34\android.jar

YANG TIDAK DIBUTUHKAN:
   - Android Studio / Gradle (build langsung via tool di atas)
   - adb (cuma perlu pas install hasil APK ke HP, bukan pas build)
   - Koneksi internet (kecuali download awal di atas)
