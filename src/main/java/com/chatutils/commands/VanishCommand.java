package com.chatutils.commands;

import com.chatutils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class VanishCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;

    public VanishCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chatutils.vanish")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        // Kendi üzerinde çalıştırma: /v, /vanish, /v on, /v off
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("only-players"));
                return true;
            }
            Player player = (Player) sender;
            plugin.getVanishManager().toggleVanish(player);
            return true;
        }

        if (args.length == 1) {
            String arg = args[0].toLowerCase(Locale.ROOT);
            if (arg.equals("on") || arg.equals("ac") || arg.equals("aç")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("only-players"));
                    return true;
                }
                Player player = (Player) sender;
                plugin.getVanishManager().setVanished(player, true, false);
                return true;
            }
            if (arg.equals("off") || arg.equals("kapat")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("only-players"));
                    return true;
                }
                Player player = (Player) sender;
                plugin.getVanishManager().setVanished(player, false, false);
                return true;
            }

            // Başka oyuncu için çalıştırma: /v <oyuncu>
            if (!sender.hasPermission("chatutils.vanish.other")) {
                sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("player-not-found"));
                return true;
            }

            plugin.getVanishManager().toggleVanish(target);
            boolean isVanished = plugin.getVanishManager().isVanished(target);
            sender.sendMessage(plugin.getConfigManager().getPrefix() + "§e" + target.getName() + " §7adlı yetkilinin görünmezlik durumu: " + (isVanished ? "§aAKTİF" : "§cPASİF"));
            return true;
        }

        sender.sendMessage(plugin.getConfigManager().getPrefix() + "§eKullanım: §6/" + label + " [on|off|oyuncu]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("chatutils.vanish")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            List<String> suggestions = new ArrayList<>(List.of("on", "off"));
            if (sender.hasPermission("chatutils.vanish.other")) {
                suggestions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            }
            return suggestions.stream()
                    .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(input))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
