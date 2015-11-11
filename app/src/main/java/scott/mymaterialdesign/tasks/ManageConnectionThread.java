package scott.mymaterialdesign.tasks;

//import android.os.Handler;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
        import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
        import java.io.OutputStream;

/**
 * Created by scott on 03.11.15.
 */
public class ManageConnectionThread extends Thread //implements Handler.Callback
{
    private HandlerThread mThreadHandler;  //   The message queue for this thread
    private MessageHandler mMessageQueue;  //   Manages the received messages

    private InputStream mInputStream ;
    private OutputStream mOutputStream ;

    // Frame data
    private DataSnapshot mFrameData ;

    private final static String TAG = ManageConnectionThread.class.getSimpleName();

    // Bundle keys
    public static final String BUNDLE_BYTEARRAY = "BData" ;
    public static final String BUNDLE_TIME  = "BTime" ;

    // MESSAGE 'what' type
    public static final int CONTROL_WHOLE_DATA = 19900 ;
    public static final int CONTROL_ONLY_RED = 19901 ;
    public static final int CONTROL_ONLY_GREEN = 19902 ;
    public static final int CONTROL_ONLY_BLUE = 19903 ;
    public static final int CONTROL_ONLY_FREQ = 19904 ;


    class MessageHandler extends Handler {

        public MessageHandler(Looper looper) {
            super(looper);
        }


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.v(TAG, "Received message in the thread( " + Thread.currentThread() + "): " + msg.toString());

            switch( msg.what )
            {
                case CONTROL_WHOLE_DATA :
                    send( mFrameData.setSnapshot( msg.getData() ));
                    break ;

                case CONTROL_ONLY_RED :
                    send( mFrameData.setRed((byte) msg.arg1)) ;
                    break ;

                case CONTROL_ONLY_GREEN :
                    send( mFrameData.setGreen((byte) msg.arg1)) ;
                    break ;

                case CONTROL_ONLY_BLUE :
                    send( mFrameData.setBlue((byte) msg.arg1)) ;
                    break ;

                case CONTROL_ONLY_FREQ :
                    send( mFrameData.setFrequency((byte) msg.arg1)) ;
                    break ;

                default: break ;
            }


        }
    }



    public ManageConnectionThread(InputStream input, OutputStream output)
    {
        super(TAG);
        mInputStream = input ;
        mOutputStream = output ;
        mFrameData = new DataSnapshot() ;
    }

    @Override
    public void run() {

        // TODO do the communicating things
        byte[] data = new byte[3] ;

        while(!this.isInterrupted() && ( mThreadHandler != null ) && ( mMessageQueue != null ) )
        {
            try {
                if ( mInputStream.available() >= 3 ) {
                    mInputStream.read(data, 0, 3) ;
                    Log.v(TAG, "Response received? " + new String(data).equals("OK\n"));
                }
            }
            catch( IOException ioe ) {
                // inform the target that connection was broken
                // TODO manage IOException while reading
                break ;
            }
        }

        Log.v(TAG, "Exiting.") ;
        // quit the ThreadHandler -> close the thread's message queue
        this.killMessageThread();
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

    /* it tries to close the message queue thread and message handler */
    private void killMessageThread()
    {
        mThreadHandler.interrupt();
        mThreadHandler = null ;
        mMessageQueue = null ;
    }

    public Handler getHandler() {   return mMessageQueue;   }




    /* This method is called from the MessageHandler MessageHandler(): it sends data over bluetooth */
    protected boolean send( final byte[] data )
    {
        boolean retval = false ;
        try {
            mOutputStream.write(data);
            retval = true ;
        }
        catch ( IOException ioe )
        {
            // TODO manage IOEsception while writing
            Log.e(TAG, "Cannot write to the OutputStream! Closing the message thread.") ;
            // Closing the message thread and delete handler
            killMessageThread();
        }

        return retval ;
    }



}

    /*
     *  This class represent the snapshot view of all data that will be send to the device
     */
class DataSnapshot
{
    public DataSnapshot() {}

    // fields are the data
    private byte[] mFrameData =
            {
                    0x53,   /* the start byte */
                    0x07,   /* the number of all valid data bytes */
                    0x00,   /* Red color */
                    0x00,   /* Green color */
                    0x00,   /* Blue color */
                    0x00,   /* Additional options control byte */
                    0x00,   /* Frequency of Strobe option */
                    0x00,   /* High Byte of time [ms] of Pulse option */
                    0x00,   /* Low Byte of time [ms] of Pulse option */
                    0x0A    /* The End Byte */
            } ;



    public byte[] setSnapshot( final Bundle data )
    {
        // set 5 bytes, red, green, blue and options and frequency
        setBytes( data.getByteArray(ManageConnectionThread.BUNDLE_BYTEARRAY), 2) ;
        // set the pulse time
        setPulseTime(data.getString(ManageConnectionThread.BUNDLE_TIME)) ;


        return mFrameData;
    }

    public byte[] setPulseTime( final String in )
    {
        // Convert dimming future from string to integer and then to the bytes
        int dimmingTime = 0;
        try {
            dimmingTime = Integer.parseInt(in);
        } catch (NumberFormatException e) {}

        mFrameData[7] = (byte) (dimmingTime >> 8);
        mFrameData[8] = (byte) dimmingTime;

        return mFrameData;
    }

    public byte[] setBytes( byte[] in, final int start )
    {
        for (int i = 0, j = start; (i < in.length) && (i < mFrameData.length-1); ++i, ++j) {
            mFrameData[j] = in[i];
        }

        return mFrameData;
    }

    public byte[] getData() { return mFrameData ; }

    public byte[] setRed( byte red )
    {
        mFrameData[2] = red ;
        return mFrameData;
    }

    public byte[] setGreen( byte green )
    {
        mFrameData[3] = green ;
        return mFrameData;
    }

    public byte[] setBlue( byte blue )
    {
        mFrameData[4] = blue ;
        return mFrameData;
    }

    public byte[] setFrequency( byte freq )
    {
        mFrameData[6] = freq ;
        return mFrameData;
    }

}