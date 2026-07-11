package com.example.app;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import java.io.File;

public class StorageManager {
    private static final String TAG = "StorageManager";
    private final Activity mActivity;
    private final AppConfig mConfig;

    public StorageManager(Activity activity, AppConfig config) {
        this.mActivity = activity;
        this.mConfig = config;
        Log.d(TAG, "StorageManager initialized.");
    }

    /**
     * Creates the public workspace directory under Documents/AHMHelloWorld/www.
     * Uses MediaStore on API 29+ (scoped storage) and legacy File API on older versions.
     */
    public void createPublicWorkspaceDirectory() {
        String folderName = mConfig.getWorkspaceFolderName();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = mActivity.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "placeholder.txt");
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOCUMENTS + "/" + folderName + "/www");
            Uri externalUri = MediaStore.Files.getContentUri("external");
            try {
                Uri fileUri = resolver.insert(externalUri, values);
                if (fileUri != null) {
                    resolver.delete(fileUri, null, null);
                    Log.i(TAG, "Workspace directory verified: Documents/" + folderName + "/www");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating workspace directory: " + e.getMessage());
            }
        } else {
            File legacyDocs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            if (legacyDocs != null) {
                File workspace = new File(new File(legacyDocs, folderName), "www");
                if (!workspace.exists()) {
                    boolean created = workspace.mkdirs();
                    if (created) {
                        Log.i(TAG, "Workspace directory created: " + workspace.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * Determines the startup path for the WebView.
     * Returns "/index.html" for the Hello World project.
     */
    public String determineStartupPath() {
        // Check if index.html exists in bundled assets
        try {
            String[] assetsList = mActivity.getAssets().list("www");
            if (assetsList != null) {
                for (String file : assetsList) {
                    if ("index.html".equals(file)) {
                        return "/index.html";
                    }
                }
            }
        } catch (Exception ignored) {}

        return "/index.html";
    }
}
