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

        String targetInput = args[0].trim();
        Player targetPlayer = Bukkit.getPlayerExact(targetInput);
        if (targetPlayer == null) {
            targetPlayer = Bukkit.getPlayer(targetInput);
        }

        Punishment mute = plugin.getPunishmentManager().getMute(targetInput);
        if (mute == null && targetPlayer != null) {
            mute = plugin.getPunishmentManager().getMute(targetPlayer.getName());
        }

        if (mute == null && !plugin.getPunishmentManager().isMuted(targetInput)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("not-muted"));
            return true;
        }

        String finalTargetName = (mute != null) ? mute.getTargetName() : (targetPlayer != null ? targetPlayer.getName() : targetInput);

        plugin.getPunishmentManager().removeMute(finalTargetName);
        plugin.getPunishmentManager().removeMute(targetInput);

        Map<String, String> placeholders = Map.of(
                "target", finalTargetName,
                "staff", sender.getName()
        );

        if (plugin.getConfigManager().getConfig().getBoolean("punishments.broadcast-unmute", true)) {
            List<String> lines = plugin.getConfigManager().getMessageList("unmute-broadcast", placeholders);
            for (String line : lines) {
                Bukkit.broadcastMessage(line);
            }
        } else {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + "§a" + finalTargetName + " adlı oyuncunun susturması kaldırıldı.");
        }

        if (targetPlayer != null && targetPlayer.isOnline()) {
            targetPlayer.sendMessage(plugin.getConfigManager().getPrefix() + "§aSusturmanız §f" + sender.getName() + " §atarafından kaldırıldı. Artık sohbete yazabilirsiniz.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("chatutils.unmute")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            return plugin.getPunishmentManager().getMutedPlayerNames().stream()
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(input))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
