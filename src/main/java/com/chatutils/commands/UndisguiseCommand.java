package com.chatutils.commands;

import com.chatutils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class UndisguiseCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;

    public UndisguiseCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("chatutils.undisguise") && !player.hasPermission("chatutils.disguise")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (!plugin.getDisguiseManager().isDisguised(player)) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("not-disguised"));
            return true;
        }

        boolean removed = plugin.getDisguiseManager().undisguise(player);
        if (removed) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("undisguise-success"));
        } else {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cDisguise kaldırılamadı.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
