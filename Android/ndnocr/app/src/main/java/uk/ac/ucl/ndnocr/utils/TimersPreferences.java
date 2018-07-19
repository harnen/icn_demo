package uk.ac.ucl.ndnocr.utils;

import android.content.Context;
import android.util.Log;

import net.grandcentrix.tray.AppPreferences;


/**
 * Created by srenevic on 01/01/18.
 */

public class TimersPreferences{

    private final static String WIFI="WF_Waiting_time";
    private final static String WIFI_HS_RESTART="WF_Hotspot_restart_time";
    private final static String PEER_SUCCESS="Peer_success_time";
    private final static String PEER_FAILED="Peer_retry_time";
    private final static String SD_SUCCESS="Sd_success_time";
    private final static String SD_FAILED="Sd_retry_time";
    private final static String BT_SCAN="bt_scan_time";
    private final static String BT_FG="bt_idle_fg";
    private final static String BT_BG="bt_idle_bg";
    public final static String BT_ACT="bt_activated";
    public final static String WD_ACT="wd_activated";

    /*private long wifiWaitingTime;
    private long wifiPeerSuccessRetryTime;
    private long wifiPeerFailedRetryTime;
    private long wifiSdSuccessRetryTime;
    private long wifiSdFailedRetryTime;
    private long wifiHotspotRestartTime;
    private long btScanTime;
    private long wifiHsRestartTime;
    private long btIdleFgTime;
    private long btIdleBgTime;*/

    final AppPreferences appPreferences;

    public TimersPreferences(Context context){
        //Getting if is source device checkbox enabled from sharedpreferences
       appPreferences = new AppPreferences(context); // this Preference comes for free from the library
    }


    public long getWifiWaitingTime()
    {
        return appPreferences.getLong(WIFI,Config.wifiConnectionWaitingTime);

    }

    public void setWifiWaitingTime(long time)
    {
        appPreferences.put(WIFI,time);
    }

    public long getPeerSuccessTime()
    {
        return appPreferences.getLong(PEER_SUCCESS,Config.peerDiscoverySuccessTime);
    }

    public void setPeerSuccessTime(long time)
    {
        appPreferences.put(PEER_SUCCESS,time);

    }
    public long getPeerFailedTime()
    {
        return appPreferences.getLong(PEER_FAILED,Config.peerDiscoveryFailedTime);
    }

    public void setPeerFailedTime(long time)
    {
        appPreferences.put(PEER_FAILED,time);

    }
    public long getSdSuccessTime()
    {
        return appPreferences.getLong(SD_SUCCESS,Config.serviceDiscoverySuccessTime);
    }

    public void setSdSuccessTime(long time)
    {
        appPreferences.put(SD_SUCCESS,time);

    }

    public long getSdFailedTime()
    {
        return appPreferences.getLong(SD_FAILED,Config.serviceDiscoveryFailedTime);
    }

    public void setSdFailedTime(long time)
    {
        appPreferences.put(SD_FAILED,time);

    }

    public long getBtScanTime()
    {
        return appPreferences.getLong(BT_SCAN,Config.bleScanDuration);
    }

    public void setBtScanTime(long time)
    {
        appPreferences.put(BT_SCAN,time);

    }
    public long getBtIdleFgTime()
    {
        return appPreferences.getLong(BT_BG,Config.bleAdvertiseForegroundDuration);
    }

    public void setBtIdleFgTime(long time)
    {
        appPreferences.put(BT_FG,time);

    }
    public long getBtIdleBgTime()
    {
        return appPreferences.getLong(BT_BG,Config.bleAdvertiseBackgroundDuration);
    }

    public void setBtIdleBgTime(long time)
    {
        appPreferences.put(BT_BG,time);

    }

    public long getHotspotRestartTime()
    {
        return appPreferences.getLong(WIFI_HS_RESTART,Config.hotspotRestartTime);
    }

    public void setHotspotRestartTime(long time)
    {
        appPreferences.put(WIFI_HS_RESTART,time);

    }


    public void setWd(boolean active)
    {
        Log.d("Preference","Setwd "+active);
        appPreferences.put(WD_ACT,active);
    }

    public boolean getWd()
    {
        Log.d("Preference","getwd "+appPreferences.getBoolean(WD_ACT,false));
        return appPreferences.getBoolean(WD_ACT,false);
    }

    public void setBt(boolean active)
    {
        Log.d("Preference","Setbt "+active);
        appPreferences.put(BT_ACT,active);

    }

    public boolean getBt()
    {
        Log.d("Preference","getBt "+appPreferences.getBoolean(BT_ACT,false));
        return appPreferences.getBoolean(BT_ACT,false);
    }


}
