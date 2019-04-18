import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static java.lang.System.out;
class  ChatClient extends JFrame implements ActionListener {
    private final String userName;
    private PrintWriter pw;
    private BufferedReader br;
    private JTextArea  taMessages;
    private JTextField tfInput;
    private JButton btnExit;

    private ChatClient(String userName, String servername) throws Exception {
        super(userName);  // set title for frame
        this.userName = userName;
        Socket client = new Socket(servername, 9999);
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
        btnSend.addActionListener(this);
        btnExit.addActionListener(this);
        setSize(500,300);
        setVisible(true);
        pack();
    }

    public void actionPerformed(ActionEvent evt) {
        if ( evt.getSource() == btnExit ) {
            pw.println("end");  // send end to server so that server knows about the termination
            System.exit(0);
        } else {
            // send message to server
            taMessages.append(userName + ":" + tfInput.getText() + System.lineSeparator());
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
            } catch(Exception ignored) {}
        }
    }
} //  end of client