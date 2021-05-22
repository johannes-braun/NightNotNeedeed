package de.jbraun.nightnotneeded;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {
    private static final String CONFIG_FILE_NAME = "config.yml";
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.ROUND;

    public static final String CFG_KEY_PLAYER_NUMBER_ASLEEP = "number_asleep";
    public static final String CFG_KEY_ROUNDING_MODE        = "rounding_mode";
    public static final String CFG_KEY_OVERWORLD_ONLY       = "overworld_only";

    public static final Pattern BOOLEAN_PATTERN = Pattern.compile("y|Y|yes|Yes|YES|n|N|no|No|NO|true|True|TRUE|false|False|FALSE|on|On|ON|off|Off|OFF");

    public enum RoundingMode
    {
        FLOOR, ROUND, CEIL
    }

    public Config(JavaPlugin context)
    {
        mContext = context;
        mFile = new File(context.getDataFolder() + File.separator + CONFIG_FILE_NAME);

        if(!mFile.exists())
        {
            context.getConfig().options().copyDefaults(true);
            context.saveDefaultConfig();
        }
    }

    public List<String> getPossibleOptions(String option)
    {
        switch(option)
        {
        case CFG_KEY_ROUNDING_MODE:
            {
                List<String> suitable = new ArrayList<>();
                for(RoundingMode mode : RoundingMode.values())
                    suitable.add(mode.toString().toLowerCase());
                return suitable;
            }
        case CFG_KEY_OVERWORLD_ONLY:
            return Arrays.asList("y", "n", "yes", "no", "true", "false", "on", "off");
        }
        return new ArrayList<>();
    }

    public boolean isOptionValid(String option, String value)
    {
        switch(option.toLowerCase())
        {
        case CFG_KEY_ROUNDING_MODE:
            return getPossibleOptions(option).contains(value);
        case CFG_KEY_OVERWORLD_ONLY:
            return BOOLEAN_PATTERN.matcher(value).matches();
        case CFG_KEY_PLAYER_NUMBER_ASLEEP:
        {
            try{ tryParseNumberAsleep(value); return true; }
            catch(InvalidConfigurationException ex) { return false; }
        }
        }
        return false;
    }

    private int tryParseNumberAsleep(String asleepString) throws InvalidConfigurationException {
        asleepString = asleepString.trim();
        if(asleepString == null || asleepString.isEmpty())
            asleepString = "100%";

        boolean stringShouldEnd = false;
        boolean isPercentage = false;
        int endOfNumber = -1;
        for(int i=0; i<asleepString.length(); ++i)
        {
            if(stringShouldEnd)
                throw new InvalidConfigurationException("Found invalid characters after option \"" + CFG_KEY_PLAYER_NUMBER_ASLEEP + "\".");
            final char c = asleepString.charAt(i);
            endOfNumber = i;

            if(!Character.isDigit(c))
            {
                stringShouldEnd = true;
                if(c != '%')
                {
                    throw new InvalidConfigurationException("The value for the \"" + 
                        CFG_KEY_PLAYER_NUMBER_ASLEEP + "\" option should be a number (n) or a percentage (n%).");
                }
                else
                {
                    isPercentage = true;
                }
            }
        }

        final int currentNumPlayers = overworldOnly() ? Bukkit.getServer().getWorlds().get(0).getPlayers().size() : Bukkit.getOnlinePlayers().size();
        final int value = Integer.parseInt(isPercentage ? asleepString.substring(0, endOfNumber) : asleepString);
        return Math.max(1, Math.min(currentNumPlayers, isPercentage ? round(currentNumPlayers * value / 100.0f) : value));
    }

    private boolean overworldOnly()
    {
        return mContext.getConfig().getBoolean(CFG_KEY_OVERWORLD_ONLY, false);
    }

    public int loadRequiredPlayersAsleep() throws InvalidConfigurationException
    {
        mContext.getLogger().info("number_asleep: " + mContext.getConfig().getString(CFG_KEY_PLAYER_NUMBER_ASLEEP));
        mContext.getLogger().info("rounding_mode: " + mContext.getConfig().getString(CFG_KEY_ROUNDING_MODE));
        mContext.getLogger().info("overworld_only: " + mContext.getConfig().getBoolean(CFG_KEY_OVERWORLD_ONLY, false));
        
        final String asleepString = mContext.getConfig().getString(CFG_KEY_PLAYER_NUMBER_ASLEEP);
        return tryParseNumberAsleep(asleepString);
    }

    private RoundingMode loadRoundingMode() {
        try 
        {
            return RoundingMode.valueOf(mContext.getConfig().getString(CFG_KEY_ROUNDING_MODE).toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            return DEFAULT_ROUNDING_MODE;
        }
        catch(NullPointerException e)
        {
            return DEFAULT_ROUNDING_MODE;
        }
    }

    private int round(float unrounded)
    {
        switch(loadRoundingMode())
        {
            case CEIL:
                return (int)Math.ceil(unrounded);
            case FLOOR:
                return (int)Math.floor(unrounded);
            case ROUND:
            default:
                return (int)Math.round(unrounded);
        }
    }

    private JavaPlugin mContext;
    private File mFile;
}
