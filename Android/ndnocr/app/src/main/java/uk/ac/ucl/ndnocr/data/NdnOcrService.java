package uk.ac.ucl.ndnocr.data;

import android.app.ActivityManager;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.widget.Toast;

import uk.ac.ucl.kbapp.KebappService;
import uk.ac.ucl.kbapp.net.Link;
import uk.ac.ucl.kbapp.utils.MyNfdc;
import uk.ac.ucl.ndnocr.MainActivity;
import uk.ac.ucl.ndnocr.R;
import uk.ac.ucl.ndnocr.utils.Config;
import uk.ac.ucl.kbapp.utils.G;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.InterestFilter;
import net.named_data.jndn.Name;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.util.Blob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.sleep;

public class NdnOcrService extends KebappService {

    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    private DatabaseHandler db;

    public static final String NEW_RESULT = "action_download";
    @Override
    public void onCreate(){
        super.onCreate();
        db = new DatabaseHandler(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public IBinder onBind(Intent intent) {

        G.Log(TAG,"Service onBind");
        return m_serviceMessenger.getBinder();
    }

    public void turnOnScreen(){
        // turn on screen
        G.Log(TAG,"ProximityActivity", "ON!");
        mPowerManager =  (PowerManager) getSystemService(Context.POWER_SERVICE);
        //PowerManager.ACQUIRE_CAUSES_WAKEUP
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
        mWakeLock.acquire();


    }

    @Override
    public void linkNetworkDiscovered(Link link, String network)
    {

        G.Log(TAG, "Frame received " + network+" "+db.getContentDownloaded().size());
        String[] separated = network.split(":");

        if(separated.length<=3) {
            if(db.getPendingCount()>0){
                setConnect(true);
                sendToast("Content to process on link "+separated[0]);
            }else {
                sendToast("Link "+separated[0]+" discovered but no content to process");
            }
        }
        super.linkNetworkDiscovered(link,network);
    }

    @Override
    public void wifiLinkConnected(Link link, String network) {

        G.Log(TAG, "Wifi Link connected "+network+" "+Config.SSID);
        if(db.getPendingCount()>0){
            createFaceandSend(uk.ac.ucl.kbapp.utils.Config.GW_IP, uk.ac.ucl.kbapp.utils.Config.prefix);
        }
        super.wifiLinkConnected(link,network);
    }

    public void onInterest(Name name, Interest interest, Face face, long l, InterestFilter filter) {

        G.Log(TAG,"Interest received "+interest.getName().toString());
        //filter.
        // /todo check if the file exists first
        // if(interest.getNonce().equals(nonce))return;
        try {
            byte[] bytes;
            Data data;

            File dir = getFilesDir();
            File[] subFiles = dir.listFiles();
            for (File file : subFiles) {
                G.Log("Filename " + file.getAbsolutePath() + " " + file.getName() + " " + file.length());
            }
            String filename = interest.getName().get(1).toEscapedString();
            File f = new File(getFilesDir() + "/" + filename);
            InputStream fis = new FileInputStream(f);
            bytes = new byte[(int) f.length()];



            try {
                fis.read(bytes);
                Blob blob = new Blob(bytes);
                data = new Data();
                data.setName(interest.getName());
                //data.wireDecode(blob);
                data.setContent(blob);

                G.Log(TAG,"Get file " + data.getContent().size());
                //FireBaseLogger.videoSent(this,new Date(),interest.getName().get(2).toEscapedString());
                face.putData(data);

            } catch (IOException e) {
                G.Log(TAG, e.getMessage());
            //}catch(EncodingException e){
            //     G.Log(TAG,e.getMessage());
            }finally {
                fis.close();
            }

        } catch (FileNotFoundException e) {
            G.Log(TAG, e.getMessage());
        } catch (IOException e){
            G.Log(TAG,e.getMessage());
        }

    }

    public void onData(Interest interest, Data data){

        G.Log(TAG,"OCR message received  "+data.getContent().toString());
        try {
            boolean fg = new ForegroundCheckTask().execute(this).get();
            if(fg)        sendToast("OCR message received " + data.getContent().toString());
            else sendNotification("OCR message received " + data.getContent().toString());
        }catch (ExecutionException e){
            e.printStackTrace();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        db.setContentText(data.getName().get(3).toEscapedString(),data.getContent().toString());
        Intent broadcast = new Intent(NEW_RESULT);
        sendBroadcast(broadcast);

    }

    public void onTimeout(Interest interest){
        G.Log(TAG,"Timeout for interest "+interest.getName());
    }
    public void sendToast(final String msg)
    {
        // prepare intent which is triggered if the
// notification is selected


        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        msg,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void sendNotification(final String msg){
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

// build notification
// the addAction re-use the same intent to keep the example short
        Notification n  = new Notification.Builder(this)
                .setContentTitle("NDN OCR Application")
                .setContentText(msg)
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.icon, "Call", pIntent)
                .addAction(R.drawable.icon, "More", pIntent)
                .addAction(R.drawable.icon, "And more", pIntent).build();


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, n);

        // Turn on the screen for notification
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean result= Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT_WATCH&&powerManager.isInteractive()|| Build.VERSION.SDK_INT< Build.VERSION_CODES.KITKAT_WATCH&&powerManager.isScreenOn();

        if (!result){
            PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MH24_SCREENLOCK");
            wl.acquire(10000);
            PowerManager.WakeLock wl_cpu = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MH24_SCREENLOCK");
            wl_cpu.acquire(10000);
        }
    }

    /* Creates the face towards the group owner and send interests to the NFD daemon for
      any pending video not received once connected to the WifiDirect network (WifiLink succeed)
     */
    public void createFaceandSend(final String IP, final String uri) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sleep(Config.createFaceWaitingTime);
                    MyNfdc nfdc = new MyNfdc();
                    retry++;
                    G.Log(TAG,"Create face to "+IP+" for "+uri +" "+retry);
                    faceId = nfdc.faceCreate("tcp://"+IP);

                    G.Log(TAG,"Register prefix "+uri);
                    nfdc.ribRegisterPrefix(new Name("/exec"+uk.ac.ucl.kbapp.utils.Config.prefix), faceId, 0, true, false);
                    nfdc.shutdown();

                    KeyChain keyChain = buildTestKeyChain();
                    Face mFace = new Face("localhost");
                    mFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());

                    mFace.registerPrefix(new Name("/"+getLocalIPAddress()+"/result"), that, that);
                    for(String cont : db.getPendingContent()) {
                        G.Log(TAG,"send interest /exec"+Config.prefix+cont);

                        final Name requestName = new Name("/exec"+Config.prefix+getLocalIPAddress()+"/"+cont);
                        final Name localName = new Name("/"+getLocalIPAddress()+"/"+cont);
                        mFace.registerPrefix(new Name(localName), that, that);
                        Interest interest = new Interest(requestName);
                        sleep(Config.createFaceWaitingTime);
                        interest.setInterestLifetimeMilliseconds(uk.ac.ucl.kbapp.utils.Config.interestLifeTime);
                        mFace.expressInterest(interest, that, that);
                    }
                    shouldStop=false;
                    retry=0;
                    while (!shouldStop) {
                        mFace.processEvents();
                    }
                    mFace.shutdown();
                } catch (Exception e) {
                    if(retry< uk.ac.ucl.kbapp.utils.Config.maxRetry) {
                        createFaceandSend(IP, uri);
                    }else {
                        //mWifiLink.disconnect();
                        retry=0;
                        //serviceStopUbiCDN();
                        //serviceStartUbiCDN();
                    }
                    G.Log(TAG, "Error " + e);
                }

            }
        }).start();
    }

    class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
            final Context context = params[0].getApplicationContext();
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            final String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }
}
