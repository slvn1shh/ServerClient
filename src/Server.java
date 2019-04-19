// Chat Server runs at port no. 9999
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.net.*;
import static java.lang.System.out;

class  ChatServer {
    //private final Vector<String> users = new Vector<>();
    private final ArrayList<HandleClient> clients = new ArrayList<>();
    private ServerSocket serverAddress;
    private void process() throws Exception  {
        InetAddress ip = InetAddress.getLocalHost();
        String serverName = (String) JOptionPane.showInputDialog(null,
                "Current local domain, ip: " + ip + System.lineSeparator() + "Enter server address: ",
                "Enter server ip", JOptionPane.QUESTION_MESSAGE, null, null, ip.getHostAddress()); // pane for start

        try { // checking if address is reachable
            serverAddress = new ServerSocket(9999, 10, InetAddress.getByName(serverName));
        } catch (Exception ex) { // if address is unreachable no way to continue executing..
            JOptionPane.showMessageDialog(null,
                    "An error occurred! (it may be a wrong ip address, which is unreachable.) Restart and try again!",
                    "Error!",JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        JOptionPane.showMessageDialog(null,
                "Server started successfully! Ip is: " + serverAddress.getInetAddress().getHostAddress());
        out.println("Server's ip: " + serverAddress.getInetAddress().getHostAddress());
        //noinspection InfiniteLoopStatement
        while(true) {
            Socket client = serverAddress.accept();
            HandleClient c = new HandleClient(client);
            out.println("Client with name: "+ c.getUserName() + " is connected!");
            clients.add(c);
            sendSingleServerMessage(c.getUserName(),"Welcome to chat server by vyacheslav_sharapov@nixsolutions.com!" +
                    System.lineSeparator() + "invite your friends, ip is: " + serverAddress.getInetAddress().getHostAddress());
            sendSingleMessage(c.getUserName(),c.getUserName() + " is connected! WELCOME!");
        }  // end of while
    }

    public static void main(String ... args) throws Exception {
        new ChatServer().process();
    } // end of main

    private void broadcast(String user, String message)  {
        // send message to all connected users
        for ( HandleClient c : clients )
            if ( ! c.getUserName().equals(user) )
                c.sendMessage(user,message);
    }

    private void sendSingleServerMessage(String userName, String message){
        for ( HandleClient c : clients )
            if (c.getUserName().equals(userName) )
                c.sendMessage("System", message);
    }

    private void sendSingleMessage(String userName, String message){
        for ( HandleClient c : clients )
            if (!c.getUserName().equals(userName) )
                c.sendMessage("System",message);
    }

    class  HandleClient extends Thread {
        String name;
        BufferedReader input;
        PrintWriter output;
        HandleClient(Socket client) throws Exception {
            // get input and output streams
            input = new BufferedReader( new InputStreamReader( client.getInputStream())) ;
            output = new PrintWriter ( client.getOutputStream(),true);
            // read name
            name  = input.readLine();
            //users.add(name); // add to vector
            start();
        }
        void sendMessage(String userName, String msg)  {
            output.println(userName + ": " + msg);
        }

        String getUserName() {
            return name;
        }
        public void run()  {
            String line;
            try    {
                while(true)   {
                    line = input.readLine();
                    if ( line.equals("end") ) {
                        clients.remove(this);
                        //users.remove(name);
                        break;
                    }
                    broadcast(name,line); // method  of outer class - send messages to all
                } // end of while
            } // try
            catch(Exception ex) {
                System.out.println(ex.getMessage());
            }
        } // end of run()
    } // end of inner class
} // end of Server