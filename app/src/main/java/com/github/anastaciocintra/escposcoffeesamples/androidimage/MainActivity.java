package com.github.anastaciocintra.escposcoffeesamples.androidimage;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    String actionString ="com.android.example.USB_PERMISSION";
    private UsbManager mUsbManager;
    BarcodeSample sample;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Button button;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
sample=new BarcodeSample();
        button = (Button) findViewById(R.id.button_print);
        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                mUsbManager=getSystemService(UsbManager.class);
                IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
                registerReceiver(mUsbReceiver, filter);

                handleIntent(getIntent());

               //
            }
        });
    }

    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                   // printStatus("remove");
                    new Print(getApplicationContext()).start();

                    printDeviceDescription(device);
                }
            }
        }
    };

    private void handleIntent(Intent intent) {
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (device != null) {

            new Print(getApplicationContext()).start();
            printDeviceDetails(device);
        } else {
            // List all devices connected to USB host on startup
           // printStatus(getString(R.string.status_list));
            printDeviceList();
        }
    }
    private void printDeviceList() {
        HashMap<String, UsbDevice> connectedDevices = mUsbManager.getDeviceList();

        if (connectedDevices.isEmpty()) {
            printResult("No Devices Currently Connected");
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("Connected Device Count: ");
            builder.append(connectedDevices.size());
            builder.append("\n\n");
            for (UsbDevice device : connectedDevices.values()) {
                //Use the last device detected (if multiple) to open
                builder.append(UsbHelper.readDevice(device));
                builder.append("\n\n");
            }
            printResult(builder.toString());
        }
    }
    private void printDeviceDescription(UsbDevice device) {
        String result = UsbHelper.readDevice(device) + "\n\n";
        printResult(result);
    }

    /**
     * Initiate a control transfer to request the device information
     * from its descriptors.
     *
     * @param device USB device to query.
     */
    private void printDeviceDetails(UsbDevice device) {
        UsbDeviceConnection connection = mUsbManager.openDevice(device);

        String deviceString = "";
        try {
            //Parse the raw device descriptor
            deviceString = DeviceDescriptor.fromDeviceConnection(connection)
                    .toString();
        } catch (IllegalArgumentException e) {
            Log.w("TAG", "Invalid device descriptor", e);
        }

        String configString = "";
        try {
            //Parse the raw configuration descriptor
            configString = ConfigurationDescriptor.fromDeviceConnection(connection)
                    .toString();
        } catch (IllegalArgumentException e) {
            Log.w("TAG", "Invalid config descriptor", e);
        } catch (ParseException e) {
            Log.w("TAG", "Unable to parse config descriptor", e);
        }

        printResult(deviceString + "\n\n" + configString);
        connection.close();
    }
    private void printStatus(String status) {
      //  mStatusView.setText(status);
        Log.i("TAG", status);
    }

    private void printResult(String result) {
      //  mResultView.setText(result);
        Log.i("TAG", result);
    }
}
