package com.chatutils.commands;

import com.chatutils.ChatUtils;
import com.chatutils.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KickCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;

    public KickCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chatutils.kick")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("kick-usage"));
            return true;
        }

        String targetName = args[0];
        Player targetPlayer = Bukkit.getPlayerExact(targetName);
        if (targetPlayer == null) {
            targetPlayer = Bukkit.getPlayer(targetName);
        }

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("player-not-found"));
            return true;
        }

        if (sender instanceof Player && targetPlayer.getUniqueId().equals(((Player) sender).getUniqueId())) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("cant-kick-self"));
            return true;
        }

        if (targetPlayer.hasPermission("chatutils.bypass.kick") && !sender.isOp() && !sender.getName().equalsIgnoreCase("CONSOLE")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("cant-kick-immune"));
            return true;
        }

        String reason;
        if (args.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }
            reason = sb.toString().trim();
        } else {
            reason = plugin.getConfigManager().getConfig().getString("punishments.default-kick-reason", "Sunucu kurallarını ihlal etmek");
        }

        long now = System.currentTimeMillis();
        String formattedDate = TimeUtil.formatDate(now, plugin.getConfigManager().getConfig().getString("date-format"));

        Map<String, String> placeholders = Map.of(
                "target", targetPlayer.getName(),
                "staff", sender.getName(),
                "reason", reason,
                "date", formattedDate
        );

        List<String> kickScreen = plugin.getConfigManager().getMessageList("kick-screen", placeholders);
        String kickMessage = String.join("\n", kickScreen);
        targetPlayer.kickPlayer(kickMessage);

        if (plugin.getConfigManager().getConfig().getBoolean("punishments.broadcast-kick", true)) {
            List<String> broadcastLines = plugin.getConfigManager().getMessageList("kick-broadcast", placeholders);
            for (String line : broadcastLines) {
                Bukkit.broadcastMessage(line);
            }
        } else {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + "§a" + targetPlayer.getName() + " sunucudan atıldı. Sebep: " + reason);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("chatutils.kick")) {
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
            List<String> reasons = plugin.getConfigManager().getSuggestedReasons("kick");
            if (reasons == null || reasons.isEmpty()) {
                reasons = List.of("Uyarı / Kurallara Uyunuz", "Sohbet Düzenini Bozma", "AFK / Hareketsiz Kalma", "Yetkiliyi Meşgul Etme", "Uygunsuz Davranış", "Spam / Flood");
            }
            return reasons.stream()
                    .filter(r -> r.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
