package scott.mymaterialdesign;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

//import info.androidhive.materialtabs.R;


public class OneFragment extends Fragment{

    private BluetoothAdapter mBluetoothAdapter;

    private SwitchCompat powerBluetoothSw ;

    private ListView pairedList ;


    // buttons
    private Button pairedButton ;
    private Button searchButton ;

    private final String[] values = new String[] { "Device 1", "Device 2" } ;

    static final int REQUEST_ENABLE_BT = 1;
    static final String DIALOG_DEVICES_LIST = "dialog_list_devices";



    public OneFragment() {

        powerBluetoothSw = null ;
        pairedList = null ;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;

        // Get bluetooth object
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if ( mBluetoothAdapter == null ) // error, no local bluetooth device
        {}



    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View frag_view = inflater.inflate(R.layout.fragment_one, container, false) ;



        //pairedList = (ListView)frag_view.findViewById(R.id.paired_list) ;
        powerBluetoothSw = (SwitchCompat) frag_view.findViewById(R.id.enableBluetoothSwitch) ;
        pairedButton = (Button) frag_view.findViewById(R.id.paried_devicess_button) ;
        searchButton = (Button) frag_view.findViewById(R.id.search_devices_button) ;


        return frag_view ;
    }

    @Override
    public void onStart()
    {
        super.onStart();

//
//        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>( getActivity().getApplicationContext(),
//                R.layout.list_layout,
//                R.id.id_list_row_name,
//                values
//        );
//
//
//        pairedList.setAdapter(listAdapter);


        init_powerBluetoothSw() ;
        init_buttons() ;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        // If this is result from enabling bluetooth module
        try {
            if (    (REQUEST_ENABLE_BT == requestCode)
                    && (Activity.RESULT_OK == resultCode) )
            {
                powerBluetoothSw.setChecked(true) ;
            }
            else
            {
                powerBluetoothSw.setChecked(false) ;
            }
        } catch( NullPointerException e ) {}

    }



    /*
    * Private methods
    */

    private void powerBluetooth( boolean isChecked )
    {
        if ( isChecked )
        {
            // Try to enable bluetooth module
            if (!mBluetoothAdapter.isEnabled()) // check bluetooth is disabled
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        else
        {
            // disable bluetooth module
            if (!mBluetoothAdapter.disable()) {} // if disabling module fails
        }
    }


    private void init_powerBluetoothSw() {

        // init the switch value onStart() proper to state of bluetooth adapter
        try {
            powerBluetoothSw.setChecked(mBluetoothAdapter.isEnabled());
        }
        catch ( NullPointerException e ) {}



        powerBluetoothSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                powerBluetooth(isChecked);

//                 if ( isChecked )
//                 {
//                     // showing the list in the dialog
//                     FragmentTransaction ft = getFragmentManager().beginTransaction();
//                     SelectDevicesDialog list = new SelectDevicesDialog() ;
//                     list.show(ft, "dialog_list") ;
//
//                 }
            }
        });
    }

    private void init_buttons()
    {
        pairedButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                listPairedDevices(v);
            }
        });
    }




    // ACTIONS ON BUTTONS

    private void listPairedDevices( View v )
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(DIALOG_DEVICES_LIST);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);


        // get the paired devices
        Set<BluetoothDevice> setPairedDevs = mBluetoothAdapter.getBondedDevices();

        // create list dialog
        SelectDevicesDialog list = SelectDevicesDialog.getInstance(
                setPairedDevs,
                SelectDevicesDialog.LIST_TYPE.PAIRED
        ) ;

        list.show(ft, DIALOG_DEVICES_LIST) ;
    }



}