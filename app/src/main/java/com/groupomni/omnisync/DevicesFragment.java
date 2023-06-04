package com.groupomni.omnisync;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class DevicesFragment extends Fragment implements NSDHelperDiscover.OnResolveCompleted{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private NSDHelperRegister nsdHelperRegister;
    private NSDHelperDiscover nsdHelperDiscover;

    private Button register_btn;
    private Button discover_btn;
    private Button show_btn;

    private ListView deviceListView;
    private DeviceListArrayAdapter deviceListArrayAdapter;

    private Object listUpdateLock;

    OmniSyncApplication app;

    public DevicesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        app = (OmniSyncApplication)  requireActivity().getApplicationContext();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_devices, container, false);
        List<DeviceListItem> devices = new ArrayList<>();
        deviceListView = view.findViewById(R.id.deviceList);

        listUpdateLock = new Object();

        if(app.deviceList == null){
            app.deviceList = new ArrayList<>();
        }

//        app.deviceList.add(new DeviceListItem("String and Number", 123));
        deviceListArrayAdapter = new DeviceListArrayAdapter(requireActivity().getApplicationContext(), app.deviceList);
        deviceListView.setAdapter(deviceListArrayAdapter);

        register_btn = view.findViewById(R.id.register_service);
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (nsdHelperRegister == null) {
                    Log.d("TAG", "nsdHelperRegister is NULL");
                    if (app.nsdHelperRegister == null){
                        app.registration_state = "not";
                        nsdHelperRegister = new NSDHelperRegister(requireActivity().getApplicationContext());
                        app.nsdHelperRegister = nsdHelperRegister;
                    }
                    else {
                        nsdHelperRegister = app.nsdHelperRegister;
                        if(Objects.equals(app.registration_state, "done")) {
                            Toast.makeText(requireActivity().getApplicationContext(), "Already Joined a Network", Toast.LENGTH_SHORT).show();
                        }
                        else if(Objects.equals(app.registration_state, "being")){
                            Toast.makeText(requireActivity().getApplicationContext(), "Joining to a Network", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else {
                    Log.d("TAG", "nsdHelperRegister is NOT NULL");
                    Log.d("TAG", "on click reg state : " + app.registration_state);
                    if(Objects.equals(app.registration_state, "done")) {
                        Toast.makeText(requireActivity().getApplicationContext(), "Already Joined a Network", Toast.LENGTH_SHORT).show();
                    }
                    else if(Objects.equals(app.registration_state, "being")){
                        Toast.makeText(requireActivity().getApplicationContext(), "Joining to a Network", Toast.LENGTH_SHORT).show();
                    }
                }

                Log.d("TAG", String.valueOf(app.deviceList.size()));
            }

        });

        discover_btn = view.findViewById(R.id.discover_service);
        NSDHelperDiscover.OnResolveCompleted parentObj = this;
        discover_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nsdHelperDiscover = new NSDHelperDiscover(requireActivity().getApplicationContext(), parentObj);
            }
        });

        return view;
    }

    @Override
    public void updateDeviceList() {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("TAG", "updating list");
                Log.d("TAG", String.valueOf(app.deviceList.size()));
                deviceListArrayAdapter = new DeviceListArrayAdapter(requireActivity().getApplicationContext(), app.deviceList);
                deviceListView.setAdapter(deviceListArrayAdapter);
            }
        });
    }

}