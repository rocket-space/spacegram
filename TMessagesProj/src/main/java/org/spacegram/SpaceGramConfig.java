package org.spacegram;

import android.app.Activity;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

public class SpaceGramConfig {

    private static final Object sync = new Object();
    private static boolean configLoaded;

    public static int translateStyle = 0;
    public static int translateProvider = 1;
    public static String translateTargetLang = "";
    public static String translateSkipLang = "";
    public static boolean autoTranslate = false;

    public static boolean contextShowUserId = true;
    public static boolean contextSaveMediaQuick = true;
    public static boolean contextCopyMessageLink = true;
    public static boolean contextPinShortcuts = true;
    public static boolean contextForwardProToggle = true;

    public static boolean forwardProEnabled = true;
    public static boolean forwardProMultipleOption = false;
    public static boolean forwardProConfirmAlert = false;
    public static boolean forwardProDrawingMulti = false;
    public static int forwardProRepeatCount = 5;

    // 0 = normal, 1 = fast, 2 = extreme
    public static int networkUploadSpeedMode = 0;
    public static int networkDownloadSpeedMode = 0;

    @Deprecated
    public static int networkSpeedMode = 0;

    static {
        loadConfig();
    }

    public static void loadConfig() {
        synchronized (sync) {
            if (configLoaded) {
                return;
            }
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("spacegram_config", Activity.MODE_PRIVATE);
            translateStyle = preferences.getInt("translateStyle", 0);
            translateProvider = preferences.getInt("translateProvider", 1);
            translateTargetLang = preferences.getString("translateTargetLang", "");
            translateSkipLang = preferences.getString("translateSkipLang", "");
            autoTranslate = preferences.getBoolean("autoTranslate", false);
            contextShowUserId = preferences.getBoolean("contextShowUserId", true);
            contextSaveMediaQuick = preferences.getBoolean("contextSaveMediaQuick", true);
            contextCopyMessageLink = preferences.getBoolean("contextCopyMessageLink", true);
            contextPinShortcuts = preferences.getBoolean("contextPinShortcuts", true);
            contextForwardProToggle = preferences.getBoolean("contextForwardProToggle", true);

            forwardProEnabled = preferences.getBoolean("forwardProEnabled", true);
            forwardProMultipleOption = preferences.getBoolean("forwardProMultipleOption", false);
            forwardProConfirmAlert = preferences.getBoolean("forwardProConfirmAlert", false);
            forwardProDrawingMulti = preferences.getBoolean("forwardProDrawingMulti", false);
            forwardProRepeatCount = clampRepeatCount(preferences.getInt("forwardProRepeatCount", 5));

            networkUploadSpeedMode = clampSpeedMode(preferences.getInt("networkUploadSpeedMode", -1));
            networkDownloadSpeedMode = clampSpeedMode(preferences.getInt("networkDownloadSpeedMode", -1));

            // Backward compatibility with old unified setting.
            networkSpeedMode = clampSpeedMode(preferences.getInt("networkSpeedMode", 0));
            if (networkUploadSpeedMode < 0) {
                networkUploadSpeedMode = networkSpeedMode;
            }
            if (networkDownloadSpeedMode < 0) {
                networkDownloadSpeedMode = networkSpeedMode;
            }

            configLoaded = true;
        }
    }

    public static void saveConfig() {
        synchronized (sync) {
            try {
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("spacegram_config", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("translateStyle", translateStyle);
                editor.putInt("translateProvider", translateProvider);
                editor.putString("translateTargetLang", translateTargetLang);
                editor.putString("translateSkipLang", translateSkipLang);
                editor.putBoolean("autoTranslate", autoTranslate);
                editor.putBoolean("contextShowUserId", contextShowUserId);
                editor.putBoolean("contextSaveMediaQuick", contextSaveMediaQuick);
                editor.putBoolean("contextCopyMessageLink", contextCopyMessageLink);
                editor.putBoolean("contextPinShortcuts", contextPinShortcuts);
                editor.putBoolean("contextForwardProToggle", contextForwardProToggle);
                editor.putBoolean("forwardProEnabled", forwardProEnabled);
                editor.putBoolean("forwardProMultipleOption", forwardProMultipleOption);
                editor.putBoolean("forwardProConfirmAlert", forwardProConfirmAlert);
                editor.putBoolean("forwardProDrawingMulti", forwardProDrawingMulti);
                editor.putInt("forwardProRepeatCount", clampRepeatCount(forwardProRepeatCount));
                editor.putInt("networkUploadSpeedMode", networkUploadSpeedMode);
                editor.putInt("networkDownloadSpeedMode", networkDownloadSpeedMode);

                // Keep legacy key updated with the strongest selected mode.
                networkSpeedMode = Math.max(networkUploadSpeedMode, networkDownloadSpeedMode);
                editor.putInt("networkSpeedMode", networkSpeedMode);
                editor.apply();
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    public static int getUploadBoostMultiplier() {
        return getBoostMultiplier(networkUploadSpeedMode);
    }

    public static int getDownloadBoostMultiplier() {
        return getBoostMultiplier(networkDownloadSpeedMode);
    }

    private static int getBoostMultiplier(int mode) {
        switch (clampSpeedMode(mode)) {
            case 1:
                return 2;
            case 2:
                return 3;
            default:
                return 1;
        }
    }

    private static int clampSpeedMode(int mode) {
        if (mode < 0) {
            return -1;
        }
        return Math.max(0, Math.min(2, mode));
    }

    public static int clampRepeatCount(int count) {
        return Math.max(1, Math.min(20, count));
    }
}
