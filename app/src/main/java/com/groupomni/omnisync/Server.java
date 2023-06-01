package com.groupomni.omnisync;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable
{
    private Thread thread;
    private ServerSocket serverSocket;
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public Server()
    {
        this.thread = new Thread( this );
        this.thread.setPriority( Thread.NORM_PRIORITY );
        this.thread.start();
    }

    @Override
    public void run()
    {
        // create a server socket
        try
        {
            this.serverSocket = new ServerSocket( 8856 );
        }
        catch ( IOException e )
        {
            Log.e("TEST", "failed to start server socket" );
            e.printStackTrace();
        }

        // wait for a connection
        Log.d("TEST", "waiting for connections..." );
        try
        {
            this.socket = serverSocket.accept();
        }
        catch ( IOException e )
        {
            Log.e("TEST", "failed to accept" );
            e.printStackTrace();
        }
        Log.d("TEST", "client connected" );

        // create input and output streams
        try
        {
            this.dataInputStream = new DataInputStream( new BufferedInputStream( this.socket.getInputStream() ) );
            this.dataOutputStream = new DataOutputStream( new BufferedOutputStream( this.socket.getOutputStream() ) );
        }
        catch ( IOException e )
        {
            Log.e("TEST", "failed to create streams" );
            e.printStackTrace();
        }

        // send some test data
        try
        {
            this.dataOutputStream.writeInt( 123 );
            this.dataOutputStream.flush();
        }
        catch ( IOException e )
        {
            Log.e("TEST", "failed to send" );
            e.printStackTrace();
        }

        // placeholder recv loop
        Integer i = 0;
        while ( true )
        {
            Log.d("TEST", "i = "+ String.valueOf(i));
            i += 1;
            try
            {
                byte test = this.dataInputStream.readByte();
                Log.d("TEST", "byte received: "+test );

                Log.d("TEST", "test = "+ String.valueOf(test));
                if ( test == 42 ) break;

            }
            catch ( IOException e )
            {
                Log.e("TEST", e.toString());
                break;
            }
        }

        Log.d("TEST", "server thread stopped" );
    }
}
