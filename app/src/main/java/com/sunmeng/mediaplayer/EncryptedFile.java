package com.sunmeng.mediaplayer;

import android.support.annotation.NonNull;

import com.sunmeng.mediaplayer.downloader.IEncrypt;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by sunmeng on 16/8/7.
 */
public class EncryptedFile {

    private final IEncrypt encrypt;
    private final File target;

    public EncryptedFile(@NonNull IEncrypt encrypt, @NonNull File target) {
        this.encrypt = encrypt;
        this.target = target;
    }

    public boolean isEncrypted() {
        FileInputStream is = null;
        try {
            is = new FileInputStream(target);
            byte[] buffer = new byte[encrypt.getSignature().getBytes().length];
            int len = is.read(buffer);
            return len != -1 && encrypt.getSignature().equals(new String(buffer));
        } catch (Exception e) {
            return false;
        } finally {
            Utils.close(is);
        }
    }

    public long length() {
        return isEncrypted() ? (target.length() - encrypt.getSignature().getBytes().length) :
                target.length();
    }
}
