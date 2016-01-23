package scott.mymaterialdesign.interfaces;

import android.bluetooth.BluetoothSocket;



/* INTERFACE which ONLY control tab [ TwoFragment ] must implement */
public interface TwoFragmentConnectionCallback
{
    /**
     * Call back to the control fragment when BluetoothSocket was opened
     * @param btSocket  opened bluetooth socket
     */
    void onConnected( BluetoothSocket btSocket ) ;

    /**
     *  Call back to the control fragment when user wants to disconnect
     * @return  true if connection ( socket ) was closed and thread was interrupted,
     *          false otherwise.
     */
    void onDisconnecting() ;
}