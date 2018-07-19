package uk.ac.ucl.ndnocr;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import uk.ac.ucl.ndnocr.utils.G;


/*
 * UbiCDN base app used for global variables and libraries initialization
 */

public class App extends Application {
    protected static final String TAG = "App";

    protected boolean source;

    private String m_name;

    @Override
    public void onCreate() {
        super.onCreate();
        initNotificationChannel();

        // Initialize image loader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        //SharedPreferences m_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //setSource(m_sharedPreferences.getBoolean(ServiceFragment.PREF_UBICDN_SERVICE_SOURCE,false));
        try{
            m_name = new String(Settings.Secure.getString(getContentResolver(), "bluetooth_name").getBytes("UTF-8"));
        }catch (Exception e){
            G.Log("Getname exception "+e);}


    }


    public void initNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }


        // Create channel to show notifications.
        String channelId  = getString(R.string.default_notification_channel_id);
        String channelName = getString(R.string.default_notification_channel_name);
        NotificationManager notificationManager =
                getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_LOW));


    }

    public void setSource (boolean source)
    {
        this.source = source;
    }

    public boolean getSource()
    {
        return source;
    }

    public String getName()
    {
        return m_name;
    }

    public void setName(String name)
    {
        m_name = name;
    }

}
