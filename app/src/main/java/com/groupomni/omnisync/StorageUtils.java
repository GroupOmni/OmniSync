package com.groupomni.omnisync;

import android.os.Environment;
import android.os.StatFs;

public class StorageUtils {

    public static long getRemainingStorage() {
        String externalStorageDirectory = Environment.getExternalStorageDirectory().getPath();
        StatFs stat = new StatFs(externalStorageDirectory);
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }
}
