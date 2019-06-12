package com.serverclientapp;// Chat Server runs at port no. 9999

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static java.lang.System.out;

class ChatServer extends JFrame{
    private final ArrayList<HandleClient> clients = new ArrayList<>();
    private ServerSocket serverAddress;
    private JTextArea serverLog;
    private int countUsers;
    private JTextField connectedCount;
    private JScrollPane scrollableLog;
    private ChatServer(){
        countUsers = 0;
    }
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
        }
        JOptionPane.showMessageDialog(null,
                "Server started successfully! Ip is: " + serverAddress.getInetAddress().getHostAddress());
        out.println("Server's ip: " + serverAddress.getInetAddress().getHostAddress());
        // UI_draw
        buildInterface();
        //noinspection InfiniteLoopStatement
        while(true) {
            Socket client = serverAddress.accept();
            HandleClient c = new HandleClient(client);
            if (isUserExist(c.getUserName())) {
                clients.add(c); // to users list
                sendSingleServerMessage(c.getUserName(), client);
                clients.remove(c);
                client.close();
                continue;
            }
            clients.add(c);
            out.println("Client with name: " + c.getUserName() + " is connected!");
            addUser(c); // to server UI
            sendSingleServerMessage(c.getUserName(), "Welcome to chat server!"/*by vyacheslav_sharapov@nixsolutions.com*/ +
                    System.lineSeparator() + "invite your friends, ip is: " + serverAddress.getInetAddress().getHostAddress());
            sendSingleMessage(c.getUserName(), c.getUserName() + " is connected! WELCOME!");
        }  // end of while
    }
    private void buildInterface(){
        JLabel welcomeMessage = new JLabel("Center");
        welcomeMessage.setText("Welcome to the chat server made by VS from NIX!");
        welcomeMessage.setFont(new Font("Times New Roman",Font.BOLD,24));
        welcomeMessage.setBorder(BorderFactory.createLineBorder(Color.CYAN,2,true));
        add(welcomeMessage,BorderLayout.NORTH);

        JPanel UI_ip = new JPanel(new FlowLayout());
        JLabel serverIPMessage = new JLabel();

        serverIPMessage.setText("Server's ip is: ");
        JTextField serverIP = new JTextField();
        serverIP.setText(serverAddress.getInetAddress().getHostAddress());
        serverIP.setEditable(false);

        JLabel countConnected = new JLabel();
        countConnected.setText("Count of connected users: ");

        connectedCount = new JTextField();
        connectedCount.setText(Integer.toString(countUsers));
        connectedCount.setEditable(false);
        JPanel UI_connected = new JPanel(new FlowLayout());
        UI_connected.add(countConnected);
        UI_connected.add(connectedCount);

        UI_ip.add(serverIPMessage);
        UI_ip.add(serverIP);
        JPanel UI = new JPanel();
        UI.add(UI_ip);
        UI.add(UI_connected);
        add(UI, BorderLayout.WEST);

        JLabel serverLogTip = new JLabel();
        serverLogTip.setText("Server log:");
        add(serverLogTip,BorderLayout.AFTER_LINE_ENDS);

        serverLog = new JTextArea();
        serverLog.setRows(10);
        serverLog.setColumns(50);
        serverLog.setEditable(false);
        scrollableLog = new JScrollPane(serverLog, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollableLog,BorderLayout.SOUTH);
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setResizable(false);
        setVisible(true);
    }

    private void addUser(HandleClient user){
        countUsers++;
        connectedCount.setText(String.valueOf(countUsers));
        serverLog.append("(" + DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + ") User " + user.name + " is connected!\n");
        scrollableLog.getVerticalScrollBar().setValue(scrollableLog.getVerticalScrollBar().getMaximum());
    }
    public static void main(String ... args) throws Exception {
        new ChatServer().process();
    } // end of main

    private void broadcast(String user, String message)  {
        // send message to all connected users
        for ( HandleClient c : clients )
            if (!c.getUserName().equals(user))
                c.sendMessage(user,message);
    }

    private void sendSingleServerMessage(String userName, String message){
        for ( HandleClient c : clients )
            if (c.getUserName().equals(userName) )
                c.sendMessage("System", message);
    }

    private void sendSingleServerMessage(String userName, Socket client){
        for ( HandleClient c : clients )
            if (c.getUserName().equals(userName) )
                if(c.userSocket.equals(client))
                c.sendMessage("System", "db_usernameIsTaken");
    }

    private void sendSingleMessage(String userName, String message){
        for ( HandleClient c : clients )
            if (!c.getUserName().equals(userName) )
                c.sendMessage("System", message);
    }
    private boolean isUserExist(String userName){
        for ( HandleClient c : clients )
            if(c.getUserName().equals(userName)) return true;
        return false;
    }
    class  HandleClient extends Thread {
        String name;
        BufferedReader input;
        PrintWriter output;
        final Socket userSocket;
        HandleClient(Socket client) throws Exception {
            userSocket = client;
            sendMessageBySocket(client);
        }
        void sendMessageBySocket(Socket user) throws Exception{
            // get input and output streams
            input = new BufferedReader( new InputStreamReader( user.getInputStream()));
            output = new PrintWriter ( user.getOutputStream(),true);
            // read name
            name  = input.readLine();
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
            try   {
                while(true){
                    line = input.readLine();
                    if (line.equals("end") ) {
                        serverLog.append("(" + DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + ") User " + getUserName() + " is disconnected!\n");
                        countUsers--;
                        connectedCount.setText(String.valueOf(countUsers));
                        clients.remove(this);
                        //users.remove(name);
                        break;
                    }
                    broadcast(name,line); // method  of outer class - send messages to all
                } // end of while
            } // try
            catch(Exception ex) {
                System.out.println(ex.getMessage());
                System.exit(0);
            }
        } // end of run()
    } // end of inner class
} // end of Server