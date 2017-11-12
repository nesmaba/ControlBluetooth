package org.tictacnes.controlbluetooth;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class ControlBluetoothFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
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
            System.out.println("Hola");
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
    // VOY POR Búsqueda de dispositivos

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

        View view = inflater.inflate(R.layout.fragment_control_bluetooth, container, false);
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        Button btConectarBT = (Button) view.findViewById(R.id.buttonConectarBT);
        btConectarBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                habilitarBT();
                consultaSincronizados();

                mBluetoothAdapter.startDiscovery();
                // una vez encontrado un dispositivo usar CANCELDISCOVERY() OBLIGADO)
                buscaDispositivosBT();
                getActivity().getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                       new BTdevicesFragment(), "LISTA").commit();

            }
        });
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
        // getActivity().unregisterReceiver(mReceiver);
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
}
