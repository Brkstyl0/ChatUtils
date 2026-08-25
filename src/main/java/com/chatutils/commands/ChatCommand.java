package com.chatutils.commands;

import com.chatutils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChatCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;
    private static final List<String> SUB_COMMANDS = List.of("kapat", "aç", "toggle", "durum", "temizle");

    public ChatCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chatutils.chat.toggle")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("chat-usage"));
            return true;
        }

        String sub = args[0].toLowerCase();
        Map<String, String> placeholders = Map.of("staff", sender.getName());

        switch (sub) {
            case "kapat":
            case "lock":
            case "close":
                if (plugin.getPunishmentManager().isChatLocked()) {
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + "§c[!] Sohbet zaten kilitli durumda!");
                    return true;
                }
                plugin.getPunishmentManager().setChatLocked(true);
                for (String line : plugin.getConfigManager().getMessageList("chat-locked-broadcast", placeholders)) {
                    Bukkit.broadcastMessage(line);
                }
                break;

            case "aç":
            case "ac":
            case "unlock":
            case "open":
                if (!plugin.getPunishmentManager().isChatLocked()) {
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + "§c[!] Sohbet zaten açık durumda!");
                    return true;
                }
                plugin.getPunishmentManager().setChatLocked(false);
                for (String line : plugin.getConfigManager().getMessageList("chat-unlocked-broadcast", placeholders)) {
                    Bukkit.broadcastMessage(line);
                }
                break;

            case "toggle":
                boolean newState = !plugin.getPunishmentManager().isChatLocked();
                plugin.getPunishmentManager().setChatLocked(newState);
                String msgPath = newState ? "chat-locked-broadcast" : "chat-unlocked-broadcast";
                for (String line : plugin.getConfigManager().getMessageList(msgPath, placeholders)) {
                    Bukkit.broadcastMessage(line);
                }
                break;

            case "durum":
            case "status":
                if (plugin.getPunishmentManager().isChatLocked()) {
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("chat-status-locked"));
                } else {
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("chat-status-unlocked"));
                }
                break;

            case "temizle":
            case "clear":
                if (!sender.hasPermission("chatutils.chat.clear")) {
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                ClearChatCommand.executeClear(plugin, sender);
                break;

            default:
                sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("chat-usage"));
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("chatutils.chat.toggle")) {
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
