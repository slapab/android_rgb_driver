package scott.mymaterialdesign.tasks;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
//import android.os.Handler;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.UUID;

/**
 * Created by scott on 03.11.15.
 */
public class ManageConnectionThread extends Thread //implements Handler.Callback
{
    private HandlerThread mThreadHandler;  //   The message queue for this thread
    private MessageHandler mMessageQueue;  //   Manages the received messages

    private InputStream mInputStream ;
    private OutputStream mOutputStream ;


    private final static String TAG = ManageConnectionThread.class.getSimpleName();

    public static final String BUNDLE_BYTEARRAY = "BData" ;
    public static final String BUNDLE_TIME  = "BTime" ;
    public static final int MESSAGE_DATA = 1990 ;


    class MessageHandler extends Handler {

        public MessageHandler(Looper looper) {
            super(looper);
        }


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.v(TAG, "Received message in the thread( " + Thread.currentThread() + "): " + msg.toString());


            if ( msg.what == MESSAGE_DATA )
                sendData ( msg.getData() ) ;

        }
    }



    public ManageConnectionThread(InputStream input, OutputStream output)
    {
        super(TAG);
        mInputStream = input ;
        mOutputStream = output ;
    }

    @Override
    public void run() {

        // TODO do the communicating things
        while(!this.isInterrupted())
        {
            try {
                mInputStream.read();
            }
            catch( IOException ioe ) {
                // inform the target that connection was broken
                // TODO manage IOException while reading
                break ;
            }
        }

        // quit the ThreadHandler -> close the thread's message queue
        mThreadHandler.quit();
    }

    @Override
    public synchronized void start() {

        // Create and run message queue
        mThreadHandler = new HandlerThread(TAG + "_MESSAGES");
        mThreadHandler.start();

        // Register an action to receiving the message
        for (int i = 0; i < 5; ++i) {
            try {
                mMessageQueue = new MessageHandler(mThreadHandler.getLooper());
                break;
            } catch (NullPointerException ne) {
                // try for 2ms to connect
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ie) {}

            }
        }
        // close HandlerThread if cannot get looper
        if (mMessageQueue == null) {
            mThreadHandler.quit();
            mThreadHandler = null;
        }

        super.start();
    }


    public Handler getHandler() {   return mMessageQueue;   }


    // TODO call this method after received message with data. Need to review definiton once again
    private void sendData( Bundle data ) {
        byte[] outputData = new byte[10];

        outputData[0] = 0x53; // The start byte
        outputData[1] = 0x07; // how many bytes are valid data ( not the header bytes )

        // Copy the input data to the output data buffer
        byte[] preparedArray = data.getByteArray(BUNDLE_BYTEARRAY);
        for (int i = 0, j = 2; i < preparedArray.length; ++i, ++j) {
            outputData[j] = preparedArray[i];
        }

        // Convert dimming future from string to integer and then to the bytes
        int dimmingTime = 0;
        try {
            dimmingTime = Integer.parseInt(data.getString(BUNDLE_TIME));
        } catch (NumberFormatException e) {
        }

        outputData[7] = (byte) (dimmingTime >> 8);
        outputData[8] = (byte) dimmingTime;

        // add the stop byte
        outputData[9] = 0x0A;


        try {
            mOutputStream.write(outputData);
        }
        catch ( IOException ioe )
        {
            // TODO manage IOEsception while writing
            Log.e(TAG, "Cannot write to the Outputstream!!") ;
        }
    }

}