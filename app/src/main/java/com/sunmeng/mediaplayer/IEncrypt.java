package com.sunmeng.mediaplayer;

import android.support.annotation.NonNull;

/**
 * Created by sunmeng on 16/8/7.
 */
public interface IEncrypt {

    @NonNull
    String getSignature();

    byte[] encrypt(final byte[] data, int offset, int length);

    byte[] decrypt(final byte[] data, int offset, int length);
}
