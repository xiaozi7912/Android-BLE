package com.xiaozi.androidble.socket;

import java.net.Socket;

/**
 * Created by xiaoz on 2017-05-25.
 */

public class BaseSocket {
    protected final String LOG_TAG = getClass().getSimpleName();
    protected Socket mConnectedSocket = null;

    protected final static int DEFAULT_SOCKET_PORT = 9001;
    protected final static int BUFFER_SIZE = 512;

    protected int mPort = DEFAULT_SOCKET_PORT;
}
