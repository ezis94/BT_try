package com.example.edgars.bt_try;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mDevice;
    ConnectThread mConnectThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView t;


        t = (TextView) findViewById(R.id.text);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            t.setText("do not work work work");
        }
       if (!mBluetoothAdapter.isEnabled()) {
            t.setText(" work work work");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
       Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mDevice = device;
                Toast.makeText(getApplicationContext(), "device found", Toast.LENGTH_SHORT).show();
            }

        }

         mConnectThread = new ConnectThread(mDevice);
        mConnectThread.start();
    }
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                Toast.makeText(getApplicationContext(), "almost connected", Toast.LENGTH_SHORT).show();
            } catch (IOException e) { }
            mmSocket = tmp;
        }
        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
//                mHandler.obtainMessage(3, begin, i, buffer).sendToTarget();

            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }
            ConnectedThread mConnectedThread = new ConnectedThread(mmSocket);
            mConnectedThread.start();
            mConnectedThread.write(("*").getBytes());
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;


        }
        public void run() {
            byte[] buffer = new byte[1024];
            int begin = 0;
            int bytes = 0;
            while (true) {
                try {
                    bytes += mmInStream.read(buffer, bytes, buffer.length - bytes);
                    for(int i = begin; i < bytes; i++) {
                        if(buffer[i] == "#".getBytes()[0]) {

                            mHandler.obtainMessage(1, begin, i, buffer).sendToTarget();
                            begin = i + 1;
                            if(i == bytes - 1) {
                                bytes = 0;
                                begin = 0;
                            }
                        }
                    }
                } catch (IOException e) {
                    break;
                }
            }

        }
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                Message msg = mHandler.obtainMessage(2);
                Bundle bundle = new Bundle();
                bundle.putString("whatever", "name");
                msg.setData(bundle);

                mHandler.sendMessage(msg);
            } catch (IOException e) { }
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            byte[] writeBuf = (byte[]) msg.obj;
            int begin = (int)msg.arg1;
            int end = (int)msg.arg2;

            switch(msg.what) {
                case 1:
                    String writeMessage = new String(writeBuf);
                    writeMessage = writeMessage.substring(begin, end);
                    break;
                case 2:
            Toast.makeText(getApplicationContext(), "succes1", Toast.LENGTH_SHORT).show();

                    break;
                case 3:

                    break;
            }
        }
    };
}
