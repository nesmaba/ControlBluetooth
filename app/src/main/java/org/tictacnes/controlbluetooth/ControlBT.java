package org.tictacnes.controlbluetooth;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class ControlBT extends AppCompatActivity {

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
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBackPressed(){

        if(getFragmentManager().findFragmentByTag("LISTA")!=null){
            getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ControlBluetoothFragment(), "PRINCIPAL").commit();
        }else{

            finish();
        }
    }
}
