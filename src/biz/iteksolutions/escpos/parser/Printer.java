package biz.iteksolutions.escpos.parser;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Printer extends Command {

    public static final int CUT_FULL = 48;
    public static final int CUT_HALF = 49;
    public static final int CUT_FULL_FEED = 65;
    public static final int CUT_HALF_FEED = 66;

    private List<Command> commands;
    private Map<Character, String> search;
    private List<Character> searchStack;
    private Command candidate;
    
    public Printer() {
        commands = new ArrayList<>();
        searchStack = new ArrayList<>();
        reset();
    }

    public List<Command> getCommands() {
        return commands;
    }

    /// Return only completed commands
    public List<Command> popCommands() {
        List<Command> out = new ArrayList<>();      
        while (commands.size() > 0) {
            var cmd = commands.get(0);
            if (cmd.done()) {
                out.add(cmd);
                commands.remove(0);
            } else {
                break;
            }
        }
        return out;
    }
    
    public void reset() {
        search = Command.commandsMap;
        searchStack.clear();
        candidate = null;
    }

    private void pushCandidate() {
        commands.add(candidate);
        reset(); // nulls candidate 
    }
    
    @Override
    public boolean addChar(Character c) {
        searchStack.add(c);
        if (candidate != null) {
            if (candidate.addChar(c)) {
                if (candidate.done()) {
                    pushCandidate();
                }
                return true;
            } else {
                // rejected char (mostly for text candidate)
                pushCandidate();
                searchStack.add(c); // re-add char
            }
        }
        
        if (!search.keySet().contains(c)) {
            var tc = new TextCommand();
            handleTextCommand(tc);
            candidate = tc;
            candidate.addChar(c);
            return true;
        }
        
        String type = search.get(c);
        if (type.indexOf("Arr") > 0) {
            switch (type) {
                case Command.ESC_COMMAND_ARR:
                    search = Command.escCommandsMap;
                    break;
                case Command.GS_COMMAND_ARR:
                    search = Command.gsCommandsMap;
                    break;
                case Command.GS_OPEN_BRACKET_ARR:
                    search = Command.gsOpenBracketCommandsMap;
                    break;
            }
            return true;
        }

        try {
            Class<?> clazz = Class.forName(type);
            //Constructor<?> ctor = clazz.getConstructor(String.class);
            Constructor<?> ctor = clazz.getConstructor();
            Command cmd = (Command) ctor.newInstance();

            if (cmd.done()) {
                commands.add(cmd);
                reset();
                return true;
            } else {
                candidate = cmd;
                return false;
            }
        } catch (Exception e) {
            System.out.println("Class has problem: " + type);
            e.printStackTrace();
        }

        return false;
    }

    private void handleTextCommand(TextCommand textCommand) {
        //check if TextChineseCommand
        if (commands.size() > 0) {
            Command top = commands.get(commands.size() - 1);
            if (top instanceof TextChineseCommand) {
                textCommand.setChinese(true);
            }
        }
    }

}
