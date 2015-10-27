package scott.mymaterialdesign;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

//import info.androidhive.materialtabs.R;


public class OneFragment extends Fragment{

    private SwitchCompat powerBluetoothSw ;

    private ListView pairedList ;

    private final String[] values = new String[] { "Device 1", "Device 2" } ;

    public OneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;




    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View frag_view = inflater.inflate(R.layout.fragment_one, container, false) ;


        pairedList = (ListView)frag_view.findViewById(R.id.paired_list) ;
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>( getActivity().getApplicationContext(),
                R.layout.list_layout,
                R.id.id_list_row_name,
                values
        );

        pairedList.setAdapter(listAdapter);


        return frag_view ;
    }

}