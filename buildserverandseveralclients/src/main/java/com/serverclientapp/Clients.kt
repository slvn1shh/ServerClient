package com.serverclientapp

import javax.swing.*
import java.awt.*
import java.awt.event.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import java.rmi.UnexpectedException
import kotlin.system.exitProcess

internal class ChatClient @Throws(Exception::class)
private constructor(private val userName: String, serverName: String) : JFrame("MsgSender - $userName"), ActionListener {
    private val pw: PrintWriter
    private val br: BufferedReader
    private var taMessages: JTextArea? = null
    private var tfInput: JTextField? = null
    private var btnExit: JButton? = null

    init {
        val client = Socket(serverName, 9999)
        br = BufferedReader(InputStreamReader(client.getInputStream()))
        pw = PrintWriter(client.getOutputStream(), true)
        pw.println(userName)  // send name to server
        buildInterface()
        minimumSize = Dimension(width, height) //prevent unusual resizing
        MessagesThread().start()  // create thread to listen for messages
    }// set title for frame

    private fun buildInterface() {
        val btnSend = JButton("Send")
        btnExit = JButton("Exit")
        taMessages = JTextArea()
        taMessages!!.rows = 10
        taMessages!!.columns = 50
        taMessages!!.isEditable = false
        tfInput = JTextField(50)
        val sp = JScrollPane(taMessages, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        add(sp, "Center")
        val bp = JPanel(FlowLayout())
        bp.add(tfInput)
        bp.add(btnSend)
        bp.add(btnExit)
        add(bp, "South")
        tfInput!!.addActionListener(this)
        btnSend.addActionListener(this)
        btnExit!!.addActionListener(this)
        tfInput!!.requestFocus()

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                pw.println("end")
                e!!.window.dispose()
            }
        })

        isVisible = true
        isAlwaysOnTop = true

        pack()
    }

    override fun actionPerformed(evt: ActionEvent) {
        if (evt.source === btnExit) {
            pw.println("end")  // send end to server so that server knows about the termination
            exitProcess(0)
        }
        if (evt.source == KeyEvent.VK_ENTER) {
            //taMessages.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            taMessages!!.append(userName + ": " + tfInput!!.text + System.lineSeparator())
            pw.println(tfInput!!.text)
            tfInput!!.text = ""
        } else {
            // send message to server
            //taMessages.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            taMessages!!.append(userName + ": " + tfInput!!.text + System.lineSeparator())
            pw.println(tfInput!!.text)
            tfInput!!.text = ""
        }
    }

    // inner class for Messages Thread
    internal inner class MessagesThread : Thread() {
        override fun run() {
            var line: String
            try {

                while (true) {
                    line = br.readLine()
                    if (line.contains("db_"))
                        throw UnexpectedException("Username is taken! Try another one")
                    taMessages!!.append(line + "\n")
                } // end of while
            } catch (ex: UnexpectedException) {
                JOptionPane.showConfirmDialog(null, "Username is already taken! Restart the program and try again!", "Error!",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null)
                exitProcess(-1)
            } catch (ignored: Exception) {
                JOptionPane.showMessageDialog(null, "Server shutdown! Thanks for using.")
                exitProcess(0)
            }

        }
    }

    companion object {

        @Throws(UnknownHostException::class)
        @JvmStatic
        fun main(args: Array<String>) {

            // take username from user

            val name = JOptionPane.showInputDialog(null, "Enter your name :",
                    "Username", JOptionPane.PLAIN_MESSAGE)
            val serverName = JOptionPane.showInputDialog(null, "Enter server address: ",
                    "Server", JOptionPane.WARNING_MESSAGE, null, null, InetAddress.getLocalHost().hostAddress) as String
            try {
                ChatClient(name, serverName)
            } catch (ex: Exception) {
                // ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "An error occurred! (it may be a wrong ip address, which is unreachable.) Restart and try again!",
                        "Error!", JOptionPane.ERROR_MESSAGE)
                exitProcess(-1)
            }

        } // end of main
    }
} //  end of client