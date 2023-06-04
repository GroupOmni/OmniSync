package com.groupomni.omnisync;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    NSDHelperRegister nsdHelperRegister;
    NSDHelperDiscover nsdHelperDiscover;

    OmniSyncApplication app;

    TabLayout tabLayout;
    ViewPager2 viewPager2;
    VPAdapter vpAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app = (OmniSyncApplication) getApplicationContext();

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



//
    }

    @Override
    protected void onPause() {
//        if (nsdHelperRegister != null) {
//            nsdHelperRegister.tearDown();
//        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (nsdHelperRegister != null) {
//            nsdHelperRegister.registerService(nsdHelperRegister.getLocalPort());
//        }
    }

    @Override
    protected void onDestroy() {
        if (nsdHelperDiscover != null) {
            nsdHelperDiscover.tearDown();
        }
        super.onDestroy();
    }
}
