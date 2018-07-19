package uk.ac.ucl.kbapp.net;

import android.app.Activity;

/**
 * Created by srene on 12/10/17.
 */

public interface Discovery {


    /**
     * Starts underlying network advertising and discovery.
     * For each call of this method there must be corresponding
     * stop() call.
     */
    void start(boolean source, int id);

    /**
     * Stops network advertising and discovery
     * and disconnects all links.
     * For each call of this method there must be corresponding
     * start() call in the past.
     */
    void stop();


    public interface AcceptCallback {
        void accept(Activity activity);
    }

    public interface DiscoveryCallback {

    void onUserDiscovered();


    void onServiceDiscovered();


    void onNetworkConnected();

    }

    /**
     * Called when transport needs application activity for its function.
     * @param transport transport that requests activity.
     * @param callback callback to send activity. Can be called back on any thread.
     */
    //void transportNeedsActivity(AcceptCallback callback);

    /**
     * Called when transport discovered new device and established connection with it.
     * @param transport transport instance that discovered the device
     * @param link connection object to discovered device
     */
    void transportLinkConnected(Link link);

    /**
     * Called when connection to device is closed explicitly from either side
     * or because device is out of range.
     * @param transport transport instance that lost the device
     * @param link connection object to disconnected device
     */
    void transportLinkDisconnected(Link link);

    /**
     * Called when new data frame is received from remote device.
     * @param transport transport instance that connected to the device
     * @param link connection object for the device
     * @param frameData frame data received from remote device
     * @see Link#sendFrame(byte[])
     */
   // void btLinkDidReceiveFrame(Link link, byte[] frameData);

}
