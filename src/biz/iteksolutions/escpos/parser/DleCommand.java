/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biz.iteksolutions.escpos.parser;

/**
 *
 * @author alancchen
 */
public class DleCommand extends Command {
    // main command byte 0x10, DLE
    
    public DleCommand() {
        remaining = -1;
    }
    
    static final char SUB_RTSTATUS = 0x04;
    
    int remaining;
    int  ibmax;
    public Character subcmd;
    public Character nval;
    
    public byte getNval() {
        if (nval == null) {
            return 0;
        }
        return (byte)nval.charValue();
    }
    
    public byte getSubcmd() {
        if (subcmd == null) {
            return 0;
        }
        return (byte)subcmd.charValue();
    }
        
    @Override
    public boolean addChar(Character c) {
        
        byte bc = (byte)c.charValue();
        switch(remaining) {
            case -1: 
                subcmd = c;
                remaining = 2;
                break;
            case 1:
                nval = c;
                //TODO: certain nvals require more bytes
                //     7, 8, 18
                break;
        }
        remaining -= 1;
//        System.out.print(
//                String.format(" (dle addchar 0x%02x ib %d) ", bc, remaining));
        return (remaining > 0);
    }  
    
    @Override
    public boolean done() {
        return (remaining == 0);
    }
}
