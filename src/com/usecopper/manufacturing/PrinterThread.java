/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.usecopper.manufacturing;

import biz.iteksolutions.IPrinterOutput;
import biz.iteksolutions.escpos.parser.Command;
import biz.iteksolutions.escpos.parser.CutCommand;
import biz.iteksolutions.escpos.parser.DleCommand;
import biz.iteksolutions.escpos.parser.EscposCommand;
import biz.iteksolutions.escpos.parser.IContentOutput;
import biz.iteksolutions.escpos.parser.InitCommand;
import biz.iteksolutions.escpos.parser.Printer;

import com.fazecast.jSerialComm.SerialPortIOException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author alan
 */
public class PrinterThread extends Thread {

    final private IPrinterOutput display;
    final private InputStream dev_in;
    final private OutputStream dev_out;
    final private boolean verbose;
    final private static byte STATUS_REPLY = (byte)0x12;
            
    public PrinterThread(IPrinterOutput display, 
            InputStream dev_in, OutputStream dev_out) {
        
        this.display = display;
        this.dev_in = dev_in;
        this.dev_out = dev_out;
        this.verbose = false;
    }

    public static void endit(PrinterThread pt) {
        pt.interrupt();
    }
    
    @Override
    public void run() {
        procEscPosStream(display, dev_in, dev_out);
    }

    
    private void guiPrint(String str) {
        if (display == null) {
            return;
        }
        display.setText(str);
    }
   
    private void devlogPrint(String str) {     
        if (!verbose) {
            return;
        }
        System.out.print(str);
    }
    
    private void devlogPrintln(String str) {    
        if (!verbose) {
            return;
        }
        System.out.println(str);
    }    
    
    private void wait_a_little() {
        try{
            Thread.sleep(100);
        }
        catch(InterruptedException e){
            System.out.println(e);
        }  
    }
    
    private void runEchoTest(BufferedReader in) {

        guiPrint("\n## Test 4: Starting Echotest\n"); 

        var rdbuf = new char[8];
        int numRead;
        
        try {
            // try to clear the buffer
            var tries = 30;
            while (tries > 0 && in.ready()) {
                if (tries == 30) {
                    guiPrint("  == clearing buffer\n");
                }
                numRead = in.read(rdbuf, 0, rdbuf.length);
//                guiPrint(String.format("DBG: nr %d : 0x%x\n", numRead, (byte)rdbuf[0]));
                if (numRead == 0) {
                    tries = 0;
                } else {
                    tries--;
                }
            }

            // test write - hopefully doesn't confuse the fw too much
            byte[] reply = { STATUS_REPLY };
            dev_out.write(reply, 0, 1);
            wait_a_little();
            if (in.ready()) {
                numRead = in.read(rdbuf, 0, rdbuf.length);
            } else {
                numRead = 0;
            }
//            guiPrint(String.format("DBG: nr %d : 0x%x\n", numRead, (byte)rdbuf[0]));

            if ((1 == numRead) && (rdbuf[0] == (char)STATUS_REPLY)) {
                guiPrint("\n!! Test 4: Fail Echotest !!\n");    
            } else {
                guiPrint("\n== Test 4: Pass Echotest\n"); 
            }
        } 
        catch (SerialPortIOException sio) {
            // exit loop
            guiPrint("== Port closed\n");
        }
        catch (IOException ex) {
            ex.printStackTrace(System.err);
        }      
    }
    
    /**
     * Process inputs and outputs to a Copper Cord device
     * 
     * @param devin  input from device
     * @param gui    output for panel
     * @param devout output for device responses
     */
    public void procEscPosStream(IPrinterOutput gui, 
            InputStream devin, OutputStream devout) {
        
        BufferedReader in;
        try {        
            in = new BufferedReader(new InputStreamReader(devin, "CP437"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(PrintTestMain.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        Printer po = new Printer();
        var rdbuf = new char[8];

        int icmds = 0;
        int ncmds = 0;
       
        boolean enable_echotest = true;
        boolean echotest_reply_written = false;
        int test_step = 0;
        int numRead;
        try {
            
            while (true) {
                if (!in.ready() && enable_echotest && test_step == 3) {
                    runEchoTest(in);
                    test_step = 0; 
                    continue;
                } else {
                    numRead = in.read(rdbuf, 0, rdbuf.length);
                }
//                guiPrint(String.format("DBG: nr %d : 0x%x\n", numRead, (byte)rdbuf[0]));                
                for (int i = 0; i < numRead; i++) {
                    char ch = rdbuf[i];
                    po.addChar(ch);

                    devlogPrint(String.format(" 0x%02x", (byte)rdbuf[i]));       
                    var pcmds = po.getCommands().size();
                    if (pcmds != ncmds) {
                        devlogPrint(" : "); // parse cmd mark
                        ncmds = pcmds;
                    }
                }                

                for (Command obj : po.popCommands()) {
                    icmds += 1;
                    if (verbose) {
                        System.out.print("\nc" + icmds + " ");
                    }
                    
                    boolean textable = false;
                    if (obj instanceof IContentOutput) {
                        IContentOutput container = (IContentOutput) obj;
                        var txt = container.getText();
                        
                        if (echotest_reply_written && txt.length() == 1 && 
                                txt.charAt(0) == (char)STATUS_REPLY) {
                            guiPrint("\n## Warning: possible echo failure\n");
                        } else {
                            guiPrint(txt);
                        }
                        textable = true;
                        
                        devlogPrintln("recv displayable cmd, " + 
                            txt.length() + " chars");
                        
                        // Search text for key phrase for pass/fail
                        // could search for the QR box top and bottoms at 
                        // the start of lines, which would be more forgiving
                        // than a specific qr code line.
                        final String pass_str = "▀▀▀▀▀▀▀ ▀  ▀▀▀ ▀▀▀      ▀▀ ▀  ▀▀";                        
                        if (txt.length() >= pass_str.length()) {
                            if (txt.contains(pass_str)) {
                                guiPrint("\n== Test 2: Pass Output Test\n");
                                test_step += 1;                                
                                echotest_reply_written = false; // reset echo reply test
                            }
                        }
                    }                 

                    if (obj instanceof InitCommand) {
                        devlogPrintln("recv init cmd");

                    } else if (obj instanceof DleCommand) {
                        var cmd = (DleCommand)obj;
                        byte sc = cmd.getSubcmd();
                        byte nv = cmd.getNval();
                        
                        var msg = String.format("recv dle cmd %x %x", sc, nv);
                        devlogPrintln(msg);

                        byte[] reply = { STATUS_REPLY };
                        devout.write(reply, 0, 1);
                        devlogPrintln("  reply wrtten");

                        echotest_reply_written = true;
                        
                        if (cmd.getNval() == 4) {
                            guiPrint("\n== Test 1: Pass, Printer Initialized\n");
                            test_step = 1;
                        }
                    } else if (obj instanceof EscposCommand) {
                        devlogPrintln("recv excpos cmd");

                    } else if (obj instanceof CutCommand) {
                        guiPrint("\n== Test 3: Pass, Received Cut Cmd\n");
                        test_step += 1;
                        
                    } else if (!textable) {
                        // no action needed
                        String cmdtype;
                        if (obj == null) {
                            cmdtype = "Nullobj";
                        } else {
                            cmdtype = obj.getClass().getSimpleName();
                        }
                        devlogPrintln("recv unhandled cmd " + cmdtype);
                    }
                } // for cmd : Commands
                
            } // while (true)  
        }
        catch (SerialPortIOException sio) {
            // exit loop
            guiPrint("== Port closed\n");
        }
        catch (IOException ex) {
            ex.printStackTrace(System.err);
        } 
//       catch (InterruptedException ie) {
//            // exit loop
//        }
    }
}

