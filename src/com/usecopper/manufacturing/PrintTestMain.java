/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.usecopper.manufacturing;

import biz.iteksolutions.IPrinterOutput;
import com.fazecast.jSerialComm.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JFrame;


/**
 *
 * @author alan
 */
public class PrintTestMain {
    
    private static IPrinterOutput setupGui() {
        TesterPanel pPanel = new TesterPanel();
        JFrame frame = new JFrame("Copper Cord Print Test");
        frame.setContentPane(pPanel);
        frame.setDefaultCloseOperation((JFrame.EXIT_ON_CLOSE));
        frame.pack();
        frame.setVisible(true);
        
        return pPanel;
    }    
  
    public static void setupPort(SerialPort port) {
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        int baud_rate = 9600;
        port.setBaudRate(baud_rate);
        port.setNumDataBits(8);
        port.setParity(SerialPort.NO_PARITY); // n
        port.setNumStopBits(1);    
    }
    
    /**
     * A "--test" arg will open the selftest-in.bin file
     * and send it through the PrinterThread. Mostly useful for development
     * of the tester itself.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        boolean do_gui = true;
        boolean do_fileio_test = false;
        
        for (var arg : args) {
            if ("--test-no-dev".equals(arg)) {
                do_fileio_test = true;
            }
        }
//        do_fileio_test = true;
        
        try {
            IPrinterOutput gui = null;
            if (do_gui) {
                //gui = setupGui();
                gui = setupGui();
            }

            if (do_fileio_test) {
                File f = new File("selftest-in.bin");
                FileInputStream fin = new FileInputStream(f);
                var out = OutputStream.nullOutputStream();

                var pt = new PrinterThread(gui, fin, out);
                pt.start();
            }  
        } 
        catch (IOException ex) {
            System.out.println("IOException");
            ex.printStackTrace(System.err);
        }
    }
}
