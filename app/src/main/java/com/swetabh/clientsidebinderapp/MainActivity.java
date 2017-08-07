package com.swetabh.clientsidebinderapp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Flag to get random number
    public static final int GET_RANDOM_NUMBER_FLAG = 0;

    //server side package name
    private static final String SERVER_SIDE_APP_PKG = "com.swetabh.serversideapp";

    // server side service class name
    private static final String SERVER_SIDE_SERVICE_NAME = "com.swetabh.serversideapp.MyService";

    private static final String TAG = MainActivity.class.getSimpleName();
    // boolean to check whether the service is bound or not
    private boolean mIsBound;

    // variable to get random number
    private int randomNumberValue;

    // messenger to request random number
    private Messenger randomNumberRequestMessenger;

    // messenger to receive random number
    private Messenger randomNumberReceiveMessenger;
    ServiceConnection randomNumberServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            randomNumberRequestMessenger = new Messenger(service);
            randomNumberReceiveMessenger = new Messenger(new ReceiveRandomNumberHandler());
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            randomNumberReceiveMessenger = null;
            randomNumberRequestMessenger = null;
            mIsBound = false;
        }
    };
    private TextView textViewRandomNumber;
    private Button buttonBindService, buttonUnBindService, buttonGetRandomNumber;

    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewRandomNumber = (TextView) findViewById(R.id.textView);

        buttonBindService = (Button) findViewById(R.id.button);
        buttonUnBindService = (Button) findViewById(R.id.button2);
        buttonGetRandomNumber = (Button) findViewById(R.id.button3);

        buttonGetRandomNumber.setOnClickListener(this);
        buttonBindService.setOnClickListener(this);
        buttonUnBindService.setOnClickListener(this);

        serviceIntent = new Intent();
        serviceIntent.setComponent(new ComponentName(SERVER_SIDE_APP_PKG, SERVER_SIDE_SERVICE_NAME));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                bindToRemoteService();
                break;
            case R.id.button2:
                unbindFromRemoteService();
                break;
            case R.id.button3:
                fetchRandomNumber();
                break;
            default:
                break;
        }
    }

    private void fetchRandomNumber() {
        if (mIsBound) {
            Message requestMessage = Message.obtain(null, GET_RANDOM_NUMBER_FLAG);
            requestMessage.replyTo = randomNumberReceiveMessenger;
            try {
                randomNumberRequestMessenger.send(requestMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Service is unbound can't get random number", Toast.LENGTH_SHORT).show();
        }


    }

    private void unbindFromRemoteService() {
        unbindService(randomNumberServiceConnection);
        mIsBound = false;
        Toast.makeText(this, "Service Unbound", Toast.LENGTH_SHORT).show();
    }

    private void bindToRemoteService() {
        bindService(serviceIntent, randomNumberServiceConnection, BIND_AUTO_CREATE);
        Toast.makeText(this, "Service bound", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        randomNumberServiceConnection = null;
    }

    /**
     * custom handler class to receive random number
     */
    class ReceiveRandomNumberHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            randomNumberValue = 0;
            switch (msg.what) {
                case GET_RANDOM_NUMBER_FLAG:
                    randomNumberValue = msg.arg1;
                    textViewRandomNumber.setText("Random Number : " + randomNumberValue);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
