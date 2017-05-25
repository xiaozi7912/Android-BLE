package com.xiaozi.androidble.socket;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by xiaoz on 2017-05-25.
 */

public class SocketClient extends BaseSocket {
    private static SocketClient mInstance = null;
    private Callback mCallback = null;

    public SocketClient(Callback callback) {
        mCallback = callback;
    }

    public SocketClient(Callback callback, int port) {
        this(callback);
        mPort = port;
    }

    public static SocketClient getInstance(Callback callback) {
        if (mInstance == null) mInstance = new SocketClient(callback);
        return mInstance;
    }

    public void init(final String dstAddress) {
        Log.i(LOG_TAG, "init");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mConnectedSocket = new Socket();
                    InetSocketAddress socketAddress = new InetSocketAddress(dstAddress, mPort);
                    mConnectedSocket.connect(socketAddress);
                    mCallback.onConnected();
                    receive();
                } catch (IOException e) {
                    e.printStackTrace();
                    mCallback.onConnectFailure();
                }
            }
        }).start();
    }

    private void receive() {
        Log.i(LOG_TAG, "receive");
        try {
            InputStream inputStream = mConnectedSocket.getInputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int readSize = 0;
            while ((readSize = inputStream.read(buffer)) != -1) {
                String readString = new String(buffer, 0, readSize);
                Log.d(LOG_TAG, "receive readSize : " + readSize);
                Log.d(LOG_TAG, "receive readString : " + readString);
                mCallback.onReceived(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(byte[] buffer) {
        Log.i(LOG_TAG, "close");
        try {
            OutputStream outputStream = mConnectedSocket.getOutputStream();
            outputStream.write(buffer);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            mCallback.onSendFailure();
        }
    }

    public void close() {
        Log.i(LOG_TAG, "close");
        try {
            if (mConnectedSocket != null) mConnectedSocket.close();
            mCallback.onClosed();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface Callback {
        void onConnected();

        void onConnectFailure();

        void onReceived(byte[] buffer);

        void onReceiveFailure();

        void onSendFailure();

        void onClosed();
    }
}
