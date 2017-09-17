/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MyNetworkClasses;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dmalonas
 */
public class MyServer
{
    
    private void connectToClients()
    {
        System.out.println("Server: Server starting.\n\n");
        //Open socket to listen for connection InputStreams and OutputStreams
        try (ServerSocket serverSocket = new ServerSocket(2222)) {
            
            while (true)
            {
                System.out.println("Server: Waiting for connecting client...");
                //evaluate and establish thread connection
                try
                {
                    Socket socket = serverSocket.accept();
                    
                    MyClientHandler myClientHandler = new MyClientHandler(socket);
                    Thread connectionThread = new Thread(myClientHandler);
                    connectionThread.start();
                }
                catch (IOException ex)
                {
                    System.out.println("Server: Could not start connection to a client.");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MyServer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Server: Closed down");
        }
    }
    
    public static void main(String args[]) {
        //create a server
        MyServer myServer = new MyServer();
        //establish an active sccket 
        myServer.connectToClients();
    }
}
