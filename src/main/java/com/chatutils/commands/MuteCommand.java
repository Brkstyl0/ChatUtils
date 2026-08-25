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

public class MuteCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;
    private static final List<String> DURATION_SUGGESTIONS = List.of(
            "5m", "10m", "15m", "30m", "1h", "2h", "6h", "12h", "1d", "3d", "7d", "30d", "kalici"
    );

    public MuteCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chatutils.mute")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("mute-usage"));
            return true;
        }

        String targetName = args[0];
        String durationStr = args[1];
        Long durationMillis = TimeUtil.parseDuration(durationStr);

        int reasonStartIndex = 2;
        if (durationMillis == null) {
            // Belki süre yazılmadan direkt sebep yazıldı: varsayılan süreyi dene
            durationStr = plugin.getConfigManager().getConfig().getString("punishments.default-mute-duration", "15m");
            durationMillis = TimeUtil.parseDuration(durationStr);
            reasonStartIndex = 1;
        }

        if (durationMillis == null) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("invalid-time"));
            return true;
        }

        String reason;
        if (args.length > reasonStartIndex) {
            StringBuilder sb = new StringBuilder();
            for (int i = reasonStartIndex; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }
            reason = sb.toString().trim();
        } else {
            reason = plugin.getConfigManager().getConfig().getString("punishments.default-mute-reason", "Kurallara uymamak");
        }

        if (plugin.getPunishmentManager().isMuted(targetName)) {
            Punishment current = plugin.getPunishmentManager().getMute(targetName);
            Map<String, String> pl = Map.of("remaining", TimeUtil.formatRemaining(current.getEndTimestamp()));
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("already-muted", pl));
            return true;
        }

        Player targetPlayer = Bukkit.getPlayerExact(targetName);
        UUID targetUuid = targetPlayer != null ? targetPlayer.getUniqueId() : null;
        String finalTargetName = targetPlayer != null ? targetPlayer.getName() : targetName;

        long start = System.currentTimeMillis();
        long end = (durationMillis == -1L) ? -1L : (start + durationMillis);

        Punishment punishment = new Punishment(
                targetUuid,
                finalTargetName,
                sender.getName(),
                reason,
                start,
                end,
                PunishmentType.MUTE
        );

        plugin.getPunishmentManager().addMute(punishment);

        String readableDuration = TimeUtil.formatDuration(durationMillis);
        String formattedDate = TimeUtil.formatDate(start, plugin.getConfigManager().getConfig().getString("date-format"));

        Map<String, String> placeholders = Map.of(
                "target", finalTargetName,
                "staff", sender.getName(),
                "duration", readableDuration,
                "reason", reason,
                "date", formattedDate,
                "time", readableDuration
        );

        if (plugin.getConfigManager().getConfig().getBoolean("punishments.broadcast-mute", true)) {
            List<String> broadcastLines = plugin.getConfigManager().getMessageList("mute-broadcast", placeholders);
            for (String line : broadcastLines) {
                Bukkit.broadcastMessage(line);
            }
        } else {
            // Sadece yetkiliye ve hedefe mesaj at
            sender.sendMessage(plugin.getConfigManager().getPrefix() + "§a" + finalTargetName + " başarıyla " + readableDuration + " boyunca susturuldu. Sebep: " + reason);
            if (targetPlayer != null) {
                List<String> targetMsg = plugin.getConfigManager().getMessageList("muted-chat-attempt", Map.of(
                        "staff", sender.getName(),
                        "remaining", readableDuration,
                        "reason", reason
                ));
                for (String line : targetMsg) {
                    targetPlayer.sendMessage(line);
                }
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("chatutils.mute")) {
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
            List<String> reasons = plugin.getConfigManager().getSuggestedReasons("mute");
            if (reasons == null || reasons.isEmpty()) {
                reasons = List.of("Küfür", "Spam", "Reklam", "Hakaret", "Yetkiliye Saygısızlık");
            }
            return reasons.stream()
                    .filter(r -> r.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
