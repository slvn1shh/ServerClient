package com.serverclientapp

// Chat Server runs at port no. 9999

import javax.swing.*
import java.awt.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.ArrayList

internal class ChatServer private constructor() : JFrame() {
    private val clients = ArrayList<HandleClient>()
    private var serverAddress: ServerSocket? = null
    private var serverLog: JTextArea? = null
    private var countUsers: Int = 0
    private var connectedCount: JTextField? = null
    private var scrollableLog: JScrollPane? = null

    init {
        countUsers = 0
    }

    @Throws(Exception::class)
    private fun process() {
        val ip = InetAddress.getLocalHost()
        val serverName = JOptionPane.showInputDialog(null,
                "Current local domain, ip: " + ip + System.lineSeparator() + "Enter server address: ",
                "Enter server ip", JOptionPane.QUESTION_MESSAGE, null, null, ip.hostAddress) as String // pane for start

        try { // checking if address is reachable
            serverAddress = ServerSocket(9999, 10, InetAddress.getByName(serverName))
        } catch (ex: Exception) { // if address is unreachable no way to continue executing..
            JOptionPane.showMessageDialog(null,
                    "An error occurred! (it may be a wrong ip address, which is unreachable.) Restart and try again!",
                    "Error!", JOptionPane.ERROR_MESSAGE)
        }

        JOptionPane.showMessageDialog(null,
                "Server started successfully! Ip is: " + serverAddress!!.inetAddress.hostAddress)
        println("Server's ip: " + serverAddress!!.inetAddress.hostAddress)
        // UI_draw
        buildInterface()

        while (true) {
            val client = serverAddress!!.accept()
            val c = HandleClient(client)
            if (isUserExist(c.userName)) {
                clients.add(c) // to users list
                sendSingleServerMessage(c.userName, client)
                clients.remove(c)
                client.close()
                continue
            }
            clients.add(c)
            println("Client with name: " + c.userName + " is connected!")
            addUser(c) // to server UI
            sendSingleServerMessage(c.userName, "Welcome to chat server!"/*by vyacheslav_sharapov@nixsolutions.com*/ +
                    System.lineSeparator() + "invite your friends, ip is: " + serverAddress!!.inetAddress.hostAddress)
            sendSingleMessage(c.userName, c.userName + " is connected! WELCOME!")
        }  // end of while
    }

    private fun buildInterface() {
        val welcomeMessage = JLabel("Center")
        welcomeMessage.text = "Welcome to the chat server made by VS from NIX!"
        welcomeMessage.font = Font("Times New Roman", Font.BOLD, 24)
        welcomeMessage.border = BorderFactory.createLineBorder(Color.CYAN, 2, true)
        add(welcomeMessage, BorderLayout.NORTH)

        val UI_ip = JPanel(FlowLayout())
        val serverIPMessage = JLabel()

        serverIPMessage.text = "Server's ip is: "
        val serverIP = JTextField()
        serverIP.text = serverAddress!!.inetAddress.hostAddress
        serverIP.isEditable = false

        val countConnected = JLabel()
        countConnected.text = "Count of connected users: "

        connectedCount = JTextField()
        connectedCount!!.text = countUsers.toString()
        connectedCount!!.isEditable = false
        val connectedUI = JPanel(FlowLayout())
        connectedUI.add(countConnected)
        connectedUI.add(connectedCount)

        UI_ip.add(serverIPMessage)
        UI_ip.add(serverIP)
        val ui = JPanel()
        ui.add(UI_ip)
        ui.add(connectedUI)
        add(ui, BorderLayout.WEST)

        val serverLogTip = JLabel()
        serverLogTip.text = "Server log:"
        add(serverLogTip, BorderLayout.AFTER_LINE_ENDS)

        serverLog = JTextArea()
        serverLog!!.rows = 10
        serverLog!!.columns = 50
        serverLog!!.isEditable = false
        scrollableLog = JScrollPane(serverLog, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        add(scrollableLog!!, BorderLayout.SOUTH)
        pack()
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        isAlwaysOnTop = true
        isResizable = false
        isVisible = true
    }

    private fun addUser(user: HandleClient) {
        countUsers++
        connectedCount!!.text = countUsers.toString()
        serverLog!!.append("(" + DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + ") User " + user.userName + " is connected!\n")
        scrollableLog!!.verticalScrollBar.value = scrollableLog!!.verticalScrollBar.maximum
    }

    private fun broadcast(user: String, message: String) {
        // send message to all connected users
        for (c in clients)
            if (c.userName != user)
                c.sendMessage(user, message)
    }

    private fun sendSingleServerMessage(userName: String, message: String) {
        for (c in clients)
            if (c.userName == userName)
                c.sendMessage("System", message)
    }

    private fun sendSingleServerMessage(userName: String, client: Socket) {
        for (c in clients)
            if (c.userName == userName)
                if (c.userSocket == client)
                    c.sendMessage("System", "db_usernameIsTaken")
    }

    private fun sendSingleMessage(userName: String, message: String) {
        for (c in clients)
            if (c.userName != userName)
                c.sendMessage("System", message)
    }

    private fun isUserExist(userName: String): Boolean {
        for (c in clients)
            if (c.userName == userName) return true
        return false
    }

    internal inner class HandleClient @Throws(Exception::class)
    constructor(val userSocket: Socket) : Thread() {
        lateinit var userName: String
        private lateinit var input: BufferedReader
        private lateinit var output: PrintWriter

        init {
            sendMessageBySocket(userSocket)
        }

        @Throws(Exception::class)
        fun sendMessageBySocket(user: Socket) {
            // get input and output streams
            input = BufferedReader(InputStreamReader(user.getInputStream()))
            output = PrintWriter(user.getOutputStream(), true)
            // read name
            userName = input.readLine()
            start()
        }

        fun sendMessage(userName: String, msg: String) {
            output.println("$userName: $msg")
        }

        override fun run() {
            var line: String
            try {
                while (true) {
                    line = input.readLine()
                    if (line == "end") {
                        serverLog!!.append("(" + DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + ") User " + userName + " is disconnected!\n")
                        countUsers--
                        connectedCount!!.text = countUsers.toString()
                        clients.remove(this)
                        break
                    }
                    broadcast(userName, line) // method  of outer class - send messages to all
                } // end of while
            } // try
            catch (ex: Exception) {
                println(ex.message)
            }

        } // end of run()
    } // end of inner class

    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            ChatServer().process()
        } // end of main
    }
} // end of Server