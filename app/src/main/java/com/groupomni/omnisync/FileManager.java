package com.groupomni.omnisync;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

public class FileManager {
    private Context context;

    public FileManager(Context context) {
        this.context = context;
    }

    public DocumentFile createDirectory(Uri directoryUri, String directoryName) {
        DocumentFile parentDirectory = DocumentFile.fromTreeUri(context, directoryUri);
        if (parentDirectory != null) {
            if(directoryName != null && directoryName != "") {
                DocumentFile directory = parentDirectory.createDirectory(directoryName);
                if (directory != null) {
                    Log.i("FTP FileManager", "Directory created: " + directory.getName());
                    Log.i("FTP FileManager", "Directory wanted to create : " + directoryName);
                    return directory;
                } else {
                    Log.e("FTP FileManager", "Failed to create directory: " + directoryName);
                    Log.e("FTP FileManager", "Directory wanted to create : " + directoryName);
                }
            }else{
                return parentDirectory;
            }
        } else {
            Log.e("FTP FileManager", "Invalid directory URI: " + directoryUri.toString());
        }
        return null;
    }

    public DocumentFile createFile(Uri directoryUri, String fileName, String mimeType) {
        DocumentFile parentDirectory = DocumentFile.fromTreeUri(context, directoryUri);
        if (parentDirectory != null) {
            DocumentFile file = parentDirectory.createFile(mimeType, fileName);
            if (file != null) {
                Log.i("FTP FileManager", "File created: " + file.getName());
                return file;
            } else {
                Log.e("FTP FileManager", "Failed to create file: " + directoryUri + ", " + fileName);
            }
        } else {
            Log.e("FTP FileManager", "Invalid directory URI: " + directoryUri.toString());
        }
        return null;
    }
}
