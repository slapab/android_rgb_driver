package scott.mymaterialdesign;


import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
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

    private ManageConnectionThread mManageThread ;
    private Handler mManageQueue ;          // This is the message handler for ManageConnectionThread
    private UIMessageHandler mMainQueue ;   // This is the message handler for mainUI thread ;

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

    // Class that handles the message from the thread ( ManageConnectionThread )
    private class UIMessageHandler extends Handler
    {
        public UIMessageHandler( Looper looper ) { super(looper) ; }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if ( ManageConnectionThread.MESSAGE_CONN_LOST == msg.what) {
                Log.v(TAG, "Received 'connection lost' message. Notifying "
                        + MainActivity.class.getSimpleName()) ;
                // Close the thread and socket
                interruptThread();
                closeSocket();

                // Notify the MainActivity that connection was lost
                mMainActivityNotifier.onConnectionLost();
            }
        }

    }


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

        // Create message queue handler fo this ( UI Thread's ) queue
        mMainQueue = new UIMessageHandler( context.getMainLooper() ) ;

        // get new looper for main UI thread and pass it to the thread only if it exists and is running
        if ( (mManageThread != null) && (mManageThread.isAlive()))
        {
            mManageThread.updateParentQueueHandler( mMainQueue );
        }
    }

    @Override
    public void onConnected(BluetoothSocket btSocket)
    {
        Log.v(TAG, "Received onConnected() signal.") ;

        // If the null socket received
        if ( btSocket == null )     return ;

        // if the socket has already opened and/or thread is running then close.
        if ( (mSocket != null) && (mManageThread != null) )
        {
            Log.d(TAG, "Unusual situation detected."
                    + "Socket and Thread ("+mManageThread.getId()
                    +") already exist. Trying to close them.");
            this.closeSocket();
            this.interruptThread();
        }

        Log.v(TAG, "Trying to open streams and create new thread.") ;
        // Assign the socket
        mSocket = btSocket ;
        try
        {
            InputStream inputStream = mSocket.getInputStream();
            OutputStream outputStream = mSocket.getOutputStream();

            // Create the thread to manage the connection

            mManageThread = new ManageConnectionThread(
                    inputStream,
                    outputStream,
                    mMainQueue
            );
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
    public void onDisconnecting()
    {
        Log.v(TAG, "Received onDisconnecting() signal. Closing the thread and the socket.") ;

        // Stop the thread
        interruptThread() ;

        // Close the connection
        closeSocket() ;
    }


    private void closeSocket()
    {
        if ( mSocket == null ) return ;

        Log.v(TAG, "Closing the IO streams and socket");
        try
        {
            mSocket.getOutputStream().close();
            mSocket.getInputStream().close();

            mSocket.close();
            Log.v(TAG, "Streams and socket have been closed.");
        } catch( Exception e ) {}

        mSocket = null ;
    }

    private void interruptThread()
    {
        if ( mManageQueue != null ) { mManageQueue = null ; }

        // closing the thread
        if ( mManageThread != null )
        {
            Log.v(TAG, "Trying to interrupt the thread") ;

            // send the interrupt signal to the thread
            if ( mManageThread.isAlive())
            {
                mManageThread.interrupt();
                Log.v(TAG, "Interrupt signal has been sent.");
                // wait some time - thread can be interrupt on its own
                try { mManageThread.join(300) ;}
                catch(InterruptedException e) {}
            }
            else
            {
                Log.v(TAG, "The thread is already dead") ;
            }
            mManageThread = null;

        }
    }

    private void initUIActions()
    {
        // Red seek bar
        mRedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // Send only that change to the thread
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sendOneParameter(ManageConnectionThread.CONTROL_ONLY_RED, progress) ;
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
                sendOneParameter(ManageConnectionThread.CONTROL_ONLY_GREEN, progress) ;
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
                sendOneParameter(ManageConnectionThread.CONTROL_ONLY_BLUE, progress) ;
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
                sendOneParameter(ManageConnectionThread.CONTROL_ONLY_FREQ, progress) ;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            // Send whole "view value" to the thread
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendSnapshotData(false);
            }
        });

        // Strobe option checkbox
        mStrobeOption.setOnCheckedChangeListener(new AppCompatCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendSnapshotData(false);
            }
        });


        // Pulse option checkbox
        mPulseOption.setOnCheckedChangeListener(new AppCompatCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendSnapshotData(false);
            }
        });


        // Time for pulse option - action on text change
        mDimmingTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                sendSnapshotData(false);
            }
        });
    }

    private void sendOneParameter( final int what, final int value )
    {
        if ( (mManageQueue == null) || (mManageThread == null) )     return ;

        Message msg = mManageQueue.obtainMessage(
                what,
                value, 0) ;
        mManageQueue.sendMessage(msg);  // send only that change
    }

    private void sendSnapshotData(boolean needReply) {

        if ( (mManageQueue == null) || (mManageThread == null) ) return ;


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