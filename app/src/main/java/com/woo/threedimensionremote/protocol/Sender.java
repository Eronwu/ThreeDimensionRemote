package com.woo.threedimensionremote.protocol;

import android.content.Context;

// USE TCP Socket as default connect solution
public class Sender {
    private static Sender mSender;

    public static Sender getInstance() {
        if (mSender == null) {
            mSender = new Sender();
        }

        return mSender;
    }

    public void init(Context context) {
//        TCPSocketManager.getInstance().init(context);
        UDPSocketManager.getInstance().init(context);
    }

    public void sendData(byte[] data) {
//        TCPSocketManager.getInstance().sendData(data);
        UDPSocketManager.getInstance().sendData(data);
    }

    public byte[] receiveData() {
        byte[] data = TCPSocketManager.getInstance().receiveData();

        return data;
    }

    public void deInit() {
//        TCPSocketManager.getInstance().deInit();
        UDPSocketManager.getInstance().deInit();
    }
}
