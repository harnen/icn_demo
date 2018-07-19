package uk.ac.ucl.kbapp;

import android.app.Notification;
import android.app.Service;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.content.Intent;

import com.intel.jndn.management.types.ForwarderStatus;

import net.grandcentrix.tray.AppPreferences;
import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.InterestFilter;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnInterestCallback;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.OnTimeout;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;

//import uk.ac.ucl.ndnocr.backend.FireBaseLogger;
//import uk.ac.ucl.ndnocr.backend.VideoListService;
//import uk.ac.ucl.ndnocr.data.Content;
//import uk.ac.ucl.ndnocr.data.StatsHandler;
//import uk.ac.ucl.ndnocr.data.VideoDatabaseHandler;
//import uk.ac.ucl.ndnocr.ui.fragments.ServiceFragment;

import uk.ac.ucl.kbapp.net.Discovery;
import uk.ac.ucl.kbapp.net.Link;
import uk.ac.ucl.kbapp.net.LinkListener;
import uk.ac.ucl.kbapp.utils.Config;
import uk.ac.ucl.kbapp.utils.MyNfdc;
import uk.ac.ucl.kbapp.utils.DispatchQueue;
import uk.ac.ucl.kbapp.utils.G;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import uk.ac.ucl.kbapp.net.wifi.*;
import uk.ac.ucl.kbapp.utils.TimersPreferences;

import static java.lang.Thread.sleep;

/**
 * Created by srenevic on 03/08/17.
 */

public abstract class KebappService extends Service implements OnInterestCallback, OnRegisterFailed, OnData, OnTimeout, LinkListener, WifiLinkListener {

    /** debug tag */
    public static final String TAG = "KebappService";

    //Service type advertised in WiFi Direct
    public static final String SERVICE_INSTANCE = ".kbapp";
    public static final String SERVICE_TYPE = ".kbapp._tcp";

    public static final int CHECK_SERVICE = 0;
    /** Message to start Service */
    public static final int START_SERVICE = 1;
    /** Message to stop Service */
    public static final int STOP_SERVICE = 2;
    /** Message to indicate that Service is running */
    public static final int SERVICE_RUNNING = 3;
    /** Message to indicate that Service is not running */
    public static final int SERVICE_STOPPED = 4;
    /** Message to indicate that WifiDirect is connected */
    public static final int SERVICE_WIFI_CONNECTED = 5;

    //Connectivity classes
    public KebappService that = this;
    Link mWifiLink = null;
    Discovery mWifiServiceDiscovery = null;
    DispatchQueue queue;

    //While true NDN face is processing events
    public boolean shouldStop=true;
    public int retry=0;
    public int nfdRetry=0;

    /** Messenger to handle messages that are passed to the NfdService */
    protected Messenger m_serviceMessenger = null;

    /** Flag that denotes if the NFD has been started */
    private boolean m_isServiceStarted = false;
    private boolean m_isConnected = false;

    public int id;
    private Handler m_handler;
    private Runnable m_statusUpdateRunnable;

    public int faceId;
    TimersPreferences timers;

    protected List<String> init = new ArrayList<>();

    boolean shouldConnect=false;

    //Loading JNI libraries used to run NFD
    static {
        System.loadLibrary("crystax");
        System.loadLibrary("gnustl_shared");
        System.loadLibrary("cryptopp_shared");
        System.loadLibrary("boost_system");
        System.loadLibrary("boost_filesystem");
        System.loadLibrary("boost_date_time");
        System.loadLibrary("boost_iostreams");
        System.loadLibrary("boost_program_options");
        System.loadLibrary("boost_chrono");
        System.loadLibrary("boost_random");
        System.loadLibrary("ndn-cxx");
        System.loadLibrary("boost_thread");
        System.loadLibrary("nfd-daemon");
        System.loadLibrary("nfd-wrapper");
    }
    /**
     * Native API for starting the NFD.
     *
     * @param params NFD parameters.  Must include 'homePath' with absolute path of the home directory
     *               for the service (ContextWrapper.getFilesDir().getAbsolutePath())
     */
    public native static void
    startNfd(Map<String, String> params);

    /**
     * Native API for stopping the NFD.
     */
    public native static void
    stopNfd();

    public native static List<String>
    getNfdLogModules();

    public native static boolean
    isNfdRunning();

    @Override
    public IBinder onBind(Intent intent) {

        G.Log(TAG,"Service onBind");
        return m_serviceMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        G.Log(TAG,"KebappService::onCreate()");
        super.onCreate();

        m_serviceMessenger = new Messenger(new ServiceMessageHandler());
        this.queue = new DispatchQueue();
        mWifiServiceDiscovery = new WifiServiceDiscovery(this, this);//, stats););
        mWifiLink = new WifiLink(this);
        m_handler = new Handler();

        m_statusUpdateRunnable = new Runnable() {
            @Override
            public void run()
            {
                new StatusUpdateTask().execute();
            }
        };


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new Notification.Builder(this).getNotification();
        AppPreferences appPreferences = new AppPreferences(getApplicationContext());
        startForeground(1000,notification);
        serviceStart();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        G.Log(TAG,"KebappService::onDestroy()");
        serviceStop();
        stopSelf();
        m_serviceMessenger = null;
        super.onDestroy();

    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Thread safe way of starting the service and updating the
     * started flag.
     */
    private synchronized void
    serviceStart() {

        if (!m_isServiceStarted) {
            m_isServiceStarted = true;
            G.Log(TAG,"Started");
            faceId=0;
            HashMap<String, String> params = new HashMap<>();
            params.put("homePath", getFilesDir().getAbsolutePath());
            Set<Map.Entry<String,String>> e = params.entrySet();

            startNfd(params);

            // Example how to retrieve all available NFD log modules
            List<String> modules = getNfdLogModules();
            for (String module : modules) {
                G.Log(module);
            }

            m_handler.postDelayed(m_statusUpdateRunnable, 50);

            G.Log(TAG, "serviceStart()");
        } else {
            G.Log(TAG, "serviceStart(): UbiCDN Service already running!");
        }
    }

    /**
     * Thread safe way of stopping the service and updating the
     * started flag.
     */
    private synchronized void
    serviceStop() {
        if (m_isServiceStarted) {
            m_isServiceStarted = false;
            // TODO: Save NFD and NRD in memory data structures.
            stopNfd();
            mWifiLink.disconnect();
            mWifiServiceDiscovery.stop();
            m_handler.removeCallbacks(m_statusUpdateRunnable);
            G.Log(TAG, "serviceStop()");
        }
    }


    private void initService()
    {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    G.Log(TAG, "Init service thread");

                    final Face mFace = new Face("localhost");
                    KeyChain keyChain = buildTestKeyChain();
                    mFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());
                    mWifiServiceDiscovery.start(false,id);

                    while (m_isServiceStarted) {
                            mFace.processEvents();
                    }


                } catch (Exception ex) {
                    G.Log(TAG, ex.toString());
                }
            }
        }).start();

    }


    @Override
    public void linkConnected(Link link)
    {
        G.Log(TAG,"btLinkConnected");

    }

    @Override
    public void linkDisconnected(Link link)
    {
        G.Log(TAG,"btLinkDisconnected");
    }

    protected void setConnect(boolean connect){
         shouldConnect=connect;
    }
    @Override
    public void linkNetworkDiscovered(Link link, String network)
    {

        G.Log(TAG, "Frame received " + network);
        String[] separated = network.split(":");

        if(!separated[0].equals("")&&!separated[1].equals("")&&shouldConnect) {
            shouldConnect=false;
            G.Log(TAG,"StartConnection");
            mWifiServiceDiscovery.stop();
            //turnOnScreen();
            mWifiLink.connect(separated[0], separated[1]);

        }

    }


    @Override
    public void wifiLinkConnected(Link link, String network)
    {
        G.Log(TAG,"wifiLinkConnected "+network);
        if(!m_isConnected) {
            m_isConnected = true;
        }
    } // btLinkConnected()



    @Override
    public void wifiLinkDisconnected(Link link)
    {
        G.Log(TAG,"wifiLinkDisconnected");
        m_isConnected = false;

        try {
            MyNfdc nfdc = new MyNfdc();
            if(faceId!=0){
                nfdc.ribUnregisterPrefix(new Name("/ubicdn/video/"), faceId);
                nfdc.faceDestroy(faceId);
            }
            faceId=0;
            nfdc.shutdown();
        }catch (Exception e){}
        if(m_isServiceStarted) {
            mWifiServiceDiscovery.start(false, id);
        }
        //serviceStop();
        //serviceStart();
    } // btLinkDisconnected()

    public void disconnect(){

        G.Log(TAG,"Just disconnect");
        serviceStop();
        serviceStart();
    }


    //For any interest received sends the corresponding video back if found in local storage
    public abstract void onInterest(Name name, Interest interest, Face face, long l, InterestFilter filter);

    //For any data received (video file) save it in the local storage and update the video list db
    public abstract void onData(Interest interest, Data data);


    public abstract void onTimeout(Interest interest);

    public void onRegisterFailed(Name name){
        G.Log(TAG, "Failed to register the data");

    }


    public String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            //G.Log(TAG,"IP address "+getDottedDecimalIP(inetAddress.getAddress()));
                            if(getDottedDecimalIP(inetAddress.getAddress()).startsWith("192.168."))
                                return getDottedDecimalIP(inetAddress.getAddress());
                        }
                        //return inetAddress.getHostAddress().toString(); // Galaxy Nexus returns IPv6
                    }
                }
            }
        } catch (SocketException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        } catch (NullPointerException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        }
        return null;
    }


    private String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }


    private String getOwnerAddress() {
        return "192.168.49.1";
    }


    public static KeyChain buildTestKeyChain() throws SecurityException {
        MemoryIdentityStorage identityStorage = new MemoryIdentityStorage();
        MemoryPrivateKeyStorage privateKeyStorage = new MemoryPrivateKeyStorage();
        IdentityManager identityManager = new IdentityManager(identityStorage, privateKeyStorage);
        KeyChain keyChain = new KeyChain(identityManager);
        try {
            keyChain.getDefaultCertificateName();
        } catch (SecurityException e) {
            keyChain.createIdentity(new Name("/test/identity"));
            keyChain.getIdentityManager().setDefaultIdentity(new Name("/test/identity"));
        }
        return keyChain;
    }

    /*public static KeyChain buildValidationKeyChain(Context context) throws SecurityException {

        //prepare the key chain
        MemoryIdentityStorage identityStorage = new MemoryIdentityStorage();
        PrivateKeyStorage keyStorage = new FilePrivateKeyStorage();
        IdentityManager identityManager = new IdentityManager(identityStorage, keyStorage);
        SelfVerifyPolicyManager policyManager = new SelfVerifyPolicyManager(identityStorage);
        KeyChain keyChain = new KeyChain(identityManager, policyManager);

        IdentityCertificate newCert = new IdentityCertificate();
        byte[] certData = Common.base64Decode(CERT_DUMP);
        byte[] keyData = Common.base64Decode(KEY_DUMP);
        PublicKey newKey = new PublicKey(new Blob(keyData));

        try {
            newCert.wireDecode(new Blob(certData, false));
        }catch (EncodingException e){G.Log(TAG,"Exception buildValidationKeyChain "+e);}

        newCert.setPublicKeyInfo(newKey);
        keyChain.installIdentityCertificate(newCert);

        return keyChain;

    }*/

    /**
     * Message handler for the the ubiCDN Service.
     */
    private class ServiceMessageHandler extends Handler {

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case KebappService.START_SERVICE:
                    G.Log(TAG,"Non source start service");
                    //source=false;
                    serviceStart();
                    replyToClient(message, KebappService.SERVICE_RUNNING);
                    break;

                case KebappService.CHECK_SERVICE:
                    if(m_isServiceStarted)
                        replyToClient(message, KebappService.SERVICE_RUNNING);
                    else
                        replyToClient(message, KebappService.SERVICE_STOPPED);
                    break;
                case KebappService.STOP_SERVICE:
                    G.Log(TAG,"Non source stop service");
                    serviceStop();
                    stopForeground(true);
                    stopSelf();
                    replyToClient(message, KebappService.SERVICE_STOPPED);
                    break;

                case KebappService.SERVICE_WIFI_CONNECTED:
                    G.Log(TAG,"Wifi connection completed");
                    // createFaceandSend();
                    break;

                default:
                    super.handleMessage(message);
                    break;
            }
        }

        private void
        replyToClient(Message message, int replyMessage) {
            try {
                message.replyTo.send(Message.obtain(null, replyMessage));
            } catch (RemoteException e) {
                // Nothing to do here; It means that client end has been terminated.
            }
        }
    }

    private class StatusUpdateTask extends AsyncTask<Void, Void, ForwarderStatus> {
        /**
         * @param voids
         * @return ForwarderStatus if operation succeeded, null if operation failed
         */
        @Override
        protected ForwarderStatus
        doInBackground(Void... voids)
        {
            try {
                MyNfdc nfdcHelper = new MyNfdc();
                ForwarderStatus fs = nfdcHelper.generalStatus();
                nfdcHelper.shutdown();
                return fs;
            }
            catch (Exception e) {
                nfdRetry++;
                G.Log(TAG,"Error communicating with NFD (" + e.getMessage() + ")");
                if(nfdRetry>Config.nfdMaxRetry){
                    serviceStop();
                    serviceStart();
                }
                return null;
            }
        }

        @Override
        protected void
        onPostExecute(ForwarderStatus fs)
        {
            if (fs == null) {
                // when failed, try after 0.5 seconds
                m_handler.postDelayed(m_statusUpdateRunnable, 50);
            }
            else {
                // refresh after 5 seconds
                initService();
            }
        }
    }


}