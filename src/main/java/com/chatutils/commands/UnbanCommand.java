package com.chatutils.commands;

import com.chatutils.ChatUtils;
import com.chatutils.data.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

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

        String targetInput = args[0].trim();
        Player targetPlayer = Bukkit.getPlayerExact(targetInput);
        if (targetPlayer == null) {
            targetPlayer = Bukkit.getPlayer(targetInput);
        }

        Punishment ban = plugin.getPunishmentManager().getBan(targetInput);
        if (ban == null && targetPlayer != null) {
            ban = plugin.getPunishmentManager().getBan(targetPlayer.getName());
        }

        if (ban == null && !plugin.getPunishmentManager().isBanned(targetInput)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("not-banned"));
            return true;
        }

        String finalTargetName = (ban != null) ? ban.getTargetName() : (targetPlayer != null ? targetPlayer.getName() : targetInput);

        plugin.getPunishmentManager().removeBan(finalTargetName);
        plugin.getPunishmentManager().removeBan(targetInput);

        Map<String, String> placeholders = Map.of(
                "target", finalTargetName,
                "staff", sender.getName()
        );

        if (plugin.getConfigManager().getConfig().getBoolean("punishments.broadcast-unban", true)) {
            List<String> lines = plugin.getConfigManager().getMessageList("unban-broadcast", placeholders);
            for (String line : lines) {
                Bukkit.broadcastMessage(line);
            }
        } else {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + "§a" + finalTargetName + " adlı oyuncunun yasağı kaldırıldı.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("chatutils.unban")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            return plugin.getPunishmentManager().getBannedPlayerNames().stream()
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(input))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
