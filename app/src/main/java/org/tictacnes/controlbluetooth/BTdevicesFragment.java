package org.tictacnes.controlbluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class BTdevicesFragment extends Fragment {

    // MIRAR Para el listener http://www.sgoliver.net/blog/controles-de-seleccion-v-recyclerview/

    // FUENTE BT https://developer.android.com/guide/topics/connectivity/bluetooth.html?hl=es-419
    // Voy por enviar comandos mediante write

    public AcceptThread acceptThread;

    private static final String ARG_COLUMN_COUNT = "column-count";
    private OnListFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    public static final java.util.UUID MY_UUID
            = java.util.UUID.fromString("DEADBEEF-0000-0000-0000-000000000000"); // The String is in a format according to RFC 4122. Para Arduino

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private final Handler mHandler;
    // private AcceptThread mSecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private int mNewState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    private int mColumnCount = 1;
    private static final int REQUEST_ENABLE_BT = 1;
    private static BluetoothAdapter mBluetoothAdapter;
    private static BTdeviceRecyclerViewAdapter btDeviceRecyclerViewAdapter;
    int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                System.out.println("Enc: "+device);
                // Add the name and address to an array adapter to show in a ListView
                btDeviceRecyclerViewAdapter.add(device);
                btDeviceRecyclerViewAdapter.notifyDataSetChanged();
                // mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    };
    // Register the BroadcastReceiver
    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

    // 1º Tengo que crear un fragment con un recyclerview para los dispositivos BT encontrados
    // TODA LA INFO AQUÍ https://developer.android.com/guide/topics/connectivity/bluetooth.html?hl=es-419
    // VOY POR TESTEARLO Y ENVIAR DATOS (write)

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    public BTdevicesFragment() {
        mHandler = new Handler();
     }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static BTdevicesFragment newInstance(int columnCount) {
        BTdevicesFragment fragment = new BTdevicesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        habilitarBT();
        consultaSincronizados();

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            this.recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            recyclerView.setAdapter(getBTdeviceRecyclerViewAdapter());
            System.out.println(getBTdeviceRecyclerViewAdapter());
            ((BTdeviceRecyclerViewAdapter) (recyclerView.getAdapter())).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView tvName = (TextView) view.findViewById(R.id.tvNameBT);
                    TextView tvAddress = (TextView) view.findViewById(R.id.tvAddressBT);

                    BluetoothDevice btPulsado = btDeviceRecyclerViewAdapter.getBluetootDeviceList(tvAddress.getText().toString());
                    if(btPulsado!=null){
                        // CONECTARSE A ESTE DISPOSITIVO
                        try {
                            // Iniciamos conexión BT como clientes con el dispositivo seleccionado
                            BluetoothSocket btSocketCliente=btPulsado.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                            System.out.println("socketCliente: "+btSocketCliente);
                            ConnectedThread connectedThread = new ConnectedThread(btSocketCliente);
                            connectedThread.start();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    // ConnectedThread connectedThread = new ConnectedThread();
                }
            });
        }

        mBluetoothAdapter.startDiscovery();
        // una vez encontrado un dispositivo usar CANCELDISCOVERY() OBLIGADO)
        buscaDispositivosBT();

        this.acceptThread = new AcceptThread();
        this.acceptThread.start(); // Automáticamente llamará a run()

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            Toast.makeText(getActivity().getBaseContext(), "Se ha habilitado el BT", Toast.LENGTH_LONG);
            System.out.println("Se ha habilitado el BT");
        }else {
            Toast.makeText(getActivity().getBaseContext(), "NO se ha habilitado el BT", Toast.LENGTH_LONG);
            System.out.println("ERROR NO se ha habilitado el BT");
        }
    }


    public void habilitarBT(){
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Comprobamos si el BT es compatible
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(getActivity().getBaseContext(), "ERROR: BT no compatible.", Toast.LENGTH_LONG);
        }else{
            // Comprobamos si el BT está habilitado
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    public BTdeviceRecyclerViewAdapter consultaSincronizados(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        btDeviceRecyclerViewAdapter = new BTdeviceRecyclerViewAdapter(new ArrayList<BluetoothDevice>(),null);
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices

            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                btDeviceRecyclerViewAdapter.add(device);
            }
        }
        return btDeviceRecyclerViewAdapter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public void onPause() {
        if(mBluetoothAdapter!=null)
            mBluetoothAdapter.cancelDiscovery();
        super.onPause();
    }

    public void buscaDispositivosBT(){
        /*
        //Hacemos que nuestro dispositivo sea visible (no hace falta)
        Intent discoverableIntent = new
                   Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
        */
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(this.mReceiver, filter); // Don't forget to unregister during onDestroy
    }

    public static BTdeviceRecyclerViewAdapter getBTdeviceRecyclerViewAdapter() {
        return btDeviceRecyclerViewAdapter;
    }

    public static BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            //throw new RuntimeException(context.toString()
            //        + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(BluetoothDevice item);
    }

    private class ConnectThread extends Thread {
        // Clase para hacer de Servidor Bluetooth
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            // manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread {
        // Clase para hacer de Cliente Bluetooth
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            try {
                mmSocket.connect(); // MUY IMPORTANTE, SINO NO FUNCIONA
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    System.out.println("run: "+mmInStream);
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
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
