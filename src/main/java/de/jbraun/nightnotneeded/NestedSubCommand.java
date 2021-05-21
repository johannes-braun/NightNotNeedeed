package de.jbraun.nightnotneeded;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class NestedSubCommand extends SubCommand {

    final private HashMap<String, SubCommand> mPossibleSubCommands = new HashMap<>();

    public NestedSubCommand(String action, ArgExecutor onExecute) {
        super(action, onExecute);
    }

    @Override
    public boolean execute(String[] args)
    {
        if(args != null)
        {
            String lowerCase = args[0].toLowerCase();
            if(mPossibleSubCommands.containsKey(lowerCase))
            {
                if(args.length > 1)
                {
                    String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                    if(!mPossibleSubCommands.get(lowerCase).execute(subArgs))
                        return false;
                }
                else
                {
                    if(!mPossibleSubCommands.get(lowerCase).execute(null))
                        return false;
                }
            }
        }
        return super.execute(args);
    }

    public void set(String subAction, SubCommand subCommand)
    {
        mPossibleSubCommands.put(subAction.toLowerCase(), subCommand);
    }

    @Override
    public List<String> onResolveParameters(String[] args)
    {
        if(args.length == 0)
            return new ArrayList<>();
        if(args.length == 1)
        {
            final List<String> suitable = new ArrayList<>();
            for(final HashMap.Entry<String, SubCommand> cmd : mPossibleSubCommands.entrySet())
                if(cmd.getKey().startsWith(args[0].toLowerCase()))
                    suitable.add(cmd.getKey());
            return suitable;
        }
        final String lowerCase = args[0].toLowerCase();
        if(mPossibleSubCommands.containsKey(lowerCase))
        {
            final String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            return mPossibleSubCommands.get(lowerCase).onResolveParameters(subArgs);
        }
        return new ArrayList<>();
    }
    
}
