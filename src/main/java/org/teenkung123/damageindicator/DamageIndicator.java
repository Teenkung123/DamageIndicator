package org.teenkung123.damageindicator;

import eu.decentsoftware.holograms.api.holograms.Hologram;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.teenkung123.damageindicator.Events.DeathEvent;
import org.teenkung123.damageindicator.Events.HealEvent;
import org.teenkung123.damageindicator.Events.MythicMobsDamage;
import org.teenkung123.damageindicator.Events.VanillaEvent;
import org.teenkung123.damageindicator.Loader.ConfigLoader;
import org.teenkung123.damageindicator.Utils.HoloDisplays;

public final class DamageIndicator extends JavaPlugin {

    private ConfigLoader configLoader;
    private HoloDisplays holoDisplays;
    private boolean usePlaceholderAPI;
    private boolean useMythicMobs;
    private MythicBukkit mythicBukkit;


    @Override
    public void onEnable() {
        usePlaceholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        useMythicMobs = Bukkit.getPluginManager().isPluginEnabled("MythicMobs");
        removeHolograms();
        loadData();

        if (useMythicMobs) {
            Bukkit.getPluginManager().registerEvents(new MythicMobsDamage(this), this);
        } else {
            Bukkit.getPluginManager().registerEvents(new VanillaEvent(this), this);
        }

        Bukkit.getPluginManager().registerEvents(new HealEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new DeathEvent(this), this);
        //noinspection DataFlowIssue
        getCommand("DamageIndicator").setExecutor(new CommandHandler(this));
    }

    @Override
    public void onDisable() {
        removeHolograms();
    }

    public ConfigLoader getConfigLoader() { return configLoader; }
    public HoloDisplays getHoloDisplays() { return holoDisplays; }
    public boolean getUsePlaceholderAPI() { return usePlaceholderAPI; }
    public boolean getUseMythicMobs() { return useMythicMobs; }
    public MythicBukkit getMythicBukkit() { return mythicBukkit; }

    public void loadData() {
        this.reloadConfig();
        configLoader = new ConfigLoader(this);
        holoDisplays = new HoloDisplays(this);
        if (useMythicMobs) {
            mythicBukkit = MythicBukkit.inst();
        }
    }

    public void removeHolograms() {
        // Remove holograms
        for (Hologram holo : Hologram.getCachedHolograms()) {
            if (holo.getId().startsWith("DamageIndicator_")) {
                holo.delete();
            }
        }

        // Remove entities from the team
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getMainScoreboard();
        Team damageIndicatorTeam = board.getTeam("DamageIndicatorBars");
        if (damageIndicatorTeam != null) {
            for (String entry : damageIndicatorTeam.getEntries()) {
                damageIndicatorTeam.removeEntry(entry);
            }
        }
    }

}
