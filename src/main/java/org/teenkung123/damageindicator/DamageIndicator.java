package org.teenkung123.damageindicator;

import com.maximde.hologramlib.HologramLib;
import com.maximde.hologramlib.hologram.HologramManager;
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
import org.teenkung123.damageindicator.Utils.HealthBarBatchUpdater;
import org.teenkung123.damageindicator.Utils.HoloDisplays;
import org.teenkung123.damageindicator.api.DamageIndicatorAPI;
import org.teenkung123.damageindicator.manager.PlaceholderManager;

public final class DamageIndicator extends JavaPlugin {

    private ConfigLoader configLoader;
    private HoloDisplays holoDisplays;
    private HealthBarBatchUpdater healthBarBatchUpdater;
    private PlaceholderManager placeholderManager;
    private DamageIndicatorAPI api;
    private boolean usePlaceholderAPI;
    private boolean useMythicMobs;
    private boolean useMyPet;
    private MythicBukkit mythicBukkit;
    private HologramManager hologramManager;

    @Override
    public void onLoad() {
        HologramLib.onLoad(this);
    }

    @Override
    public void onEnable() {
        usePlaceholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        useMythicMobs = Bukkit.getPluginManager().isPluginEnabled("MythicMobs");
        useMyPet = Bukkit.getPluginManager().isPluginEnabled("MyPet");
        placeholderManager = new PlaceholderManager();
        api = new DamageIndicatorAPI(this);
        HologramLib.getManager().ifPresentOrElse(
                manager -> hologramManager = manager,
                () -> getLogger().severe("Failed to initialize HologramLib manager.")
        );
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
        scheduleAutoDelete();
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
    public HealthBarBatchUpdater getHealthBarBatchUpdater() { return healthBarBatchUpdater; }
    public PlaceholderManager getPlaceholderManager() { return placeholderManager; }
    public DamageIndicatorAPI getApi() { return api; }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public void loadData() {
        if (healthBarBatchUpdater != null) healthBarBatchUpdater.stopBatchUpdating();
        this.reloadConfig();
        configLoader = new ConfigLoader(this);
        holoDisplays = new HoloDisplays(this, placeholderManager);
        healthBarBatchUpdater = new HealthBarBatchUpdater(this, configLoader, holoDisplays);
        if (useMythicMobs) {
            mythicBukkit = MythicBukkit.inst();
        }

        if (configLoader.getHealthBarAlwaysVisible()) {
            healthBarBatchUpdater.startBatchUpdating();
        }

    }

    public void removeHolograms() {
        // Remove holograms
        for (String holo : hologramManager.getHologramIds()) {
            if (holo.startsWith("damageindicator_")) {
                hologramManager.remove(holo);
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

    public void scheduleAutoDelete() {
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (String holoId : hologramManager.getHologramIds()) {
                if (holoId.startsWith("damageindicator_health_")) {
                    hologramManager.remove(holoId);
                }
            }
        }, 20*300L, 20*300L);
    }

    public boolean getUseMyPet() {
        return useMyPet;
    }
}
