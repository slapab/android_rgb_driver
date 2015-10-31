package scott.mymaterialdesign;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.database.DataSetObserver;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import java.util.Collections;
import java.util.Set;

/**
 * Created by scott on 28.10.15.
 */
public class SelectDevicesDialog extends DialogFragment {

    private Set<BluetoothDevice> devices ;
    private LIST_TYPE listType ;


    public static enum LIST_TYPE { PAIRED, SEARCHED, DEFAULT_CONSTR } ;


    public SelectDevicesDialog() {}

    public static SelectDevicesDialog getInstance( final Set<BluetoothDevice> pairedDevices,
                                                   final LIST_TYPE tlist )
    {
        SelectDevicesDialog instance = new SelectDevicesDialog() ;

        // pass data to object :
        instance.setBtDevices( pairedDevices );
        instance.setListType( tlist ) ;

        return instance ;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState )
    {
        // Use the Builder class for convenient dialog construction
        FragmentActivity fActivity = getActivity() ;
        AlertDialog.Builder builder = new AlertDialog.Builder(fActivity);

        switch( listType )
        {
            case PAIRED:
                pairedBuiler(builder, fActivity);
                break ;
            case SEARCHED:
                break ;
        }

        // Create the AlertDialog object and return it
        return builder.create();
    }

    // methods for passing arguments to object
    public void setBtDevices(final Set<BluetoothDevice> devs )
    {
        this.devices = devs ;
    }
    public void setListType( final LIST_TYPE tlist )
    {
        this.listType = tlist ;
    }


    private void pairedBuiler( AlertDialog.Builder builder, FragmentActivity activity)
    {
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String> (
                            activity,
                            android.R.layout.select_dialog_singlechoice) ;


        // Show message when there is 0 paired devices
        if (this.devices.size() == 0)
        {
            builder.setMessage("Currently there is no paired devices.") ;
            builder.setNeutralButton("OK", null) ;
            return ;
        }

        // Show list of paired devices
        for ( BluetoothDevice item : this.devices )
        {
            listAdapter.add( item.getName() + "\n" + item.getAddress() ) ;
        }

        builder.setTitle("Select one of paired devices")
                .setAdapter(listAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                    }
                });

    }

}
