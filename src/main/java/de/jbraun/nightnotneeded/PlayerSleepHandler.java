package de.jbraun.nightnotneeded;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

public class PlayerSleepHandler implements Listener {

    private int mNumPlayersAsleep = 0;
    private int mRequiredAsleep = 0;
    private NightNotNeeded mContext;

    public PlayerSleepHandler(NightNotNeeded context)
    {
        mContext = context;
    }

    public boolean shouldSkipNight() {
        return mNumPlayersAsleep > 0 && mNumPlayersAsleep >= mRequiredAsleep;
    }

    @EventHandler
    public void playerEnterSleep(PlayerBedEnterEvent ev)
    {
        if(ev.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK)
        {
            mRequiredAsleep = getNumPlayersRequired();
            ++mNumPlayersAsleep;
            broadcastPlayerAsleepMessage(ev.getPlayer(), mRequiredAsleep);
        }
    }

    private int getNumPlayersRequired() {
        try{
            return mContext.getCurrentConfig().loadRequiredPlayersAsleep();
        } 
        catch(InvalidConfigurationException e)
        {
            Bukkit.getServer().broadcastMessage(ChatColor.RED + "[NightNotNeeded] Plugin encountered an error, please check your config: " + e.getMessage());
            return Bukkit.getOnlinePlayers().size();
        }
    } 

    @EventHandler
    public void playerLeaveSleep(PlayerBedLeaveEvent ev)
    {
        mNumPlayersAsleep = Math.max(0, mNumPlayersAsleep-1);
        if(ev.isCancelled())
        {
            mRequiredAsleep = getNumPlayersRequired();
            broadcastPlayerLeaveMessage(ev.getPlayer(), mRequiredAsleep);
        }
    }

    private void broadcastPlayerLeaveMessage(Player player, int numPlayersRequired)
    {
        final String playerName = player.getName();
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(mNumPlayersAsleep);
        sb.append('/');
        sb.append(numPlayersRequired);
        sb.append("] ");
        sb.append(ChatColor.YELLOW.toString());
        sb.append(playerName);
        sb.append(" does not want so sleep anymore.");
        Bukkit.getServer().broadcastMessage(sb.toString());
    }

    private void broadcastPlayerAsleepMessage(Player player, int numPlayersRequired)
    {
        final String playerName = player.getName();
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(mNumPlayersAsleep);
        sb.append('/');
        sb.append(numPlayersRequired);
        sb.append("] ");
        sb.append(ChatColor.YELLOW.toString());
        sb.append(playerName);
        sb.append(" is now");
        if(mNumPlayersAsleep > 1)
        {
            sb.append(" also");
        }
        sb.append(" trying to sleep.");
        Bukkit.getServer().broadcastMessage(sb.toString());
    }
}
