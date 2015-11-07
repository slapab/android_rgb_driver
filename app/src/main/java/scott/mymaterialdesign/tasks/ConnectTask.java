package scott.mymaterialdesign.tasks;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import java.io.IOException;
import java.util.UUID;


import scott.mymaterialdesign.R;
import scott.mymaterialdesign.interfaces.MainActivityConfigConnectionCallback;

/**
 * Created by scott on 05.11.15.
 */
public class ConnectTask extends AsyncTask< BluetoothDevice, Void, BluetoothSocket > {

    private View connectingProgressBarView;
    private Activity mainActivity;
//    private final BluetoothDevice mDevice ;


    public final static UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") ;
    private final String TAG = ConnectTask.class.getSimpleName() ;


    // CONSTRUCTOR
    public ConnectTask(Activity activity)
    {
        super() ;
        mainActivity = activity ;
//        mDevice = device ;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Show progress ring on the activity
        connectingProgressBarView = mainActivity.findViewById(R.id.connecting_group_view) ;
        connectingProgressBarView.setVisibility(View.VISIBLE);

        // Cancel the discovery procedure on the bluetooth adapter
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery() ;

        Log.v(TAG, "onPreExecute()") ;
    }


    @Override
    protected BluetoothSocket doInBackground(BluetoothDevice... params) {

        BluetoothDevice device = params[0] ;
        Log.v(TAG, "Trying to connect to the device: " + device.getName()
                + " " + device.getAddress()) ;

        BluetoothSocket socket = this.performConnection( device ) ;

        return socket;
    }



    @Override
    protected void onPostExecute( BluetoothSocket socket ) {
        Log.v(TAG, "onPostExecute()") ;
        super.onPostExecute(socket);

        // Hide progress ring
        connectingProgressBarView = mainActivity.findViewById(R.id.connecting_group_view) ;
        connectingProgressBarView.setVisibility(View.INVISIBLE);

        if ( socket == null )  // if connection fails
        {
            Log.v(TAG, "Cannot connect to the device.") ;
            return ;
        }


        // get the MainActivity notifier handle
        MainActivityConfigConnectionCallback notifyHandle =
                (MainActivityConfigConnectionCallback) mainActivity;

        // notify the MainActivity
        notifyHandle.onConnected( socket );
    }



    private BluetoothSocket performConnection( BluetoothDevice device )
    {
        // GETTING THE SOCKET
        BluetoothSocket socket = null;
        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID) ;
        }
        catch( IOException e ) {
            Log.e(TAG, "Cannot get socket: " + e) ;
        }

        // Trying to connect to the device
        try
        {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            socket.connect();
            Log.v(TAG, "Connection to " + device.getName() + " has been established") ;
        }
        catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            Log.d(TAG, "Cannot connect to the device. Reason: " + connectException);
            try {
                socket.close();
                socket = null ;
            } catch (IOException closeException) {
            }
        }

        return socket ;
    }
}
