package com.asdeveloper.astube;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.app.DownloadManager;
import android.content.Context;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG          = "ASTUBE";
    private static final String TWA_URL      = "https://asdeveloperszone.github.io/ASTUBE2/index.html";
    private static final int    PERM_REQUEST = 100;

    private WebView webView;

    // ── JavaScript Bridge ──────────────────────────────────────────────────────
    public class ASTUBEBridge {

        @JavascriptInterface
        public void setDownloadTitle(String title) {
            getSharedPreferences("astube", Context.MODE_PRIVATE)
                    .edit().putString("pending_download_title", title).apply();
        }

        @JavascriptInterface
        public String getASTUBEFolder() {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File astube = new File(dir, "ASTUBE");
            return astube.getAbsolutePath();
        }

        @JavascriptInterface
        public String listVideos() {
            try {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File astube = new File(dir, "ASTUBE");
                if (!astube.exists()) astube.mkdirs();

                String[] exts = {".mp4", ".mkv", ".webm", ".avi", ".3gp", ".mov", ".m4v"};
                JSONArray arr = new JSONArray();

                File[] files = astube.listFiles();
                if (files != null) {
                    for (File f : files) {
                        String name = f.getName().toLowerCase();
                        for (String ext : exts) {
                            if (name.endsWith(ext)) {
                                JSONObject obj = new JSONObject();
                                obj.put("name", f.getName());
                                obj.put("path", f.getAbsolutePath());
                                obj.put("size", f.length());
                                obj.put("lastModified", f.lastModified());
                                obj.put("uri", Uri.fromFile(f).toString());
                                arr.put(obj);
                                break;
                            }
                        }
                    }
                }
                return arr.toString();
            } catch (Exception e) {
                Log.e(TAG, "listVideos error", e);
                return "[]";
            }
        }

        @JavascriptInterface
        public boolean isAndroid() {
            return true;
        }

        @JavascriptInterface
        public String getDownloadsPath() {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            return new File(dir, "ASTUBE").getAbsolutePath();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createASTUBEFolder();

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Inject bridge as window.ASTUBEAndroid
        webView.addJavascriptInterface(new ASTUBEBridge(), "ASTUBEAndroid");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false; // stay in app
            }
        });

        webView.setWebChromeClient(new WebChromeClient());

        // ── Download to Downloads/ASTUBE/ ──────────────────────
        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            try {
                // Get filename from content disposition or URL
                String filename = "video_" + System.currentTimeMillis() + ".mp4";
                if (contentDisposition != null && contentDisposition.contains("filename=")) {
                    String[] parts = contentDisposition.split("filename=");
                    if (parts.length > 1) {
                        filename = parts[1].replace("\"", "").trim();
                    }
                }
                // Get title from JS bridge if available (set by player page)
                String savedTitle = getSharedPreferences("astube", Context.MODE_PRIVATE)
                        .getString("pending_download_title", null);
                if (savedTitle != null && !savedTitle.isEmpty()) {
                    // Sanitize title for filename
                    savedTitle = savedTitle.replaceAll("[^a-zA-Z0-9\\s\\-_]", "").trim();
                    if (!savedTitle.isEmpty()) filename = savedTitle + ".mp4";
                    getSharedPreferences("astube", Context.MODE_PRIVATE).edit()
                            .remove("pending_download_title").apply();
                }

                DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
                req.setMimeType(mimeType);
                req.addRequestHeader("User-Agent", userAgent);
                req.setTitle(filename);
                req.setDescription("Downloading via ASTUBE");
                req.setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                // Save to Downloads/ASTUBE/
                req.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, "ASTUBE/" + filename);

                DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                dm.enqueue(req);

                android.widget.Toast.makeText(MainActivity.this,
                        "Downloading to ASTUBE folder...",
                        android.widget.Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Log.e(TAG, "Download error", e);
            }
        });

        requestStoragePermissions();
    }

    private void createASTUBEFolder() {
        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File astube = new File(dir, "ASTUBE");
            if (!astube.exists()) astube.mkdirs();
            Log.d(TAG, "ASTUBE folder: " + astube.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Failed to create folder", e);
        }
    }

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_VIDEO}, PERM_REQUEST);
            } else {
                loadApp();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }, PERM_REQUEST);
            } else {
                loadApp();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        loadApp();
    }

    private void loadApp() {
        webView.loadUrl(TWA_URL);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }
}
