package scott.mymaterialdesign;

//import android.app.Activity;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothDevice;
//import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.support.v7.app.AppCompatDialogFragment ;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by scott on 28.10.15.
 */
public class SelectDevicesDialog extends AppCompatDialogFragment {//DialogFragment {

    // interface to communicate with main Activity:
    public interface SelectDeviceListener
    {
        public void onSelectedItem( BluetoothDevice dev ) ;
    }

    private List<BluetoothDevice> devices ;
    private LIST_TYPE listType ;
    private SelectDeviceListener mListener ;

    public enum LIST_TYPE { PAIRED, SEARCHED }


    public SelectDevicesDialog() {}

    public static SelectDevicesDialog getInstance( final List<BluetoothDevice> pairedDevices,
                                                   final LIST_TYPE tlist )
    {
        SelectDevicesDialog instance = new SelectDevicesDialog() ;

        // pass data to object :
        instance.setBtDevices( pairedDevices );
        instance.setListType( tlist ) ;

        return instance ;
    }


    // Override the Fragment.onAttach() method to instantiate the SelectDeviceListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        MainActivity mainActivity = (MainActivity)activity ;

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the SelectDeviceListener so it can send events to the host
            int currentItemNO = mainActivity.getViewPager().getCurrentItem() ;
            Fragment myFrag = mainActivity.getViewPagerAdapter().getItem( currentItemNO ) ;

//            FragmentManager fragManager = mainActivity.getSupportFragmentManager();
//            fragManager.getFragments() ;
            //FragmentTransaction fragTransaction = fragManager.beginTransaction();
            //Bundle bn = new Bundle() ;
            //fragManager.getFragment( bn, "" ) ;


            mListener = (SelectDeviceListener)myFrag;

        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement SelectDeviceListener");
            //Log.v("EXCEPTION", "activity does not implements the SelectDeviceListener") ;
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState )
    {
        // Use the Builder class for convenient dialog construction
        FragmentActivity fActivity = getActivity() ;
        AlertDialog.Builder builder = new AlertDialog.Builder(fActivity);

        // After rotation data are missed ( null ) ( so then create empty dialog and then dismiss )
        try
        {
            switch (listType) {
                case PAIRED:
                    pairedBuiler(builder, fActivity);
                    break;
                case SEARCHED:
                    break;
            }
        } catch ( NullPointerException e ) {}


        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Dismiss dialog after rotation
        if ( ( this.devices == null ) || ( this.listType == null ))
            this.dismiss();

    }


    // methods for passing arguments to object
    public void setBtDevices(final List<BluetoothDevice> devs )
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
                /* mListener must be an fragment object which implements its interface */
                .setAdapter(listAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        mListener.onSelectedItem(devices.get(which));
                    }
                });

    }

}
