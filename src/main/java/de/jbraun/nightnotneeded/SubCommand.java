package de.jbraun.nightnotneeded;

import java.util.ArrayList;
import java.util.List;

public class SubCommand {
    public interface ArgExecutor
    {
        boolean execute(Object sender, String[] args);
    }

    private String mAction;
    private ArgExecutor mArgExecutor;
    
    public SubCommand(String action, ArgExecutor onExecute)
    {
        mAction = action.toLowerCase();
        mArgExecutor = onExecute;
    }

    public String getAction() {
        return mAction;
    }

    public List<String> onResolveParameters(String[] args)
    {
        return new ArrayList<>();
    }

    public boolean execute(String[] args)
    {
        if(mArgExecutor != null)
            return mArgExecutor.execute(this, args);
        return true;
    }
}
