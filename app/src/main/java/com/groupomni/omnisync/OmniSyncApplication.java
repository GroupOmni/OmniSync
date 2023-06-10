package com.groupomni.omnisync;

import android.app.Application;
import android.content.Context;
import android.net.nsd.NsdServiceInfo;

import java.util.List;

public class OmniSyncApplication extends Application {

    public NSDHelperDiscover nsdHelperDiscover;
    public NSDHelperRegister nsdHelperRegister;

    public String registration_state;

    public List<DeviceListItem> deviceList;
    public List<NsdServiceInfo> serviceList;

    public int upstreamBandwidth;
    public int downstreamBandwidth;

    public long remainingStorage;

    public int foregroundOnStartCommand;

    public boolean isSuperPeer;
    public List<DeviceListItem> peers;

    public HTTPServer httpServer;
    public HTTPClient httpClient;

    public PeerManagerUtils peerManagerUtils;

    public static Context appContext;

    public String syncFolder;

    public FileMangerUtils fileMangerUtils;

    public String selfIP;

    public SyncFTPServer ftpServer;
    public SyncFTPClient ftpClient;
}
