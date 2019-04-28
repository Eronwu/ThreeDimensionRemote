package com.woo.threedimensionremote.protocol;

import android.content.Context;
import android.widget.Toast;

import com.woo.threedimensionremote.SwingDotActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class TCPSocketManager {
    private static TCPSocketManager mTCPSocketManager;
    private Socket mSocket;
    OutputStream mOutputStream;

    public TCPSocketManager() {
    }

    public static TCPSocketManager getInstance() {
        if (mTCPSocketManager == null) {
            mTCPSocketManager = new TCPSocketManager();
        }

        return mTCPSocketManager;
    }

    // TODO: need to auto get server ip
    public void init(Context context) {
        if (SwingDotActivity.mStringServerIP != null) {
            Toast.makeText(context, SwingDotActivity.mStringServerIP, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "pls input box ip!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            mSocket = new Socket(SwingDotActivity.mStringServerIP, 6666);
            if (mSocket != null) {
                mOutputStream = mSocket.getOutputStream();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendData(byte[] data) {
        if (mSocket != null && mOutputStream != null) {
            try {
                mOutputStream.write(data);
                mOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] receiveData() {

        return null;
    }

    public void deInit() {
        try {
            if (mOutputStream != null)
                mOutputStream.close();
            if (mSocket != null)
                mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
