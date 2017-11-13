package org.tictacnes.controlbluetooth;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

public class ControlBT extends AppCompatActivity {

    // VOY POR CONEXION CLIENTE BT https://developer.android.com/guide/topics/connectivity/bluetooth.html?hl=es-419
    public static final java.util.UUID MY_UUID
            = java.util.UUID.fromString("DEADBEEF-0000-0000-0000-000000000000"); // The String is in a format according to RFC 4122. Para Arduino
    private AcceptThread acceptThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new ControlBluetoothFragment(), "PRINCIPAL").commit();
        /*
          <fragment android:name="org.tictacnes.controlbluetooth.ControlBluetoothFragment"
        android:id="@+id/headlines_fragment"
        android:layout_weight="1"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
         */
        this.acceptThread = new AcceptThread();
        this.acceptThread.start(); // Automáticamente llamará a run()

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBackPressed(){

        if(getFragmentManager().findFragmentByTag("LISTA")!=null){
            getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ControlBluetoothFragment(), "PRINCIPAL").commit();
        }else{
            this.acceptThread.cancel();
            finish();
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = BTdevicesFragment.getmBluetoothAdapter().listenUsingRfcommWithServiceRecord("controlBT", MY_UUID);
            } catch (IOException e) {
                Log.e("serverBT", e.toString());
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    // manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e("serverBT", e.toString());
            }
        }
    }
}
