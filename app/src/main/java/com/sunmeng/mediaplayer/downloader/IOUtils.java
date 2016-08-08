package com.sunmeng.mediaplayer.downloader;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by sunmeng on 16/8/8.
 */
public class IOUtils {

    public static void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
        }
    }
}
