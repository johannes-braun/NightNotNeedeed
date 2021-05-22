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

    private NightNotNeeded mContext;

    public PlayerSleepHandler(NightNotNeeded context)
    {
        mContext = context;
    }

    public boolean shouldSkipNight() {
        int numAsleep = getNumPlayersSleeping();
        int numRequired = getNumPlayersRequired();
        return numAsleep > 0 && numAsleep >= numRequired;
    }

    private int getNumPlayersSleeping() {
        int counter = 0;
        for(Player p : Bukkit.getOnlinePlayers())
        {
            if(p.isSleeping())
                ++counter;
        }
        return counter;
    }

    @EventHandler
    public void playerEnterSleep(PlayerBedEnterEvent ev)
    {
        if(ev.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK)
        {
            int numRequired = getNumPlayersRequired();
            int numAsleep = getNumPlayersSleeping();
            broadcastPlayerAsleepMessage(ev.getPlayer(), numAsleep+1, numRequired);
        }
    }

    private int getNumPlayersRequired() {
        try{
            return mContext.getCurrentConfig().loadRequiredPlayersAsleep();
        } 
        catch(InvalidConfigurationException e)
        {
            Bukkit.getServer().broadcastMessage(ChatColor.RED + "[NightNotNeeded] Plugin encountered an error, please check your config: " + e.getMessage());
            // Default to "all overworld players...";
            return Bukkit.getServer().getWorlds().get(0).getPlayers().size();
        }
    } 

    private void broadcastPlayerAsleepMessage(Player player, int numPlayersAsleep, int numPlayersRequired)
    {
        final String playerName = player.getName();
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(numPlayersAsleep);
        sb.append('/');
        sb.append(numPlayersRequired);
        sb.append("] ");
        sb.append(ChatColor.YELLOW.toString());
        sb.append(playerName);
        sb.append(" is now");
        if(numPlayersAsleep > 1)
        {
            sb.append(" also");
        }
        sb.append(" trying to sleep.");
        Bukkit.getServer().broadcastMessage(sb.toString());
    }
}
