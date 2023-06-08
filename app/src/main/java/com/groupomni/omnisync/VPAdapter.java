package com.groupomni.omnisync;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class VPAdapter extends FragmentStateAdapter {


    public VPAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return  new DevicesFragment();
            case 1:
                return new FilesFragment();
            default:
                return  new DevicesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
