package com.faa.frcl;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private static final String APP_VERSION = "2.0.0";
    private static final String DASHBOARD_URL = "http://127.0.0.1:5572/";

    private static final String RCD_DIR = "/data/adb/rclone";
    private static final String PIDF = RCD_DIR + "/rcd.pid";
    private static final String CONF = RCD_DIR + "/rclone.conf";
    private static final String LOGF = RCD_DIR + "/rcd.log";

    private final ExecutorService pool = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private WebView webView;
    private TextView status;
    private Button btnStart;
    private Button btnStop;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        buildUi();
        checkStatus();
        webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl(DASHBOARD_URL);
            }
        }, 600);
        log("FMR Manager v" + APP_VERSION);
        log("Dashboard: " + DASHBOARD_URL);
    }

    private void buildUi() {
        int blue = Color.rgb(33, 150, 243);
        int blueDark = Color.rgb(25, 118, 210);
        int bg = Color.rgb(227, 242, 253);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg);

        TextView title = new TextView(this);
        title.setText("FMR Manager");
        title.setTextSize(20);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setBackgroundColor(blueDark);
        title.setPadding(0, dp(28), 0, dp(28));
        root.addView(title);

        webView = new WebView(this);
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        webView.setBackgroundColor(bg);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                if (req.isForMainFrame()) {
                    log("Dashboard belum bisa dibuka (rcd mati?)");
                }
            }
        });
        root.addView(webView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f));

        status = new TextView(this);
        status.setTextSize(13);
        status.setTextColor(Color.WHITE);
        status.setGravity(Gravity.CENTER);
        status.setBackgroundColor(Color.rgb(255, 152, 0));
        status.setPadding(0, dp(10), 0, dp(10));
        root.addView(status);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setBackgroundColor(blue);

        btnStart = styleBarButton("START RCD", blueDark);
        btnStop = styleBarButton("STOP RCD", Color.rgb(183, 28, 28));
        bar.addView(btnStart, barLp());
        bar.addView(btnStop, barLp());
        root.addView(bar);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRcd();
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRcd();
            }
        });

        setContentView(root);
    }

    private Button styleBarButton(String text, int color) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(14);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(color);
        return b;
    }

    private LinearLayout.LayoutParams barLp() {
        return new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f);
    }

    private void checkStatus() {
        runSu("if [ -f " + PIDF + " ] && kill -0 $(cat " + PIDF + ") 2>/dev/null; then echo RUNNING; else echo STOPPED; fi",
                new Callback() {
                    @Override
                    public void onResult(int exit, String out) {
                        if (out.contains("RUNNING")) {
                            setStatus("RCD AKTIF", Color.rgb(0, 150, 80));
                        } else {
                            setStatus("RCD MATI", Color.rgb(255, 152, 0));
                        }
                    }
                });
    }

    private void startRcd() {
        setBusy(true);
        log("Menyalakan rclone rcd...");
        runSu("mkdir -p " + RCD_DIR + " && " +
                "setsid sh -c 'rclone rcd --config " + CONF +
                " --log-file " + LOGF + " --log-level INFO --rc-addr 127.0.0.1:5572 --rc-web-gui" +
                " >/dev/null 2>&1 < /dev/null' & echo $! > " + PIDF,
                new Callback() {
                    @Override
                    public void onResult(int exit, String out) {
                        log("rcd start exit=" + exit);
                        checkStatus();
                        webView.loadUrl(DASHBOARD_URL);
                        setBusy(false);
                    }
                });
    }

    private void stopRcd() {
        setBusy(true);
        log("Mematikan rclone rcd...");
        runSu("kill $(cat " + PIDF + ") 2>/dev/null; rm -f " + PIDF,
                new Callback() {
                    @Override
                    public void onResult(int exit, String out) {
                        log("rcd stop exit=" + exit);
                        checkStatus();
                        webView.stopLoading();
                        setBusy(false);
                    }
                });
    }

    private void setBusy(boolean busy) {
        btnStart.setEnabled(!busy);
        btnStop.setEnabled(!busy);
    }

    private void setStatus(final String text, final int color) {
        main.post(new Runnable() {
            @Override
            public void run() {
                status.setText(text);
                status.setBackgroundColor(color);
            }
        });
    }

    private void log(final String s) {
        main.post(new Runnable() {
            @Override
            public void run() {
                if (status != null) status.setText(s);
            }
        });
    }

    private interface Callback {
        void onResult(int exit, String out);
    }

    private void runSu(final String cmd, final Callback cb) {
        pool.execute(new Runnable() {
            @Override
            public void run() {
                String output = "";
                int exit = -1;
                try {
                    Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(p.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line).append("\n");
                    output = sb.toString().trim();
                    exit = p.waitFor();
                } catch (Exception e) {
                    output = "ERR: " + e.getMessage();
                }
                final String out = output;
                final int code = exit;
                main.post(new Runnable() {
                    @Override
                    public void run() {
                        cb.onResult(code, out);
                    }
                });
            }
        });
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pool.shutdownNow();
    }
}