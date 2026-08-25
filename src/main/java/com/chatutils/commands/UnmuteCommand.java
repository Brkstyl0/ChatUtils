package com.chatutils.commands;

import com.chatutils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;
import java.util.stream.Collectors;

public class UnmuteCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;

    public UnmuteCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chatutils.unmute")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("unmute-usage"));
            return true;
        }

        String targetName = args[0];

        if (!plugin.getPunishmentManager().isMuted(targetName)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("not-muted"));
            return true;
        }

        plugin.getPunishmentManager().removeMute(targetName);

        Map<String, String> placeholders = Map.of(
                "target", targetName,
                "staff", sender.getName()
        );

        if (plugin.getConfigManager().getConfig().getBoolean("punishments.broadcast-unmute", true)) {
            List<String> lines = plugin.getConfigManager().getMessageList("unmute-broadcast", placeholders);
            for (String line : lines) {
                Bukkit.broadcastMessage(line);
            }
        } else {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + "§a" + targetName + " adlı oyuncunun susturması kaldırıldı.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("chatutils.unmute")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return plugin.getPunishmentManager().getMutedPlayerNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
