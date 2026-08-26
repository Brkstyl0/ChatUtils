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

public class BanCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;
    public static final long MAX_TEMP_BAN_MILLIS = 30L * 24 * 60 * 60 * 1000L; // 30 Gün

    private static final List<String> SHORT_DURATION_SUGGESTIONS = List.of(
            "1h", "6h", "12h", "1d", "3d", "7d", "14d", "30d"
    );
    private static final List<String> ALL_DURATION_SUGGESTIONS = List.of(
            "1h", "6h", "12h", "1d", "3d", "7d", "14d", "30d", "60d", "90d", "kalici"
    );

    public BanCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chatutils.ban")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("ban-usage"));
            return true;
        }

        String targetName = args[0];

        if (plugin.getPunishmentManager().isBanned(targetName)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("already-banned"));
            return true;
        }

        Long durationMillis = -1L; // Varsayılan kalıcı
        int reasonStartIndex = 1;

        if (args.length >= 2) {
            Long parsed = TimeUtil.parseDuration(args[1]);
            if (parsed != null) {
                durationMillis = parsed;
                reasonStartIndex = 2;
            }
        }

        // 30 günden fazla veya kalıcı banlar için kalıcı ban yetkisi kontrolü
        boolean isPermanentOrOver30d = (durationMillis == -1L || durationMillis > MAX_TEMP_BAN_MILLIS);
        if (isPermanentOrOver30d && !sender.hasPermission("chatutils.ban.permanent")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("ban-no-permanent-permission"));
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
            reason = plugin.getConfigManager().getConfig().getString("punishments.default-ban-reason", "Sunucu kurallarını ihlal etmek");
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

        // Hedef oyuncu açıksa sunucudan at
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
            sender.sendMessage(plugin.getConfigManager().getPrefix() + "§a" + finalTargetName + " sunucudan yasaklandı. Süre: " + readableDuration + " Sebep: " + reason);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("chatutils.ban")) {
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
            List<String> durationList = sender.hasPermission("chatutils.ban.permanent") ? ALL_DURATION_SUGGESTIONS : SHORT_DURATION_SUGGESTIONS;
            return durationList.stream()
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
