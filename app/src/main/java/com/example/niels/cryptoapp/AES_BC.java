package com.example.niels.cryptoapp;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;

public class AES_BC {

    private static byte[] cipherData(PaddedBufferedBlockCipher cipher, byte[] data)
            throws Exception
    {
        int minSize = cipher.getOutputSize(data.length);
        byte[] outBuf = new byte[minSize];
        int length1 = cipher.processBytes(data, 0, data.length, outBuf, 0);
        int length2 = cipher.doFinal(outBuf, length1);
        int actualLength = length1 + length2;
        byte[] result = new byte[actualLength];
        System.arraycopy(outBuf, 0, result, 0, result.length);
        return result;
    }

    public static byte[] encrypt(byte[] plain, byte[] key) throws Exception
    {
        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(
                new AESEngine());
        CipherParameters Key = new KeyParameter(key);
        aes.init(true, Key);
        return cipherData(aes, plain);
    }
}