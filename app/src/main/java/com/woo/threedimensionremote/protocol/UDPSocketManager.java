package com.woo.threedimensionremote.protocol;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.woo.threedimensionremote.SwingDotActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class UDPSocketManager {
    private static final String TAG = "UDPSocketManager";
    private final int PORT_NUM = 6666;
    private static UDPSocketManager mUDPSocketManager;
    private DatagramSocket mDatagramSocket;
    private InetAddress mServerAddress;

    public UDPSocketManager() {
    }

    public static UDPSocketManager getInstance() {
        if (mUDPSocketManager == null) {
            mUDPSocketManager = new UDPSocketManager();
        }

        return mUDPSocketManager;
    }

    // TODO: need to auto get server ip
    public void init(Context context) {
        if (SwingDotActivity.mStringServerIP != null) {
            Log.d(TAG, "run: start init");
        } else {
            Toast.makeText(context, "pls input box ip!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    mDatagramSocket = new DatagramSocket(PORT_NUM);
//                    if (mDatagramSocket == null) {
//                        Log.d(TAG, "start mDatagramSocket.bind");
//                        mDatagramSocket = new DatagramSocket(null);
//                        mDatagramSocket.setReuseAddress(true);
//                        mDatagramSocket.bind(new InetSocketAddress(PORT_NUM));
//                        Log.d(TAG, "mDatagramSocket.bind");
//                    }
                    mServerAddress = InetAddress.getByName(SwingDotActivity.mStringServerIP);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void sendData(byte[] data) {
        if (data.length == 0) {
            return;
        }

        // need use thread or will cause android.os.NetworkOnMainThreadException error.
        new UDPSendThread(data).start();
    }

    private class UDPSendThread extends Thread{
        byte[] data;

        public UDPSendThread(byte[] data) {
            this.data = data;
        }

        @Override
        public void run() {
            if (mDatagramSocket != null) {
                try {
                    Log.d(TAG, "sendData: send data: " + data.length);
                    DatagramPacket datagramPacket = new DatagramPacket(data, data.length, mServerAddress, PORT_NUM);
                    if (datagramPacket == null)
                        Log.e(TAG, "sendData: null!");
                    else
                        mDatagramSocket.send(datagramPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public byte[] receiveData() {

        return null;
    }

    public void deInit() {
        if (mDatagramSocket != null)
            mDatagramSocket.close();
    }
}
