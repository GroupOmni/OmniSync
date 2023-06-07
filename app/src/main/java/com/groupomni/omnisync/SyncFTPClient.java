package com.groupomni.omnisync;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SyncFTPClient {

    private String username;
    private String password;
    private FTPClient ftpClient;

    public SyncFTPClient(String username, String password) {
        this.username = username;
        this.password = password;
        this.ftpClient = new FTPClient();
    }

    public void connectAndDownloadFile(String host, int port, String remoteFilePath, String localFolderPath) {
        try {
            // Connect to the FTP server
            ftpClient.connect(host, port);

            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return;
            }

            // Login to the FTP server
            if (ftpClient.login(username, password)) {
                // Set binary mode for file transfer
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                // Enter passive mode (optional)
                ftpClient.enterLocalPassiveMode();

                // Get the file name from the remote file path
                String fileName = remoteFilePath.substring(remoteFilePath.lastIndexOf("/") + 1);

                // Set the local file path
                String localFilePath = localFolderPath + "/" + fileName;

                // Check if the local file already exists
                File localFile = new File(localFilePath);
                if (localFile.exists()) {
                    // Delete the existing local file
                    if (!localFile.delete()) {
                        System.out.println("Failed to delete existing file: " + localFilePath);
                        return;
                    }
                }

                // Download the file
                OutputStream outputStream = Files.newOutputStream(Paths.get(localFilePath));
                ftpClient.retrieveFile(remoteFilePath, outputStream);
                outputStream.close();

                System.out.println("File downloaded successfully.");
            } else {
                System.out.println("Failed to login to the FTP server.");
            }

            // Logout and disconnect from the FTP server
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
