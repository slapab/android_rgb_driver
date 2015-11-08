package scott.mymaterialdesign;


import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

import scott.mymaterialdesign.interfaces.MainActivityControlConnectionCallback;
import scott.mymaterialdesign.interfaces.TwoFragmentConnectionCallback;

//import info.androidhive.materialtabs.R;


public class TwoFragment extends Fragment implements TwoFragmentConnectionCallback
{

    private BluetoothSocket mSocket ;

    private MainActivityControlConnectionCallback mMainActivityNotifier;

    // used to identify fragment in ViewPager in the main activity
    private final String TAG = TwoFragment.class.getSimpleName() ;



    public TwoFragment() {
        mSocket = null ;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_two, container, false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivityNotifier = (MainActivityControlConnectionCallback)context ;

        //TODO need to inform the mainActivity that we had a valid BluetoothSocket?
    }

    @Override
    public void onConnected(BluetoothSocket btSocket) {
        // TODO impelements onConnected()
        Log.v(TAG, "Trying to create thread to manage a bluetooth connection") ;

        // If the null socket received
        if ( btSocket == null )     return ;

        // Assign the socket
        mSocket = btSocket ;
        // Create and start thread to manage connection

    }

    @Override
    public void onDisconnecting() {
        // TODO implements onConnected()

        Log.v(TAG, "Closing the thread and the socket") ;
        // Stop the thread.

        // Close the connection
        try {
            mSocket.close();
        } catch( IOException ioe ) {
            Log.e(TAG, "Exception while tried to close the socket: " + ioe) ;
        }
        mSocket = null;

    }


}