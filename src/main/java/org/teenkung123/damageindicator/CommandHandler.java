package org.teenkung123.damageindicator;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandHandler implements CommandExecutor {

    private final DamageIndicator plugin;

    public CommandHandler(DamageIndicator plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("damageindicator.admin")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("analyze")) {
                    plugin.getHealthBarBatchUpdater().debugGatherDataSync((Player) sender);
                    return true;
                }
                if (args[0].equalsIgnoreCase("removeall")) {
                    plugin.getHologramManager().removeAll();
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Successfully removed all holograms"));
                    return true;
                }
            }
            plugin.loadData();
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Successfully reloaded the Damage Indicator Plugin"));
        }
        return true;
    }
}
