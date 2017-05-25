package com.xiaozi.androidble.socket;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;

/**
 * Created by xiaoz on 2017-05-25.
 */

public class SocketServer extends BaseSocket {
    private static SocketServer mInstance = null;
    private ServerSocket mServerSocket = null;
    private Callback mCallback = null;

    private boolean IS_SERVICE_RUNNING = false;

    public SocketServer(Callback callback) {
        mCallback = callback;
    }

    public SocketServer(Callback callback, int port) {
        this(callback);
        mPort = port;
    }

    public static SocketServer getInstance(Callback callback) {
        if (mInstance == null) mInstance = new SocketServer(callback);
        return mInstance;
    }

    public void init() {
        Log.i(LOG_TAG, "init");
        IS_SERVICE_RUNNING = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mServerSocket = new ServerSocket(mPort);
                    mCallback.onInitSuccess();
                    waitConnect();
                } catch (IOException e) {
                    e.printStackTrace();
                    mCallback.onInitFailure();
                }
            }
        }).start();
    }

    private void waitConnect() {
        Log.i(LOG_TAG, "waitConnect");
        while (IS_SERVICE_RUNNING) {
            try {
                mConnectedSocket = mServerSocket.accept();
                mCallback.onConnected();
                receive();
            } catch (IOException e) {
                e.printStackTrace();
                mCallback.onConnectFailure();
            }
        }
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
        IS_SERVICE_RUNNING = false;
        try {
            if (mConnectedSocket != null) mConnectedSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCallback.onClosed();
    }

    public interface Callback {
        void onInitSuccess();

        void onInitFailure();

        void onConnected();

        void onConnectFailure();

        void onReceived(byte[] buffer);

        void onReceiveFailure();

        void onSendFailure();

        void onClosed();
    }
}
