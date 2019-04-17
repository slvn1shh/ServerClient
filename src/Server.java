// Chat Server runs at port no. 9999
import java.io.*;
import java.util.*;
import java.net.*;
import static java.lang.System.out;

class  ChatServer {
    //private final Vector<String> users = new Vector<>();
    private final Vector<HandleClient> clients = new Vector<>();
    private void process() throws Exception  {
        ServerSocket server = new ServerSocket(9999,10);
        out.println("Server Started...");
        //noinspection InfiniteLoopStatement
        while(true) {
            Socket client = server.accept();
            HandleClient c = new HandleClient(client);
            clients.add(c);
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
        void sendMessage(String uname, String msg)  {
            output.println( uname + ":" + msg);
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