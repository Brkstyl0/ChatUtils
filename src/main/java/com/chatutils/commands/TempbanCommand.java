package com.chatutils.commands;

import com.chatutils.ChatUtils;
import com.chatutils.data.Punishment;
import com.chatutils.data.PunishmentType;
import com.chatutils.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class TempbanCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;
    private static final List<String> DURATION_SUGGESTIONS = List.of(
            "1h", "6h", "12h", "1d", "3d", "7d", "14d", "30d"
    );

    public TempbanCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chatutils.tempban")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("tempban-usage"));
            return true;
        }

        String targetName = args[0];
        Long durationMillis = TimeUtil.parseDuration(args[1]);

        if (durationMillis == null || durationMillis <= 0) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("invalid-time"));
            return true;
        }

        if (plugin.getPunishmentManager().isBanned(targetName)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("already-banned"));
            return true;
        }

        String reason;
        if (args.length > 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }
            reason = sb.toString().trim();
        } else {
            reason = plugin.getConfigManager().getConfig().getString("punishments.default-ban-reason", "Sunucu kurallarını ihlal etmek");
        }

        Player targetPlayer = Bukkit.getPlayerExact(targetName);
        UUID targetUuid = targetPlayer != null ? targetPlayer.getUniqueId() : null;
        String finalTargetName = targetPlayer != null ? targetPlayer.getName() : targetName;

        long start = System.currentTimeMillis();
        long end = start + durationMillis;

        Punishment punishment = new Punishment(
                targetUuid,
                finalTargetName,
                sender.getName(),
                reason,
                start,
                end,
                PunishmentType.BAN
        );

        plugin.getPunishmentManager().addBan(punishment);

        String readableDuration = TimeUtil.formatDuration(durationMillis);
        String formattedDate = TimeUtil.formatDate(start, plugin.getConfigManager().getConfig().getString("date-format"));

        Map<String, String> placeholders = Map.of(
                "target", finalTargetName,
                "staff", sender.getName(),
                "duration", readableDuration,
                "reason", reason,
                "date", formattedDate,
                "time", readableDuration,
                "remaining", readableDuration
        );

        if (targetPlayer != null && targetPlayer.isOnline()) {
            List<String> kickScreen = plugin.getConfigManager().getMessageList("ban-screen", placeholders);
            String kickMessage = String.join("\n", kickScreen);
            targetPlayer.kickPlayer(kickMessage);
        }

        if (plugin.getConfigManager().getConfig().getBoolean("punishments.broadcast-ban", true)) {
            List<String> broadcastLines = plugin.getConfigManager().getMessageList("ban-broadcast", placeholders);
            for (String line : broadcastLines) {
                Bukkit.broadcastMessage(line);
            }
        } else {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + "§a" + finalTargetName + " süreli olarak yasaklandı. Süre: " + readableDuration + " Sebep: " + reason);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("chatutils.tempban")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String input = args[1].toLowerCase();
            return DURATION_SUGGESTIONS.stream()
                    .filter(d -> d.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }

        if (args.length == 3) {
            String input = args[2].toLowerCase();
            List<String> reasons = plugin.getConfigManager().getSuggestedReasons("ban");
            if (reasons == null || reasons.isEmpty()) {
                reasons = List.of("Hile / 3. Parti Yazılım", "Griefing", "Dolandırıcılık", "Bug Kullanımı", "Ağır Küfür", "Reklam");
            }
            return reasons.stream()
                    .filter(r -> r.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
