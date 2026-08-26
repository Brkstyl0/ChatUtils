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

public class UnvoicemuteCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;

    public UnvoicemuteCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chatutils.unvoicemute")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("unvoicemute-usage"));
            return true;
        }

        String targetInput = args[0].trim();
        Player targetPlayer = Bukkit.getPlayerExact(targetInput);
        if (targetPlayer == null) {
            targetPlayer = Bukkit.getPlayer(targetInput);
        }

        Punishment voiceMute = plugin.getPunishmentManager().getVoiceMute(targetInput);
        if (voiceMute == null && targetPlayer != null) {
            voiceMute = plugin.getPunishmentManager().getVoiceMute(targetPlayer.getName());
            if (voiceMute == null) {
                voiceMute = plugin.getPunishmentManager().getVoiceMute(targetPlayer.getUniqueId());
            }
        }

        if (voiceMute == null && !plugin.getPunishmentManager().isVoiceMuted(targetInput)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("not-voicemuted"));
            return true;
        }

        String finalTargetName = (voiceMute != null) ? voiceMute.getTargetName() : (targetPlayer != null ? targetPlayer.getName() : targetInput);

        plugin.getPunishmentManager().removeVoiceMute(finalTargetName);
        plugin.getPunishmentManager().removeVoiceMute(targetInput);
        if (targetPlayer != null) {
            plugin.getPunishmentManager().removeVoiceMute(targetPlayer.getUniqueId());
        }

        Map<String, String> placeholders = Map.of(
                "target", finalTargetName,
                "staff", sender.getName()
        );

        if (plugin.getConfigManager().getConfig().getBoolean("punishments.broadcast-unvoicemute", true)) {
            List<String> lines = plugin.getConfigManager().getMessageList("unvoicemute-broadcast", placeholders);
            for (String line : lines) {
                Bukkit.broadcastMessage(line);
            }
        } else {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + "§a" + finalTargetName + " adlı oyuncunun sesli sohbet susturması kaldırıldı.");
        }

        if (targetPlayer != null && targetPlayer.isOnline()) {
            List<String> targetMsg = plugin.getConfigManager().getMessageList("unvoicemuted-notify", placeholders);
            for (String line : targetMsg) {
                targetPlayer.sendMessage(line);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("chatutils.unvoicemute")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            return plugin.getPunishmentManager().getVoiceMutedPlayerNames().stream()
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(input))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
