package com.xiaozi.androidble;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xiaozi.androidble.socket.SocketClient;
import com.xiaozi.androidble.socket.SocketServer;

/**
 * Created by xiaoz on 2017-05-25.
 */

public class SocketActivity extends BaseActivity {
    private TextView mIPAddressText = null;
    private Button mStartServerButton = null;
    private EditText mDstAddressInput = null;
    private Button mConnectServerButton = null;
    private Button mDisconnectServerButton = null;
    private EditText mSendTextInput = null;
    private Button mSendButton = null;

    private SocketServer mSocketServer = null;
    private SocketClient mSocketClient = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);

        initView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSocketServer != null) mSocketServer.close();
    }

    private void initView() {
        mIPAddressText = (TextView) findViewById(R.id.main_ip_address_text);
        mStartServerButton = (Button) findViewById(R.id.socket_start_server_button);
        mDstAddressInput = (EditText) findViewById(R.id.socket_dst_address_input);
        mConnectServerButton = (Button) findViewById(R.id.socket_connect_server_button);
        mDisconnectServerButton = (Button) findViewById(R.id.socket_disconnect_server_button);
        mSendTextInput = (EditText) findViewById(R.id.socket_send_text_input);
        mSendButton = (Button) findViewById(R.id.socket_send_button);

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        String strAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        mIPAddressText.setText(strAddress);
        mDstAddressInput.setText(strAddress);
        mSendButton.setEnabled(false);

        mStartServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartServerClick();
            }
        });
        mConnectServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConnectServerClick();
            }
        });
        mDisconnectServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSocketClient.close();
            }
        });
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sendMessage = mSendTextInput.getText().toString();
                mSocketClient.send(sendMessage.getBytes());
            }
        });
    }

    private void onStartServerClick() {
        Log.i(LOG_TAG, "onStartServerClick");
        mSocketServer = SocketServer.getInstance(new SocketServer.Callback() {
            @Override
            public void onInitSuccess() {
                Log.i(LOG_TAG, "SocketServer onInitSuccess");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mStartServerButton.setEnabled(false);
                    }
                });
            }

            @Override
            public void onInitFailure() {
                Log.i(LOG_TAG, "SocketServer onInitFailure");
                mSocketServer.close();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mStartServerButton.setEnabled(true);
                    }
                });
            }

            @Override
            public void onConnected() {
                Log.i(LOG_TAG, "SocketServer onConnected");
            }

            @Override
            public void onConnectFailure() {
                Log.i(LOG_TAG, "SocketServer onConnectFailure");
            }

            @Override
            public void onReceived(byte[] buffer) {
                Log.i(LOG_TAG, "SocketServer onReceived");
            }

            @Override
            public void onReceiveFailure() {
                Log.i(LOG_TAG, "SocketServer onReceiveFailure");
            }

            @Override
            public void onSendFailure() {
                Log.i(LOG_TAG, "SocketServer onSendFailure");
            }

            @Override
            public void onClosed() {
                Log.i(LOG_TAG, "SocketServer onClosed");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mStartServerButton.setEnabled(true);
                    }
                });
            }
        });
        mSocketServer.init();
    }

    private void onConnectServerClick() {
        Log.i(LOG_TAG, "onConnectServerClick");
        String dstAddress = mDstAddressInput.getText().toString();
        Log.d(LOG_TAG, "onConnectServerClick dstAddress : " + dstAddress);
        mSocketClient = SocketClient.getInstance(new SocketClient.Callback() {
            @Override
            public void onConnected() {
                Log.i(LOG_TAG, "SocketClient onConnected");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mConnectServerButton.setEnabled(false);
                        mDisconnectServerButton.setEnabled(true);
                        mSendButton.setEnabled(true);
                    }
                });
            }

            @Override
            public void onConnectFailure() {
                Log.i(LOG_TAG, "SocketClient onConnectFailure");
            }

            @Override
            public void onReceived(byte[] buffer) {
                Log.i(LOG_TAG, "SocketClient onReceived");
            }

            @Override
            public void onReceiveFailure() {
                Log.i(LOG_TAG, "SocketClient onReceiveFailure");
            }

            @Override
            public void onSendFailure() {
                Log.i(LOG_TAG, "SocketClient onSendFailure");
            }

            @Override
            public void onClosed() {
                Log.i(LOG_TAG, "SocketClient onClosed");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mConnectServerButton.setEnabled(true);
                        mDisconnectServerButton.setEnabled(false);
                        mSendButton.setEnabled(false);
                    }
                });
            }
        });
        mSocketClient.init(dstAddress);
    }
}
