package com.groupomni.omnisync;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.OutputStream;

public class SyncFTPClient {

    private String username;
    private String password;
    private FTPClient ftpClient;
    private FileManager fileManager;

    public SyncFTPClient(String username, String password) {
        this.username = username;
        this.password = password;
        this.ftpClient = new FTPClient();
        this.fileManager = new FileManager(OmniSyncApplication.appContext);
    }

    public void connectAndDownloadFile(String host, int port, String remoteFilePath, String localFolderPath) {
        new FTPConnectTask().execute(host, String.valueOf(port), remoteFilePath, localFolderPath);
    }

    private class FTPConnectTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String host = params[0];
            int port = Integer.parseInt(params[1]);
            String remoteFilePath = params[2];
            String localFolderPath = params[3];

            try {
                ftpClient.connect(host, port);

                int reply = ftpClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftpClient.disconnect();
                    return false;
                }

                if (ftpClient.login(username, password)) {
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                    ftpClient.enterLocalPassiveMode();

                    String fileName = remoteFilePath.substring(remoteFilePath.lastIndexOf("/") + 1);

                    String localFilePath = localFolderPath + "/" + fileName;

                    Log.d("DIRECTORY FTP", "Folder path and file name : " + localFolderPath + ", "+ fileName);

                    DocumentFile localFile = fileManager.createFile(Uri.parse(localFolderPath), fileName, "application/octet-stream");
                    if (localFile != null && localFile.exists()) {
                        Log.i("DIRECTORY FTP", "Local file exists and is not null : " + localFile.getUri());

                    }else{
                        Log.i("DIRECTORY FTP", "Local file is null");
                    }

                    // Create the local directory if it doesn't exist
                    DocumentFile localFolder = fileManager.createDirectory(Uri.parse(localFolderPath), "");
                    if (localFolder == null) {
                        Log.e("FTP CLIENT", "Failed to create local folder: " + localFolderPath);
                        return false;
                    }

                    // Download the file
                    try {
                        assert localFile != null;
                        Log.d("DIRECTORY FTP", String.valueOf(localFile.getUri()));
                        Log.d("DIRECTORY FTP", Uri.decode(String.valueOf(localFile.getUri())));
                        try (OutputStream outputStream = OmniSyncApplication.appContext.getContentResolver().openOutputStream(localFile.getUri())) {
                            if (outputStream != null) {
                                Log.d("FTP CLIENT", "Remote file path : " + remoteFilePath);
                                boolean downSuccess = ftpClient.retrieveFile(remoteFilePath, outputStream);
                                outputStream.close();
                                if(downSuccess){
                                    Log.d("FTP CLIENT", "File download SUCCESSFUL");
                                }else{
                                    Log.d("FTP CLIENT", "File download FAILED");
                                }
                                Log.i("FTP CLIENT", "File downloaded successfully.");
                                return true;
                            } else {
                                Log.e("FTP CLIENT", "Failed to open output stream for file: " + localFilePath);
                                return false;
                            }
                        }
                    } catch (IOException e) {
                        Log.e("FTP CLIENT", "Failed to download file: " + e.getMessage());
                        return false;
                    }
                } else {
                    Log.e("FTP CLIENT", "Failed to login to the FTP server.");
                }

                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
        }
    }

}
