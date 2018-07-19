package uk.ac.ucl.kbapp.net.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import uk.ac.ucl.ndnocr.data.StatsHandler;
import uk.ac.ucl.kbapp.net.Link;
import uk.ac.ucl.kbapp.utils.Config;
import uk.ac.ucl.kbapp.utils.G;
import uk.ac.ucl.kbapp.utils.TimersPreferences;

//import uk.ac.ucl.ndnocr.backend.FireBaseLogger;


/**
 * Created by srenevic on 03/08/17.
 */
public class WifiLink implements Link {


    static final public int ConectionStateNONE = 0;
    static final public int ConectionStatePreConnecting = 1;
    static final public int ConectionStateConnecting = 2;
    static final public int ConectionStateConnected = 3;
    static final public int ConectionStateDisconnected = 4;

    private int  mConectionState = ConectionStateNONE;

    public static final String TAG = "WifiLink";

    private boolean hadConnection = false;

    //StatsHandler stats;
    WifiLinkListener listener;
    WifiManager wifiManager = null;
    WifiConfiguration wifiConfig = null;
    Context context = null;
    int netId = 0;

    WiFiConnectionReceiver receiver;
    private IntentFilter filter;
    String inetAddress = "";
    boolean connected=false;
    String ssid;
    WifiLink that;

    Handler handler;
    // create a class member variable.
    WifiManager.WifiLock mWifiLock = null;
    PowerManager.WakeLock wakeLock;

    Date started;

public WifiLink(Context context)
   {

        this.context = context;
        this.listener = (WifiLinkListener)context;

        filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        //mWifiDirectServiceDiscovery.stop();
        receiver = new WiFiConnectionReceiver();

        //WIFI connection
        this.wifiManager = (WifiManager)this.context.getSystemService(this.context.WIFI_SERVICE);
        handler = new Handler();

        that = this;

    }


    @Override
    public void connect(String SSID, String password){

       // G.Log(TAG,"Connect "+connected+" "+mConectionState);
        if(!connected&&(mConectionState==ConectionStateNONE||mConectionState==ConectionStateDisconnected)) {
            started = new Date();
            //FireBaseLogger.connectionStarted(context,started);
            G.Log(TAG, "New connection SSID:" + SSID + " Pass:" + password);

            this.wifiConfig = new WifiConfiguration();
            this.wifiConfig.SSID = String.format("\"%s\"", SSID);
            //this.wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            this.wifiConfig.preSharedKey = String.format("\"%s\"", password);
            ssid = this.wifiManager.getConnectionInfo().getSSID();
            this.wifiConfig.priority = 10000;
            G.Log(TAG,"Connected to "+ssid);
            List<WifiConfiguration> wifis = this.wifiManager.getConfiguredNetworks();
            boolean result;
            if(wifis!=null) {
                for (WifiConfiguration wifi : wifis) {
                    result = this.wifiManager.disableNetwork(wifi.networkId);
                    G.Log(TAG,"Disable "+wifi.SSID+" "+result);
                    if(wifi.SSID.startsWith("DIRECT-"))
                        this.wifiManager.removeNetwork(wifi.networkId);
                }
            }

            this.context.registerReceiver(receiver, filter);
            this.netId = this.wifiManager.addNetwork(this.wifiConfig);
            this.wifiManager.disconnect();
            this.wifiManager.enableNetwork(this.netId, false);
            boolean success = this.wifiManager.reconnect();

            connected = true;
            hadConnection=false;

            holdWifiLock();
            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 100ms
                    if (!hadConnection) {
                        disconnect();
                    }
                }
            },Config.wifiConnectionWaitingTime);
        }
    }

    @Override
    public void disconnect(){
        releaseWifiLock();
        handler.removeCallbacksAndMessages(null);
        G.Log(TAG,"Disconnect");
        if(connected){
            connected = false;
            this.context.unregisterReceiver(receiver);
            this.wifiManager.removeNetwork(this.netId);
            List<WifiConfiguration> wifis = this.wifiManager.getConfiguredNetworks();
            if(wifis!=null) {
                for (WifiConfiguration wifi : wifis) {
                    boolean attempt = false;
                    if (wifi.SSID.equals(ssid)) attempt = true;
                    boolean result = this.wifiManager.enableNetwork(wifi.networkId, attempt);
                    G.Log(TAG,"Wifi enable "+wifi.SSID + " "+result);

                }
            }
           // wakeLock.release();
            mConectionState=0;
            listener.wifiLinkDisconnected(this);
        }

    }

    public void SetInetAddress(String address){
        this.inetAddress = address;
    }

    public String GetInetAddress(){
        return this.inetAddress;
    }

    @Override
    public void sendFrame(byte[] frameData)
    {

    }

    @Override
    public long getNodeId(){
        return 0;
    }

    @Override
    public int getPriority(){
        return 0;

    }

    /***
     * Calling this method will aquire the lock on wifi. This is avoid wifi
     * from going to sleep as long as <code>releaseWifiLock</code> method is called.
     **/
    private void holdWifiLock() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if( mWifiLock == null )
            mWifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, TAG);

        mWifiLock.setReferenceCounted(false);

        if( !mWifiLock.isHeld() )
            mWifiLock.acquire();
    }

    /***
     * Calling this method will release if the lock is already help. After this method is called,
     * the Wifi on the device can goto sleep.
     **/
    private void releaseWifiLock() {

        if( mWifiLock == null )
            Log.w(TAG, "#releaseWifiLock mWifiLock was not created previously");

        if( mWifiLock != null && mWifiLock.isHeld() ){
            mWifiLock.release();
        }

    }


    @SuppressWarnings("unchecked")
    private static void setStaticIpConfiguration(WifiManager manager, WifiConfiguration config, InetAddress ipAddress, int prefixLength, InetAddress gateway, InetAddress[] dns) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException, InstantiationException
    {
        // First set up IpAssignment to STATIC.
        Object ipAssignment = getEnumValue("android.net.IpConfiguration$IpAssignment", "STATIC");
        callMethod(config, "setIpAssignment", new String[] { "android.net.IpConfiguration$IpAssignment" }, new Object[] { ipAssignment });

        // Then set properties in StaticIpConfiguration.
        Object staticIpConfig = newInstance("android.net.StaticIpConfiguration");
        Object linkAddress = newInstance("android.net.LinkAddress", new Class<?>[] { InetAddress.class, int.class }, new Object[] { ipAddress, prefixLength });

        setField(staticIpConfig, "ipAddress", linkAddress);
        setField(staticIpConfig, "gateway", gateway);
        getField(staticIpConfig, "dnsServers", ArrayList.class).clear();
        for (int i = 0; i < dns.length; i++)
            getField(staticIpConfig, "dnsServers", ArrayList.class).add(dns[i]);

        callMethod(config, "setStaticIpConfiguration", new String[] { "android.net.StaticIpConfiguration" }, new Object[] { staticIpConfig });
        manager.updateNetwork(config);
        manager.saveConfiguration();
    }


    private static Object newInstance(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException
    {
        return newInstance(className, new Class<?>[0], new Object[0]);
    }

    private static Object newInstance(String className, Class<?>[] parameterClasses, Object[] parameterValues) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException
    {
        Class<?> clz = Class.forName(className);
        Constructor<?> constructor = clz.getConstructor(parameterClasses);
        return constructor.newInstance(parameterValues);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object getEnumValue(String enumClassName, String enumValue) throws ClassNotFoundException
    {
        Class<Enum> enumClz = (Class<Enum>)Class.forName(enumClassName);
        return Enum.valueOf(enumClz, enumValue);
    }

    private static void setField(Object object, String fieldName, Object value) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException
    {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.set(object, value);
    }

    private static <T> T getField(Object object, String fieldName, Class<T> type) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException
    {
        Field field = object.getClass().getDeclaredField(fieldName);
        return type.cast(field.get(object));
    }

    private static void callMethod(Object object, String methodName, String[] parameterTypes, Object[] parameterValues) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
    {
        Class<?>[] parameterClasses = new Class<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++)
            parameterClasses[i] = Class.forName(parameterTypes[i]);

        Method method = object.getClass().getDeclaredMethod(methodName, parameterClasses);
        method.invoke(object, parameterValues);
    }

    private class WiFiConnectionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if(info != null) {

                    if (info.isConnected()) {
                        mConectionState = ConectionStateConnected;
                    }else if(info.isConnectedOrConnecting()) {
                        mConectionState = ConectionStateConnecting;
                    }else {
                        if(hadConnection){
                            mConectionState = ConectionStateDisconnected;
                        }else{
                            mConectionState = ConectionStatePreConnecting;
                        }
                    }

                    G.Log(TAG,"DetailedState: " + info.getDetailedState());

                    String conStatus = "";
                    if(mConectionState == WifiLink.ConectionStateNONE) {
                        conStatus = "NONE";
                    }else if(mConectionState == WifiLink.ConectionStatePreConnecting) {
                        conStatus = "PreConnecting";
                    }else if(mConectionState == WifiLink.ConectionStateConnecting) {
                        conStatus = "Connecting";
                    }else if(mConectionState == WifiLink.ConectionStateConnected) {
                        conStatus = "Connected";
                    }else if(mConectionState == WifiLink.ConectionStateDisconnected) {
                        conStatus = "Disconnected";
                        G.Log(TAG,"Had connection "+hadConnection);

                        if(hadConnection)disconnect();

                    }
                    G.Log(TAG, "Status " + conStatus);

                }

                WifiInfo wiffo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);

                if(wiffo!=null)Log.d(TAG,"Wifiinfo "+wiffo.getSSID()+" "+that.wifiConfig.SSID);
                if(wiffo!=null&&mConectionState==ConectionStateConnected){

                    if(wiffo.getSSID().equals(wifiConfig.SSID)&&!hadConnection) {
                        //G.Log(TAG, "Ip address: " + wiffo.getIpAddress());
                        //G.Log(TAG, "Create face to " + inetAddress);
                        hadConnection=true;
                        G.Log(TAG, "Connected to " + wiffo);
                        listener.wifiLinkConnected(that,wifiConfig.SSID);
                        //FireBaseLogger.connectionCompleted(context,started,new Date(),wiffo.getRssi(),wiffo.getLinkSpeed(),wiffo.getFrequency());

                    }

                }
            }
        }
    }


}
