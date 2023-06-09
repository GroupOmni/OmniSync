package com.groupomni.omnisync;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    String[] permissions = {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, MANAGE_EXTERNAL_STORAGE};

    OmniSyncApplication app;

    TabLayout tabLayout;
    ViewPager2 viewPager2;
    VPAdapter vpAdapter;

    private HTTPServer httpServer;
    private HTTPClient httpClient;

    private NSDHelperRegister nsdHelperRegister;
    private NSDHelperDiscover nsdHelperDiscover;

    private PeerManagerUtils peerManagerUtils;

    private String selfIP;

    private BackgroundService backgroundService;
    private boolean isServiceBound;

    private final int STORAGE_PERMISSION_CODE = 1;

    private ActivityResultLauncher<Intent> folderSelectLauncher;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor prefEditor;

    private String syncFolder;
    private FileMangerUtils fileManger;


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BackgroundService.LocalBinder binder = (BackgroundService.LocalBinder) iBinder;
            backgroundService = binder.getService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (OmniSyncApplication) getApplicationContext();

        sharedPreferences = getSharedPreferences("OmniSyncPref", Context.MODE_PRIVATE);
        prefEditor  = sharedPreferences.edit();

        if (ContextCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { WRITE_EXTERNAL_STORAGE }, 100);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { READ_EXTERNAL_STORAGE }, 101);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if(!Environment.isExternalStorageManager()){
                try{
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                    startActivityIfNeeded(intent,102);
                }catch (Exception e){
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityIfNeeded(intent,102);
                }
            }
        }

        fileManger = new FileMangerUtils(getApplicationContext());



        app.fileMangerUtils = fileManger;

        folderSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode()== Activity.RESULT_OK){
                    Intent intent = result.getData();
                    Uri uri = intent.getData();
                    String decodedUri = Uri.decode(String.valueOf(uri));
                    prefEditor.putString("syncFolder", decodedUri);
                    syncFolder = decodedUri;
                    app.syncFolder = decodedUri;
                    prefEditor.apply();
                }
            }
        });

        syncFolder = sharedPreferences.getString("syncFolder",null);
        Uri syncFolderUri;
        Log.d("DIRECTORY", "Sync folder URI : " + syncFolder);


        if(syncFolder != null){
            syncFolderUri = Uri.parse(syncFolder);
            if (!fileManger.folderExists(syncFolderUri)) {
                selectSyncFolderDialog();
            }
        }else {
            selectSyncFolderDialog();
        }



        Log.d("DIRECTORY", "The folder is : " + syncFolder);

        if(syncFolder != null) {
            syncFolderUri = Uri.parse(syncFolder);
            Log.d("DIRECTORY", "Sync folder path : " + syncFolderUri.getPath());
            app.syncFolder = syncFolder;

            Log.d("DIRECTORY", fileManger.hashMapToJson(fileManger.scanFolder(syncFolderUri)));
        }
        setContentView(R.layout.activity_main);

        Intent serviceIntent = new Intent(this, BackgroundService.class);

        createNotificationChannel();
        startForegroundService(serviceIntent);

        selfIP = NetworkUtils.getIPAddress();
        app.selfIP = selfIP;

        Log.i("NETWORK", selfIP);

        httpServer = new HTTPServer(8080, getApplicationContext());
        httpClient = new HTTPClient(getApplicationContext());

        nsdHelperDiscover = new NSDHelperDiscover(getApplicationContext());
        app.nsdHelperDiscover = nsdHelperDiscover;

        peerManagerUtils = new PeerManagerUtils(getApplicationContext());

        app.httpServer = httpServer;
        app.httpClient = httpClient;

        app.peerManagerUtils = peerManagerUtils;

        OmniSyncApplication.appContext = this;

        if(app.deviceList == null){
            app.deviceList = new ArrayList<>();
        }

        try {
            httpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        tabLayout = findViewById(R.id.tabLayout2);
        viewPager2 = findViewById(R.id.ViewPager);
        vpAdapter = new VPAdapter(this);

        viewPager2.setAdapter(vpAdapter);

        if (nsdHelperRegister == null) {
            Log.d("TAG", "nsdHelperRegister is NULL");
            app.registration_state = "not";
            if (app.nsdHelperRegister == null){
                nsdHelperRegister = new NSDHelperRegister(getApplicationContext());
                app.nsdHelperRegister = nsdHelperRegister;
            }
            else {
                nsdHelperRegister = app.nsdHelperRegister;
            }
        }
        else {
            if(Objects.equals(app.registration_state, "done")) {
                Toast.makeText(getApplicationContext(), "Already Joined a Network", Toast.LENGTH_SHORT).show();
            }
            else if(Objects.equals(app.registration_state, "being")){
                Toast.makeText(getApplicationContext(), "Joining to a Network", Toast.LENGTH_SHORT).show();
            }
        }

        tabLayout.addOnTabSelectedListener((new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        }));

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });

        getBandwidth();

        SyncFTPServer ftpServer = new SyncFTPServer(getApplicationContext());

        app.ftpServer = ftpServer;

        SyncFTPClient ftpClient = new SyncFTPClient("omnisync", "omnisync");
        app.ftpClient = ftpClient;
    }

    private void selectSyncFolder(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        folderSelectLauncher.launch(intent);
    }

    private void selectSyncFolderDialog(){
        selectSyncFolder();
    }

    private void getBandwidth() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        if (capabilities != null) {
            int linkDownstreamBandwidthKbps = capabilities.getLinkDownstreamBandwidthKbps();
            int linkUpstreamBandwidthKbps = capabilities.getLinkUpstreamBandwidthKbps();

            app.upstreamBandwidth = linkUpstreamBandwidthKbps;
            app.downstreamBandwidth = linkDownstreamBandwidthKbps;

            String bandwidthText = "Downstream Bandwidth: " + linkDownstreamBandwidthKbps + " Kbps\n"
                    + "Upstream Bandwidth: " + linkUpstreamBandwidthKbps + " Kbps";

            Log.d("BANDWIDTH", bandwidthText);
        }
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel("My Notification", "Foreground Service", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
