package com.chatutils.listeners;

import com.chatutils.ChatUtils;
import com.chatutils.data.Punishment;
import com.chatutils.utils.TimeUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.Map;

public class ChatListener implements Listener {

    private final ChatUtils plugin;

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
}
