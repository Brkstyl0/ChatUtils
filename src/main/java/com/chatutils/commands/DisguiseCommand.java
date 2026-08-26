package com.chatutils.commands;

import com.chatutils.ChatUtils;
import com.chatutils.hook.LuckPermsHook;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class DisguiseCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;

    public DisguiseCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("chatutils.disguise")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("disguise-usage"));
            return true;
        }

        String targetName = args[0].trim();
        String targetRank = (args.length >= 2) ? args[1].trim() : null;

        // Geçersiz karakter kontrolü
        if (!targetName.matches("^[a-zA-Z0-9_]{3,16}$")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("disguise-invalid-name"));
            return true;
        }

        player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("disguise-loading", Map.of("name", targetName)));

        plugin.getDisguiseManager().disguise(player, targetName, targetRank, success -> {
            if (success) {
                Map<String, String> pl = Map.of(
                        "name", targetName,
                        "rank", (targetRank != null ? targetRank : "Varsayılan")
                );
                player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("disguise-success", pl));
            } else {
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§cDisguise uygulanırken bir sorun oluştu.");
            }
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("chatutils.disguise")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            List<String> names = plugin.getConfigManager().getConfig().getStringList("disguise.suggested-names");
            if (names.isEmpty()) {
                names = List.of("Steve", "Alex", "GamerPro", "KralOyuncu", "Warrior", "Legend");
            }
            return names.stream()
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(input))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String input = args[1].toLowerCase(Locale.ROOT);
            List<String> groups = new ArrayList<>();
            if (LuckPermsHook.isAvailable()) {
                groups.addAll(LuckPermsHook.getGroupNames());
            }
            if (groups.isEmpty()) {
                groups.addAll(List.of("default", "vip", "vip+", "mvp", "oyuncu"));
            }
            return groups.stream()
                    .filter(g -> g.toLowerCase(Locale.ROOT).startsWith(input))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
