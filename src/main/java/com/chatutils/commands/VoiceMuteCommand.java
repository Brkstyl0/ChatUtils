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

public class VoiceMuteCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;
    public static final long MAX_TEMP_VOICE_MUTE_MILLIS = 30L * 24 * 60 * 60 * 1000L; // 30 Gün

    private static final List<String> SHORT_DURATION_SUGGESTIONS = List.of(
            "5m", "15m", "30m", "1h", "2h", "6h", "12h", "1d", "3d", "7d", "14d", "30d"
    );
    private static final List<String> ALL_DURATION_SUGGESTIONS = List.of(
            "5m", "15m", "30m", "1h", "2h", "6h", "12h", "1d", "3d", "7d", "14d", "30d", "60d", "90d", "kalici"
    );

    public VoiceMuteCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chatutils.voicemute")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("voicemute-usage"));
            return true;
        }

        String targetName = args[0];

        if (plugin.getPunishmentManager().isVoiceMuted(targetName)) {
            Punishment current = plugin.getPunishmentManager().getVoiceMute(targetName);
            String remaining = (current != null) ? TimeUtil.formatRemaining(current.getEndTimestamp()) : "Bilinmiyor";
            Map<String, String> pl = Map.of("remaining", remaining);
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("already-voicemuted", pl));
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

        // 30 günden fazla veya kalıcı ses susturması için kalıcı yetkisi kontrolü
        boolean isPermanentOrOver30d = (durationMillis == -1L || durationMillis > MAX_TEMP_VOICE_MUTE_MILLIS);
        if (isPermanentOrOver30d && !sender.hasPermission("chatutils.voicemute.permanent")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("voicemute-no-permanent-permission"));
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
            reason = plugin.getConfigManager().getConfig().getString("punishments.default-voicemute-reason", "Mikrofonu kötüye kullanmak");
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
                PunishmentType.VOICE_MUTE
        );

        plugin.getPunishmentManager().addVoiceMute(punishment);

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

        if (plugin.getConfigManager().getConfig().getBoolean("punishments.broadcast-voicemute", true)) {
            List<String> broadcastLines = plugin.getConfigManager().getMessageList("voicemute-broadcast", placeholders);
            for (String line : broadcastLines) {
                Bukkit.broadcastMessage(line);
            }
        } else {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + "§a" + finalTargetName + " başarıyla sesli sohbetten susturuldu. Süre: " + readableDuration + " Sebep: " + reason);
        }

        if (targetPlayer != null && targetPlayer.isOnline()) {
            List<String> targetMsg = plugin.getConfigManager().getMessageList("voicemuted-notify", placeholders);
            for (String line : targetMsg) {
                targetPlayer.sendMessage(line);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("chatutils.voicemute")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(input))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String input = args[1].toLowerCase(Locale.ROOT);
            List<String> durationList = sender.hasPermission("chatutils.voicemute.permanent") ? ALL_DURATION_SUGGESTIONS : SHORT_DURATION_SUGGESTIONS;
            return durationList.stream()
                    .filter(d -> d.toLowerCase(Locale.ROOT).startsWith(input))
                    .collect(Collectors.toList());
        }

        if (args.length == 3) {
            String input = args[2].toLowerCase(Locale.ROOT);
            List<String> reasons = plugin.getConfigManager().getSuggestedReasons("voicemute");
            if (reasons == null || reasons.isEmpty()) {
                reasons = List.of("Mikrofon Basma", "Seste Küfür", "Soundpad / Rahatsız Edici Ses", "Cızırtı / Uygunsuz Ses", "Seste Reklam");
            }
            return reasons.stream()
                    .filter(r -> r.toLowerCase(Locale.ROOT).startsWith(input))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
