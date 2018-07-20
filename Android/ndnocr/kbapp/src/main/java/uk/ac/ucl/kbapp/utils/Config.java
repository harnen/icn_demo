/*
 * Copyright (c) 2016 Vladimir L. Shabanov <virlof@gmail.com>
 *
 * Licensed under the Underdark License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://underdark.io/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ucl.kbapp.utils;

public class Config
{
	// All times are in milliseconds.

	public static final int frameSizeMax = 50 * 1024 * 1024;

	public static final String prefix ="/OCR/";
	public static final int bnjHeartbeatInterval = 2 * 1000;
	public static final int bnjTimeoutInterval = 7 * 1000;

	// How long to discover for Bluetooth devices.
	public static final long btScanDuration = 4 * 1000;

	// How long to scan for Bluetooth LE devices.
	public static final long bleScanDuration =2000;

	// How long to advertise for Bluetooth LE devices in foreground.
	public static final long bleAdvertiseForegroundDuration = 1 * 1000;

	// How long to advertise for Bluetooth LE devices in background.
	public static final long bleAdvertiseBackgroundDuration = 3 * 1000;

	// How long to wait between advertisements in background mode.
	public static final long bleIdleBackgroundDuration = 2 * 5 * 1000;

	// How long to remember that BLE device is unsuitable for connection.
	public static final long bleUnsuitableCooldown = 30 * 1000;

	//How long to wait for a connection to be successful
	public static final long wifiConnectionWaitingTime = 60000;

	//How long to wait for another source device
	public static final long sourceDeviceWaitingTime = 30000;

	public static final long createFaceWaitingTime = 2000;

	public static final long interestLifeTime = 50000;

	public static final long videoLifeTime = 50000;

	public static final long peerDiscoverySuccessTime = 3000;

	public static final long peerDiscoveryFailedTime = 1000;

	public static final long serviceDiscoverySuccessTime = 6000;

	public static final long serviceDiscoveryFailedTime = 1000;

	public static final long hotspotRestartTime = 30000;

	//Tries to create a face if failed because network not ready
	public static final int maxRetry = 5;

	public static final int nfdMaxRetry = 10;

	public static final String passwd = "Raspberry";

	public static final String SSID = "NDNOCR";

	public static final String GW_IP ="192.168.0.1";

	public static final String CLIENT_IP ="10.0.0.10";

} // Config
