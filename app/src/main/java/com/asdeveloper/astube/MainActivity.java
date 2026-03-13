package com.asdeveloper.astube;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.androidbrowserhelper.trusted.TwaLauncher;
import com.google.androidbrowserhelper.trusted.TrustedWebActivityIntentBuilder;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG     = "ASTUBE";
    private static final String TWA_URL = "https://asdeveloperszone.github.io/ASTUBE2/index.html";
    private static final int    PERM_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create ASTUBE folder in Downloads on first run
        createASTUBEFolder();

        // Request storage permissions
        requestStoragePermissions();
    }

    private void createASTUBEFolder() {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File astubeDir = new File(downloadsDir, "ASTUBE");
            if (!astubeDir.exists()) {
                boolean created = astubeDir.mkdirs();
                Log.d(TAG, "ASTUBE folder created: " + created + " at " + astubeDir.getAbsolutePath());
            } else {
                Log.d(TAG, "ASTUBE folder exists: " + astubeDir.getAbsolutePath());
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to create ASTUBE folder", e);
        }
    }

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ — use READ_MEDIA_VIDEO
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_VIDEO},
                        PERM_REQUEST);
            } else {
                launchTWA();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10-12 — no write permission needed, just launch
            launchTWA();
        } else {
            // Android 9 and below — request read/write
            boolean readGranted  = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            boolean writeGranted = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

            if (!readGranted || !writeGranted) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        PERM_REQUEST);
            } else {
                launchTWA();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_REQUEST) {
            // Launch regardless — app works without storage permission
            // (just can't access local videos)
            launchTWA();
        }
    }

    private void launchTWA() {
        Uri uri = Uri.parse(TWA_URL);

        TrustedWebActivityIntentBuilder builder =
                new TrustedWebActivityIntentBuilder(uri)
                        .setNavigationBarColor(0xFF000000)
                        .setToolbarColor(0xFF000000)
                        .setNavigationBarDividerColor(0xFF000000);

        new TwaLauncher(this).launch(builder, null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
