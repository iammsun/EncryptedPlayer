package com.sunmeng.mediaplayer;

/**
 * Created by sunmeng on 16/8/7.
 */
public class EncryptImpl implements IEncrypt {

    @Override
    public String getSignature() {
        return "encrypted_by_sunmeng";
    }

    @Override
    public byte[] encrypt(final byte[] data, int offset, int length) {
        if (data == null) {
            return null;
        }
        byte[] result = new byte[data.length];
        for (int i = offset; i < length; ++i) {
            result[i] = (byte) (data[i] ^ Byte.MAX_VALUE);
        }
        return result;
    }

    @Override
    public byte[] decrypt(final byte[] data, int offset, int length) {
        if (data == null) {
            return null;
        }
        byte[] result = new byte[data.length];
        for (int i = offset; i < length; ++i) {
            result[i] = (byte) (data[i] ^ Byte.MAX_VALUE);
        }
        return result;
    }
}
