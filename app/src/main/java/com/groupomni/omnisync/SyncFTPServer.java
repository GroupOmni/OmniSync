package com.groupomni.omnisync;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletContext;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncFTPServer {

    private OmniSyncApplication app;
    public boolean isRunning;

    public SyncFTPServer(Context context) {
        app = (OmniSyncApplication) context;
        isRunning = false;
    }

    public void startServer() {
        isRunning = true;
        FtpServerFactory serverFactory = new FtpServerFactory();

        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(8085);

        serverFactory.addListener("default", listenerFactory.createListener());

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String userPropertiesPath = externalStoragePath + "/.OmniSync/user.properties";
        userManagerFactory.setFile(new File(userPropertiesPath));

        File userPropertiesFile = new File(userPropertiesPath);
        if (!userPropertiesFile.exists()) {
            // Create the parent directory if it doesn't exist
            File parentDir = userPropertiesFile.getParentFile();
            assert parentDir != null;
            if (!parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    System.out.println("Parent directory created: " + parentDir.getAbsolutePath());
                } else {
                    System.err.println("Failed to create parent directory: " + parentDir.getAbsolutePath());
                    // Handle the error accordingly
                }
            }

            try {
                // Create the user.properties file
                if (userPropertiesFile.createNewFile()) {
                    System.out.println("user.properties file created: " + userPropertiesFile.getAbsolutePath());
                } else {
                    System.err.println("Failed to create user.properties file: " + userPropertiesFile.getAbsolutePath());
                    // Handle the error accordingly
                }
            } catch (IOException e) {
                System.err.println("Failed to create user.properties file: " + e.getMessage());
                // Handle the exception accordingly
            }
        }

        Log.d("FTP SERVER", userPropertiesPath);


        BaseUser user = new BaseUser();
        user.setName("omnisync");
        user.setPassword("omnisync");
        String home = app.syncFolder.split(":")[2];
        String FTPHome = externalStoragePath + "/" + home;
        Log.d("FTP SERVER", "Home directory : " + FTPHome);
        user.setHomeDirectory(FTPHome);

        List<Authority> authorities = new ArrayList<Authority>();
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);
        UserManager um = userManagerFactory.createUserManager();

//        AuthorizationRequest writeRequest = new WriteRequest();

//        user.authorize(writeRequest);

        try
        {
            um.save(user);//Save the user to the user list on the filesystem
        }
        catch (FtpException e1)
        {
            //Deal with exception as you need
        }
        serverFactory.setUserManager(um);
        Map<String, Ftplet> m = new HashMap<String, Ftplet>();
        m.put("miaFtplet", new Ftplet()
        {

            @Override
            public void init(FtpletContext ftpletContext) throws FtpException {
                //System.out.println("init");
                //System.out.println("Thread #" + Thread.currentThread().getId());
            }

            @Override
            public void destroy() {
                //System.out.println("destroy");
                //System.out.println("Thread #" + Thread.currentThread().getId());
            }

            @Override
            public FtpletResult beforeCommand(FtpSession session, FtpRequest request) throws FtpException, IOException
            {
                //System.out.println("beforeCommand " + session.getUserArgument() + " : " + session.toString() + " | " + request.getArgument() + " : " + request.getCommand() + " : " + request.getRequestLine());
                //System.out.println("Thread #" + Thread.currentThread().getId());

                //do something
                return FtpletResult.DEFAULT;//...or return accordingly
            }

            @Override
            public FtpletResult afterCommand(FtpSession session, FtpRequest request, FtpReply reply) throws FtpException, IOException
            {
                //System.out.println("afterCommand " + session.getUserArgument() + " : " + session.toString() + " | " + request.getArgument() + " : " + request.getCommand() + " : " + request.getRequestLine() + " | " + reply.getMessage() + " : " + reply.toString());
                //System.out.println("Thread #" + Thread.currentThread().getId());

                //do something
                return FtpletResult.DEFAULT;//...or return accordingly
            }

            @Override
            public FtpletResult onConnect(FtpSession session) throws FtpException, IOException
            {
                //System.out.println("onConnect " + session.getUserArgument() + " : " + session.toString());
                //System.out.println("Thread #" + Thread.currentThread().getId());

                //do something
                return FtpletResult.DEFAULT;//...or return accordingly
            }

            @Override
            public FtpletResult onDisconnect(FtpSession session) throws FtpException, IOException
            {
                //System.out.println("onDisconnect " + session.getUserArgument() + " : " + session.toString());
                //System.out.println("Thread #" + Thread.currentThread().getId());

                //do something
                return FtpletResult.DEFAULT;//...or return accordingly
            }
        });
        serverFactory.setFtplets(m);
        FtpServer server = serverFactory.createServer();

        try {
            // Start the server
            server.start();

            Log.i("FTP SERVER","FTP server started." + server.isStopped());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
