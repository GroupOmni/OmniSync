package com.groupomni.omnisync;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;


public class DevicesFragment extends Fragment implements NSDHelperDiscover.OnResolveCompleted{

    private NSDHelperRegister nsdHelperRegister;
    private NSDHelperDiscover nsdHelperDiscover;

    private ListView deviceListView;
    private DeviceListArrayAdapter deviceListArrayAdapter;

    private OmniSyncApplication app;

    public DevicesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (OmniSyncApplication)  requireActivity().getApplicationContext();

        List<DeviceListItem> devices = new ArrayList<>();

        if (app.nsdHelperDiscover != null) {
            nsdHelperDiscover = app.nsdHelperDiscover;
            nsdHelperDiscover.setOnResolveCompletedListUpdate(this);
        }

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_devices, container, false);
        deviceListView = view.findViewById(R.id.deviceList);
        deviceListView.setAdapter(deviceListArrayAdapter);

        Log.d("FROM DF",String.valueOf(app.deviceList.size()));

        deviceListArrayAdapter = new DeviceListArrayAdapter(requireActivity().getApplicationContext(), app.deviceList);
        deviceListView.setAdapter(deviceListArrayAdapter);
        return view;
    }

    @Override
    public void updateDeviceList() {
        try {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("TAG", "updating list");
                    Log.d("TAG", String.valueOf(app.deviceList.size()));
                    deviceListArrayAdapter = new DeviceListArrayAdapter(requireActivity().getApplicationContext(), app.deviceList);
                    deviceListView.setAdapter(deviceListArrayAdapter);
                }
            });
        }catch (IllegalStateException e){

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        deviceListArrayAdapter = new DeviceListArrayAdapter(requireActivity().getApplicationContext(), app.deviceList);
        deviceListView.setAdapter(deviceListArrayAdapter);
    }

}