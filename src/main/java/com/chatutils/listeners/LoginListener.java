package com.chatutils.listeners;

import com.chatutils.ChatUtils;
import com.chatutils.data.Punishment;
import com.chatutils.utils.TimeUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.List;
import java.util.Map;

public class LoginListener implements Listener {

    private final ChatUtils plugin;

    public LoginListener(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        String playerName = event.getName();

        Punishment ban = plugin.getPunishmentManager().getBan(playerName);
        if (ban != null) {
            String remaining = TimeUtil.formatRemaining(ban.getEndTimestamp());
            String date = TimeUtil.formatDate(ban.getStartTimestamp(), plugin.getConfigManager().getConfig().getString("date-format"));

            Map<String, String> placeholders = Map.of(
                    "target", playerName,
                    "staff", ban.getStaffName(),
                    "reason", ban.getReason(),
                    "remaining", remaining,
                    "date", date
            );

            List<String> screenLines = plugin.getConfigManager().getMessageList("ban-screen", placeholders);
            String kickMessage = String.join("\n", screenLines);

            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, kickMessage);
        }
    }
}
