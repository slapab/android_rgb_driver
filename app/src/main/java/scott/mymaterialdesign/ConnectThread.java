package scott.mymaterialdesign;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by scott on 03.11.15.
 */
public class ConnectThread extends Thread
{
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final Activity mmActivity;

    public final static UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") ;

    private final String TAG = "CONN_TH" ;


    ConnectThread( final BluetoothDevice device, final Activity activity )
    {
        mmDevice = device ;
        mmActivity = activity ;

        BluetoothSocket tmp_sock = null ;
        try {
            tmp_sock = device.createRfcommSocketToServiceRecord(SPP_UUID) ;
        }
        catch( IOException e ) {
            Log.e(TAG, "Cannot get socket: " + e) ;
        }

        mmSocket = tmp_sock ;
    }


    @Override
    public void run() {
        // disable discovering
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery() ;

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
            Log.v(TAG, "Connection to " + mmDevice.getName() + " has been established") ;

        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            Log.d(TAG, "Cannot connect to the device: " + connectException) ;
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.d( TAG, "Cannot close socket: " + closeException ) ;
            }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        //manageConnectedSocket(mmSocket);
        manageConnection() ;

    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }


    private void manageConnection()
    {
        for ( int i = 0 ; i < 20 ; ++i ) {
            Log.v(TAG, "task will be doing here, " + i );
            try {
                Thread.sleep(1000);
            } catch( InterruptedException e ) {}

        }
    }
}
