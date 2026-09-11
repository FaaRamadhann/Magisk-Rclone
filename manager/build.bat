@echo off
rem ============================================================
rem  build.bat — Build APK Android TANPA Android Studio
rem  (cukup JDK + Android SDK build-tools)
rem
rem  Cara pakai:
rem    1. Jalankan dari root project:  build.bat
rem
rem  Struktur project yang diharapkan:
rem      AndroidManifest.xml
rem      src\**\*.java          (kode sumber)
rem      res\...                (opsional, boleh tidak ada)
rem
rem  Hasil: build\<APP_NAME>.apk (sudah zipalign + signed)
rem ============================================================
setlocal

rem ---------------- KONFIG (ubah sesuai project) ----------------
set "APP_NAME=frcl"
set "PACKAGE=com.faa.frcl"
set "MIN_SDK=24"
set "BT=C:\AndroidSDK\build-tools\35.0.0"
set "PLAT=C:\AndroidSDK\platforms\android-34\android.jar"
set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.10"
set "KEYSTORE=frcl.keystore"
set "KEY_ALIAS=frcl"
set "STOREPASS=android"
set "KEYPASS=android"
rem -------------- akhir KONFIG (jangan ubah ke bawah) ------------

set "JAVAC=%JAVA_HOME%\bin\javac.exe"
set "KEYTOOL=%JAVA_HOME%\bin\keytool.exe"
if not exist build mkdir build
if not exist build\obj mkdir build\obj
if not exist build\dex mkdir build\dex

echo [1/6] kumpulkan source...
del /q build\sources.txt 2>nul
for /R src %%f in (*.java) do echo %%f>> build\sources.txt
if errorlevel 1 exit /b 1

echo [2/6] javac...
"%JAVAC%" --release 8 -classpath "%PLAT%" -d build\obj @build\sources.txt
if errorlevel 1 exit /b 1

echo [3/6] d8 (java -^> dex)...
set CLASSES=
for /R build\obj %%f in (*.class) do call set CLASSES=%%CLASSES%% "%%f"
call "%BT%\d8.bat" --min-api %MIN_SDK% --lib "%PLAT%" --output build\dex %CLASSES%
if errorlevel 1 exit /b 1

echo [4/6] aapt package...
if exist res ( set "RESFLAG=-S res" ) else ( set "RESFLAG=" )
"%BT%\aapt.exe" package -f -M AndroidManifest.xml %RESFLAG% -I "%PLAT%" -F build\unsigned.apk
if errorlevel 1 exit /b 1
cd build\dex
"%BT%\aapt.exe" add ..\unsigned.apk classes.dex
if errorlevel 1 exit /b 1
cd ..\..

echo [5/6] keystore (sekali saja, lalu dipakai terus)...
echo PENTING: backup file %KEYSTORE% — update APK wajib pakai key yang sama!
if not exist %KEYSTORE% "%KEYTOOL%" -genkeypair -keystore %KEYSTORE% -alias %KEY_ALIAS% -keyalg RSA -keysize 2048 -validity 10950 -storepass %STOREPASS% -keypass %KEYPASS% -dname "CN=%APP_NAME%"
if errorlevel 1 exit /b 1

echo [6/6] zipalign + apksigner...
"%BT%\zipalign.exe" -f 4 build\unsigned.apk build\aligned.apk
if errorlevel 1 exit /b 1
call "%BT%\apksigner.bat" sign --ks %KEYSTORE% --ks-key-alias %KEY_ALIAS% --ks-pass pass:%STOREPASS% --key-pass pass:%KEYPASS% --out build\%APP_NAME%.apk build\aligned.apk
if errorlevel 1 exit /b 1
call "%BT%\apksigner.bat" verify build\%APP_NAME%.apk
if errorlevel 1 exit /b 1

echo.
echo SELESAI: build\%APP_NAME%.apk
endlocal
