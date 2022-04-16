package com.example.bloodpressuremonitor.ui.gallery;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.bloodpressuremonitor.R;
import com.example.bloodpressuremonitor.databinding.FragmentGalleryBinding;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    public BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public String deviceName = "DSD TECH HC-05";
    public String deviceAddr;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button buttonClick = (Button) root.findViewById(R.id.button3);
        TextView tv = (TextView) root.findViewById(R.id.textView3);

        buttonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                tv.setText("");

                // Check for Bluetooth support
                if (bluetoothAdapter == null) {
                    tv.setText("Bluetooth Not Supported");
                }

                // Check to see if the adapter is enabled
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBtIntent);
                }


                // listing off paired devices - not sure what to do with this
                if (ActivityCompat.checkSelfPermission(root.getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    // Not sure how to handle permissions here. Gonna try commenting out the return line
                    // so Android Studio doesn't freak out when I try to do stuff.

                    // return;
                }
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                        tv.append(device.getName() + "\n");
                        if (deviceName.equals(device.getName())) {
                            deviceAddr = device.getAddress();
                        }
                    }
                }

                BluetoothDevice hc05 = bluetoothAdapter.getRemoteDevice(deviceAddr);

                BluetoothSocket btSocket = null;

                // Connect to the Bluetooth device.
                // Right now this code turns on an LED in the HC-05 component. It will need to be changed.
                try {
                    btSocket = (BluetoothSocket) hc05.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(hc05, 1);
                    //btSocket = hc05.createInsecureRfcommSocketToServiceRecord(myUUID);
                    btSocket.connect();
                } catch (Exception e) {
                    tv.append("Error at btsocket.connect\n");
                    e.printStackTrace();
                }

                // ... specifically, we need to change this part here.
                // This needs to:
                //      (a) Read data from the Bluetooth device. (this part's probably as easy as opening an input stream and reading bytes)
                //      (b) Ensure it is compliant with the SQLite database structure. (this I'm not so sure on.
                //          Gonna have to brush up on transmitting data in Java, and I will also have to see if data can be generated in
                //          the Arduino code.
                //      (c) Append the data to the database for display. That bit works thankfully.
                try {
                    OutputStream outputStream = btSocket.getOutputStream();
                    outputStream.write("1".toString().getBytes());
                } catch (IOException e) {
                    tv.setText("Error at outputStream\n");
                    e.printStackTrace();
                }

                try {
                    btSocket.close();
                } catch (IOException e) {
                    tv.setText("Error at close\n");
                    e.printStackTrace();
                }

            }



});
return root;
    }}