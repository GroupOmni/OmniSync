package com.groupomni.omnisync;

import android.app.Application;

import java.util.List;

public class OmniSyncApplication extends Application {

    public NSDHelperDiscover nsdHelperDiscover;
    public NSDHelperRegister nsdHelperRegister;

    public String registration_state;

    public List<DeviceListItem> deviceList;
}
