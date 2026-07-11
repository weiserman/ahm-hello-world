package com.example.app;

import android.content.Context;
import android.util.Log;

public class AppConfig {
    private static final String TAG = "AppConfig";

    public static final String STORAGE_MODE_SANDBOX = "sandbox";
    public static final String STORAGE_MODE_PUBLIC = "public";

    private final Context context;

    public AppConfig(Context context) {
        this.context = context.getApplicationContext();
        Log.d(TAG, "AppConfig initialized.");
    }

    public String getVirtualHost() {
        String host = context.getString(R.string.virtual_host);
        if (host == null || host.isEmpty()) return "";
        return host.startsWith("https://") ? host : "https://" + host;
    }

    public String getWorkspaceFolderName() {
        return context.getString(R.string.config_workspace_folder_name);
    }

    public String getStorageMode() {
        return STORAGE_MODE_SANDBOX;
    }

    public boolean isPublicWorkspaceEnabled() {
        return STORAGE_MODE_PUBLIC.equals(getStorageMode());
    }
}
