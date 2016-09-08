
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Mohit Mittal SID: 4677985
 */
public class Server {

    private ServerSocket ss = null;
    private Socket s = null;
    private ObjectInputStream objIS = null; // Streams definition for connection
    private ObjectOutputStream objOS = null;
    private Thread connThreads[];

    public static void main(String args[]) {
        new Server();
    }  // end of main method
    private int clientCount;
    private int connectionNum;

    public Server() {
        connThreads = new Thread[50];

        this.run();
    }  // end of ServerExample2 constructor

    public void run() {
        try {
            this.ss = new ServerSocket(2000);
            while (true) {
                this.s = ss.accept();
                this.clientCount++;
                System.out.println("Connection " + this.connectionNum
                        + " made: " + this.clientCount + " Clients connected");

                //Create and start thread to process client requests
                this.connThreads[this.connectionNum] = new Thread(new ThreadedConnect(s));
                this.connThreads[this.connectionNum].start();

                for (int i = 0; i < this.connectionNum; i++) {
                    if (!this.connThreads[i].isAlive()) {
                        this.connThreads[i] = null;
                        this.clientCount--;
                        System.out.println("Connection " + i + " dead: "
                                + this.clientCount + " Clients connected");
                    }  // end if
                }  // end for
                this.connectionNum++;
            }  // end while
        } catch (IOException e) {
            System.out.println("Trouble making a connection" + e);
        }
    }
}
