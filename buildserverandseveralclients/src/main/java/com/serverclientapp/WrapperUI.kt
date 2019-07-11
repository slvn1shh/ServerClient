package com.serverclientapp

import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

class WrapperUI(frameTitle: String): JFrame(frameTitle), ActionListener{
    private val startServerButton = JButton("Start Server") // todo: add possibility to start Server side with UI
    private val startClientButton = JButton("Start Client") // todo: add possibility to start Client side with UI
    private val stopServerButton = JButton("Stop Server") // todo: add possibility to stop server with UI
    private val stopClientButton = JButton("Stop Client") // todo: add possibility to close connection to the all and only one clients

    fun buildUI(){

        val panel = JPanel(FlowLayout())

        startClientButton.addActionListener(this)
        startServerButton.addActionListener(this)
        stopClientButton.addActionListener(this)
        stopServerButton.addActionListener(this)

        panel.add(startServerButton, BorderLayout.NORTH)
        panel.add(startClientButton, BorderLayout.NORTH)
        panel.add(stopClientButton, BorderLayout.WEST)
        panel.add(stopServerButton, BorderLayout.WEST)
        add(panel)



        isVisible = true
        isAlwaysOnTop = true

        pack()
    }

    override fun actionPerformed(e: ActionEvent?) {
        when(e!!.source){
            startServerButton -> {

            }
            startClientButton -> {

            }
            stopClientButton -> {

            }
            stopServerButton -> {

            }
        }
    }
}

fun main() {
    WrapperUI("MsgSender").buildUI()
}