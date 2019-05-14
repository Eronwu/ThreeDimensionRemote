package com.woo.threedimensionremote;

public class MathUtil {

    public static byte[] int2ByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    public static byte[] packetAxisBytes(byte[] x, byte[] y) {
        byte[] result = new byte[9];
        result[0] = 0;
        for (int i = 0; i < 4; i++) {
            result[i + 1] = x[i];
            result[i + 5] = y[i];
        }
        return result;
    }

    public static int bytes2Int(byte[] bytes) {
        int num = bytes[3] & 0xFF;
        num |= ((bytes[2] << 8) & 0xFF00);
        num |= ((bytes[1] << 16) & 0xFF0000);
        num |= ((bytes[0] << 24) & 0xFF000000);
        return num;
    }
}
