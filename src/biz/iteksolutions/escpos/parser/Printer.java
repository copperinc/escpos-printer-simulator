package biz.iteksolutions.escpos.parser;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Printer extends Command {
    
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
    public boolean addChar(Character ch) {
        searchStack.add(ch);
        if (candidate != null) {
            if (candidate.addChar(ch)) {
                if (candidate.done()) {
                    pushCandidate();
                }
                return true;
            } else {
                // rejected char (mostly for text candidate)
                if (candidate.done()) {
                    pushCandidate();
                } else {
                    reset();
                }
                searchStack.add(ch); // re-add char, pushCand clears search stack
            }
        }
        
        if (!search.keySet().contains(ch)) {
            var tc = new TextCommand();
            handleTextCommand(tc);
            candidate = tc;
            candidate.addChar(ch);
            return true;
        }
        
        String type = search.get(ch);
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
