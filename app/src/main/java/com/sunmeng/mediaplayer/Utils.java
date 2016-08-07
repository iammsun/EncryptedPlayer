package com.sunmeng.mediaplayer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.RecoverySystem;
import android.support.v4.content.ContextCompat;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by sunmeng on 16/8/6.
 */
public class Utils {

    private static final int TIME_UNIT = 60;

    public static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        if (seconds < TIME_UNIT) {
            return String.format("00:%02d", seconds);
        }
        long mins = seconds / TIME_UNIT;
        if (mins < TIME_UNIT) {
            return String.format("%02d:%02d", mins, seconds % TIME_UNIT);
        }
        long hours = mins / TIME_UNIT;
        return String.format("%d:%02d:%02d", hours, mins % TIME_UNIT, seconds % TIME_UNIT);
    }

    public static void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
        }
    }

    public static String generateCacheFilePath(Context context, String ext) {
        return new File(Environment.getExternalStorageDirectory(), context.getPackageName() + File
                .separator + System.currentTimeMillis() + "." + ext).getAbsolutePath();
    }

    public static boolean permissionCheck(Context context, String... permissions) {
        for (String permission : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context,
                    permission)) {
                return false;
            }
        }
        return true;
    }
}
