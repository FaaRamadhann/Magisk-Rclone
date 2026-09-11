package com.faa.frcl;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private static final String APP_VERSION = "1.0.0";
    private static final String UPDATE_JSON =
            "https://raw.githubusercontent.com/FaaRamadhann/Magisk-Rclone/main/update.json";
    private static final String REPO_URL =
            "https://github.com/FaaRamadhann/Magisk-Rclone";

    private final ExecutorService pool = Executors.newFixedThreadPool(2);

    private ScrollView scroll;
    private TextView status;
    private Button btnCheck;
    private Button btnDownload;
    private Button btnOpen;

    private String latestZipUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        log("FMR Manager v" + APP_VERSION);
        log("Manager resmi Faa Magisk Rclone (FMR).");
        log("Pilih menu di bawah.");
    }

    private void buildUi() {
        int blue = Color.rgb(33, 150, 243);
        int blueDark = Color.rgb(25, 118, 210);
        int bg = Color.rgb(227, 242, 253);
        int white = Color.WHITE;
        int txt = Color.rgb(33, 33, 33);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg);
        root.setPadding(dp(20), dp(24), dp(20), dp(24));

        TextView title = new TextView(this);
        title.setText("FMR Manager");
        title.setTextSize(28);
        title.setTextColor(blueDark);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Faa Magisk Rclone (FMR)");
        sub.setTextSize(14);
        sub.setTextColor(Color.rgb(90, 135, 195));
        sub.setGravity(Gravity.CENTER);
        root.addView(sub);

        root.addView(spacer(24));

        status = new TextView(this);
        status.setTextSize(13);
        status.setTextColor(txt);
        status.setBackgroundColor(white);
        status.setPadding(dp(14), dp(14), dp(14), dp(14));
        status.setMinHeight(dp(200));
        status.setMaxHeight(dp(300));

        scroll = new ScrollView(this);
        scroll.addView(status);
        root.addView(scroll);

        btnCheck = new Button(this);
        btnCheck.setText("CEK UPDATE");
        styleButton(btnCheck, blue);
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUpdate();
            }
        });
        root.addView(btnCheck);

        btnDownload = new Button(this);
        btnDownload.setText("DOWNLOAD MODULE");
        styleButton(btnDownload, blue);
        btnDownload.setEnabled(false);
        btnDownload.setAlpha(0.4f);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadModule();
            }
        });
        root.addView(btnDownload);

        btnOpen = new Button(this);
        btnOpen.setText("BUKA REPOSITORY");
        styleButton(btnOpen, Color.rgb(120, 144, 200));
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser(REPO_URL);
            }
        });
        root.addView(btnOpen);

        setContentView(root);
    }

    private LinearLayout spacer(int h) {
        LinearLayout s = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, h);
        s.setLayoutParams(lp);
        return s;
    }

    private void styleButton(Button btn, int color) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(12);
        lp.leftMargin = dp(12);
        lp.rightMargin = dp(12);
        btn.setLayoutParams(lp);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundColor(color);
        btn.setAllCaps(false);
        btn.setTextSize(14);
        btn.setPadding(dp(6), dp(12), dp(6), dp(12));
    }

    private void setBusy(boolean busy) {
        btnCheck.setEnabled(!busy);
        btnCheck.setAlpha(busy ? 0.4f : 1f);
        btnDownload.setEnabled(!busy && !latestZipUrl.isEmpty());
        btnDownload.setAlpha(!busy && !latestZipUrl.isEmpty() ? 1f : 0.4f);
    }

    private void log(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.append(s + "\n");
                scroll.post(new Runnable() {
                    @Override
                    public void run() {
                        scroll.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private void checkUpdate() {
        setBusy(true);
        log("Mengecek update...");
        pool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection c = openConn(UPDATE_JSON);
                    int code = c.getResponseCode();
                    if (code != 200) {
                        log("HTTP " + code + " saat ambil update.json");
                        setBusy(false);
                        return;
                    }
                    InputStream in = c.getInputStream();
                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) != -1) bo.write(buf, 0, n);
                    in.close();

                    JSONObject j = new JSONObject(bo.toString("UTF-8"));
                    String ver = j.optString("version", "");
                    latestZipUrl = j.optString("zipUrl", "");

                    log("Versi terbaru: " + ver);
                    if (latestZipUrl.isEmpty()) {
                        log("URL zip kosong di update.json!");
                    } else {
                        log("Zip: " + latestZipUrl);
                    }
                    setBusy(false);
                } catch (Exception e) {
                    log("Gagal cek update: " + e.getMessage());
                    setBusy(false);
                }
            }
        });
    }

    private void downloadModule() {
        if (latestZipUrl.isEmpty()) {
            log("Belum ada URL. Tekan CEK UPDATE dulu.");
            return;
        }
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 29
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            log("Izinkan akses storage, lalu tekan DOWNLOAD lagi.");
            return;
        }
        setBusy(true);
        log("Downloading: " + latestZipUrl);
        pool.execute(new Runnable() {
            @Override
            public void run() {
                boolean done = false;
                try {
                    HttpURLConnection c = openConn(latestZipUrl);
                    int code = c.getResponseCode();
                    if (code != 200) {
                        log("HTTP " + code + " saat download");
                        setBusy(false);
                        return;
                    }
                    InputStream in = c.getInputStream();
                    String name = fileNameOf(latestZipUrl);

                    if (Build.VERSION.SDK_INT >= 29) {
                        ContentValues v = new ContentValues();
                        v.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                        v.put(MediaStore.MediaColumns.MIME_TYPE, "application/zip");
                        v.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                        Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                        if (uri == null) {
                            throw new IOException("Gagal buat file di MediaStore");
                        }
                        OutputStream os = getContentResolver().openOutputStream(uri);
                        copy(in, os);
                        os.close();
                    } else {
                        File dir = new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
                        if (!dir.exists() && !dir.mkdirs()) {
                            throw new IOException("Gagal buat folder Downloads");
                        }
                        File f = new File(dir, name);
                        FileOutputStream fos = new FileOutputStream(f);
                        copy(in, fos);
                        fos.close();
                    }
                    in.close();
                    done = true;
                    log("Selesai: Download/" + name);
                } catch (Exception e) {
                    log("Gagal download: " + e.getMessage());
                } finally {
                    final boolean doneF = done;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setBusy(false);
                        }
                    });
                    if (doneF) logDone();
                }
            }
        });
    }

    private void logDone() {
        log("Install zip lewat Magisk App (Modules -> Install from storage).");
    }

    private HttpURLConnection openConn(String urlStr) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(urlStr).openConnection();
        c.setConnectTimeout(20000);
        c.setReadTimeout(60000);
        c.setRequestProperty("User-Agent", "FMR-Manager/" + APP_VERSION);
        c.setInstanceFollowRedirects(true);
        return c;
    }

    private String fileNameOf(String url) {
        String s = url.substring(url.lastIndexOf('/') + 1);
        return (s == null || s.isEmpty()) ? "frcl-module.zip" : s;
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[65536];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
    }

    private void openBrowser(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            log("Gagal buka browser: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pool.shutdownNow();
    }
}