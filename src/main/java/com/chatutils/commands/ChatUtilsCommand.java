package com.chatutils.commands;

import com.chatutils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChatUtilsCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;
    private static final List<String> SUB_COMMANDS = List.of("reload", "version", "info");

    public ChatUtilsCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chatutils.admin") && !sender.hasPermission("chatutils.reload")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("version")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + "§eKullanım: §6/chatutils reload");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            long startTime = System.currentTimeMillis();

            plugin.getConfigManager().load();
            plugin.getPunishmentManager().loadData();

            long elapsed = System.currentTimeMillis() - startTime;
            Map<String, String> placeholders = Map.of("time", String.valueOf(elapsed));

            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("plugin-reloaded", placeholders));
            return true;
        }

        sender.sendMessage(plugin.getConfigManager().getPrefix() + "§eKullanım: §6/chatutils reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("chatutils.admin") && !sender.hasPermission("chatutils.reload")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return SUB_COMMANDS.stream()
                    .filter(s -> s.startsWith(input))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
