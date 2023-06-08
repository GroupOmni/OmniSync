package com.groupomni.omnisync;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.activity.result.ActivityResultLauncher;
import androidx.documentfile.provider.DocumentFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FileMangerUtils {

    private ActivityResultLauncher<Intent> folderSelectionLauncher;
    private Context context;

    public FileMangerUtils(Context context){
        this.context = context;
    }

    public boolean folderExists(Uri folderUri) {
        DocumentFile folder = DocumentFile.fromTreeUri(context, folderUri);
        if(folder == null){
            return false;
        }
        return folder.exists() && folder.isDirectory();
    }

    public HashMap<String, HashMap<String, Object>> scanFolder(Uri folderUri) {
        HashMap<String, HashMap<String, Object>> fileMap = new HashMap<>();

        DocumentFile folder = DocumentFile.fromTreeUri(context, folderUri);
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return fileMap;
        }

        scanFilesRecursively(folder, "", fileMap);

        return fileMap;
    }

    private void scanFilesRecursively(DocumentFile folder, String subfolderPath, HashMap<String, HashMap<String, Object>> fileMap) {
        DocumentFile[] files = folder.listFiles();
        for (DocumentFile file : files) {
            if (file.isDirectory()) {
                String currentSubfolderPath = subfolderPath + "/" + file.getName();
                scanFilesRecursively(file, currentSubfolderPath, fileMap);
            } else {
                String name = file.getName();
                String fullPath = subfolderPath + "/" + name;
                String md5 = calculateMD5(file);
                long modifiedTimestamp = file.lastModified();
                boolean isLocked = isFileWriteLocked(file);
                HashMap<String, Object> fileDetails = new HashMap<>();
                fileDetails.put("name", name);
                fileDetails.put("md5", md5);
                fileDetails.put("modifiedTimestamp", modifiedTimestamp);
                fileDetails.put("isLocked", isLocked);
                fileMap.put(fullPath, fileDetails);
            }
        }
    }

    private boolean isFileWriteLocked(DocumentFile documentFile) {
        Context context = getContextFromDocumentFile(documentFile);
        if (context == null) {
            return false;
        }

        ContentResolver contentResolver = context.getContentResolver();
        String authority = documentFile.getUri().getAuthority();

        if (authority != null) {
            ProviderInfo providerInfo = context.getPackageManager().resolveContentProvider(authority, PackageManager.GET_META_DATA);
            if (providerInfo != null && providerInfo.exported) {
                // Provider is exported, assume it is not write-locked
                return false;
            }
        }

        try {
            ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(documentFile.getUri(), "w");
            if (pfd != null) {
                pfd.close();
            } else {
                // Failed to open file descriptor, assume it is write-locked
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Error occurred, assume it is write-locked
            return true;
        }

        // File is not write-locked
        return false;
    }

    public boolean isFileLockedFromHashMap(HashMap<String, HashMap<String, Object>> fileMap, String filePath) {
        HashMap<String, Object> fileDetails = fileMap.get(filePath);
        if (fileDetails != null) {
            return (boolean) fileDetails.get("isLocked");
        }
        return false;
    }

    private Context getContextFromDocumentFile(DocumentFile documentFile) {
        Uri uri = documentFile.getUri();
        String packageName = uri.getAuthority();
        if (packageName == null) {
            return null;
        }
        try {
            return context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
            return null;
        }
    }

    private String calculateMD5(DocumentFile file) {
        try (InputStream is = context.getContentResolver().openInputStream(file.getUri())) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
            byte[] digest = md.digest();
            BigInteger bi = new BigInteger(1, digest);
            return String.format("%032x", bi);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String hashMapToJson(HashMap<String, HashMap<String, Object>> hashMap) {
        JSONObject jsonObject = new JSONObject();

        try {
            for (String key : hashMap.keySet()) {
                HashMap<String, Object> innerMap = hashMap.get(key);
                assert innerMap != null;
                JSONObject innerObject = new JSONObject(innerMap);
                jsonObject.put(key, innerObject);
            }
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public HashMap<String, HashMap<String, Object>> jsonToHashMap(String jsonString) {
        HashMap<String, HashMap<String, Object>> hashMap = new HashMap<>();

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            for (Iterator<String> keys = jsonObject.keys(); keys.hasNext(); ) {
                String key = keys.next();
                JSONObject innerObject = jsonObject.getJSONObject(key);
                HashMap<String, Object> innerMap = new HashMap<>();
                for (Iterator<String> innerKeys = innerObject.keys(); innerKeys.hasNext(); ) {
                    String innerKey = innerKeys.next();
                    Object value = innerObject.get(innerKey);
                    innerMap.put(innerKey, value);
                }
                hashMap.put(key, innerMap);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return hashMap;
    }

    public HashMap<String, HashMap<String, Object>> compareHashMaps(HashMap<String, HashMap<String, Object>> firstMap, HashMap<String, HashMap<String, Object>> secondMap) {
        HashMap<String, HashMap<String, Object>> diffMap = new HashMap<>();

        for (Map.Entry<String, HashMap<String, Object>> entry : secondMap.entrySet()) {
            String fileName = entry.getKey();
            HashMap<String, Object> fileDetails = entry.getValue();

            if (!firstMap.containsKey(fileName)) {
                // File exists only in the second map
                boolean isLocked = (boolean) fileDetails.get("isLocked");
                if (!isLocked) {
                    diffMap.put(fileName, fileDetails);
                }
            } else {
                // File exists in both maps
                HashMap<String, Object> firstFileDetails = firstMap.get(fileName);
                assert firstFileDetails != null;
                String firstMd5 = (String) firstFileDetails.get("md5");
                String secondMd5 = (String) fileDetails.get("md5");

                long firstTimeStamp = (long) firstFileDetails.get("modifiedTimestamp");
                long secondTimeStamp = (long) fileDetails.get("modifiedTimestamp");

                assert firstMd5 != null;
                if (!firstMd5.equals(secondMd5) && (firstTimeStamp < secondTimeStamp)) {
                    // File has the same MD5 hash
                    boolean isLocked = (boolean) fileDetails.get("isLocked");
                    if (!isLocked) {
                        diffMap.put(fileName, fileDetails);
                    }
                }
            }
        }

        return diffMap;
    }

}
