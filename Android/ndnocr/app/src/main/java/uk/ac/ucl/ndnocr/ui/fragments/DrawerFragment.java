/* -*- Mode:jde; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
/**
 * Copyright (c) 2015 Regents of the University of California
 *
 * This file is part of NFD (Named Data Networking Forwarding Daemon) Android.
 * See AUTHORS.md for complete list of NFD Android authors and contributors.
 *
 * NFD Android is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * NFD Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * NFD Android, e.g., in COPYING.md file.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ucl.ndnocr.ui.fragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import uk.ac.ucl.ndnocr.R;
import uk.ac.ucl.ndnocr.data.NdnOcrService;
import uk.ac.ucl.ndnocr.utils.G;

import java.util.ArrayList;

//import android.support.v7.app.ActionBarActivity;

/**
 * DrawerFragment that provides navigation for MainActivity.
 */
public class DrawerFragment extends Fragment {

  public static DrawerFragment
  newInstance(ArrayList<DrawerItem> items) {
    Bundle drawerParams = new Bundle();
    drawerParams.putParcelableArrayList(DrawerFragment.BUNDLE_PARAMETERS, items);

    DrawerFragment fragment = new DrawerFragment();
    fragment.setArguments(drawerParams);
    return fragment;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      // Register callback
      m_callbacks = (DrawerCallbacks)activity;
    } catch (ClassCastException e) {
      throw new ClassCastException("Host activity must implement DrawerFragment.DrawerCallbacks.");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    m_callbacks = null;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Read in the flag indicating whether or not the user has demonstrated awareness of the
    // drawer. See PREF_DRAWER_SHOWN_TO_USER_FOR_THE_FIRST_TIME for details.
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
    m_hasUserSeenDrawer = sp.getBoolean(PREF_DRAWER_SHOWN_TO_USER_FOR_THE_FIRST_TIME, false);

    if (savedInstanceState != null) {
      m_drawerSelectedPosition = savedInstanceState.getInt(DRAWER_SELECTED_POSITION_BUNDLE_KEY);
      m_restoredFromSavedInstanceState = true;
    }
    m_drawerItems = getArguments().getParcelableArrayList(BUNDLE_PARAMETERS);
    //bindService();
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState)
  {

    View drawer = inflater.inflate(
            R.layout.activity_main_drawer_listview, container, false);
    m_drawerListView = drawer.findViewById(R.id.drawer_listview);

   // m_drawerListView = (ListView)inflater.inflate(
    //  R.layout.activity_main_drawer_listview, container, false);

    m_drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Update UI
        updateSelection(position);
      }
    });
    m_drawerListView.setAdapter(new DrawerListAdapter(getContext(),m_drawerItems));
    m_drawerListView.setItemChecked(m_drawerSelectedPosition, true);

    m_serviceStartStopSwitch = drawer.findViewById(R.id.serviceSwitch);

    m_serviceStartStopSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton compoundButton, boolean isOn)
          {

              if (isOn) {
                 // isSource.setEnabled(false);
                 startKbappService();
              }
              else {
                //  isSource.setEnabled(true);
                stopKbappService();
              }
          }
      });

    return drawer;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    // Fragment influences action bar
    setHasOptionsMenu(true);

    // Initialize and set up the navigation drawer UI
    initializeDrawerFragment(getActivity().findViewById(R.id.navigation_drawer),
                             (DrawerLayout)getActivity().findViewById(R.id.drawer_layout));

    if (savedInstanceState == null) {
      // when restoring (e.g., after rotation), rely on system to restore previous state of
      // fragments
      updateSelection(m_drawerSelectedPosition);
    }
  }

  /**
   * Initialize drawer fragment after being attached to the host activity.
   *
   * @param drawerFragmentViewContainer View container that holds the navigation drawer
   * @param drawerLayout DrawerLayout of the drawer in the host Activity
   */
  private void initializeDrawerFragment(View drawerFragmentViewContainer,
                                        DrawerLayout drawerLayout)
  {
    m_drawerFragmentViewContainer = drawerFragmentViewContainer;
    m_drawerLayout = drawerLayout;

  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(DRAWER_SELECTED_POSITION_BUNDLE_KEY, m_drawerSelectedPosition);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // Forward the new configuration the drawer toggle component.
    //m_drawerToggle.onConfigurationChanged(newConfig);
  }


  @Override
  public void onResume()
  {
    bindService();
    super.onResume();
  }

  @Override
  public void onPause()
  {
    unbindService();
    super.onPause();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    // Update menu UI when the drawer is open. This gives the user a better
    // contextual perception of the application.
    if (isDrawerOpen()) {
      // Inflate drawer specific menu here (if any)
      showGlobalContextActionBar();
    }

  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    // Remove option menu items when drawer is sliding out
    if (m_shouldHideOptionsMenu) {
      for (int i = 0; i < menu.size(); i++) {
        menu.getItem(i).setVisible(false);
      }
    }

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle drawer selection events
   /* if (m_drawerToggle.onOptionsItemSelected(item)) {
      return true;
    }*/

    // Handle other menu items
    switch (item.getItemId()) {
    // Handle activity menu item here (if any)
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  public boolean
  shouldHideOptionsMenu() {
    return m_shouldHideOptionsMenu;
  }

  /**
   * Convenience method that updates the UI and callbacks the host activity.
   *
   * @param position Position of the selected item within the Drawer's ListView.
   */
  private void updateSelection(int position) {
    // Update Position
    m_drawerSelectedPosition = position;

    // Update UI of selected position
    if (m_drawerListView != null) {
      m_drawerListView.setItemChecked(position, true);
    }

    // Close drawer
    if (m_drawerLayout != null) {
      m_drawerLayout.closeDrawer(m_drawerFragmentViewContainer);
    }

    // Invoke host activity callback
    if (m_callbacks != null) {
      DrawerItem item = m_drawerItems.get(position);
      m_callbacks.onDrawerItemSelected(item.getItemCode(), item.getItemName());
    }
  }

  /**
   * Safe convenience method to determine if drawer is open.
   *
   * @return True if drawer is present and in an open state; false otherwise
   */
  private boolean
  isDrawerOpen() {
    return m_drawerLayout != null && m_drawerLayout.isDrawerOpen(m_drawerFragmentViewContainer);
  }

  /**
   * Convenience method to update host activity action bar so that the user is informed of
   * the app's "current context" of the fragment.
   */
  private void showGlobalContextActionBar() {
    ActionBar actionBar = getActionBar();
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setTitle(R.string.app_name);
  }

  /**
   * Convenience method to get host activity's ActionBar. This makes for easy updates
   * in a single location when upgrading to use >= HONEYCOMB (API 11) ActionBar.
   *
   * @return Host activity's ActionBar.
   */
  private ActionBar getActionBar() {
    return ((AppCompatActivity)getActivity()).getSupportActionBar();
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
   * DrawerItem represents a single selection option in the Drawer, complete
   * with the ability to set a Drawable resource icon for display along
   * with the drawer item name.
   */
  public static class DrawerItem implements Parcelable {
    @Override
    public int describeContents()
    {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i)
    {
      parcel.writeInt(m_itemNameId);
      parcel.writeInt(m_iconResId);
      parcel.writeInt(m_itemCode);
    }

    public static final Creator<DrawerItem> CREATOR = new Creator<DrawerItem>() {
      public DrawerItem
      createFromParcel(Parcel in) {
        return new DrawerItem(in.readInt(), in.readInt(), in.readInt());
      }

      public DrawerItem[] newArray(int size) {
        return new DrawerItem[size];
      }
    };

    public
    DrawerItem(int itemNameId, int resIconId, int itemCode) {
      m_itemNameId = itemNameId;
      m_iconResId = resIconId;
      m_itemCode = itemCode;
    }

    public int
    getItemName() {
      return m_itemNameId;
    }

    public int
    getIconResId() {
      return m_iconResId;
    }

    public int
    getItemCode() {
      return m_itemCode;
    }

    ///////////////////////////////////////////////////////////////////////////

    /** Drawer item name */
    private final int m_itemNameId;

    /** Resource ID of a drawable to be shown as the item's icon */
    private final int m_iconResId;

    /** Item code for feedback to the host activity's implemented callback. */
    private final int m_itemCode;

  }

  /**
   * Customized DrawerListAdapter to furnishes the Drawer with DrawerItem
   * information.
   */
  private static class DrawerListAdapter extends ArrayAdapter<DrawerItem> {

    public DrawerListAdapter(Context context, ArrayList<DrawerItem> drawerItems) {
      super(context, 0, drawerItems);
      m_layoutInflater =
          (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      m_resources = context.getResources();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      DrawerItemHolder holder;

      if (convertView == null) {
        holder = new DrawerItemHolder();

        convertView = m_layoutInflater.inflate(R.layout.list_item_drawer_item, null);
        convertView.setTag(holder);

        holder.m_icon = (ImageView) convertView.findViewById(R.id.drawer_item_icon);
        holder.m_text = (TextView) convertView.findViewById(R.id.drawer_item_text);
      } else {
        holder = (DrawerItemHolder)convertView.getTag();
      }

      // Update items in holder
      DrawerItem item = getItem(position);
      if (item.getIconResId() != 0) {
        holder.m_icon.setImageDrawable(m_resources.getDrawable(item.getIconResId()));
      }
      holder.m_text.setText(item.getItemName());

      return convertView;
    }

    private static class DrawerItemHolder {
      private ImageView m_icon;
      private TextView m_text;
    }

    /** Layout inflater for use */
    private final LayoutInflater m_layoutInflater;

    /** Reference to get context's resources */
    private final Resources m_resources;
  }

  //////////////////////////////////////////////////////////////////////////////

  /** Callback that host activity must implement */
  public static interface DrawerCallbacks {
    /** Callback to host activity when a drawer item is selected */
    void onDrawerItemSelected(int itemCode, int itemNameId);
  }

  //////////////////////////////////////////////////////////////////////////////
  private boolean isMyServiceRunning(Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Method that binds the current activity to the UbiCDN Service.
   */
  private void
  bindService() {
    if (!m_isServiceConnected) {
      // Bind to Service
      getActivity().bindService(new Intent(getActivity(),NdnOcrService.class),
              m_ServiceConnection, Context.BIND_AUTO_CREATE);
      G.Log("ServiceFragment::bindUbiCDNService()");
    }
  }

  /**
   * Method that unbinds the current activity from the UbiCDN Service.
   */
  private void
  unbindService() {
    if (m_isServiceConnected) {
      // Unbind from Service
      getActivity().unbindService(m_ServiceConnection);

      m_isServiceConnected = false;

      G.Log("ServiceFragment::unbindUbiCDNService()");
    }

  }

  /**
   * Client ServiceConnection to UbiCDN Service.
   */
  public final ServiceConnection m_ServiceConnection = new ServiceConnection() {
    @Override
    public void
    onServiceConnected(ComponentName className, IBinder service) {
      // Establish Messenger to the Service
      m_serviceMessenger = new Messenger(service);
      m_isServiceConnected = true; // onServiceConnected runs on the main thread

      // Check if UbiCDN  Service is running
      try {

        Message msg = Message.obtain(null,NdnOcrService.CHECK_SERVICE);
        msg.replyTo = m_clientMessenger;
        m_serviceMessenger.send(msg);
      } catch (RemoteException e) {
        // If Service crashes, nothing to do here
        G.Log("onServiceConnected(): " + e);
      }

      G.Log("m_ServiceConnection::onServiceConnected()");
    }

    @Override
    public void
    onServiceDisconnected(ComponentName componentName) {
      // In event of unexpected disconnection with the Service; Not expecting to get here.
      G.Log("m_ServiceConnection::onServiceDisconnected()");
      m_isServiceConnected = false; // onServiceDisconnected runs on the main thread
    }
  };

  public void
  startKbappService() {
    assert m_isServiceConnected;

    m_serviceStartStopSwitch.setText(R.string.starting_service);
    Intent myService = new Intent(getActivity(),NdnOcrService.class);
    getActivity().startService(myService);
    sendServiceMessage(NdnOcrService.CHECK_SERVICE);
  }

  public void
  stopKbappService() {
    assert m_isServiceConnected;
    m_serviceStartStopSwitch.setText(R.string.stopping_service);
    sendServiceMessage(NdnOcrService.STOP_SERVICE);

  }

  /**
   * Convenience method to send a message to the UbiCDN Service
   * through a Messenger.
   *
   * @param message Message from a set of predefined UbiCDN Service messages.
   */
  private void
  sendServiceMessage(int message) {
    if (m_serviceMessenger == null) {
      G.Log("UbiCDN Service not yet connected");
      return;
    }
    try {
      Message msg = Message.obtain(null, message);
      msg.replyTo = m_clientMessenger;
      m_serviceMessenger.send(msg);
    } catch (RemoteException e) {
      // If Service crashes, nothing to do here
      G.Log("UbiCDN service Disconnected: " + e);
    }
  }

  private void
  setServiceRunning() {
    m_serviceStartStopSwitch.setEnabled(true);
    m_serviceStartStopSwitch.setText(R.string.service_started);
    m_serviceStartStopSwitch.setChecked(true);
  }

  private void
  setServiceStopped() {
    m_serviceStartStopSwitch.setEnabled(true);
    m_serviceStartStopSwitch.setText(R.string.service_stopped);
    m_serviceStartStopSwitch.setChecked(false);

  }

  private class ClientHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case NdnOcrService.SERVICE_RUNNING:
          setServiceRunning();
          G.Log("ClientHandler: UbiCDN is Running.");

          //m_handler.postDelayed(m_statusUpdateRunnable, 500);
          break;

        case NdnOcrService.SERVICE_STOPPED:
          setServiceStopped();
          Intent myService = new Intent(getActivity(),NdnOcrService.class);
          getActivity().stopService(myService);
          G.Log("ClientHandler: UbiCDN is Stopped.");
          break;

        default:
          super.handleMessage(msg);
          break;
      }
    }
  }
  /** Flag that marks that application is connected to the UbiCDN Service */
  private boolean m_isServiceConnected = false;

  /** Client Message Handler */
  private final Messenger m_clientMessenger = new Messenger(new ClientHandler());

  /** Messenger connection to UbiCDN Service */
  private Messenger m_serviceMessenger = null;

  /** SharedPreference: Display drawer when drawer loads for the very first time */
  private static final String PREF_DRAWER_SHOWN_TO_USER_FOR_THE_FIRST_TIME
      = "DRAWER_PRESENTED_TO_USER_ON_FIRST_LOAD";

  /** Bundle key used to (re)store position of selected drawer item */
  private static final String DRAWER_SELECTED_POSITION_BUNDLE_KEY
      = "DRAWER_SELECTED_POSITION";

  /** Bundle argument key for bundle parameters */
  private static final String BUNDLE_PARAMETERS = "net.named_data.nfd.drawer_fragment_parameters";

  /** Callback to parent activity */
  private DrawerCallbacks m_callbacks;

  /** DrawerToggle for interacting with drawer and action bar app icon */
  //private ActionBarDrawerToggle m_drawerToggle;

  /** Reference to DrawerLayout fragment in host activity */
  private DrawerLayout m_drawerLayout;

  /** Reference to drawer's ListView */
  private ListView m_drawerListView;

  /** Drawer's fragment container in the host activity */
  private View m_drawerFragmentViewContainer;

  /** Current position of the Drawer's selection */
  private int m_drawerSelectedPosition = 0;

  /** Flag that denotes if the fragment is restored from an instance state */
  private boolean m_restoredFromSavedInstanceState;

  /** Flag that denotes if the user has seen the Drawer when the app loads for the first time */
  private boolean m_hasUserSeenDrawer;

  /** ArrayList of DrawerItems to be displayed in the Drawer */
  private ArrayList<DrawerItem> m_drawerItems;

  /** Flag that marks if drawer is sliding outwards and being displayed */
  private boolean m_shouldHideOptionsMenu = false;

  private Switch m_serviceStartStopSwitch;

}
