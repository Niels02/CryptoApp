package com.example.niels.cryptoapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class MainCrypto extends AppCompatActivity {

    outputData out;
    String keyStr;// = "ffffffffffffffffffffffffffffffffffffffffffffffff"; default
    int numRuns;
    int runs;
    int sleepTime;

    UsbManager mUsbManager;
    UsbAccessory mUsbAccessory;

    ParcelFileDescriptor mFileDescriptor;
    FileInputStream mInputStream;
    FileOutputStream mOutputStream;

    private static final String ACTION_USB_PERMISSION = "com.example.niels.aesapp.USB_PERMISSION";

    public MainCrypto() {
        runs = 0;
    }

    private class MyTaskParams {
        int maxRuns;
        byte[] key;
        int sleepTime;
        outputData outFile;

        MyTaskParams(byte[] key, int maxRuns, int sleepTime, outputData outFile) {
            this.maxRuns = maxRuns;
            this.key = key;
            this.sleepTime = sleepTime;
            this.outFile = outFile;
        }
    }

    private class runEncryption extends AsyncTask<MyTaskParams, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(MyTaskParams... params) {
            for (int i = 0; i<params[0].maxRuns; i++) {
                //Plaintext length = 15 bytes + padding '01'
                byte[] plain = new byte[15];
                new Random().nextBytes(plain);
                byte[] cipher;
//                String plainStr = bytesToHex(plain);
                try {
//                    params[0].outFile.write(plainStr);
                    cipher = AES_BC.encrypt(plain, params[0].key);
//                    String hexString = new String(Hex.encodeHex(cipher));
//                    System.out.println(hexString);
                    Thread.sleep(params[0].sleepTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

//    protected void onProgressUpdate() {
//        setProgressPercent(progress[0]);
//    }

        protected void onPostExecute(Boolean finish) {
            ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
            toggle.setChecked(!finish);
        }
    }

    private class GoEnc {
        runEncryption runEnc;

        public void start (MyTaskParams params) {
            this.runEnc = new runEncryption();
            this.runEnc.execute(params);
        }

        public void stop() {
            this.runEnc.cancel(false);
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void usb() {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
//        mPermissionIntent = PendingIntent.getBroadcast(this)
//        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
//        while(deviceIterator.hasNext()){
//            UsbDevice device = deviceIterator.next();
//            System.out.println("device:");
//            System.out.println(device.getDeviceName());
//            Toast.makeText(getApplicationContext(), "msg msg", Toast.LENGTH_SHORT).show();
//            //your code
//        }
        System.out.println("aa");

    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("USB", "USB registering");
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    mUsbAccessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(mUsbAccessory != null){
                            //call method to set up accessory communication
                            Log.d("USB", "USB registered");
                        }
                        else
                            Log.d("USB", "USB accessory empty");
                    }
                    else {
                        Log.d("USB", "permission denied for accessory " + mUsbAccessory);
                    }
                }
            }
        }
    };

    private void openAccessory() {
        Log.d("USB", "openAccessory: " + mUsbAccessory);
        mFileDescriptor = mUsbManager.openAccessory(mUsbAccessory);
        if (mFileDescriptor != null) {
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("test", "start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_crypto);
        final GoEnc enc = new GoEnc();

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        IntentFilter i = new IntentFilter();
        i.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        i.addAction("com.example.niels.aesapp.USB_PERMISSION");
        registerReceiver(mUsbReceiver,i);

        if(getIntent().getAction().equals("android.hardware.usb.action.USB_ACCESSORY_ATTACHED")) {
            Log.d("USB", "USB Connected");
            mUsbAccessory = getIntent().getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
            openAccessory();

//            ParcelFileDescriptor mFileDescriptor = manager.openAccessory(accessory);
//            if (mFileDescriptor != null) {
//                Log.d("USB", "Accessory connected");
//            }
//            Toast.makeText(getApplicationContext(), "msg msg", Toast.LENGTH_SHORT).show();

        }


        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String filename = "output/messages.txt";
                if (isChecked) {

                    EditText keyText = (EditText) findViewById(R.id.encKey);
                    keyStr = keyText.getText().toString();
                    byte [] key = hexStringToByteArray(keyStr);
                    EditText runsText = (EditText) findViewById(R.id.numRuns);
                    numRuns = Integer.parseInt(runsText.getText().toString());
                    CheckBox checkBox = (CheckBox) findViewById(R.id.appendM);
                    EditText sleepText = (EditText) findViewById(R.id.sleepTime);
                    sleepTime = (int)(Double.parseDouble(sleepText.getText().toString()) * 1000);

                    try {
                        if (checkBox.isChecked()) {
                            out = new outputData(filename, true);
                        } else {
                            out = new outputData(filename, false);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    MyTaskParams params = new MyTaskParams(key, numRuns, sleepTime, out);
//                    usb();
                    System.out.println("hoi");
//                    enc.start(params);
                } else {
//                    enc.stop();
                    out.close();
                }
                new Thread(new Runnable() {
                    public void run() {
                        byte[] output = new byte[] {0x01, 0x02, 0x03, 0x04, 0x01, 0x02, 0x03, 0x04, 0x05};
                        String out = "Hoi";
                        byte[] input = new byte[3];
                        try {
                            mOutputStream.write(output, 0, 5);
//                            mInputStream.read(input, 0, 3);
                            Log.d("USB", "Bytes send");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });
    }

    @Override
    protected void onStop()
    {
        unregisterReceiver(mUsbReceiver);
        super.onStop();
    }


}

