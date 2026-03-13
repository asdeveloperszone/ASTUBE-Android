package com.asdeveloper.astube;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;

/**
 * Handles download completion — renames "videoplayback" files
 * to proper titles and moves them to Downloads/ASTUBE/
 */
public class ASTUBEFileService extends Service {

    private static final String TAG = "ASTUBEFileService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && DownloadManager.ACTION_DOWNLOAD_COMPLETE
                .equals(intent.getAction())) {
            long downloadId = intent.getLongExtra(
                    DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadId != -1) {
                handleDownloadComplete(downloadId);
            }
        }
        return START_NOT_STICKY;
    }

    private void handleDownloadComplete(long downloadId) {
        DownloadManager dm = (DownloadManager) getSystemService(
                Context.DOWNLOAD_SERVICE);
        if (dm == null) return;

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        try (Cursor cursor = dm.query(query)) {
            if (cursor != null && cursor.moveToFirst()) {
                int statusIdx = cursor.getColumnIndex(
                        DownloadManager.COLUMN_STATUS);
                int uriIdx = cursor.getColumnIndex(
                        DownloadManager.COLUMN_LOCAL_URI);

                if (statusIdx >= 0 && cursor.getInt(statusIdx)
                        == DownloadManager.STATUS_SUCCESSFUL) {
                    String localUri = uriIdx >= 0 ? cursor.getString(uriIdx) : null;
                    if (localUri != null) {
                        moveToASTUBEFolder(Uri.parse(localUri));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling download", e);
        }
    }

    private void moveToASTUBEFolder(Uri fileUri) {
        try {
            String path = fileUri.getPath();
            if (path == null) return;

            File srcFile = new File(path);
            if (!srcFile.exists()) return;

            // Create ASTUBE folder
            File astubeDir = new File(
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS),
                    "ASTUBE");
            if (!astubeDir.exists()) astubeDir.mkdirs();

            // Keep original name if not "videoplayback"
            String name = srcFile.getName();
            if (name.startsWith("videoplayback")) {
                // Try to get a better name from title stored in prefs
                String savedTitle = getSharedPreferences("astube_prefs", MODE_PRIVATE)
                        .getString("pending_dl_title", null);
                if (savedTitle != null && !savedTitle.isEmpty()) {
                    name = savedTitle.replaceAll("[^a-zA-Z0-9\\s\\-_]", "")
                            .trim()
                            .replaceAll("\\s+", "_") + ".mp4";
                    getSharedPreferences("astube_prefs", MODE_PRIVATE)
                            .edit().remove("pending_dl_title").apply();
                }
            }

            File destFile = new File(astubeDir, name);
            // Avoid overwrite
            int counter = 1;
            while (destFile.exists()) {
                String base = name.replace(".mp4", "");
                destFile = new File(astubeDir, base + "_" + counter + ".mp4");
                counter++;
            }

            boolean moved = srcFile.renameTo(destFile);
            Log.d(TAG, "Moved to ASTUBE folder: " + moved + " → " + destFile.getAbsolutePath());

        } catch (Exception e) {
            Log.e(TAG, "Error moving file", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
