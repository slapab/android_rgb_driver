package scott.mymaterialdesign;


import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import scott.mymaterialdesign.interfaces.MainActivityControlConnectionCallback;
import scott.mymaterialdesign.interfaces.TwoFragmentConnectionCallback;
import scott.mymaterialdesign.tasks.ManageConnectionThread;


public class TwoFragment extends Fragment implements TwoFragmentConnectionCallback
{

    private BluetoothSocket mSocket ;
    private InputStream mInputStream ;
    private OutputStream mOutputStream ;

    private ManageConnectionThread mManageThread ;
    private Handler mManageQueue ;

    private MainActivityControlConnectionCallback mMainActivityNotifier;


    // UI elements
    AppCompatSeekBar mRedSeekBar ;
    AppCompatSeekBar mGreenSeekBar ;
    AppCompatSeekBar mBlueSeekBar ;
    AppCompatSeekBar mFreqSeekBar ;

    AppCompatCheckBox mStrobeOption ;
    AppCompatCheckBox mPulseOption ;
    EditText mDimmingTime ;


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
        View v_frag = inflater.inflate(R.layout.fragment_two, container, false);

        mRedSeekBar = (AppCompatSeekBar)v_frag.findViewById(R.id.red_slider) ;
        mGreenSeekBar = (AppCompatSeekBar)v_frag.findViewById(R.id.green_slider) ;
        mBlueSeekBar = (AppCompatSeekBar)v_frag.findViewById(R.id.blue_slider) ;
        mFreqSeekBar = (AppCompatSeekBar)v_frag.findViewById(R.id.strobe_slider) ;

        mStrobeOption = (AppCompatCheckBox)v_frag.findViewById(R.id.strobe_opt_checkbox) ;
        mPulseOption = (AppCompatCheckBox)v_frag.findViewById(R.id.pulse_opt_checkbox) ;
        mDimmingTime = (EditText)v_frag.findViewById(R.id.pulse_opt_time) ;


        return v_frag ;
    }


    @Override
    public void onStart() {
        super.onStart();

        initUIActions() ;
    }

    //TODO Maybe disable bluetooth?
    @Override
    public void onDestroy() {

        if (mSocket != null)
        {
            Log.v(TAG, "onDestroy(). Closing the connection.") ;
            try { mSocket.close(); } catch ( IOException e ) { }
            mSocket = null ;
        }

        if ((mManageThread != null) && (mManageThread.isAlive()))
        {
            Log.v(TAG, "onDestroy(). Interrupting the thread.") ;
            interruptThread();
            mManageThread = null ;
        }


        super.onDestroy();
    }


    // TODO on attach notify the main activity about connection ( if socket is open and is using )
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Always have valid pointer to the main Activity
        mMainActivityNotifier = (MainActivityControlConnectionCallback)context ;
    }

    @Override
    public void onConnected(BluetoothSocket btSocket) {
        // TODO impelements onConnected()
        Log.v(TAG, "Trying to create thread to manage a bluetooth connection") ;

        // If the null socket received
        if ( btSocket == null )     return ;

        // TODO what if socket already is open and/or thread is running
        // Assign the socket
        mSocket = btSocket ;

        try
        {
            mInputStream = mSocket.getInputStream();
            mOutputStream = mSocket.getOutputStream();

            // Create the thread to manage the connection
            mManageThread = new ManageConnectionThread(mInputStream, mOutputStream);
            mManageThread.start() ;

            // get the handler to the message queue
            mManageQueue = mManageThread.getHandler();

        }
        catch( IOException ioe )
        {
            Log.e(TAG, "Cannot get input/output streams!") ;
        }
    }

    @Override
    public void onDisconnecting() {
        Log.v(TAG, "onDisconnecting() -> Closing the thread and the socket") ;

        // Close the connection
        closeSocket() ;
        // Stop the thread
        interruptThread() ;
    }


    private void closeSocket()
    {
        try {
            // close the OutputStream and InputStream before
            mSocket.getOutputStream().close();
            mSocket.getInputStream().close();
            mSocket.close();
        } catch( IOException ioe ) {
            Log.e(TAG, "Exception while tried to close the socket: " + ioe) ;
        }
        mSocket = null ;
    }



    private void interruptThread()
    {
        mManageThread.interrupt();
        try {
            mManageThread.join();
        }catch ( InterruptedException ie ) {}
    }

    private void initUIActions()
    {
        // Red seek bar
        mRedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // Send only that change to the thread
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Message msg = mManageQueue.obtainMessage(
                        ManageConnectionThread.CONTROL_ONLY_RED,
                        progress, 0) ;
                mManageQueue.sendMessage(msg);  // send only that change
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            // Send whole "view value" to the thread
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendSnapshotData(false);
            }
        });

        // Green seek bar
        mGreenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // Send only that change to the thread
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Message msg = mManageQueue.obtainMessage(
                        ManageConnectionThread.CONTROL_ONLY_GREEN,
                        progress, 0) ;
                mManageQueue.sendMessage(msg);  // send only that change
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            // Send whole "view value" to the thread
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendSnapshotData(false);
            }
        });

        // Blue seek bar
        mBlueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // Send only that change to the thread
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Message msg = mManageQueue.obtainMessage(
                        ManageConnectionThread.CONTROL_ONLY_BLUE,
                        progress, 0) ;
                mManageQueue.sendMessage(msg);  // send only that change
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            // Send whole "view value" to the thread
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendSnapshotData(false);
            }
        });

        // Frequency seek bar
        mFreqSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // Send only that change to the thread
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Message msg = mManageQueue.obtainMessage(
                        ManageConnectionThread.CONTROL_ONLY_FREQ,
                        progress, 0) ;
                mManageQueue.sendMessage(msg);  // send only that change
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            // Send whole "view value" to the thread
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendSnapshotData(false);
            }
        });

    }



    private void sendSnapshotData(boolean needReply) {

        // additional futures - checkboxes
        byte addFutures = 0 ;
        if ( mPulseOption.isChecked() )
            addFutures |= (byte)(1 << 0) ;   // pulse option
        if ( mStrobeOption.isChecked() )
            addFutures |= (byte)(1 << 1) ;   // strobe option

        // TODO handle this option -> some timers or something
        if ( needReply )
            addFutures |= (byte)(1 << 2) ;   // needed reply from the hardware


        // Frequency
        byte freq = (byte)mFreqSeekBar.getProgress();


        // Create an array of slider values
        byte[] byte_data =
                {
                        (byte)mRedSeekBar.getProgress(),
                        (byte)mGreenSeekBar.getProgress(),
                        (byte)mBlueSeekBar.getProgress(),
                        addFutures,
                        freq
                } ;


        // The thread need to covert this to the two bytes
        String time = mDimmingTime.getText().toString();

        // store data into Bundle object
        Bundle data = new Bundle() ;
        data.putByteArray(ManageConnectionThread.BUNDLE_BYTEARRAY, byte_data);
        data.putString(ManageConnectionThread.BUNDLE_TIME, time) ;


        Message msg = mManageQueue.obtainMessage() ;
        msg.what = ManageConnectionThread.CONTROL_WHOLE_DATA;
        msg.setData(data);
        mManageQueue.sendMessage(msg);  // send to the thread
    }
}