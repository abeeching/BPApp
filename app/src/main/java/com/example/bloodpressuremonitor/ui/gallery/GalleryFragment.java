package com.example.bloodpressuremonitor.ui.gallery;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.bloodpressuremonitor.DBHandler;
import com.example.bloodpressuremonitor.R;
import com.example.bloodpressuremonitor.databinding.FragmentGalleryBinding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    public BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public String deviceName = "DSD TECH HC-05";
    public String deviceAddr;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public DBHandler dbHandler;

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
                    tv.setText(R.string.unsupported_bt_error);
                }

                // Check to see if the adapter is enabled
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBtIntent);
                }


                // listing off paired devices - not sure what to do with this
                if (ActivityCompat.checkSelfPermission(root.getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PERMISSION_GRANTED) {
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
                        // tv.append(device.getName() + "\n");
                        if (deviceName.equals(device.getName())) {
                            deviceAddr = device.getAddress();
                        }
                    }
                }

                BluetoothDevice hc05 = bluetoothAdapter.getRemoteDevice(deviceAddr);

                ConnectThread btConnectThread = new ConnectThread(hc05);
                btConnectThread.start();
                tv.append("Getting data...\nPlease wait for a notification below\nbefore viewing your data.");
                // btConnectThread.cancel();
            }


        });
        return root;
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket btSocket;
        private final BluetoothDevice btDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket temp = null;
            btDevice = device;
            try {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    // return;
                }
                temp = btDevice.createInsecureRfcommSocketToServiceRecord(myUUID);
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(GalleryFragment.this.binding.getRoot().getContext(), "ERROR: Could not create a socket.", Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
                e.printStackTrace();
            }
            btSocket = temp;
        }

        public void run() {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                // return;
            }
            bluetoothAdapter.cancelDiscovery();
            try {
                btSocket.connect();
            } catch (IOException connectException) {
                try {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(GalleryFragment.this.binding.getRoot().getContext(), "ERROR: Could not connect to device.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    });
                    connectException.printStackTrace();
                    btSocket.close();
                } catch (IOException closeException) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(GalleryFragment.this.getContext(), "ERROR: Could not close Bluetooth connection.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    });
                    closeException.printStackTrace();
                }
                return;
            }
            ConnectedThread btConnected = new ConnectedThread(btSocket);
            btConnected.start();
            btConnected.write("1".getBytes());

        }

        public void cancel() {
            try {
                btSocket.close();
            } catch (IOException e) {
                System.out.print("Exception in ConnectThread cancel()\n");
                e.printStackTrace();
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket btSocket;
        private final InputStream inStream;
        private final OutputStream outStream;

        public ConnectedThread(BluetoothSocket socket) {
            btSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = socket.getInputStream();
                tempOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(GalleryFragment.this.binding.getRoot().getContext(), "ERROR: Could not initiate connection.", Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
            }

            inStream = tempIn;
            outStream = tempOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int begin = 0;
            int bytes = 0;

            while (true) {
                try {
                    bytes += inStream.read(buffer, bytes, buffer.length - bytes);
                    for (int i = begin; i < bytes; i++) {
                        if (buffer[i] == "#".getBytes()[0]) {
                            btHandler.obtainMessage(1, begin, i, buffer).sendToTarget();
                            begin = i + 1;
                            if (i == bytes - 1) {
                                bytes = 0;
                                begin = 0;
                            }
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast toast = Toast.makeText(GalleryFragment.this.binding.getRoot().getContext(), "Received data. Closing connection.", Toast.LENGTH_LONG);
                                    toast.show();
                                }
                            });
                            this.cancel();
                        }
                    }
                } catch (IOException e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(GalleryFragment.this.binding.getRoot().getContext(), "ERROR: Could not receive data.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    });
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outStream.write(bytes);
            } catch (IOException e) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(GalleryFragment.this.binding.getRoot().getContext(), "ERROR: Could not write to Bluetooth.", Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                outStream.flush();
                outStream.close();
                inStream.close();
                btSocket.close();
            } catch (IOException e) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(GalleryFragment.this.binding.getRoot().getContext(), "ERROR: Could not close connection.", Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
                e.printStackTrace();
            }
        }
    }

    Handler btHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            byte[] writeBuf = (byte[]) msg.obj;
            int begin = (int)msg.arg1;
            int end = (int)msg.arg2;

            try {
                switch (msg.what) {
                    case 1:
                        Calendar calendar = Calendar.getInstance();
                        DateFormat df = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
                        String strDate = df.format(calendar.getTime());
                        String writeMessage = new String(writeBuf);
                        writeMessage = writeMessage.substring(begin, end);
                        System.out.println(writeMessage);
                        String values[] = writeMessage.split(",");
                        dbHandler = new DBHandler(binding.getRoot().getContext());
                        dbHandler.addBPData(strDate, values[0], values[1]);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

}