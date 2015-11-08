package scott.mymaterialdesign;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import java.util.ArrayList;
import java.util.List;


import scott.mymaterialdesign.interfaces.MainActivityConfigConnectionCallback;
import scott.mymaterialdesign.tasks.ConnectTask;



public class OneFragment extends Fragment implements SelectDevicesDialog.SelectDeviceListener
{

    //private BluetoothDevice mBluetoothDevice;             // device on which need to connect

    private SwitchCompat powerBluetoothSw;

//    private ListView pairedList ;

    private BroadcastReceiver mReceiver;

    private SelectDevicesDialog listDevicesDialog;     // Object of Dialog to show bluetooth devices list


    private MainActivityConfigConnectionCallback mMainActivityNotifier ;


    // buttons
    private Button pairedButton;
    private Button searchButton;
    private Button disconnectButton ;


    private final List<BluetoothDevice> mFoundBtDevs = new ArrayList<BluetoothDevice>();


    private final int REQUEST_ENABLE_BT = 1;
    private final String DIALOG_DEVICES_LIST = "dialog_list_devices";

    private final String TAG = OneFragment.class.getSimpleName() ;




    public OneFragment() {
        powerBluetoothSw = null ;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
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
        disconnectButton = (Button) frag_view.findViewById(R.id.disconnect_button) ;

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mMainActivityNotifier = (MainActivityConfigConnectionCallback) context;


        // register ACTION_FOUND for getting bluetooth discovered devices
        // Create a BroadcastReceiver for ACTION_FOUND
        mReceiver  = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // TODO Cleanup here
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    mFoundBtDevs.add(device) ;
                    listDevicesDialog.addDataToList(device); // update dialog list

                    Log.v("BT_DEV_FOUND", device.getName() + " / " + device.getAddress()) ;
                }
//                else if ( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
//                {
//                    //  add here call to update title -> this is not possible I presume
//                    //  handle this state somehow - log event because now it is not logged automically
//                    // tutaj sie w ogole nie wykonuje kurde molek
//                    Log.v("FIN", "Discoverying bt dev is over") ;
//                    View coordinatorLayoutView = getActivity().findViewById(R.id.main_coordinator_layout_id);
//                    Snackbar.make( coordinatorLayoutView, "Searching is over", Snackbar.LENGTH_SHORT  )
//                            .show() ;
//                }
            }
        };

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

    }


    @Override
    public void onDetach() {
        super.onDetach();

        mMainActivityNotifier = null;

        // Clean up after bluetooth operations
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        this.getContext().unregisterReceiver(mReceiver);
        if ( btAdapter.isDiscovering() ) {
            btAdapter.cancelDiscovery();
        }
    }


    /*
    * Private methods
    */

    private void powerBluetooth( boolean isChecked )
    {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if ( isChecked )
        {

            // Try to enable bluetooth module
            if (!btAdapter.isEnabled()) // check bluetooth is disabled
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else {
            // disable bluetooth module
            if (!btAdapter.disable()) {
            } // if disabling module fails
        }
    }


    private void init_powerBluetoothSw() {

        // init the switch value onStart() proper to state of bluetooth adapter
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            powerBluetoothSw.setChecked(btAdapter.isEnabled());
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
            @Override
            public void onClick(View v) {
                listPairedDevices(v);
            }
        });

        // searching button
        this.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearchingDevices(v);
            }
        });

        // disconnect button
        this.disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainActivityNotifier.onDisconnecting();
            }
        }) ;

    }




    // ACTIONS ON BUTTONS
    private void listPairedDevices( View v )
    {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        // get the paired devices into list
        List<BluetoothDevice> setPairedDevs = new ArrayList<BluetoothDevice>(
                                                btAdapter.getBondedDevices() );

        // Create dialog list
        FragmentManager fm = getActivity().getSupportFragmentManager();
        SelectDevicesDialog list = SelectDevicesDialog.getInstance(
                setPairedDevs,
                SelectDevicesDialog.LIST_TYPE.PAIRED
        ) ;

        // set target fragment and then show dialog
        list.setTargetFragment(this, 0);
        list.show(fm, DIALOG_DEVICES_LIST);
    }

    private void startSearchingDevices(View v)
    {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!btAdapter.isEnabled()) return ;

        btAdapter.startDiscovery();

        mFoundBtDevs.clear(); // clear before scanning

        // Create new dialog list
        FragmentManager fm = getActivity().getSupportFragmentManager();
        listDevicesDialog = SelectDevicesDialog.getInstance(
                mFoundBtDevs,
                SelectDevicesDialog.LIST_TYPE.SEARCHED
        ) ;

        // set target fragment and show dialog list
        listDevicesDialog.setTargetFragment(this, 0);
        listDevicesDialog.show(fm, DIALOG_DEVICES_LIST);
    }


    // implements the onSelectItem from SelectDeviceDialog class
    @Override
    public void onSelectedItem(BluetoothDevice dev )
    {
        Log.v(TAG, "Trying to connect to [ " + Thread.currentThread() + " ] " + dev.getName() + dev.getAddress()) ;
//        mBluetoothDevice = dev ;
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery() ;

        // Run AsyncTask : Connect task
        Activity tmp = getActivity() ;
        new ConnectTask( (Activity)this.mMainActivityNotifier ).execute(dev);

        // end of async task

//        ManageConnectionThread connectThread = new ManageConnectionThread(mBluetoothDevice, m_oneFragHandler ) ;
//        connectThread.start() ;
//
//        while( !connectThread.isAlive() ) ; // hang until thread starts
//
//        Handler taskHandler = connectThread.getHandler() ;
//
//        Message myMsg = taskHandler.obtainMessage() ;
//            Bundle data = new Bundle() ;
//            data.putString("KLUCZ1", "tak");
//        myMsg.setData(data);
//
//        taskHandler.sendMessageAtFrontOfQueue(myMsg) ;



//        View coordinatorLayoutView = getActivity().findViewById(R.id.main_coordinator_layout_id);
//        Snackbar.make( coordinatorLayoutView, "BT dev selected, and thread started!", Snackbar.LENGTH_SHORT  )
//                .show() ;
    }
}



class BluetoothHandler extends Handler
{
    private final String TAG = BluetoothHandler.class.getSimpleName() ;


    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Log.v(TAG, msg.toString()) ;
    }
}