package biz.iteksolutions.escpos.parser;

public class CutCommand extends CommandTwoArgs implements IContentOutput {

    public static final int CUT_FULL = 48;
    public static final int CUT_HALF = 49;
    public static final int CUT_FULL_ALT = 0;
    public static final int CUT_HALF_ALT = 1;    
    public static final int CUT_FULL_FEED = 65;
    public static final int CUT_HALF_FEED = 66;
    
    @Override
    public String getText() {
        int mode = (int) arg1;
        int lines = 0;
        if (arg2 != null) {
            lines = (int) arg2;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines; i++)
            sb.append("\n");
        switch(mode) {
            case CUT_FULL:
            case CUT_FULL_ALT:
            case CUT_FULL_FEED:
                sb.append("<<CUT_FULL>>");   
                break;
                
            case CUT_HALF:
            case CUT_HALF_ALT:                                
            case CUT_HALF_FEED:
                sb.append("<<CUT_HALF>>"); 
                break;
            
            default:
                sb.append("<<CUT_INVALID_CMD>>");
                break;
        }
        return sb.toString();
    }
    
    @Override
    public boolean done() {
        boolean a2_ok = true;
        if (arg1 != null) {
            int mode = (int) arg1;
            if (mode == CUT_FULL_FEED || mode == CUT_FULL_FEED) {
                a2_ok = (arg2 != null);
            } 
        }
        return (arg1 != null && a2_ok);
    }    
}
