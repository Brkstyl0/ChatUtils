package com.chatutils.commands;

import com.chatutils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;
import java.util.stream.Collectors;

public class UnbanCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;

    public UnbanCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chatutils.unban")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("unban-usage"));
            return true;
        }

        String targetName = args[0];

        if (!plugin.getPunishmentManager().isBanned(targetName)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("not-banned"));
            return true;
        }

        plugin.getPunishmentManager().removeBan(targetName);

        Map<String, String> placeholders = Map.of(
                "target", targetName,
                "staff", sender.getName()
        );

        if (plugin.getConfigManager().getConfig().getBoolean("punishments.broadcast-unban", true)) {
            List<String> lines = plugin.getConfigManager().getMessageList("unban-broadcast", placeholders);
            for (String line : lines) {
                Bukkit.broadcastMessage(line);
            }
        } else {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + "§a" + targetName + " adlı oyuncunun yasağı kaldırıldı.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("chatutils.unban")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return plugin.getPunishmentManager().getBannedPlayerNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
