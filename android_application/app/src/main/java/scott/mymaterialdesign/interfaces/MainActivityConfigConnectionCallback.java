package scott.mymaterialdesign.interfaces;

import android.bluetooth.BluetoothSocket;

/**
 * Created by scott on 07.11.15.
 */
 /* INTERFACE which main activity must implement */
public interface MainActivityConfigConnectionCallback {

    /**
     * When BluetoothSocket was opened
     * @param btSocket  BluetoothSocket
     */
    void onConnected( BluetoothSocket btSocket ) ;

    /**
     * When user took action to disconnect from the hardware
     */
    void onDisconnecting() ;

}
