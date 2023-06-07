package com.groupomni.omnisync;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.command.Command;
import org.apache.ftpserver.command.CommandFactory;
import org.apache.ftpserver.command.CommandFactoryFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletContext;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.listener.ListenerFactory;

import org.apache.ftpserver.command.impl.STOR;
import org.apache.ftpserver.command.impl.RETR;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WriteRequest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncFTPServer {

    private OmniSyncApplication app;

    public SyncFTPServer(Context context) {
        app = (OmniSyncApplication) context;
    }

    public void startServer() {
        FtpServerFactory serverFactory = new FtpServerFactory();

        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(8085);

        serverFactory.addListener("default", listenerFactory.createListener());

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String userPropertiesPath = externalStoragePath + "/OmniSync/user.properties";
        userManagerFactory.setFile(new File(userPropertiesPath));

        Log.d("FTP SERVER", userPropertiesPath);


        BaseUser user = new BaseUser();
        user.setName("omnisync");
        user.setPassword("omnisync");
        user.setHomeDirectory(externalStoragePath + "/OmniSync");

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