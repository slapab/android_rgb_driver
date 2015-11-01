package scott.mymaterialdesign;


//import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

//import info.androidhive.materialtabs.R;


public class OneFragment extends Fragment implements SelectDevicesDialog.SelectDeviceListener
{

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice ;             // device on which need to connect

    private SwitchCompat powerBluetoothSw ;

    private ListView pairedList ;

    private BroadcastReceiver mReceiver ;

    private SelectDevicesDialog listDevicesDialog ;     // Object of Dialog to show bluetooth devices list

    // buttons
    private Button pairedButton ;
    private Button searchButton ;



    private final List<BluetoothDevice> mFoundBtDevs = new ArrayList<BluetoothDevice>() ;



    private final String[] values = new String[] { "Device 1", "Device 2" } ;






    static final int REQUEST_ENABLE_BT = 1;
    static final String DIALOG_DEVICES_LIST = "dialog_list_devices";



    public OneFragment() {

        powerBluetoothSw = null ;
        pairedList = null ;
    }

    @Override
    // TODO put definition of BroadCastReceiver into new method
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;

        // Get bluetooth object
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if ( mBluetoothAdapter == null ) // error, no local bluetooth device
        {}

        // register ACTION_FOUND for getting bluetooth discovered devices
        // Create a BroadcastReceiver for ACTION_FOUND
        mReceiver  = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    mFoundBtDevs.add(device) ;
                    listDevicesDialog.addDataToList(device); // update dialog list

                    Log.v("BT_DEV_FOUND", device.getName() + " / " + device.getAddress()) ;
                }
                else if ( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                {
                    // TODO add here call to update title -> this is not possible I presume
                    // TODO handle this state somehow - log event because now it is not logged automically

                }
            }
        };

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.getContext().registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy


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
    // TODO Pack into method all about bluetooth
    public void onDestroy()
    {
        super.onDestroy();

        // Clean up after bluetooth operations
        this.getContext().unregisterReceiver(mReceiver);
        if ( mBluetoothAdapter.isDiscovering() ) {
            mBluetoothAdapter.cancelDiscovery();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        // If this is result from enabling bluetooth module
        try {
            if (    (REQUEST_ENABLE_BT == requestCode)
                    && (AppCompatActivity.RESULT_OK == resultCode) )
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
        } else {
            // disable bluetooth module
            if (!mBluetoothAdapter.disable()) {
            } // if disabling module fails
        }
    }


    private void init_powerBluetoothSw() {

        // init the switch value onStart() proper to state of bluetooth adapter
        try {
            powerBluetoothSw.setChecked(mBluetoothAdapter.isEnabled());
        } catch (NullPointerException e ) {}



        powerBluetoothSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                powerBluetooth(isChecked);
            }
        });
    }

    private void init_buttons()
    {
        // list paired button
        pairedButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                listPairedDevices(v);
            }
        });

        // searching button
        this.searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startSearchingDevices(v);
            }
        });

    }




    // ACTIONS ON BUTTONS
    // TODO check if list-dialog can be used from class field like in the startSearchingDevices.
    private void listPairedDevices( View v )
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(DIALOG_DEVICES_LIST);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);


        // get the paired devices into list
        List<BluetoothDevice> setPairedDevs = new ArrayList<BluetoothDevice>(
                                        mBluetoothAdapter.getBondedDevices() );


        // create list dialog
        SelectDevicesDialog list = SelectDevicesDialog.getInstance(
                setPairedDevs,
                SelectDevicesDialog.LIST_TYPE.PAIRED
        ) ;

        list.show(ft, DIALOG_DEVICES_LIST) ;
    }

    private void startSearchingDevices(View v)
    {
        if (!mBluetoothAdapter.isEnabled()) return ;


        mBluetoothAdapter.startDiscovery();

        // Create view with list of found devices
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(DIALOG_DEVICES_LIST);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        mFoundBtDevs.clear(); // clear before scanning

        // create list dialog
        listDevicesDialog = SelectDevicesDialog.getInstance(
                mFoundBtDevs,
                SelectDevicesDialog.LIST_TYPE.SEARCHED
        ) ;

        listDevicesDialog.show(ft, DIALOG_DEVICES_LIST) ;


    }


    // implements the onSelectItem from SelectDeviceDialog class
    @Override
    public void onSelectedItem(BluetoothDevice dev )
    {
        mBluetoothDevice = dev ;
        Log.v("SelBtDev", dev.getName() + dev.getAddress()) ;
    }
}