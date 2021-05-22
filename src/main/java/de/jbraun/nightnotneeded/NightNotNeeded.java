package de.jbraun.nightnotneeded;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class NightNotNeeded extends JavaPlugin {
    private PlayerSleepHandler mSleepHandler;
    private int mTask;
    private CheckTask mCheckTask;

    private final int TIME_DAY = 0;
    private final int INVALID_TASK = -1;
    private final long TIME_RESET_DELAY_TICKS = 50;
    private final long CHECK_REPEAT_TICKS = 10;
    
    private final String PERM_COMMANDS = "nightnotneeded.commands";

    private final String CMD_BASE = "nnn";
    private final String CMD_RELOAD = "reload";
    private final String CMD_SET = "set";
    private final List<String> ALL_OPTIONS = Arrays.asList(Config.CFG_KEY_PLAYER_NUMBER_ASLEEP, Config.CFG_KEY_ROUNDING_MODE, Config.CFG_KEY_OVERWORLD_ONLY);

    private class CheckTask implements Runnable
    {
        private int mAwaiterTask = INVALID_TASK;

        @Override
        public void run() {
            if(mSleepHandler.shouldSkipNight())
            {
                if(mAwaiterTask == INVALID_TASK)
                {
                    mAwaiterTask = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(getThis(), new Runnable(){
                        @Override
                        public void run() {
                            Bukkit.getServer().getWorlds().get(0).setTime(TIME_DAY);
                            Bukkit.getServer().getWorlds().get(0).setStorm(false);
                            Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + "Good morning world!");
                            mAwaiterTask = INVALID_TASK;
                        }
                    }, TIME_RESET_DELAY_TICKS);
                }
            }
            else
            {
                Bukkit.getServer().getScheduler().cancelTask(mAwaiterTask);
                mAwaiterTask = INVALID_TASK;
            }
        }
    }

    private NestedSubCommand mCommandTree;

    public NightNotNeeded()
    {
        loadConfig();
        mCommandTree = new NestedSubCommand("nnn", null);
        mCommandTree.set(CMD_RELOAD, new SubCommand(CMD_RELOAD, new SubCommand.ArgExecutor(){
            @Override
            public boolean execute(Object sender, String[] args) {
                if(args != null && args.length != 0)
                    return false;
                loadConfig();
                Bukkit.getServer().broadcastMessage("[NNN] Config reloaded.");
                return true;
            }
        }));

        NestedSubCommand setCommand = new NestedSubCommand(CMD_SET, null);
        for(final String opt : ALL_OPTIONS)
        {
            NestedSubCommand optCommand = new NestedSubCommand(opt, new SubCommand.ArgExecutor(){
                @Override
                public boolean execute(Object sender, String[] args) {
                    if(args == null || args.length != 1)
                        return false;
                    if(mConfig.isOptionValid(opt, args[0]))
                    {
                        getConfig().set(opt, args[0]);
                        saveConfig();
                        loadConfig();
                        Bukkit.getServer().broadcastMessage("[NNN] Config value set: " + opt + " = " + args[0]);
                    }
                    else{
                        Bukkit.getServer().broadcastMessage("[NNN] The given value was invalid.");
                    }
                    return true;
                }
            });
            for(final String child : mConfig.getPossibleOptions(opt))
            {
                optCommand.set(child, new SubCommand(child, null));
            }
            setCommand.set(opt, optCommand);
        }

        mCommandTree.set(CMD_SET, setCommand);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        mSleepHandler = new PlayerSleepHandler(this);
        Bukkit.getPluginManager().registerEvents(mSleepHandler, this);
        mCheckTask = new CheckTask();
        mTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, mCheckTask, 0, CHECK_REPEAT_TICKS);
    }

    @Override
    public void onDisable() {
        getCommand(CMD_BASE).setExecutor(null);
        Bukkit.getServer().getScheduler().cancelTask(mTask);
        super.onDisable();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(sender.hasPermission(PERM_COMMANDS))
        {
            return mCommandTree.onResolveParameters(args);
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender.hasPermission(PERM_COMMANDS))
        {
            return mCommandTree.execute(args);
        }
        return false;
    }

    public Config getCurrentConfig() {
        return mConfig;
    }

    private void loadConfig() {
        mConfig = new Config(this);
    }

    /**
     * Why is this needed, Java???? Let me just put this@ParentClass into a child class like Kotlin.
     * @return this.
     */
    private NightNotNeeded getThis() {
        return this;
    }

    Config mConfig;
}
