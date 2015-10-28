package scott.mymaterialdesign;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by scott on 28.10.15.
 */
public class SelectDevicesDialog extends DialogFragment {

    private CharSequence[] items = new CharSequence[] {"One", "Two", "Three", "Four", "Five", "Six", "Seven",
            "One", "Two", "Three", "Four", "Five", "Six", "Seven"} ;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select one of paried devices")
                .setItems( items,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item
                            }
                        } ) ;
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
