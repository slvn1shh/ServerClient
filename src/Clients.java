import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class  ChatClient extends JFrame implements ActionListener {
    private final String userName;
    private PrintWriter pw;
    private BufferedReader br;
    private JTextArea  taMessages;
    private JTextField tfInput;
    private JButton btnExit;
    private ChatClient(String userName, String serverName) throws Exception {
        super(userName);  // set title for frame
        this.userName = userName;
        Socket client = new Socket(serverName, 9999);
        br = new BufferedReader( new InputStreamReader( client.getInputStream()) ) ;
        pw = new PrintWriter(client.getOutputStream(),true);
        pw.println(userName);  // send name to server
        buildInterface();
        new MessagesThread().start();  // create thread to listen for messages
    }

    private void buildInterface() {
        JButton btnSend = new JButton("Send");
        btnExit = new JButton("Exit");
        taMessages = new JTextArea();
        taMessages.setRows(10);
        taMessages.setColumns(50);
        taMessages.setEditable(false);
        tfInput  = new JTextField(50);
        JScrollPane sp = new JScrollPane(taMessages, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(sp,"Center");
        JPanel bp = new JPanel( new FlowLayout());
        bp.add(tfInput);
        bp.add(btnSend);
        bp.add(btnExit);
        add(bp,"South");
        tfInput.addActionListener(this);
        btnSend.addActionListener(this);
        btnExit.addActionListener(this);
        setSize(500,300);
        setVisible(true);
        tfInput.requestFocus();
        pack();
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == btnExit) {
            pw.println("end");  // send end to server so that server knows about the termination
            System.exit(0);
        }
        if (evt.getSource().equals(KeyEvent.VK_ENTER)){
            //taMessages.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            taMessages.append(userName + ": " + tfInput.getText() + System.lineSeparator());
            pw.println(tfInput.getText());
            tfInput.setText("");
        } else {
            // send message to server
            //taMessages.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            taMessages.append(userName + ": " + tfInput.getText() + System.lineSeparator());
            pw.println(tfInput.getText());
            tfInput.setText("");
        }
    }

    public static void main(String ... args) {

        // take username from user

        String name = JOptionPane.showInputDialog(null,"Enter your name :",
                "Username", JOptionPane.PLAIN_MESSAGE);
        String serverName = JOptionPane.showInputDialog(null,"Enter server address: ",
                "Server", JOptionPane.WARNING_MESSAGE);
        try {
            new ChatClient(name ,serverName);
        } catch(Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "An error occurred! (it may be a wrong ip address, which is unreachable.) Restart and try again!",
                    "Error!",JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

    } // end of main

    // inner class for Messages Thread
    class  MessagesThread extends Thread {
        public void run() {
            String line;
            try {
                //noinspection InfiniteLoopStatement
                while(true) {
                    line = br.readLine();
                    taMessages.append(line + "\n");
                } // end of while
            } catch(Exception ignored) {
                JOptionPane.showMessageDialog(null,"Server shutdown! Thanks for using.");
                System.exit(0);
            }
        }
    }
} //  end of client