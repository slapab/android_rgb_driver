package scott.mymaterialdesign;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.util.ArrayList;
import java.util.List;
import scott.mymaterialdesign.interfaces.MainActivityConfigConnectionCallback ;
import scott.mymaterialdesign.interfaces.MainActivityControlConnectionCallback ;
import scott.mymaterialdesign.interfaces.TwoFragmentConnectionCallback;


public class MainActivity extends AppCompatActivity implements
                            MainActivityConfigConnectionCallback, /* When connection was opened or closed explicitly */
                            MainActivityControlConnectionCallback /* When connection was lost implicitly */
{

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private BluetoothSocket mConnectionSocket ; /*Bluetooth connection socket */

    /* Is using to notify the Control fragment [TwoFragment]  */
    private TwoFragmentConnectionCallback mControlFragmentNotifier ;

    /// Saves the actual status of bluetooth module at creating the activity
    /// It is used to sets up the bluetooth state when destroying this app
    private boolean mBluetoothStateAtStartUp ;


    private final String TAG = MainActivity.class.getSimpleName() ;
    private final String SAVE_BLUETOOTH_STATE = "bluetooth_state_at_startup" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().hide(); // this will hide ( permanently ) title and navigation icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(false); // this will disable icon ( home )

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        // Set icons
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_settings_bluetooth_white_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_tune_white_24dp);


        // Configure callback handler to the TwoFragment
        ViewPagerAdapter ad = (ViewPagerAdapter)viewPager.getAdapter();
        mControlFragmentNotifier = (TwoFragmentConnectionCallback)ad.getItemByName(getString(R.string.tab_control)) ;



        if ( (savedInstanceState != null) && !savedInstanceState.isEmpty() )
        {
            // Restore the bluetooth status on re creating the activity ( on conf change )
            mBluetoothStateAtStartUp = savedInstanceState.getBoolean(this.SAVE_BLUETOOTH_STATE) ;
        }
        else
        {
            // save current bluetooth configuration when starting for a first time an activity
            mBluetoothStateAtStartUp = BluetoothAdapter.getDefaultAdapter().isEnabled();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();


        // If this is called because of configuration changing
        // then do not take action that will be done on closing when exiting the app
        if ( isChangingConfigurations() ) return ;


        // stop discovering
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        // Put the bluetooth module off if was turned off while starting this app
        if ( BluetoothAdapter.getDefaultAdapter().isEnabled() && !mBluetoothStateAtStartUp )
        {
            BluetoothAdapter.getDefaultAdapter().disable() ;
        }
    }




    /*
    *   Storing here all 'objs' which may need to be recreated in onCreate()
    */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // store the bluetooth module state which was read when activity started up
        outState.putBoolean(this.SAVE_BLUETOOTH_STATE, mBluetoothStateAtStartUp);
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter mViewPageAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Create tabs
        mViewPageAdapter.addFragment(new OneFragment(), getString(R.string.tab_connection));
        mViewPageAdapter.addFragment(new TwoFragment(), getString(R.string.tab_control));

        // Add tabs to adapter
        viewPager.setAdapter(mViewPageAdapter);
        viewPager.getCurrentItem();
    }

    public ViewPager getViewPager()
    {
        return viewPager ;
    }
    public ViewPagerAdapter getViewPagerAdapter() {
        return (ViewPagerAdapter)viewPager.getAdapter();
    }

    private void setConnectedViewGroup() {
        View v = findViewById(R.id.search_paired_butt_view_group) ;
        v.setVisibility(View.INVISIBLE);
        v = findViewById(R.id.disconnect_button_view_group) ;
        v.setVisibility(View.VISIBLE);
    }

    private void setDisconnectedViewGroup() {
        View v = findViewById(R.id.search_paired_butt_view_group) ;
        v.setVisibility(View.VISIBLE);
        v = findViewById(R.id.disconnect_button_view_group) ;
        v.setVisibility(View.INVISIBLE);
    }

    /* Implements the callback function [from OneFragment] */
    @Override
    public void onConnected(BluetoothSocket btSocket) {
        Log.v(TAG, "Received message with the valid socket") ;

        if ( btSocket == null ) return ;

        // If currently connection is established
        if ( mConnectionSocket != null )
        {
            // Tell TwoFragment that need to disconnect with this socket
            // it  should be closed
            mControlFragmentNotifier.onDisconnecting();
            try {Thread.sleep(200) ;} catch(Exception e){}
        }
        mConnectionSocket = btSocket ;


        // Hide the Search and Paired list buttons
        // And show the Disconnect button
        setConnectedViewGroup();
        setIconForConnectedState();

        // Switch to control tab
        switchToTabByName(getString(R.string.tab_control)) ;

        // Notify the Control Tab [TwoFragment] that connection was established
        mControlFragmentNotifier.onConnected( btSocket ) ;
    }



    /* Implements the callback when user want to disconnect from the hardware [from OneFragment] */
    @Override
    public void onDisconnecting() {

        Log.v(TAG, "Explicitly closing the connection") ;

        // change icon of first tab (control tab)
        this.setIconForDisconnectedState();

        // Inform the Control Tab on disconnecting process
        mControlFragmentNotifier.onDisconnecting();

        // Show search and paired list buttons
        // and hide disconnect button
        setDisconnectedViewGroup();
        mConnectionSocket  = null ;
    }



    /* Implements the callback when connection has been lost [from TwoFragment]*/
    @Override
    public void onConnectionLost() {
        // Show search and paired list buttons
        // and hide disconnect button
        setDisconnectedViewGroup();
        setIconForDisconnectedState();
        mConnectionSocket = null ;

        // switch tab connection tab
        switchToTabByName( getString(R.string.tab_connection));
    }


    private void setIconForDisconnectedState()
    {
        ViewPagerAdapter vpAdapter = (ViewPagerAdapter)viewPager.getAdapter() ;
        int index = vpAdapter.getItemIndexByName(getString(R.string.tab_connection)) ;
        tabLayout.getTabAt(index).setIcon(R.drawable.ic_settings_bluetooth_white_24dp);
    }

    private void setIconForConnectedState()
    {
        ViewPagerAdapter vpAdapter = (ViewPagerAdapter)viewPager.getAdapter() ;
        int index = vpAdapter.getItemIndexByName(getString(R.string.tab_connection)) ;
        tabLayout.getTabAt(index).setIcon(R.drawable.ic_bluetooth_connected_white_24dp);
    }


    private void switchToTabByName( final String tabName )
    {
        // get the ViewPagerAdapter
        ViewPagerAdapter vpAdapter = (ViewPagerAdapter)viewPager.getAdapter() ;
        // Get index of Control tab
        int index = vpAdapter.getItemIndexByName(tabName) ;
        // set visible the control tab
        viewPager.setCurrentItem(index) ;
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }


        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        public Fragment getItemByName(final String name)
        {
            int idx = mFragmentTitleList.indexOf( name ) ;
            if ( idx < 0 )
                return null ;
            else
                return mFragmentList.get(idx) ;
        }

        public int getItemIndexByName( final String name )
        {
            return mFragmentTitleList.indexOf( name ) ;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
