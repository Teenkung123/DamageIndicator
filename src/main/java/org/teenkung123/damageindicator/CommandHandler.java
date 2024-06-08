package org.teenkung123.damageindicator;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandHandler implements CommandExecutor {

    private final DamageIndicator plugin;

    public CommandHandler(DamageIndicator plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("damageindicator.admin")) {
            plugin.loadData();
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Successfully reloaded the Damage Indicator Plugin"));
        }
        return false;
    }
}
