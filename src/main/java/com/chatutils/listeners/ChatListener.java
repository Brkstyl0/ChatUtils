package com.chatutils.listeners;

import com.chatutils.ChatUtils;
import com.chatutils.data.Punishment;
import com.chatutils.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ChatListener implements Listener {

    private final ChatUtils plugin;

    private static final Set<String> MSG_COMMANDS = Set.of(
            "msg", "tell", "w", "whisper", "m", "pm", "message", "t", "emsg", "etell", "ewhisper", "epm"
    );

    private static final Set<String> REPLY_COMMANDS = Set.of(
            "r", "reply", "er", "ereply"
    );

    public ChatListener(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // 1. Sohbet Kilidi Kontrolü
        if (plugin.getPunishmentManager().isChatLocked()) {
            if (!player.hasPermission("chatutils.chat.bypass")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("chat-is-locked-warning"));
                return;
            }
        }

        // 2. Mute (Susturma) Kontrolü
        Punishment mute = plugin.getPunishmentManager().getMute(player.getName());
        if (mute != null) {
            event.setCancelled(true);

            String remaining = TimeUtil.formatRemaining(mute.getEndTimestamp());
            Map<String, String> placeholders = Map.of(
                    "target", player.getName(),
                    "staff", mute.getStaffName(),
                    "remaining", remaining,
                    "reason", mute.getReason()
            );

            List<String> warningLines = plugin.getConfigManager().getMessageList("muted-chat-attempt", placeholders);
            for (String line : warningLines) {
                player.sendMessage(line);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (message == null || !message.startsWith("/")) {
            return;
        }

        String raw = message.substring(1).trim();
        if (raw.isEmpty()) {
            return;
        }

        String[] parts = raw.split("\\s+");
        if (parts.length < 2) {
            return;
        }

        String commandName = parts[0].toLowerCase(Locale.ROOT);
        if (commandName.contains(":")) {
            commandName = commandName.substring(commandName.indexOf(':') + 1);
        }

        Player sender = event.getPlayer();

        boolean isMsgCmd = MSG_COMMANDS.contains(commandName);
        boolean isReplyCmd = REPLY_COMMANDS.contains(commandName);

        if (!isMsgCmd && !isReplyCmd) {
            return;
        }

        // 1. Mute Kontrolü (Susturulmuş oyuncular özel mesaj atamaz)
        Punishment mute = plugin.getPunishmentManager().getMute(sender.getName());
        if (mute != null) {
            event.setCancelled(true);
            String remaining = TimeUtil.formatRemaining(mute.getEndTimestamp());
            Map<String, String> placeholders = Map.of(
                    "target", sender.getName(),
                    "staff", mute.getStaffName(),
                    "remaining", remaining,
                    "reason", mute.getReason()
            );
            List<String> warningLines = plugin.getConfigManager().getMessageList("muted-chat-attempt", placeholders);
            for (String line : warningLines) {
                sender.sendMessage(line);
            }
            return;
        }

        // 2. Social Spy: Yetkisi olan yetkililere özel mesajı göster
        if (isMsgCmd && parts.length >= 3) {
            String targetName = parts[1];
            StringBuilder msgBuilder = new StringBuilder();
            for (int i = 2; i < parts.length; i++) {
                msgBuilder.append(parts[i]).append(" ");
            }
            String content = msgBuilder.toString().trim();

            Map<String, String> placeholders = Map.of(
                    "sender", sender.getName(),
                    "receiver", targetName,
                    "target", targetName,
                    "message", content
            );

            String spyMessage = plugin.getConfigManager().getMessage("socialspy-format", placeholders);

            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("chatutils.socialspy") || staff.hasPermission("chatutils.spy")) {
                    if (!staff.equals(sender) && !staff.getName().equalsIgnoreCase(targetName)) {
                        staff.sendMessage(spyMessage);
                    }
                }
            }
        } else if (isReplyCmd && parts.length >= 2) {
            StringBuilder msgBuilder = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                msgBuilder.append(parts[i]).append(" ");
            }
            String content = msgBuilder.toString().trim();

            Map<String, String> placeholders = Map.of(
                    "sender", sender.getName(),
                    "receiver", "Cevap",
                    "target", "Cevap",
                    "message", content
            );

            String spyMessage = plugin.getConfigManager().getMessage("socialspy-format", placeholders);

            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("chatutils.socialspy") || staff.hasPermission("chatutils.spy")) {
                    if (!staff.equals(sender)) {
                        staff.sendMessage(spyMessage);
                    }
                }
            }
        }
    }
}
